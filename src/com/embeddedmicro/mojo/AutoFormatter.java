package com.embeddedmicro.mojo;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import com.embeddedmicro.mojo.parser.Verilog2001Lexer;
import com.embeddedmicro.mojo.parser.Verilog2001Parser;

public class AutoFormatter implements VerifyListener {
	private StyledCodeEditor editor;
	private IndentListener indentListener;

	public AutoFormatter(StyledCodeEditor editor) {
		this.editor = editor;
		indentListener = new IndentListener();
	}

	public void updateIndentList() {
		ANTLRInputStream input = new ANTLRInputStream(editor.getText());
		Verilog2001Lexer lexer = new Verilog2001Lexer(input);
		lexer.removeErrorListeners();
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		Verilog2001Parser parser = new Verilog2001Parser(tokens);
		indentListener.initWalk(editor, tokens);
		parser.addParseListener(indentListener);
		parser.removeErrorListeners();
		parser.source_text();
	}

	public void fixIndent() {
		StringBuilder builder = new StringBuilder();
		updateIndentList();
		String[] lines = editor.getText().split("(?:\r)?\n");
		for (int lineNum = 0; lineNum < lines.length; lineNum++) {
			int indent = indentListener.getTabs(lineNum);
			builder.append(appendTabs(lines[lineNum], indent)
					+ System.lineSeparator());
		}
		int seperatorLen = System.lineSeparator().length();
		builder.delete(builder.length() - seperatorLen, builder.length());
		editor.replaceTextRange(0, editor.getCharCount(), builder.toString());
	}

	private String appendTabs(String line, int tabs) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < tabs; i++) {
			text.append('\t');
		}
		text.append(line.trim());
		return text.toString();
	}

	private void unindent(VerifyEvent e) {

	}

	public void modifyText(ExtendedModifyEvent e) {
		if (e.length > 0) {
			String text = editor.getText(e.start, e.start + e.length - 1);
			if (text.equals("\n") || text.equals("\r\n")) {
				System.out.println("Updating indent");
				updateIndentList();

				StringBuilder newText = new StringBuilder();
				int lineNum = editor.getLineAtOffset(e.start);
				int tabs = indentListener.getTabs(lineNum + 1);

				for (int i = 0; i < tabs; i++) {
					newText.append('\t');
				}
				editor.replaceTextRange(e.start + e.length, 0,
						newText.toString());
				editor.setCaretOffset(e.start + e.length + newText.length());
			} else {
				// unindent(e);
			}
		}
	}

	private int countIndents(String line) {
		int idx = 0;
		while (idx < line.length() && line.charAt(idx) == '\t')
			idx++;
		return idx;
	}

	@Override
	public void verifyText(VerifyEvent e) {
		if (e.text.equals("\n") || e.text.equals("\r\n")) {
			int indents = countIndents(editor.getLine(editor
					.getLineAtOffset(e.start)));
			StringBuilder newText = new StringBuilder(e.text);
			for (int i = 0; i < indents; i++) {
				newText.append('\t');
			}
			e.text = newText.toString();
		} else {
			unindent(e);
		}
	}
}