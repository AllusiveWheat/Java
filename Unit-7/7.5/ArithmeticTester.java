import java.io.*;
import java.util.*;

public class ArithmeticTester {

	public static void main(String[] args) {
		System.out.println("Welcome to the Arithmetic Tester!  Some informative but not really relevant values: ");
		//printValues();
		System.out.println();
		
		System.out.println("This amazing program lets you do many things");
		
		while(true) {
			System.out.println("\n(1) Multiply integers (2) Divide integers\n"
					+ "(3) Square root an integer (4) Read a number from a file"
					+ "\n(-1) Quit");
			System.out.print("Choice: ");

			Scanner stdin = new Scanner(System.in);
			int x = stdin.nextInt();
			
			
			switch(x) { 
			case -1:
				System.out.println("Goodbye...Program terminating!");
			case 1: 
				multiply(stdin);
				break;
			case 2: 
				divide(stdin);
				break;
			case 3:
				squareRoot(stdin);
				break;
			case 4: 
				readNumFromFile();
				break;
			default:
				System.out.println("Select between choices (1) - (4) or get yourself a new program, buddy");
			}	

		}
	}
	
	private static void printValues() {
		System.out.println("Integer.MAX_VALUE: " + Integer.MAX_VALUE);
		System.out.println("Integer.MIN_VALUE: " + Integer.MIN_VALUE);
		System.out.println("Integer.MAX_VALUE + 1: " + (Integer.MAX_VALUE + 1));
		System.out.println("Long.MAX_VALUE: " + Long.MAX_VALUE);
	}
	
	private static void multiply(Scanner stdin) {
		try {
			System.out.println("Enter an int: ");
			int x = stdin.nextInt(); // could generate InputMismatchException
			System.out.println("Enter an int to multiply by: ");
			int y = stdin.nextInt();
			
			if(okToMultiply(x,y)) // could generate ArithmeticException
				System.out.println(x + " * " + y + " = " + (x * y));
		}
//		catch(RuntimeException e) {
//			System.out.println("There was a problem.");
//		}
		catch(ArithmeticException e) {
			System.out.println("Ints will overflow after multiplication!");
		}
	}
	
	private static void divide(Scanner stdin) {
		System.out.println("Enter an int: ");
		int x = stdin.nextInt(); // could generate InputMismatchException
		System.out.println("Enter an int to divide by: ");
		int y = stdin.nextInt();
		
		if(okToDivide(x,y))
			System.out.println(x + " / " + y + " = " + (x / (double)y));
	}
	
	private static void squareRoot(Scanner stdin) {
		System.out.println("Enter an int: ");
		int x = stdin.nextInt();
		
		if(radicandOK(x)) 
			System.out.println("Square root of " + x + " = " + Math.sqrt(x));
	}
	
	private static void readNumFromFile()  {
        FileReader reader = null;
        String fileName = "test.txt";
        
        int charValue = -1; // read from FileReader reader
        
        System.out.println("The number in the file was: " + Character.getNumericValue(charValue));
	}
	
	private static boolean okToMultiply(int x, int y) {
		if(y > Integer.MAX_VALUE / (double)x) 
			throw new ArithmeticException("Numbers will overflow!");
		return true;
	}
	
	private static boolean okToDivide(int x, int y) {
		return true;
	}
	
	private static boolean radicandOK(int x){
		return true;
	}
	
}
