package com.terrakort.api;

import java.util.List;
import java.util.Random;
import java.util.Set;

public interface GenerationStep {
    String id();
    default Set<String> requires() { return Set.of(); }
    Set<String> produces();
    default List<Parameter> parameters() { return List.of(); }
    void apply(MapContext ctx, Random rng);
}