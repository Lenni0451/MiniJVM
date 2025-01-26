package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.stack.StackInt;

import java.util.function.Consumer;

public class SignalNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("jdk/internal/misc/Signal.findSignal0(Ljava/lang/String;)I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackInt(-1));
        });
    }

}
