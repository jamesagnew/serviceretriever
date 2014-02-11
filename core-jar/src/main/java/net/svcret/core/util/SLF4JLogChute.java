package net.svcret.core.util;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;

/**
 * Implementation of a simple SLF4J system that will either latch onto
 * an existing category, or just do a simple rolling file log.
 *
 * @author Mandus Elfving
 */
public class SLF4JLogChute implements LogChute {

    private final Logger myLog;

    public SLF4JLogChute(Logger theLog) {
		myLog = theLog;
	}

	/**
     * @see org.apache.velocity.runtime.log.LogChute#init(org.apache.velocity.runtime.RuntimeServices)
     */
    public void init(RuntimeServices rs) {
    }

    /**
     * @see org.apache.velocity.runtime.log.LogChute#log(int, java.lang.String)
     */
    public void log(int level, String message) {
        switch (level) {
            case LogChute.WARN_ID:
                myLog.warn(message);
                break;
            case LogChute.INFO_ID:
                myLog.info(message);
                break;
            case LogChute.TRACE_ID:
                myLog.trace(message);
                break;
            case LogChute.ERROR_ID:
                myLog.error(message);
                break;
            case LogChute.DEBUG_ID:
            default:
                myLog.debug(message);
                break;
        }
    }

    /**
     * @see org.apache.velocity.runtime.log.LogChute#log(int, java.lang.String, java.lang.Throwable)
     */
    public void log(int level, String message, Throwable t) {
        switch (level) {
            case LogChute.WARN_ID:
                myLog.warn(message, t);
                break;
            case LogChute.INFO_ID:
                myLog.info(message, t);
                break;
            case LogChute.TRACE_ID:
                myLog.trace(message, t);
                break;
            case LogChute.ERROR_ID:
                myLog.error(message, t);
                break;
            case LogChute.DEBUG_ID:
            default:
                myLog.debug(message, t);
                break;
        }
    }

    /**
     * @see org.apache.velocity.runtime.log.LogChute#isLevelEnabled(int)
     */
    public boolean isLevelEnabled(int level) {
        switch (level) {
            case LogChute.DEBUG_ID:
                return myLog.isDebugEnabled();
            case LogChute.INFO_ID:
                return myLog.isInfoEnabled();
            case LogChute.TRACE_ID:
                return myLog.isTraceEnabled();
            case LogChute.WARN_ID:
                return myLog.isWarnEnabled();
            case LogChute.ERROR_ID:
                return myLog.isErrorEnabled();
            default:
                return true;
        }
    }
}