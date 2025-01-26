package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.stack.StackElement;

import java.util.Arrays;

public class ExecutorStack {

    private final ExecutionContext executionContext;
    private final StackElement[] stack;
    private int stackPointer = 0;

    public ExecutorStack(final ExecutionContext executionContext, final int maxSize) {
        this.executionContext = executionContext;
        this.stack = new StackElement[maxSize];
    }

    public void clear() {
        this.stackPointer = 0;
        Arrays.fill(this.stack, null);
    }

    public StackElement[] getStack() {
        return Arrays.copyOf(this.stack, this.stackPointer);
    }

    public void push(final StackElement element) {
        switch (element.size()) {
            case 1:
                if (this.stackPointer >= this.stack.length) {
                    throw new ExecutorException(this.executionContext, "Tried to push an element to the stack but the stack is full");
                }
                this.stack[this.stackPointer++] = element;
                break;
            case 2:
                if (this.stackPointer + 1 >= this.stack.length) {
                    throw new ExecutorException(this.executionContext, "Tried to push an element to the stack but the stack is full");
                }
                this.stack[this.stackPointer++] = element;
                this.stack[this.stackPointer++] = element;
                break;
            default:
                throw new ExecutorException(this.executionContext, "Tried to push an element to the stack with a size of " + element.size() + " but the stack only supports elements with a size of 1 or 2");
        }
    }

    public StackElement pop() {
        if (this.stackPointer == 0) {
            throw new ExecutorException(this.executionContext, "Tried to pop an element from an empty stack");
        }
        StackElement previous = this.stack[--this.stackPointer];
        this.stack[this.stackPointer] = null;
        return previous;
    }

    public StackElement popSized() {
        StackElement element = this.pop();
        if (element.size() == 2 && this.pop() != element) {
            throw new ExecutorException(this.executionContext, "Tried to pop an element from the stack with a size of " + element.size() + " but the stack contains an element of different type");
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
        if (this.stackPointer == 0) {
            throw new ExecutorException(this.executionContext, "Tried to peek an element from an empty stack");
        }
        return this.stack[this.stackPointer - 1];
    }

    public void swap() {
        if (this.stackPointer < 2) {
            throw new ExecutorException(this.executionContext, "Tried to swap the top two elements of the stack but the stack size is smaller than 2");
        }
        StackElement first = this.pop();
        StackElement second = this.pop();
        this.push(first);
        this.push(second);
    }

    public void dup() {
        if (this.stackPointer == 0) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top element of the stack but the stack is empty");
        }
        this.push(this.peek());
    }

    public void dupX1() {
        if (this.stackPointer < 2) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top element of the stack and insert it below the second element but the stack size is smaller than 2");
        }
        StackElement first = this.pop();
        StackElement second = this.pop();
        this.push(first);
        this.push(second);
        this.push(first);
    }

    public void dupX2() {
        if (this.stackPointer < 3) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top element of the stack and insert it below the third element but the stack size is smaller than 3");
        }
        StackElement first = this.pop();
        StackElement second = this.pop();
        StackElement third = this.pop();
        this.push(first);
        this.push(third);
        this.push(second);
        this.push(first);
    }

    public void dup2() {
        if (this.stackPointer < 2) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top two elements of the stack but the stack size is smaller than 2");
        }
        StackElement first = this.pop();
        StackElement second = this.pop();
        this.push(second);
        this.push(first);
        this.push(second);
        this.push(first);
    }

    public void dup2X1() {
        if (this.stackPointer < 3) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top two elements of the stack and insert them below the third element but the stack size is smaller than 3");
        }
        StackElement first = this.pop();
        StackElement second = this.pop();
        StackElement third = this.pop();
        this.push(second);
        this.push(first);
        this.push(third);
        this.push(second);
        this.push(first);
    }

    public void dup2X2() {
        if (this.stackPointer < 4) {
            throw new ExecutorException(this.executionContext, "Tried to duplicate the top two elements of the stack and insert them below the fourth element but the stack size is smaller than 4");
        }
        StackElement first = this.pop();
        StackElement second = this.pop();
        StackElement third = this.pop();
        StackElement fourth = this.pop();
        this.push(second);
        this.push(first);
        this.push(fourth);
        this.push(third);
        this.push(second);
        this.push(first);
    }

}
