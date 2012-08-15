package com.filter.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;

import android.util.Log;

import com.filter.config.LogConfig;

/**
 * 日志输出,可以通过LogConfig进行配置和控制
 */
public class LogUtils {

	public static void v(String tag, String msg) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.VERBOSE >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.v(tag, msg);
		}
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.VERBOSE >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.v(tag, msg, tr);
		}
	}

	public static void d(String tag, String msg) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.DEBUG >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.d(tag, msg);
		}
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.DEBUG >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.d(tag, msg, tr);
		}
	}

	public static void i(String tag, String msg) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.INFO >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.i(tag, msg);
		}
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.INFO >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.WARN >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.w(tag, msg);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.WARN >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.w(tag, msg, tr);
		}
	}

	public static void w(String tag, Throwable tr) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.WARN >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.w(tag, tr);
		}
	}

	public static void e(String tag, String msg) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.ERROR >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.e(tag, msg);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (LogConfig.PRINT_TO_SYSTEM && Log.ERROR >= LogConfig.LEVEL_PRINT_TO_SYSTEM) {
			Log.e(tag, msg, tr);
		}
	}

	public static void e(String tag, Throwable e) {
		e(tag, e.toString());
		if (e instanceof ConnectTimeoutException || e instanceof SocketTimeoutException
				|| e instanceof UnknownHostException) {
			return;
		}
		StackTraceElement[] trace = e.getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			e(tag, "\tat " + trace[i]);
		}
		Throwable ourCause = e.getCause();
		if (ourCause != null) {
			e(tag, ourCause);
		}
	}

	public static void println(int priority, String tag, String msg) {
		if (LogConfig.PRINT_TO_SYSTEM) {
			Log.println(priority, tag, msg);
		}
	}


	public static String getStackTraceString(Throwable tr) {
		if (tr == null) {
			return "";
		}

		Throwable t = tr;
		while (t != null) {
			if (t instanceof UnknownHostException) {
				return "";
			}
			t = t.getCause();
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		return sw.toString();
	}

}
