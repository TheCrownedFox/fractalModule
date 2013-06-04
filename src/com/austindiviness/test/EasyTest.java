package com.austindiviness.test;

import edu.mines.acmX.exhibit.module_management.modules.ProcessingModule;


public class EasyTest extends ProcessingModule {

	public void setup() {
		size(800, 800);
		background(0);
	}
	
	public void draw() {
		background(0);
		fill(255, 0, 0);
		ellipse(mouseX, mouseY, 30, 30);
	}
	
	public void mouseClicked() {
		exit();
	}
}
