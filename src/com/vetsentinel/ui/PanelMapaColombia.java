package com.vetsentinel.ui;

import javax.swing.*;
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
    // Bounding box of Colombia
    private static final double MIN_LON = -79.2;
    private static final double MAX_LON = -66.8;
    private static final double MIN_LAT = -4.3;
    private static final double MAX_LAT = 12.5;

    // Dark-mode colors matching VetBaseFrame
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
    
    // Zoom and Pan States
    private double zoomFactor = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    
    // Cache of screen shapes for drawing and hit testing
    private final Map<String, List<Path2D.Double>> screenShapes = new HashMap<>();

    private java.util.function.Consumer<String> selectionListener = null;

    public void setSelectionListener(java.util.function.Consumer<String> listener) {
        this.selectionListener = listener;
    }

    public PanelMapaColombia() {
        setOpaque(false);
        cargarGeoJSON();
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point pressStart = null;
            private Point dragStart = null;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressStart = e.getPoint();
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    double dx = e.getX() - dragStart.x;
                    double dy = e.getY() - dragStart.y;
                    panX += dx;
                    panY += dy;
                    dragStart = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (pressStart != null) {
                        double dist = e.getPoint().distance(pressStart);
                        if (dist < 5 && hoveredDepto != null) {
                            if (selectionListener != null) {
                                selectionListener.accept(hoveredDepto);
                            }
                        }
                    }
                    pressStart = null;
                    dragStart = null;
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double click to reset zoom and pan
                if (e.getClickCount() == 2) {
                    zoomFactor = 1.0;
                    panX = 0.0;
                    panY = 0.0;
                    repaint();
                }
            }

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

        // Zoom relative to mouse position
        addMouseWheelListener(e -> {
            double rotation = e.getPreciseWheelRotation();
            double zoomMultiplier = Math.pow(1.1, -rotation);
            double oldZoom = zoomFactor;
            zoomFactor *= zoomMultiplier;
            
            // Limit zoom factors
            if (zoomFactor < 0.6) zoomFactor = 0.6;
            if (zoomFactor > 30.0) zoomFactor = 30.0;
            
            double actualMultiplier = zoomFactor / oldZoom;

            double mx = e.getX();
            double my = e.getY();
            double w2 = getWidth() / 2.0;
            double h2 = getHeight() / 2.0;
            
            panX = mx - w2 - (mx - w2 - panX) * actualMultiplier;
            panY = my - h2 - (my - h2 - panY) * actualMultiplier;

            repaint();
        });
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
            
            int featuresIdx = json.indexOf("\"features\"");
            if (featuresIdx == -1) return;
            int startArray = json.indexOf("[", featuresIdx);
            if (startArray == -1) return;
            
            int len = json.length();
            int pos = startArray + 1;
            int bracketCount = 0;
            StringBuilder featureSb = null;
            
            // Read features token by token (bracket matching) to make sure we parse whole independent objects
            while (pos < len) {
                char c = json.charAt(pos);
                if (bracketCount == 0) {
                    if (c == '{') {
                        bracketCount = 1;
                        featureSb = new StringBuilder();
                        featureSb.append(c);
                    }
                } else {
                    featureSb.append(c);
                    if (c == '{') {
                        bracketCount++;
                    } else if (c == '}') {
                        bracketCount--;
                        if (bracketCount == 0) {
                            parseFeature(featureSb.toString());
                        }
                    }
                }
                pos++;
            }
        } catch (IOException e) {
            com.vetsentinel.util.VetLogger.error("Error al cargar co.json", e);
        }
    }

    private void parseFeature(String featureStr) {
        Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher nameMatcher = namePattern.matcher(featureStr);
        String name = "";
        if (nameMatcher.find()) {
            name = nameMatcher.group(1);
        }
        
        if (name.isEmpty()) return;
        
        int coordsIdx = featureStr.indexOf("\"coordinates\"");
        if (coordsIdx == -1) return;
        
        int startArr = featureStr.indexOf("[", coordsIdx);
        if (startArr == -1) return;
        
        int[] pos = new int[]{startArr + 1};
        List<Object> parsedArray = parseArray(featureStr, pos);
        
        DepartamentoGeo depto = new DepartamentoGeo();
        depto.name = name;
        
        extractRings(parsedArray, depto.rings);
        
        if (!depto.rings.isEmpty()) {
            departamentos.add(depto);
        }
    }

    // Elegant JSON array parser (runs in O(N))
    private static List<Object> parseArray(String s, int[] pos) {
        List<Object> list = new ArrayList<>();
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == '[') {
                pos[0]++;
                list.add(parseArray(s, pos));
            } else if (c == ']') {
                pos[0]++;
                return list;
            } else if (Character.isDigit(c) || c == '-' || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (pos[0] < s.length() && (Character.isDigit(s.charAt(pos[0])) || s.charAt(pos[0]) == '-' || s.charAt(pos[0]) == '.' || s.charAt(pos[0]) == 'e' || s.charAt(pos[0]) == 'E')) {
                    sb.append(s.charAt(pos[0]));
                    pos[0]++;
                }
                list.add(Double.parseDouble(sb.toString()));
            } else {
                pos[0]++;
            }
        }
        return list;
    }

    // Dynamic ring extractor that supports deep coordinates nesting for both Polygon and MultiPolygon
    @SuppressWarnings("unchecked")
    private void extractRings(List<Object> array, List<List<Point2D>> targetRings) {
        if (array.isEmpty()) return;
        
        Object first = array.get(0);
        if (first instanceof Double) {
            return;
        }
        
        List<Object> subList = (List<Object>) first;
        if (!subList.isEmpty() && subList.get(0) instanceof Double) {
            List<Point2D> ring = new ArrayList<>();
            for (Object pointObj : array) {
                List<Double> pt = (List<Double>) pointObj;
                if (pt.size() >= 2) {
                    ring.add(new Point2D(pt.get(0), pt.get(1)));
                }
            }
            if (ring.size() >= 3) {
                targetRings.add(ring);
            }
        } else {
            for (Object item : array) {
                if (item instanceof List) {
                    extractRings((List<Object>) item, targetRings);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        double centerX = w / 2.0;
        double centerY = h / 2.0;

        double mapCenterX = (MIN_LON + MAX_LON) / 2.0;
        double mapCenterY = (MIN_LAT + MAX_LAT) / 2.0;

        // Base scale to fit screen
        double scaleX = w / (MAX_LON - MIN_LON);
        double scaleY = h / (MAX_LAT - MIN_LAT);
        double scale = Math.min(scaleX, scaleY) * 0.95;

        screenShapes.clear();

        // Project and render coordinates
        for (DepartamentoGeo depto : departamentos) {
            List<Path2D.Double> paths = new ArrayList<>();
            for (List<Point2D> ring : depto.rings) {
                Path2D.Double path = new Path2D.Double();
                boolean first = true;
                for (Point2D pt : ring) {
                    double rx = (pt.lon - mapCenterX) * scale;
                    double ry = (mapCenterY - pt.lat) * scale;

                    // Apply zoom and pan translation relative to screen center
                    double sx = centerX + panX + rx * zoomFactor;
                    double sy = centerY + panY + ry * zoomFactor;

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

            // Fetch sqlite risks
            String normName = normalizarNombre(depto.name);
            String risk = riesgos.getOrDefault(normName, "SIN DATOS");

            Color baseColor = new Color(55, 65, 81); // Default SIN DATOS (Cool Grey)
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

            // Draw fill
            g2.setColor(isHovered ? baseColor.brighter() : baseColor);
            for (Path2D.Double path : paths) {
                g2.fill(path);
            }

            // Draw border
            g2.setColor(isHovered ? accentTeal : new Color(75, 85, 99));
            g2.setStroke(new BasicStroke(isHovered ? 2.0f : 1.0f));
            for (Path2D.Double path : paths) {
                g2.draw(path);
            }
        }

        // Draw Interactive Tooltip box
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

            g2.setColor(new Color(17, 24, 39, 245));
            g2.fillRoundRect(bx, by, boxW, boxH, 8, 8);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(bx, by, boxW, boxH, 8, 8);

            g2.setColor(accentTeal);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(t1, bx + pad, by + pad + fm.getAscent());

            g2.setColor(riskColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(t2, bx + pad, by + pad + fm.getHeight() + fm.getAscent());
        }

        g2.dispose();
    }
}
