package com.embeddedmicro.mojo;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

public class MojoLoader {
	private Display display;
	private InputStream in;
	private OutputStream out;
	private SerialPort serialPort;
	private StyledText console;
	private Thread thread;

	public MojoLoader(Display display, StyledText console) {
		this.display = display;
		this.console = console;
	}

	public boolean isLoading() {
		return thread != null && thread.isAlive();
	}

	public static ArrayList<String> listPorts() {
		ArrayList<String> ports = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier
				.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier portIdentifier = portEnum.nextElement();
			if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				ports.add(portIdentifier.getName());
			}
		}
		return ports;
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

	private int read(int timeout) throws IOException, TimeoutException {
		long initTime = System.currentTimeMillis();
		while (true)
			if (in.available() > 0)
				return in.read();
			else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {

				}
				if (System.currentTimeMillis() - initTime >= timeout) {
					return -1;
					// throw new TimeoutException(
					// "Timeout while reading from serial port!");
				}
			}
	}

	private void restartMojo() throws InterruptedException {
		serialPort.setDTR(false);
		Thread.sleep(5);
		for (int i = 0; i < 5; i++) {
			serialPort.setDTR(false);
			Thread.sleep(5);
			serialPort.setDTR(true);
			Thread.sleep(5);
		}
	}

	public void clearFlash(final String port) {
		thread = new Thread() {
			public void run() {
				clearConsole();
				printText("Connecting...");
				try {
					connect(port);
				} catch (Exception e) {
					onError("Could not connect to port " + port + "!");
					return;
				}

				try {
					restartMojo();
				} catch (InterruptedException e) {
					onError(e.getMessage());
					return;
				}

				try {
					printText("Erasing...");

					while (in.available() > 0)
						in.skip(in.available()); // Flush the buffer

					out.write('E'); // Erase flash

					if (read(1000) != 'D') {
						onError("Mojo did not acknowledge flash erase!");
						return;
					}

					printText("Done");

				} catch (IOException | TimeoutException e) {
					onError(e.getMessage());
					return;
				}

				try {
					in.close();
					out.close();
				} catch (IOException e) {
					onError(e.getMessage());
					return;
				}

				serialPort.close();
			}
		};
		thread.start();
	}

	public void sendBin(final String port, final String binFile,
			final boolean flash, final boolean verify) {
		thread = new Thread() {
			public void run() {
				clearConsole();
				printText("Connecting...");

				try {
					connect(port);
				} catch (Exception e) {
					onError("Could not connect to port " + port + "!");
					return;
				}

				File file = new File(binFile);
				InputStream bin = null;
				try {
					bin = new BufferedInputStream(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					onError("The bin file could not be opened!");
					return;
				}

				try {
					restartMojo();
				} catch (InterruptedException e) {
					onError(e.getMessage());
					try {
						bin.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return;
				}

				try {
					while (in.available() > 0)
						in.skip(in.available()); // Flush the buffer

					printText("Loading...");

					if (flash) {
						if (verify)
							out.write('V'); // Write to flash
						else
							out.write('F');
					} else {
						out.write('R'); // Write to FPGA
					}

					if (read(1000) != 'R') {
						onError("Mojo did not respond! Make sure the port is correct.");
						bin.close();
						return;
					}

					int length = (int) file.length();

					byte[] buff = new byte[4];

					for (int i = 0; i < 4; i++) {
						buff[i] = (byte) (length >> (i * 8) & 0xff);
					}

					out.write(buff);

					if (read(1000) != 'O') {
						onError("Mojo did not acknowledge transfer size!");
						bin.close();
						return;
					}

					int num;
					int count = 0;
					int oldCount = 0;
					int percent = length / 100;
					byte[] data = new byte[percent];
					while (true) {
						int avail = bin.available();
						avail = avail > percent ? percent : avail;
						if (avail == 0)
							break;
						int read = bin.read(data, 0, avail);
						out.write(data, 0, read);
						count += read;

						if (count - oldCount > percent) {
							oldCount = count;
							float prog = (float) count / length;
							updateProgress(Math.round(prog * 100.0f));
						}
					}

					updateProgress(100);
					printText("");

					if (read(2000) != 'D') {
						onError("Mojo did not acknowledge the transfer!");
						bin.close();
						return;
					}

					bin.close();

					if (flash && verify) {
						printText("Verifying...");
						bin = new BufferedInputStream(new FileInputStream(file));
						out.write('S');

						int size = (int) (file.length() + 5);

						int tmp;
						if ((tmp = read(1000)) != 0xAA) {
							onError("Flash does not contain valid start byte! Got: "
									+ tmp);
							bin.close();
							return;
						}

						int flashSize = 0;
						for (int i = 0; i < 4; i++) {
							flashSize |= read(1000) << (i * 8);
						}

						if (flashSize != size) {
							onError("File size mismatch!\nExpected " + size
									+ " and got " + flashSize);
							bin.close();
							return;
						}

						count = 0;
						oldCount = 0;
						while ((num = bin.read()) != -1) {
							int d = read(1000);
							if (d != num) {
								onError("Verification failed at byte " + count
										+ " out of " + length + "\nExpected "
										+ num + " got " + d);
								bin.close();
								return;
							}
							count++;
							if (count - oldCount > percent) {
								oldCount = count;
								float prog = (float) count / length;
								updateProgress(Math.round(prog * 100.0f));
							}
						}
						updateProgress(100);
						printText("");
					}

					if (flash) {
						out.write('L');
						if (read(3000) != 'D') {
							onError("Could not load from flash!");
							bin.close();
							return;
						}
					}

					bin.close();
				} catch (IOException | TimeoutException e) {
					onError(e.getMessage());
					return;
				}

				printText("Done");

				try {
					in.close();
					out.close();
				} catch (IOException e) {
					onError(e.getMessage());
					return;
				}

				serialPort.close();
			}
		};
		thread.start();
	}

	private void onError(String e) {
		if (e == null)
			e = "";

		printText("Error: " + e, true);
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		} catch (IOException err) {
			System.err.print(err);
		}
		if (serialPort != null)
			serialPort.close();
	}

	private void connect(String portName) throws Exception {
		if (portName.equals(""))
			throw new Exception("A serial port must be selected!");
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),
					2000);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();

			} else {
				System.out.println("Error: Only serial ports can be used!");
			}
		}
	}

}
