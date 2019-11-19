package editor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.scene.control.ToggleButton;
import java.io.File;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private GridPane mainGrid;



    @FXML //Action to connect with Raspberry
    public void onButtonRspClicked()
    {
        System.out.printf("Connected");
    }
    @FXML  //action to turn on
    public void onButtonTurnOnClicked()
    {
        System.out.printf("Turned on");
    }
    @FXML  //action to turn off
    public  void onButtonTurnOffClicked()
    {
        System.out.printf("Turned off");
    }
    @FXML //connecting with speakers
    public void onButtonSpkClicked()
    {
        System.out.printf("Connected");
    }
    @FXML //loading animation
    public void onButtonLoadClicked()
    {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);

        if(selectedFile != null){
            System.out.printf("File has been loaded: " + selectedFile.getName());
        }else
        {
            System.out.printf("Invalid file");
        }
    }





    public void initialize(URL url, ResourceBundle resourceBundle) {
        int numCols = 5 ;
        int numRows = 5 ;



        for (int i = 0 ; i < numCols ; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.SOMETIMES);
            mainGrid.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0 ; i < numRows ; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.SOMETIMES);
            mainGrid.getRowConstraints().add(rowConstraints);
        }

        for (int i = 0 ; i < numCols ; i++) {
            for (int j = 0; j < numRows; j++) {
                addPane(i, j);
            }
        }
        System.out.println("Initialized");
    }

    private void addPane(int colIndex, int rowIndex) {
        Pane pane = new Pane();
        pane.setOnMousePressed(e -> {
            pane.setStyle("-fx-background-color: blue; -fx-border-color: black;");
            System.out.printf("Mouse enetered cell [%d, %d]%n", colIndex, rowIndex);
        });
        pane.setOnMouseReleased(e -> {
            pane.setStyle("-fx-background-color: white; -fx-border-color: black;");
        });
        mainGrid.add(pane, colIndex, rowIndex);
    }
}
