package editor;

public class Frame {
    public int[][][] matrix = new int[32][16][3];
    public int frame_index = 0;

    Frame() {
        for(int i = 0; i < 32; i++) {
            for(int j = 0; j < 16; j++) {
                matrix[i][j][0] = 255;
                matrix[i][j][1] = 255;
                matrix[i][j][2] = 255;
            }
        }
    }
}
