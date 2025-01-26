package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ClassUtils;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ClassNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Class.registerNatives()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/lang/Class.desiredAssertionStatus0(Ljava/lang/Class;)Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(StackInt.ZERO);
        });
        manager.registerMethodExecutor("java/lang/Class.getPrimitiveClass(Ljava/lang/String;)Ljava/lang/Class;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            String className = ExecutorTypeUtils.fromExecutorString(executionContext, ((StackObject) arguments[0]).value());
            if (!ClassUtils.PRIMITIVE_CLASS_TO_DESCRIPTOR.containsKey(className)) {
                return ExceptionUtils.newException(executionContext, Types.CLASS_NOT_FOUND_EXCEPTION, className);
            }
            ExecutorClass primitiveClass = executionContext.getExecutionManager().loadClass(executionContext, Type.getType(ClassUtils.PRIMITIVE_CLASS_TO_DESCRIPTOR.get(className)));
            return returnValue(new StackObject(executionContext.getExecutionManager().instantiateClass(executionContext, primitiveClass)));
        });
        manager.registerMethodExecutor("java/lang/Class.isArray()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            boolean isArray = ((ClassObject) instance).getClassType().getClassNode().name.startsWith("[");
            return returnValue(new StackInt(isArray));
        });
        manager.registerMethodExecutor("java/lang/Class.isPrimitive()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassObject clazz = (ClassObject) instance;
            return ExecutionResult.returnValue(new StackInt(Types.isPrimitive(clazz.getClassType().getType())));
        });
    }

}
