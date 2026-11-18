package com.terrakort.app;

import com.terrakort.api.GenerationStep;
import com.terrakort.api.Layer;
import com.terrakort.api.MapContext;
import com.terrakort.api.Parameter;
import com.terrakort.core.Pipeline;
import com.terrakort.api.Brush;
import com.terrakort.core.Brushes;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javafx.geometry.Bounds;

public class App extends Application {
    private final Pipeline pipeline = Pipeline.fromServiceLoader();
    private final List<Brush> brushes = Brushes.load();
    private Brush activeBrush = brushes.get(0);
    private final ImageView view = new ImageView();
    private final Map<String, Double> params = new HashMap<>();

    private int mapWidth = 800;
    private int mapHeight = 600;
    private long currentSeed = 42;

    private MapContext ctx;
    private WritableImage image;

    private final ScrollPane mapScroll = new ScrollPane(view);
    private boolean fitToWindow = false;
    private Spinner<Integer> widthSpinner;
    private Spinner<Integer> heightSpinner;

    //Slider position → speed. Add, remove, or move points to reshape the slider.
    private static final double[] SLIDER_POS = { 0.0,     0.25,   0.75,  1.0  };
    private static final double[] SPEED      = { 0.0005,  0.002,  0.01,  0.05 };

    private static float speedFor(double pos) {
        for (int i = 1; i < SLIDER_POS.length; i++) {
            if (pos <= SLIDER_POS[i]) {
                double t = (pos - SLIDER_POS[i - 1]) / (SLIDER_POS[i] - SLIDER_POS[i - 1]);
                return (float) (SPEED[i - 1] + (SPEED[i] - SPEED[i - 1]) * t);
            }
        }
        return (float) SPEED[SPEED.length - 1];
    }

    @Override
    public void start(Stage stage) {
        TextField seedField = new TextField("42");
        Button generate = new Button("Generate");
        Button random = new Button("Random");

        // Brush settings apply to all brushes, so switching tools keeps your size and speed
        Slider brushSize = new Slider(5, 100, activeBrush.radius());
        brushSize.valueProperty().addListener((obs, o, n) ->
                brushes.forEach(b -> b.setRadius(n.floatValue())));

        Slider brushStrength = new Slider(0, 1, 0.5);
        brushStrength.valueProperty().addListener((obs, o, n) ->
                brushes.forEach(b -> b.setStrength(speedFor(n.doubleValue()))));
        brushes.forEach(b -> b.setStrength(speedFor(brushStrength.getValue())));

// One button per brush, found automatically
        ToggleGroup tools = new ToggleGroup();
        HBox toolButtons = new HBox(4);
        for (Brush b : brushes) {
            ToggleButton button = new ToggleButton(b.name());
            button.setToggleGroup(tools);
            button.setUserData(b);
            button.setSelected(b == activeBrush);
            toolButtons.getChildren().add(button);
        }
        tools.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == null) {
                tools.selectToggle(o);        // don't allow "no tool selected"
            } else {
                activeBrush = (Brush) n.getUserData();
            }
        });

        generate.setOnAction(e -> generate(parseSeed(seedField.getText())));
        random.setOnAction(e -> {
            long seed = new Random().nextLong();
            seedField.setText(Long.toString(seed));
            generate(seed);
        });

        view.setOnMousePressed(e -> {
            Brush.Button button = buttonOf(e);
            if (button == null) return;
            activeBrush.begin(ctx, (int) e.getX(), (int) e.getY(), button);
            paint(e);
        });
        view.setOnMouseDragged(this::paint);

        HBox toolbar = new HBox(8,
                new Label("Seed:"), seedField, generate, random,
                toolButtons,
                new Label("Brush:"), brushSize,
                new Label("Speed:"), brushStrength);
        toolbar.setPadding(new Insets(8));

        BorderPane root = new BorderPane(mapScroll);
        root.setTop(toolbar);
        root.setRight(buildAdvancedPanel());

        generate(42);
        stage.setScene(new Scene(root, 1150, 700));
        stage.setTitle("TerraKort");
        stage.show();
    }

    private Node buildAdvancedPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(8));
        box.setPrefWidth(250);

        //Map size
        widthSpinner = new Spinner<>(200, 4000, mapWidth, 100);
        heightSpinner = new Spinner<>(200, 4000, mapHeight, 100);
        widthSpinner.setEditable(true);
        heightSpinner.setEditable(true);
        widthSpinner.valueProperty().addListener((obs, o, n) -> mapWidth = n);
        heightSpinner.valueProperty().addListener((obs, o, n) -> mapHeight = n);

        CheckBox fit = new CheckBox("Fit to window");
        fit.selectedProperty().addListener((obs, o, n) -> {
            fitToWindow = n;
            widthSpinner.setDisable(n);
            heightSpinner.setDisable(n);
        });

        Label sizeTitle = new Label("Map size");
        sizeTitle.setStyle("-fx-font-weight: bold");
        box.getChildren().addAll(sizeTitle,
                fit,
                new HBox(6, new Label("Width"), widthSpinner),
                new HBox(6, new Label("Height"), heightSpinner));

        // One section per step, built from its parameters
        for (GenerationStep step : pipeline.steps()) {
            if (step.parameters().isEmpty()) continue;

            Label stepTitle = new Label(step.id());
            stepTitle.setStyle("-fx-font-weight: bold");
            box.getChildren().add(stepTitle);

            for (Parameter p : step.parameters()) {
                String key = p.keyFor(step);
                Slider slider = new Slider(p.min(), p.max(), params.getOrDefault(key, p.defaultValue()));
                Label value = new Label(format(slider.getValue()));
                slider.valueProperty().addListener((obs, o, n) -> {
                    params.put(key, n.doubleValue());
                    value.setText(format(n.doubleValue()));
                });
                box.getChildren().addAll(new HBox(6, new Label(p.label() + ":"), value), slider);
            }
        }

        Button apply = new Button("Apply");
        apply.setOnAction(e -> generate(currentSeed));
        box.getChildren().add(apply);

        TitledPane pane = new TitledPane("Advanced settings", new ScrollPane(box));
        pane.setCollapsible(false);
        return pane;
    }

    private static String format(double v) {
        return String.format("%.2f", v);
    }

    private long parseSeed(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return text.hashCode();
        }
    }

    private void generate(long seed) {
        currentSeed = seed;

        if (fitToWindow) {
            Bounds visible = mapScroll.getViewportBounds();
            mapWidth = Math.max(200, (int) visible.getWidth());
            mapHeight = Math.max(200, (int) visible.getHeight());
            // Show the actual size in the (greyed out) boxes
            widthSpinner.getValueFactory().setValue(mapWidth);
            heightSpinner.getValueFactory().setValue(mapHeight);
        }

        ctx = pipeline.generate(mapWidth, mapHeight, seed, params);
        image = new WritableImage(ctx.width(), ctx.height());
        view.setImage(image);
        redraw(0, 0, ctx.width() - 1, ctx.height() - 1);
    }

    private void paint(MouseEvent e) {
        Brush.Button button = buttonOf(e);
        if (button == null) return;
        Brush.Region r = activeBrush.stroke(ctx, (int) e.getX(), (int) e.getY(), button);
        redraw(r.x0(), r.y0(), r.x1(), r.y1());
    }

    private static Brush.Button buttonOf(MouseEvent e) {
        if (e.isSecondaryButtonDown()) return Brush.Button.SECONDARY;
        if (e.isPrimaryButtonDown()) return Brush.Button.PRIMARY;
        return null;
    }

    private void redraw(int x0, int y0, int x1, int y1) {
        float[] height = ctx.layer(Layer.HEIGHT);
        int w = ctx.width();
        PixelWriter pw = image.getPixelWriter();
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                pw.setColor(x, y, colorFor(height[y * w + x]));
            }
        }
    }

    private static Color colorFor(float v) {
        if (v < 0.30f) return Color.rgb(30, 60, 120);
        if (v < 0.35f) return Color.rgb(60, 110, 180);
        if (v < 0.37f) return Color.rgb(220, 205, 150);
        if (v < 0.50f) return Color.rgb(90, 150, 70);
        if (v < 0.60f) return Color.rgb(50, 110, 50);
        if (v < 0.70f) return Color.rgb(120, 110, 100);
        return Color.rgb(240, 240, 240);
    }

    public static void main(String[] args) {
        launch(args);
    }
}