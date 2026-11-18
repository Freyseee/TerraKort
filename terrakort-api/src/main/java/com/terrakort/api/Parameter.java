package com.terrakort.api;

public record Parameter(String id, String label, double min, double max, double defaultValue) {

    /** Unique key across all steps, e.g. "noise.falloff" */
    public String keyFor(GenerationStep step) {
        return step.id() + "." + id;
    }
}