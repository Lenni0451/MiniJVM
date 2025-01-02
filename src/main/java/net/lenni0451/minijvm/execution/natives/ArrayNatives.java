package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ClassUtils;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ArrayNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/reflect/Array.newArray(Ljava/lang/Class;I)Ljava/lang/Object;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (arguments[0] == StackObject.NULL) {
                return ExceptionUtils.newException(executionManager, executionContext, Types.NULL_POINTER_EXCEPTION, "class");
            }
            ExecutorClass arrayClass = ClassUtils.getClassFromClassInstance(executionContext, (StackObject) arguments[0]);
            if (arrayClass.getClassNode().name.equals("void")) {
                return ExceptionUtils.newException(executionManager, executionContext, Types.ILLEGAL_ARGUMENT_EXCEPTION, "void is not a valid array type");
            }
            int length = ((StackInt) arguments[1]).value();
            if (length < 0) {
                return ExceptionUtils.newException(executionManager, executionContext, Types.NEGATIVE_ARRAY_SIZE_EXCEPTION, "Negative array size: " + length);
            } else if (length > 255) {
                return ExceptionUtils.newException(executionManager, executionContext, Types.ILLEGAL_ARGUMENT_EXCEPTION, "Array size too large: " + length);
            }
            return returnValue(new StackObject(executionManager.instantiateArray(executionContext, arrayClass, length)));
        });
    }

}
