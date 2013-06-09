package com.austindiviness.test;

import edu.mines.acmX.exhibit.input_services.events.EventManager;
import edu.mines.acmX.exhibit.input_services.events.EventType;
import edu.mines.acmX.exhibit.input_services.events.HandReceiver;
import edu.mines.acmX.exhibit.input_services.hardware.BadFunctionalityRequestException;
import edu.mines.acmX.exhibit.input_services.hardware.HardwareManager;
import edu.mines.acmX.exhibit.input_services.hardware.HardwareManagerManifestException;
import edu.mines.acmX.exhibit.input_services.hardware.UnknownDriverRequest;
import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
import edu.mines.acmX.exhibit.module_management.modules.ProcessingModule;
import edu.mines.acmX.exhibit.stdlib.graphics.Coordinate3D;
import edu.mines.acmX.exhibit.stdlib.graphics.HandPosition;

public class LaunchModule extends ProcessingModule {

	//time (in ms) to wait before closing
	public static final int COUNTDOWN = 15000;
	private static HardwareManager hwMgr;
	private static EventManager evtMgr;
	
	private HandTrackerInterface driver;
	private MyHandReceiver receiver;
	
	private float curlx = 0;
	private float curly = 0;
	private float f = (float) (sqrt(2) / 2.);
	private float deley = 10;
	private float growth = 0;
	private float growthTarget = 0;
	private float x;
	private float y;
	private int timeLostHand;
	

	public void setup() {
		size(width, height, P2D);
		noCursor();
		// smooth();
		timeLostHand = -1;
		try {
			hwMgr = HardwareManager.getInstance();
		} catch (HardwareManagerManifestException e) {
			e.printStackTrace();
		}
		
		try {
			
			driver = (HandTrackerInterface) hwMgr.getInitialDriver("handtracking");
			
		} catch (BadFunctionalityRequestException | InvalidConfigurationFileException e) {
			e.printStackTrace();
		} catch (UnknownDriverRequest e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		evtMgr = EventManager.getInstance();
		
		receiver = new MyHandReceiver();
		
		evtMgr.registerReceiver(EventType.HAND_CREATED, receiver);
		evtMgr.registerReceiver(EventType.HAND_UPDATED, receiver);
		evtMgr.registerReceiver(EventType.HAND_DESTROYED, receiver);
		
	}
	
	public void update() {
		driver.updateDriver();
		if (receiver.whichHand() != -1) {			
			x = receiver.getX();
			y = receiver.getY();
			growthTarget = (receiver.getZ() - receiver.getOffset()) / 20;
		}
		else if (receiver.lostHand()) {
			if (timeLostHand == -1) {
				timeLostHand = millis();
			}
		}
		if (!receiver.lostHand()) {
			timeLostHand = -1;
		}
		if (millis() - timeLostHand >= COUNTDOWN && receiver.lostHand()) {
			exit();
		}
	}

	public void draw() {
		update();
		background(250);
		stroke(0);
		curlx += (radians((float) (360. / height * x)) - curlx) / deley;
		curly += (radians((float) (360. / height * y)) - curly) / deley;
		translate(width / 2, height / 3 * 2);
		line(0, 0, 0, height / 2);
		branch((float) (height / 4.), 17);
		growth += (growthTarget / 10 - growth + 1.) / deley;
		if (receiver.whichHand() == -1) {
			pushMatrix();
			resetMatrix();
			fill(255, 0, 0);
			textSize(96);
			textAlign(CENTER, CENTER);
			text("Wave to Begin", width / 2, (int) (height * 0.35));
			textAlign(LEFT, TOP);
			popMatrix();
		}
		if (receiver.lostHand()) {
			pushMatrix();
			resetMatrix();
			fill(255, 0, 0);
			int secondsLeft = (COUNTDOWN - millis() + timeLostHand) / 1000;
			textSize(96);
			textAlign(CENTER, CENTER);
			text("Wave to Begin", width / 2, (int) (height * 0.35));
			text("" + secondsLeft, width / 2, height / 2);
			textAlign(LEFT, TOP);
			popMatrix();
		}
		
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
	
	class MyHandReceiver extends HandReceiver {
		
		private Coordinate3D position;
		int handID = -1;
		float offsetZ;
		boolean lostHand;
		
		public MyHandReceiver() {
			position = new Coordinate3D(0, 0, 0);
			offsetZ = 0;
			lostHand = false;
		}
		
		public void handCreated(HandPosition pos) {
			if (handID == -1) {
				handID = pos.getId();
			}
			position = pos.getPosition();
			offsetZ = pos.getPosition().getZ();
			lostHand = false;
			
		}
		
		public void handUpdated(HandPosition pos) {
			if (handID == -1) {
				handID = pos.getId();
				lostHand = false;
			}
			
			if (pos.getId() == handID) {
				position = pos.getPosition();
			}
			
		}
		
		public void handDestroyed(int id) {
			if (id == handID) {
				handID = -1;
				lostHand = true;
			}
		}
		
		public int whichHand() {
			return handID;
		}
		
		public float getX() {
			return position.getX();
		}
		
		public float getY() {
			return position.getY();
		}
		
		public float getZ() {
			return position.getZ();
		}
		
		public float getOffset() {
			return offsetZ;
		}
		
		public boolean lostHand() {
			return lostHand;
		}
	}
}
