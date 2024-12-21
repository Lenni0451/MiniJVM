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
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class is used to manage the classes and fields that are loaded by the executor.
 */
public class ExecutionManager {

    public static final boolean DEBUG = true;
    private static final Set<String> PRIMITIVE_CLASSES = Set.of("void", "boolean", "byte", "short", "char", "int", "long", "float", "double");
    private static final Map<String, String> PRIMITIVE_DESCRIPTOR_TO_CLASS = Map.of("V", "void", "Z", "boolean", "B", "byte", "S", "short", "C", "char", "I", "int", "J", "long", "F", "float", "D", "double");

    private final ClassProvider classProvider;
    private final Map<String, ExecutorClass> loadedClasses;
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
    }

    public void accept(final Consumer<ExecutionManager> consumer) {
        consumer.accept(this);
    }

    public void registerMethodExecutor(final String name, final MethodExecutor methodExecutor) {
        this.methodExecutors.put(name, methodExecutor);
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
    public ExecutorClass loadClass(final ExecutionContext executionContext, final String name) {
        if (this.loadedClasses.containsKey(name)) return this.loadedClasses.get(name);
        ClassNode classNode;
        if (PRIMITIVE_CLASSES.contains(name)) {
            classNode = new ClassNode();
            classNode.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, name, null, null, null);
        } else if (name.startsWith("[")) {
            String elementName = name;
            while (elementName.startsWith("[")) elementName = elementName.substring(1);
            if ((!elementName.startsWith("L") || !elementName.endsWith(";")) && elementName.length() != 1) {
                throw new ExecutorException(executionContext, "Invalid array element type: " + name);
            }

            classNode = new ClassNode();
            classNode.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, name, null, "java/lang/Object", new String[]{"java/lang/Cloneable", "java/io/Serializable"});
        } else {
            if ((name.startsWith("L") && name.endsWith(";")) || name.contains(".")) {
                throw new ExecutorException(executionContext, "Invalid class name: " + name);
            }

            classNode = this.classProvider.getClassNode(name);
        }
        if (classNode == null) throw new ClassNotFoundException(name);
        ExecutorClass executorClass = new ExecutorClass(this, executionContext, classNode);
        this.loadedClasses.put(name, executorClass); //Add the class here to prevent infinite loops
        executorClass.invokeStatic(this, executionContext);
        return executorClass;
    }

    public ExecutorObject instantiateClass(final ExecutionContext executionContext, final ExecutorClass executorClass) {
        if (this.classInstances.containsKey(executorClass)) return this.classInstances.get(executorClass);
        ExecutorObject classInstance = new ClassObject(this, executionContext, executorClass);
        { //Component type
            ExecutorClass.ResolvedField componentTypeField = classInstance.getOwner().findField("componentType", "Ljava/lang/Class;");
            if (componentTypeField != null) {
                if (executorClass.getClassNode().name.startsWith("[")) {
                    String componentType = executorClass.getClassNode().name.substring(1);
                    if (!componentType.startsWith("[") & componentType.length() > 1) componentType = componentType.substring(1, componentType.length() - 1);
                    ExecutorClass componentTypeClass = this.loadClass(executionContext, PRIMITIVE_DESCRIPTOR_TO_CLASS.getOrDefault(componentType, componentType));
                    classInstance.setField(componentTypeField.field(), new StackObject(this.instantiateClass(executionContext, componentTypeClass)));
                } else {
                    classInstance.setField(componentTypeField.field(), StackObject.NULL);
                }
            }
        }
        { //Name
            ExecutorClass.ResolvedField nameField = classInstance.getOwner().findField("name", "Ljava/lang/String;");
            if (nameField != null) {
                classInstance.setField(nameField.field(), ExecutorTypeUtils.parse(this, executionContext, executorClass.getClassNode().name));
            }
        }
        this.classInstances.put(executorClass, classInstance);
        return classInstance;
    }

    public ExecutorObject instantiate(final ExecutionContext executionContext, final ExecutorClass executorClass) {
        ExecutorObject object = new ExecutorObject(this, executionContext, executorClass);
        return object;
    }

    public ExecutorObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final int length) {
        return new ArrayObject(this, executionContext, executorClass, new StackElement[length]);
    }

    public ExecutorObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final StackElement[] elements) {
        return new ArrayObject(this, executionContext, executorClass, elements);
    }

    public ArrayObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final int length, final Supplier<StackElement> elementSupplier) {
        StackElement[] elements = new StackElement[length];
        for (int i = 0; i < length; i++) elements[i] = elementSupplier.get();
        return new ArrayObject(this, executionContext, executorClass, elements);
    }

}
