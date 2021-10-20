package se.kth.infosys.logging;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

public class LogTest {
    private static Logger LOG = Logger.getLogger(LogTest.class.getName());

    @Before public void before() throws Exception {
        LogManager.getLogManager().readConfiguration(this.getClass().getClassLoader().getResourceAsStream("logging.properties"));
    }

    @Test
    public void printStuff() {
        LOG.finest("Finest log");
        LOG.finer("Finerlog");
        LOG.fine("Fine log");
        LOG.info("Info log");
        LOG.warning("Warning log");
        LOG.severe("Severe log");

        LOG.warning("0123456789 ".repeat(2458));

        try {
            throw new RuntimeException("A runtime exception");
        }
        catch (Exception e) {
            LOG.log(Level.SEVERE, "An exception", e);
        }
    }
}
