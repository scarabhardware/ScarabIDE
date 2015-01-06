package com.embeddedmicro.mojo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

public class StyledCodeEditor extends StyledText implements ModifyListener {

	private String filePath;
	private boolean edited;
	private CTabItem tabItem;
	private CTabFolder tabFolder;
	private boolean opened;
	private AutoFormatter formatter;
	private UndoRedo undoRedo;
	private String fileName;

	public StyledCodeEditor(Composite parent, int style, CTabFolder tabFolder,
			String file) {
		super(parent, style);
		this.tabFolder = tabFolder;
		
		setAlwaysShowScrollBars(false);
		setBackground(Theme.editorBackgroundColor);
		setForeground(Theme.editorForegroundColor);
		LineHighlighter highligher = new LineHighlighter(this);
		addCaretListener(highligher);
		addExtendedModifyListener(highligher);
		setSelectionBackground(new Color(getDisplay(), 100, 100, 100));
		setSelectionForeground(null);
		
		ErrorChecker errorChecker = new ErrorChecker(this);
		addLineStyleListener(errorChecker);
		addModifyListener(errorChecker);
		
		ToolTipListener tooltips = new ToolTipListener(this, errorChecker);
		addMouseTrackListener(tooltips);
		addMouseMoveListener(tooltips);

		LineStyler styler = new LineStyler(this);
		addLineStyleListener(styler);
		addModifyListener(styler);
		addLineBackgroundListener(new LineBackground());
		setTabs(2);

		setFont(new Font(getDisplay(), "Ubuntu Mono", 12, SWT.NORMAL));

		tabItem = new CTabItem(tabFolder, SWT.CLOSE);

		if (file != null)
			fileName = file.substring(file.lastIndexOf("/") + 1);
		else
			fileName = "Untitled";

		tabItem.setText(fileName);

		tabItem.setControl(this);
		
		formatter = new AutoFormatter(this);
		addVerifyListener(formatter);
		
		opened = openFile(file);

		addModifyListener(this);
		addKeyListener(new HotKeys(this));
		

		undoRedo = new UndoRedo(this);
		addExtendedModifyListener(undoRedo);
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isOpen() {
		return opened;
	}

	public boolean isModifed() {
		return edited;
	}

	public void formatText() {
		formatter.fixIndent();
	}

	public String getFilePath() {
		return filePath;
	}
	
	public void grabFocus(){
		tabFolder.setSelection(tabItem);
	}

	private boolean openFile(String path) {
		String fileContents;
		if (path != null) {
			byte[] encoded;
			try {
				encoded = Files.readAllBytes(Paths.get(path));
				fileContents = StandardCharsets.UTF_8.decode(
						ByteBuffer.wrap(encoded)).toString();
			} catch (IOException e1) {
				System.err.println("Could not open file " + path);
				return false;
			}
		} else {
			fileContents = "";
		}

		filePath = path;
		edited = false;
		setText(fileContents);
		tabFolder.setSelection(tabItem);

		return true;
	}

	public boolean save() {
		if (filePath == null) {
			FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
			dialog.setFilterExtensions(new String[] { "*.v", "*" });
			dialog.setText("Save File");
			String path = dialog.open();
			if (path == null) {
				return false;
			}

			filePath = path;
			tabItem.setText(fileName);
			edited = false;
		}
		try {
			PrintWriter out = new PrintWriter(filePath);
			out.print(getText());
			out.close();
		} catch (FileNotFoundException e1) {
			return false;
		}

		if (edited == true) {
			tabItem.setText(tabItem.getText().substring(1));
		}
		edited = false;
		return true;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (edited == false) {
			tabItem.setText("*" + fileName);
		}
		edited = true;

		// work around for selectAll -> delete bug
		if (getText() == "")
			redraw();
	}

	public void undo() {
		undoRedo.undo();
	}

	public void redo() {
		undoRedo.redo();
	}

	@Override
	public void dispose() {
		super.dispose();
		tabItem.dispose();
	}

}
