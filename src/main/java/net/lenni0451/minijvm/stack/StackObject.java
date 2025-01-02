package net.lenni0451.minijvm.stack;

import net.lenni0451.minijvm.object.ExecutorObject;

public record StackObject(ExecutorObject value) implements StackElement {

    public static StackObject NULL = new StackObject(null);

    public StackObject {
        if (value == null && NULL != null) throw new IllegalArgumentException("Value cannot be null");
    }

    @Override
    public int size() {
        return 1;
    }

}
