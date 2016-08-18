package rpulp.chezzz.log;

public class Log {
    public static void error(final String msg, final Throwable th) {
        System.err.println(msg);
        th.printStackTrace(System.err);
    }
}
