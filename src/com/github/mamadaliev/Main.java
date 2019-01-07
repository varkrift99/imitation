package com.github.mamadaliev;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.github.mamadaliev.utils.Options;

public class Main extends Application implements Options {
    private GraphicsContext context;
    private Scene scene;
    private Scene diagrams;
    private static long nextSecond = System.currentTimeMillis() + 1000;
    private static int framesInLastSecond = 0;
    private static int framesInCurrentSecond;
    private int grid_width = 10;
    private int grid_height = 10;
    private int[][] grids = new int[ARRAY_WIDTH][ARRAY_HEIGHT];
    private int[][] future = new int[ARRAY_WIDTH][ARRAY_HEIGHT];
    private int neighbours;
    private int bad_neighbours;
    private int population = 0;
    private ObservableList<XYChart.Data> data1;
    private ObservableList<XYChart.Data> data2;

    /**
     * The main method.
     * @param args Arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Imitation");
        Group root = new Group();
        Canvas canvas = new Canvas(ARRAY_WIDTH * grid_width, ARRAY_HEIGHT * grid_height);
        root.getChildren().add(canvas);
        context = canvas.getGraphicsContext2D();
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        data1 = FXCollections.observableArrayList();
        data2 = FXCollections.observableArrayList();
        stage.setScene(scene);

        // initArray();

        // generate
        generate();

        // showArray();

        keyEvents();

        // update
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (population == 50) {
                    showDiagrams(stage);
                    stop();
                }
                clear();
                algorithm();
                drawGrids();
                showFPS();
                population++;
                data1.add(new XYChart.Data(population, countOfTypesInArray(1)));
                data2.add(new XYChart.Data(population, countOfTypesInArray(2)));
                delay();
            }
        }.start();

        stage.show();
    }

    /**
     * Key event listeners.
     */
    private void keyEvents() {
        scene.setOnKeyPressed(event -> {
            System.out.println(event.getText());
            if (event.getText().equals("=")) {
                if (grid_width < 15) {
                    grid_width++;
                    grid_height++;
                    System.out.println("+: " + grid_width + " : "  + grid_height);
                }
            }
            if (event.getText().equals("-")) {
                if (grid_width > 1) {
                    grid_width--;
                    grid_height--;
                    System.out.println("-: " + grid_width + " : "  + grid_height);
                }
            }
        });
    }

    /**
     * The algorithm.
     */
    private void algorithm() {
        neighbours = 0;
        bad_neighbours = 0;
        future = new int[ARRAY_WIDTH][ARRAY_HEIGHT];

        for (int i = 1; i < ARRAY_WIDTH-1; i++) {
            for (int j = 1; j < ARRAY_HEIGHT-1; j++) {
                neighbours = 0;
                bad_neighbours = 0;
                for (int l=-1; l<=1; l++)
                    for (int m=-1; m<=1; m++) {
                        if (grids[i + l][j + m] != 0) neighbours += 1;
                        if (grids[i + l][j + m] == 2) bad_neighbours += 1;
                    }
                if (grids[i][j] != 0) neighbours -= 1;

                // Rules of neighbours
                if (grids[i][j] == 1 && neighbours < 3) future[i][j] = 0;
                else if (grids[i][j] == 1 && neighbours > 3) future[i][j] = 0;
                else if(grids[i][j] == 0 && neighbours == 3) future[i][j] = 1;
                else future[i][j] = grids[i][j];

                // Rules of bad neighbours
                if (grids[i][j] == 2 && bad_neighbours > 2) future[i][j] = 0;
                else if (grids[i][j] == 1 && bad_neighbours > 0) future[i][j] = 2;
            }
        }
        grids = future;
    }

    /**
     * Delay update to get the required FPS.
     */
    private void delay() {
        try {
            int millis = 200;
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear grids.
     */
    private void clear() {
        context.clearRect(0, 0, ARRAY_WIDTH * 10, ARRAY_HEIGHT * 10);
    }

    /**
     * Initialization an array.
     */
    private void initArray() {
        for (int i=0; i<grid_width; i++) {
            for (int j=0; j<grid_height; j++) {
                grids[i][j] = 0;
            }
        }
    }

    /**
     * Output the array to display.
     */
    private void showArray() {
        for (int i=0; i<ARRAY_WIDTH; i++) {
            for (int j=0; j<ARRAY_HEIGHT; j++) {
                System.out.print(grids[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Drawing grids on the window.
     */
    private void drawGrids() {
        context.setFill(Color.CADETBLUE);
        for (int i=0; i<ARRAY_WIDTH; i++) {
            for (int j=0; j<ARRAY_HEIGHT; j++) {
                if (grids[i][j] == 1) {
                    context.setFill(Color.LIGHTSLATEGRAY);
                    context.fillRect(i * grid_width, j * grid_height, grid_width, grid_height);
                } else if (grids[i][j] == 2) {
                    context.setFill(Color.LIGHTGRAY);
                    context.fillRect(i * grid_width, j * grid_height, grid_width, grid_height);
                }
            }
        }
    }

    /**
     * Fill the array randomly.
     */
    private void generate() {
        int x;
        int y;

        // generate people
        for (int i=0; i<25000; i++) {
            x = ((int) (Math.random() * ARRAY_WIDTH));
            y = ((int) (Math.random() * ARRAY_HEIGHT));
            if (grids[x][y] == 0) {
                grids[x][y] = 1;
            } else --i;
        }

        // generate virus
        for (int i=0; i<2500; i++) {
            x = ((int) (Math.random() * ARRAY_WIDTH));
            y = ((int) (Math.random() * ARRAY_HEIGHT));
            if (grids[x][y] == 1) {
                grids[x][y] = 2;
            } else --i;
        }
    }

    /**
     * Show FPS in the window.
     */
    private void showFPS() {
        if (System.currentTimeMillis() > nextSecond) {
            nextSecond += 1000;
            framesInLastSecond = framesInCurrentSecond;
            framesInCurrentSecond = 0;
        }
        framesInCurrentSecond++;
        context.setFill(Color.BLACK);
        context.fillText("FPS: " + framesInLastSecond, 10, 20, 200);
        context.fillText("Array: " + ARRAY_WIDTH + "x" + ARRAY_HEIGHT, 10, 40, 200);
        context.fillText("Population: " + Integer.toString(population), 10, 60, 200);
    }

    /**
     * Count of types in array.
     */
    private int countOfTypesInArray(int type) {
        int count = 0;
        for (int i = 0; i < ARRAY_WIDTH; i++) {
            for (int j = 0; j < ARRAY_HEIGHT; j++) {
                if (grids[i][j] == type) count++;
            }
        }
        return count;
    }

    /**
     * Show diagrams.
     */
    private void showDiagrams(Stage stage) {
        NumberAxis x = new NumberAxis();
        NumberAxis y = new NumberAxis();
        LineChart<Number, Number> numberLineChart = new LineChart<Number, Number>(x, y);
        numberLineChart.setTitle("Population");
        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();
        series1.setName("Virus A");
        series2.setName("Virus B");
        series1.setData(data1);
        series2.setData(data2);
        diagrams = new Scene(numberLineChart, WINDOW_WIDTH, WINDOW_WIDTH >> 1);
        numberLineChart.getData().add(series1);
        numberLineChart.getData().add(series2);
        stage.setScene(diagrams);
    }
}