package org.psk.playground;

interface Int1 {
	default void printName() {
		System.out.println("Hi from Int1");
	}
	
}

interface Int2 {
	default void printNames() {
		System.out.println("Hi from Int2");
	}
}

public class HelloWorld2 implements Int1, Int2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		HelloWorld2 hw = new HelloWorld2();
		hw.printNames();
	}

	@Override
	public void printNames() {
			System.out.println("Hi from HW");
	}
}
