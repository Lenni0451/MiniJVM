package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackInt;

import java.util.function.Consumer;

public class ObjectNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("java/lang/Object.hashCode()I", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> new StackInt(instance.hashCode()));
    }

}
