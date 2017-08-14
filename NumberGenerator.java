package Project_2;

import java.util.Arrays;
import java.util.Random;

public class NumberGenerator {

	public static int[] randomArray(int max, int number) {
		Random rand = new Random(System.currentTimeMillis());
		int[] array = new int[number];
		for (int i = 0; i < array.length; i++) {
			array[i] = rand.nextInt(max) + 1;
		}
		return array;
	}
	
	public static int[] orderedArray(int max, int number) {
		int[] array = new int[number];
		for (int i = 0; i < array.length; i++) {
			array[i] = i + 1;
		}
		return array;
	}
	
	public static int[] reverseOrderedArray(int max, int number) {
		int[] array = new int[number];
		for (int i = 0; i < array.length; i++) {
			array[i] = number - i;
		}
		return array;
	}

	public static int One(int max) {
		Random rand = new Random(System.currentTimeMillis());
		return rand.nextInt(max);
	}

	public static void print(int[] a) {
		System.out.println(Arrays.toString(a));
	}
}
