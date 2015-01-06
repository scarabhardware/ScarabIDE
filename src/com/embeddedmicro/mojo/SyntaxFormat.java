package com.embeddedmicro.mojo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class SyntaxFormat {

	public final Style[] formats;
	public final Style stringFormat;
	public final Style commentFormat;

	public SyntaxFormat(Display display) {
		stringFormat = new Style(
				new String[] { "\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"" },
				Theme.stringColor);
		commentFormat = new Style(new String[] { "//.*" }, Theme.commentColor);

		formats = new Style[5];
		formats[0] = new Style(getKeywords(new String[] { "always", "begin",
				"end", "assign", "if", "for", "else", "case", "endcase",
				"casex", "posedge", "negedge", "generate", "endgenerate" }), Theme.keyWordColor, SWT.BOLD);
		formats[1] = new Style(new String[] { "\\b[0-9]+'d[0-9xzXZ]+\\b",
				"\\b[0-9]+'b[0-1xzXZ]+\\b", "\\b[0-9]+'h[A-Fa-f0-9xzXZ]+\\b",
				"\\b[0-9]+'o[0-7xzXZ]+\\b" }, Theme.valueColor);
		formats[2] = new Style(getKeywords(new String[] { "module",
				"endmodule", "input", "output", "reg", "wire", "localparam",
				"parameter", "integer", "genvar" }), Theme.varTypeColor, SWT.ITALIC);
		formats[3] = new Style(new String[] { "[*=><!~+#\\-/:@|&{}?]" },
				Theme.operatorColor, SWT.BOLD);
		formats[4] = new Style(new String[] { "(?<=\\.)\\w+(?=\\()" },
				Theme.instantiationColor);

	}

	private String[] getKeywords(String[] words) {
		for (int i = 0; i < words.length; i++) {
			words[i] = "\\b" + words[i] + "\\b";
		}
		return words;
	}
}
