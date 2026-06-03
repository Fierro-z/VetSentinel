package com.vetsentinel.test;

public class InputValidationTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Input Validation & Sanitization Tests...");
        System.out.println("==================================================");

        try {
            testSanitizar();
            testCedulaRegex();
            testNombrePropietarioRegex();
            testNombreMascotaRegex();
            testDireccionRegex();

            System.out.println("==================================================");
            System.out.println("SUCCESS: All input validation tests passed!");
            System.out.println("==================================================");
        } catch (Throwable t) {
            System.err.println("==================================================");
            System.err.println("FAILURE: Input validation tests failed!");
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

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static String sanitizar(String input) {
        if (input == null) return "";
        String clean = input.replaceAll("<[^>]*>", "");
        clean = clean.replace("'", "")
                     .replace("\"", "")
                     .replace(";", "")
                     .replace("--", "")
                     .replace("/*", "")
                     .replace("*/", "");
        return clean.trim();
    }

    private static void testSanitizar() {
        System.out.print("Running: testSanitizar... ");
        assertEquals("normal text", sanitizar("normal text"), "Simple text should not change");
        assertEquals("safe text", sanitizar("<script>safe text</script>"), "HTML tags should be stripped");
        assertEquals("DROP TABLE Users", sanitizar("DROP TABLE Users;--"), "SQL injection symbols should be stripped");
        assertEquals("commented", sanitizar("/* commented */"), "CSS/SQL block comment indicators should be stripped");
        System.out.println("PASSED");
    }

    private static void testCedulaRegex() {
        System.out.print("Running: testCedulaRegex... ");
        String regex = "^[0-9]{5,15}$";
        assertTrue("12345678".matches(regex), "Standard cedula should match");
        assertTrue("1000999888".matches(regex), "10-digit cedula should match");
        assertFalse("123".matches(regex), "Too short should fail");
        assertFalse("1234567890123456".matches(regex), "Too long should fail");
        assertFalse("123456a8".matches(regex), "Contains character should fail");
        assertFalse("123-456".matches(regex), "Contains hyphen should fail");
        System.out.println("PASSED");
    }

    private static void testNombrePropietarioRegex() {
        System.out.print("Running: testNombrePropietarioRegex... ");
        String regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'\\-]{2,80}$";
        assertTrue("Carlos Andrés".matches(regex), "Standard name with spaces and accents should match");
        assertTrue("Jean-Luc".matches(regex), "Hyphenated name should match");
        assertTrue("O'Connor".matches(regex), "Apostrophized name should match");
        assertFalse("C".matches(regex), "Too short should fail");
        assertFalse("Carlos123".matches(regex), "Numbers should fail");
        assertFalse("Carlos;".matches(regex), "Special characters should fail");
        System.out.println("PASSED");
    }

    private static void testNombreMascotaRegex() {
        System.out.print("Running: testNombreMascotaRegex... ");
        String regex = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s'\\-]{1,40}$";
        assertTrue("Lucas".matches(regex), "Standard pet name should match");
        assertTrue("Rambo II".matches(regex), "Pet name with Roman numbers or space should match");
        assertTrue("Michi 3".matches(regex), "Pet name with numbers should match");
        assertFalse("".matches(regex), "Empty should fail");
        assertFalse("Lucas;".matches(regex), "Special character should fail");
        System.out.println("PASSED");
    }

    private static void testDireccionRegex() {
        System.out.print("Running: testDireccionRegex... ");
        String regex = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s.,#\\-()]{3,120}$";
        assertTrue("Calle 123 # 45-67".matches(regex), "Standard address should match");
        assertTrue("Diag. 50 (Apto 302)".matches(regex), "Address with parenthesis and dot should match");
        assertFalse("Ab".matches(regex), "Too short should fail");
        assertFalse("Calle 100 <script>".matches(regex), "HTML tags should fail");
        assertFalse("Calle 100; DROP".matches(regex), "SQL injection symbols should fail");
        System.out.println("PASSED");
    }
}
