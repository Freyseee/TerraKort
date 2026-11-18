package com.terrakort.api;

public abstract class Brush {
    public enum Button { PRIMARY, SECONDARY }
    public record Region(int x0, int y0, int x1, int y1) {}

    private float radius = 30f;
    private float strength = 0.02f;

    public abstract String name();

    /** What the brush does to one pixel. falloff is 1 in the centre and 0 at the edge. */
    protected abstract float apply(float height, float falloff, Button button);

    /** Called once when the mouse is pressed. Override to remember things, like a target height. */
    public void begin(MapContext ctx, int x, int y, Button button) {}

    public final Region stroke(MapContext ctx, int cx, int cy, Button button) {
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

                float falloff = 1 - d * d;
                falloff *= falloff;

                int i = y * w + x;
                height[i] = Math.max(0, Math.min(1, apply(height[i], falloff, button)));
            }
        }
        return new Region(x0, y0, x1, y1);
    }

    protected float heightAt(MapContext ctx, int x, int y) {
        int cx = Math.max(0, Math.min(ctx.width() - 1, x));
        int cy = Math.max(0, Math.min(ctx.height() - 1, y));
        return ctx.layer(Layer.HEIGHT)[cy * ctx.width() + cx];
    }

    public float radius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }
    public float strength() { return strength; }
    public void setStrength(float strength) { this.strength = strength; }
}