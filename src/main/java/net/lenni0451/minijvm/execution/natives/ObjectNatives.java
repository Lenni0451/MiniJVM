package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ObjectNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Object.hashCode()I", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(instance.hashCode()));
        });
        manager.registerMethodExecutor("java/lang/Object.getClass()Ljava/lang/Class;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackObject(executionManager.instantiateClass(executionContext, instance.getOwner())));
        });
    }

}
