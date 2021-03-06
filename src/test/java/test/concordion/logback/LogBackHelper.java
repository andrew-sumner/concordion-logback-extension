package test.concordion.logback;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.concordion.logback.html.HTMLLayout;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LogBackHelper {
	private static String HTML_FILE_APPENDER = "HTML-FILE-PER-TEST";
	private static String TEXT_FILE_APPENDER = "FILE-PER-TEST";
	private static String CONSOLE_APPENDER = "STDOUT";
	
	public static Logger getRootLogger() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		return context.getLogger(Logger.ROOT_LOGGER_NAME);
	}

	public static boolean isConfiguredForHtmlLog() {
		return getSiftingAppender(HTML_FILE_APPENDER) != null;
	}

	public static boolean isConfiguredForTextLog() {
		return getSiftingAppender(TEXT_FILE_APPENDER) != null;
	}

	private static SiftingAppender getSiftingAppender(String appenderName) {
		return (SiftingAppender) getRootLogger().getAppender(appenderName);
	}

	private static FileAppender<?> getFileAppender(SiftingAppender siftingAppender) {
		if (siftingAppender != null) {
			for (Appender<ILoggingEvent> appender : siftingAppender.getAppenderTracker().allComponents()) {
				if (appender instanceof FileAppender) {
					return (FileAppender<?>) appender;
				}
			}
		}

		throw new IllegalStateException("Requested appender is not configured");
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getLayout(String appenderName, Class<T> expectedClass) {
		FileAppender<?> fileAppender = getFileAppender(getSiftingAppender(appenderName));

		if (fileAppender.getEncoder() instanceof LayoutWrappingEncoder<?> == false) {
			throw new IllegalStateException(appenderName + " encoder is not configured");
		}

		LayoutWrappingEncoder<?> encoder = (LayoutWrappingEncoder<?>) fileAppender.getEncoder();

		if (encoder.getLayout().getClass() == expectedClass) {
			return (T) encoder.getLayout();
		}

		throw new IllegalStateException(appenderName + " layout is not configured");		
	}
	
	public static HTMLLayout getHtmlLayout() {
		return getLayout(HTML_FILE_APPENDER, HTMLLayout.class);
	}

	public static PatternLayout getTextLayout() {
		return getLayout(TEXT_FILE_APPENDER, PatternLayout.class);
	}

	public static Layout<ILoggingEvent> getConsoleLayout() {
		ConsoleAppender<?> consoleAppender = (ConsoleAppender<?>) getRootLogger().getAppender(CONSOLE_APPENDER);

		if (consoleAppender != null) {
			PatternLayoutEncoder encoder = (PatternLayoutEncoder) consoleAppender.getEncoder();
			return encoder.getLayout();
		}

		throw new IllegalStateException(CONSOLE_APPENDER + " appender is not configured");
	}
	
	public static void switchToClassicLoggerConfiguration() {
		StringBuilder sb = new StringBuilder();

		sb.append("<configuration>");
		sb.append("  <include resource=\"logback-include.xml\"/>");
		sb.append("  <root level=\"ALL\">");
		sb.append("    <appender-ref ref=\"STDOUT\" />");
		sb.append("    <appender-ref ref=\"FILE-PER-TEST\" />");
		sb.append("  </root>");
		sb.append("</configuration>");

		InputStream stream = new ByteArrayInputStream(sb.toString().getBytes());
		
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			loggerContext.reset();
			configurator.doConfigure(stream);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}

		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
	}

	public static void restoreLoggerConfiguration() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		ContextInitializer ci = new ContextInitializer(loggerContext);
		URL url = ci.findURLOfDefaultConfigurationFile(true);

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			loggerContext.reset();
			configurator.doConfigure(url);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}

		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
	}
}
