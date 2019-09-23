package dcmtools.util;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LoggingUtils {

    public static void setLogLevel(Level level) {
        getRootLogger().setLevel(level);
    }

    public static Logger getRootLogger() {
        return (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

}
