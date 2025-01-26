package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackInt;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class StringUTF16Natives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/StringUTF16.isBigEndian()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(StackInt.ZERO);
        });
    }

}
