package com.example.wifiapp.ConnectedClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

//Source :
//http://stackoverflow.com/questions/16998095/list-connected-devices-to-android-phone-working-as-wifi-access-point
public class WifiAccessPointManager {
	
	private WifiManager wifiManager;
	
	public WifiAccessPointManager(Context context) {
	    wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}



	/**
	 * Gets a list of the clients connected to the Hotspot, reachable timeout is 300
	 * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
	 * @return ArrayList of {@link ClientScanResult}
	 */
	public ArrayList<AccessPointClient> getClientList(boolean onlyReachables) {
	    return getClientList(onlyReachables, 300);
	}

	/**
	 * Gets a list of the clients connected to the Hotspot 
	 * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
	 * @param reachableTimeout Reachable Timout in miliseconds
	 * @return ArrayList of {@link ClientScanResult}
	 */
	public ArrayList<AccessPointClient> getClientList(boolean onlyReachables, int reachableTimeout) {
	    BufferedReader br = null;
	    ArrayList<AccessPointClient> result = null;

	    try {
	        result = new ArrayList<AccessPointClient>();
	        br = new BufferedReader(new FileReader("/proc/net/arp"));
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] splitted = line.split(" +");

	            if ((splitted != null) && (splitted.length >= 4)) {
	                // Basic sanity check
	                String mac = splitted[3];

	                if (mac.matches("..:..:..:..:..:..")) {
	                    boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

	                    if (!onlyReachables || isReachable) {
	                        result.add(new AccessPointClient(splitted[0], splitted[3], splitted[5], isReachable, false));
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        Log.e(this.getClass().toString(), e.getMessage());
	    } finally {
	        try {
	            br.close();
	        } catch (IOException e) {
	            Log.e(this.getClass().toString(), e.getMessage());
	        }
	    }

	    return result;
	}
	
}
