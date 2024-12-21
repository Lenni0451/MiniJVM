package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.ExecutionResult.returnValue;

public class CDSNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("jdk/internal/misc/CDS.isDumpingClassList0()Z", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(0));
        });
        manager.registerNativeExecutor("jdk/internal/misc/CDS.isSharingEnabled0()Z", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(0));
        });
        manager.registerNativeExecutor("jdk/internal/misc/CDS.isDumpingArchive0()Z", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackInt(0));
        });
        manager.registerNativeExecutor("jdk/internal/misc/CDS.logLambdaFormInvoker(Ljava/lang/String;)V", MethodExecutor.NOOP_VOID);
        manager.registerNativeExecutor("jdk/internal/misc/CDS.initializeFromArchive(Ljava/lang/Class;)V", MethodExecutor.NOOP_VOID);
        manager.registerNativeExecutor("jdk/internal/misc/CDS.defineArchivedModules(Ljava/lang/ClassLoader;Ljava/lang/ClassLoader;)V", MethodExecutor.NOOP_VOID);
        manager.registerNativeExecutor("jdk/internal/misc/CDS.getRandomSeedForDumping()J", (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(new StackLong(0));
        });
        manager.registerNativeExecutor("jdk/internal/misc/CDS.dumpClassList(Ljava/lang/String;)V", MethodExecutor.NOOP_VOID);
        manager.registerNativeExecutor("jdk/internal/misc/CDS.dumpDynamicArchive(Ljava/lang/String;)V", MethodExecutor.NOOP_VOID);
    }

}
