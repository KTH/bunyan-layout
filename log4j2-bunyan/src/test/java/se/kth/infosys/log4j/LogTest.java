package se.kth.infosys.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class LogTest {
    private static Logger LOG = LogManager.getLogger(LogTest.class);

    @Test
    public void printStuff() {
        LOG.trace("Trace log");
        LOG.debug("Debug log");
        LOG.info("Info log");
        LOG.warn("Warn log");
        LOG.error("Error log");
        LOG.fatal("Fatal log");

        LOG.error("0123456789 ".repeat(2458));

        try {
            throw new RuntimeException("A runtime exception");
        }
        catch (Exception e) {
            LOG.error("An exception", e);
        }
    }
}
