package net.lenni0451.minijvm.object;

import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.util.HashMap;
import java.util.Map;

public class ExecutorObject {

    private final ExecutorClass clazz;
    private final Map<FieldNode, StackElement> fields;

    public ExecutorObject(final ExecutionContext executionContext, final ExecutorClass clazz) {
        this.clazz = clazz;
        this.fields = new HashMap<>();

        this.initFields(executionContext);
    }

    private void initFields(final ExecutionContext executionContext) {
        for (ExecutorClass executorClass : this.clazz.superClasses.values()) {
            for (FieldNode field : executorClass.getClassNode().fields) {
                if (Modifiers.has(field.access, Opcodes.ACC_STATIC)) continue;
                StackElement value = ExecutorTypeUtils.parse(executionContext, field.value);
                if (value.isNull()) value = ExecutorTypeUtils.getFieldDefault(ExecutorTypeUtils.typeToStackType(Type.getType(field.desc)));
                this.fields.put(field, value);
            }
        }
    }

    public ExecutorClass getClazz() {
        return this.clazz;
    }

    public StackElement getField(final FieldNode field) {
        if (!this.fields.containsKey(field)) throw new IllegalArgumentException("Field not found: " + field.name + ":" + field.desc);
        return this.fields.get(field);
    }

    public void setField(final FieldNode field, final StackElement value) {
        if (!this.fields.containsKey(field)) throw new IllegalArgumentException("Field not found: " + field.name + ":" + field.desc);
        this.fields.put(field, value.normalize());
    }

    @Override
    public String toString() {
        return "ExecutorObject{" + this.clazz + "}";
    }

}
