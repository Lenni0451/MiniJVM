package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ThrowableNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Throwable.fillInStackTrace(I)Ljava/lang/Throwable;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            //TODO: Implement fillInStackTrace
            return returnValue(new StackObject(instance));
        });
    }

}
