package com.carrotgarden.maven.scalor.slf4j;

import java.lang.reflect.*;

import org.apache.maven.plugin.logging.Log;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

/**
 * Hack around Slf4j static binder.
 */
public interface MavenSlf4j {

	static boolean hasNoop() {
		ILoggerFactory factory = LoggerFactory.getILoggerFactory();
		return factory instanceof NOPLoggerFactory;
	}

	static void setFinalStatic(Field field, Object value) throws Exception {
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, value);
	}

	static void setupFactory(Log log) throws Exception {
		String name = "NOP_FALLBACK_FACTORY";
		Field field = LoggerFactory.class.getDeclaredField(name);
		NOPLoggerFactory factory = new MavenLoggerFactory();
		setFinalStatic(field, factory);
	}

}
