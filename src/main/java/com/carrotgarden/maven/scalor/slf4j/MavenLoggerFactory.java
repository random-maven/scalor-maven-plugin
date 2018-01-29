package com.carrotgarden.maven.scalor.slf4j;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLoggerFactory;

/**
 * https://github.com/jcabi/jcabi-maven-slf4j/tree/master/src/main/java/org/slf4j/impl
 *
 * * Implementation of {@link ILoggerFactory} returning the appropriate named
 * {@link MavenLoggerAdapter} instance.
 *
 * <p>
 * The class is thread-safe.
 */
public class MavenLoggerFactory extends NOPLoggerFactory {

	/**
	 * Maven log.
	 */
	protected Log mavenLog = new SystemStreamLog();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Logger getLogger(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("logger name can't be NULL");
		}
		return new MavenLoggerAdapter(this.mavenLog, name);
	}

	/**
	 * Set Maven log.
	 * 
	 * @param log
	 *            The log to set
	 */
	public void setMavenLog(final Log log) {
		this.mavenLog = log;
	}

}