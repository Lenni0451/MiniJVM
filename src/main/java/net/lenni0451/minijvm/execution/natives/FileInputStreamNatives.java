package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.MethodExecutor;

import java.util.function.Consumer;

public class FileInputStreamNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("java/io/FileInputStream.initIDs()V", MethodExecutor.NOOP_VOID);
    }

}
