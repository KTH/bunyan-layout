/*
 * MIT License
 *
 * Copyright (c) 2018 Kungliga Tekniska högskolan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package se.kth.infosys.log4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * A Log4j 1.2 Layout which prints events in Node Bunyan JSON format.
 * The layout takes no options and requires no additional configuration.
 */
public class BunyanLayout extends Layout {
    private static final Map<Level, Integer> BUNYAN_LEVEL;

    static {
        BUNYAN_LEVEL = new HashMap<>();
        BUNYAN_LEVEL.put(Level.FATAL, 60);
        BUNYAN_LEVEL.put(Level.ERROR, 50);
        BUNYAN_LEVEL.put(Level.WARN, 40);
        BUNYAN_LEVEL.put(Level.INFO, 30);
        BUNYAN_LEVEL.put(Level.DEBUG, 20);
        BUNYAN_LEVEL.put(Level.TRACE, 10);
    }

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Format the event as a Banyan style JSON object.
     */
    public String format(LoggingEvent event) {
        JsonObject jsonEvent = new JsonObject();
        jsonEvent.addProperty("v", 0);
        jsonEvent.addProperty("level", BUNYAN_LEVEL.get(event.getLevel()));
        jsonEvent.addProperty("name", event.getLoggerName());
        try {
            jsonEvent.addProperty("hostname", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            jsonEvent.addProperty("hostname", "unkown");
        }
        jsonEvent.addProperty("pid", getThreadId(event));
        jsonEvent.addProperty("time", formatAsIsoUTCDateTime(event.getTimeStamp()));
        jsonEvent.addProperty("msg", event.getMessage().toString());

        if (event.getLevel().isGreaterOrEqual(Level.ERROR) && event.getThrowableInformation() != null) {
            JsonObject jsonError = new JsonObject();
            Throwable e = event.getThrowableInformation().getThrowable();

            jsonError.addProperty("message", e.getMessage());
            jsonError.addProperty("name", e.getClass().getSimpleName());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            jsonError.addProperty("stack", sw.toString());
            jsonEvent.add("err", jsonError);
        }
        return GSON.toJson(jsonEvent) + "\n";
    }

    private static String formatAsIsoUTCDateTime(long timeStamp) {
        final Instant instant = Instant.ofEpochMilli(timeStamp);
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * The throwable object is rendered in the output as an "err" property.
     */
    public boolean ignoresThrowable() {
        return false;
    }

    /**
     * This Layout renders JSON objects, hence we use application/json.
     * This is in a strict sense untrue, since the entire stream is not proper JSON.
     */
    @Override
    public String getContentType() {
        return "application/json";
    }

    /**
     * No options, hence doing nothing.
     */
    public void activateOptions() {
    }

    private long getThreadId(LoggingEvent event) {
        long threadId;
        String threadName = event.getThreadName();
        if (Thread.currentThread().getName().equals(threadName)) {
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
}
