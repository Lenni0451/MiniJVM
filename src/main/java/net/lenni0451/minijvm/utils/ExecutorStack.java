package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.stack.StackElement;

import java.util.Stack;

public class ExecutorStack {

    private final ExecutionContext executionContext;
    private final Stack<StackElement> stack;

    public ExecutorStack(final ExecutionContext executionContext) {
        this.executionContext = executionContext;
        this.stack = new Stack<>();
    }

    public StackElement[] getStack() {
        return this.stack.toArray(new StackElement[0]);
    }

    public void push(final StackElement element) {
        for (int i = 0; i < element.size(); i++) {
            this.stack.push(element);
        }
    }

    public StackElement pop() {
        if (this.stack.isEmpty()) {
            throw new ExecutorException(this.executionContext, "Tried to pop an element from an empty stack");
        }
        return this.stack.pop();
    }

    public StackElement popSized() {
        StackElement element = this.pop();
        for (int i = 1; i < element.size(); i++) {
            StackElement other = this.stack.pop();
            if (other != element) {
                throw new ExecutorException(this.executionContext, "Tried to pop an element from the stack with a size of " + element.size() + " but the stack contains an element of different type");
            }
        }
        return element;
    }

    public <T extends StackElement> T pop(final Class<T> expected) {
        StackElement element = this.popSized();
        if (!expected.isInstance(element)) {
            throw new ExecutorException(this.executionContext, "Tried to pop " + expected.getSimpleName() + " but the top element is " + element.getClass().getSimpleName());
        }
        return (T) element;
    }

    public StackElement peek() {
        if (this.stack.isEmpty()) {
            throw new ExecutorException(this.executionContext, "Tried to peek an element from an empty stack");
        }
        return this.stack.peek();
    }

    public void swap() {
        if (this.stack.size() < 2) {
            throw new ExecutorException(this.executionContext, "Tried to swap the top two elements of the stack but the stack size is smaller than 2");
        }
        StackElement first = this.stack.pop();
        StackElement second = this.stack.pop();
        this.stack.push(first);
        this.stack.push(second);
    }

    public void dup() {
        if (this.stack.isEmpty()) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top element of the stack but the stack is empty");
        }
        this.stack.push(this.stack.peek());
    }

    public void dupX1() {
        if (this.stack.size() < 2) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top element of the stack and insert it below the second element but the stack size is smaller than 2");
        }
        StackElement first = this.stack.pop();
        StackElement second = this.stack.pop();
        this.stack.push(first);
        this.stack.push(second);
        this.stack.push(first);
    }

    public void dupX2() {
        if (this.stack.size() < 3) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top element of the stack and insert it below the third element but the stack size is smaller than 3");
        }
        StackElement first = this.stack.pop();
        StackElement second = this.stack.pop();
        StackElement third = this.stack.pop();
        this.stack.push(first);
        this.stack.push(third);
        this.stack.push(second);
        this.stack.push(first);
    }

    public void dup2() {
        if (this.stack.size() < 2) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top two elements of the stack but the stack size is smaller than 2");
        }
        StackElement first = this.stack.pop();
        StackElement second = this.stack.pop();
        this.stack.push(second);
        this.stack.push(first);
        this.stack.push(second);
        this.stack.push(first);
    }

    public void dup2X1() {
        if (this.stack.size() < 3) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top two elements of the stack and insert them below the third element but the stack size is smaller than 3");
        }
        StackElement first = this.stack.pop();
        StackElement second = this.stack.pop();
        StackElement third = this.stack.pop();
        this.stack.push(second);
        this.stack.push(first);
        this.stack.push(third);
        this.stack.push(second);
        this.stack.push(first);
    }

    public void dup2X2() {
        if (this.stack.size() < 4) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top two elements of the stack and insert them below the fourth element but the stack size is smaller than 4");
        }
        StackElement first = this.stack.pop();
        StackElement second = this.stack.pop();
        StackElement third = this.stack.pop();
        StackElement fourth = this.stack.pop();
        this.stack.push(second);
        this.stack.push(first);
        this.stack.push(fourth);
        this.stack.push(third);
        this.stack.push(second);
        this.stack.push(first);
    }

}
