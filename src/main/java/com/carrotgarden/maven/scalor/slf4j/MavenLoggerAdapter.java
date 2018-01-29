package com.carrotgarden.maven.scalor.slf4j;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * https://github.com/jcabi/jcabi-maven-slf4j/tree/master/src/main/java/org/slf4j/impl
 * 
 * Implementation of {@link org.slf4j.Logger} transforming SLF4J messages to
 * Maven log messages.
 *
 * <p>
 * The class has too many methods, but we can't do anything with this since the
 * parent class requires us to implement them all.
 *
 * <p>
 * The class is thread-safe.
 */
public class MavenLoggerAdapter extends MarkerIgnoringBase {

	/**
	 * Serialization ID.
	 */
	public static final long serialVersionUID = 0x12C0976798AB5439L;

	/**
	 * The log to use.
	 */
	private final transient Log mlog;

	/**
	 * The name of the log.
	 */
	private final transient String label;

	/**
	 * Public ctor.
	 * 
	 * @param log
	 *            The log to use
	 * @param name
	 *            The label of the logger
	 */
	public MavenLoggerAdapter(final Log log, final String name) {
		super();
		this.mlog = log;
		this.label = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return this.getClass().getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String msg) {
		this.mlog.debug(this.decorate(msg));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String format, final Object arg) {
		this.mlog.debug(this.decorate(this.format(format, arg)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String format, final Object first, final Object second) {
		this.mlog.debug(this.decorate(this.format(format, first, second)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String format, final Object... array) {
		this.mlog.debug(this.decorate(this.format(format, array)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final String msg, final Throwable thr) {
		this.mlog.debug(this.decorate(msg), thr);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDebugEnabled() {
		return this.mlog.isDebugEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String msg) {
		this.mlog.debug(this.decorate(msg));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String format, final Object arg) {
		this.mlog.debug(this.decorate(this.format(format, arg)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String format, final Object first, final Object second) {
		this.mlog.debug(this.decorate(this.format(format, first, second)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String format, final Object... array) {
		this.mlog.debug(this.decorate(this.format(format, array)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String msg, final Throwable thr) {
		this.mlog.debug(this.decorate(msg), thr);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String msg) {
		this.mlog.info(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String format, final Object arg) {
		this.mlog.info(this.format(format, arg));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String format, final Object first, final Object second) {
		this.mlog.info(this.format(format, first, second));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String format, final Object... array) {
		this.mlog.info(this.format(format, array));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String msg, final Throwable thr) {
		this.mlog.info(msg, thr);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String msg) {
		this.mlog.warn(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String format, final Object arg) {
		this.mlog.warn(this.format(format, arg));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String format, final Object... array) {
		this.mlog.warn(this.format(format, array));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String format, final Object first, final Object second) {
		this.mlog.warn(this.format(format, first, second));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String msg, final Throwable thr) {
		this.mlog.warn(msg, thr);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String msg) {
		this.mlog.error(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String format, final Object arg) {
		this.mlog.error(this.format(format, arg));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String format, final Object first, final Object second) {
		this.mlog.error(this.format(format, first, second));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String format, final Object... array) {
		this.mlog.error(this.format(format, array));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String msg, final Throwable thr) {
		this.mlog.error(msg, thr);
	}

	/**
	 * Format with one object.
	 * 
	 * @param format
	 *            Format to use
	 * @param arg
	 *            One argument
	 * @return The message
	 */
	private String format(final String format, final Object arg) {
		final FormattingTuple tuple = MessageFormatter.format(format, arg);
		return tuple.getMessage();
	}

	/**
	 * Format with two objects.
	 * 
	 * @param format
	 *            Format to use
	 * @param first
	 *            First argument
	 * @param second
	 *            Second argument
	 * @return The message
	 */
	private String format(final String format, final Object first, final Object second) {
		final FormattingTuple tuple = MessageFormatter.format(format, first, second);
		return tuple.getMessage();
	}

	/**
	 * Format with array.
	 * 
	 * @param format
	 *            Format to use
	 * @param array
	 *            List of arguments
	 * @return The message
	 */
	private String format(final String format, final Object[] array) {
		final FormattingTuple tuple = MessageFormatter.format(format, array);
		return tuple.getMessage();
	}

	/**
	 * Decorate a message with a label prefix.
	 * 
	 * @param msg
	 *            The text to decorate
	 * @return The message decorated
	 */
	private String decorate(final String msg) {
		return String.format("%s %s: %s", Thread.currentThread().getName(), this.label, msg);
	}

}