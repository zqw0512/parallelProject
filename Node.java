package Project_2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Node implements Runnable {

	int port;

	public Node(int port) {
		super();
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		new Thread(new Node(10000)).start();
		new Thread(new Node(10001)).start();
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

			int start = 0;
			int end = a.length - 1;
			int pivot, count = 0;

			while (end >= start) {
				Message msg = (Message) inFromInitiator.readObject();
				switch (msg.sign) {
				case PARTITION:
					pivot = msg.pivot;
					count = partition(a, pivot, start, end);
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
		} catch (Exception e) {
		}
	}

	public static int partition(int[] a, int pivot, int start, int end) {
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
}
