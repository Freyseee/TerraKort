package com.terrakort.app;

import com.terrakort.api.Layer;
import com.terrakort.api.MapContext;
import com.terrakort.core.Pipeline;
import com.terrakort.core.TerrainBrush;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class App extends Application {
    private static final int W = 800;
    private static final int H = 600;

    private final Pipeline pipeline = Pipeline.fromServiceLoader();
    private final TerrainBrush brush = new TerrainBrush();
    private final ImageView view = new ImageView();

    private MapContext ctx;
    private WritableImage image;

    @Override
    public void start(Stage stage) {
        TextField seedField = new TextField("42");
        Button generate = new Button("Generate");
        Button random = new Button("Random");

        Slider brushSize = new Slider(5, 100, brush.radius()); //Brush size
        brushSize.valueProperty().addListener((obs, oldV, newV) -> brush.setRadius(newV.floatValue()));

        Slider brushStrength = new Slider(0.0001, 0.005, brush.strength()); //How much the terrain raises and lowers
        brushStrength.valueProperty().addListener((obs, oldV, newV) -> brush.setStrength(newV.floatValue()));

        generate.setOnAction(e -> generate(parseSeed(seedField.getText())));
        random.setOnAction(e -> {
            long seed = new Random().nextLong();
            seedField.setText(Long.toString(seed));
            generate(seed);
        });

        // Painting: right click raises, left click lowers
        view.setOnMousePressed(this::paint);
        view.setOnMouseDragged(this::paint);

        HBox toolbar = new HBox(8,
                new Label("Seed:"), seedField, generate, random,
                new Label("Brush:"), brushSize,
                new Label("Speed:"), brushStrength);
        toolbar.setPadding(new Insets(8));

        BorderPane root = new BorderPane(view);
        root.setTop(toolbar);

        generate(42);
        stage.setScene(new Scene(root, W, H + 50));
        stage.setTitle("TerraKort");
        stage.show();
    }

    private long parseSeed(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return text.hashCode();
        }
    }

    private void generate(long seed) {
        ctx = pipeline.generate(W, H, seed);
        image = new WritableImage(W, H);
        view.setImage(image);
        redraw(0, 0, W - 1, H - 1);
    }

    private void paint(MouseEvent e) {
        int direction;
        if (e.isSecondaryButtonDown()) direction = 1;       // right click: raise
        else if (e.isPrimaryButtonDown()) direction = -1;   // left click: lower
        else return;

        TerrainBrush.Region r = brush.apply(ctx, (int) e.getX(), (int) e.getY(), direction);
        redraw(r.x0(), r.y0(), r.x1(), r.y1());
    }

    // Only redraws the area that changed, so painting stays smooth
    private void redraw(int x0, int y0, int x1, int y1) {
        float[] height = ctx.layer(Layer.HEIGHT);
        PixelWriter pw = image.getPixelWriter();
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                pw.setColor(x, y, colorFor(height[y * W + x]));
            }
        }
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