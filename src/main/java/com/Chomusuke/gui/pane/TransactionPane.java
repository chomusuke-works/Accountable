/*  Accountable: a personal spending monitoring program
    Copyright (C) 2023  Artur Yukhanov

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.chomusuke.gui.pane;

import com.chomusuke.Accountable.SceneID;
import com.chomusuke.gui.element.DateSelector;
import com.chomusuke.gui.element.PlusButton;
import com.chomusuke.gui.element.SquareButton;
import com.chomusuke.gui.element.tile.TransactionTile;
import com.chomusuke.gui.popup.AddFileScreen;
import com.chomusuke.gui.popup.AddTransactionScreen;
import com.chomusuke.logic.Account;
import com.chomusuke.logic.TransactionList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.*;

public class TransactionPane extends BorderPane{

    private static final float REMAINDER_COLOR_THRESHOLD = 0.1f;
    private static final int PADDING = 8;

    private final VBox transactionPane;
    private final PlusButton addTransaction;

    public TransactionPane(ObjectProperty<SceneID> selectedScene, TransactionList txs, Map<Byte, Account> balances, StringProperty year, StringProperty month) {

        // Main
        {
            getStyleClass().add("background");
            setPadding(new Insets(PADDING, PADDING, 0, PADDING));
        }



        // ----- TOP -----
        VBox top = new VBox();

        // Title of the app
        Text title = new Text("Accountable.");
        HBox titleContainer = new HBox(title);

        // Date selector and related
        HBox controls = new HBox();

        SquareButton showAccounts = new SquareButton("wallet.png", a -> selectedScene.set(SceneID.ACCOUNTS));
        SquareButton newFile = new SquareButton("new.png", a -> new AddFileScreen().show());
        DateSelector dateSelector = new DateSelector();
        Text loadedDate = new Text();

        controls.getChildren().addAll(showAccounts, newFile, dateSelector, loadedDate);

        Text remainder = new Text();
        HBox remainderContainer = new HBox(remainder);

        top.getChildren().addAll(titleContainer, controls, remainderContainer);
        setTop(top);

        // Top
        {
            top.setSpacing(PADDING);
            top.setPadding(new Insets(0, 0, PADDING, 0));

            title.setId("title");
            title.getStyleClass().add("stdText");
            titleContainer.setAlignment(Pos.CENTER);

            loadedDate.getStyleClass().add("stdText");
            HBox.setMargin(loadedDate, new Insets(0, 0, 0, PADDING));

            year.bind(dateSelector.getYearProperty());
            month.bind(dateSelector.getMonthProperty());

            remainder.setStyle("-fx-font: 18 'Arial Rounded MT Bold'");
            remainderContainer.setAlignment(Pos.BASELINE_LEFT);
        }



        // ----- CONTENT -----
        Pane content = new Pane();

        transactionPane = new VBox();
        ScrollPane scrollPane = new ScrollPane(transactionPane);
        Text emptyText = new Text("Please select a date.");
        transactionPane.getChildren().add(emptyText);

        // "Add transaction" button
        addTransaction = new PlusButton();

        content.getChildren().addAll(scrollPane, addTransaction);
        setCenter(content);

        // Content
        {
            content.setPadding(new Insets(PADDING));

            transactionPane.getStyleClass().add("background");
            transactionPane.setSpacing(PADDING);
            transactionPane.setPadding(new Insets(0, 0, PADDING, 0));
            transactionPane.prefWidthProperty().bind(content.widthProperty());
            transactionPane.prefHeightProperty().bind(content.heightProperty());
            transactionPane.setAlignment(Pos.TOP_CENTER);

            emptyText.getStyleClass().add("stdText");

            scrollPane.getStyleClass().add("scrollPane");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            addTransaction.layoutXProperty().bind(content.widthProperty().subtract(PlusButton.RADIUS*2+PADDING));
            addTransaction.layoutYProperty().bind(content.heightProperty().subtract(PlusButton.RADIUS*2+PADDING*2));
            addTransaction.setVisible(false);
        }



        // ----- EVENTS -----
        {
            // Transaction addition (Big Fat + Button)
            addTransaction.setOnMouseClicked((e) -> new AddTransactionScreen(txs, balances).show());

            // Colors for remainder
            remainder.textProperty().addListener((v, o, n) -> {
                if (Float.parseFloat(n) == 0)
                    remainder.setFill(Color.BLUE);
                else if (Float.parseFloat(n) < 0)
                    remainder.setFill(Color.RED);
                else if (Float.parseFloat(n) < txs.getTotalRevenue() * REMAINDER_COLOR_THRESHOLD)
                    remainder.setFill(Color.ORANGE);
                else
                    remainder.setFill(Color.GREEN);
            });

            dateSelector.getMonthProperty().addListener(e -> {
                loadedDate.setText(String.format("%s/%s", year.get(), month.get()));
                remainder.setText(String.format(Locale.ROOT, "%.2f", txs.getRemainder()));
            });
        }
    }

    public void update(TransactionList txs, Map<Byte, Account> balances) {

        // Tiles generation
        List<TransactionTile> tiles = new ArrayList<>();
        for (int i = 0; i < txs.getTransactionList().size(); i++) {
            TransactionTile tile = new TransactionTile(txs.getTransactionList().get(i), txs.getValues()[i]);
            // Event handler
            tile.setOnMouseClicked(m -> {
                if (m.getButton() == MouseButton.PRIMARY) {
                    new AddTransactionScreen(txs, tile.getBaseTransaction(), balances).show();
                }
            });

            tiles.add(tile);
        }

        addTransaction.setVisible(true);
        transactionPane.getChildren().setAll(tiles);
    }
}
