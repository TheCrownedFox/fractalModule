package com.austindiviness.test;

import java.util.List;

import edu.mines.acmX.exhibit.input_services.events.EventManager;
import edu.mines.acmX.exhibit.input_services.events.EventType;
import edu.mines.acmX.exhibit.input_services.hardware.BadFunctionalityRequestException;
import edu.mines.acmX.exhibit.input_services.hardware.DeviceConnectionException;
import edu.mines.acmX.exhibit.input_services.hardware.HandPosition;
import edu.mines.acmX.exhibit.input_services.hardware.HardwareManager;
import edu.mines.acmX.exhibit.input_services.hardware.HardwareManagerManifestException;
import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
import edu.mines.acmX.exhibit.input_services.hardware.drivers.openni.HandReceiver;
import edu.mines.acmX.exhibit.stdlib.graphics.Coordinate3D;

public class LaunchModule extends edu.mines.acmX.exhibit.module_management.modules.ProcessingModule {

	public static final int COUNTDOWN = 15000;
	private static HardwareManager hwMgr;
	private static EventManager evtMgr;
	
	private HandTrackerInterface driver;
	private MyHandReceiver receiver;
	
	float curlx = 0;
	float curly = 0;
	float f = (float) (sqrt(2) / 2.);
	float deley = 10;
	float growth = 0;
	float growthTarget = 0;
	float x;
	float y;
	int timeToClose;
	int timeLostHand;
	

	public void setup() {
		size(width, height, P2D);
		// smooth();
		timeLostHand = -1;
		try {
			hwMgr = HardwareManager.getInstance();
		} catch (HardwareManagerManifestException | DeviceConnectionException e) {
			e.printStackTrace();
		}
		
		try {
			
			List<String> drivers = hwMgr.getDevices("handtracking");
			driver = (HandTrackerInterface) hwMgr.inflateDriver(drivers.get(0), "handtracking");
			
		} catch (BadFunctionalityRequestException e) {
			e.printStackTrace();
		}
		
		evtMgr = EventManager.getInstance();
		
		receiver = new MyHandReceiver();
		
		evtMgr.registerReceiver(EventType.HAND_CREATED, receiver);
		evtMgr.registerReceiver(EventType.HAND_UPDATED, receiver);
		evtMgr.registerReceiver(EventType.HAND_DESTROYED, receiver);
		
		
		addMouseWheelListener(new java.awt.event.MouseWheelListener() {
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				mouseWheel(evt.getWheelRotation());
			}
		});
		
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
		if (millis() - timeLostHand < COUNTDOWN && receiver.lostHand()) {
			pushMatrix();
			resetMatrix();
			fill(255, 0, 0);
			int secondsLeft = (COUNTDOWN - millis() + timeLostHand) / 1000;
			textSize(96);
			text("" + secondsLeft, width / 2, height / 2);
			popMatrix();
		}
		else if (millis() - timeLostHand >= COUNTDOWN && receiver.lostHand()) {
			exit();
		}
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
			System.out.println("Got a hand: " + pos.id);
			if (handID == -1) {
				handID = pos.id;
			}
			position = pos.position;
			offsetZ = pos.position.getZ();
			lostHand = false;
			
		}
		
		public void handUpdated(HandPosition pos) {
			if (pos.id == handID) {
				position = pos.position;
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
