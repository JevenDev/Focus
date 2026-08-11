package com.jvn.focus.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public final class CustomIndicatorPixelTexture {
    public static final int SIZE = CustomIndicatorPixelArt.SIZE;
    public static final int PIXEL_COUNT = CustomIndicatorPixelArt.PIXEL_COUNT;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("focus", "dynamic/custom_indicator_pixel_art");
    private static DynamicTexture dynamicTexture;
    private static String uploadedPixelArt;

    private CustomIndicatorPixelTexture() {}

    public static ResourceLocation texture() {
        String pixelArt = FocusClientConfig.customIndicatorPixelArt();
        if (dynamicTexture == null || !pixelArt.equals(uploadedPixelArt)) {
            upload(pixelArt);
        }
        return TEXTURE;
    }

    public static ResourceLocation preview(int[] pixels) {
        String encoded = encode(pixels);
        if (dynamicTexture == null || !encoded.equals(uploadedPixelArt)) {
            upload(encoded);
        }
        return TEXTURE;
    }

    public static void invalidate() {
        uploadedPixelArt = null;
    }

    public static int[] decode(String encoded) {
        return CustomIndicatorPixelArt.decode(encoded);
    }

    public static String encode(int[] pixels) {
        return CustomIndicatorPixelArt.encode(pixels);
    }

    public static int[] defaultPixels() {
        return CustomIndicatorPixelArt.defaultPixels();
    }

    private static void upload(String encoded) {
        int[] pixels = decode(encoded);
        NativeImage image = new NativeImage(SIZE, SIZE, true);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int argb = pixels[y * SIZE + x];
                image.setPixelRGBA(x, y, FastColor.ABGR32.color(
                        argb >>> 24,
                        argb & 0xFF,
                        argb >>> 8 & 0xFF,
                        argb >>> 16 & 0xFF));
            }
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (dynamicTexture == null) {
            dynamicTexture = new DynamicTexture(image);
            dynamicTexture.setFilter(false, false);
            minecraft.getTextureManager().register(TEXTURE, dynamicTexture);
        } else {
            dynamicTexture.setPixels(image);
            dynamicTexture.upload();
        }
        uploadedPixelArt = encoded;
    }
}
