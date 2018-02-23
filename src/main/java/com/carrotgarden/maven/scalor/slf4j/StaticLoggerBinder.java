package com.carrotgarden.maven.scalor.slf4j;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * https://github.com/jcabi/jcabi-maven-slf4j/tree/master/src/main/java/org/slf4j/impl
 * 
 * The binding of {@link ILoggerFactory} class with an actual instance of
 * {@link ILoggerFactory} is performed using information returned by this class.
 *
 * <p>
 * This is what you should do in your Maven plugin (before everything else):
 *
 * <pre>
 * import org.apache.maven.plugin.AbstractMojo;
 * import org.slf4j.impl.StaticLoggerBinder;
 * 
 * public class MyMojo extends AbstractMojo {
 * 	&#64;Override
 * 	public void execute() {
 * 		StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
 * 		// ... all the rest
 * 	}
 * }
 * </pre>
 *
 * <p>
 * All SLF4J calls will be forwarded to Maven Log.
 *
 * <p>
 * The class is thread-safe. *
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

	/**
	 * The unique instance of this class.
	 */
	protected static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

	/**
	 * The {@link ILoggerFactory} instance returned by the
	 * {@link #getLoggerFactory()} method should always be the same object.
	 */
	protected final transient MavenLoggerFactory loggers = new MavenLoggerFactory();

	/**
	 * Return the singleton of this class.
	 * 
	 * @return The StaticLoggerBinder singleton
	 */
	public static StaticLoggerBinder getSingleton() {
		return StaticLoggerBinder.SINGLETON;
	}

	/**
	 * Set Maven Log.
	 * 
	 * @param log
	 *            The log from Maven plugin
	 */
	public void setMavenLog(final Log log) {
		this.loggers.setMavenLog(log);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ILoggerFactory getLoggerFactory() {
		return this.loggers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLoggerFactoryClassStr() {
		return this.loggers.getClass().getName();
	}

}