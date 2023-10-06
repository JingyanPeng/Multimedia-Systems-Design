
To run the code from the command line, first compile with:


>> j

and then, you can run it to take in 3 parameters "C:/myDir/foreGroundVideo", "C:/myDir/backGroundVideo", and "mode":


>> java VideoDisplay C:/myDir/foreGroundVideo C:/myDir/backGroundVideo mode

where the parameter "C:/myDir/foreGroundVideo" is the path of the folder. The folder contains a series of foreground.rgb files(foreground.0000.rgb ~ foreground.0479.rgb), and this string cannot contain any spaces;

the parameter "C:/myDir/backGroundVideo" is the path of the folder. The folder contains a series of background.rgb files(background.0000.rgb ~ background.0479.rgb), and this string cannot contain any spaces;

the parameter "mode" is a mode that can take values 1 or 0. 1 indicating that the foreground video
has a green screen and 0 indicating there is no green screen.