package org.tlr.mandelbrot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class MandelbrotApp extends JFrame {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final int MAX_ITER = 1000;

    private final double minRe = -2.0;
    private final double maxRe = 1.0;
    private final double minIm = -1.5;
    private final double maxIm = 1.5;

    private final MandelbrotPanel panel;

    public MandelbrotApp() {
        setTitle("Mandelbrot Set");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new MandelbrotPanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        startMandelbrotComputation();
    }

    private void startMandelbrotComputation() {
        panel.showMessage("Computing Mandelbrot set...");

        CompletableFuture.supplyAsync(() -> {
            int[] iterCounts = new int[WIDTH * HEIGHT];
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    double re = minRe + (double) x / WIDTH * (maxRe - minRe);
                    double im = minIm + (double) y / HEIGHT * (maxIm - minIm);
                    iterCounts[y * WIDTH + x] = calculateMandelbrot(re, im);
                }
            }
            return iterCounts;
        }).thenAccept(iterCounts -> {
            SwingUtilities.invokeLater(() -> {
                panel.setImage(iterCounts);
                panel.hideMessage();
            });
        });
    }

    private int calculateMandelbrot(double re, double im) {
        double zRe = 0;
        double zIm = 0;
        int iter = 0;
        while (zRe * zRe + zIm * zIm <= 4 && iter < MAX_ITER) {
            double nextRe = zRe * zRe - zIm * zIm + re;
            double nextIm = 2 * zRe * zIm + im;
            zRe = nextRe;
            zIm = nextIm;
            iter++;
        }
        return iter;
    }

    private static class MandelbrotPanel extends JPanel {
        private BufferedImage image;
        private String message;

        public MandelbrotPanel() {
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.BLACK);
        }

        public void setImage(int[] iterCounts) {
            image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int iter = iterCounts[y * WIDTH + x];
                    image.setRGB(x, y, getColor(iter).getRGB());
                }
            }
            repaint();
        }

        public void showMessage(String message) {
            this.message = message;
            repaint();
        }

        public void hideMessage() {
            this.message = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
            if (message != null) {
                g.setColor(Color.WHITE);
                g.drawString(message, 20, 20);
            }
        }

        private Color getColor(int iter) {
            if (iter == MAX_ITER) {
                return Color.BLACK;
            }
            // Simple coloring based on iteration count
            float hue = (float) iter / MAX_ITER;
            return Color.getHSBColor(hue, 1.0f, 1.0f);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MandelbrotApp::new);
    }
}
