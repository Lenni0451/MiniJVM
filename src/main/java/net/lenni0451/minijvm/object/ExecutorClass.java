package net.lenni0451.minijvm.object;

import lombok.SneakyThrows;
import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExecutorClass {

    private final Type type;
    private final ClassNode classNode;
    final Map<String, ExecutorClass> superClasses;
    private final Map<FieldNode, StackElement> staticFields;

    public ExecutorClass(final ExecutionManager executionManager, final ExecutionContext executionContext, final Type type, final ClassNode classNode) {
        this.type = type;
        this.classNode = classNode;
        this.superClasses = new LinkedHashMap<>();
        this.staticFields = new HashMap<>();

        this.initSuperClasses(executionManager, executionContext);
        this.initFields(executionManager, executionContext);
    }

    public Type getType() {
        return this.type;
    }

    public ClassNode getClassNode() {
        return this.classNode;
    }

    @SneakyThrows
    private void initSuperClasses(final ExecutionManager executionManager, final ExecutionContext executionContext) {
        this.superClasses.put(this.classNode.name, this);
        for (String itf : this.classNode.interfaces) {
            this.superClasses.put(itf, executionManager.loadClass(executionContext, Type.getObjectType(itf)));
        }

        ExecutorClass superClass = this;
        while (superClass.classNode.superName != null) {
            superClass = executionManager.loadClass(executionContext, Type.getObjectType(superClass.classNode.superName));
            this.superClasses.putAll(superClass.superClasses);
        }
    }

    private void initFields(final ExecutionManager executionManager, final ExecutionContext executionContext) {
        for (FieldNode field : this.classNode.fields) {
            if (Modifiers.has(field.access, Opcodes.ACC_STATIC)) {
                StackElement value = ExecutorTypeUtils.parse(executionManager, executionContext, field.value);
                if (value.isNull()) value = ExecutorTypeUtils.getFieldDefault(ExecutorTypeUtils.typeToStackType(Type.getType(field.desc)));
                this.staticFields.put(field, value);
            }
        }
    }

    public void invokeStaticInit(final ExecutionManager executionManager, final ExecutionContext executionContext) {
        for (MethodNode method : this.classNode.methods) {
            if (Modifiers.has(method.access, Opcodes.ACC_STATIC) && method.name.equals("<clinit>")) {
                Executor.execute(executionManager, executionContext, this, method, null);
            }
        }
    }

    public boolean isInstance(final ExecutionManager executionManager, final ExecutionContext executionContext, final Type type) {
        if (this.type.getSort() == Type.ARRAY && type.getSort() == Type.ARRAY) {
            if (type.getElementType().equals(Types.OBJECT)) {
                return this.type.getDimensions() >= type.getDimensions();
            } else if (this.type.getDimensions() != type.getDimensions()) {
                return false;
            } else {
                ExecutorClass elementClass = executionManager.loadClass(executionContext, this.type.getElementType());
                return elementClass.isInstance(executionManager, executionContext, type.getElementType());
            }
        }
        return this.superClasses.containsKey(type.getInternalName());
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
                superClass.staticFields.put(field, element.normalize());
                return;
            }
        }
        throw new IllegalArgumentException("Field not found: " + field.name + ":" + field.desc);
    }

    @Override
    public String toString() {
        return "ExecutorClass{" + this.classNode.name + "}";
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
