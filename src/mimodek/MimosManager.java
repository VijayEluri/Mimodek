package mimodek;


import java.util.Enumeration;
import java.util.Hashtable;

import processing.core.PApplet;
import processing.core.PVector;

import mimodek.tracking.TrackingInfo;
import mimodek.tracking.TrackingListener;

public class MimosManager implements TrackingListener {

	PVector directionChangeRange = new PVector(0.1f, 6.1f);
	float maxSpeed = 10;

	// thread safe list

	Hashtable<Integer, Mimo> mimos;

	public MimosManager() {
		mimos = new Hashtable<Integer, Mimo>();
	}

	void randomWalk(Mimo m) {
		MainHandler.app.noiseSeed(MainHandler.app.millis());
		float speed = MainHandler.app.random(1) * maxSpeed;
		if (MainHandler.app.random(1) < 0.99) {
			return;
		}
		float a = PApplet.map(MainHandler.app.random(1), 0, 1,
				directionChangeRange.x, directionChangeRange.y);
		m.vel = new PVector(PApplet.cos(a), PApplet.sin(a));
		m.vel.mult(speed);
	}

	// we only update ancestor mimos
	private void update(Mimo m) {
		if (!m.ancestor)
			return;

		if (m.pos.x <= 0 || m.pos.x >= MainHandler.screenWidth || m.pos.y <= 0
				|| m.pos.y >= MainHandler.screenHeight) {
			// orient the ancestor towards the center
			float a = PApplet.atan2(MainHandler.screenHeight / 2 - m.pos.y,
					MainHandler.screenWidth / 2 - m.pos.x);
			m.vel = new PVector(PApplet.cos(a), PApplet.sin(a));
			float speed = MainHandler.app
					.noise(MainHandler.app.frameCount * 0.1f)
					* maxSpeed;
			m.vel.mult(speed);
		} else {
			randomWalk(m);
		}
		m.pos.add(m.vel);
	}

	public void update() {
		Enumeration<Integer> e = mimos.keys();
		while (e.hasMoreElements()) {
			int i = e.nextElement();
			Mimo m = mimos.get(i);

			if (!m.ancestor
					&& (m.pos.x <= 0 || m.pos.x >= MainHandler.screenWidth
							|| m.pos.y <= 0 || m.pos.y >= MainHandler.screenHeight)) {
				if(m.entered){
					m.ancestor = true;
					if (MainHandler.organism.cellCount() == 0) {
						System.out.println("Seeded!");
						MainHandler.organism.attachTo(m);
	
						mimos.remove(i);
	
						i--;
					}
				}else{
					m.entered = true;
				}
			} else if (m.ancestor) {
				update(m);
				if (MainHandler.organism.attachTo(m)) {

					mimos.remove(i);
					i--;
				}
			}
		}

	}

	public void draw() {
		Enumeration<Integer> e = mimos.keys();
		while (e.hasMoreElements()) {
			int i = e.nextElement();
			Mimo m = (Mimo) mimos.get(i);
			MainHandler.texturizer.draw(m);
		}
	}

	public void trackingEvent(TrackingInfo info) {
		if (mimos.containsKey(info.id) && !mimos.get(info.id).ancestor) {
			mimos.get(info.id).pos = new PVector(info.x, info.y);
		} else {
			mimos.put(info.id, new Mimo(new PVector(info.x, info.y)));
		}
	}

}
