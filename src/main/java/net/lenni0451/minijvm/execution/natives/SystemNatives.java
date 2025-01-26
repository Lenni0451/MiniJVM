package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;
import static net.lenni0451.minijvm.execution.ExecutionResult.voidResult;

public class SystemNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/System.registerNatives()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            StackObject src = (StackObject) arguments[0];
            StackInt srcPos = (StackInt) arguments[1];
            StackObject dest = (StackObject) arguments[2];
            StackInt destPos = (StackInt) arguments[3];
            StackInt length = (StackInt) arguments[4];
            if (src == null) {
                return ExceptionUtils.newException(executionContext, Types.NULL_POINTER_EXCEPTION, "src");
            }
            if (dest == null) {
                return ExceptionUtils.newException(executionContext, Types.NULL_POINTER_EXCEPTION, "dest");
            }
            ArrayObject srcArray = (ArrayObject) src.value();
            ArrayObject destArray = (ArrayObject) dest.value();
            if (srcPos.value() < 0 || srcPos.value() + length.value() > srcArray.getElements().length) {
                return ExceptionUtils.newException(executionContext, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "srcPos");
            }
            if (destPos.value() < 0 || destPos.value() + length.value() > destArray.getElements().length) {
                return ExceptionUtils.newException(executionContext, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "destPos");
            }
            if (length.value() < 0) {
                return ExceptionUtils.newException(executionContext, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "length");
            }
            //TODO: Component type check
            System.arraycopy(srcArray.getElements(), srcPos.value(), destArray.getElements(), destPos.value(), length.value());
            return voidResult();
        });
        manager.registerMethodExecutor("java/lang/System.nanoTime()J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackLong(System.nanoTime()));
        });
        manager.registerMethodExecutor("java/lang/System.currentTimeMillis()J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackLong(System.currentTimeMillis()));
        });
    }

}
