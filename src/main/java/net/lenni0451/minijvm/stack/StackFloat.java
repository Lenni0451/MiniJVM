package net.lenni0451.minijvm.stack;

public record StackFloat(float value) implements StackElement {

    @Override
    public int size() {
        return 1;
    }

}
