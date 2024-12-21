package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ClassUtils;
import net.lenni0451.minijvm.utils.ExceptionUtils;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ArrayNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/reflect/Array.newArray(Ljava/lang/Class;I)Ljava/lang/Object;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (arguments[0] == StackObject.NULL) {
                return ExceptionUtils.newException(executionManager, executionContext, "java/lang/NullPointerException", "class");
            }
            ExecutorClass arrayClass = ClassUtils.getClassFromClassInstance(executionContext, (StackObject) arguments[0]);
            if (arrayClass.getClassNode().name.equals("void")) {
                return ExceptionUtils.newException(executionManager, executionContext, "java/lang/IllegalArgumentException", "void is not a valid array type");
            }
            int length = ((StackInt) arguments[1]).value();
            if (length < 0) {
                return ExceptionUtils.newException(executionManager, executionContext, "java/lang/NegativeArraySizeException", "Negative array size: " + length);
            } else if (length > 255) {
                return ExceptionUtils.newException(executionManager, executionContext, "java/lang/IllegalArgumentException", "Array size too large: " + length);
            }
            return returnValue(new StackObject(executionManager.instantiateArray(executionContext, arrayClass, length)));
        });
    }

}
