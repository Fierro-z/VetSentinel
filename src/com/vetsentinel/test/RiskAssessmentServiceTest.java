package com.vetsentinel.test;

import com.vetsentinel.model.*;
import com.vetsentinel.service.*;
import com.vetsentinel.service.impl.*;

import java.util.ArrayList;
import java.util.List;

public class RiskAssessmentServiceTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running VetSentinel Unit Tests...");
        System.out.println("==================================================");

        try {
            testLeishmaniasisVisceralStrategy_Vulnerable();
            testLeishmaniasisVisceralStrategy_NotVulnerable();
            testLeishmaniasisCutaneaStrategy_HighRiskDept();
            testToxoplasmosisStrategy_GestanteMultiparaGato();
            testToxoplasmosisStrategy_GestantePrimipara();
            testDefaultRiskStrategy_RuralNinos();
            
            System.out.println("==================================================");
            System.out.println("SUCCESS: All unit tests passed successfully!");
            System.out.println("==================================================");
        } catch (Throwable t) {
            System.err.println("==================================================");
            System.err.println("FAILURE: Unit tests failed!");
            t.printStackTrace();
            System.err.println("==================================================");
            System.exit(1);
        }
    }

    private static RiskAssessmentService createService() {
        List<RiskStrategy> strategies = new ArrayList<>();
        strategies.add(new LeishmaniasisVisceralStrategy());
        strategies.add(new LeishmaniasisCutaneaStrategy());
        strategies.add(new ToxoplasmosisStrategy());
        strategies.add(new DefaultRiskStrategy());
        return new RiskAssessmentService(strategies);
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

    private static void testLeishmaniasisVisceralStrategy_Vulnerable() {
        System.out.print("Running: testLeishmaniasisVisceralStrategy_Vulnerable... ");
        RiskAssessmentService service = createService();

        Propietario p = new Propietario(1, "123", "Carlos", "Calle 1", "Sucre", false, false, 0, false, 1, "Subsidiado", 100);
        Mascota m = new Mascota(1, "Lucas", "Perro", 3, p);
        Parasito par = new Parasito(1, "Leishmaniasis Visceral", "Riesgo visceral", "Medidas", false, true, true);
        Diagnostico d = new Diagnostico(1, m, par, "2026-06-03", "Activo");

        RiskAssessmentResult res = service.evaluarRiesgoHumano(d);
        assertEquals(RiskLevel.EMERGENCIA_CRITICA, res.getNivel(), "Debería ser emergencia crítica para estrato 1 / subsidiado");
        assertTrue(res.getAlertaTexto().contains("supera el 95%"), "Debería mencionar la letalidad");
        System.out.println("PASSED");
    }

    private static void testLeishmaniasisVisceralStrategy_NotVulnerable() {
        System.out.print("Running: testLeishmaniasisVisceralStrategy_NotVulnerable... ");
        RiskAssessmentService service = createService();

        // Not vulnerable (Estrato 3, Contributivo, no niños, no rural)
        Propietario p = new Propietario(1, "123", "Carlos", "Calle 1", "Sucre", false, false, 0, false, 3, "Contributivo", 100);
        Mascota m = new Mascota(1, "Lucas", "Perro", 3, p);
        // We use Leishmaniasis Visceral, it won't match visceral strategy conditions so it will fall back to DefaultRiskStrategy
        Parasito par = new Parasito(1, "Leishmaniasis Visceral", "Riesgo visceral", "Medidas", false, true, true);
        Diagnostico d = new Diagnostico(1, m, par, "2026-06-03", "Activo");

        RiskAssessmentResult res = service.evaluarRiesgoHumano(d);
        assertEquals(RiskLevel.MEDIO, res.getNivel(), "Debería caer en riesgo medio general");
        System.out.println("PASSED");
    }

    private static void testLeishmaniasisCutaneaStrategy_HighRiskDept() {
        System.out.print("Running: testLeishmaniasisCutaneaStrategy_HighRiskDept... ");
        RiskAssessmentService service = createService();

        Propietario p = new Propietario(1, "123", "Carlos", "Calle 1", "Risaralda", true, false, 0, false, 3, "Contributivo", 100);
        Mascota m = new Mascota(1, "Lucas", "Perro", 3, p);
        Parasito par = new Parasito(1, "Leishmaniasis Cutánea", "Riesgo cutáneo", "Medidas", false, true, true);
        Diagnostico d = new Diagnostico(1, m, par, "2026-06-03", "Activo");

        RiskAssessmentResult res = service.evaluarRiesgoHumano(d);
        assertEquals(RiskLevel.CRITICO, res.getNivel(), "Debería ser crítico en Risaralda si hay niños");
        System.out.println("PASSED");
    }

    private static void testToxoplasmosisStrategy_GestanteMultiparaGato() {
        System.out.print("Running: testToxoplasmosisStrategy_GestanteMultiparaGato... ");
        RiskAssessmentService service = createService();

        Propietario p = new Propietario(1, "123", "Maria", "Calle 1", "Huila", false, true, 2, false, 3, "Contributivo", 100);
        Mascota m = new Mascota(1, "Michi", "Gato", 2, p);
        Parasito par = new Parasito(1, "Toxoplasmosis", "Riesgo toxo", "Medidas", true, false, false);
        Diagnostico d = new Diagnostico(1, m, par, "2026-06-03", "Activo");

        RiskAssessmentResult res = service.evaluarRiesgoHumano(d);
        assertEquals(RiskLevel.CRITICO, res.getNivel(), "Debería ser crítico para gestante multípara");
        assertTrue(res.getAlertaTexto().contains("ALERTA FELINA CRÍTICA"), "Debería tener alerta felina para gatos");
        System.out.println("PASSED");
    }

    private static void testToxoplasmosisStrategy_GestantePrimipara() {
        System.out.print("Running: testToxoplasmosisStrategy_GestantePrimipara... ");
        RiskAssessmentService service = createService();

        Propietario p = new Propietario(1, "123", "Maria", "Calle 1", "Huila", false, true, 0, false, 3, "Contributivo", 100);
        Mascota m = new Mascota(1, "Michi", "Gato", 2, p);
        Parasito par = new Parasito(1, "Toxoplasmosis", "Riesgo toxo", "Medidas", true, false, false);
        Diagnostico d = new Diagnostico(1, m, par, "2026-06-03", "Activo");

        RiskAssessmentResult res = service.evaluarRiesgoHumano(d);
        assertEquals(RiskLevel.CRITICO, res.getNivel(), "Debería ser crítico por embarazo");
        System.out.println("PASSED");
    }

    private static void testDefaultRiskStrategy_RuralNinos() {
        System.out.print("Running: testDefaultRiskStrategy_RuralNinos... ");
        RiskAssessmentService service = createService();

        Propietario p = new Propietario(1, "123", "Carlos", "Calle 1", "Sucre", true, false, 0, true, 3, "Contributivo", 100);
        Mascota m = new Mascota(1, "Lucas", "Perro", 3, p);
        Parasito par = new Parasito(1, "Toxocariasis", "Riesgo toxocara", "Medidas", false, true, false);
        Diagnostico d = new Diagnostico(1, m, par, "2026-06-03", "Activo");

        RiskAssessmentResult res = service.evaluarRiesgoHumano(d);
        assertEquals(RiskLevel.ALTO, res.getNivel(), "Debería evaluar a ALTO debido a los niños");
        System.out.println("PASSED");
    }
}
