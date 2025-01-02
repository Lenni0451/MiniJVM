package net.lenni0451.minijvm;

import lombok.SneakyThrows;
import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.commons.asm.provider.ClassProvider;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.execution.JVMMethodExecutor;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.execution.natives.*;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.*;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * This class is used to manage the classes and fields that are loaded by the executor.
 */
public class ExecutionManager {

    public static final boolean DEBUG = true;

    private final ClassProvider classProvider;
    private final Map<Type, ExecutorClass> loadedClasses;
    private final Map<ExecutorClass, ExecutorObject> classInstances;
    private final Map<String, MethodExecutor> methodExecutors;

    public ExecutionManager(final ClassProvider classProvider) {
        this.classProvider = classProvider;
        this.loadedClasses = new HashMap<>();
        this.classInstances = new HashMap<>();
        this.methodExecutors = new HashMap<>();

        this.registerMethodExecutor(null, new JVMMethodExecutor());
        this.accept(new ClassNatives());
        this.accept(new StringUTF16Natives());
        this.accept(new SystemNatives());
        this.accept(new FloatNatives());
        this.accept(new DoubleNatives());
        this.accept(new CDSNatives());
        this.accept(new ObjectNatives());
        this.accept(new RuntimeNatives());
        this.accept(new UnsafeNatives());
        this.accept(new ReflectionNatives());
        this.accept(new ArrayNatives());
        this.accept(new ThrowableNatives());
        this.accept(new ThreadNatives());
        this.accept(new ClassLoaderNatives());
    }

    public void accept(final Consumer<ExecutionManager> consumer) {
        consumer.accept(this);
    }

    public void registerMethodExecutor(final String classMethodDescriptor, final MethodExecutor methodExecutor) {
        this.methodExecutors.put(classMethodDescriptor, methodExecutor);
    }

    public MethodExecutor getMethodExecutor(final ExecutionContext executionContext, final String owner, final MethodNode methodNode) {
        MethodExecutor methodExecutor = this.methodExecutors.get(owner + "." + methodNode.name + methodNode.desc);
        if (methodExecutor != null) return methodExecutor;
        if (Modifiers.has(methodNode.access, Opcodes.ACC_NATIVE)) {
            throw new ExecutorException(executionContext, "Native method not implemented: " + owner + "." + methodNode.name + methodNode.desc);
        } else {
            return this.methodExecutors.get(null);
        }
    }

    @SneakyThrows
    public ExecutorClass loadClass(final ExecutionContext executionContext, final Type type) {
        if (this.loadedClasses.containsKey(type)) return this.loadedClasses.get(type);
        ClassNode classNode;
        if (type.getSort() >= Type.VOID && type.getSort() <= Type.DOUBLE) {
            classNode = new ClassNode();
            classNode.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, type.getClassName(), null, null, null);
        } else if (type.getSort() == Type.ARRAY) {
            if (type.getElementType().equals(Type.VOID_TYPE)) {
                throw new ExecutorException(executionContext, "Invalid array element type: " + type.getElementType());
            }

            classNode = new ClassNode();
            classNode.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, type.getInternalName(), null, "java/lang/Object", new String[]{"java/lang/Cloneable", "java/io/Serializable"});
        } else if (type.getSort() == Type.OBJECT) {
            classNode = this.classProvider.getClassNode(type.getInternalName());
            if (classNode == null) throw new ClassNotFoundException(type.getClassName());
        } else {
            throw new ExecutorException(executionContext, "Unsupported type: " + type.getSort() + " (" + type + ")");
        }
        ExecutorClass executorClass = new ExecutorClass(this, executionContext, type, classNode);
        this.loadedClasses.put(type, executorClass); //Add the class here to prevent infinite loops
        executorClass.invokeStaticInit(this, executionContext);
        return executorClass;
    }

    public ExecutorObject instantiateClass(final ExecutionContext executionContext, final ExecutorClass executorClass) {
        if (this.classInstances.containsKey(executorClass)) return this.classInstances.get(executorClass);
        ExecutorObject classInstance = new ClassObject(this, executionContext, executorClass);
        { //Component type
            ExecutorClass.ResolvedField componentTypeField = classInstance.getClazz().findField("componentType", "Ljava/lang/Class;");
            if (componentTypeField != null) {
                if (executorClass.getType().getSort() == Type.ARRAY) {
                    ExecutorClass componentTypeClass = this.loadClass(executionContext, Types.arrayType(executorClass.getType()));
                    classInstance.setField(componentTypeField.field(), new StackObject(this.instantiateClass(executionContext, componentTypeClass)));
                } else {
                    classInstance.setField(componentTypeField.field(), StackObject.NULL);
                }
            }
        }
        { //Name
            ExecutorClass.ResolvedField nameField = classInstance.getClazz().findField("name", "Ljava/lang/String;");
            if (nameField != null) {
                classInstance.setField(nameField.field(), ExecutorTypeUtils.parse(this, executionContext, executorClass.getClassNode().name));
            }
        }
        this.classInstances.put(executorClass, classInstance);
        return classInstance;
    }

    public ExecutorObject instantiate(final ExecutionContext executionContext, final ExecutorClass executorClass) {
        return new ExecutorObject(this, executionContext, executorClass);
    }

    public ExecutorObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final int length) {
        IntFunction<StackElement> initializer = switch (executorClass.getType().getSort()) {
            case Type.BOOLEAN -> i -> new StackInt(false);
            case Type.CHAR -> i -> new StackInt(0);
            case Type.BYTE -> i -> new StackInt(0);
            case Type.SHORT -> i -> new StackInt(0);
            case Type.INT -> i -> new StackInt(0);
            case Type.FLOAT -> i -> new StackFloat(0);
            case Type.LONG -> i -> new StackLong(0);
            case Type.DOUBLE -> i -> new StackDouble(0);
            default -> i -> StackObject.NULL;
        };
        return this.instantiateArray(executionContext, executorClass, length, initializer);
    }

    public ExecutorObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final StackElement[] elements) {
        return this.instantiateArray(executionContext, executorClass, elements.length, i -> elements[i]);
    }

    public ArrayObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final int length, final IntFunction<StackElement> elementSupplier) {
        if (executorClass.getType().getSort() != Type.ARRAY) {
            throw new ExecutorException(executionContext, "Class is not an array: " + executorClass.getType());
        }

        StackElement[] elements = new StackElement[length];
        for (int i = 0; i < length; i++) elements[i] = elementSupplier.apply(i);
        return new ArrayObject(this, executionContext, executorClass, elements);
    }

}
