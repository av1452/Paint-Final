package com.example.finalturnin;

//source for shapes and other functions:
//oracle: https://docs.oracle.com/javafx/2/ui_controls/jfxpub-ui_controls.htm
//https://stackoverflow.com/questions/29064225/how-to-create-a-javafx-keycombination-with-three-or-more-keys
//https://github.com/junit-team/junit5/?search=1

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;
import java.util.Stack;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.control.Tooltip;

import javax.imageio.ImageIO;

/**
 * FinalTurnIn is a JavaFX application that provides a drawing canvas where users can create and manipulate images.
 * It allows users to draw various shapes, use different tools, and adjust properties such as line width and color.
 */
public class FinalTurnIn extends Application {
    /**
     * The canvas used to display the current image.
     */
    private Canvas imageCanvas = new Canvas(1000, 900);

    /**
     * The canvas used for drawing operations.
     */
    private Canvas drawingCanvas = new Canvas(1000, 900);

    /**
     * Color picker for selecting the drawing color.
     */
    private final ColorPicker colorPicker = new ColorPicker(Color.BLACK);

    /**
     * Slider to adjust the line width for drawing.
     */
    private final Slider lineWidthSlider = new Slider(1, 20, 2);

    /**
     * Label displaying the current line width.
     */
    private final Label lineWidthLabel = new Label("Width: " + (int) lineWidthSlider.getValue());

    /**
     * Button to clear the drawing canvas.
     */
    private final Button clearButton = new Button("⌧");

    /**
     * Label to display the current color.
     */
    private final Label colorLabel = new Label();

    /**
     * File chooser for opening and saving images.
     */
    private final FileChooser fileChooser = new FileChooser();

    /**
     * Graphics context for the image canvas.
     */
    private GraphicsContext imageGC = imageCanvas.getGraphicsContext2D();

    /**
     * Graphics context for the drawing canvas.
     */
    private GraphicsContext drawingGC = drawingCanvas.getGraphicsContext2D();

    /**
     * The currently loaded image.
     */
    private Image currentImage;

    /**
     * The x-coordinate of the image's position on the canvas.
     */
    private double imageX, imageY;

    /**
     * The width of the image.
     */
    private double imageWidth, imageHeight;

    /**
     * Toggle button for the pencil tool.
     */
    private final ToggleButton pencilToggle = new ToggleButton("✎");

    /**
     * Toggle button for the straight line tool.
     */
    private final ToggleButton straightLineToggle = new ToggleButton("/");

    /**
     * Toggle button for the eraser tool.
     */
    private final ToggleButton eraserToggle = new ToggleButton("⌫");

    /** Group for toggling between shape tools. */
    static {
        new ToggleGroup();
    }

    /**
     * Toggle button for the rectangle shape.
     */
    private final ToggleButton rectangleToggle = new ToggleButton("□");

    /**
     * Toggle button for the circle shape.
     */
    private final ToggleButton circleToggle = new ToggleButton("◯");

    /**
     * Toggle button for the star shape.
     */
    private final ToggleButton starToggle = new ToggleButton("★");

    /**
     * Toggle button for the triangle shape.
     */
    private final ToggleButton triangleToggle = new ToggleButton("△"); // New shape toggle

    /**
     * Toggle button for the polygon shape.
     */
    private final ToggleButton polygonToggle = new ToggleButton("⬠"); // New shape toggle

    /**
     * Toggle button for the text input tool.
     */
    private final ToggleButton textToggle = new ToggleButton("⇢");

    /**
     * Checkbox for enabling dashed outlines.
     */
    private final CheckBox dashedOutlineCheckBox = new CheckBox("--");

    /**
     * Slider to select the number of points for the star shape.
     */
    private Slider starPointSlider;

    /**
     * Enumeration of the supported shape types.
     */
    private enum ShapeType {NONE, RECTANGLE, CIRCLE, STAR, POLYGON, TRIANGLE}

    /**
     * The currently selected shape type.
     */
    private ShapeType currentShapeType = ShapeType.NONE;

    /**
     * Flag indicating whether a shape is currently being drawn.
     */
    private boolean drawingShape = false;

    /**
     * The starting x-coordinate for shape drawing.
     */
    private double shapeStartX, shapeStartY;

    /**
     * Enumeration of the available drawing tools.
     */
    private enum Tool {PENCIL, LINE, ERASER, RECTANGLE, CIRCLE, STAR, TRIANGLE, POLYGON, TEXTBOX}

    /**
     * The currently selected drawing tool.
     */
    private Tool currentTool = Tool.PENCIL; // Initializes the current tool to Pencil

    /**
     * The pane that holds the canvas.
     */
    private StackPane canvasPane;

    /**
     * Stack for undo operations.
     */
    private Stack<WritableImage> undoStack = new Stack<>();

    /**
     * Stack for redo operations.
     */
    private Stack<WritableImage> redoStack = new Stack<>();


    @Override
/**
 * Starts the JavaFX application and sets up the main stage, including the user interface components
 * for the drawing application, such as the menu bar, canvases, tool options, and event handling.
 *
 * @param stage The primary stage for this application, onto which the application scene can be set.
 */
    public void start(final Stage stage) {
        stage.setTitle("JavaDraw: A Canvas Where You Espresso Yourself!");

        // Create the main TabPane
        TabPane tabPane = new TabPane();

        // Create the Drawing tab
        Tab drawingTab = new Tab("Drawing");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(createMenuBar(stage));

        // Initialize the drawing and image canvases
        GraphicsContext imageGC = imageCanvas.getGraphicsContext2D();
        GraphicsContext drawingGC = drawingCanvas.getGraphicsContext2D();

        // Create HBox for color picker, line width slider, and buttons
        HBox topBox = new HBox(10);
        topBox.getChildren().addAll(colorPicker, lineWidthLabel, lineWidthSlider, clearButton);

        // Create and add buttons for drawing tools and shapes
        HBox toolsBox = new HBox(10);
        toolsBox.getChildren().addAll(pencilToggle, straightLineToggle, eraserToggle);

        HBox shapeBox = new HBox(10);
        shapeBox.getChildren().addAll(rectangleToggle, circleToggle, starToggle, triangleToggle, polygonToggle, textToggle, dashedOutlineCheckBox);

        // Create canvas pane
        StackPane canvasPane = new StackPane();
        canvasPane.getChildren().addAll(imageCanvas, drawingCanvas);

        // Add topBox, toolsBox, shapeBox, and canvasPane to root
        root.getChildren().addAll(topBox, toolsBox, shapeBox, colorLabel, canvasPane);

        // Add the drawing tab to the TabPane
        drawingTab.setContent(root);
        tabPane.getTabs().add(drawingTab);

        // Set the TabPane as the center of the scene
        Scene mainScene = new Scene(tabPane, 1000, 900);
        stage.setScene(mainScene);
        stage.show();

        // Add slider for star points dynamically
        Slider starPointSlider = new Slider(4, 20, 5); // Min 4 points, max 20, default 5
        starPointSlider.setShowTickLabels(true);
        starPointSlider.setShowTickMarks(true);
        starPointSlider.setMajorTickUnit(1);
        starPointSlider.setSnapToTicks(true);

        // Add the slider to the controls (below other elements)
        root.getChildren().add(starPointSlider);

        // Add keyboard shortcuts
        mainScene.setOnKeyPressed(event -> {
            // Check for Save (Command + S or Ctrl + S)
            if (new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN).match(event) ||
                    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
                saveImage(stage);
                event.consume(); // Prevent further handling
            }
            // Check for Undo (Command + Z or Ctrl + Z)
            else if (new KeyCodeCombination(KeyCode.Z, KeyCombination.META_DOWN).match(event) ||
                    new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN).match(event)) {
                undo(); // Call your undo function here
                event.consume(); // Prevent further handling
            }
            // Check for Redo (Command + Y or Ctrl + Y)
            else if (new KeyCodeCombination(KeyCode.Y, KeyCombination.META_DOWN).match(event) ||
                    new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN).match(event)) {
                redo(); // Call your redo function here
                event.consume(); // Prevent further handling
            }
        });

        // Ensure that the main scene can receive focus
        mainScene.getRoot().setFocusTraversable(true);
        mainScene.getRoot().requestFocus();

        // Add clear button with confirmation dialog
        clearButton.setOnAction(e -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Clear Canvas Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to clear the canvas? This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                saveStateForUndo();
                drawingGC.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
            }
        });

        // Set up drawing tool actions
        pencilToggle.setOnAction(e -> setActiveTool(Tool.PENCIL));
        pencilToggle.setTooltip(new Tooltip("Pencil"));
        straightLineToggle.setOnAction(e -> setActiveTool(Tool.LINE));
        straightLineToggle.setTooltip(new Tooltip("Straight Line"));
        eraserToggle.setOnAction(e -> setActiveTool(Tool.ERASER));
        eraserToggle.setTooltip(new Tooltip("Eraser"));
        rectangleToggle.setOnAction(e -> {
            currentShapeType = ShapeType.RECTANGLE;
            setActiveTool(Tool.RECTANGLE);
            rectangleToggle.setTooltip(new Tooltip("Square"));
        });
        circleToggle.setOnAction(e -> {
            currentShapeType = ShapeType.CIRCLE;
            setActiveTool(Tool.CIRCLE);
            circleToggle.setTooltip(new Tooltip("Circle"));
        });
        starToggle.setOnAction(e -> {
            currentShapeType = ShapeType.STAR;
            setActiveTool(Tool.STAR);
            starToggle.setTooltip(new Tooltip("Star"));
        });
        triangleToggle.setOnAction(e -> {
            currentShapeType = ShapeType.TRIANGLE;
            setActiveTool(Tool.TRIANGLE);
            triangleToggle.setTooltip(new Tooltip("Triangle"));
        });
        polygonToggle.setOnAction(e -> {
            currentShapeType = ShapeType.POLYGON;
            setActiveTool(Tool.POLYGON);
            polygonToggle.setTooltip(new Tooltip("Polygon"));
        });
        textToggle.setOnAction(e -> {
            currentTool = Tool.TEXTBOX;
            setActiveTool(Tool.TEXTBOX);
            textToggle.setTooltip(new Tooltip("Text"));
        });
    }

    /**
     * Loads an image from the specified file and updates the drawing canvas with the image.
     *
     * @param file The image file to be loaded. It must be a valid image file format.
     */
    private void loadImage(File file) {
        try {
            System.out.println("Loading image from: " + file.toURI()); // Debugging
            currentImage = new Image(file.toURI().toString());

            // Check if there was an error loading the image
            if (currentImage.isError()) {
                System.out.println("Error loading image: " + currentImage.getException().getMessage()); // Debugging
                return; // Exit if there's an error
            }

            // Get image dimensions and calculate position for centering
            imageWidth = currentImage.getWidth();
            imageHeight = currentImage.getHeight();
            imageX = (drawingCanvas.getWidth() - imageWidth) / 2;
            imageY = (drawingCanvas.getHeight() - imageHeight) / 2;

            System.out.println("Image loaded successfully. Width: " + imageWidth + ", Height: " + imageHeight); // Debugging
            redrawCanvas(); // Ensure the canvas is redrawn after loading the image
        } catch (Exception e) {
            e.printStackTrace(); // Log any unexpected exceptions
        }
    }


    /**
     * Creates and initializes a drawing canvas.
     *
     * @return A Canvas object with a specified width and height, ready for drawing.
     */
    private Canvas createDrawingCanvas() {
        Canvas canvas = new Canvas(1000, 900);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        setupDrawing(gc);
        return canvas;
    }


    /**
     * Sets up the drawing environment for the given GraphicsContext.
     *
     * @param gc The GraphicsContext to be configured for drawing operations.
     */
    private void setupDrawing(GraphicsContext gc) {
        // Set the GraphicsContext to the provided gc parameter
        drawingGC = gc;  // Ensure drawingGC is assigned the provided gc

        // Apply color and line width from the color picker and slider
        drawingGC.setStroke(colorPicker.getValue());  // Apply color
        drawingGC.setLineWidth(lineWidthSlider.getValue());  // Apply line width

        // Create a WritableImage for the current canvas state
        WritableImage drawingLayer = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
        drawingCanvas.snapshot(null, drawingLayer);  // Snapshot the current canvas to the image

        // Draw the image onto the graphics context
        drawingGC.drawImage(drawingLayer, 0, 0);

        // Save the initial state for undo
        saveStateForUndo();
    }


    /**
     * Sets the active drawing tool and updates the corresponding event handlers
     * for mouse actions and UI components.
     *
     * @param tool The tool to be set as active for drawing operations.
     */
    private void setActiveTool(Tool tool) {
        currentTool = tool;

        // Assuming you have colorPicker and lineWidthSlider already initialized
        colorPicker.setOnAction(event -> {
            // Update the stroke color whenever a new color is selected
            drawingGC.setStroke(colorPicker.getValue());  // Apply selected color
            System.out.println("Selected Color: " + colorPicker.getValue());
        });

        lineWidthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the line width whenever the slider value changes
            drawingGC.setLineWidth(newValue.doubleValue());  // Apply selected line width
            System.out.println("Line Width: " + newValue.doubleValue());
        });

        // Clear existing mouse event handlers
        drawingCanvas.setOnMousePressed(null);
        drawingCanvas.setOnMouseDragged(null);
        drawingCanvas.setOnMouseReleased(null);

        // Switch case statements for drawing tools and shapes
        switch (currentTool) {
            case PENCIL:
                handlePencilTool();
                break;
            case LINE:
                handleLineTool();
                break;
            case ERASER:
                handleEraserTool();
                break;
            case RECTANGLE:
                handleShapeTool();
                break;
            case CIRCLE:
                handleShapeTool();
                break;
            case STAR:
                handleStarTool();
                break;
            case TRIANGLE:
                handleShapeTool();
                break;
            case POLYGON:
                handlePolygonTool();
                break;
            case TEXTBOX:
                handleTextTool();
                break;
        }
    }

    /**
     * Handles the drawing actions for the pencil tool on the canvas.
     * Sets up mouse event handlers for pencil strokes when the mouse is pressed,
     * dragged, or released.
     */
    private void handlePencilTool() {
        drawingCanvas.setOnMousePressed(e -> {
            drawingGC.beginPath(); // Use drawingGC for pencil strokes
            drawingGC.moveTo(e.getX(), e.getY());
            drawingGC.stroke();
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for text box border (if applicable)
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for text box border (if applicable)
            saveStateForUndo();
        });

        drawingCanvas.setOnMouseDragged(e -> {
            drawingGC.lineTo(e.getX(), e.getY());
            drawingGC.stroke();
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for text box border (if applicable)
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for text box border (if applicable)
            saveStateForUndo();
        });

        drawingCanvas.setOnMouseReleased(e -> drawingGC.closePath());
    }


    /**
     * Sets up the logic for the line drawing tool.
     * This method defines the mouse event handlers for drawing a line on the canvas.
     * It captures the starting point of the line when the mouse is pressed and
     * extends the line to the ending point when the mouse is released.
     */
    private void handleLineTool() {
        drawingCanvas.setOnMousePressed(e -> {
            drawingGC.beginPath();
            drawingGC.moveTo(e.getX(), e.getY());
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for the line
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for the line
            saveStateForUndo(); // Save the current state for undo functionality
        });

        drawingCanvas.setOnMouseReleased(e -> {
            drawingGC.lineTo(e.getX(), e.getY());
            drawingGC.stroke(); // Draw the line on the canvas
            drawingGC.closePath();
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for the line
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for the line
            saveStateForUndo(); // Save the current state for undo functionality
        });
    }

    /**
     * Sets up the logic for the eraser tool, including mouse event handlers
     * for erasing on the drawing canvas.
     */
    private void handleEraserTool() {
        // Sets the action for when the mouse is pressed on the drawing canvas
        drawingCanvas.setOnMousePressed(e -> {
            // Clear a rectangle area at the mouse position to simulate erasing
            drawingGC.clearRect(e.getX(), e.getY(), lineWidthSlider.getValue(), lineWidthSlider.getValue());
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for text box border (if applicable)
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for text box border (if applicable)
            saveStateForUndo(); // Save the current state for undo functionality
        });

        // Sets the action for when the mouse is dragged on the drawing canvas
        drawingCanvas.setOnMouseDragged(e -> {
            // Clear a rectangle area at the mouse position to simulate erasing
            drawingGC.clearRect(e.getX(), e.getY(), lineWidthSlider.getValue(), lineWidthSlider.getValue());
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for text box border (if applicable)
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for text box border (if applicable)
            saveStateForUndo(); // Save the current state for undo functionality
        });
    }

    /**
     * Handles mouse events for drawing shapes on the canvas.
     * Sets up event handlers for mouse pressed and released actions.
     */
    private void handleShapeTool() {
        drawingCanvas.setOnMousePressed(e -> {
            // Record the starting coordinates for the shape
            shapeStartX = e.getX();
            shapeStartY = e.getY();
            drawingShape = true;

            // Apply the selected color and line width for the shape
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for shape border
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for shape border
            saveStateForUndo(); // Save the current state for undo functionality
        });

        drawingCanvas.setOnMouseReleased(e -> {
            // Check if a shape is being drawn
            if (drawingShape) {
                double endX = e.getX(); // Get the ending x-coordinate
                double endY = e.getY(); // Get the ending y-coordinate
                drawShape(shapeStartX, shapeStartY, endX, endY); // Draw the shape using the start and end coordinates
                drawingShape = false; // Reset the drawing shape flag

                // Apply the selected color and line width for the shape
                drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for shape border
                drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for shape border
                saveStateForUndo(); // Save the current state for undo functionality
            }
        });
    }

    /**
     * Handles the drawing of a polygon shape on the canvas.
     * Sets mouse event handlers for when the user presses and releases the mouse button
     * to define the starting point and dimensions of the polygon.
     */
    private void handlePolygonTool() {
        drawingCanvas.setOnMousePressed(e -> {
            // Store the starting coordinates for the polygon
            shapeStartX = e.getX();
            shapeStartY = e.getY();
            drawingShape = true; // Indicate that a shape is being drawn
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for polygon border
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for polygon border
            saveStateForUndo(); // Save the current state for undo functionality
        });

        drawingCanvas.setOnMouseReleased(e -> {
            if (drawingShape) {
                // Get the ending coordinates for the polygon
                double endX = e.getX();
                double endY = e.getY();
                double radius = Math.sqrt(Math.pow(endX - shapeStartX, 2) + Math.pow(endY - shapeStartY, 2));
                drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for polygon border
                drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for polygon border

                // Prompt the user to enter the number of sides for the polygon
                TextInputDialog dialog = new TextInputDialog("5");
                dialog.setTitle("Polygon Sides");
                dialog.setHeaderText("Enter the number of sides for the polygon:");
                dialog.setContentText("Sides:");
                int sides = Integer.parseInt(dialog.showAndWait().orElse("5")); // Default to 5 sides if not specified

                // Draw the polygon with the specified number of sides
                drawPolygon(shapeStartX, shapeStartY, radius, sides);
                drawingShape = false; // Reset drawing shape flag
                saveStateForUndo(); // Save the current state for undo functionality
            }
        });
    }

    /**
     * Handles the drawing functionality for the star tool.
     * Sets up mouse event handlers to manage the drawing of a star shape on the canvas.
     */
    private void handleStarTool() {
        drawingCanvas.setOnMousePressed(e -> {
            // Record the starting point when the mouse is pressed
            shapeStartX = e.getX();
            shapeStartY = e.getY();
            drawingShape = true; // Set the drawing shape flag to true
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for the star's outline
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for the star's outline
            saveStateForUndo(); // Save the current state for undo functionality
        });

        drawingCanvas.setOnMouseReleased(e -> {
            if (drawingShape) {
                double endX = e.getX();
                double endY = e.getY();
                double radius = Math.sqrt(Math.pow(endX - shapeStartX, 2) + Math.pow(endY - shapeStartY, 2));

                // Prompt the user to enter the number of points for the star
                TextInputDialog dialog = new TextInputDialog("5");
                dialog.setTitle("Star Points");
                dialog.setHeaderText("Enter the number of points for the star:");
                dialog.setContentText("Points:");
                drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for the star's outline
                drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for the star's outline

                // Get the input and validate it
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        int sides = Integer.parseInt(result.get());
                        if (sides < 4) {
                            throw new NumberFormatException(); // Enforce minimum points
                        }
                        drawStar(shapeStartX, shapeStartY, radius, sides); // Draw the star with specified points
                    } catch (NumberFormatException ex) {
                        // Show an error message if input is invalid
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Invalid Input");
                        alert.setHeaderText("Number of Points Error");
                        alert.setContentText("Please enter a valid number of points (4 or more).");
                        alert.showAndWait();
                    }
                }
                drawingShape = false; // Reset the drawing flag
                saveStateForUndo(); // Save the current state for undo functionality
            }
        });
    }

    /**
     * Draws a shape on the canvas based on the specified starting and ending coordinates.
     *
     * @param startX The x-coordinate where the shape drawing starts.
     * @param startY The y-coordinate where the shape drawing starts.
     * @param endX   The x-coordinate where the shape drawing ends.
     * @param endY   The y-coordinate where the shape drawing ends.
     */
    private void drawShape(double startX, double startY, double endX, double endY) {
        if (dashedOutlineCheckBox.isSelected()) {
            drawingGC.setLineDashes(10); // Apply dashed line
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for text box border (if applicable)
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for text box border (if applicable)
            saveStateForUndo();
        } else {
            drawingGC.setLineDashes((double[]) null); // Use solid line
            drawingGC.setStroke(colorPicker.getValue()); // Apply selected color for text box border (if applicable)
            drawingGC.setLineWidth(lineWidthSlider.getValue()); // Set width for text box border (if applicable)
            saveStateForUndo();
        }

        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);

        switch (currentShapeType) {
            case RECTANGLE:
                drawingGC.strokeRect(x, y, width, height);
                break;
            case CIRCLE:
                double radius = Math.min(width, height) / 2;
                drawingGC.strokeOval(x + width / 2 - radius, y + height / 2 - radius, radius * 2, radius * 2);
                break;
            case STAR:
                int numPoints = (int) starPointSlider.getValue(); // Get the dynamic number of points from the slider
                drawStar(x, y, Math.min(50, 50) / 2, numPoints); // Example radius; adjust as needed
                break;
            case TRIANGLE:
                drawTriangle(x + width / 2, y + height / 2, Math.min(width, height) / 2); // Use the drawTriangle function
                break;
            default:
                break;
        }

        drawingGC.setLineDashes((double[]) null); // Reset after drawing
    }


    /**
     * Draws a star shape on the canvas with the specified parameters.
     *
     * @param centerX     The x-coordinate of the center of the star.
     * @param centerY     The y-coordinate of the center of the star.
     * @param outerRadius The outer radius of the star points.
     * @param numPoints   The total number of points on the star (must be 4 or greater).
     * @throws IllegalArgumentException if the number of points is less than 4.
     */
    private void drawStar(double centerX, double centerY, double outerRadius, int numPoints) {
        if (numPoints < 4) {
            throw new IllegalArgumentException("Number of points must be 4 or greater.");
        }

        double[] xPoints = new double[numPoints * 2];
        double[] yPoints = new double[numPoints * 2];

        for (int i = 0; i < numPoints * 2; i++) {
            double angle = Math.toRadians(i * 180.0 / numPoints); // Angle step depends on number of points
            double radius = (i % 2 == 0) ? outerRadius : outerRadius / 2; // Alternate between outer and inner radius
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY - radius * Math.sin(angle);
        }

        drawingGC.setStroke(Color.BLACK); // Set the stroke color
        drawingGC.strokePolygon(xPoints, yPoints, numPoints * 2); // Draw the star
    }


    /**
     * Draws an equilateral triangle on the canvas.
     *
     * @param centerX The x-coordinate of the triangle's center.
     * @param centerY The y-coordinate of the triangle's center.
     * @param radius  The radius, which determines the size of the triangle.
     */
    private void drawTriangle(double centerX, double centerY, double radius) {
        double[] xPoints = new double[3];
        double[] yPoints = new double[3];

        // We need 3 points for the triangle, equally spaced by 120 degrees
        for (int i = 0; i < 3; i++) {
            double angle = Math.toRadians(120 * i - 90); // -90 to start the first point at the top
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }

        drawingGC.strokePolygon(xPoints, yPoints, 3);
    }

    /**
     * Draws a regular polygon on the canvas using the specified parameters.
     *
     * @param centerX The x-coordinate of the center of the polygon.
     * @param centerY The y-coordinate of the center of the polygon.
     * @param radius  The radius from the center to each vertex of the polygon.
     * @param sides   The number of sides (vertices) the polygon should have.
     *                Must be at least 3.
     * @throws IllegalArgumentException If the number of sides is less than 3.
     */
    private void drawPolygon(double centerX, double centerY, double radius, int sides) {
        if (sides < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 sides.");
        }

        double[] xPoints = new double[sides];
        double[] yPoints = new double[sides];

        // Calculate the angle between each vertex in the polygon
        double angleStep = 2 * Math.PI / sides;

        // Calculate the position of each vertex
        for (int i = 0; i < sides; i++) {
            double angle = i * angleStep;
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }

        drawingGC.strokePolygon(xPoints, yPoints, sides);
    }


    /**
     * Creates a menu bar for the application, containing options for file operations,
     * editing actions, and help information.
     *
     * @param stage The primary stage for the application, used to open dialog windows.
     * @return A MenuBar containing the defined menus and menu items.
     */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save/Save As");
        MenuItem exitItem = new MenuItem("Exit");

        newItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to create a new canvas?");
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                createNewCanvas();
            }
        });

        openItem.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                loadImage(file);
            }
        });

        // Connect the saveItem to the saveImage method
        saveItem.setOnAction(e -> saveImage(stage)); // Call saveImage on saveItem action
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        MenuItem redoItem = new MenuItem("Redo");

        undoItem.setOnAction(e -> undo());
        redoItem.setOnAction(e -> redo());

        editMenu.getItems().addAll(undoItem, redoItem);

        Menu helpMenu = new Menu("Help");
        MenuItem helpItem = new MenuItem("Help");
        MenuItem aboutItem = new MenuItem("About");

        helpItem.setOnAction(e -> showHelpDialog());
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(helpItem, aboutItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
        return menuBar;
    }

    // Save image method
    private void saveImage(Stage stage) {
        // Create a FileChooser to select the save location and file type
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        // Set extension filters for the file types
        FileChooser.ExtensionFilter extFilterPng = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilterJpg = new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg");
        fileChooser.getExtensionFilters().addAll(extFilterPng, extFilterJpg);

        // Show save dialog and get the file
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            // Check for correct extension
            String filePath = file.getPath();
            if (!filePath.endsWith(".png") && !filePath.endsWith(".jpg")) {
                file = new File(filePath + ".png"); // Default to PNG if no extension
            }

            // Capture the canvas as an image
            WritableImage image = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
            drawingCanvas.snapshot(new SnapshotParameters(), image);

            // Convert WritableImage to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(
                    (int) drawingCanvas.getWidth(),
                    (int) drawingCanvas.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );

            // Retrieve the PixelReader from the WritableImage
            PixelReader pixelReader = image.getPixelReader();

            // Loop through each pixel to retrieve its color
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    // Get the pixel color from the WritableImage using PixelReader
                    Color color = pixelReader.getColor(x, y);

                    // Convert Color to ARGB int for BufferedImage
                    int alpha = (int) (color.getOpacity() * 255); // Get alpha as int
                    int red = (int) (color.getRed() * 255); // Get red as int
                    int green = (int) (color.getGreen() * 255); // Get green as int
                    int blue = (int) (color.getBlue() * 255); // Get blue as int

                    // Set the pixel color in BufferedImage
                    int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue; // Combine into ARGB int
                    bufferedImage.setRGB(x, y, rgb);
                }
            }

            // Save the image to the specified file
            try {
                if (filePath.endsWith(".png")) {
                    ImageIO.write(bufferedImage, "png", file);
                } else if (filePath.endsWith(".jpg")) {
                    ImageIO.write(bufferedImage, "jpg", file);
                }
                System.out.println("Image saved successfully: " + file.getPath());
            } catch (IOException e) {
                System.out.println("Error saving image: " + e.getMessage());
            }
        }
    }


    /**
     * Displays a help dialog with instructions or guidance for the user.
     * The dialog presents information in an informational alert format.
     */
    private void showHelpDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.setContentText("This is the Help section.\nHere you can provide instructions or guidance.");
        alert.showAndWait();
    }

    /**
     * Displays an about dialog with information about the application.
     * The dialog presents the version and developer information of the application
     * in an informational alert format.
     */
    private void showAboutDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText("JavaDraw v1.1.2\nDeveloped by Aidan Vazquez.\nA simple drawing application.");
        alert.showAndWait();
    }

    /**
     * Saves the current state of the drawing canvas to the undo stack.
     * This allows for the ability to revert to this state if needed.
     */
    private void saveStateForUndo() {
        // Save current state to the undo stack
        WritableImage currentSnapshot = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
        drawingCanvas.snapshot(null, currentSnapshot);
        undoStack.push(currentSnapshot);
    }

    /**
     * Undoes the last action by restoring the previous state of the drawing canvas.
     * The current state is saved to the redo stack before the undo action is performed.
     */
    private void undo() {
        if (!undoStack.isEmpty()) {
            // Save the current state to the redo stack before undoing
            WritableImage currentSnapshot = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
            drawingCanvas.snapshot(null, currentSnapshot);
            redoStack.push(currentSnapshot);

            // Restore the last state from the undo stack
            WritableImage lastImage = undoStack.pop();
            drawingGC.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
            drawingGC.drawImage(lastImage, 0, 0);
        }
    }

    /**
     * Redoes the last undone action by restoring the next state of the drawing canvas.
     * The current state is saved to the undo stack before the redo action is performed.
     */
    private void redo() {
        if (!redoStack.isEmpty()) {
            // Save the current state to the undo stack before redoing
            WritableImage currentSnapshot = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
            drawingCanvas.snapshot(null, currentSnapshot);
            undoStack.push(currentSnapshot);

            // Restore the last state from the redo stack
            WritableImage nextImage = redoStack.pop();
            drawingGC.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
            drawingGC.drawImage(nextImage, 0, 0);
        }
    }

    /**
     * Redraws the canvas by clearing the previous content and rendering the current image
     * along with any drawn shapes or lines.
     */
    private void redrawCanvas() {
        // Clear the image canvas to prepare for redrawing
        imageGC.clearRect(0, 0, imageCanvas.getWidth(), imageCanvas.getHeight());

        // Draw the current image if it exists
        if (currentImage != null) {
            imageGC.drawImage(currentImage, imageX, imageY, imageWidth, imageHeight);
        }

        // Clear the drawing canvas to prepare for new drawings
        drawingGC.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }

    /**
     * Creates a new drawing canvas and clears the existing one.
     * <p>
     * This function clears the current drawing canvas and the image canvas,
     * creates new canvases with specified dimensions, and updates the graphics contexts.
     * Finally, it clears the canvas pane and adds the new canvases for rendering.
     * </p>
     */
    private void createNewCanvas() {
        // Clear the existing drawing canvas
        drawingGC.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        // Optionally clear the image canvas too
        imageGC.clearRect(0, 0, imageCanvas.getWidth(), imageCanvas.getHeight());

        // Create new canvases (if necessary, you can set different sizes or reset states)
        drawingCanvas = new Canvas(imageWidth, imageHeight);
        imageCanvas = new Canvas(imageWidth, imageHeight);

        // Update the graphics contexts
        drawingGC = drawingCanvas.getGraphicsContext2D();
        imageGC = imageCanvas.getGraphicsContext2D();

        // Clear the canvas pane and add the new canvases
        canvasPane.getChildren().clear();
        canvasPane.getChildren().addAll(imageCanvas, drawingCanvas);
    }


    /**
     * Clears all mouse event handlers from the drawing canvas.
     * This method is useful for resetting event handling
     * when switching tools or when it's necessary
     * to stop responding to mouse events.
     */
    private void clearEventHandlers() {
        Canvas mainCanvas = drawingCanvas;

        // Remove all mouse event handlers
        mainCanvas.setOnMousePressed(null);
        mainCanvas.setOnMouseDragged(null);
        mainCanvas.setOnMouseReleased(null);
    }


    /**
     * Handles the text drawing tool, allowing the user to input text and place it on the drawing canvas.
     * This method prompts the user for text input and sets up mouse event handlers to draw the text at
     * the specified location on the canvas.
     */
    protected void handleTextTool() {
        // Clear any existing event handlers
        clearEventHandlers();
        Canvas mainCanvas = drawingCanvas;

        // Prompt user for text input
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input Text");
        dialog.setHeaderText("Enter the text to draw:");
        dialog.setContentText("Text:");

        // Show the dialog and wait for user input
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            // Clear any previous event handlers to avoid conflicts
            clearEventHandlers();

            // Set mouse event to handle text drawing on canvas
            drawingCanvas.setOnMousePressed(e -> {
                // Get the GraphicsContext of the canvas
                GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

                // Coordinates where user clicks to place the text
                double startX = e.getX();
                double startY = e.getY();

                // Draw the text at the specified location
                gc.strokeText(text, startX, startY);
            });
        });
    }

    /**
     * The main method to launch the JavaFX application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
