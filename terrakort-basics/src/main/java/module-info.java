module terrakort.basics {
    requires terrakort.api;
    provides com.terrakort.api.GenerationStep
            with com.terrakort.basics.NoiseStep;
    provides com.terrakort.api.Brush
            with com.terrakort.basics.SculptBrush, com.terrakort.basics.FlattenBrush;
}