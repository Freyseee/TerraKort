package com.terrakort.basics;

final class ValueNoise {
    private final long seed;

    ValueNoise(long seed) {
        this.seed = seed;
    }

    // Gives a "random" but repeatable value between 0 and 1 for each grid point
    private float hash(int x, int y) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (h >>> 40) / (float) (1L << 24);
    }

    // Smoothly blends between the four nearest grid points
    float sample(float x, float y) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        float tx = x - x0;
        float ty = y - y0;

        // Smoothstep: eases in and out so there are no sharp creases
        float sx = tx * tx * (3 - 2 * tx);
        float sy = ty * ty * (3 - 2 * ty);

        float top = lerp(hash(x0, y0), hash(x0 + 1, y0), sx);
        float bottom = lerp(hash(x0, y0 + 1), hash(x0 + 1, y0 + 1), sx);
        return lerp(top, bottom, sy);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}