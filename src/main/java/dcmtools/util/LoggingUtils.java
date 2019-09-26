package dcmtools.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

public class LoggingUtils {

	public static void setLogLevel(Class<?> c, Level level) {
		Configurator.setLevel(LogManager.getLogger(c).getName(), level);
	}

	public static void setLogLevel(Level level) {
		Configurator.setAllLevels(LogManager.getRootLogger().getName(), level);
	}

}
