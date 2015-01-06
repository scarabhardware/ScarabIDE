package com.embeddedmicro.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.embeddedmicro.mojo.boards.Board;
import com.embeddedmicro.mojo.boards.Boards;

public class NewProjectDialog extends Dialog {

	protected Project result;
	protected Shell shlNewProject;
	private Text projFolder;
	private Text projName;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public NewProjectDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Project open() {
		createContents();
		shlNewProject.open();
		shlNewProject.layout();
		Display display = getParent().getDisplay();
		while (!shlNewProject.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlNewProject = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlNewProject.setSize(461, 378);
		shlNewProject.setText("New Project");
		shlNewProject.setLayout(new GridLayout(3, false));

		Label lblProjectName = new Label(shlNewProject, SWT.NONE);
		lblProjectName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblProjectName.setText("Project Name:");

		projName = new Text(shlNewProject, SWT.BORDER);
		GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1);
		gd_text_1.widthHint = 341;
		projName.setLayoutData(gd_text_1);

		Label lblProjectFolder = new Label(shlNewProject, SWT.NONE);
		lblProjectFolder.setText("Location:");

		projFolder = new Text(shlNewProject, SWT.BORDER);
		projFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Button btnNewButton = new Button(shlNewProject, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(shlNewProject);
				// dialog.setFilterPath(string)
				String path = dialog.open();
				if (path != null) {
					projFolder.setText(path);
				}
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		btnNewButton.setText("Browse...");

		Label lblBoard = new Label(shlNewProject, SWT.NONE);
		lblBoard.setText("Board:");

		final Combo combo = new Combo(shlNewProject, SWT.READ_ONLY);

		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1));
		String[] boards = new String[Boards.boards.size()];
		for (int i = 0; i < boards.length; i++) {
			boards[i] = Boards.boards.get(i).getName();
		}
		combo.setItems(boards);
		combo.select(0);
		new Label(shlNewProject, SWT.NONE);

		Button btnNewButton_2 = new Button(shlNewProject, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlNewProject.close();
			}
		});
		GridData gd_btnNewButton_2 = new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1);
		gd_btnNewButton_2.widthHint = 80;
		btnNewButton_2.setLayoutData(gd_btnNewButton_2);
		btnNewButton_2.setText("Cancel");

		Button btnNewButton_1 = new Button(shlNewProject, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String projectName = projName.getText();
				String projectFolder = projFolder.getText()
						+ File.separatorChar + projectName;
				String boardType = combo.getText();
				Project project = new Project(projectName, projectFolder,
						boardType);

				Board board = Boards.getByName(boardType);
				if (board == null) {
					showError("Board type is invalid!");
				}

				File srcDir = new File("base" + File.separatorChar
						+ board.getBaseProjectName());
				if (!srcDir.exists()) {
					showError("Could not find starter code!");
					return;
				}

				File destDir = new File(projectFolder);
				if (!destDir.exists() || !destDir.isDirectory()) {
					boolean success = destDir.mkdir();
					if (!success) {
						showError("Could not create project folder!");
						return;
					}
				}

				try {
					FileUtils.copyDirectory(srcDir, destDir);
				} catch (IOException e1) {
					showError(e1.getMessage());
					return;
				}

				//System.out.println("destDir.getAbsolutePath()");
				//System.out.println(destDir.getAbsolutePath());

				File topFile = new File(destDir.getAbsolutePath()
						+ File.separatorChar + "source" + File.separatorChar
						+ "top.v");
				
				/*if (!topFile.exists()) {
					showError("Could not locate \"mojo_top.v\"");
					return;
				}
				File newTopFile = new File(topFile.getParent()
						+ File.separatorChar + projectName + "_top.v");

				if (!topFile.renameTo(newTopFile)) {
					showError("Could not rename top file!");
					return;
				}*/

				File source = new File(destDir.getAbsolutePath()
						+ File.separatorChar + "source");
				if (!source.exists()) {
					showError("Source folder is missing!");
				}
				for (String file : source.list()) {
					project.addExistingSourceFile(file);
					if (file.equals(topFile.getName())) //set to newTopFile if above is uncommented
						project.setTopFile(file);
				}

				File constraint = new File(destDir.getAbsolutePath()
						+ File.separatorChar + "constraint");
				if (!constraint.exists()) {
					showError("Constraints folder is missing!");
				}
				for (String file : constraint.list()) {
					project.addExistingUCFFile(file);
				}

				try {
					project.saveXML();

				} catch (IOException e1) {
					showError(e1.getMessage());
					return;
				}

				result = project;
				shlNewProject.close();
			}
		});
		GridData gd_btnNewButton_1 = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1);
		gd_btnNewButton_1.widthHint = 80;
		btnNewButton_1.setLayoutData(gd_btnNewButton_1);
		btnNewButton_1.setText("Create");

		shlNewProject.pack();
	}

	private void showError(String error) {
		MessageBox b = new MessageBox(shlNewProject, SWT.OK | SWT.ERROR);
		b.setText("Could not create project");
		b.setMessage("The project could not be created."
				+ System.lineSeparator() + error);
		b.open();
	}

}
