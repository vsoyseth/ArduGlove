package org.arduglove.mkinterface;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Mouse simulation mode
 */
public class MouseMode extends Mode {
	boolean mousePressed = false;
	boolean rightMousePressed = false;
	long mousePressedLast;

	int div = 50;
	double remX = 0;
	double remY = 0;

	int fullSpeed = 15;

	@Override
	void process(SensorData data) {
		data.aY *= -1;
		//data.aY -= 25;
		//data.aX -= 85;

		div = data.oneG / 5;

		PointerInfo pointer = MouseInfo.getPointerInfo();
		// Be careful moving mouse. If we move outside screen pointer == null
		Point mouse = pointer.getLocation();
		Rectangle screen = pointer.getDevice().getDefaultConfiguration().getBounds();

		double deltaX = mapHalfQuad(data.aX, data.oneG, fullSpeed) + remX;
		double deltaY = mapHalfQuad(data.aY, data.oneG, fullSpeed) + remX;

		// Calculate planned position (does not move yet)
		mouse.translate((int) deltaX, (int) deltaY);

		// Store remainder as we can only move whole pixels at a time
		remX = deltaX - (int) deltaX;
		remY = deltaY - (int) deltaY;

		if (!anyScreenContains(mouse.x, mouse.y)) {
			// Since the point we are moving to is not in any screen,
			// constrain it inside the current one
			if(mouse.x < screen.x) mouse.x = screen.x;
			if(mouse.x >= screen.x + screen.width) mouse.x = screen.x + screen.width - 1;
			if(mouse.y < screen.y) mouse.y = screen.y;
			if(mouse.y >= screen.y + screen.height) mouse.y = screen.y + screen.height - 1;
		}

		if (data.index && !mousePressed) {
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			mousePressed = true;
			mousePressedLast = System.currentTimeMillis();
		}

		if (!data.index && mousePressed) {
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			mousePressed = false;
		}

		if (data.middle && !rightMousePressed) {
			robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
			rightMousePressed = true;
			mousePressedLast = System.currentTimeMillis();
		}

		if (!data.middle && rightMousePressed) {
			robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
			rightMousePressed = false;
		}

		// Freeze mouse long enough to double-click
		if (System.currentTimeMillis() - mousePressedLast > 300) {
			robot.mouseMove(mouse.x, mouse.y);
		}

		if (data.pinky) {
			robot.mouseWheel(1);
		}

		if (data.ring) {
			robot.mouseWheel(-1);
		}
	}

	private boolean anyScreenContains(int x, int y) {
		GraphicsDevice[] screens = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getScreenDevices();
		for (GraphicsDevice screen : screens) {
			if (screen.getDefaultConfiguration().getBounds().contains(x, y)) {
				return true;
			}
		}
		return false;
	}

	double mapHalfQuad(double value, double fromMax, double toMax) {
		value /= fromMax;
		return (Math.signum(value)*toMax*value*value + toMax*value) / 2;
	}
}
