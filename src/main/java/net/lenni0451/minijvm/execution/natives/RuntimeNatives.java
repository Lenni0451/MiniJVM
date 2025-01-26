package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class RuntimeNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Runtime.availableProcessors()I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(Runtime.getRuntime().availableProcessors()));
        });
        manager.registerMethodExecutor("java/lang/Runtime.maxMemory()J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackLong(Runtime.getRuntime().maxMemory()));
        });
    }

}
