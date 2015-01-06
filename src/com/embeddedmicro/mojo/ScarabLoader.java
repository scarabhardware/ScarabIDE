package com.embeddedmicro.mojo;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

public class ScarabLoader {

	private Display display;
	private InputStream in;
	private OutputStream out;
	private StyledText console;
	private Thread thread;

	public ScarabLoader(Display display, StyledText console) {
		this.display = display;
		this.console = console;
	}

	public boolean isLoading() {
		return thread != null && thread.isAlive();
	}

	
	private void updateProgress(final int percent) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				int lastLine = console.getLineCount() - 1;
				int lineOffset = console.getOffsetAtLine(lastLine);
				int lastOffset = console.getCharCount() - 1;
				if (lastOffset < lineOffset)
					lastOffset = lineOffset;

				StringBuilder bar = new StringBuilder("[");

				for (int i = 0; i < 50; i++) {
					if (i < (percent / 2)) {
						bar.append("=");
					} else if (i == (percent / 2)) {
						bar.append(">");
					} else {
						bar.append(" ");
					}
				}

				bar.append("]   " + percent + "%     ");

				console.replaceTextRange(lineOffset, lastOffset - lineOffset,
						bar.toString());
			}
		});
	}

	private void clearConsole() {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				console.setText("");
			}
		});
	}

	private void printText(final String text) {
		printText(text, false);
	}

	private void printText(final String text, final boolean red) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				String line = text + System.lineSeparator();
				console.append(line);
				if (red) {
					int end = console.getCharCount();
					StyleRange styleRange = new StyleRange();
					styleRange.start = end - line.length();
					styleRange.length = line.length();
					styleRange.foreground = Theme.errorTextColor;
					console.setStyleRange(styleRange);
				}
			}
		});
	}

	
	
	public void sendBin(final String port, final String binFile,
			final boolean flash, final boolean verify) {
		
		console.setText("Start Programming the FPGA using XC3SPROG \r\n \r\n");
		try {  
            Process p = Runtime.getRuntime().exec("xc3sprog -c " + port + " " + binFile);  
           try{
    		            p.waitFor();
           }
           catch ( InterruptedException e1 ) {
	            
           }

            BufferedReader in = new BufferedReader(  
                                new InputStreamReader(p.getErrorStream()));  

    		console.setText(console.getText() + in.read()+ "\r\n");

            System.out.println(in.read());
            String line = null;  
            while ((line = in.readLine()) != null) {  
                System.out.println(line);  
        	
                console.setText(console.getText() + line + "\r\n");

            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  


					
		System.out.println("4");
		
	}
}
