package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

public class AccessControllerNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/security/AccessController.getStackAccessControlContext()Ljava/security/AccessControlContext;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(StackObject.NULL);
        });
    }

}
