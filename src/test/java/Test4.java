import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackDouble;
import net.lenni0451.minijvm.stack.StackElement;

public class Test4 {

    public static void main(String[] args) {
        ExecutionManager manager = new ExecutionManager(new LoaderClassProvider());
        ExecutionContext context = new ExecutionContext();

        ExecutorClass test4Class = manager.loadClass(context, "Test4");
        ExecutorClass.ResolvedMethod doitMethod = test4Class.findMethod("doit", "(D)D");
        StackElement result = Executor.execute(manager, context, test4Class, doitMethod.method(), null, new StackElement[]{new StackDouble(1 / 0.75)}).getReturnValue();
        System.out.println(result);
        System.out.println(doit(1 / 0.75));
    }

    public static double doit(final double a) {
        return Math.fma(a, 2, 45);
    }

}
