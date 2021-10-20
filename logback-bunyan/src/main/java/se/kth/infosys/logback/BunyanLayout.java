package se.kth.infosys.logback;

import static java.util.Collections.singletonList;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.LayoutBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BunyanLayout extends LayoutBase<ILoggingEvent> {

    private static final int MSG_MAX_LENGTH = 20000;

    private static final Map<Level, Integer> BUNYAN_LEVEL;
    static {
        BUNYAN_LEVEL = new HashMap<>();
        BUNYAN_LEVEL.put(Level.ERROR, 50);
        BUNYAN_LEVEL.put(Level.WARN, 40);
        BUNYAN_LEVEL.put(Level.INFO, 30);
        BUNYAN_LEVEL.put(Level.DEBUG, 20);
        BUNYAN_LEVEL.put(Level.TRACE, 10);
    }

    private static final Gson GSON = new GsonBuilder().create();
    private ThrowableProxyConverter rtpc = new RootCauseFirstThrowableProxyConverter();

    @Override
    public void start() {
        rtpc.setOptionList(singletonList("full"));
        rtpc.start();
        super.start();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        JsonObject jsonEvent = new JsonObject();
        jsonEvent.addProperty("v", 0);
        jsonEvent.addProperty("level", BUNYAN_LEVEL.get(event.getLevel()));
        jsonEvent.addProperty("levelStr", event.getLevel().toString());
        jsonEvent.addProperty("name", event.getLoggerName());
        try {
            jsonEvent.addProperty("hostname", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            jsonEvent.addProperty("hostname", "unkown");
        }
        jsonEvent.addProperty("pid", getThreadId(event));
        jsonEvent.addProperty("time", formatAsIsoUTCDateTime(event.getTimeStamp()));
        jsonEvent.addProperty("msg", getMessage(event.getFormattedMessage()));

        if (event.getLevel().isGreaterOrEqual(Level.ERROR) && event.getThrowableProxy() != null) {
            JsonObject jsonError = new JsonObject();
            IThrowableProxy e = event.getThrowableProxy();

            jsonError.addProperty("message", e.getMessage());
            jsonError.addProperty("name", e.getClassName());
            jsonError.addProperty("stack", rtpc.convert(event));
            jsonEvent.add("err", jsonError);
        }
        return GSON.toJson(jsonEvent) + "\n";
    }

    private String getMessage(final String msg) {
        if (msg == null || msg.length() <= MSG_MAX_LENGTH) {
            return msg;
        }
        return msg.substring(0, MSG_MAX_LENGTH);
    }

    private long getThreadId(ILoggingEvent event) {
        long threadId;
        String threadName = event.getThreadName();
        if(Thread.currentThread().getName().equals(threadName)){
            threadId = Thread.currentThread().getId();
        } else {
            try {
                threadId = Long.parseLong(threadName.substring(threadName.lastIndexOf('-')));
            } catch (NumberFormatException e) {
                threadId = 0;
            }
        }
        return threadId;
    }

    private static String formatAsIsoUTCDateTime(long timeStamp) {
        final Instant instant = Instant.ofEpochMilli(timeStamp);
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }


}
