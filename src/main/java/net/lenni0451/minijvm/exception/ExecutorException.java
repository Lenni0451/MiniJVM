package net.lenni0451.minijvm.exception;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;

import java.io.PrintStream;

/**
 * An exception that is thrown when illegal operations are performed by the executed code.<br>
 * On the JVM this would a critical error that would cause the JVM to crash.
 */
public class ExecutorException extends RuntimeException {

    private static String getMessage(final ExecutionContext context, final ExecutorObject executorCause) {
        try {
            return ExceptionUtils.getMessage(context, executorCause);
        } catch (Throwable t) {
            return "";
        }
    }


    private final ExecutionContext.StackFrame[] stackFrames;

    public ExecutorException(final ExecutionContext context, final String message) {
        super(message);
        this.stackFrames = context.getStackFrames();
    }

    public ExecutorException(final ExecutionContext context, final String message, final Throwable cause) {
        super(message, cause);
        this.stackFrames = context.getStackFrames();
    }

    public ExecutorException(final ExecutionContext context, final String message, final ExecutorObject executorCause) {
        super(message + " - " + executorCause.getClazz().getType().getClassName() + ": " + getMessage(context, executorCause));
        this.stackFrames = context.getStackFrames();
    }

    public ExecutionContext.StackFrame[] getStackFrames() {
        return this.stackFrames;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        s.println("VM Stack Trace:");
        for (int i = this.stackFrames.length - 1; i >= 0; i--) {
            s.println("\t" + this.stackFrames[i]);
        }
    }

}
