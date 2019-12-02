package editor;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.zip.*;

public class Controller implements Initializable {
    Data data = new Data();
    int numCols = 32;
    int numRows = 16;
    Pane[][] paneMatrix = new Pane[numCols][numRows];
    int selectedFrame = 0;
    String dirName;
    String musicFileName;



    final static int BITMAPFILEHEADER_SIZE = 14;
    final static int BITMAPINFOHEADER_SIZE = 40;

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
    @FXML
    Button loadMusicFile;

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
    @FXML //loading music file
    public void onButtonLoadMusicFile(){
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);
        if(selectedFile != null){
            if(selectedFile.getName().endsWith("wav")){
                setMusicTitle(selectedFile.getName());
                System.out.println("Zostal wybrany plik: " + selectedFile.getName());
                musicFileName = selectedFile.getAbsolutePath();
            }
            else{
                System.out.println("To nie jest plik wav");
            }
        }


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
    public String getPathOfMusicFile(File file)
    {
        return file.getAbsolutePath();
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
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String path = entry.getName();

                if (path.endsWith("/")) {
                    File dir = new File(selectedFile.getParent() + "/" + path);
                    dirName = selectedFile.getParent() + "/" + path;
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    FileOutputStream fos = new FileOutputStream(selectedFile.getParent() + "/" + path);
                    InputStream is = zip.getInputStream(entry);
                    int len;
                    while ((len = is.read(buffer)) > 0) {
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
            int frameCount = 0;
            for (File fileEntry : dirAnim.listFiles()) {
                Frame frame = new Frame();

                if (fileEntry.getName().endsWith("bmp")) {
                    BufferedImage imgBuffer = ImageIO.read(fileEntry);


                    byte[] getBytes = (byte[]) imgBuffer.getRaster().getDataElements(0, 0, imgBuffer.getWidth(), imgBuffer.getHeight(), null);


                    int count = 0;
                    for (int j = 15; j >= 0; j--) {
                        for (int i = 0; i < 32; i++) {
                            frame.matrix[i][j][0] = getBytes[count] & 0xFF;
                            frame.matrix[i][j][1] = getBytes[count + 1] & 0xFF;
                            frame.matrix[i][j][2] = getBytes[count + 2] & 0xFF;


                            count += 3;
                        }
                    }
                    frame.frame_index = frameCount;


                    data.frames.add(frameCount, frame);

                    frameCount++;


                }
                else if(fileEntry.getName().endsWith("wav")){
                    musicFileName=fileEntry.getAbsolutePath();
                }
            }
            int sizeOfArray = data.frames.size();
            for (int i = sizeOfArray-1 ; i >= arraySize; i--){
                data.frames.remove(i);
            }
            loadFrame(selectedFrame);
            frameCounter.setText(String.valueOf(selectedFrame + 1) + '/' + String.valueOf(data.frames.size()));


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
        data.frames.get(selectedFrame).frame_index = selectedFrame;
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

    public byte[] bitMapHeader() throws IOException{

        int height = 16;
        int width = 32;

        byte[] bitmapFileHeader = {
                0, 0,       /// signature
                0, 0, 0, 0, /// image file size in bytes
                0, 0, 0, 0, /// reserved
                0, 0, 0, 0, /// start of pixel array
        } ;

        int fileSize = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE + (3 *width ) * height;
        bitmapFileHeader[0] = ('B');
        bitmapFileHeader[1] = ('M');
        bitmapFileHeader[2] = (byte)((fileSize)& 0xFF);
        bitmapFileHeader[3] = (byte)((fileSize >> 8)& 0xFF);
        bitmapFileHeader[4] = (byte)((fileSize >> 16)& 0xFF);
        bitmapFileHeader[5] = (byte)((fileSize >> 24)& 0xFF);
        bitmapFileHeader[10] = (byte)(BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE);


        byte[] bitmapInfoHeader = {
                0, 0, 0, 0, /// header size
                0, 0, 0, 0, /// image width
                0, 0, 0, 0, /// image height
                0, 0,       /// number of color planes
                0, 0,       /// bits per pixel
                0, 0, 0, 0, /// compression
                0, 0, 0, 0, /// image size
                0, 0, 0, 0, /// horizontal resolution
                0, 0, 0, 0, /// vertical resolution
                0, 0, 0, 0, /// colors in color table
                0, 0, 0, 0, /// important color count
        };
        bitmapInfoHeader[0] = (byte)(BITMAPINFOHEADER_SIZE);
        bitmapInfoHeader[4] = (byte)((width)& 0xFF);
        bitmapInfoHeader[5] = (byte)((width >> 8) & 0xFF);
        bitmapInfoHeader[6] = (byte)((width >> 16) & 0xFF);
        bitmapInfoHeader[7] = (byte) ((width >> 24)& 0xFF);
        bitmapInfoHeader[8] = (byte) ((height)& 0xFF);
        bitmapInfoHeader[9] = (byte) ((height >> 8)& 0xFF);
        bitmapInfoHeader[10] = (byte) ((height >> 16)& 0xFF);
        bitmapInfoHeader[11] = (byte) ((height >> 24)& 0xFF);
        bitmapInfoHeader[12] = (byte)(1);
        bitmapInfoHeader[14] = (byte)(24);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(bitmapFileHeader);
        outputStream.write(bitmapInfoHeader);

        byte[] buffer = outputStream.toByteArray();


        return buffer;
    }

    @FXML
    public void moveBack() {
        if(selectedFrame > 0) {
            Collections.swap(data.frames, selectedFrame, selectedFrame-1);
            selectedFrame--;
            frameCounter.setText(String.valueOf(selectedFrame + 1) + '/' + String.valueOf(data.frames.size()));
        }
    }

    @FXML
    public void moveNext() {
        if(selectedFrame < data.frames.size()-1) {
            Collections.swap(data.frames, selectedFrame, selectedFrame+1);
            selectedFrame++;
            frameCounter.setText(String.valueOf(selectedFrame + 1) + '/' + String.valueOf(data.frames.size()));
        }
    }

    @FXML //saving animation
    public void onButtonSaveClicked() throws Exception{

        if(getAnimationName().equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Podaj nazwe animacji!");
            alert.showAndWait();
        }
        else {
            File dir = new File("animations/"+ getAnimationName());
            if(!dir.exists()) {
                dir.mkdir();

                JsonWriter writer = new JsonWriter(new FileWriter(dir.getPath()+"/meta_template.json"));
                writer.setIndent("  ");
                writer.beginObject();
                writer.name("frame_count").value(data.frames.size());
                writer.name("frame_duration").value(getFrameDuration());
                writer.name("music_file").value(getMusicTitle());
                writer.name("animation_name").value(getAnimationName());
                writer.name("description").value(getDescOfAnim());
                writer.endObject();
                writer.close();

                for(int i=0; i<data.frames.size(); i++) {
                    int count = 0;
                    // System.out.printf("NUMER FRAME: %d\n", data.frames.get(i).frame_index);
                    Frame frame = data.frames.get(i);
                    byte buffer[] = new byte[32 * 16 * 3];
                    for (int k = 15; k >= 0; k--) {
                        for (int j = 0; j < 32; j++) {

                            buffer[count] = (byte) frame.matrix[j][k][0] ;
                            buffer[count + 1] = (byte) frame.matrix[j][k][1];
                            buffer[count + 2] = (byte) frame.matrix[j][k][2];
//                            System.out.printf( "PIXEL: %d,",((int) buffer[count] & 0xFF));
//                            System.out.printf( "%d,",((int) buffer[count + 1] & 0xFF));
//                            System.out.printf( "%d\n",((int) buffer[count + 2] & 0xFF));
                            count += 3;


                        }
                    }
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                    outputStream.write(bitMapHeader());
                    outputStream.write(buffer);

                    byte[] bmpFile = outputStream.toByteArray();

                    BufferedImage image = ImageIO.read( new ByteArrayInputStream( bmpFile ) );
                    ImageIO.write(image, "BMP", new File(dir.getAbsolutePath() +"/"+ data.frames.get(i).frame_index +".bmp"));

                }
                if(getMusicTitle().equals("")){
                    System.out.printf("Brak pliku wav\n");
                }
                else {
                    byte[] buffer = new byte[1024];
                    File file = new File(musicFileName);
                    System.out.printf(file.getName());
                    FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + "/" + file.getName());
                    InputStream is = new FileInputStream(file);
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();

                }
                FileOutputStream fileWriter = new FileOutputStream(dir.getAbsolutePath()+".zip");
                ZipOutputStream zip = new ZipOutputStream(fileWriter);

                addFolderToZip("", dir.getAbsolutePath(), zip);

                zip.flush();
                zip.close();


            }

        }


    }
    public void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception
    {
        File folder = new File(srcFolder);

        for (String fileName : folder.list())
        {
            if (path.equals(""))
            {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            }
            else
            {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }
    public void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception
    {
        File folder = new File(srcFile);
        if (folder.isDirectory())
        {
            addFolderToZip(path, srcFile, zip);
        }
        else
        {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0)
            {
                zip.write(buf, 0, len);
            }
            in.close();
        }
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
