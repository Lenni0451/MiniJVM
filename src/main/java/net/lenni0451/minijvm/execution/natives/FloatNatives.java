package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackFloat;
import net.lenni0451.minijvm.stack.StackInt;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class FloatNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Float.floatToRawIntBits(F)I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            float value = ((StackFloat) arguments[0]).value();
            return returnValue(new StackInt(Float.floatToRawIntBits(value)));
        });
    }

}
