package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackDouble;
import net.lenni0451.minijvm.stack.StackLong;

import java.util.function.Consumer;

public class DoubleNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("java/lang/Double.doubleToRawLongBits(D)J", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            double value = ((StackDouble) arguments[0]).value();
            return new StackLong(Double.doubleToRawLongBits(value));
        });
        manager.registerNativeExecutor("java/lang/Double.longBitsToDouble(J)D", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            long value = ((StackLong) arguments[0]).value();
            return new StackDouble(Double.longBitsToDouble(value));
        });
    }

}
