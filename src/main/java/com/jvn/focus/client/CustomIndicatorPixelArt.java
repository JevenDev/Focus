package com.jvn.focus.client;

import java.util.Locale;

public final class CustomIndicatorPixelArt {
    public static final int SIZE = 16;
    public static final int PIXEL_COUNT = SIZE * SIZE;

    private CustomIndicatorPixelArt() {}

    public static int[] decode(String encoded) {
        if (encoded == null || encoded.length() != PIXEL_COUNT * 8) {
            return defaultPixels();
        }

        int[] pixels = new int[PIXEL_COUNT];
        try {
            for (int i = 0; i < PIXEL_COUNT; i++) {
                pixels[i] = (int) Long.parseLong(encoded, i * 8, i * 8 + 8, 16);
            }
            return pixels;
        } catch (NumberFormatException ignored) {
            return defaultPixels();
        }
    }

    public static String encode(int[] pixels) {
        if (pixels == null || pixels.length != PIXEL_COUNT) {
            pixels = defaultPixels();
        }
        StringBuilder encoded = new StringBuilder(PIXEL_COUNT * 8);
        for (int pixel : pixels) {
            encoded.append(String.format(Locale.ROOT, "%08x", pixel));
        }
        return encoded.toString();
    }

    public static int[] defaultPixels() {
        int[] pixels = new int[PIXEL_COUNT];
        int white = 0xFFFFFFFF;
        int accent = 0xFFFFD45A;
        for (int y = 2; y < SIZE - 2; y++) {
            for (int x = 2; x < SIZE - 2; x++) {
                double distance = Math.hypot(x - 7.5D, y - 7.5D);
                if (distance >= 4.6D && distance <= 5.8D) {
                    pixels[y * SIZE + x] = white;
                }
            }
        }
        pixels[7 * SIZE + 7] = accent;
        pixels[7 * SIZE + 8] = accent;
        pixels[8 * SIZE + 7] = accent;
        pixels[8 * SIZE + 8] = accent;
        return pixels;
    }
}
