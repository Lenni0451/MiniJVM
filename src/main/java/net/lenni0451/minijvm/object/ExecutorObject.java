package net.lenni0451.minijvm.object;

import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import java.util.HashMap;
import java.util.Map;

import static net.lenni0451.commons.asm.Types.type;

public class ExecutorObject {

    private final ExecutorClass owner;
    private final Map<FieldNode, StackElement> fields;

    public ExecutorObject(final ExecutionManager executionManager, final ExecutionContext executionContext, final ExecutorClass owner) {
        this.owner = owner;
        this.fields = new HashMap<>();

        this.initFields(executionManager, executionContext);
    }

    private void initFields(final ExecutionManager executionManager, final ExecutionContext executionContext) {
        for (ExecutorClass executorClass : this.owner.superClasses.values()) {
            for (FieldNode field : executorClass.getClassNode().fields) {
                if (Modifiers.has(field.access, Opcodes.ACC_STATIC)) continue;
                StackElement value = ExecutorTypeUtils.parse(executionManager, executionContext, field.value);
                if (value == StackObject.NULL) value = ExecutorTypeUtils.getFieldDefault(ExecutorTypeUtils.typeToStackType(type(field)));
                this.fields.put(field, value);
            }
        }
    }

    public ExecutorClass getOwner() {
        return this.owner;
    }

    public StackElement getField(final FieldNode field) {
        if (!this.fields.containsKey(field)) throw new IllegalArgumentException("Field not found: " + field.name + ":" + field.desc);
        return this.fields.get(field);
    }

    public void setField(final FieldNode field, final StackElement value) {
        if (!this.fields.containsKey(field)) throw new IllegalArgumentException("Field not found: " + field.name + ":" + field.desc);
        this.fields.put(field, value);
    }

    @Override
    public String toString() {
        return "ExecutorObject{" + this.owner + "}";
    }
}
