package com.jvn.focus.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CustomIndicatorPixelTextureTest {
    @Test
    void pixelArtRoundTripsAllArgbChannels() {
        int[] pixels = new int[CustomIndicatorPixelArt.PIXEL_COUNT];
        pixels[0] = 0x00000000;
        pixels[1] = 0x80FF8040;
        pixels[2] = 0xFFFFFFFF;
        pixels[pixels.length - 1] = 0x01020304;

        String encoded = CustomIndicatorPixelArt.encode(pixels);

        assertEquals(CustomIndicatorPixelArt.PIXEL_COUNT * 8, encoded.length());
        assertArrayEquals(pixels, CustomIndicatorPixelArt.decode(encoded));
    }

    @Test
    void malformedPixelArtFallsBackToVisibleDefaultIcon() {
        int[] pixels = CustomIndicatorPixelArt.decode("not-pixel-art");

        assertEquals(CustomIndicatorPixelArt.PIXEL_COUNT, pixels.length);
        assertTrue(java.util.Arrays.stream(pixels).anyMatch(pixel -> (pixel >>> 24) != 0));
    }
}
