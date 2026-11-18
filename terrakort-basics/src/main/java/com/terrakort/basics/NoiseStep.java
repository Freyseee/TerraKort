package com.terrakort.basics;

import com.terrakort.api.GenerationStep;
import com.terrakort.api.Layer;
import com.terrakort.api.MapContext;
import com.terrakort.api.Parameter;

import java.util.List;
import java.util.Random;
import java.util.Set;

public final class NoiseStep implements GenerationStep {
    static final Parameter SCALE   = new Parameter("scale", "Feature size", 32, 512, 128);
    static final Parameter OCTAVES = new Parameter("octaves", "Detail", 1, 8, 6);
    static final Parameter FALLOFF = new Parameter("falloff", "Island falloff", 0, 1.5, 0.6);

    @Override public String id() { return "noise"; }
    @Override public Set<String> produces() { return Set.of(Layer.HEIGHT); }
    @Override public List<Parameter> parameters() { return List.of(SCALE, OCTAVES, FALLOFF); }

    @Override
    public void apply(MapContext ctx, Random rng) {
        float scale = (float) ctx.param(this, SCALE);
        int octaves = (int) Math.round(ctx.param(this, OCTAVES));
        float falloff = (float) ctx.param(this, FALLOFF);

        ValueNoise noise = new ValueNoise(rng.nextLong());
        int w = ctx.width();
        int h = ctx.height();
        float[] height = ctx.createLayer(Layer.HEIGHT);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float value = 0, amplitude = 1, frequency = 1f / scale, total = 0;
                for (int octave = 0; octave < octaves; octave++) {
                    value += noise.sample(x * frequency + octave * 17.3f, y * frequency) * amplitude;
                    total += amplitude;
                    amplitude *= 0.5f;
                    frequency *= 2f;
                }
                value /= total;

                float dx = x / (float) w * 2 - 1;
                float dy = y / (float) h * 2 - 1;
                value -= falloff * (dx * dx + dy * dy);

                height[y * w + x] = Math.max(0, Math.min(1, value));
            }
        }
    }
}