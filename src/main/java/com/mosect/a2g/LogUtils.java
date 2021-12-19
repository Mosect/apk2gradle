package com.mosect.a2g;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class LogUtils {

    private final static Impl impl = new Impl();

    private LogUtils() {
    }

    public synchronized static void i(String tag, String msg) {
        impl.print(System.out, tag, msg);
    }

    public synchronized static void e(String tag, String msg) {
        impl.print(System.err, tag, msg);
    }

    static class Impl {

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Impl() {
        }

        void print(PrintStream stream, String tag, String msg) {
            stream.print(dateFormat.format(new Date()));
            stream.print(" : ");
            stream.print(tag);
            stream.print(" : ");
            stream.print(msg);
            stream.println();
        }
    }
}
