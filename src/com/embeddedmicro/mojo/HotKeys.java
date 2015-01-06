package com.embeddedmicro.mojo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

public class HotKeys implements KeyListener {
	private StyledCodeEditor editor;

	public HotKeys(StyledCodeEditor editor) {
		this.editor = editor;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
			if ((e.stateMask & SWT.SHIFT) == SWT.SHIFT) {
				switch (e.keyCode) {
				case 'z':
					editor.redo(); // CTRL + SHIFT + z
					break;
				case 'f':
					editor.formatText();
					break;
				}
			} else {
				switch (e.keyCode) {
				case 'a':
					editor.selectAll();
					break;
				case 's':
					editor.save();
					break;
				case 'z':
					editor.undo();
					break;
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
