
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay1 {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 512; // default image width and height
	int height = 512;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img, int lowPassLevel)
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
			double[][][] encodingData1 = new double[height][width][3];
			double[][][] encodingData2 = new double[height][width][3];
			double[][][] decodingData1 = new double[height][width][3];
			double[][][] decodingData2 = new double[height][width][3];

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					encodingData1[y][x][0] = Byte.toUnsignedInt(bytes[ind]);
					encodingData1[y][x][1] = Byte.toUnsignedInt(bytes[ind+height*width]);
					encodingData1[y][x][2] = Byte.toUnsignedInt(bytes[ind+height*width*2]);
					ind++;
				}
			}
			encodingData2 = encodingData1;
			//encoding processing (DWT
			for(int i = 1; i < 10 - lowPassLevel; i++){
				//for rows
				for(int y = 0; y < height; y++){
					for(int x = 0; x < (Math.pow(2, 9-i)); x++){
						encodingData2[y][x][0] = (encodingData1[y][2*x][0] + encodingData1[y][2*x+1][0])/2.0;
						encodingData2[y][(int)(Math.pow(2, 9-i)+x)][0] = (encodingData1[y][2*x][0] - encodingData1[y][2*x+1][0])/2.0;
						encodingData2[y][x][1] = (encodingData1[y][2*x][1] + encodingData1[y][2*x+1][1])/2.0;
						encodingData2[y][(int)(Math.pow(2, 9-i)+x)][1] = (encodingData1[y][2*x][1] - encodingData1[y][2*x+1][1])/2.0;
						encodingData2[y][x][2] = (encodingData1[y][2*x][2] + encodingData1[y][2*x+1][2])/2.0;
						encodingData2[y][(int)(Math.pow(2, 9-i)+x)][2] = (encodingData1[y][2*x][2] - encodingData1[y][2*x+1][2])/2.0;
					}
				}
				encodingData1 = encodingData2;
				//for columns
				for(int x = 0; x < width; x++){
					for(int y = 0; y < Math.pow(2, 9-i); y++){
						encodingData2[y][x][0] = (encodingData1[2*y][x][0] + encodingData1[2*y+1][x][0])/2.0;
						encodingData2[(int)(y+Math.pow(2, 9-i))][x][0] = (encodingData1[2*y][x][0] - encodingData1[2*y+1][x][0])/2.0;
						encodingData2[y][x][1] = (encodingData1[2*y][x][1] + encodingData1[2*y+1][x][1])/2.0;
						encodingData2[(int)(y+Math.pow(2, 9-i))][x][1] = (encodingData1[2*y][x][1] - encodingData1[2*y+1][x][1])/2.0;
						encodingData2[y][x][2] = (encodingData1[2*y][x][2] + encodingData1[2*y+1][x][2])/2.0;
						encodingData2[(int)(y+Math.pow(2, 9-i))][x][2] = (encodingData1[2*y][x][2] - encodingData1[2*y+1][x][2])/2.0;
					}
				}
				encodingData1 = encodingData2;
			}
	/*
			//zero out high pass
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					if(y >= Math.pow(2,lowPassLevel) || x >= Math.pow(2,lowPassLevel)){
						encodingData1[y][x][0] = 0;
						encodingData1[y][x][1] = 0;
						encodingData1[y][x][2] = 0;
					}
				}
			}
			decodingData1 = encodingData1;


			//decoding processing (IDWT
			for(int i = 1; i < 10 - lowPassLevel; i++){
				//for rows
				for(int y = 0; y < height; y++){
					for(int x = 0; x < (Math.pow(2, 9-i)); x++){
						decodingData2[][][]
					}
				}
				encodingData1 = encodingData2;
				//for columns
				for(int x = 0; x < width; x++){
					for(int y = 0; y < Math.pow(2, 9-i); y++){
						decodingData2[][][]
					}
				}
				encodingData1 = encodingData2;
			}


	 */
			decodingData1 = encodingData1;

			for(int y = 0; y < height; y++)
			{
				for(int x =0; x < width; x++)
				{
					int r,g,b;
					r = Math.max(0, Math.min(255, (int)decodingData1[y][x][0]));
					g = Math.max(0, Math.min(255, (int)decodingData1[y][x][1]));
					b = Math.max(0, Math.min(255, (int)decodingData1[y][x][2]));
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
				}
			}
			//************************************************************************************//
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
		//String param1 = args[1];
		//System.out.println("The second parameter was: " + param1);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne, Integer.parseInt(args[1]));

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ImageDisplay1 ren = new ImageDisplay1();
		ren.showIms(args);
	}

}
