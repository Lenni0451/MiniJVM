package net.lenni0451.minijvm.object;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public class ArrayClass extends ExecutorClass {

    private static ClassNode buildClass(final Type type) {
        ClassNode classNode = new ClassNode();
        classNode.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, type.getInternalName(), null, "java/lang/Object", new String[]{"java/lang/Cloneable", "java/io/Serializable"});
        return classNode;
    }


    public ArrayClass(final ExecutionManager executionManager, final ExecutionContext executionContext, final Type arrayType) {
        super(executionManager, executionContext, buildClass(arrayType));
    }

}
