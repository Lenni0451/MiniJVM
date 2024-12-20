package net.lenni0451.minijvm.stack;

public record StackLong(long value) implements StackElement {

    @Override
    public int size() {
        return 2;
    }

}
