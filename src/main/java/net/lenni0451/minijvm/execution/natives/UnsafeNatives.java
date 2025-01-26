package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.Types;
import net.lenni0451.minijvm.utils.UnsafeUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

/**
 * The real deal.
 */
public class UnsafeNatives implements Consumer<ExecutionManager> {

    private static final int ARRAY_BASE_OFFSET = 16;

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.registerNatives()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.arrayBaseOffset0(Ljava/lang/Class;)I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(ARRAY_BASE_OFFSET));
        });
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.arrayIndexScale0(Ljava/lang/Class;)I", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject executorClass = (ClassObject) ((StackObject) arguments[0]).value();
            Type type = Types.arrayType(executorClass.getClassType().getType());
            return returnValue(new StackInt(UnsafeUtils.arrayIndexScale(type)));
        });
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.objectFieldOffset1(Ljava/lang/Class;Ljava/lang/String;)J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (arguments[0].isNull()) {
                return ExceptionUtils.newException(executionContext, Types.NULL_POINTER_EXCEPTION, "class");
            } else if (arguments[1].isNull()) {
                return ExceptionUtils.newException(executionContext, Types.NULL_POINTER_EXCEPTION, "name");
            }
            ExecutorClass executorClass = ((ClassObject) ((StackObject) arguments[0]).value()).getClassType();
            String fieldName = ExecutorTypeUtils.fromExecutorString(executionContext, ((StackObject) arguments[1]).value());
            FieldNode fieldNode = UnsafeUtils.getFieldByName(executorClass, fieldName);
            if (fieldNode == null) {
                return ExceptionUtils.newException(executionContext, Types.INTERNAL_ERROR, fieldName);
            }
            return returnValue(new StackLong(UnsafeUtils.getFieldHashCode(fieldNode)));
        });
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.fullFence()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.compareAndSetInt(Ljava/lang/Object;JII)Z", this.compareAndSet(e -> ((StackInt) e).value(), StackInt::new, Integer::equals));
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.compareAndSetLong(Ljava/lang/Object;JJJ)Z", this.compareAndSet(e -> ((StackLong) e).value(), StackLong::new, Long::equals));
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.compareAndSetReference(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", this.compareAndSet(e -> ((StackObject) e).value(), StackObject::new, (o1, o2) -> o1 == o2));
        manager.registerMethodExecutor("jdk/internal/misc/Unsafe.getReferenceVolatile(Ljava/lang/Object;J)Ljava/lang/Object;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ExecutorObject object = ((StackObject) arguments[0]).value();
            long offset = ((StackLong) arguments[1]).value();
            if (object instanceof ArrayObject array) {
                offset -= ARRAY_BASE_OFFSET;
                offset /= UnsafeUtils.arrayIndexScale(Types.arrayType(array.getClazz().getType()));
                if (offset < 0 || offset >= array.getElements().length) {
                    throw new ExecutorException(executionContext, "Tried reading from invalid array index: " + offset + "/" + array.getElements().length);
                }
                return returnValue(array.getElements()[(int) offset]);
            } else {
                FieldNode field = UnsafeUtils.getFieldByHashCode(object.getClazz(), offset);
                if (field == null) {
                    throw new ExecutorException(executionContext, "Tried reading from invalid field offset: " + offset);
                }
                return returnValue(object.getField(field));
            }
        });
    }

    private <T> MethodExecutor compareAndSet(final Function<StackElement, T> getter, final Function<T, StackElement> constructor, final BiPredicate<T, T> comparator) {
        return (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ExecutorObject object = ((StackObject) arguments[0]).value();
            long offset = ((StackLong) arguments[1]).value();
            T expected = getter.apply(arguments[2]);
            T update = getter.apply(arguments[3]);
            if (object instanceof ArrayObject array) {
                offset -= ARRAY_BASE_OFFSET;
                offset /= UnsafeUtils.arrayIndexScale(Types.arrayType(array.getClazz().getType()));
                if (offset < 0 || offset >= array.getElements().length) {
                    throw new ExecutorException(executionContext, "Tried writing to invalid array index: " + offset + "/" + array.getElements().length);
                }

                T current = getter.apply(array.getElements()[(int) offset]);
                if (comparator.test(current, expected)) {
                    array.getElements()[(int) offset] = constructor.apply(update);
                    return returnValue(StackInt.ONE);
                } else {
                    return returnValue(StackInt.ZERO);
                }
            } else {
                FieldNode field = UnsafeUtils.getFieldByHashCode(object.getClazz(), offset);
                if (field == null) {
                    throw new ExecutorException(executionContext, "Tried writing to invalid field offset: " + offset);
                }

                T current = getter.apply(object.getField(field));
                if (comparator.test(current, expected)) {
                    object.setField(field, constructor.apply(update));
                    return returnValue(StackInt.ONE);
                } else {
                    return returnValue(StackInt.ZERO);
                }
            }
        };
    }

}
