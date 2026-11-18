// terrakort-core
module terrakort.core {
    requires transitive terrakort.api;
    exports com.terrakort.core;
    uses com.terrakort.api.GenerationStep;
}