package test;

import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackDouble;
import org.objectweb.asm.Type;

public class Test2 {

    public static void main(String[] args) {
        ExecutionManager manager = new ExecutionManager(new LoaderClassProvider());
        ExecutionContext context = new ExecutionContext();

        final double d = 1 / 0.75;
        ExecutorClass test4Class = manager.loadClass(context, Type.getType(Test2.class));
        ExecutorClass.ResolvedMethod doitMethod = test4Class.findMethod(manager, context, "doit", "(D)D");
        ExecutionResult result = Executor.execute(manager, context, test4Class, doitMethod.method(), null, new StackDouble(d));
        System.out.println(result);
        System.out.println(doit(d));
    }

    public static double doit(final double a) {
        //Those math operations only work in Java version which don't implement them as native methods but with Java code
        //They typically are marked as intrinsic methods if implemented in Java
        return Math.fma(a, 2, 45) + Math.expm1(a) * Math.log1p(a) - Math.sin(a) * Math.cos(a);
    }

}
