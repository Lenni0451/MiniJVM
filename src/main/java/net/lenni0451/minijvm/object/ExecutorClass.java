package net.lenni0451.minijvm.object;

import lombok.SneakyThrows;
import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.lenni0451.commons.asm.Types.type;

public class ExecutorClass {

    private final ClassNode classNode;
    final Map<String, ExecutorClass> superClasses;
    private final Map<FieldNode, StackElement> staticFields;

    public ExecutorClass(final ExecutionManager executionManager, final ExecutionContext executionContext, final ClassNode classNode) {
        this.classNode = classNode;
        this.superClasses = new LinkedHashMap<>();
        this.staticFields = new HashMap<>();

        this.initSuperClasses(executionManager, executionContext);
        this.initFields(executionManager, executionContext);
    }

    @SneakyThrows
    private void initSuperClasses(final ExecutionManager executionManager, final ExecutionContext executionContext) {
        this.superClasses.put(this.classNode.name, this);
        for (String itf : this.classNode.interfaces) {
            this.superClasses.put(itf, executionManager.loadClass(executionContext, itf));
        }

        ExecutorClass superClass = this;
        while (superClass.classNode.superName != null) {
            superClass = executionManager.loadClass(executionContext, superClass.classNode.superName);
            this.superClasses.putAll(superClass.superClasses);
        }
    }

    private void initFields(final ExecutionManager executionManager, final ExecutionContext executionContext) {
        for (FieldNode field : this.classNode.fields) {
            if (Modifiers.has(field.access, Opcodes.ACC_STATIC)) {
                StackElement value = ExecutorTypeUtils.parse(executionManager, executionContext, field.value);
                if (value == StackObject.NULL) value = ExecutorTypeUtils.getFieldDefault(ExecutorTypeUtils.typeToStackType(type(field)));
                this.staticFields.put(field, value);
            }
        }
    }

    public ClassNode getClassNode() {
        return this.classNode;
    }

    public boolean isInstance(final String className) {
        return this.superClasses.containsKey(className);
    }

    @Nullable
    public ResolvedField findField(final String name, final String descriptor) {
        for (Map.Entry<String, ExecutorClass> entry : this.superClasses.entrySet()) {
            FieldNode field = ASMUtils.getField(entry.getValue().classNode, name, descriptor);
            if (field != null) return new ResolvedField(entry.getValue(), field);
        }
        return null;
    }

    @Nullable
    public ResolvedMethod findMethod(final String name, final String descriptor) {
        for (Map.Entry<String, ExecutorClass> entry : this.superClasses.entrySet()) {
            MethodNode method = ASMUtils.getMethod(entry.getValue().classNode, name, descriptor);
            if (method != null && !Modifiers.has(method.access, Opcodes.ACC_ABSTRACT)) return new ResolvedMethod(entry.getValue(), method);
        }
        return null;
    }

    public StackElement getStaticField(final FieldNode field) {
        for (ExecutorClass superClass : this.superClasses.values()) {
            if (superClass.staticFields.containsKey(field)) {
                return superClass.staticFields.get(field);
            }
        }
        throw new IllegalArgumentException("Field not found: " + field.name + ":" + field.desc);
    }

    public void setStaticField(final FieldNode field, final StackElement element) {
        for (ExecutorClass superClass : this.superClasses.values()) {
            if (superClass.staticFields.containsKey(field)) {
                superClass.staticFields.put(field, element);
                return;
            }
        }
        throw new IllegalArgumentException("Field not found: " + field.name + ":" + field.desc);
    }


    public record ResolvedField(ExecutorClass owner, FieldNode field) {
        public StackElement get() {
            return this.owner.getStaticField(this.field);
        }

        public void set(final StackElement element) {
            this.owner.setStaticField(this.field, element);
        }
    }

    public record ResolvedMethod(ExecutorClass owner, MethodNode method) {
    }

}
