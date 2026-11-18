package com.terrakort.core;

import com.terrakort.api.GenerationStep;
import com.terrakort.api.MapContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.Set;

public final class Pipeline {
    private final List<GenerationStep> steps = new ArrayList<>();

    public static Pipeline fromServiceLoader() {
        Pipeline pipeline = new Pipeline();
        ServiceLoader.load(GenerationStep.class).forEach(pipeline::register);
        return pipeline;
    }

    public void register(GenerationStep step) {
        steps.add(step);
    }

    public List<GenerationStep> steps() {
        return List.copyOf(steps);
    }

    public MapContext generate(int width, int height, long seed) {
        MapContext ctx = new MapContext(width, height, seed);
        for (GenerationStep step : ordered()) {
            // Each step gets its own random stream, so adding a plugin
            // doesn't change what the other steps generate.
            Random rng = new Random(seed ^ step.id().hashCode() * 0x9E3779B97F4A7C15L);
            step.apply(ctx, rng);
        }
        return ctx;
    }

    private List<GenerationStep> ordered() {
        List<GenerationStep> remaining = new ArrayList<>(steps);
        List<GenerationStep> result = new ArrayList<>();
        Set<String> available = new HashSet<>();

        while (!remaining.isEmpty()) {
            GenerationStep next = remaining.stream()
                    .filter(s -> available.containsAll(s.requires()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Steps with missing layers: "
                                    + remaining.stream().map(GenerationStep::id).toList()));
            remaining.remove(next);
            result.add(next);
            available.addAll(next.produces());
        }
        return result;
    }
}