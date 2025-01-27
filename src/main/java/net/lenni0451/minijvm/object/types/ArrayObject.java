package net.lenni0451.minijvm.object.types;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;

public class ArrayObject extends ExecutorObject {

    private final StackElement[] elements;

    public ArrayObject(final ExecutionContext context, final ExecutorClass clazz, final StackElement[] elements) {
        super(context, clazz);
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
