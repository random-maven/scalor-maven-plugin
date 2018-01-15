package com.carrotgarden.maven.design;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AnnoDemo {

	public static void main(String[] args) throws Exception {

		Foo foo = new Foo();
		Field field = foo.getClass().getDeclaredFields()[0];

		Anno anno = field.getAnnotation(Anno.class);
		System.out.println(String.format("Old properties: %s, %s, %s", anno.value(), anno.bar(), anno.barr()));

		Anno anno2 = (Anno) setAttrValue(anno, Anno.class, "value", "new");
		System.out.println(String.format("New properties: %s, %s, %s", anno2.value(), anno2.bar(), anno2.barr()));

		Anno anno3 = (Anno) setAttrValue(anno2, Anno.class, "bar", "new bar");
		System.out.println(String.format("New properties: %s, %s, %s", anno3.value(), anno3.bar(), anno3.barr()));
	}

	public static Annotation setAttrValue(Annotation anno, Class<? extends Annotation> type, String attrName,
			Object newValue) throws Exception {
		InvocationHandler handler = new AnnotationInvocationHandler(anno, attrName, newValue);
		Annotation proxy = (Annotation) Proxy.newProxyInstance(anno.getClass().getClassLoader(), new Class[] { type },
				handler);
		return proxy;
	}
}

class AnnotationInvocationHandler implements InvocationHandler {
	private Annotation orig;
	private String attrName;
	private Object newValue;

	public AnnotationInvocationHandler(Annotation orig, String attrName, Object newValue) throws Exception {
		this.orig = orig;
		this.attrName = attrName;
		this.newValue = newValue;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals(attrName) && args == null) {
			// "override" the return value for the property we want
			return newValue;

		} else {
			// keep other properties and methods we want like equals() and hashCode()
			Class<?>[] paramTypes = toClassArray(args);
			return orig.getClass().getMethod(method.getName(), paramTypes).invoke(orig, args);
		}
	}

	private static Class<?>[] toClassArray(Object[] arr) {
		if (arr == null)
			return null;
		Class<?>[] classArr = new Class[arr.length];
		for (int i = 0; i < arr.length; i++)
			classArr[i] = arr[i].getClass();
		return classArr;
	}

}

class Foo {
	@Anno(value = "old", bar = "bar", barr = "barr")
	public Object field1;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
	String value();

	String bar();

	String barr();
}