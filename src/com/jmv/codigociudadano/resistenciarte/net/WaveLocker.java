package com.jmv.codigociudadano.resistenciarte.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class WaveLocker {

	private static WaveLocker instance;
	private HashMap<String, PowerManager.WakeLock> lockers;
	private PowerManager pm;

	private WaveLocker(Context context) {
		super();
		lockers = new HashMap<String, PowerManager.WakeLock>();
		// take CPU lock to prevent CPU from going off if the user
		// presses the power button during download
		pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
	}

	public static WaveLocker getInstance(Context context){
		if (instance == null){
			instance = new WaveLocker(context);
		}
		return instance;
	}
	
	public synchronized void adquireLock(String codeName) {
		WakeLock mWakeLock = lockers.get(codeName);
		if (mWakeLock == null) {
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, codeName);
			lockers.put(codeName, mWakeLock);
		}
		mWakeLock.acquire();
	}
	
	public synchronized void releaseLock(String codeName){
		WakeLock mWakeLock = lockers.get(codeName);
		if (mWakeLock != null) {
			mWakeLock.release();
			lockers.remove(codeName);
		}
	}

}
