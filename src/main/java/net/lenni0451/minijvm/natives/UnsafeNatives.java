package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ClassClass;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

/**
 * The real deal.
 */
public class UnsafeNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("jdk/internal/misc/Unsafe.registerNatives()V", NativeExecutor.NOOP_VOID);
        manager.registerNativeExecutor("jdk/internal/misc/Unsafe.arrayBaseOffset0(Ljava/lang/Class;)I", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> new StackInt(16));
        manager.registerNativeExecutor("jdk/internal/misc/Unsafe.arrayIndexScale0(Ljava/lang/Class;)I", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassClass clazz = (ClassClass) ((StackObject) arguments[0]).value().getOwner();
            Type type = Type.getType(clazz.getClassNode().name);
            type = type.getElementType();
            return new StackInt(switch (type.getSort()) {
                case Type.BOOLEAN -> 1;
                case Type.BYTE -> Byte.BYTES;
                case Type.SHORT -> Short.BYTES;
                case Type.CHAR -> Character.BYTES;
                case Type.INT -> Integer.BYTES;
                case Type.LONG -> Long.BYTES;
                case Type.FLOAT -> Float.BYTES;
                case Type.DOUBLE -> Double.BYTES;
                default -> 4; //Address size
            });
        });
    }

}
