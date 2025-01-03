package net.lenni0451.minijvm.stack;

public record StackLong(long value) implements StackElement {

    public static final StackLong ZERO = new StackLong(0);
    public static final StackLong ONE = new StackLong(1);

    @Override
    public int size() {
        return 2;
    }

}
