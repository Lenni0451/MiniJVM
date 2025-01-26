package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;

import java.util.function.Consumer;

public class FileDescriptorNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/io/FileDescriptor.initIDs()V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("java/io/FileDescriptor.getHandle(I)J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return ExecutionResult.returnValue(new StackLong(((StackInt) arguments[0]).value()));
        });
        manager.registerMethodExecutor("java/io/FileDescriptor.getAppend(I)Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            int id = ((StackInt) arguments[0]).value();
            if (id == 0 /*stdin*/) {
                return ExecutionResult.returnValue(StackInt.ZERO);
            } else if (id == 1 /*stdout*/ || id == 2/*stderr*/) {
                return ExecutionResult.returnValue(StackInt.ONE);
            } else {
                throw new ExecutorException(executionContext, "TODO - Invalid file descriptor: " + id);
            }
        });
    }

}
