package com.embeddedmicro.mojo;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Images {
	public static Image loadIcon;
	public static Image fileIcon;
	public static Image buildIcon;
	
	public static void loadImages(Display display){
		loadIcon = new Image(display, "res/load.png");
		fileIcon = new Image(display, "res/file.png");
		buildIcon = new Image(display, "res/build.png");
	}
	
	public static void dispose(){
		loadIcon.dispose();
		fileIcon.dispose();
		buildIcon.dispose();
	}
}
