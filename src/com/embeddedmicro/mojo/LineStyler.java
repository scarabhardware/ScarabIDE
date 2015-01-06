package com.embeddedmicro.mojo;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GlyphMetrics;

import com.embeddedmicro.mojo.parser.Verilog2001Lexer;

public class LineStyler implements LineStyleListener, ModifyListener {

	private StyledText styledText;
	private StyleTokenReader listener;
	private int numBlockComments = -1;

	public LineStyler(StyledText text) {
		styledText = text;
		listener = new StyleTokenReader();
	}

	@Override
	public void lineGetStyle(LineStyleEvent event) {
		// Set the line number
		int activeLine = styledText
				.getLineAtOffset(styledText.getCaretOffset());
		int currentLine = styledText.getLineAtOffset(event.lineOffset);
		event.bulletIndex = currentLine;

		int width = 36;
		if (styledText.getLineCount() > 999)
			width = (int) ((Math.floor(Math.log10(styledText.getLineCount())) + 1) * 12);

		// Set the style, 12 pixles wide for each digit
		StyleRange style = new StyleRange();
		style.metrics = new GlyphMetrics(0, 0, width);

		if (activeLine == currentLine) {
			style.background = Theme.highlightedLineColor;
		}

		style.foreground = Theme.bulletTextColor;

		// Create and set the bullet
		event.bullet = new Bullet(ST.BULLET_NUMBER, style);

		ANTLRInputStream input = new ANTLRInputStream(styledText.getText());
		Verilog2001Lexer lexer = new Verilog2001Lexer(input);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		event.styles = listener.getStyles(tokens, event.styles);

		List<Token> blockComments = tokens.getTokens(0, tokens.size() - 1,
				Verilog2001Lexer.Block_comment);
		if ((blockComments != null && blockComments.size() != numBlockComments)
				|| (blockComments == null && numBlockComments != 0)) {
			numBlockComments = blockComments == null ? 0 : blockComments.size();
			styledText.redraw();
		}

	}

	@Override
	public void modifyText(ModifyEvent e) {
		// TODO Auto-generated method stub

	}

}
