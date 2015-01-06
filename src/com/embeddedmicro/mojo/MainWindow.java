package com.embeddedmicro.mojo;
import java.io.*;  
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class MainWindow implements Callback {
	private static final String VERSION = " : Beta Version";

	protected final Display display = Display.getDefault();
	protected Shell shlMojoLoader;
	protected SashForm sideSashForm;
	protected SashForm bottomSashForm;
	protected CTabFolder tabFolder;
	protected Tree tree;
	protected Menu treeMenu;
	protected Project project;

	private boolean opened;

	private int leftWidth, oldLeftWeight;
	private int bottomHeight, oldBottomWeight;
	private ArrayList<StyledCodeEditor> editors;
	private StyledText console;
	private ProjectBuilder projectBuilder;
	//private MojoLoader loader;
	private ScarabLoader loader;
	
	// private MojoLoader loader;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {
		if (parseCommand(args))
			return;

		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public MainWindow() {
		opened = false;
	}

	private static boolean parseCommand(String[] args) {
		boolean term = false;
		String port = null;
		String binFile = null;
		boolean flash = false;
		boolean verify = false;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-t"))
				term = true;
			else if (args[i].equals("-p") && i < args.length - 1)
				port = args[++i];
			else if (args[i].equals("-b") && i < args.length - 1)
				binFile = args[++i];
			else if (args[i].equals("-f"))
				flash = true;
			else if (args[i].equals("-v"))
				verify = true;
		}
		if (term) {
			if (port == null) {
				System.err
						.println("You must specify a port using the -p flag!");
				return true;
			}
			if (binFile == null) {
				System.err
						.println("You must specify a bin file using the -b flag!");
				return true;
			}

			MojoLoader loader = new MojoLoader(null, null);
			loader.sendBin(port, binFile, flash, verify);
			return true;
		}
		return false;
	}

	/**
	 * Open the window.
	 */
	public void open() {
		createContents();
		shlMojoLoader.open();
		shlMojoLoader.layout();
		while (!shlMojoLoader.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	// private void updatePorts(MenuItem portMenu) {
	// ArrayList<String> ports = MojoLoader.listPorts();
	// for (MenuItem i : items)
	// i.dispose();
	// if (ports.size() != 0) {
	// Object[] array = ports.toArray();
	// String selectedPort = Settings.settings.get(Settings.MOJO_PORT, "");
	//
	// for (int i = 0; i < array.length; i++) {
	// MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
	// menuItem.setText((String) array[i]);
	// menuItem.addSelectionListener(new SelectionListener() {
	// @Override
	// public void widgetDefaultSelected(SelectionEvent event) {
	// widgetSelected(event);
	// }
	//
	// @Override
	// public void widgetSelected(SelectionEvent event) {
	// Settings.settings.put(Settings.MOJO_PORT,
	// ((MenuItem) event.widget).getText());
	// }
	// });
	// if (menuItem.getText().equals(selectedPort))
	// menuItem.setSelection(true);
	// }
	// } else {
	// MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
	// menuItem.setText("No Serial Ports!");
	// }
	// }

	private boolean saveAll(boolean ask) {
		for (StyledCodeEditor editor : editors) {
			switch (saveEditor(editor, ask)) {
			case SWT.YES:
			case SWT.NO:
				continue;
			case SWT.CANCEL:
			case SWT.ERROR:
				return false;
			}
		}
		return true;
	}

	private int saveEditor(StyledCodeEditor editor, boolean ask) {
		if (editor.isModifed()) {
			int returnCode = SWT.YES;
			if (ask) {
				MessageBox dialog = new MessageBox(shlMojoLoader,
						SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
				dialog.setText(editor.getFileName() + " has been modified");
				dialog.setMessage("Do you want to save the changes to "
						+ editor.getFileName() + "?");

				returnCode = dialog.open();
			}

			switch (returnCode) {
			case SWT.YES:
				if (!editor.save()) {
					System.err.println("Could not save file!");
					return SWT.ERROR;
				}
				return SWT.YES;
			case SWT.NO:
				return SWT.NO;
			case SWT.CANCEL:
			default:
				return SWT.CANCEL;
			}
		}
		return SWT.YES;
	}

	private boolean closeEditor(StyledCodeEditor editor) {
		switch (saveEditor(editor, true)) {
		case SWT.YES:
		case SWT.NO:
			editors.remove(editor);
			return true;
		case SWT.CANCEL:
		case SWT.ERROR:
			return false;
		}
		return false;
	}

	private void updatePlanAheadLocation() {
		FileDialog dialog = new FileDialog(shlMojoLoader, SWT.OPEN);
		String result = dialog.open();
		if (result != null) {
			Settings.settings.put(Settings.PLANAHEAD_LOC, result);
		}
	}

	private void loadFonts() {
		int fontsLoaded = 0;
		fontsLoaded = display.loadFont("res" + File.separatorChar
				+ "UbuntuMono-R.ttf") ? fontsLoaded + 1 : fontsLoaded;
		fontsLoaded = display.loadFont("res" + File.separatorChar
				+ "UbuntuMono-RI.ttf") ? fontsLoaded + 1 : fontsLoaded;
		fontsLoaded = display.loadFont("res" + File.separatorChar
				+ "UbuntuMono-B.ttf") ? fontsLoaded + 1 : fontsLoaded;
		fontsLoaded = display.loadFont("res" + File.separatorChar
				+ "UbuntuMono-BI.ttf") ? fontsLoaded + 1 : fontsLoaded;
		if (fontsLoaded != 4) {
			showError("Could not load the fonts! Only " + fontsLoaded
					+ " out of 4 fonts were loaded.");
		}
	}

	private void saveSettings() {
		try {
			Rectangle r = shlMojoLoader.getBounds();
			boolean max = shlMojoLoader.getMaximized();
			Settings.settings.putBoolean(Settings.MAXIMIZED, max);
			if (!max) {
				Settings.settings.putInt(Settings.WINDOW_HEIGHT, r.height);
				Settings.settings.putInt(Settings.WINDOW_WIDTH, r.width);
			}
			int[] weights = sideSashForm.getWeights();
			Settings.settings.putInt(
					Settings.FILE_LIST_WIDTH,
					leftWidth = (int) Math.round((double) sideSashForm
							.getClientArea().width
							* (double) weights[0]
							/ (double) (weights[0] + weights[1])));
			weights = bottomSashForm.getWeights();
			Settings.settings.putInt(
					Settings.CONSOLE_HEIGHT,
					bottomHeight = (int) Math.round((double) bottomSashForm
							.getClientArea().height
							* (double) weights[1]
							/ (double) (weights[0] + weights[1])));

			if (project.isOpen())
				Settings.settings.put(Settings.OPEN_PROJECT,
						project.getProjectFile());
			else
				Settings.settings.remove(Settings.OPEN_PROJECT);

			Settings.settings.flush();
		} catch (BackingStoreException e1) {
			System.err.println("Failed to save settings! " + e1.getMessage());
		}
	}

	/**
	 * Create contents of the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		loadFonts();
		Theme.initColors(display);
		Images.loadImages(display);
		editors = new ArrayList<StyledCodeEditor>();
		shlMojoLoader = new Shell();
		shlMojoLoader.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				if (!saveAll(true)) {
					e.doit = false;
					return;
				}

				saveSettings();
				shlMojoLoader.getImage().dispose();
				Theme.dispose();
			}
		});

		shlMojoLoader.setImage(SWTResourceManager.getImage(MainWindow.class,
				"/resources/icon.png"));
		int height = Settings.settings.getInt(Settings.WINDOW_HEIGHT, 700);
		int width = Settings.settings.getInt(Settings.WINDOW_WIDTH, 1000);
		shlMojoLoader.setSize(width, height);
		shlMojoLoader.setMinimumSize(450, 178);
		shlMojoLoader.setText("Scarab IDE" + VERSION);
		shlMojoLoader.setLayout(new GridLayout(1, false));
		shlMojoLoader.setMaximized(Settings.settings.getBoolean(
				Settings.MAXIMIZED, false));

		shlMojoLoader.setBackground(Theme.windowBackgroundColor);
		shlMojoLoader.setForeground(Theme.windowForgroundColor);

		final Menu m = new Menu(shlMojoLoader, SWT.BAR);
		shlMojoLoader.setMenuBar(m);
	    // create a file menu and add an exit item
	    final MenuItem file = new MenuItem(m, SWT.CASCADE);
	    file.setText("&File");
	    final Menu filemenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    file.setMenu(filemenu);
	    
	    //create a new project
	    final MenuItem newItem = new MenuItem(filemenu, SWT.CASCADE);
	    newItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewProjectDialog dialog = new NewProjectDialog(shlMojoLoader,
						SWT.DIALOG_TRIM);
				shlMojoLoader.setEnabled(false);
				Project p = dialog.open();
				if (p != null) {
					project = p;
					project.setShell(shlMojoLoader);
					project.setTree(tree);
					project.updateTree();
				}
				shlMojoLoader.setEnabled(true);
			}
		});
	    newItem.setText("&New Project ...");
	    
	    //open an existing project
	    final MenuItem openItem = new MenuItem(filemenu, SWT.CASCADE);
	    openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shlMojoLoader, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.mojo", "*" });
				String path = dialog.open();
				if (path != null) {
					try {
						project.openXML(path);
					} catch (ParseException e1) {
						MessageBox box = new MessageBox(shlMojoLoader,
								SWT.ICON_ERROR | SWT.OK);
						box.setText("Error opening file!");
						box.setMessage("Encountered an error while parsing "
								+ path + " the error was: " + e1.getMessage());
						box.open();
					} catch (IOException e1) {
						MessageBox box = new MessageBox(shlMojoLoader,
								SWT.ICON_ERROR | SWT.OK);
						box.setText("Error opening file!");
						box.setMessage("Encountered an error while opening "
								+ path + " the error was: " + e1.getMessage());
						box.open();
					}

					project.updateTree();
				}
			}
		});
	    openItem.setText("&Open Project ...");
	    	
	    
	    
	    //save the current project
	    //TODO this is reduntant, we already save everytime we add a file to the project or build
	    final MenuItem saveItem = new MenuItem(filemenu, SWT.CASCADE);
	    saveItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					project.saveXML();
				} catch (IOException esave) {
					showError("Failed to save project file!");
				}
			}
			});
	    
	    saveItem.setText("&Save Project");
	    
	    //open a single file
	    //TODO when you open a file, it should be added to our tree
	    MenuItem openFileItem = new MenuItem(filemenu, SWT.CASCADE);
		openFileItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shlMojoLoader, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.v", "*" });
				String path = dialog.open();
				if (path != null) {
					openFile(path);
				}
			}
		});
		openFileItem.setText("Open File");

	    //save a single file
		MenuItem saveFileItem = new MenuItem(filemenu, SWT.CASCADE);
		saveFileItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((StyledCodeEditor) (tabFolder.getSelection().getControl()))
						.save();
			}
		});
		saveFileItem.setText("Save File");
		
		
/*	    final Menu submenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    openItem.setMenu(submenu);
	    final MenuItem childItem = new MenuItem(submenu, SWT.PUSH);
	    childItem.setText("&Child\tCTRL+C");
	    childItem.setAccelerator(SWT.CTRL + 'C');
	    final MenuItem dialogItem = new MenuItem(submenu, SWT.PUSH);
	    dialogItem.setText("&Dialog\tCTRL+D");
	    dialogItem.setAccelerator(SWT.CTRL + 'D');
*/
	    
	    final MenuItem separator = new MenuItem(filemenu, SWT.SEPARATOR);
	    final MenuItem exitItem = new MenuItem(filemenu, SWT.PUSH);
	    exitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
	    });
	    exitItem.setText("E&xit");
	    
	    // create an edit menu and add cut copy and paste items
	    final MenuItem edit = new MenuItem(m, SWT.CASCADE);
	    edit.setText("&Edit");
	    final Menu editmenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    edit.setMenu(editmenu);
	    
	    final MenuItem undoItem = new MenuItem(editmenu, SWT.PUSH);
	    undoItem.setText("&Undo\tCTRL+Z");
	    undoItem.setAccelerator(SWT.CTRL + 'Z');

	    final MenuItem redoItem = new MenuItem(editmenu, SWT.PUSH);
	    redoItem.setText("&Redo\tCTRL+Y");
	    redoItem.setAccelerator(SWT.CTRL + 'Y');

	    final MenuItem separator2 = new MenuItem(editmenu, SWT.SEPARATOR);
	    
	    final MenuItem cutItem = new MenuItem(editmenu, SWT.PUSH);
	    cutItem.setText("&Cut\tCTRL+X");
	    cutItem.setAccelerator(SWT.CTRL + 'X');

	    final MenuItem copyItem = new MenuItem(editmenu, SWT.PUSH);
	    copyItem.setText("Co&py\tCTRL+C");
	    copyItem.setAccelerator(SWT.CTRL + 'X');

	    final MenuItem pasteItem = new MenuItem(editmenu, SWT.PUSH);
	    pasteItem.setText("&Paste\tCTRL+V");
	    pasteItem.setAccelerator(SWT.CTRL + 'V');

	    //TODO there should be one method we pass library paths to or something for loading these
	    //create an options menu and add menu items
	    final MenuItem Library = new MenuItem(m, SWT.CASCADE);
	    Library.setText("&Library");
	    final Menu Librarymenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    Library.setMenu(Librarymenu);
	    
	//    final Menu submenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	//    Librarymenu.setMenu(submenu);
	//    final MenuItem childItem = new MenuItem(Librarymenu, SWT.PUSH);
	//    childItem.setText("&Child\tCTRL+C");
	    
	    final MenuItem audioItem = new MenuItem(Librarymenu, SWT.CASCADE);
	    audioItem.setText("&Audio");
	    
	    final Menu submenu2 = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    audioItem.setMenu(submenu2);
	    final MenuItem sigmaDelta = new MenuItem(submenu2, SWT.CHECK);
	    sigmaDelta.setText("&Sigma-Delta");
	    final MenuItem toneGenerator = new MenuItem(submenu2, SWT.CHECK);
	    toneGenerator.setText("&Tone Generator");
	    
	    final MenuItem commItem = new MenuItem(Librarymenu, SWT.CASCADE);
	    commItem.setText("&Communication");
	    
	    final Menu submenu3 = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    commItem.setMenu(submenu3);
	    final MenuItem USBLink = new MenuItem(submenu3, SWT.CHECK);
	    USBLink.setText("&USB Link");
	    final MenuItem serialComm = new MenuItem(submenu3, SWT.CHECK);
	    serialComm.setText("&Serial Comm");
	    
	    final MenuItem memoryItem = new MenuItem(Librarymenu, SWT.CASCADE);
	    memoryItem.setText("&Memory");
	    
	    final Menu submenu4 = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    memoryItem.setMenu(submenu4);
	    final MenuItem sdram = new MenuItem(submenu4, SWT.CHECK);
	    sdram.setText("&SDRAM");
	    final MenuItem microSD = new MenuItem(submenu4, SWT.CHECK);
	    microSD.setText("&micro SD Card");
	    
	    final MenuItem softProcessorItem = new MenuItem(Librarymenu, SWT.CASCADE);
	    softProcessorItem.setText("&Soft Processor");
	    
	    final MenuItem vedioItem = new MenuItem(Librarymenu, SWT.CASCADE);
	    vedioItem.setText("&Vedio");
	    
	    final Menu submenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    vedioItem.setMenu(submenu);
	    final MenuItem camera = new MenuItem(submenu, SWT.CHECK);
	    camera.setText("&Camera");
	    final MenuItem hdmi = new MenuItem(submenu, SWT.CHECK);
	    hdmi.setSelection(false);
		hdmi.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("HDMI Library");
				project.HDMILibrary();
				try {
					project.saveXML();
				} catch (IOException e1) {
				}
				
			}		
		});
	    hdmi.setText("&HDMI");

	    final MenuItem patternGenerator = new MenuItem(submenu, SWT.CHECK);
	    patternGenerator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Video Pattern Library");
				project.PatternLibrary();
				try {
					project.saveXML();
				} catch (IOException e1) {
				}
				
			}		
		});
	    patternGenerator.setText("&Pattern Generator");
	    

	   

	    /*    final MenuItem optionsseparator = new MenuItem(optionsmenu,
	        SWT.SEPARATOR);
	    final MenuItem radioItem1 = new MenuItem(optionsmenu, SWT.RADIO);
	    radioItem1.setText("Radio &One");
	    final MenuItem radioItem2 = new MenuItem(optionsmenu, SWT.RADIO);
	    radioItem2.setText("Radio &Two");
*/
	    //create a Window menu and add Child item
	    final MenuItem window = new MenuItem(m, SWT.CASCADE);
	    window.setText("&Example");
	    final Menu windowmenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    window.setMenu(windowmenu);
	    final MenuItem ArduinoEmulator = new MenuItem(windowmenu, SWT.PUSH);
	    ArduinoEmulator.setText("&Arduino Emulator");
	    final MenuItem games = new MenuItem(windowmenu, SWT.PUSH);
	    games.setText("&Games Emulator");
	    
	    final MenuItem maxItem = new MenuItem(windowmenu, SWT.PUSH);
	    maxItem.setText("&Logic Analyzer");
	    
	    final MenuItem StereoCameraItem = new MenuItem(windowmenu, SWT.PUSH);
	    StereoCameraItem.setText("&Stereo Vision Camera");
	    
	    
	    final MenuItem Tools = new MenuItem(m, SWT.CASCADE);
	    Tools.setText("&Tools");
	    final Menu toolsmenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    Tools.setMenu(toolsmenu);
	    final MenuItem LogicInterfacetItem = new MenuItem(toolsmenu, SWT.PUSH);
	    LogicInterfacetItem.setText("&Logic Analyzer GUI");
	    final MenuItem serialItem = new MenuItem(toolsmenu, SWT.PUSH);
	    serialItem.setText("&Serial Console");
	    final MenuItem hardwareItem = new MenuItem(toolsmenu, SWT.PUSH);
	    hardwareItem.setText("&Hardware");
	    
	    //create a Settings menu
	    final MenuItem SettingsMenuItem = new MenuItem(m, SWT.CASCADE);
	    SettingsMenuItem.setText("&Settings");    
	    final Menu SettingsMenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    SettingsMenuItem.setMenu(SettingsMenu);
	    
	    MenuItem PlanaheadLocationItem = new MenuItem(SettingsMenu, SWT.PUSH);
		PlanaheadLocationItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePlanAheadLocation();
			}
		});
		PlanaheadLocationItem.setText("&Set PlanAhead Location");
	    

	    // create a Help menu and add an about item
	    final MenuItem help = new MenuItem(m, SWT.CASCADE);
	    help.setText("&Help");
	    final Menu helpmenu = new Menu(shlMojoLoader, SWT.DROP_DOWN);
	    help.setMenu(helpmenu);
	    final MenuItem tutorialItem = new MenuItem(helpmenu, SWT.PUSH);
	    tutorialItem.setText("&Tutorials");
	    final MenuItem aboutItem = new MenuItem(helpmenu, SWT.PUSH);
	    aboutItem.setText("&About");
	
/*		Menu menu = new Menu(shlMojoLoader, SWT.BAR);
		shlMojoLoader.setMenuBar(menu);

		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");

		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);

		MenuItem mntmNewProject = new MenuItem(menu_1, SWT.NONE);
		mntmNewProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewProjectDialog dialog = new NewProjectDialog(shlMojoLoader,
						SWT.DIALOG_TRIM);
				shlMojoLoader.setEnabled(false);
				Project p = dialog.open();
				if (p != null) {
					project = p;
					project.setShell(shlMojoLoader);
					project.setTree(tree);
					project.updateTree();
				}
				shlMojoLoader.setEnabled(true);
			}
		});
		mntmNewProject.setText("New Project...");

		MenuItem mntmOpenProject = new MenuItem(menu_1, SWT.NONE);
		mntmOpenProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shlMojoLoader, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.mojo", "*" });
				String path = dialog.open();
				if (path != null) {
					try {
						project.openXML(path);
					} catch (ParseException e1) {
						MessageBox box = new MessageBox(shlMojoLoader,
								SWT.ICON_ERROR | SWT.OK);
						box.setText("Error opening file!");
						box.setMessage("Encountered an error while parsing "
								+ path + " the error was: " + e1.getMessage());
						box.open();
					} catch (IOException e1) {
						MessageBox box = new MessageBox(shlMojoLoader,
								SWT.ICON_ERROR | SWT.OK);
						box.setText("Error opening file!");
						box.setMessage("Encountered an error while opening "
								+ path + " the error was: " + e1.getMessage());
						box.open();
					}

					project.updateTree();
				}
			}
		});
		mntmOpenProject.setText("Open Project...");

		MenuItem mntmOpenFile = new MenuItem(menu_1, SWT.NONE);
		mntmOpenFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shlMojoLoader, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.v", "*" });
				String path = dialog.open();
				if (path != null) {
					openFile(path);
				}
			}
		});
		mntmOpenFile.setText("Open File...");

		MenuItem mntmSaveFile = new MenuItem(menu_1, SWT.NONE);
		mntmSaveFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((StyledCodeEditor) (tabFolder.getSelection().getControl()))
						.save();
			}
		});
		mntmSaveFile.setText("Save");

		MenuItem mntmLibrary = new MenuItem(menu, SWT.CASCADE);
		mntmLibrary.setText("Librarry");

		Menu menu_L = new Menu(mntmLibrary);
		mntmLibrary.setMenu(menu_L);

		final MenuItem mntmVedio = new MenuItem(menu_L, SWT.POP_UP);
		mntmVedio.setSelection(true);
		mntmVedio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Updating Menu");
				System.out.println(mntmVedio.getSelection());
				
				if (project.isOpen()) {
				//	NewSourceDialog dialog = new NewSourceDialog(shlMojoLoader,
				//			SWT.DIALOG_TRIM);
					FileDialog dialog = new FileDialog(shlMojoLoader, SWT.OPEN);
					dialog.setFilterExtensions(new String[] { "*.v", "*" });
					
					shlMojoLoader.setEnabled(false);
					String path = dialog.open();
					System.out.println(path);
					if (path != null) {
						String filePath = null;
						
						
							if ((filePath = project
									.addMySourceFile(path)) == null)
								showError("Could not create new source file!");
							
						
						

						if (filePath != null)
							openFile(filePath);
					}
					try {
						project.saveXML();
					} catch (IOException e1) {
						showError("Failed to save project file!");
					}
					shlMojoLoader.setEnabled(true);
				} else {
					showError("A project must be open to add a new file.");
				}
			}
			// shlMojoLoader.setEnabled(true);

				
				
		});
		mntmVedio.setText("Vedio");

		
		

		MenuItem mntmSettings = new MenuItem(menu, SWT.CASCADE);
		mntmSettings.setText("Settings");

		final Menu menu_2 = new Menu(mntmSettings);
		mntmSettings.setMenu(menu_2);


		MenuItem mntmSerialPort = new MenuItem(menu_2, SWT.NONE);
		mntmSerialPort.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<String> p = MojoLoader.listPorts();
				String[] ports = p.toArray(new String[p.size()]);
				if (ports.length > 0) {
					SerialPortSelector dialog = new SerialPortSelector(
							shlMojoLoader, ports);
					String port = dialog.open();
					System.out.println(port);
					if (port != null)
						Settings.settings.put(Settings.MOJO_PORT, port);
				} else {
					MessageBox box = new MessageBox(shlMojoLoader,
							SWT.ICON_ERROR | SWT.OK);
					box.setText("No Serial Ports Detected!");
					box.setMessage("No serial ports were detected. Make sure your Mojo is connected and the drivers are loaded.");
					box.open();
				}
			}
		});
		mntmSerialPort.setText("Serial Port");


		MenuItem mntmPlanaheadLocation = new MenuItem(menu_2, SWT.NONE);
		mntmPlanaheadLocation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePlanAheadLocation();
			}
		});
		mntmPlanaheadLocation.setText("PlanAhead Location");
*/
	    
		Composite composite = new Composite(shlMojoLoader, SWT.NONE);
		composite.setBackground(Theme.windowBackgroundColor);
		composite.setForeground(Theme.windowForgroundColor);
		RowLayout rl_composite = new RowLayout(SWT.HORIZONTAL);
		composite.setLayout(rl_composite);

		CustomButton newbtn = new CustomButton(composite, SWT.NONE);
		newbtn.setIcon(Images.fileIcon);
		newbtn.setToolTipText("New File");
		newbtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (project.isOpen()) {
					NewSourceDialog dialog = new NewSourceDialog(shlMojoLoader,
							SWT.DIALOG_TRIM);
					shlMojoLoader.setEnabled(false);
					SourceFile file = dialog.open();
					System.out.println(file.fileName);
					if (file != null) {
						String filePath = null;
						switch (file.type) {
						case SourceFile.SOURCE:
							if ((filePath = project
									.addSourceFile(file.fileName)) == null)
								showError("Could not create new source file!");
							break;
						case SourceFile.CONSTRAINT:
							if ((filePath = project
									.addConstraintFile(file.fileName)) == null)
								showError("Could not create constraint file!");
							break;
						}

						if (filePath != null)
							openFile(filePath);
					}
					try {
						project.saveXML();
					} catch (IOException e) {
						showError("Failed to save project file!");
					}
					shlMojoLoader.setEnabled(true);
				} else {
					showError("A project must be open to add a new file.");
				}
			}
		});

		CustomButton buildbtn = new CustomButton(composite, SWT.NONE);
		buildbtn.setIcon(Images.buildIcon);
		buildbtn.setToolTipText("Build Project");
		buildbtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!project.isOpen()) {
					showError("A project must be opened before you can build it!");
					return;
				}
				if (projectBuilder.isBuilding()) {
					showError("Your project is already building!");
					return;
				}
				if (loader.isLoading()) {
					showError("You can't build your project while the Mojo is being programmed!");
					return;
				}
				if (!saveAll(false)) {
					showError("Could not save all open tabs before build!");
					MessageBox box = new MessageBox(shlMojoLoader, SWT.YES
							| SWT.NO);
					box.setMessage("Continue with the build anyway?");
					box.setText("All files not saved...");
					if (box.open() != SWT.YES) {
						return;
					}
				}
				projectBuilder.buildProject(project);
			}
		});

		//TODO check for already loading process
		//TODO move implementation, call of xc3sprog out to loader class
		CustomButton loadbtn = new CustomButton(composite, SWT.NONE);
		loadbtn.setIcon(Images.loadIcon);
		loadbtn.setToolTipText("Load to Scarab");
		loadbtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				
				
				if (!project.isOpen()) {
					showError("A project must be opened before you can load it!");
					return;
				}
				if (projectBuilder.isBuilding()) {
					showError("You must wait for your design to finish building before loading it.");
					return;
				}
				if (loader.isLoading()) {
					showError("The board is already being programmed!");
					return;
				}
				String binFile = project.getBinFile();
					
				if (binFile == null) {
					showError("Could not find the bin file! Make sure the project is built.");
					return;
				}
				
				console.setText("Using bin file: "+ binFile);
				
				String port = "ftdi";
				
				loader.sendBin(port, binFile, true, true);
			}
			
		});

		bottomSashForm = new SashForm(shlMojoLoader, SWT.VERTICAL);
		bottomSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		bottomSashForm.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int height = bottomSashForm.getClientArea().height;
				int[] weights = bottomSashForm.getWeights();

				double perBottom = (double) bottomHeight / (double) height;

				if (perBottom < 0.8) {
					weights[1] = (int) (perBottom * 1000.0);
					weights[0] = 1000 - weights[1];
				} else {
					weights[1] = 800;
					weights[0] = 200;
				}

				// oldWeights must be set before form.setWeights
				oldBottomWeight = weights[0];
				bottomSashForm.setWeights(weights);
			}
		});
		bottomSashForm.setBackground(Theme.windowBackgroundColor);

		sideSashForm = new SashForm(bottomSashForm, SWT.NONE);
		sideSashForm.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int width = sideSashForm.getClientArea().width;
				int[] weights = sideSashForm.getWeights();

				double perLeft = (double) leftWidth / (double) width;

				if (perLeft < 0.8) {
					weights[0] = (int) (perLeft * 1000.0);
					weights[1] = 1000 - weights[0];
				} else {
					weights[0] = 800;
					weights[1] = 200;
				}

				// oldWeights must be set before form.setWeights
				oldLeftWeight = weights[0];
				sideSashForm.setWeights(weights);
			}
		});
		sideSashForm.setBackground(Theme.windowBackgroundColor);

		tree = new Tree(sideSashForm, SWT.NONE);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				Point point = new Point(event.x, event.y);
				TreeItem item = tree.getItem(point);
				if (item != null) {
					if (item.getItemCount() > 0) {
						if (item.getExpanded()) {
							item.setExpanded(false);
						} else {
							item.setExpanded(true);
						}
					} else {
						if (item.getParentItem().getText().equals("Source"))
							openFile(project.getFolder() + File.separatorChar
									+ "source" + File.separatorChar
									+ item.getText());
						else if (item.getParentItem().getText()
								.equals("Constraint"))
							openFile(project.getFolder() + File.separatorChar
									+ "constraint" + File.separatorChar
									+ item.getText());
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent event) {
				if (event.button == 3) { // right click
					Point point = new Point(event.x, event.y);
					final TreeItem item = tree.getItem(point);
					for (MenuItem i : treeMenu.getItems())
						i.dispose();
					if (item != null) {
						MenuItem mi = new MenuItem(treeMenu, SWT.NONE);
						mi.setText("Remove " + item.getText());
						mi.setData(item.getText());
						mi.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								if (item.getParentItem().getText()
										.equals("Source")) {
									if (!project
											.removeSourceFile((String) ((MenuItem) e
													.getSource()).getData()))
										showError("Could not remove file!");
								} else if (item.getParentItem().getText()
										.equals("Constraint")) {
									if (!project
											.removeConstaintFile((String) ((MenuItem) e
													.getSource()).getData()))
										showError("Could not remove file!");
								}
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});
					}
				}
			}
		});
		tree.setBackground(Theme.editorBackgroundColor);
		tree.setForeground(Theme.editorForegroundColor);
		tree.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) != 0) {
					GC gc = event.gc;

					Rectangle rect = event.getBounds();
					Color foreground = gc.getForeground();
					Color background = gc.getBackground();
					if (tree.isFocusControl())
						gc.setBackground(Theme.treeSelectedFocusedColor);
					else
						gc.setBackground(Theme.treeSelectedColor);
					gc.setForeground(Theme.editorForegroundColor);
					gc.fillRectangle(rect);
					// restore colors for subsequent drawing
					gc.setForeground(foreground);
					gc.setBackground(background);
					event.detail &= ~SWT.SELECTED;
				}
			}
		});

		tree.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int[] weights = sideSashForm.getWeights();
				if (oldLeftWeight != weights[0]) {
					oldLeftWeight = weights[0];
					leftWidth = (int) Math.round((double) sideSashForm
							.getClientArea().width
							* (double) weights[0]
							/ (double) (weights[0] + weights[1]));
				}

				weights = bottomSashForm.getWeights();

				if (oldBottomWeight != weights[1]) {
					oldBottomWeight = weights[1];
					bottomHeight = (int) Math.round((double) bottomSashForm
							.getClientArea().height
							* (double) weights[1]
							/ (double) (weights[0] + weights[1]));
				}
			}
		});

		treeMenu = new Menu(shlMojoLoader, SWT.POP_UP);
		tree.setMenu(treeMenu);

		project = new Project(shlMojoLoader);
		project.setTree(tree);
		String oldProject = Settings.settings.get(Settings.OPEN_PROJECT, null);
		if (oldProject != null)
			try {
				project.openXML(oldProject);
				project.updateTree();
			} catch (ParseException | IOException e1) {
				System.err.println("Error: could not open old project file "
						+ oldProject);
			}

		tabFolder = new CTabFolder(sideSashForm, SWT.NULL);
		tabFolder.setSimple(false);
		tabFolder.setDragDetect(true);

		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent event) {
				int tab = editors.indexOf(((CTabItem) event.item).getControl());
				StyledCodeEditor editor = editors.get(tab);
				if (!closeEditor(editor)) {
					event.doit = false;
				}
			}
		});
		tabFolder.setBackground(Theme.tabBackgroundColor);
		tabFolder.setForeground(Theme.tabForegroundColor);
		tabFolder.setSelectionBackground(Theme.tabSelectedBackgroundColor);
		tabFolder.setSelectionForeground(Theme.tabSelectedForegroundColor);

		console = new StyledText(bottomSashForm, SWT.READ_ONLY | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		console.setBackground(Theme.consoleBackgroundColor);
		console.setForeground(Theme.consoleForgoundColor);
		console.setAlwaysShowScrollBars(false);
		console.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				console.setTopIndex(console.getLineCount() - 1);
			}
		});
		console.setFont(new Font(display, "Ubuntu Mono", 12, SWT.NORMAL));
		bottomSashForm.setWeights(new int[] { 8, 2 });

		openFile(null);

		leftWidth = Settings.settings.getInt(Settings.FILE_LIST_WIDTH, 200);
		bottomHeight = Settings.settings.getInt(Settings.CONSOLE_HEIGHT, 200);

		projectBuilder = new ProjectBuilder(display, shlMojoLoader, console);
		loader = new ScarabLoader(display, console);
	}

	private void showError(String error) {
		MessageBox b = new MessageBox(shlMojoLoader, SWT.OK | SWT.ERROR);
		b.setText("Error!");
		b.setMessage(error);
		b.open();
	}

	private boolean openFile(String path) {
		for (StyledCodeEditor editor : editors) {
			if (editor.getFilePath() != null
					&& editor.getFilePath().equals(path)) {
				editor.grabFocus();
				return true;
			}
		}

		final StyledCodeEditor codeEditor = new StyledCodeEditor(tabFolder,
				SWT.V_SCROLL | SWT.MULTI | SWT.H_SCROLL, tabFolder, path);

		if (codeEditor.isOpen()) {
			int size = editors.size();
			if (size == 1 && !opened && !editors.get(0).isModifed()) {
				editors.get(0).dispose();
				editors.remove(0);
			}
			if (size > 0)
				opened = true;
			editors.add(codeEditor);
			return true;
		}
		return false;
	}

	private void setUI(boolean active) {
		/*
		 * btnLoad.setEnabled(active); btnOpen.setEnabled(active);
		 * btnStore.setEnabled(active);
		 * btnVerify.setEnabled(btnStore.getSelection() && active);
		 * btnClear.setEnabled(active); combo.setEnabled(active);
		 * text.setEnabled(active);
		 */
	}

	@Override
	public void onSuccess() {
		display.asyncExec(new Runnable() {
			public void run() {
				setUI(true);
			}
		});
	}

	@Override
	public void onError(final String error) {
		System.out.println(error);

		display.asyncExec(new Runnable() {
			public void run() {
				// Message with ok and cancel button and info icon
				MessageBox dialog = new MessageBox(shlMojoLoader,
						SWT.ICON_ERROR | SWT.OK);
				dialog.setText("Error!");
				dialog.setMessage(error);
				dialog.open();

				setUI(true);
			}
		});
	}
}
