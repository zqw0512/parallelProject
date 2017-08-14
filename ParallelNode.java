package Project_2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelNode implements Runnable {

	int port;
	ExecutorService pool;

	public ParallelNode(int port) {
		super();
		this.port = port;
		pool = Executors.newFixedThreadPool(4);
	}

	public static void main(String[] args) throws Exception {
		new Thread(new ParallelNode(10000)).start();
		new Thread(new ParallelNode(10001)).start();
	}

	ObjectInputStream inFromInitiator;
	ObjectOutputStream outToInitiator;
	ServerSocket serverSocket;

	public void run() {
		// TODO Auto-generated method stub
		try {
			serverSocket = new ServerSocket(port);
			Socket initiatorSocket = serverSocket.accept();

			inFromInitiator = new ObjectInputStream(initiatorSocket.getInputStream());
			outToInitiator = new ObjectOutputStream(initiatorSocket.getOutputStream());

			int[] a = (int[]) inFromInitiator.readObject();
			// System.out.println(Arrays.toString(a));

			int start = 0;
			int end = a.length - 1;
			int pivot, count = 0;

			while (end >= start) {
				Message msg = (Message) inFromInitiator.readObject();
				switch (msg.sign) {
				case PARTITION:
					pivot = msg.pivot;
					if (end - start >= 4) {
						count = parallelPartition(a, pivot, start, end);
					} else {
						count = sequentialPartition(a, pivot, start, end);
					}
					outToInitiator.writeObject(new Message(count));
					break;
				case CHOOSE:
					if (msg.side == Side.LEFT) {
						end = start + count - 1;
					} else {
						start = start + count;
					}
					break;
				case REQUEST:
					outToInitiator.writeObject(new Message(true, a[start]));
					start++;
					break;
				}
			}
			while (!initiatorSocket.isClosed()) {
				inFromInitiator.readObject();
				outToInitiator.writeObject(new Message(false));
			}
			
			serverSocket.close();
			pool.shutdown();
		} catch (Exception e) {
		}
	}

	public int sequentialPartition(int[] a, int pivot, int start, int end) {
		int i = start, j = end;
		int temp = a[i];
		while (i < j) {
			while (i < j && pivot <= a[j]) {
				j--;
			}
			a[i] = a[j];
			while (i < j && pivot >= a[i]) {
				i++;
			}
			a[j] = a[i];
		}
		a[i] = temp;
		if (a[i] < pivot)
			return i - start + 1;
		else
			return i - start;
	}

	public int parallelPartition(int[] a, int pivot, int start, int end) throws Exception {
		int mid = (start + end) / 2;
		int quarter = (start + mid) / 2;
		int threeQuarter = (mid + end) / 2;
		int[] temp = new int[end - start + 1];
		Future<Integer> future1 = pool.submit(new Partition(a, pivot, start, quarter));
		Future<Integer> future2 = pool.submit(new Partition(a, pivot, quarter + 1, mid));
		Future<Integer> future3 = pool.submit(new Partition(a, pivot, mid + 1, threeQuarter));
		Future<Integer> future4 = pool.submit(new Partition(a, pivot, threeQuarter + 1, end));
		int result1 = future1.get();
		int result2 = future2.get();
		int result3 = future3.get();
		int result4 = future4.get();
		int sum = result1 + result2 + result3 + result4;
		ArrayList<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
		tasks.add(new ArrayCopy(a, start, temp, 0, result1));
		tasks.add(new ArrayCopy(a, quarter + 1, temp, result1, result2));
		tasks.add(new ArrayCopy(a, mid + 1, temp, result1 + result2, result3));
		tasks.add(new ArrayCopy(a, threeQuarter + 1, temp, result1 + result2 + result3, result4));
		tasks.add(new ArrayCopy(a, start + result1, temp, sum, quarter - start - result1 + 1));
		tasks.add(new ArrayCopy(a, quarter + result2 + 1, temp, sum + quarter - start - result1 + 1,
				mid - quarter - result2));
		tasks.add(new ArrayCopy(a, mid + result3 + 1, temp, result3 + result4 - start + mid + 1,
				threeQuarter - mid - result3));
		tasks.add(new ArrayCopy(a, threeQuarter + result4 + 1, temp, result4 - start + threeQuarter + 1,
				end - threeQuarter - result4));
		pool.invokeAll(tasks);
		System.arraycopy(temp, 0, a, start, temp.length);
		return sum;
	}

	public class Partition implements Callable<Integer> {

		private int[] a;
		private int pivot;
		private int start;
		private int end;

		public Partition(int[] a, int pivot, int start, int end) {
			this.a = a;
			this.pivot = pivot;
			this.start = start;
			this.end = end;
		}

		public Integer call() {
			int i = start, j = end;
			int temp = a[i];
			while (i < j) {
				while (i < j && pivot <= a[j]) {
					j--;
				}
				a[i] = a[j];
				while (i < j && pivot >= a[i]) {
					i++;
				}
				a[j] = a[i];
			}
			a[i] = temp;
			if (temp < pivot) {
				return i - start + 1;
			} else {
				return i - start;
			}
		}
	}

	public class ArrayCopy implements Callable<Boolean> {

		private int[] a;
		private int starta;
		private int[] b;
		private int startb;
		private int length;

		public ArrayCopy(int[] a, int starta, int[] b, int startb, int length) {
			this.a = a;
			this.starta = starta;
			this.b = b;
			this.startb = startb;
			this.length = length;
		}

		public Boolean call() {
			System.arraycopy(a, starta, b, startb, length);
			return true;
		}
	}
}
