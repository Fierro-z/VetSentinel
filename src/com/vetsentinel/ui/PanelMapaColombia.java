package com.vetsentinel.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PanelMapaColombia extends JPanel {
    // Bounding box of Colombia's coordinates
    private static final double MIN_LON = -79.2;
    private static final double MAX_LON = -66.8;
    private static final double MIN_LAT = -4.3;
    private static final double MAX_LAT = 12.5;

    // Dark-mode colors matching VetBaseFrame
    private final Color bgDark      = new Color(17, 24, 39);
    private final Color bgCard      = new Color(31, 41, 55);
    private final Color borderColor = new Color(55, 65, 81);
    private final Color textPrimary = new Color(243, 244, 246);
    private final Color textMuted   = new Color(156, 163, 175);
    private final Color accentTeal  = new Color(20, 184, 166);

    private static class DepartamentoGeo {
        String name;
        List<List<Point2D>> rings = new ArrayList<>();
    }

    private static class Point2D {
        double lon, lat;
        Point2D(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }
    }

    private final List<DepartamentoGeo> departamentos = new ArrayList<>();
    private final Map<String, String> riesgos = new HashMap<>();
    
    private String hoveredDepto = null;
    private Point mousePoint = null;
    
    // Cache of projected shapes for quick mouse-hover collision checks and repainting
    private final Map<String, List<Path2D.Double>> screenShapes = new HashMap<>();

    public PanelMapaColombia() {
        setOpaque(false);
        cargarGeoJSON();
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                String currentHover = null;
                for (Map.Entry<String, List<Path2D.Double>> entry : screenShapes.entrySet()) {
                    for (Path2D.Double path : entry.getValue()) {
                        if (path.contains(mousePoint)) {
                            currentHover = entry.getKey();
                            break;
                        }
                    }
                    if (currentHover != null) break;
                }
                
                if (currentHover == null || !currentHover.equals(hoveredDepto)) {
                    hoveredDepto = currentHover;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredDepto = null;
                mousePoint = null;
                repaint();
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void actualizarRiesgos(Map<String, String> nuevosRiesgos) {
        this.riesgos.clear();
        for (Map.Entry<String, String> entry : nuevosRiesgos.entrySet()) {
            this.riesgos.put(normalizarNombre(entry.getKey()), entry.getValue());
        }
        repaint();
    }

    private String normalizarNombre(String str) {
        if (str == null) return "";
        return str.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replace("distrito capital de ", "")
                .replace("departamento de ", "")
                .trim();
    }

    private void cargarGeoJSON() {
        try {
            String pathStr = "resources/map/co.json";
            if (!Files.exists(Paths.get(pathStr))) {
                System.out.println("Archivo co.json no encontrado en: " + pathStr);
                return;
            }
            String json = new String(Files.readAllBytes(Paths.get(pathStr)), java.nio.charset.StandardCharsets.UTF_8);
            
            // Fast custom regex splitter to parse features
            String[] features = json.split("\\{\\s*\"type\"\\s*:\\s*\"Feature\"");
            Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
            Pattern coordPattern = Pattern.compile("\\[\\s*(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+\\.\\d+)\\s*\\]");

            for (String feature : features) {
                if (!feature.contains("\"geometry\"")) continue;
                
                Matcher nameMatcher = namePattern.matcher(feature);
                String name = "";
                if (nameMatcher.find()) {
                    name = nameMatcher.group(1);
                }
                
                if (name.isEmpty()) continue;
                
                DepartamentoGeo depto = new DepartamentoGeo();
                depto.name = name;
                
                int coordIdx = feature.indexOf("\"coordinates\"");
                if (coordIdx == -1) continue;
                String coordsSection = feature.substring(coordIdx);
                
                // Split coordinate coordinates list into individual ring clusters
                String[] ringsRaw = coordsSection.split("\\[\\[");
                for (String ringRaw : ringsRaw) {
                    if (!ringRaw.contains("]")) continue;
                    
                    List<Point2D> ring = new ArrayList<>();
                    Matcher coordMatcher = coordPattern.matcher(ringRaw);
                    while (coordMatcher.find()) {
                        double lon = Double.parseDouble(coordMatcher.group(1));
                        double lat = Double.parseDouble(coordMatcher.group(2));
                        ring.add(new Point2D(lon, lat));
                    }
                    if (ring.size() >= 3) {
                        depto.rings.add(ring);
                    }
                }
                
                if (!depto.rings.isEmpty()) {
                    departamentos.add(depto);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar co.json: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Calculate maximum aspect-ratio scale for Colombia bounding box
        double scaleX = w / (MAX_LON - MIN_LON);
        double scaleY = h / (MAX_LAT - MIN_LAT);
        double scale = Math.min(scaleX, scaleY) * 0.95; // 5% padding

        double offsetX = (w - (MAX_LON - MIN_LON) * scale) / 2;
        double offsetY = (h - (MAX_LAT - MIN_LAT) * scale) / 2;

        screenShapes.clear();

        // Render shapes
        for (DepartamentoGeo depto : departamentos) {
            List<Path2D.Double> paths = new ArrayList<>();
            for (List<Point2D> ring : depto.rings) {
                Path2D.Double path = new Path2D.Double();
                boolean first = true;
                for (Point2D pt : ring) {
                    double sx = offsetX + (pt.lon - MIN_LON) * scale;
                    double sy = offsetY + (MAX_LAT - pt.lat) * scale;
                    if (first) {
                        path.moveTo(sx, sy);
                        first = false;
                    } else {
                        path.lineTo(sx, sy);
                    }
                }
                path.closePath();
                paths.add(path);
            }
            screenShapes.put(depto.name, paths);

            // Choose color based on SQLite registry risks
            String normName = normalizarNombre(depto.name);
            String risk = riesgos.getOrDefault(normName, "SIN DATOS");

            Color baseColor = new Color(55, 65, 81); // Grey default (SIN DATOS)
            if (risk.equals("EMERGENCIA CRÍTICA") || risk.equals("EMERGENCIA CRITICA") || risk.equals("CRITICO") || risk.equals("CRÍTICO")) {
                baseColor = new Color(239, 68, 68); // Red
            } else if (risk.equals("ALTO")) {
                baseColor = new Color(249, 115, 22); // Orange
            } else if (risk.equals("MEDIO") || risk.equals("MODERADO")) {
                baseColor = new Color(234, 179, 8); // Yellow
            } else if (risk.equals("BAJO")) {
                baseColor = new Color(16, 185, 129); // Emerald Green
            }

            boolean isHovered = depto.name.equals(hoveredDepto);

            // Fill shapes
            g2.setColor(isHovered ? baseColor.brighter() : baseColor);
            for (Path2D.Double path : paths) {
                g2.fill(path);
            }

            // Draw borders
            g2.setColor(isHovered ? accentTeal : new Color(75, 85, 99));
            g2.setStroke(new BasicStroke(isHovered ? 2.0f : 1.0f));
            for (Path2D.Double path : paths) {
                g2.draw(path);
            }
        }

        // Draw customized glassmorphic tooltip near mouse cursor
        if (hoveredDepto != null && mousePoint != null) {
            String normName = normalizarNombre(hoveredDepto);
            String risk = riesgos.getOrDefault(normName, "SIN DATOS");

            Color riskColor = textMuted;
            if (risk.equals("EMERGENCIA CRÍTICA") || risk.equals("EMERGENCIA CRITICA") || risk.equals("CRITICO") || risk.equals("CRÍTICO")) {
                riskColor = new Color(239, 68, 68);
            } else if (risk.equals("ALTO")) {
                riskColor = new Color(249, 115, 22);
            } else if (risk.equals("MEDIO") || risk.equals("MODERADO")) {
                riskColor = new Color(234, 179, 8);
            } else if (risk.equals("BAJO")) {
                riskColor = new Color(16, 185, 129);
            }

            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            String t1 = hoveredDepto;
            String t2 = "Estado: " + risk;

            int pad = 8;
            int boxW = Math.max(fm.stringWidth(t1), fm.stringWidth(t2)) + pad * 2;
            int boxH = fm.getHeight() * 2 + pad * 2;

            int bx = mousePoint.x + 15;
            int by = mousePoint.y + 15;

            if (bx + boxW > w) bx = mousePoint.x - boxW - 5;
            if (by + boxH > h) by = mousePoint.y - boxH - 5;

            g2.setColor(new Color(17, 24, 39, 240));
            g2.fillRoundRect(bx, by, boxW, boxH, 8, 8);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(bx, by, boxW, boxH, 8, 8);

            // Draw department name
            g2.setColor(accentTeal);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(t1, bx + pad, by + pad + fm.getAscent());

            // Draw alert status
            g2.setColor(riskColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(t2, bx + pad, by + pad + fm.getHeight() + fm.getAscent());
        }

        g2.dispose();
    }
}
