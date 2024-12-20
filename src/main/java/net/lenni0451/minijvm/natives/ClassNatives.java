package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ClassClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;

import java.util.function.Consumer;

public class ClassNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("java/lang/Class.registerNatives()V", NativeExecutor.NOOP_VOID);
        manager.registerNativeExecutor("java/lang/Class.desiredAssertionStatus0(Ljava/lang/Class;)Z", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> new StackInt(0));
        manager.registerNativeExecutor("java/lang/Class.getPrimitiveClass(Ljava/lang/String;)Ljava/lang/Class;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            //TODO: Exceptions
            String name = ExecutorTypeUtils.fromExecutorString(executionManager, executionContext, ((StackObject) arguments[0]).value());
            ClassClass classClass = executionManager.loadClassClass(executionContext, name);
            ExecutorObject classObject = executionManager.instantiate(executionContext, classClass);
            return new StackObject(classObject);
        });
        manager.registerNativeExecutor("java/lang/Class.isArray()Z", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> new StackInt(instance.getOwner().getClassNode().name.startsWith("[") ? 1 : 0));
    }

}
