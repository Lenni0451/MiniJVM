package test;

import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackDouble;
import org.objectweb.asm.Type;

import java.text.DecimalFormat;

public class Benchmark {

    public static void main(String[] args) {
        ExecutionManager manager = new ExecutionManager(new LoaderClassProvider());
        ExecutionContext context = new ExecutionContext();

        final double d = 1 / 0.75;
        ExecutorClass test4Class = manager.loadClass(context, Type.getType(Benchmark.class));
        ExecutorClass.ResolvedMethod doitMethod = test4Class.findMethod("run", "()J");
        long start = System.nanoTime();
        ExecutionResult result = Executor.execute(manager, context, test4Class, doitMethod.method(), null, new StackDouble(d));
        System.out.println(result);
        System.out.println("MiniJVM: " + new DecimalFormat().format(System.nanoTime() - start));

        start = System.nanoTime();
        System.out.println(run());
        System.out.println("Java: " + new DecimalFormat().format(System.nanoTime() - start));
    }

    public static long run() {
        byte[] bytes = new byte[256 * 1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = i % 127 == 0 ? 5 : (byte) (i % 127);
        }

        long sum = 0;
        for (byte aByte : bytes) {
            sum += modify(aByte);

            switch (aByte) {
                case 5:
                    sum *= 2;
                    break;
                case 10:
                    sum *= 3;
                    break;
                case 20:
                    sum *= 4;
                    break;
            }

            if (aByte == 5) {
                sum /= 2;
            } else if (aByte == 10) {
                sum /= 3;
            } else if (aByte == 20) {
                sum /= 4;
            }
        }
        return sum;
    }

    private static int modify(int num) {
        return num * 2;
    }

}
