package net.lenni0451.minijvm.object;

import lombok.SneakyThrows;
import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.commons.asm.Modifiers;
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
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorClass {

    private final Type type;
    private final ClassNode classNode;
    final Map<String, ExecutorClass> superClasses;
    private final Map<FieldNode, StackElement> staticFields;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public ExecutorClass(final ExecutionContext executionContext, final Type type, final ClassNode classNode) {
        this.type = type;
        this.classNode = classNode;
        this.superClasses = new LinkedHashMap<>();
        this.staticFields = new HashMap<>();

        this.initSuperClasses(executionContext);
        this.initFields(executionContext);
    }

    public Type getType() {
        return this.type;
    }

    public ClassNode getClassNode() {
        return this.classNode;
    }

    @SneakyThrows
    private void initSuperClasses(final ExecutionContext executionContext) {
        Stack<ExecutorClass> stack = new Stack<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            ExecutorClass current = stack.pop();
            if (this.superClasses.containsKey(current.classNode.name)) continue;
            this.superClasses.put(current.classNode.name, current);
            for (String itf : current.classNode.interfaces) {
                stack.push(executionContext.getExecutionManager().loadClass(executionContext, Type.getObjectType(itf)));
            }
            if (current.classNode.superName != null) {
                stack.push(executionContext.getExecutionManager().loadClass(executionContext, Type.getObjectType(current.classNode.superName)));
            }
        }
    }

    private void initFields(final ExecutionContext executionContext) {
        for (FieldNode field : this.classNode.fields) {
            if (Modifiers.has(field.access, Opcodes.ACC_STATIC)) {
                StackElement value = ExecutorTypeUtils.parse(executionContext, field.value);
                if (value.isNull()) value = ExecutorTypeUtils.getFieldDefault(ExecutorTypeUtils.typeToStackType(Type.getType(field.desc)));
                this.staticFields.put(field, value);
            }
        }
    }

    public void invokeStaticInit(final ExecutionContext executionContext) {
        if (!this.initialized.compareAndSet(false, true)) return;
        for (MethodNode method : this.classNode.methods) {
            if (Modifiers.has(method.access, Opcodes.ACC_STATIC) && method.name.equals("<clinit>")) {
                Executor.execute(executionContext, this, method, null);
            }
        }
    }

    public boolean isInstance(final ExecutionContext executionContext, final Type type) {
        if (this.type.getSort() == Type.ARRAY && type.getSort() == Type.ARRAY) {
            if (type.getElementType().equals(Types.OBJECT)) {
                return this.type.getDimensions() >= type.getDimensions();
            } else if (this.type.getDimensions() != type.getDimensions()) {
                return false;
            } else {
                ExecutorClass elementClass = executionContext.getExecutionManager().loadClass(executionContext, this.type.getElementType());
                return elementClass.isInstance(executionContext, type.getElementType());
            }
        }
        String name = type.getInternalName();
        if (Types.isPrimitive(type)) name = type.getClassName();
        return this.superClasses.containsKey(name);
    }

    @Nullable
    public ResolvedField findField(final ExecutionContext executionContext, final String name, final String descriptor) {
        this.invokeStaticInit(executionContext);
        for (Map.Entry<String, ExecutorClass> entry : this.superClasses.entrySet()) {
            FieldNode field = ASMUtils.getField(entry.getValue().classNode, name, descriptor);
            if (field != null) return new ResolvedField(entry.getValue(), field);
        }
        return null;
    }

    @Nullable
    public ResolvedMethod findMethod(final ExecutionContext executionContext, final String name, final String descriptor) {
        this.invokeStaticInit(executionContext);
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
