package net.lenni0451.minijvm.stack;

public record StackDouble(double value) implements StackElement {

    @Override
    public int size() {
        return 2;
    }

}
