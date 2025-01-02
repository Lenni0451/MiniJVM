package net.lenni0451.minijvm.stack;

import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Type;

public record StackObject(Type stackType, ExecutorObject value) implements StackElement {

    public static StackObject NULL = new StackObject(Types.OBJECT, null);

    public StackObject(final ExecutorObject value) {
        this(value.getClazz().getType(), value);
    }

    public StackObject withType(final Type type) {
        return new StackObject(type, this.value);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public StackElement normalize() {
        if (this.value == null) return this;
        if (this.stackType.equals(this.value.getClazz().getType())) return this;
        else return new StackObject(this.value);
    }

    @Override
    public boolean isNull() {
        return this.value == null;
    }

}
