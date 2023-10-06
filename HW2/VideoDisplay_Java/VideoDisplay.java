//CSCI576 HW2 Programming Part
//author: Jingyan Peng
//date: 10/07/2022

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class VideoDisplay {

	JFrame frame;
	JLabel lbIm;
	BufferedImage imgOne;
	int width = 640; // default image width and height
	int height = 480;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	//for mode 1
	private void read2ImageRGB(int width, int height, String imgPath1, String imgPath2, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			//read foreground
			File file1 = new File(imgPath1);
			RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
			raf1.seek(0);
			//read background
			File file2 = new File(imgPath2);
			RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
			raf2.seek(0);

			long len = frameLength;
			byte[] bytes1 = new byte[(int) len];
			byte[] bytes2 = new byte[(int) len];
			raf1.read(bytes1);
			raf2.read(bytes2);

			//-------------------------------------------------------------------------------------------//
			double[][][] hslData = new double[height][width][3];
			int[][][] rgbData = new int[height][width][3];

			//foreground: RGB->HSL
			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					int r = Byte.toUnsignedInt(bytes1[ind]);
					int g = Byte.toUnsignedInt(bytes1[ind + height * width]);
					int b = Byte.toUnsignedInt(bytes1[ind + height * width * 2]);
					//HSL
					double h,s,l;
					double rr = ((double)r) / 255;
					double gg = ((double)g) / 255;
					double bb = ((double)b) / 255;
					double max = Math.max(Math.max(r, g), b);
					double min = Math.min(Math.min(r, g), b);
					double change = (max - min) / 255;
					//compute L
					l = (max + min) / 255 * 0.5;
					//compute S
					if(l == 0 | change == 0){
						s = (double) 0;
					} else if (l <= 0.5) {
						s = change / (2 * l);
					}else {
						s = change / (2 - 2 * l);
					}
					//compute H
					h = (double) 0;
					if(change == (double)0){
						h = (double) 0;
					} else if (max == (double)r) {
						h = (Math.abs(gg - bb))/change * 60;
					} else if (max == (double)g) {
						h = (bb - rr)/change * 60 + 120;
					} else if (max == (double)b) {
						h = (rr - gg)/change * 60 + 240;
					}
					hslData[y][x][0] = h;
					hslData[y][x][1] = s;
					hslData[y][x][2] = l;
					//System.out.println(h);
					ind++;
				}
			}

			//foreground: HSL->RGB (Green pixels are set to R:-1,G:-1,B:-1）
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					double h = hslData[y][x][0];
					double s = hslData[y][x][1];
					double l = hslData[y][x][2];
					int r,g,b;
					if( 65 < h && h < 175 && s > 0.17 && l < 0.85 && l > 0.2){
						r = -1;
						g = -1;
						b = -1;
					}else {
						double cc = (1-Math.abs(2 * l - 1)) * s;
						double hh = h / 60;
						double xx = cc * (1- Math.abs( hh % 2 - 1));
						double rr, gg, bb;
						if(0 <= hh && hh < 1){
							rr = cc;
							gg = xx;
							bb = 0;
						} else if (hh < 2) {
							rr = xx;
							gg = xx;
							bb = 0;
						} else if (hh < 3) {
							rr = 0;
							gg = cc;
							bb = xx;
						} else if (hh < 4) {
							rr = 0;
							gg = xx;
							bb = cc;
						} else if (hh < 5) {
							rr = xx;
							gg = 0;
							bb = cc;
						}else {
							rr = cc;
							gg = 0;
							bb = xx;
						}
						double mm = l - cc/2;
						r = Math.max(0, Math.min(255, (int)((rr + mm)*255)));
						g = Math.max(0, Math.min(255, (int)((gg + mm)*255)));
						b = Math.max(0, Math.min(255, (int)((bb + mm)*255)));
					}
					rgbData[y][x][0] = r;
					rgbData[y][x][1] = g;
					rgbData[y][x][2] = b;
				}
			}

			//Composite Output Image (foreground + background)
			ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					int r1 = rgbData[y][x][0];
					int g1 = rgbData[y][x][1];
					int b1 = rgbData[y][x][2];
					int r2 = Byte.toUnsignedInt(bytes2[ind]);
					int g2 = Byte.toUnsignedInt(bytes2[ind + height * width]);
					int b2 = Byte.toUnsignedInt(bytes2[ind + height * width * 2]);

					int pix;
					if(r1 == -1){
						pix = 0xff000000 | ((r2 & 0xff) << 16) | ((g2 & 0xff) << 8) | (b2 & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					}else if(x < 620 && x > 1 && rgbData[y][x-2][0] == -1 && rgbData[y][x+1][0] != -1 && rgbData[y][x+20][0] != -1){ //left boundry blending
						int rle =(int) (rgbData[y][x+1][0] * 0.5 + r2 * 0.5);
						int gle =(int) (rgbData[y][x+1][1] * 0.5 + g2 * 0.5);
						int ble =(int) (rgbData[y][x+1][2] * 0.5 + b2 * 0.5);
						pix = 0xff000000 | ((rle & 0xff) << 16) | ((gle & 0xff) << 8) | (ble & 0xff);
					} else if (x > 19 && x < 638 && rgbData[y][x+2][0] == -1 && rgbData[y][x-1][0] != -1 && rgbData[y][x-20][0] != -1) { // right boundry blending
						int rri =(int) (rgbData[y][x-1][0] * 0.5 + r2 * 0.5);
						int gri =(int) (rgbData[y][x-1][1] * 0.5 + g2 * 0.5);
						int bri =(int) (rgbData[y][x-1][2] * 0.5 + b2 * 0.5);
						pix = 0xff000000 | ((rri & 0xff) << 16) | ((gri & 0xff) << 8) | (bri & 0xff);
					} else {
						pix = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
					}
					img.setRGB(x,y,pix);
					ind++;
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

	//for mode 0
	private void read3ImageRGB(int width, int height, String imgPath1, String imgPath2, String imgPath3, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			//read foreground
			File file1 = new File(imgPath1);
			RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
			raf1.seek(0);
			File file2 = new File(imgPath2);
			RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
			raf2.seek(0);

			//read background
			File file3 = new File(imgPath3);
			RandomAccessFile raf3 = new RandomAccessFile(file3, "r");
			raf3.seek(0);

			long len = frameLength;
			byte[] bytes1 = new byte[(int) len];
			byte[] bytes2 = new byte[(int) len];
			byte[] bytes3 = new byte[(int) len];
			raf1.read(bytes1);
			raf2.read(bytes2);
			raf3.read(bytes3);

			//-------------------------------------------------------------------------------------------//
			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					int r1 = Byte.toUnsignedInt(bytes1[ind]);
					int g1 = Byte.toUnsignedInt(bytes1[ind + height * width]);
					int b1 = Byte.toUnsignedInt(bytes1[ind + height * width * 2]);
					int r2 = Byte.toUnsignedInt(bytes2[ind]);
					int g2 = Byte.toUnsignedInt(bytes2[ind + height * width]);
					int b2 = Byte.toUnsignedInt(bytes2[ind + height * width * 2]);
					boolean foreground = false;
					if((Math.pow((r1-r2),2) + Math.pow((g1-g2),2) + Math.pow((b1-b2),2)) > 25){
						foreground = true;
					}
					byte r3 = bytes3[ind];
					byte g3 = bytes3[ind+height*width];
					byte b3 = bytes3[ind+height*width*2];

					int pix;
					if(foreground){
						pix = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					} else{
						pix = 0xff000000 | ((r3 & 0xff) << 16) | ((g3 & 0xff) << 8) | (b3 & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					}
					img.setRGB(x,y,pix);
					ind++;
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

	//for mode 1
	public void showVideo1(String[] args){

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		//Processing Video Path -> Video Name
		String foregroundName = args[0].substring(args[0].lastIndexOf("/") + 1);
		String backgroundName = args[1].substring(args[1].lastIndexOf("/") + 1);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		read2ImageRGB(width, height, args[0]+"/"+foregroundName+".0000.rgb", args[1]+"/"+backgroundName+".0000.rgb", imgOne);
		lbIm = new JLabel(new ImageIcon(imgOne));
		frame.getContentPane().add(lbIm, c);
		frame.pack();
		frame.setVisible(true);
		long lastTime = System.currentTimeMillis();


		for(int i = 1; i < 480; i++){
			while(System.currentTimeMillis() - lastTime < 1000/24){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lastTime = System.currentTimeMillis();
			if(i < 10){
				read2ImageRGB(width, height, args[0]+"/"+foregroundName+".000"+i+".rgb", args[1]+"/"+backgroundName+".000"+i+".rgb", imgOne);
			} else if (i < 100) {
				read2ImageRGB(width, height, args[0]+"/"+foregroundName+".00"+i+".rgb", args[1]+"/"+backgroundName+".00"+i+".rgb", imgOne);
			}else {
				read2ImageRGB(width, height, args[0]+"/"+foregroundName+".0"+i+".rgb", args[1]+"/"+backgroundName+".0"+i+".rgb", imgOne);
			}
			lbIm.setIcon(new ImageIcon(imgOne));
			frame.pack();
			frame.setVisible(true);
		}

	}

	//for mode 0
	public void showVideo0(String[] args){

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		//Processing Video Path -> Video Name
		String foregroundName = args[0].substring(args[0].lastIndexOf("/") + 1);
		String backgroundName = args[1].substring(args[1].lastIndexOf("/") + 1);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		read3ImageRGB(width, height, args[0]+"/"+foregroundName+".0000.rgb", args[0]+"/"+foregroundName+".0001.rgb", args[1]+"/"+backgroundName+".0000.rgb", imgOne);
		lbIm = new JLabel(new ImageIcon(imgOne));
		frame.getContentPane().add(lbIm, c);
		frame.pack();
		frame.setVisible(true);
		long lastTime = System.currentTimeMillis();

		for(int i = 2; i < 480; i++){
			while(System.currentTimeMillis() - lastTime < 1000/24){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lastTime = System.currentTimeMillis();
			int ii = i - 1;
			if(i < 10){
				read3ImageRGB(width, height, args[0]+"/"+foregroundName+".000"+ii+".rgb", args[0]+"/"+foregroundName+".000"+i+".rgb", args[1]+"/"+backgroundName+".000"+i+".rgb", imgOne);
			} else if (i < 11) {
				read3ImageRGB(width, height, args[0]+"/"+foregroundName+".000"+ii+".rgb", args[0]+"/"+foregroundName+".00"+i+".rgb", args[1]+"/"+backgroundName+".00"+i+".rgb", imgOne);
			}
			else if (i < 100) {
				read3ImageRGB(width, height, args[0]+"/"+foregroundName+".00"+ii+".rgb", args[0]+"/"+foregroundName+".00"+i+".rgb",args[1]+"/"+backgroundName+".00"+i+".rgb", imgOne);
			} else if (i < 101) {
				read3ImageRGB(width, height, args[0]+"/"+foregroundName+".00"+ii+".rgb", args[0]+"/"+foregroundName+".0"+i+".rgb", args[1]+"/"+backgroundName+".0"+i+".rgb", imgOne);
			} else {
				read3ImageRGB(width, height, args[0]+"/"+foregroundName+".0"+ii+".rgb", args[0]+"/"+foregroundName+".0"+i+".rgb",args[1]+"/"+backgroundName+".0"+i+".rgb", imgOne);
			}
			lbIm.setIcon(new ImageIcon(imgOne));
			frame.pack();
			frame.setVisible(true);
		}

	}

	public static void main(String[] args) {
		VideoDisplay play = new VideoDisplay();
		if(Integer.parseInt(args[2]) == 0) { //mode 0
			play.showVideo0(args);
		} else if (Integer.parseInt(args[2]) == 1) { //mode 1
			play.showVideo1(args);
		}
	}

}
