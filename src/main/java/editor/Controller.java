package editor;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


import java.io.*;

import java.net.URL;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.zip.*;

public class Controller implements Initializable {
    Data data = new Data();
    int numCols = 32;
    int numRows = 16;
    Pane[][] paneMatrix = new Pane[numCols][numRows];
    int selectedFrame = 0;
    String dirName;

    @FXML
    private GridPane mainGrid;
    @FXML
    TextField animName;
    @FXML
    TextField musicTitle;
    @FXML
    TextField frameDuration;
    @FXML
    TextArea descOfAnim;
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

    public void setAnimationName(String name) {
        animName.setText(name);
    }
    public void setMusicTitle(String title) {
        musicTitle.setText(title);
    }
    public void setFrameDuration(Integer count) {
        frameDuration.setText(Integer.toString(count));
    }
    public void setDescOfAnim(String desc) {
        descOfAnim.setText(desc);
    }
    public String getAnimationName(){
        return animName.getText();
    }
    public String getMusicTitle() {
        return musicTitle.getText();
    }
    public String getDescOfAnim(){
        return descOfAnim.getText();
    }
    public Integer getFrameDuration(){
        return Integer.parseInt(frameDuration.getText());
    }

    @FXML //loading animation
    public void onButtonLoadClicked() throws IOException {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);

        if (selectedFile != null) {
            System.out.printf("File has been loaded: " + selectedFile.getName() + "\n");
            byte[] buffer = new byte[1024];
            ZipFile zip = new ZipFile(selectedFile.getCanonicalPath());
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String path = entry.getName();

                if(path.endsWith("/")) {
                    File dir = new File(selectedFile.getParent()+"/"+path);
                    dirName = selectedFile.getParent()+"/"+path;
                    if(!dir.exists()){
                        dir.mkdirs();
                    }
                }else {
                    FileOutputStream fos = new FileOutputStream(selectedFile.getParent()+"/"+path);
                    InputStream is = zip.getInputStream(entry);
                    int len;
                    while((len = is.read(buffer)) > 0){
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }

            }
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(dirName + "/meta_template.json"));
            JsonElement json = gson.fromJson(reader, JsonElement.class);

            String jsonInString = gson.toJson(json);
            System.out.printf(jsonInString);
            JsonObject jsonObject = (JsonObject) json;
            Integer arraySize = jsonObject.get("frame_count").getAsInt();

            setAnimationName(jsonObject.get("animation_name").getAsString());
            setMusicTitle(jsonObject.get("music_file").getAsString());
            setDescOfAnim(jsonObject.get("description").getAsString());
            setFrameDuration(jsonObject.get("frame_duration").getAsInt());

            File dirAnim = new File(dirName);

            for(File fileEntry : dirAnim.listFiles()) {
                Frame frame = new Frame();
                int frameCount=1;
                if(fileEntry.getName().endsWith("bmp")){
                    BufferedImage imgBuffer = ImageIO.read(fileEntry);


                    byte[] getBytes  =(byte[]) imgBuffer.getRaster().getDataElements(0,0,imgBuffer.getWidth(),imgBuffer.getHeight(), null);


                    int count =0;
                    for(int j = 15; j >= 0; j--) {
                        for (int i = 0; i < 32; i++) {
                            frame.matrix[i][j][0] = getBytes[count] & 0xFF;
                            frame.matrix[i][j][1] = getBytes[count + 1] & 0xFF;
                            frame.matrix[i][j][2] = getBytes[count + 2] & 0xFF;


                            count += 3;
                        }
                    }
                    frame.frame_index = frameCount;
                    data.frames.add(frame);

                    frameCount++;


                }
            }



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
