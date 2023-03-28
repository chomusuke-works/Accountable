package com.chomusuke.gui.stage;

import com.chomusuke.logic.Storage;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;

public class AddFileScreen {

    private static final int PADDING = 8;

    public static void show() {
        Stage stage = new Stage();
        stage.setResizable(false);
        VBox root = new VBox();
        root.getStyleClass().add("background");
        root.setPadding(new Insets(PADDING));
        root.setSpacing(PADDING);
        stage.setScene(new Scene(root));

        HBox inputs = new HBox();
        inputs.setSpacing(PADDING);

        TextField year = new TextField();
        TextField month = new TextField();
        inputs.getChildren().addAll(year, month);
        inputs.getChildren().forEach(n ->
                ((TextField) n).setTextFormatter(new TextFormatter<>(new IntegerStringConverter()))
        );
        month.textProperty().addListener(t -> {
            StringProperty monthText = (StringProperty) t;
            int v;
            try {
                v = Integer.parseInt(monthText.getValue());

                if (v < 0)
                    monthText.setValue(Integer.toString(1));
                else if (v > 12) {
                    monthText.setValue(Integer.toString(12));
                }
            } catch (NumberFormatException ignored) {
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        Button submit = new Button("Ajouter");
        submit.setOnAction(a -> {
            if (year.getText().equals("")
                    || month.getText().equals("")) {
                return;
            }

            int yearValue = Integer.parseInt(year.getText());
            int monthValue = Integer.parseInt(month.getText());

            if (Integer.parseInt(year.getText()) < 1
                    || Integer.parseInt(month.getText()) > 12) {
                return;
            }

            Storage.write(new ArrayList<>(), yearValue, monthValue);

            stage.close();
        });
        submit.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(submit, Priority.ALWAYS);

        root.getChildren().addAll(inputs, submit);

        stage.show();
    }
}
