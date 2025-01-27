package net.lenni0451.minijvm.execution;

import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class Executor {

    public static ExecutionResult execute(final ExecutionContext context, final ExecutorClass currentClass, final MethodNode currentMethod, final ExecutorObject instance, final StackElement... arguments) {
        if (ExecutionManager.DEBUG) {
            System.out.println("Invoking method: " + currentClass.getClassNode().name + " " + currentMethod.name + currentMethod.desc);
        }
        boolean isStatic = Modifiers.has(currentMethod.access, Opcodes.ACC_STATIC);
        if (isStatic && instance != null) {
            throw new IllegalStateException("Tried to execute a static method with an instance");
        } else if (!isStatic && instance == null) {
            throw new IllegalStateException("Tried to execute an instance method without an instance");
        }

        context.pushStackFrame(currentClass, currentMethod, Modifiers.has(currentMethod.access, Opcodes.ACC_NATIVE) ? -2 : -1);
        MethodExecutor methodExecutor = context.getExecutionManager().getMethodExecutor(context, currentClass.getClassNode().name, currentMethod);
        ExecutionResult result = methodExecutor.execute(context, currentClass, currentMethod, instance, arguments);
        if (!currentMethod.desc.endsWith("V") && !result.hasException() && !result.hasReturnValue()) {
            throw new ExecutorException(context, "Method " + currentClass.getClassNode().name + "." + currentMethod.name + currentMethod.desc + " did not return a value");
        } else if (currentMethod.desc.endsWith("V") && result.hasReturnValue()) {
            throw new ExecutorException(context, "Void method " + currentClass.getClassNode().name + "." + currentMethod.name + currentMethod.desc + " returned a value");
        }
        context.popStackFrame();
        if (ExecutionManager.DEBUG) {
            System.out.println("----- Finished " + currentClass.getClassNode().name + " " + currentMethod.name + currentMethod.desc + " execution with result " + result + " -----");
        }
        return result;
    }

}
