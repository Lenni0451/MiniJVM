package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.Types;
import net.lenni0451.minijvm.utils.UnsafeUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * The real deal.
 */
public class UnsafeNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.registerNatives()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.arrayBaseOffset0(Ljava/lang/Class;)I", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(16));
        });
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.arrayIndexScale0(Ljava/lang/Class;)I", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject executorClass = (ClassObject) ((StackObject) arguments[0]).value();
            Type type = Type.getType(executorClass.getClassType().getClassNode().name).getElementType();
            return returnValue(new StackInt(switch (type.getSort()) {
                case Type.BOOLEAN -> 1;
                case Type.BYTE -> Byte.BYTES;
                case Type.SHORT -> Short.BYTES;
                case Type.CHAR -> Character.BYTES;
                case Type.INT -> Integer.BYTES;
                case Type.LONG -> Long.BYTES;
                case Type.FLOAT -> Float.BYTES;
                case Type.DOUBLE -> Double.BYTES;
                default -> 4; //Address size
            }));
        });
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.objectFieldOffset1(Ljava/lang/Class;Ljava/lang/String;)J", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (arguments[0].isNull()) {
                return ExceptionUtils.newException(executionManager, executionContext, Types.NULL_POINTER_EXCEPTION, "class");
            } else if (arguments[1].isNull()) {
                return ExceptionUtils.newException(executionManager, executionContext, Types.NULL_POINTER_EXCEPTION, "name");
            }
            ExecutorClass executorClass = ((ClassObject) ((StackObject) arguments[0]).value()).getClassType();
            String fieldName = ExecutorTypeUtils.fromExecutorString(executionManager, executionContext, ((StackObject) arguments[1]).value());
            FieldNode fieldNode = UnsafeUtils.getFieldByName(executorClass, fieldName);
            if (fieldNode == null) {
                return ExceptionUtils.newException(executionManager, executionContext, Types.INTERNAL_ERROR, fieldName);
            }
            return returnValue(new StackLong(UnsafeUtils.getFieldHashCode(fieldNode)));
        });
    }

}
