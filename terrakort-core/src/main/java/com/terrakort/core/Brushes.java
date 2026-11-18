package com.terrakort.core;

import com.terrakort.api.Brush;
import java.util.List;
import java.util.ServiceLoader;

public final class Brushes {
    private Brushes() {}

    public static List<Brush> load() {
        return ServiceLoader.load(Brush.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }
}