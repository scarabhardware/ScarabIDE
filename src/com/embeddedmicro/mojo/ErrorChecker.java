package com.embeddedmicro.mojo;

import java.util.ArrayList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Display;

import com.embeddedmicro.mojo.parser.Verilog2001Lexer;
import com.embeddedmicro.mojo.parser.Verilog2001Parser;

public class ErrorChecker extends BaseErrorListener implements ModifyListener,
		LineStyleListener, Runnable {
	private ArrayList<SyntaxError> errors = new ArrayList<SyntaxError>();
	private StyledCodeEditor editor;
	private String text;
	private Display display;

	public ErrorChecker(StyledCodeEditor editor) {
		this.editor = editor;
		display = editor.getDisplay();
	}

	public void updateErrors() {
		ANTLRInputStream input = new ANTLRInputStream(text);
		Verilog2001Lexer lexer = new Verilog2001Lexer(input);
		lexer.removeErrorListeners();
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		Verilog2001Parser parser = new Verilog2001Parser(tokens);
		parser.removeErrorListeners();
		errors.clear();
		parser.addErrorListener(this);
		parser.source_text();

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				editor.redraw();
			}
		});
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
			Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		underlineError((Token) offendingSymbol, msg);
	}

	protected void underlineError(Token offendingToken, String message) {
		int start = offendingToken.getStartIndex();
		int stop = offendingToken.getStopIndex();
		StyleRange style = new StyleRange();
		style.start = start;
		style.length = stop - start + 1;
		style.underline = true;
		style.underlineColor = Theme.errorTextColor;
		style.underlineStyle = SWT.UNDERLINE_SINGLE;
		errors.add(new SyntaxError(style, message, start, stop));
	}

	@Override
	public void modifyText(ModifyEvent e) {
		display.timerExec(-1, this);
		text = editor.getText();
		display.timerExec(500, this);
	}

	@Override
	public void run() {
		synchronized (errors) {
			updateErrors();
		}
	}


	@Override
	public void lineGetStyle(LineStyleEvent event) {
		synchronized (errors) {
			StyleRange[] styles = new StyleRange[errors.size()];
			for (int i = 0; i < errors.size(); i++){
				styles[i] = errors.get(i).style;
			}
			event.styles = styles;
		}
	}
	
	public SyntaxError getErrorAtOffset(int pos){
		for (SyntaxError e : errors){
			if (e.start <= pos && e.stop >= pos)
				return e;
		}
		return null;
	}
	
	public class SyntaxError {
		public StyleRange style;
		public String message;
		public int start;
		public int stop;
		
		public SyntaxError(){
		}
		
		public SyntaxError(StyleRange style, String msg, int start, int stop){
			this.style = style;
			this.message = msg;
			this.start = start;
			this.stop = stop;
		}
	}
}
