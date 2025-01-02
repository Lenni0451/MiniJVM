package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.Types;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ObjectNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/lang/Object.hashCode()I", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(instance.hashCode()));
        });
        manager.registerMethodExecutor("java/lang/Object.getClass()Ljava/lang/Class;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackObject(executionManager.instantiateClass(executionContext, instance.getClazz())));
        });
        manager.registerMethodExecutor("java/lang/Object.clone()Ljava/lang/Object;", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            if (instance instanceof ArrayObject) {
                ExecutorObject clone = executionManager.instantiateArray(executionContext, instance.getClazz(), ((ArrayObject) instance).getElements().clone());
                return returnValue(new StackObject(clone));
            } else {
                return ExceptionUtils.newException(executionManager, executionContext, Types.CLONE_NOT_SUPPORTED_EXCEPTION, "Object does not support cloning");
            }
        });
    }

}
