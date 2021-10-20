package se.kth.infosys.logback;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {
    private static Logger LOG = LoggerFactory.getLogger(LogTest.class);

    @Test
    public void printStuff() {
        LOG.trace("Trace log");
        LOG.debug("Debug log");
        LOG.info("Info log");
        LOG.warn("Warn log");
        LOG.error("Error log");
        LOG.error("0123456789 ".repeat(2458));
        try {
            throw new RuntimeException("A runtime exception", nestedException1());
        }
        catch (Exception e) {
            LOG.error("An exception", e);
        }
    }

    private RuntimeException nestedException1() {
        try {
            nestedException2();
        } catch (RuntimeException e) {
            return new RuntimeException("nested1", e);
        }
        return null;
    }

    private RuntimeException nestedException2() {
        throw new RuntimeException("nested2");
    }
}
