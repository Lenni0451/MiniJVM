package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackInt;

import java.util.function.Consumer;

public class StringUTF16Natives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("java/lang/StringUTF16.isBigEndian()Z", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> new StackInt(0));
    }

}
