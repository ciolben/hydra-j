package epfl.project.threadpoolcomparison;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

// http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/RecursiveTask.html
/**
 * Simple exemple to understand how work the fork/join
 * @author Nicolas
 *
 */
public class FibonnaciTest {
	public static void main(String[] args) {
		Fibonacci worker = new Fibonacci(10);
		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		pool.invoke(worker);
		System.out.println(worker.result);
	}
}

@SuppressWarnings("serial")
class Fibonacci extends RecursiveTask<Integer> {
	final int n;
	public int result;

	Fibonacci(int n) {
		this.n = n;
	}

	protected Integer compute() {
		if (n <= 1)
			return n;
		Fibonacci f1 = new Fibonacci(n - 1);
		f1.fork();
		Fibonacci f2 = new Fibonacci(n - 2);
		result = f2.compute() + f1.join();
		return result;
	}
}