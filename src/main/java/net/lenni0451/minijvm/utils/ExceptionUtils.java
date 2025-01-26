package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.Type;

public class ExceptionUtils {

    public static ExecutionResult newException(final ExecutionContext executionContext, final Type exceptionType) {
        if (ExecutionManager.DEBUG) {
            System.out.println("Creating new exception: " + exceptionType);
            for (ExecutionContext.StackFrame stackFrame : executionContext.getStackFrames()) System.out.println(" -> " + stackFrame);
        }
        return invoke(executionContext, exceptionType, "()V");
    }

    public static ExecutionResult newException(final ExecutionContext executionContext, final Type exceptionType, final String message) {
        if (ExecutionManager.DEBUG) {
            System.out.println("Creating new exception: " + exceptionType + " with message: " + message);
            for (ExecutionContext.StackFrame stackFrame : executionContext.getStackFrames()) System.out.println(" -> " + stackFrame);
        }
        return invoke(executionContext, exceptionType, "(Ljava/lang/String;)V", ExecutorTypeUtils.parse(executionContext, message));
    }

    private static ExecutionResult invoke(final ExecutionContext executionContext, final Type exceptionType, final String constructorDesc, final StackElement... arguments) {
        ExecutorClass exceptionClass = executionContext.getExecutionManager().loadClass(executionContext, exceptionType);
        ExecutorObject exceptionObject = executionContext.getExecutionManager().instantiate(executionContext, exceptionClass);
        ExecutorClass.ResolvedMethod initMethod = exceptionClass.findMethod(executionContext, "<init>", constructorDesc);
        if (initMethod == null) throw new ExecutorException(executionContext, "Could not find string constructor in exception class: " + exceptionType);
        ExecutionResult result = Executor.execute(executionContext, exceptionClass, initMethod.method(), exceptionObject, arguments);
        if (result.hasException()) throw new ExecutorException(executionContext, "Could not instantiate exception: " + exceptionType + " - " + result.getException());
        return ExecutionResult.exception(exceptionObject);
    }

    public static String getMessage(final ExecutionContext executionContext, final ExecutorObject exceptionObject) {
        ExecutorClass clazz = exceptionObject.getClazz();
        ExecutorClass.ResolvedMethod getMessage = clazz.findMethod(executionContext, "getMessage", "()Ljava/lang/String;");
        ExecutionResult result = Executor.execute(executionContext, clazz, getMessage.method(), exceptionObject);
        if (result.hasException()) throw new ExecutorException(executionContext, "Could not get message from exception: " + clazz.getType());
        return ExecutorTypeUtils.fromExecutorString(executionContext, ((StackObject) result.getReturnValue()).value());
    }

}
