A sample program to read and display an image in JavaFX panels. By default, this program will read in the first frame of a given .rgb video file.


Unzip the folder to where you want.
To run the code from command line, first compile with:

>> javac ImageDisplay.java

and then, you can run it to take in seven parameters "pathToRGBImg", "Y", "U", "V", "Sw", "Sh", "A" :

>> java ImageDisplay pathToRGBImg Y U V Sw Sh A


The result of this program is displaying two images side by side in one window(original on the left and a processed output on the right). 
Maybe you need to resize the window to view the full result.