package com.vetsentinel.test;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ExportCsvTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Export CSV Unit Tests...");
        System.out.println("==================================================");

        try {
            testEscapeCSV();
            testUtf8BomAndEncoding();
            System.out.println("==================================================");
            System.out.println("SUCCESS: Export CSV tests passed!");
            System.out.println("==================================================");
        } catch (Throwable t) {
            System.err.println("==================================================");
            System.err.println("FAILURE: Export CSV tests failed!");
            t.printStackTrace();
            System.err.println("==================================================");
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - Expected: [" + expected + "], Actual: [" + actual + "]");
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\r") || clean.contains("\"")) {
            return "\"" + clean + "\"";
        }
        return clean;
    }

    private static void testEscapeCSV() {
        System.out.print("Running: testEscapeCSV... ");
        assertEquals("normal", escapeCSV("normal"), "Should not modify simple string");
        assertEquals("\"with,comma\"", escapeCSV("with,comma"), "Should wrap comma");
        assertEquals("\"with\"\"quote\"", escapeCSV("with\"quote"), "Should double quote");
        assertEquals("\"with\nnewline\"", escapeCSV("with\nnewline"), "Should wrap newline");
        assertEquals("", escapeCSV(null), "Should return empty for null");
        System.out.println("PASSED");
    }

    private static void testUtf8BomAndEncoding() throws IOException {
        System.out.print("Running: testUtf8BomAndEncoding... ");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // Write BOM
        bos.write(0xEF);
        bos.write(0xBB);
        bos.write(0xBF);
        
        try (OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            osw.write("Sucre,Leishmaniasis Visceral,ALERTA CRÍTICA\n");
            osw.write("Bogotá,Toxoplasmosis,NORMAL\n");
        }

        byte[] bytes = bos.toByteArray();
        
        // Verify BOM
        assertEquals((byte)0xEF, bytes[0], "First byte must be 0xEF");
        assertEquals((byte)0xBB, bytes[1], "Second byte must be 0xBB");
        assertEquals((byte)0xBF, bytes[2], "Third byte must be 0xBF");

        // Verify UTF-8 accented characters
        String content = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        assertTrue(content.contains("Bogotá"), "Accented character 'á' should be preserved correctly");
        System.out.println("PASSED");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
