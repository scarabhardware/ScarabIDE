package com.embeddedmicro.mojo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class CustomButton extends Canvas {
	private int mouse = 0;
	private boolean hit = false;
	private Image icon;

	public CustomButton(Composite parent, int style) {
		super(parent, style);

		if (Theme.set) {
			setBackground(Theme.windowBackgroundColor);
			setForeground(Theme.windowForgroundColor);
		}

		this.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				CustomButton.this.paintControl(e);
			}
		});
		this.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				CustomButton.this.mouseMove(e);
			}
		});
		this.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e) {
				CustomButton.this.mouseEnter(e);
			}

			public void mouseExit(MouseEvent e) {
				CustomButton.this.mouseExit(e);
			}
		});
		this.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				CustomButton.this.mouseDown(e);
			}

			public void mouseUp(MouseEvent e) {
				CustomButton.this.mouseUp(e);
			}
		});
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				CustomButton.this.keyPressed(e);
			}
		});

	}

	public void setIcon(Image icon) {
		this.icon = icon;
	}

	private void paintControl(PaintEvent e) {
		switch (mouse) {
		case 0:
			if (Theme.set)
				e.gc.setBackground(Theme.windowBackgroundColor);
			e.gc.fillRectangle(getClientArea());
			break;
		case 1:
			if (Theme.set)
				e.gc.setBackground(Theme.toolBarHoverColor);
			e.gc.fillRectangle(getClientArea());
			break;
		case 2:
			if (Theme.set)
				e.gc.setBackground(Theme.toolBarClickColor);
			e.gc.fillRectangle(getClientArea());
			break;
		}
		if (icon != null)
			e.gc.drawImage(icon, 0, 0);
	}

	private void mouseMove(MouseEvent e) {
		if (!hit)
			return;
		mouse = 2;
		if (e.x < 0 || e.y < 0 || e.x > getBounds().width
				|| e.y > getBounds().height) {
			mouse = 0;
		}
		redraw();
	}

	private void mouseEnter(MouseEvent e) {
		mouse = 1;
		redraw();
	}

	private void mouseExit(MouseEvent e) {
		mouse = 0;
		redraw();
	}

	private void mouseDown(MouseEvent e) {
		hit = true;
		mouse = 2;
		redraw();
	}

	private void mouseUp(MouseEvent e) {
		hit = false;
		mouse = 1;
		if (e.x < 0 || e.y < 0 || e.x > getBounds().width
				|| e.y > getBounds().height) {
			mouse = 0;
		}
		redraw();
		if (mouse == 1)
			notifyListeners(SWT.Selection, new Event());
	}

	private void keyPressed(KeyEvent e) {
		if (e.keyCode == '\r' || e.character == ' ') {
			Event event = new Event();
			notifyListeners(SWT.Selection, event);
		}
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(25, 25);
	}
}
