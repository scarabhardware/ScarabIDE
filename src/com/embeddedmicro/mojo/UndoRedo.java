package com.embeddedmicro.mojo;

import java.util.Stack;

import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;

public class UndoRedo implements ExtendedModifyListener {

	private StyledCodeEditor editor;
	private Stack<Edit> undoStack;
	private Stack<Edit> redoStack;

	private class Edit {
		public int start;
		public int length;
		public String text;

		public Edit(int start, int length, String text) {
			this.start = start;
			this.length = length;
			this.text = text;
		}

		public Edit(ExtendedModifyEvent event) {
			start = event.start;
			length = event.length;
			text = event.replacedText;
		}

		@Override
		public String toString() {
			return "Start " + start + " Length " + length + " Text \"" + text
					+ "\"";
		}
	}

	public UndoRedo(StyledCodeEditor editor) {
		this.editor = editor;
		undoStack = new Stack<Edit>();
		redoStack = new Stack<Edit>();
	}

	@Override
	public void modifyText(ExtendedModifyEvent event) {
		Edit undo = null;
		Edit redo = null;

		if (undoStack.size() > 0)
			undo = undoStack.peek();
		if (redoStack.size() > 0)
			redo = redoStack.peek();

		if ((undo == null || event.start != undo.start
				|| event.length != undo.length || !event.replacedText
					.equals(undo.text))
				&& (redo == null || event.start != redo.start
						|| event.length != redo.length || !event.replacedText
							.equals(redo.text))) {
			undoStack.push(new Edit(event));
			redoStack.clear();
		}
	}
	
	private void replace(Stack<Edit> popStack, Stack<Edit> pushStack){
		if (popStack.size() > 0) {
			Edit edit = popStack.pop();
			String replacedText = "";
			if (edit.length > 0)
				replacedText = editor.getText(edit.start, edit.start + edit.length - 1);
			pushStack.push(new Edit(edit.start, edit.text.length(),
					replacedText));
			//System.out.println("Replacing from: " + edit.start+" for: "+edit.length+" with "+edit.text);
			editor.replaceTextRange(edit.start, edit.length, edit.text);
			editor.setCaretOffset(edit.start + edit.text.length());
			editor.update();
		}
	}

	public void undo() {
		replace(undoStack, redoStack);
	}

	public void redo() {
		replace(redoStack, undoStack);
	}
}
