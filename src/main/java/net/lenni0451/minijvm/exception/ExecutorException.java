package net.lenni0451.minijvm.exception;

import net.lenni0451.minijvm.ExecutionContext;

/**
 * An exception that is thrown when illegal operations are performed by the executed code.<br>
 * On the JVM this would a critical error that would cause the JVM to crash.
 */
public class ExecutorException extends RuntimeException {

    private final ExecutionContext.StackFrame[] stackFrames;

    public ExecutorException(final ExecutionContext executionContext, final String message) {
        super(message);
        this.stackFrames = executionContext.getStackFrames();
    }

    public ExecutorException(final ExecutionContext executionContext, final String message, final Throwable cause) {
        super(message, cause);
        this.stackFrames = executionContext.getStackFrames();
    }

    public ExecutionContext.StackFrame[] getStackFrames() {
        return this.stackFrames;
    }

}
