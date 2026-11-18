package com.terrakort.basics;

import com.terrakort.api.Brush;

public final class SculptBrush extends Brush {
    @Override public String name() { return "Sculpt"; }

    @Override
    protected float apply(float height, float falloff, Button button) {
        int direction = button == Button.SECONDARY ? 1 : -1; // right raises, left lowers
        return height + direction * strength() * falloff;
    }
}