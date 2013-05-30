package com.austindiviness.test;

import edu.mines.acmX.exhibit.module_manager.ProcessingModule;

public class LaunchModule extends ProcessingModule {

	float curlx = 0;
	float curly = 0;
	float f = (float) (sqrt(2) / 2.);
	float deley = 10;
	float growth = 0;
	float growthTarget = 0;

	public void setup() {
		size(950, 450, P2D);
		// smooth();
		addMouseWheelListener(new java.awt.event.MouseWheelListener() {
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				mouseWheel(evt.getWheelRotation());
			}
		});
	}

	public void draw() {
		background(250);
		stroke(0);
		curlx += (radians((float) (360. / height * mouseX)) - curlx) / deley;
		curly += (radians((float) (360. / height * mouseY)) - curly) / deley;
		translate(width / 2, height / 3 * 2);
		line(0, 0, 0, height / 2);
		branch((float) (height / 4.), 17);
		growth += (growthTarget / 10 - growth + 1.) / deley;

	}

	void mouseWheel(int delta) {
		growthTarget += delta;
	}

	void branch(float len, int num) {
		len *= f;
		num -= 1;
		if ((len > 1) && (num > 0)) {
			pushMatrix();
			rotate(curlx);
			line(0, 0, 0, -len);
			translate(0, -len);
			branch(len, num);
			popMatrix();
			len *= growth;
			pushMatrix();
			rotate(curlx - curly);
			line(0, 0, 0, -len);
			translate(0, -len);
			branch(len, num);
			popMatrix();
		}
	}
	
	public void mouseClicked() {
		exit();
	}
}
