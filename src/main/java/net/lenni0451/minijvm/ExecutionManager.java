package net.lenni0451.minijvm;

import lombok.SneakyThrows;
import net.lenni0451.commons.asm.provider.ClassProvider;
import net.lenni0451.minijvm.natives.*;
import net.lenni0451.minijvm.object.*;
import net.lenni0451.minijvm.stack.StackElement;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.lenni0451.commons.asm.Types.type;

/**
 * This class is used to manage the classes and fields that are loaded by the executor.
 */
public class ExecutionManager {

    private final ClassProvider classProvider;
    private final Map<String, ExecutorClass> loadedClasses;
    private final Map<String, ClassClass> loadedClassClasses;
    private final Map<String, NativeExecutor> nativeExecutors;

    public ExecutionManager(final ClassProvider classProvider) {
        this.classProvider = classProvider;
        this.loadedClasses = new HashMap<>();
        this.loadedClassClasses = new HashMap<>();
        this.nativeExecutors = new HashMap<>();

        this.accept(new ClassNatives());
        this.accept(new StringUTF16Natives());
        this.accept(new SystemNatives());
        this.accept(new FloatNatives());
        this.accept(new DoubleNatives());
        this.accept(new CDSNatives());
        this.accept(new ObjectNatives());
        this.accept(new RuntimeNatives());
        this.accept(new UnsafeNatives());
    }

    public void accept(final Consumer<ExecutionManager> consumer) {
        consumer.accept(this);
    }

    public ClassProvider getClassProvider() {
        return this.classProvider;
    }

    public Map<String, NativeExecutor> getNativeExecutors() {
        return this.nativeExecutors;
    }

    public void registerNativeExecutor(final String name, final NativeExecutor nativeExecutor) {
        this.nativeExecutors.put(name, nativeExecutor);
    }

    @SneakyThrows
    public ExecutorClass loadClass(final ExecutionContext executionContext, final String name) {
        if (this.loadedClasses.containsKey(name)) return this.loadedClasses.get(name);
        if (type(name).getSort() == Type.ARRAY) {
            ArrayClass arrayClass = new ArrayClass(this, executionContext, type(name));
            this.loadedClasses.put(name, arrayClass);
            return arrayClass;
        } else {
            ClassNode classNode = this.classProvider.getClassNode(name);
            if (classNode == null) throw new IllegalArgumentException("Class not found: " + name);
            ExecutorClass executorClass = new ExecutorClass(this, executionContext, classNode);
            this.loadedClasses.put(name, executorClass); //Add the class here to prevent infinite loops
            for (MethodNode method : classNode.methods) {
                if (!method.name.equals("<clinit>")) continue;
                Executor.execute(this, executionContext, executorClass, method, null, new StackElement[0]);
            }
            return executorClass;
        }
    }

    public ClassClass loadClassClass(final ExecutionContext executionContext, final String name) {
        if (this.loadedClassClasses.containsKey(name)) return this.loadedClassClasses.get(name);
        ClassClass classClass = new ClassClass(this, executionContext, type(name));
        this.loadedClassClasses.put(name, classClass);
        return classClass;
    }

    public ExecutorObject instantiate(final ExecutionContext executionContext, final ExecutorClass executorClass) {
        return new ExecutorObject(this, executionContext, executorClass);
    }

    public ExecutorObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final int length) {
        return new ArrayObject(this, executionContext, (ArrayClass) executorClass, new StackElement[length]);
    }

    public ExecutorObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final StackElement[] elements) {
        return new ArrayObject(this, executionContext, (ArrayClass) executorClass, elements);
    }

    public ArrayObject instantiateArray(final ExecutionContext executionContext, final ExecutorClass executorClass, final int length, final Supplier<StackElement> elementSupplier) {
        StackElement[] elements = new StackElement[length];
        for (int i = 0; i < length; i++) elements[i] = elementSupplier.get();
        return new ArrayObject(this, executionContext, (ArrayClass) executorClass, elements);
    }

}
