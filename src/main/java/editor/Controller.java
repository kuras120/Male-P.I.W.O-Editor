package editor;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.*;

import java.net.URL;
import java.text.ParseException;
import java.util.ResourceBundle;
import java.util.zip.*;

public class Controller implements Initializable {
    Data data = new Data();
    int numCols = 32;
    int numRows = 16;
    Pane[][] paneMatrix = new Pane[numCols][numRows];
    int selectedFrame = 0;

    @FXML
    private GridPane mainGrid;
    @FXML
    TextField R_color_field;
    @FXML
    TextField G_color_field;
    @FXML
    TextField B_color_field;
    @FXML
    Pane sample_color_field;
    @FXML
    Button clrFrameBtn;
    @FXML
    TextField frameCounter;

    public String getParsableString(String string) {
        try{
            Integer.parseInt(string);
            return string;
        } catch(Exception e){
            return "255";
        }
    }

    public String getColorString() {
        int r = Integer.parseInt(getParsableString(R_color_field.getText()));
        int g = Integer.parseInt(getParsableString(G_color_field.getText()));
        int b = Integer.parseInt(getParsableString(B_color_field.getText()));
        if(r<0) r = 0;
        if(r>255) r = 255;
        if(g<0) g = 0;
        if(g>255) g = 255;
        if(b<0) b = 0;
        if(b>255) b = 255;
        return '#' + String.format("%02X", r) + String.format("%02X", g) + String.format("%02X", b);
    }

    public String getColorStringFromFrame(Frame frame, int i, int j) {
        int r = frame.matrix[i][j][0];
        int g = frame.matrix[i][j][1];
        int b = frame.matrix[i][j][2];
        return '#' + String.format("%02X", r) + String.format("%02X", g) + String.format("%02X", b);
    }

    @FXML
    public void onContentChange() {
        sample_color_field.setStyle("-fx-background-color: " + getColorString() + "; -fx-border-color: black;");
    }

    @FXML //Action to connect with Raspberry
    public void onButtonRspClicked() {
        System.out.printf("Connected");
    }

    @FXML  //action to turn on
    public void onButtonTurnOnClicked() {
        System.out.printf("Turned on");
    }

    @FXML  //action to turn off
    public void onButtonTurnOffClicked() {
        System.out.printf("Turned off");
    }

    @FXML //connecting with speakers
    public void onButtonSpkClicked() {
        System.out.printf("Connected");
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    @FXML //loading animation
    public void onButtonLoadClicked() throws IOException {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            System.out.printf("File has been loaded: " + selectedFile.getName());
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(selectedFile));
            ZipEntry zipEntry = zis.getNextEntry();
            File destDir = new File(selectedFile.getParent());
            while (zipEntry != null) {
                System.out.println(destDir);
                File newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

        } else {
            System.out.printf("Invalid file");
        }
    }

    public void loadFrame(int index) {
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                paneMatrix[i][j].setStyle("-fx-background-color: " + getColorStringFromFrame(data.frames.get(index), i, j) + "; -fx-border-color: black;");
            }
        }
    }

    @FXML
    public void newFrame() {
        data.frames.add(selectedFrame + 1, new Frame());
        loadFrame(++selectedFrame);
        frameCounter.setText(String.valueOf(selectedFrame + 1) + '/' + String.valueOf(data.frames.size()));
    }

    @FXML
    public void deleteFrame() {
        if (data.frames.size() > 1) {
            data.frames.remove(selectedFrame);
            if (selectedFrame == data.frames.size())
                selectedFrame--;
            loadFrame(selectedFrame);
            frameCounter.setText(String.valueOf(selectedFrame + 1) + '/' + String.valueOf(data.frames.size()));
        } else {
            clearFrame();
        }
    }

    @FXML
    public void previousFrame() {
        if (selectedFrame != 0) {
            selectedFrame--;
            loadFrame(selectedFrame);
            frameCounter.setText(String.valueOf(selectedFrame + 1) + '/' + String.valueOf(data.frames.size()));
        }
    }

    @FXML
    public void nextFrame() {
        if (selectedFrame != data.frames.size() - 1) {
            selectedFrame++;
            loadFrame(selectedFrame);
            frameCounter.setText(String.valueOf(selectedFrame + 1) + '/' + String.valueOf(data.frames.size()));
        }
    }

    @FXML //saving animation
    public void onButtonSaveClicked() {

    }

    @FXML
    public void clearFrame() {
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                data.frames.get(selectedFrame).matrix[i][j][0] = 255;
                data.frames.get(selectedFrame).matrix[i][j][1] = 255;
                data.frames.get(selectedFrame).matrix[i][j][2] = 255;
                paneMatrix[i][j].setStyle("-fx-background-color: white; -fx-border-color: black;");
            }
        }
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.SOMETIMES);
            mainGrid.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i < numRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.SOMETIMES);
            mainGrid.getRowConstraints().add(rowConstraints);
        }

        data.frames.add(new Frame());
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                paneMatrix[i][j] = addPane(i, j);
            }
        }

        System.out.println("Initialized");
    }

    private Pane addPane(int colIndex, int rowIndex) {
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: white; -fx-border-color: black;");
        pane.setOnMousePressed(e -> {
            pane.setStyle("-fx-background-color: blue; -fx-border-color: black;");
            System.out.printf("Mouse enetered cell [%d, %d]%n", colIndex, rowIndex);
        });
        pane.setOnMouseReleased(e -> {
            pane.setStyle("-fx-background-color: " + getColorString() + "; -fx-border-color: black;");
            int r, g, b;
            r =Integer.parseInt(getParsableString(R_color_field.getText()));
            g =Integer.parseInt(getParsableString(G_color_field.getText()));
            b =Integer.parseInt(getParsableString(B_color_field.getText()));
            if(r<0) r = 0;
            if(r>255) r = 255;
            if(g<0) g = 0;
            if(g>255) g = 255;
            if(b<0) b = 0;
            if(b>255) b = 255;
            data.frames.get(selectedFrame).matrix[colIndex][rowIndex][0] = r;
            data.frames.get(selectedFrame).matrix[colIndex][rowIndex][1] = g;
            data.frames.get(selectedFrame).matrix[colIndex][rowIndex][2] = b;
        });
        mainGrid.add(pane, colIndex, rowIndex);
        return pane;
    }
}
