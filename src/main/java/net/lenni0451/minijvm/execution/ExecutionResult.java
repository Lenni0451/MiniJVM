package net.lenni0451.minijvm.execution;

import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;

public class ExecutionResult {

    public static ExecutionResult voidResult() {
        return new ExecutionResult(null, null);
    }

    public static ExecutionResult returnValue(final StackElement returnValue) {
        return new ExecutionResult(returnValue, null);
    }

    public static ExecutionResult exception(final ExecutorObject exception) {
        return new ExecutionResult(null, exception);
    }


    private final StackElement returnValue;
    private final ExecutorObject exception;

    private ExecutionResult(final StackElement returnValue, final ExecutorObject exception) {
        this.returnValue = returnValue;
        this.exception = exception;
    }

    public boolean hasReturnValue() {
        return this.returnValue != null;
    }

    public boolean hasException() {
        return this.exception != null;
    }

    public StackElement getReturnValue() {
        return this.returnValue;
    }

    public ExecutorObject getException() {
        return this.exception;
    }

    @Override
    public String toString() {
        if (this.hasException()) {
            return "ExecutionResult{exception=" + this.exception.getClazz().getClassNode().name + "}";
        } else if (this.returnValue == null) {
            return "ExecutionResult{void}";
        } else {
            return "ExecutionResult{returnValue=" + this.returnValue + "}";
        }
    }

}
