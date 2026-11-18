package com.terrakort.basics;

import com.terrakort.api.GenerationStep;
import com.terrakort.api.Layer;
import com.terrakort.api.MapContext;

import java.util.Random;
import java.util.Set;

public final class NoiseStep implements GenerationStep {

    @Override
    public String id() {
        return "noise";
    }

    @Override
    public Set<String> produces() {
        return Set.of(Layer.HEIGHT);
    }

    @Override
    public void apply(MapContext ctx, Random rng) {
        ValueNoise noise = new ValueNoise(rng.nextLong());
        int w = ctx.width();
        int h = ctx.height();
        float[] height = ctx.createLayer(Layer.HEIGHT);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // Fractal noise: big shapes + smaller and smaller details
                float value = 0;
                float amplitude = 1;
                float frequency = 1f / 128f;
                float total = 0;
                for (int octave = 0; octave < 6; octave++) {
                    value += noise.sample(x * frequency + octave * 17.3f, y * frequency) * amplitude;
                    total += amplitude;
                    amplitude *= 0.5f;
                    frequency *= 2f;
                }
                value /= total;

                // Push the edges down so we get an island instead of endless land
                float dx = x / (float) w * 2 - 1;
                float dy = y / (float) h * 2 - 1;
                value -= 0.6f * (dx * dx + dy * dy);

                height[y * w + x] = Math.max(0, Math.min(1, value));
            }
        }
    }
}