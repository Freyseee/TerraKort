package com.terrakort.app;

import com.terrakort.api.Layer;
import com.terrakort.api.MapContext;
import com.terrakort.core.Pipeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class App extends Application {
    private static final int W = 800;
    private static final int H = 600;

    private final Pipeline pipeline = Pipeline.fromServiceLoader();
    private final ImageView view = new ImageView();

    @Override
    public void start(Stage stage) {
        TextField seedField = new TextField("42");
        Button generate = new Button("Generate");
        Button random = new Button("Random");

        generate.setOnAction(e -> render(parseSeed(seedField.getText())));
        random.setOnAction(e -> {
            long seed = new Random().nextLong();
            seedField.setText(Long.toString(seed));
            render(seed);
        });

        HBox toolbar = new HBox(8, new Label("Seed:"), seedField, generate, random);
        toolbar.setPadding(new Insets(8));

        BorderPane root = new BorderPane(view);
        root.setTop(toolbar);

        render(42);
        stage.setScene(new Scene(root, W, H + 50));
        stage.setTitle("TerraKort");
        stage.show();
    }

    private long parseSeed(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return text.hashCode(); // words work as seeds too
        }
    }

    private void render(long seed) {
        MapContext ctx = pipeline.generate(W, H, seed);
        float[] height = ctx.layer(Layer.HEIGHT);

        WritableImage image = new WritableImage(W, H);
        PixelWriter pw = image.getPixelWriter();
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                pw.setColor(x, y, colorFor(height[y * W + x]));
            }
        }
        view.setImage(image);
    }

    private static Color colorFor(float v) {
        if (v < 0.30f) return Color.rgb(30, 60, 120);    // deep water
        if (v < 0.35f) return Color.rgb(60, 110, 180);   // shallow water
        if (v < 0.37f) return Color.rgb(220, 205, 150);  // beach
        if (v < 0.50f) return Color.rgb(90, 150, 70);    // grass
        if (v < 0.60f) return Color.rgb(50, 110, 50);    // forest
        if (v < 0.70f) return Color.rgb(120, 110, 100);  // mountain
        return Color.rgb(240, 240, 240);                 // snow
    }

    public static void main(String[] args) {
        launch(args);
    }
}