package com.carrotgarden.maven.scalor;

import java.util.IdentityHashMap;
import java.util.Map;

public class HashCodeDemo {

	static void log(String line) {
		System.out.println(line);
	}

	static void report(Object obj1, Object obj2) {
		log("obj1.hashCode() = " + obj1.hashCode());
		log("obj2.hashCode() = " + obj2.hashCode());
		log("(obj1 == obj2) : " + (obj1 == obj2));
	}

	public static void main(String[] args) {

		int limit = 10 * 1000 * 1000;

		Map<Integer, Object> map = new IdentityHashMap<>(limit);

		log("start");

		for (int count = 0; count < limit; count++) {
			// Object obj = new String(Integer.toString(count));
			Object obj = new Object();
			int code = System.identityHashCode(obj);
			if (map.containsKey(code)) {
				log("Collision after making " + count + " objects.");
				report(map.get(obj.hashCode()), obj);
				System.exit(0);
			}
			map.put(code, obj);
		}

		log("finish");

	}

}
