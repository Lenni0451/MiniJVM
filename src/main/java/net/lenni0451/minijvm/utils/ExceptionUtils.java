package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;

public class ExceptionUtils {

    public static ExecutionResult newException(final ExecutionContext context, final Type exceptionType) {
        if (ExecutionManager.DEBUG) {
            System.out.println("Creating new exception: " + exceptionType);
            for (ExecutionContext.StackFrame stackFrame : context.getStackFrames()) System.out.println(" -> " + stackFrame);
        }
        return invoke(context, exceptionType, "()V");
    }

    public static ExecutionResult newException(final ExecutionContext context, final Type exceptionType, final String message) {
        if (ExecutionManager.DEBUG) {
            System.out.println("Creating new exception: " + exceptionType + " with message: " + message);
            for (ExecutionContext.StackFrame stackFrame : context.getStackFrames()) System.out.println(" -> " + stackFrame);
        }
        return invoke(context, exceptionType, "(Ljava/lang/String;)V", ExecutorTypeUtils.parse(context, message));
    }

    private static ExecutionResult invoke(final ExecutionContext context, final Type exceptionType, final String constructorDesc, final StackElement... arguments) {
        ExecutorClass exceptionClass = context.getExecutionManager().loadClass(context, exceptionType);
        ExecutorObject exceptionObject = context.getExecutionManager().instantiate(context, exceptionClass);
        ExecutorClass.ResolvedMethod initMethod = exceptionClass.findMethod(context, "<init>", constructorDesc);
        if (initMethod == null) throw new ExecutorException(context, "Could not find string constructor in exception class: " + exceptionType);
        ExecutionResult result = Executor.execute(context, exceptionClass, initMethod.method(), exceptionObject, arguments);
        if (result.hasException()) throw new ExecutorException(context, "Could not instantiate exception: " + exceptionType + " - " + result.getException());
        return ExecutionResult.exception(exceptionObject);
    }

    @Nullable
    public static String getMessage(final ExecutionContext context, final ExecutorObject exceptionObject) {
        ExecutorClass clazz = exceptionObject.getClazz();
        ExecutorClass.ResolvedMethod getMessage = clazz.findMethod(context, "getMessage", "()Ljava/lang/String;");
        ExecutionResult result = Executor.execute(context, clazz, getMessage.method(), exceptionObject);
        if (result.hasException()) throw new ExecutorException(context, "Could not get message from exception: " + clazz.getType());
        ExecutorObject messageObject = ((StackObject) result.getReturnValue()).value();
        if (messageObject == null) return null;
        return ExecutorTypeUtils.fromExecutorString(context, ((StackObject) result.getReturnValue()).value());
    }

}
