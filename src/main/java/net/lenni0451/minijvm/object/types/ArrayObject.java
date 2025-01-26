package net.lenni0451.minijvm.object.types;

import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;

public class ArrayObject extends ExecutorObject {

    private final StackElement[] elements;

    public ArrayObject(final ExecutionContext executionContext, final ExecutorClass clazz, final StackElement[] elements) {
        super(executionContext, clazz);
        this.elements = elements;
    }

    public StackElement[] getElements() {
        return this.elements;
    }

    @Override
    public String toString() {
        return "ArrayObject{" + this.getClazz().getClassNode().name + "=" + this.elements.length + "}";
    }

}
