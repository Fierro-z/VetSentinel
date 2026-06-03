package com.vetsentinel.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class VetLogger {
    private static final Logger logger = Logger.getLogger("VetSentinel");

    static {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // Disable parent console handlers to prevent double logs
            logger.setUseParentHandlers(false);

            // Configure FileHandler to write to logs/vetsentinel.log
            // Limit to 5MB per file, rotating up to 3 files
            FileHandler fileHandler = new FileHandler("logs/vetsentinel.log", 5 * 1024 * 1024, 3, true);
            
            // Custom Formatter matching Logback/SLF4J layout
            Formatter formatter = new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                @Override
                public String format(LogRecord record) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(dateFormat.format(new Date(record.getMillis())))
                      .append(" [").append(Thread.currentThread().getName()).append("] ")
                      .append(record.getLevel().getLocalizedName())
                      .append(" ")
                      .append(record.getSourceClassName() != null ? record.getSourceClassName() : "VetSentinel")
                      .append(" - ")
                      .append(formatMessage(record))
                      .append("\n");
                    
                    if (record.getThrown() != null) {
                        java.io.StringWriter sw = new java.io.StringWriter();
                        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                        record.getThrown().printStackTrace(pw);
                        sb.append(sw.toString());
                    }
                    return sb.toString();
                }
            };
            
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);

            // ConsoleHandler for development stdout
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(formatter);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            System.err.println("Fallo al inicializar el logger de VetSentinel: " + e.getMessage());
        }
    }

    public static void info(String msg) {
        logger.log(Level.INFO, msg);
    }

    public static void warn(String msg) {
        logger.log(Level.WARNING, msg);
    }

    public static void error(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    public static void error(String msg, Throwable t) {
        logger.log(Level.SEVERE, msg, t);
    }
}
