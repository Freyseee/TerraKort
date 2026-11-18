module terrakort.core {
    requires transitive terrakort.api;
    exports com.terrakort.core;
    uses com.terrakort.api.GenerationStep;
    uses com.terrakort.api.Brush;
}