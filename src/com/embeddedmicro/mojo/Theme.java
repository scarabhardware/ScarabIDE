package com.embeddedmicro.mojo;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class Theme {
	public static boolean set;
	public static Color moduleColor;
	public static Color keyWordColor;
	public static Color valueColor;
	public static Color varTypeColor;
	public static Color operatorColor;
	public static Color commentColor;
	public static Color stringColor;
	public static Color instantiationColor;
	public static Color editorBackgroundColor;
	public static Color editorForegroundColor;
	public static Color bulletTextColor;
	public static Color highlightedLineColor;
	public static Color tabBackgroundColor;
	public static Color tabForegroundColor;
	public static Color tabSelectedForegroundColor;
	public static Color tabSelectedBackgroundColor;
	public static Color windowBackgroundColor;
	public static Color windowForgroundColor;
	public static Color treeSelectedFocusedColor;
	public static Color treeSelectedColor;
	public static Color toolBarHoverColor;
	public static Color toolBarClickColor;
	public static Color consoleBackgroundColor;
	public static Color consoleForgoundColor;
	public static Color errorTextColor;

	public static void initColors(Display display) {
		moduleColor = new Color(display, 220, 0, 0);
		keyWordColor = new Color(display, 0, 0, 150);
		valueColor = new Color(display, 0, 150, 250);
		varTypeColor = new Color(display, 150, 0, 150);
		operatorColor = new Color(display, 150, 100, 0);
		commentColor = new Color(display, 0, 0, 0);
		stringColor = new Color(display, 0, 0, 0);
		instantiationColor = new Color(display, 255, 100, 100);
		editorBackgroundColor = new Color(display, 255, 255, 255);
		editorForegroundColor = new Color(display, 0, 100, 80);
		bulletTextColor = new Color(display, 0, 0, 0);
		highlightedLineColor = new Color(display, 0, 230, 200);
		tabSelectedForegroundColor = bulletTextColor;
		tabSelectedBackgroundColor = editorBackgroundColor;
		windowBackgroundColor = new Color(display, 0, 164, 120);
		windowForgroundColor = new Color(display, 0, 255, 255);
		tabBackgroundColor = new Color(display, 0, 120, 100);
		tabForegroundColor = windowForgroundColor;
		treeSelectedFocusedColor = new Color(display, 0, 188, 120);
		treeSelectedColor = highlightedLineColor;
		toolBarHoverColor = treeSelectedFocusedColor;
		toolBarClickColor = new Color(display, 0, 160, 35);
		consoleBackgroundColor = editorBackgroundColor;
		consoleForgoundColor = editorForegroundColor;
		errorTextColor = new Color(display, 0, 120, 100);
		set = true;
	}

	public static void dispose() {
		moduleColor.dispose();
		keyWordColor.dispose();
		valueColor.dispose();
		varTypeColor.dispose();
		operatorColor.dispose();
		commentColor.dispose();
		stringColor.dispose();
		instantiationColor.dispose();
		editorBackgroundColor.dispose();
		editorForegroundColor.dispose();
		bulletTextColor.dispose();
		highlightedLineColor.dispose();
		tabSelectedBackgroundColor.dispose();
		tabSelectedForegroundColor.dispose();
		windowBackgroundColor.dispose();
		windowForgroundColor.dispose();
		tabBackgroundColor.dispose();
		tabForegroundColor.dispose();
		treeSelectedFocusedColor.dispose();
		treeSelectedColor.dispose();
		toolBarHoverColor.dispose();
		toolBarClickColor.dispose();
		consoleBackgroundColor.dispose();
		consoleForgoundColor.dispose();
		errorTextColor.dispose();
		set = false;
	}
}
