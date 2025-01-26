package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ClassUtils;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ArrayNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/reflect/Array.newArray(Ljava/lang/Class;I)Ljava/lang/Object;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (arguments[0].isNull()) {
                return ExceptionUtils.newException(executionContext, Types.NULL_POINTER_EXCEPTION, "class");
            }
            ExecutorClass elementClass = ClassUtils.getClassFromClassInstance(executionContext, (StackObject) arguments[0]);
            Type elementType = elementClass.getType();
            if (elementType.equals(Type.VOID_TYPE) || (elementType.getSort() == Type.ARRAY && Types.arrayType(elementType).equals(Type.VOID_TYPE))) {
                return ExceptionUtils.newException(executionContext, Types.ILLEGAL_ARGUMENT_EXCEPTION, "void is not a valid array type");
            }
            int length = ((StackInt) arguments[1]).value();
            if (length < 0) {
                return ExceptionUtils.newException(executionContext, Types.NEGATIVE_ARRAY_SIZE_EXCEPTION, "Negative array size: " + length);
            } else if (length > 255) {
                return ExceptionUtils.newException(executionContext, Types.ILLEGAL_ARGUMENT_EXCEPTION, "Array size too large: " + length);
            }
            ExecutorClass arrayClass = executionContext.getExecutionManager().loadClass(executionContext, Types.asArray(elementType, 1));
            return returnValue(new StackObject(executionContext.getExecutionManager().instantiateArray(executionContext, arrayClass, length)));
        });
    }

}
