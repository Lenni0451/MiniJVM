package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackInt;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class RuntimeNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Runtime.availableProcessors()I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(Runtime.getRuntime().availableProcessors()));
        });
    }

}
