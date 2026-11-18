package com.terrakort.api;

import java.util.Random;
import java.util.Set;

public interface GenerationStep {
    String id();
    default Set<String> requires() { return Set.of(); }
    Set<String> produces();
    void apply(MapContext ctx, Random rng);
}