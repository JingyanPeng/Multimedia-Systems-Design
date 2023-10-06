
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage imgOne;
	BufferedImage imgZero;
	int width = 1920; // default image width and height
	int height = 1080;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img, int sY, int sU, int sV, float sW, float sH, int antiA)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			//************************************************************************************//
			double[][][] yuvData1 = new double[height][width][3];
			double[][][] yuvData2 = new double[height][width][3];

			//RGB->YUV
			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					int r = Byte.toUnsignedInt(bytes[ind]);
					int g = Byte.toUnsignedInt(bytes[ind + height * width]);
					int b = Byte.toUnsignedInt(bytes[ind + height * width * 2]);

					double yy = 0.299 * r + 0.587 * g + 0.114 * b;
					double u = 0.596 * r - 0.274 * g - 0.322 * b;
					double v = 0.211 * r - 0.523 * g + 0.312 * b;
					yuvData1[y][x][0] = yy;
					yuvData1[y][x][1] = u;
					yuvData1[y][x][2] = v;
					ind++;
				}
			}

			//Subsampling of YUV
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++){
					if(x % sY == 0) yuvData2[y][x][0] = yuvData1[y][x][0]; //process subsampling
					else {
						if((x - (x%sY) + sY) < width){  //adjust up sampling for display (linear interpolate)
							yuvData2[y][x][0] = (yuvData1[y][x-(x%sY)+sY][0]-yuvData1[y][x-(x%sY)][0]) * (double)(x%sY)/(double)sY + yuvData1[y][x-(x%sY)][0];
						}else{ //linear interpolate the end part
							yuvData2[y][x][0] = (yuvData1[y][width-1][0]-yuvData1[y][x-(x%sY)][0]) * (double)(x%sY)/(double)((width-1)-(x-(x%sY))) + yuvData1[y][x-(x%sY)][0];
						}
					}
					if(x % sU == 0) yuvData2[y][x][1] = yuvData1[y][x][1];
					else {
						if((x-(x%sU)+sU) < width){
							yuvData2[y][x][1] = (yuvData1[y][x-(x%sU)+sU][1]-yuvData1[y][x-(x%sU)][1]) * (double)(x%sU)/(double)sU + yuvData1[y][x-(x%sU)][1];
						}else{
							yuvData2[y][x][1] = (yuvData1[y][width-1][1]-yuvData1[y][x-(x%sU)][1]) * (double)(x%sU)/(double)((width-1)-(x-(x%sU))) + yuvData1[y][x-(x%sU)][1];
						}
					}
					if(x % sV == 0) yuvData2[y][x][2] = yuvData1[y][x][2];
					else {
						if((x-(x%sV)+sV) < width){
							yuvData2[y][x][2] = (yuvData1[y][x-(x%sV)+sV][2]-yuvData1[y][x-(x%sV)][2]) * (double)(x%sV)/(double)sV + yuvData1[y][x-(x%sV)][2];
						}else{
							yuvData2[y][x][2] = (yuvData1[y][width-1][2]-yuvData1[y][x-(x%sV)][2]) * (double)(x%sV)/(double)((width-1)-(x-(x%sV))) + yuvData1[y][x-(x%sV)][2];
						}
					}
				}
			}

			//************************************************************************************//
			int[][][] rgbData1 = new int[height][width][3];
			int heightS = (int)(height * sH);
			int widthS = (int)(width * sW);
			int[][][] rgbData2 = new int[heightS][widthS][3];
			int[][][] rgbData3 = new int[heightS][widthS][3];

			//YUV->RGB
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					double yy = yuvData2[y][x][0];
					double u = yuvData2[y][x][1];
					double v = yuvData2[y][x][2];
					int r = (int)(1.000 * yy + 0.956 * u + 0.621 * v);
					r = Math.max(0, Math.min(255, r));
					int g = (int)(1.000 * yy - 0.272 * u - 0.647 * v);
					g = Math.max(0, Math.min(255, g));
					int b = (int)(1.000 * yy - 1.106 * u + 1.703 * v);
					b = Math.max(0, Math.min(255, b));
					rgbData1[y][x][0] = r;
					rgbData1[y][x][1] = g;
					rgbData1[y][x][2] = b;
				}
			}

			//Scaling without Antialiasing
			for(int y = 0; y < heightS; y++){
				for(int x = 0; x < widthS; x++){
					rgbData2[y][x][0] = rgbData1[Math.round((y * (((float)1)/sH)))][Math.round((x * (((float)1)/sW)))][0];
					rgbData2[y][x][1] = rgbData1[Math.round((y * (((float)1)/sH)))][Math.round((x * (((float)1)/sW)))][1] ;
					rgbData2[y][x][2] = rgbData1[Math.round((y * (((float)1)/sH)))][Math.round((x * (((float)1)/sW)))][2] ;
				}
			}
			//Antialiasing
			for(int y = 0; y < heightS; y++){
				for(int x = 0; x < widthS; x++){
					if(x == 0 && y == 0){
						rgbData3[y][x][0] = (int) (((float)1/(float) 4) * (rgbData2[y][x][0] + rgbData2[y][x+1][0] + rgbData2[y+1][x][0] + rgbData2[y+1][x+1][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 4) * (rgbData2[y][x][1] + rgbData2[y][x+1][1] + rgbData2[y+1][x][1] + rgbData2[y+1][x+1][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 4) * (rgbData2[y][x][2] + rgbData2[y][x+1][2] + rgbData2[y+1][x][2] + rgbData2[y+1][x+1][2]));
					} else if (x == 0 && y == heightS-1) {
						rgbData3[y][x][0] = (int) (((float)1/(float) 4) * (rgbData2[y-1][x][0] + rgbData2[y-1][x+1][0] + rgbData2[y][x][0] + rgbData2[y][x+1][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 4) * (rgbData2[y-1][x][1] + rgbData2[y-1][x+1][1] + rgbData2[y][x][1] + rgbData2[y][x+1][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 4) * (rgbData2[y-1][x][2] + rgbData2[y-1][x+1][2] + rgbData2[y][x][2] + rgbData2[y][x+1][2]));
					} else if (x == widthS-1 && y == 0) {
						rgbData3[y][x][0] = (int) (((float)1/(float) 4) * (rgbData2[y][x-1][0] + rgbData2[y][x][0] + rgbData2[y+1][x-1][0] + rgbData2[y+1][x][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 4) * (rgbData2[y][x-1][1] + rgbData2[y][x][1] + rgbData2[y+1][x-1][1] + rgbData2[y+1][x][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 4) * (rgbData2[y][x-1][2] + rgbData2[y][x][2] + rgbData2[y+1][x-1][2] + rgbData2[y+1][x][2]));
					} else if (x == widthS-1 && y == heightS-1) {
						rgbData3[y][x][0] = (int) (((float)1/(float) 4) * (rgbData2[y-1][x-1][0] + rgbData2[y-1][x][0] + rgbData2[y][x-1][0] + rgbData2[y][x][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 4) * (rgbData2[y-1][x-1][1] + rgbData2[y-1][x][1] + rgbData2[y][x-1][1] + rgbData2[y][x][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 4) * (rgbData2[y-1][x-1][2] + rgbData2[y-1][x][2] + rgbData2[y][x-1][2] + rgbData2[y][x][2]));
					} else if (x == 0) {
						rgbData3[y][x][0] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x][0] + rgbData2[y-1][x+1][0] + rgbData2[y][x][0] + rgbData2[y][x+1][0] + rgbData2[y+1][x][0] + rgbData2[y+1][x+1][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x][1] + rgbData2[y-1][x+1][1] + rgbData2[y][x][1] + rgbData2[y][x+1][1] + rgbData2[y+1][x][1] + rgbData2[y+1][x+1][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x][2] + rgbData2[y-1][x+1][2] + rgbData2[y][x][2] + rgbData2[y][x+1][2] + rgbData2[y+1][x][2] + rgbData2[y+1][x+1][2]));
					} else if (x == widthS-1) {
						rgbData3[y][x][0] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x-1][0] + rgbData2[y-1][x][0] + rgbData2[y][x-1][0] + rgbData2[y][x][0] + rgbData2[y+1][x-1][0] + rgbData2[y+1][x][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x-1][1] + rgbData2[y-1][x][1] + rgbData2[y][x-1][1] + rgbData2[y][x][1] + rgbData2[y+1][x-1][1] + rgbData2[y+1][x][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x-1][2] + rgbData2[y-1][x][2] + rgbData2[y][x-1][2] + rgbData2[y][x][2] + rgbData2[y+1][x-1][2] + rgbData2[y+1][x][2]));
					} else if (y == 0) {
						rgbData3[y][x][0] = (int) (((float)1/(float) 6) * (rgbData2[y][x-1][0] + rgbData2[y][x][0] + rgbData2[y][x+1][0] + rgbData2[y+1][x-1][0] + rgbData2[y+1][x][0] + rgbData2[y+1][x+1][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 6) * (rgbData2[y][x-1][1] + rgbData2[y][x][1] + rgbData2[y][x+1][1] + rgbData2[y+1][x-1][1] + rgbData2[y+1][x][1] + rgbData2[y+1][x+1][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 6) * (rgbData2[y][x-1][2] + rgbData2[y][x][2] + rgbData2[y][x+1][2] + rgbData2[y+1][x-1][2] + rgbData2[y+1][x][2] + rgbData2[y+1][x+1][2]));
					} else if (y == heightS-1) {
						rgbData3[y][x][0] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x-1][0] + rgbData2[y-1][x][0] + rgbData2[y-1][x+1][0] + rgbData2[y][x-1][0] + rgbData2[y][x][0] + rgbData2[y][x+1][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x-1][1] + rgbData2[y-1][x][1] + rgbData2[y-1][x+1][1] + rgbData2[y][x-1][1] + rgbData2[y][x][1] + rgbData2[y][x+1][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 6) * (rgbData2[y-1][x-1][2] + rgbData2[y-1][x][2] + rgbData2[y-1][x+1][2] + rgbData2[y][x-1][2] + rgbData2[y][x][2] + rgbData2[y][x+1][2]));
					}else {
						rgbData3[y][x][0] = (int) (((float)1/(float) 9) * (rgbData2[y-1][x-1][0] + rgbData2[y-1][x][0] + rgbData2[y-1][x+1][0] + rgbData2[y][x-1][0] + rgbData2[y][x][0] + rgbData2[y][x+1][0] + rgbData2[y+1][x-1][0] + rgbData2[y+1][x][0] + rgbData2[y+1][x+1][0]));
						rgbData3[y][x][1] = (int) (((float)1/(float) 9) * (rgbData2[y-1][x-1][1] + rgbData2[y-1][x][1] + rgbData2[y-1][x+1][1] + rgbData2[y][x-1][1] + rgbData2[y][x][1] + rgbData2[y][x+1][1] + rgbData2[y+1][x-1][1] + rgbData2[y+1][x][1] + rgbData2[y+1][x+1][1]));
						rgbData3[y][x][2] = (int) (((float)1/(float) 9) * (rgbData2[y-1][x-1][2] + rgbData2[y-1][x][2] + rgbData2[y-1][x+1][2] + rgbData2[y][x-1][2] + rgbData2[y][x][2] + rgbData2[y][x+1][2] + rgbData2[y+1][x-1][2] + rgbData2[y+1][x][2] + rgbData2[y+1][x+1][2]));
					}
				}
			}


			//Display Output Image
			for(int y = 0; y < heightS; y++)
			{
				for(int x = 0; x < widthS; x++)
				{
					int r,g,b;
					if(antiA == 0){ //no antialiasing
						r = rgbData2[y][x][0];
						g = rgbData2[y][x][1];
						b = rgbData2[y][x][2];
					}else{ // antialiasing
						r = rgbData3[y][x][0];
						g = rgbData3[y][x][1];
						b = rgbData3[y][x][2];
					}
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
				}
			}

		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void showIms(String[] args){

		// Read a parameter from command line
		String param1 = args[1];
		System.out.println("The second parameter was: " + param1);
		int sY = Integer.parseInt(args[1]);
		int sU = Integer.parseInt(args[2]);
		int sV = Integer.parseInt(args[3]);
		float sW = Float.parseFloat(args[4]);
		float sH = Float.parseFloat(args[5]);
		int antiA  = Integer.parseInt(args[6]);

		// Read in the specified image
		imgZero = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgOne = new BufferedImage((int)(width * sW), (int)(height * sH), BufferedImage.TYPE_INT_RGB);
		readImageRGB(width,height,args[0], imgZero, 1, 1, 1, 1,1,0);
		readImageRGB(width, height, args[0], imgOne, sY, sU, sV, sW, sH, antiA);


		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));
		lbIm2 = new JLabel(new ImageIcon((imgZero)));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbIm2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
