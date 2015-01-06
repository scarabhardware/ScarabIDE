package com.embeddedmicro.mojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Project {
	private ArrayList<String> sourceFiles;
	private ArrayList<String> ucfFiles;
	private String topSource;
	private String projectName;
	private String projectFolder;
	private String projectFile;
	private String boardType;
	private boolean open;
	private Tree tree;
	private Shell shell;

	public Project(String name, String folder, String board) {
		this(null);
		projectName = name;
		projectFolder = folder;
		boardType = board;
		projectFile = name + ".mojo";
		open = true;
	}

	public Project(Shell shell) {
		this.shell = shell;
		sourceFiles = new ArrayList<>();
		ucfFiles = new ArrayList<>();
		open = false;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public void addControlListener(ControlListener listener) {
		tree.addControlListener(listener);
	}

	public boolean isOpen() {
		return open;
	}

	public String getFolder() {
		return projectFolder;
	}

	public String getBinFile() {
		File binFile = new File(projectFolder + File.separatorChar + "work"
				+ File.separatorChar + "planAhead" + File.separatorChar
				+ projectName + File.separatorChar + projectName + ".runs"
				+ File.separatorChar + "impl_1" + File.separatorChar
				+ topSource.split("\\.")[0] + ".bin");
		if (binFile.exists())
			return binFile.getAbsolutePath();
		return null;
	}

	private String addFile(String fileName, String folder,
			ArrayList<String> list) {
		File srcFile = new File("base" + File.separatorChar
				+"miniSpartan6+"+ File.separatorChar + "library"+ File.separatorChar
				+"HDMI.v");
		
		File destDir = new File(projectFolder+ File.separatorChar
				+"source");
		try {
            FileUtils.copyFileToDirectory(srcFile, destDir);
        } catch(Exception e) {
        }
			
		
		
		
		//File file = new File(projectFolder + File.separatorChar + folder
		//		+ File.separatorChar + fileName);
	//	try {
		//	if (file.exists()) {
		//		MessageBox b = new MessageBox(shell, SWT.YES | SWT.NO);
		//		b.setText("File Exists");
		//		b.setMessage("File " + fileName + " exists. Overwrite?");
		//		int r = b.open();
		//		if (r != SWT.YES) {
		//			return null;
		//		}
		//		file.delete();
		//	}
		//	if (file.createNewFile()) {
		//		if (!list.contains(fileName))
					list.add("HDMI.v"/*fileName*/);
				updateTree();
				return destDir.getAbsolutePath()+File.separatorChar+"HDMI.v" ;//file.getAbsolutePath();
		 // }
	//	} catch (IOException e) {
	//		System.err.print(e);
	//		return null;
	//	}
		//System.err.println("Could not open file " + file.getAbsolutePath());
		//return null;
	}
	private String addHDMIFile(ArrayList<String> list) {
		File srcFile = new File("base" + File.separatorChar
				+"miniSpartan6+"+ File.separatorChar + "library"+ File.separatorChar
				+"HDMI.v");
		
		File destDir = new File(projectFolder+ File.separatorChar
				+"source");
		try {
            FileUtils.copyFileToDirectory(srcFile, destDir);
        } catch(Exception e) {
        }
			
		
					list.add("HDMI.v"/*fileName*/);
				updateTree();
				return destDir.getAbsolutePath()+File.separatorChar+"HDMI.v" ;//file.getAbsolutePath();
		
	}
	private String addPatternFile(ArrayList<String> list) {
		File srcFile = new File("base" + File.separatorChar
				+"miniSpartan6+"+ File.separatorChar + "library"+ File.separatorChar
				+"videoPattern.v");
		
		File destDir = new File(projectFolder+ File.separatorChar
				+"source");
		try {
            FileUtils.copyFileToDirectory(srcFile, destDir);
        } catch(Exception e) {
        }
		
					list.add("videoPattern.v"/*fileName*/);
				updateTree();
				return destDir.getAbsolutePath()+File.separatorChar+"videoPattern.v" ;//file.getAbsolutePath();
		
	}
	private String addMyFile(String filePath,ArrayList<String> list) {
		
		
		list.add(filePath);
		updateTree();
		return filePath;
		
	}
	public boolean removeSourceFile(String fileName) {
		File file = new File(projectFolder + File.separatorChar + "source"
				+ File.separatorChar + fileName);
		if (file.exists() && !file.delete()) {
			return false;
		}
		boolean ret = sourceFiles.remove(fileName);
		updateTree();
		return ret;
	}

	public boolean removeConstaintFile(String fileName) {
		File file = new File(projectFolder + File.separatorChar + "constraint"
				+ File.separatorChar + fileName);
		if (file.exists() && !file.delete()) {
			return false;
		}
		boolean ret = ucfFiles.remove(fileName);
		updateTree();
		return ret;
	}

	public String HDMILibrary() {
		return addHDMIFile(sourceFiles);
	}
	public String PatternLibrary() {
		return addPatternFile(sourceFiles);
	}
	public String addSourceFile(String fileName) {
		return addFile(fileName, "source", sourceFiles);
	}
	
	public String addMySourceFile(String fileName) {
		return addMyFile(fileName, sourceFiles);
	}
	

	public String addConstraintFile(String fileName) {
		return addFile(fileName, "constraint", ucfFiles);
	}

	public String getSourceFolder() {
		return projectFolder + File.separatorChar + "source";
	}

	public String getConstraintFolder() {
		return projectFolder + File.separatorChar + "constraint";
	}

	public void addExistingSourceFile(String file) {
		sourceFiles.add(file);
	}

	public void addExistingUCFFile(String file) {
		ucfFiles.add(file);
	}

	public boolean setTopFile(String file) {
		if (sourceFiles.contains(file)) {
			topSource = file;
			return true;
		}
		return false;
	}

	public String getTop() {
		return topSource;
	}

	public ArrayList<String> getSourceFiles() {
		return sourceFiles;
	}

	public ArrayList<String> getConstraintFiles() {
		return ucfFiles;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getBoardType() {
		return boardType;
	}

	public void setProjectName(String name) {
		projectName = name;
	}

	public String getProjectFile() {
		return projectFolder + File.separatorChar + projectFile;
	}

	public void setBoardType(String type) {
		boardType = type;
	}

	public void setProjectFolder(String folder) {
		projectFolder = folder;
	}

	public void setProjectFile(String file) {
		projectFile = file;
	}

	private class SortIgnoreCase implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
	}

	public void updateTree() {
		if (open) {
			// tree.clearAll(true);
			tree.removeAll();
			TreeItem project = new TreeItem(tree, SWT.NONE);
			project.setText(projectName);

			TreeItem sourceBranch = new TreeItem(project, SWT.NONE);
			sourceBranch.setText("Source");

			TreeItem ucfBranch = new TreeItem(project, SWT.NONE);
			ucfBranch.setText("Configuration");

			Collections.sort(sourceFiles, new SortIgnoreCase());
			for (String source : sourceFiles) {
				new TreeItem(sourceBranch, SWT.NONE).setText(source);
			}
			Collections.sort(ucfFiles, new SortIgnoreCase());
			for (String ucf : ucfFiles) {
				new TreeItem(ucfBranch, SWT.NONE).setText(ucf);
			}
			tree.showItem(sourceBranch);
		}
	}

	public void openXML(String xmlPath) throws ParseException, IOException {
		open = false;
		sourceFiles.clear();
		ucfFiles.clear();
		topSource = null;
		projectName = null;

		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(xmlPath);
		projectFolder = xmlFile.getParent();
		projectFile = xmlFile.getName();

		Document document;
		try {
			document = (Document) builder.build(xmlFile);
		} catch (JDOMException e) {
			throw new ParseException(e.getMessage());
		}
		Element project = document.getRootElement();

		if (!project.getName().equals(Tags.project)) {
			throw new ParseException("Root element not project tag");
		}

		Attribute projName = project.getAttribute(Tags.Attributes.name);
		if (projName == null) {
			throw new ParseException("Project name is missing");
		}
		projectName = projName.getValue();

		Attribute brdType = project.getAttribute(Tags.Attributes.board);
		if (brdType == null) {
			throw new ParseException("Board type is missing");
		}
		boardType = brdType.getValue();

		final List<Element> list = project.getChildren();
		for (int i = 0; i < list.size(); i++) {
			Element node = list.get(i);

			switch (node.getName()) {
			case Tags.files:
				final List<Element> files = node.getChildren();
				for (int j = 0; j < files.size(); j++) {
					Element file = files.get(j);
					switch (file.getName()) {
					case Tags.source:
						Attribute att = file.getAttribute(Tags.Attributes.top);
						if (att != null && att.getValue().equals("true")) {
							if (topSource != null)
								throw new ParseException(
										"Multiple \"top\" source files");
							topSource = file.getText();
						}
						sourceFiles.add(file.getText());
						break;
					case Tags.ucf:
						ucfFiles.add(file.getText());
						break;
					default:
						throw new ParseException("Unknown tag "
								+ file.getName());
					}
				}
				break;
			default:
				throw new ParseException("Unknown tag " + node.getName());
			}
		}
		open = true;
	}

	public void saveXML() throws IOException {
		saveXML(projectFolder + File.separatorChar + projectFile);
	}

	public void saveXML(String file) throws IOException {
		Element project = new Element(Tags.project);

		project.setAttribute(new Attribute(Tags.Attributes.name, projectName));
		project.setAttribute(new Attribute(Tags.Attributes.board, boardType));
		Document doc = new Document(project);

		Element source = new Element(Tags.files);
		for (String sourceFile : sourceFiles) {
			Element ele = new Element(Tags.source).setText(sourceFile);
			if (sourceFile == topSource)
				ele.setAttribute(new Attribute(Tags.Attributes.top, "true"));
			source.addContent(ele);
		}

		for (String ucfFile : ucfFiles) {
			source.addContent(new Element(Tags.ucf).setText(ucfFile));
		}

		project.addContent(source);

		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());

		xmlOutput.output(doc, new FileWriter(file));
	}
}
