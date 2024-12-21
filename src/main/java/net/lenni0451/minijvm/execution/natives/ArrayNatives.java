package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ClassClass;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ArrayNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/reflect/Array.newArray(Ljava/lang/Class;I)Ljava/lang/Object;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            ClassClass classClass = (ClassClass) ((StackObject) arguments[0]).value().getOwner();
            Type arrayType = Type.getType("[" + classClass.getClassNode().name);
            ExecutorClass arrayClass = executionManager.loadClass(executionContext, arrayType.getDescriptor());
            ExecutorObject array = executionManager.instantiateArray(executionContext, arrayClass, ((StackInt) arguments[1]).value());
            return returnValue(new StackObject(array));
        });
    }

}
