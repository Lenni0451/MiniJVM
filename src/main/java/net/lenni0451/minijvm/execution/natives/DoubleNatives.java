package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackDouble;
import net.lenni0451.minijvm.stack.StackLong;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class DoubleNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Double.doubleToRawLongBits(D)J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            double value = ((StackDouble) arguments[0]).value();
            return returnValue(new StackLong(Double.doubleToRawLongBits(value)));
        });
        manager.registerMethodExecutor("java/lang/Double.longBitsToDouble(J)D", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            long value = ((StackLong) arguments[0]).value();
            return returnValue(new StackDouble(Double.longBitsToDouble(value)));
        });
    }

}
