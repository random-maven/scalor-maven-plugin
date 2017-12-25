package com.carrotgarden.maven.scalor;

public class DemoCast {

	public static void main(String[] args) {
		DemoCast instance = new DemoCast();
		instance.doit();
	}
	
	static void log(String line) {
		System.out.println(line);
	}

	public void doit() {
		A instA = new A();
		B instB = new B();
		A[] arrayA = new A[0];
		B[] arrayB = new B[0];

		log("---");
		log("instB instanceof A : " + (instB instanceof A));
		log("instA instanceof B : " + (instA instanceof B));
		
		log("---");
		log("A.class.isInstance(instA) : " + A.class.isInstance(instA));
		log("A.class.isInstance(instB) : " + A.class.isInstance(instB));
		log("B.class.isInstance(instA) : " + B.class.isInstance(instA));
		log("B.class.isInstance(instB) : " + B.class.isInstance(instB));
		
		log("---");
		log("A.class.isAssignableFrom(B.class) : " + A.class.isAssignableFrom(B.class));
		log("B.class.isAssignableFrom(A.class) : " + B.class.isAssignableFrom(A.class));
		
		log("---");
		log("A.class.isInstance(arrayA) : " + A.class.isInstance(arrayA));
		log("A.class.isInstance(arrayB) : " + A.class.isInstance(arrayB));
		log("B.class.isInstance(arrayA) : " + B.class.isInstance(arrayA));
		log("B.class.isInstance(arrayB) : " + B.class.isInstance(arrayB));
		
		log("---");
		log("arrayA.getClass().isInstance(arrayB) : " + arrayA.getClass().isInstance(arrayB));
		log("arrayA.getClass().isAssignableFrom(arrayB.getClass()) : " + arrayA.getClass().isAssignableFrom(arrayB.getClass()));
		log("arrayB.getClass().isInstance(arrayA) : " + arrayB.getClass().isInstance(arrayA));
		log("arrayB.getClass().isAssignableFrom(arrayA.getClass()) : " + arrayB.getClass().isAssignableFrom(arrayA.getClass()));
	}

	class A {
	}

	class B extends A {
	}

}
