package net.lenni0451.minijvm.stack;

import net.lenni0451.minijvm.object.ExecutorObject;

public record StackObject(ExecutorObject value) implements StackElement {

    public static StackObject NULL = new StackObject(null);

    @Override
    public int size() {
        return 1;
    }

}
