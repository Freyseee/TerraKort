package com.terrakort.basics;

import com.terrakort.api.Brush;
import com.terrakort.api.MapContext;

public final class FlattenBrush extends Brush {
    private float target;

    @Override public String name() { return "Flatten"; }

    @Override
    public void begin(MapContext ctx, int x, int y, Button button) {
        target = heightAt(ctx, x, y);
    }

    @Override
    protected float apply(float height, float falloff, Button button) {
        float speed = strength() * 5;
        return height + (target - height) * Math.min(1, speed * falloff);
    }
}