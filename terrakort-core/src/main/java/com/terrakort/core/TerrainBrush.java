package com.terrakort.core;

import com.terrakort.api.Layer;
import com.terrakort.api.MapContext;

public final class TerrainBrush {
    public record Region(int x0, int y0, int x1, int y1) {}

    private float radius = 30f;
    private float strength = 0.02f;

    public float radius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }
    public float strength() { return strength; }
    public void setStrength(float strength) { this.strength = strength; }

    /** direction: +1 raises, -1 lowers. Returns the area that changed. */
    public Region apply(MapContext ctx, int cx, int cy, int direction) {
        float[] height = ctx.layer(Layer.HEIGHT);
        int w = ctx.width();
        int h = ctx.height();
        int r = (int) Math.ceil(radius);

        int x0 = Math.max(0, cx - r), x1 = Math.min(w - 1, cx + r);
        int y0 = Math.max(0, cy - r), y1 = Math.min(h - 1, cy + r);

        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                float dx = x - cx, dy = y - cy;
                float d = (float) Math.sqrt(dx * dx + dy * dy) / radius;
                if (d >= 1) continue;

                // Strongest in the centre, fading smoothly to 0 at the edge
                float falloff = 1 - d * d;
                falloff *= falloff;

                int i = y * w + x;
                height[i] = Math.max(0, Math.min(1, height[i] + direction * strength * falloff));
            }
        }
        return new Region(x0, y0, x1, y1);
    }
}