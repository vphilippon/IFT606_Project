package com.example.wifiapp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.example.wifiapp.ConnectedClient.AccessPointClient;
import com.example.wifiapp.ConnectedClient.WifiAccessPointManager;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

public class HostHotSpotActivity extends Activity {
	
	private WifiManager wifiManager;
	private ConnectedClientWatcher clientWatcher;
	
	private Thread t;
	private Object clientListMutex;
	private ArrayList<AccessPointClient> clients;
	
	// Table inside the scroll view that holds stock symbols and buttons
	private TableLayout clientsTableLayout;
	private TextView debugView;
	
	// Utility class 
	private class ConnectedClientWatcher implements Runnable{

		private WifiAccessPointManager wifiAccesPointManager;
		private boolean watchForClients;
		
		public ConnectedClientWatcher(final Context ctx){
			wifiAccesPointManager = new WifiAccessPointManager(ctx);
			watchForClients = true;
		}
		
		public void stopWatching(){
			watchForClients = false;
		}
		
		@Override
		public void run() {
			
			while(watchForClients){
				HostHotSpotActivity.this.setClientList(wifiAccesPointManager.getClientList(false));
				HostHotSpotActivity.this.updateClientTableLayout();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					
				}
			}
		}
	}
	
	public void setClientList(final ArrayList<AccessPointClient> newClientList){
		synchronized (clientListMutex) {
			clients = newClientList;
		}
	}
	
	private void updateClientTableLayout(){
		synchronized (clientListMutex) {
			StringBuilder sb = new StringBuilder();
			
			for(AccessPointClient c : clients){
				sb.append(c.toString() + "\n");
			}
			debugView.setText(sb.toString());
			
			/*clientsTableLayout.removeAllViews();
			for(AccessPointClient c : clients){
				addClientToScrollView(c);
			}*/
		}
	}
	
	private void addClientToScrollView(final AccessPointClient client){
		
		// Get the LayoutInflator service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View clientConnetectedEntry = inflater.inflate(R.layout.connected_client_entry, null);
		
		final TextView clientIp = (TextView) clientConnetectedEntry.findViewById(R.id.client_entry_ip);
		clientIp.setText(client.getIpAddr());
		
		final TextView clientMac = (TextView) clientConnetectedEntry.findViewById(R.id.client_entry_mac);
		clientMac.setText(client.getHwAddr());
		
		final TextView clientDevice = (TextView) clientConnetectedEntry.findViewById(R.id.client_entry_device);
		clientDevice.setText(client.getDevice());
		
		// Add the new wifi configuration for the stock to the TableLayout
		clientsTableLayout.addView(clientConnetectedEntry);
	}
	
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		// Executed to allow thread to stop
		clientWatcher.stopWatching();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// Executed to allow thread to stop
		clientWatcher.stopWatching();
		
		// Wifi service must be on to detect networks
	    if(!wifiManager.isWifiEnabled())
	    {
	        wifiManager.setWifiEnabled(true);          
	    } 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_hot_spot);

		
		/*if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
		
		final Bundle bundle = getIntent().getExtras();
		String ssid = bundle.getString(ScanActivity.SELECTED_SSID_KEY);
		/*String bssid = bundle.getString(ScanActivity.SELECTED_BSSID_KEY);
		String frequency = bundle.getString(ScanActivity.SELECTED_FREQ_KEY);
		String level = bundle.getString(ScanActivity.SELECTED_LEVEL_KEY);*/
		
		wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		clientWatcher = new ConnectedClientWatcher(this);
		clientsTableLayout = (TableLayout) findViewById(R.id.clientsTableLayout);
		clientListMutex = new Object();
		clients = new ArrayList<AccessPointClient>();
		
		debugView = (TextView) findViewById(R.id.testdebug);
		
		createWifiAccessPoint(ssid);
		
		t = new Thread(clientWatcher);
		t.start();
	}

	// Fonctionne
	// Uses reflexivity because required functions are not visible
	// Source : http://stackoverflow.com/questions/12926738/how-to-create-access-point-programmatically
	private void createWifiAccessPoint(final String ssid) {
		
		// Turn off wifi service before thetering
	    if(wifiManager.isWifiEnabled())
	    {
	        wifiManager.setWifiEnabled(false);          
	    }       
	    
	    Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   
	    boolean methodFound=false;
	    for(Method method: wmMethods)
	    {
	        if(method.getName().equals("setWifiApEnabled"))
	        {
	            methodFound=true;
	            WifiConfiguration netConfig = new WifiConfiguration();
	            netConfig.SSID = ssid; 
	            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	            netConfig.hiddenSSID = false;
	            
	            try 
	            {
	            	boolean apstatus = (Boolean) method.invoke(wifiManager, netConfig,true);          
	                for (Method isWifiApEnabledmethod: wmMethods)
	                {
	                    if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")) 
	                    {
	                        while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){};
	                        for(Method method1: wmMethods)
	                        {
	                            if(method1.getName().equals("getWifiApState"))
	                            {
	                                int apstate = (Integer) method1.invoke(wifiManager);
	                            }
	                        }
	                    }
	                }

	            } catch (IllegalArgumentException e) {
	                e.printStackTrace();
	            } catch (IllegalAccessException e) {
	                e.printStackTrace();
	            } catch (InvocationTargetException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
	// To be tested
	// Source : http://stackoverflow.com/questions/4374862/how-to-programatically-create-and-read-wep-eap-wifi-configurations-in-android
	void saveWepConfig()
	{
	    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    WifiConfiguration wc = new WifiConfiguration(); 
	    wc.SSID = "\"SSID_NAME\""; //IMP! This should be in Quotes!!
	    wc.hiddenSSID = true;
	    wc.status = WifiConfiguration.Status.DISABLED;     
	    wc.priority = 40;
	    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

	    wc.wepKeys[0] = "\"aaabbb1234\""; //This is the WEP Password
	    wc.wepTxKeyIndex = 0;

	    WifiManager  wifiManag = (WifiManager) this.getSystemService(WIFI_SERVICE);
	    boolean res1 = wifiManag.setWifiEnabled(true);
	    int res = wifi.addNetwork(wc);
	    boolean es = wifi.saveConfiguration();
	    boolean b = wifi.enableNetwork(res, true);   
	}
	
	
	
	
	
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.host_hot_spot, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_host_hot_spot,
					container, false);
			return rootView;
		}
	}
	*/
}
