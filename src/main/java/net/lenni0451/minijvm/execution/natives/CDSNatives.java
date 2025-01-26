package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.stack.StackInt;
import net.lenni0451.minijvm.stack.StackLong;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class CDSNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("jdk/internal/misc/CDS.isDumpingClassList0()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(StackInt.ZERO);
        });
        manager.registerMethodExecutor("jdk/internal/misc/CDS.isSharingEnabled0()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(StackInt.ZERO);
        });
        manager.registerMethodExecutor("jdk/internal/misc/CDS.isDumpingArchive0()Z", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(StackInt.ZERO);
        });
        manager.registerMethodExecutor("jdk/internal/misc/CDS.logLambdaFormInvoker(Ljava/lang/String;)V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("jdk/internal/misc/CDS.initializeFromArchive(Ljava/lang/Class;)V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("jdk/internal/misc/CDS.defineArchivedModules(Ljava/lang/ClassLoader;Ljava/lang/ClassLoader;)V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("jdk/internal/misc/CDS.getRandomSeedForDumping()J", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            return returnValue(StackLong.ZERO);
        });
        manager.registerMethodExecutor("jdk/internal/misc/CDS.dumpClassList(Ljava/lang/String;)V", MethodExecutor.NOOP_VOID);
        manager.registerMethodExecutor("jdk/internal/misc/CDS.dumpDynamicArchive(Ljava/lang/String;)V", MethodExecutor.NOOP_VOID);
    }

}
