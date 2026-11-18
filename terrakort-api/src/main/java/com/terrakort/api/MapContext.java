package com.terrakort.api;

import java.util.HashMap;
import java.util.Map;

public final class MapContext {
    private final int width;
    private final int height;
    private final long seed;
    private final Map<String, Double> params;
    private final Map<String, float[]> layers = new HashMap<>();

    public MapContext(int width, int height, long seed) {
        this(width, height, seed, Map.of());
    }

    public MapContext(int width, int height, long seed, Map<String, Double> params) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.params = Map.copyOf(params);
    }

    public int width() { return width; }
    public int height() { return height; }
    public long seed() { return seed; }

    /** The user's chosen value, or the parameter's default if they haven't changed it. */
    public double param(GenerationStep step, Parameter p) {
        return params.getOrDefault(p.keyFor(step), p.defaultValue());
    }

    public float[] createLayer(String name) {
        float[] layer = new float[width * height];
        layers.put(name, layer);
        return layer;
    }

    public float[] layer(String name) {
        float[] layer = layers.get(name);
        if (layer == null) throw new IllegalArgumentException("No layer: " + name);
        return layer;
    }

    public boolean hasLayer(String name) { return layers.containsKey(name); }
}