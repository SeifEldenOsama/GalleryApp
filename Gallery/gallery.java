package Gallery;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import DataBase.dataBaseManagement;

public class gallery extends Application {

    private final int COLUMNS = 3;
    private final double GAP = 25;
    private final double PADDING = 20;
    private double IMAGE_WIDTH;
    private GridPane gridPane;

    private final Screen screen = Screen.getPrimary();
    private final Rectangle2D bounds = screen.getBounds();
    private final double width = bounds.getWidth(), height = bounds.getHeight();
    private ImageView upload;

    @Override
    public void start(Stage stage) {
        IMAGE_WIDTH = (width - (COLUMNS + 1) * GAP - 2 * PADDING) / COLUMNS;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);");

        gridPane = new GridPane();
        gridPane.setPadding(new Insets(PADDING));
        gridPane.setHgap(GAP);
        gridPane.setVgap(GAP);
        gridPane.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(PADDING));
        scrollPane.setStyle("-fx-background-color: transparent;");

        upload = new ImageView(new Image(getClass().getResourceAsStream("/images/uploading.png")));

        Button uploadButton = new Button("Upload Image");
        uploadButton.setStyle(
            "-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 16px; " +
            "-fx-padding: 10 20 10 20; -fx-background-radius: 10px;"
        );
        uploadButton.setOnMouseEntered(e -> uploadButton.setStyle(
            "-fx-background-color: #357ABD; -fx-text-fill: white; -fx-font-size: 16px; " +
            "-fx-padding: 10 20 10 20; -fx-background-radius: 10px;"
        ));
        uploadButton.setOnMouseExited(e -> uploadButton.setStyle(
            "-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 16px; " +
            "-fx-padding: 10 20 10 20; -fx-background-radius: 10px;"
        ));
        uploadButton.setOnAction(e -> openFileChooser(stage));
        uploadButton.setGraphic(upload);
        uploadButton.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(uploadButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 15, 0));

        root.setTop(buttonBox);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, width, height - 70);
        stage.setTitle("Gallery");
        stage.setScene(scene);
        stage.show();

        loadImagesFromDatabase();
    }

    private void openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                String imageName = file.getName();
                dataBaseManagement.insertImage(file.getAbsolutePath(), imageName);
            }
            gridPane.getChildren().clear();
            loadImagesFromDatabase();
        }
    }

    private void loadImagesFromDatabase() {
        try (ResultSet rs = dataBaseManagement.getAllImages()) {
            int index = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                InputStream is = rs.getBinaryStream("image_data");
                Image img = new Image(is);

                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(IMAGE_WIDTH);
                imageView.setFitHeight(IMAGE_WIDTH);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);

                imageView.setOnMouseClicked(e -> showImageInNewStage(img));

                Button deleteButton = new Button("×");
                deleteButton.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.8); -fx-text-fill: red; -fx-font-weight: bold;" +
                    "-fx-border-color: transparent; -fx-background-radius: 50%; -fx-font-size: 14px;" +
                    "-fx-cursor: hand;"
                );
                deleteButton.setPrefSize(24, 24);
                deleteButton.setOnAction(e -> {
                    dataBaseManagement.deleteImageById(id);
                    gridPane.getChildren().clear();
                    loadImagesFromDatabase();
                });

                StackPane content = new StackPane(imageView);
                StackPane.setAlignment(deleteButton, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(deleteButton, new Insets(0, 5, 5, 0));
                content.getChildren().add(deleteButton);
                content.setMinSize(IMAGE_WIDTH, IMAGE_WIDTH);
                content.setMaxSize(IMAGE_WIDTH, IMAGE_WIDTH);
                content.setStyle("-fx-background-color: white; -fx-border-color: #ccc; " +
                        "-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);");

                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), content);
                scaleIn.setToX(1.05);
                scaleIn.setToY(1.05);

                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), content);
                scaleOut.setToX(1.0);
                scaleOut.setToY(1.0);

                content.setOnMouseEntered(e -> scaleIn.playFromStart());
                content.setOnMouseExited(e -> scaleOut.playFromStart());

                int col = index % COLUMNS;
                int row = index / COLUMNS;
                gridPane.add(content, col, row);
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showImageInNewStage(Image img) {
        Stage popup = new Stage(StageStyle.TRANSPARENT);
        popup.initModality(Modality.APPLICATION_MODAL);

        ImageView enlarged = new ImageView(img);
        enlarged.setPreserveRatio(true);
        enlarged.setFitWidth(width * 0.8);
        enlarged.setFitHeight(height * 0.8);
        enlarged.setSmooth(true);

        StackPane container = new StackPane(enlarged);
        container.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        container.setAlignment(Pos.CENTER);
        container.setOnMouseClicked(e -> popup.close());

        Scene popupScene = new Scene(container, width, height);
        popupScene.setFill(null);
        popup.setScene(popupScene);
        popup.show();
    }
}