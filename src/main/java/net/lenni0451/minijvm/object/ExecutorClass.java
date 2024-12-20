package net.lenni0451.minijvm.object;

import lombok.SneakyThrows;
import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
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

    @Nullable
    public FieldNode findField(final String owner, final String name, final String descriptor) {
        ExecutorClass executorClass = this.superClasses.get(owner);
        if (executorClass == null) throw new IllegalArgumentException("Superclass not found: " + owner);
        if (executorClass == this) {
            FieldNode field = ASMUtils.getField(this.classNode, name, descriptor);
            if (field != null) return field;
            for (Map.Entry<String, ExecutorClass> entry : this.superClasses.entrySet()) {
                if (entry.getValue() == this) continue;
                field = ASMUtils.getField(entry.getValue().classNode, name, descriptor);
                if (field != null) return field;
            }
            return null;
        } else {
            return executorClass.findField(owner, name, descriptor);
        }
    }

    @Nullable
    public MethodNode findMethod(final String owner, final String name, final String descriptor) {
        ExecutorClass executorClass = this.superClasses.get(owner);
        if (executorClass == null) throw new IllegalArgumentException("Superclass not found: " + owner);
        if (executorClass == this) {
            MethodNode method = ASMUtils.getMethod(this.classNode, name, descriptor);
            if (method != null) return method;
            for (Map.Entry<String, ExecutorClass> entry : this.superClasses.entrySet()) {
                if (entry.getValue() == this) continue;
                method = ASMUtils.getMethod(entry.getValue().classNode, name, descriptor);
                if (method != null) return method;
            }
            return null;
        } else {
            return executorClass.findMethod(owner, name, descriptor);
        }
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

}
