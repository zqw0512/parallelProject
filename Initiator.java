package Project_2;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Initiator {

	int nodePort1;
	int nodePort2;
	ExecutorService pool;

	public Initiator(int nodePort1, int nodePort2) throws Exception {
		super();
		this.nodePort1 = nodePort1;
		this.nodePort2 = nodePort2;
		this.pool = Executors.newFixedThreadPool(4);
	}

	ObjectInputStream inFromNode1;
	ObjectOutputStream outToNode1;
	ObjectInputStream inFromNode2;
	ObjectOutputStream outToNode2;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new Initiator(10000, 10001).start();
	}

	public void start() throws Exception {
		// TODO Auto-generated method stub

		int[] a = new int[] { 38, 6, 23, 2, 62, 4, 72, 7, 83, 5, 23, 6, 24, 2 };
		// int[] a = NumberGenerator.randomArray(10000000, 10000000);
		// int[] a = NumberGenerator.reverseOrderedArray(10000, 100);
		System.out.println(Arrays.toString(a));
		int[] a1 = new int[a.length / 2];
		int[] a2 = new int[a.length - a1.length - 1];
		System.arraycopy(a, 1, a1, 0, a1.length);
		System.arraycopy(a, a1.length + 1, a2, 0, a2.length);

		Socket node1Socket = new Socket("localhost", nodePort1);
		outToNode1 = new ObjectOutputStream(node1Socket.getOutputStream());
		inFromNode1 = new ObjectInputStream(node1Socket.getInputStream());
		Socket node2Socket = new Socket("localhost", nodePort2);
		outToNode2 = new ObjectOutputStream(node2Socket.getOutputStream());
		inFromNode2 = new ObjectInputStream(node2Socket.getInputStream());

		Future<?> array1Send = pool.submit(new SocketSend(outToNode1, a1));
		Future<?> array2Send = pool.submit(new SocketSend(outToNode2, a2));

		Scanner input = new Scanner(System.in);
		System.out.print("Input k: ");
		int k = input.nextInt();

		array1Send.get();
		array2Send.get();

		int kValue = findk(a, k, 0, a.length - 1);
		System.out.println("The value of the k-th number is " + kValue);

		node1Socket.close();
		node2Socket.close();
		input.close();
		pool.shutdown();
	}

	public int findk(int[] a, int k, int start, int end) throws Exception {
		int kIndex = k - 1;
		int pivot = a[0];

		while (true) {
			pool.submit(new SocketSend(outToNode1, new Message(Sign.PARTITION, pivot)));
			pool.submit(new SocketSend(outToNode2, new Message(Sign.PARTITION, pivot)));
			Future<Message> msg1Future = pool.submit(new SocketReceive(inFromNode1));
			Future<Message> msg2Future = pool.submit(new SocketReceive(inFromNode2));
			int pivotIndex = msg1Future.get().count + msg2Future.get().count;
            
                Side side;
            if(pivotIndex>k){
                side = Side.LEFT;
            }else{
                side = Side.RIGHT;
            }
                
			Future<?> node1ChooseSide = pool.submit(new SocketSend(outToNode1, new Message(sign.CHOOSE,side)));
			Future<?> node2ChooseSide = pool.submit(new SocketSend(outToNode2, new Message(sign.CHOOSE,side)));
                
                
			node1ChooseSide.get();
			pool.submit(new SocketSend(outToNode1, new Message(Sign.REQUEST)));
			Message msg = pool.submit(new SocketReceive(inFromNode1)).get();
			if (msg.getPivot) {
				pivot = msg.pivot;
			} else {
				node2ChooseSide.get();
				pool.submit(new SocketSend(outToNode2, new Message(Sign.REQUEST)));
				pivot = pool.submit(new SocketReceive(inFromNode2)).get().pivot;
			}
		}
		return pivot;
	}

	public class SocketReceive implements Callable<Message> {
		ObjectInputStream input;

		public SocketReceive(ObjectInputStream input) {
			super();
			this.input = input;
		}

		@Override
		public Message call() throws Exception {
			// TODO Auto-generated method stub
			return (Message) input.readObject();
		}
	}

	public class SocketSend implements Runnable {
		ObjectOutputStream output;
		Object data;

		public SocketSend(ObjectOutputStream output, Object data) {
			super();
			this.output = output;
			this.data = data;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				output.writeObject(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
