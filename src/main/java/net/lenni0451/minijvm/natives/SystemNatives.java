package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ArrayObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

public class SystemNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("java/lang/System.registerNatives()V", NativeExecutor.NOOP_VOID);
        manager.registerNativeExecutor("java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            //TODO: Exceptions
            StackObject src = (StackObject) arguments[0];
            StackInt srcPos = (StackInt) arguments[1];
            StackObject dest = (StackObject) arguments[2];
            StackInt destPos = (StackInt) arguments[3];
            StackInt length = (StackInt) arguments[4];

            StackElement[] srcArray = ((ArrayObject) src.value()).getElements();
            StackElement[] destArray = ((ArrayObject) dest.value()).getElements();
            System.arraycopy(srcArray, srcPos.value(), destArray, destPos.value(), length.value());
            return null;
        });
        manager.registerNativeExecutor("java/lang/System.nanoTime()J", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> new StackLong(System.nanoTime()));
        manager.registerNativeExecutor("java/lang/System.currentTimeMillis()J", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> new StackLong(System.currentTimeMillis()));
    }

}
