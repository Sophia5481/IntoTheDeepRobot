package org.firstinspires.ftc.teamcode.utils;

import android.os.Environment;
import android.util.Log;
import java.time.Instant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Logger {
    private static final String TAG = "FTClog";
    private static File logFile;

    static {
        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String timeStamp = utcFormat.format(new Date());
            File dir = new File(Environment.getExternalStorageDirectory(), "logs");
            if (!dir.exists()) dir.mkdirs();

            logFile = new File(dir, "log" + timeStamp + ".txt");
            logFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "Logger init failed", e);
        }
    }

    public static void d(String tag, String message) {
        Log.d(tag, message);
        writeToFile("DEBUG", tag, message);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
        writeToFile("INFO", tag, message);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
        writeToFile("WARN", tag, message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
        writeToFile("ERROR", tag, message);
    }

    private static void writeToFile(String level, String tag, String message) {
        if (logFile == null) return;

        try (FileWriter writer = new FileWriter(logFile, true)) {
            String time = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            writer.write(String.format("[%s] %s/%s: %s\n", time, level, tag, message));
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log to file", e);
        }
    }
}