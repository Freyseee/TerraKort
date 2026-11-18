module terrakort.basics {
    requires terrakort.api;
    provides com.terrakort.api.GenerationStep
            with com.terrakort.basics.NoiseStep;
}