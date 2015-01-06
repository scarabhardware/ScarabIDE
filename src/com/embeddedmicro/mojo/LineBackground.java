package com.embeddedmicro.mojo;

import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;

public class LineBackground implements LineBackgroundListener {

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		//event.lineBackground = new Color(event.display, 100, 100, 100);
	}

}
