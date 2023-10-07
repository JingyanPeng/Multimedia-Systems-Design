import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import javax.swing.*;

public class ImageDWT {


    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;
    BufferedImage DWTImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    int[][][] orgMatrix = new int[3][HEIGHT][WIDTH];
    double[][][] DWT = new double[3][HEIGHT][WIDTH];
    int[][][] IDWT = new int[3][HEIGHT][WIDTH];
    JFrame imageFrame = new JFrame();
    GridBagLayout gridBagLayout = new GridBagLayout();
    JLabel DWTLabel = new JLabel();
    JLabel DWTLabelText = new JLabel();


    public void prcocessDWT(String[] args) {

        try {
            File file = new File(args[0]);
            int coLevel = Integer.parseInt(args[1]);

            InputStream inputStream = new FileInputStream(file);
            long fileLength = file.length();
            byte[] bytes = new byte[(int) fileLength];

            int offset = 0;
            int readCount = 0;
            while (offset < bytes.length && (readCount = inputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += readCount;
            }

            int ind = 0;
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    orgMatrix[0][y][x] = Byte.toUnsignedInt(bytes[ind]);
                    orgMatrix[1][y][x] = Byte.toUnsignedInt(bytes[ind+HEIGHT*WIDTH]);
                    orgMatrix[2][y][x] = Byte.toUnsignedInt(bytes[ind+HEIGHT*WIDTH*2]);

                    ind++;
                }
            }

            if (coLevel >= 0) {
                DWT[0] = DWTEncoding(orgMatrix[0], coLevel);
                DWT[1] = DWTEncoding(orgMatrix[1], coLevel);
                DWT[2] = DWTEncoding(orgMatrix[2], coLevel);
                IDWT[0] = IDWTDecoding(DWT[0]);
                IDWT[1] = IDWTDecoding(DWT[1]);
                IDWT[2] = IDWTDecoding(DWT[2]);

                showDWT(coLevel);
            }else {
                for (int i = 0; i <= 9; i++) {
                    DWT[0] = DWTEncoding(orgMatrix[0], i);
                    DWT[1] = DWTEncoding(orgMatrix[1], i);
                    DWT[2] = DWTEncoding(orgMatrix[2], i);
                    IDWT[0] = IDWTDecoding(DWT[0]);
                    IDWT[1] = IDWTDecoding(DWT[1]);
                    IDWT[2] = IDWTDecoding(DWT[2]);
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException ignored) {
                    }
                    showDWT(i);
                }
            }
        }catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Show DWT image
     */
    private void showDWT(int coLevel) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int pixValue = 0xff000000 | ((IDWT[0][y][x] & 0xff) << 16) | ((IDWT[1][y][x] & 0xff) << 8) | (IDWT[2][y][x] & 0xff);
                DWTImage.setRGB(x, y, pixValue);
            }
        }
        imageFrame.getContentPane().setLayout(gridBagLayout);
        DWTLabelText.setText(String.valueOf(coLevel));
        DWTLabel.setIcon(new ImageIcon(DWTImage));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        imageFrame.getContentPane().add(DWTLabelText, gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        imageFrame.getContentPane().add(DWTLabel, gridBagConstraints);

        imageFrame.pack();
        imageFrame.setVisible(true);
    }

    /**
     * Transposed Matrix
     */
    private double[][] matrixTranspose(double[][] matrix) {
        double[][] temp = new double[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++)
            for (int x = 0; x < WIDTH; x++)
                temp[y][x] = matrix[x][y];
        return temp;
    }

    /**
     * DWT processing
     */
    private double[][] DWTEncoding(int[][] matrix, int n) {

        double[][] DWTMatrix = new double[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++)
            for (int x = 0; x < WIDTH; x++)
                DWTMatrix[y][x] = matrix[y][x];
        for (int y = 0; y < WIDTH; y++)
            DWTMatrix[y] = getEncodingArray(DWTMatrix[y]);
        DWTMatrix = matrixTranspose(DWTMatrix);
        for (int x = 0; x < HEIGHT; x++)
            DWTMatrix[x] = getEncodingArray(DWTMatrix[x]);
        DWTMatrix = matrixTranspose(DWTMatrix);
        DWTMatrix = doZeroOut(DWTMatrix, n);

        return DWTMatrix;
    }

    private double[] getEncodingArray(double[] array) {
        int height = array.length;
        while (height > 0) {
            double[] dArray = Arrays.copyOf(array, array.length);
            for (int ind = 0; ind < height / 2; ind++) {
                dArray[ind] = (array[2 * ind] + array[2 * ind + 1]) / 2;
                dArray[height / 2 + ind] = (array[2 * ind] - array[2 * ind + 1]) / 2;
            }
            array = dArray;
            height = height / 2;
        }
        return array;
    }

    /**
     * IDWT processing
     */
    private int[][] IDWTDecoding(double[][] matrix) {
        int[][] IDWTMatrix = new int[HEIGHT][WIDTH];
        matrix = matrixTranspose(matrix);
        for (int x = 0; x < WIDTH; x++) {
            matrix[x] = getDecodingArray(matrix[x]);
        }
        matrix = matrixTranspose(matrix);
        for (int y = 0; y < HEIGHT; y++) {
            matrix[y] = getDecodingArray(matrix[y]);
        }
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                IDWTMatrix[y][x] = Math.max((Math.min((int) Math.round(matrix[y][x]), 255)),0);
            }
        }

        return IDWTMatrix;
    }

    private double[] getDecodingArray(double[] array) {
        int height = 1;
        while (height <= array.length) {
            double[] dArray = Arrays.copyOf(array, array.length);
            for (int ind = 0; ind < height / 2; ind++) {
                dArray[2 * ind] = array[ind] + array[height / 2 + ind];
                dArray[2 * ind + 1] = array[ind] - array[height / 2 + ind];
            }
            array = dArray;
            height = height * 2;
        }
        return array;
    }

    /**
     * zero out high pass
     */

    public double[][] doZeroOut(double[][] matrix, int coLevel) {
        for(int y = 0; y < HEIGHT; y++){
            for(int x = 0; x < WIDTH; x++){
                if(y >= Math.pow(2,coLevel) || x >= Math.pow(2,coLevel)){
                    matrix[y][x] = 0;
                }
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        ImageDWT ren = new ImageDWT();
        ren.prcocessDWT(args);
    }

}