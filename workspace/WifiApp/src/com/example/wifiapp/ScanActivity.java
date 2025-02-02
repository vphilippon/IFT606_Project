package com.example.wifiapp;

import java.util.List;

import com.example.wifiapp.Tools.ButtonState.IdleScanState;
import com.example.wifiapp.Tools.ButtonState.ScanButtonState;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;


public class ScanActivity extends Activity {
	
	// Store a unique message using your package name to avoid conflicts
	// with other apps. This is used for debugging with Log and Logcat.
	public final static String TAG = "com.example.wifiapp";
	
	// Manages key valued pairs associated with stock symbols
	private SharedPreferences detectedNetworkPreferences;
	public final static String SELECTED_SSID_KEY = "wifi_ssid";
	public final static String SELECTED_BSSID_KEY = "wifi_bssid";
	public final static String SELECTED_LEVEL_KEY = "wifi_level";
	public final static String SELECTED_FREQ_KEY = "wifi_freq";
	
	// Table inside the scroll view that holds stock symbols and buttons
	private TableLayout networkTableScrollView;
	
	private TextView scanResultCount;
	private WifiManager wifiManager;
	private Button scanActionButton;
	private Button deleteResultsButton;
	private ScanButtonState scanButtonState;
	
	private WifiScanReceiver scanResultReceiver;
	class WifiScanReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			List<ScanResult> results = wifiManager.getScanResults();
			for (ScanResult scanResult : results){
				addNetworkToScrollView(scanResult);
			}
			
			updateNetworkCountHeader();
		}
	}
	
	private void addNetworkToScrollView(final ScanResult scanResult){
		
		// Get the LayoutInflator service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View scanResultEntry = inflater.inflate(R.layout.scan_result_entry, null);
		
		// Add custom information into created view
		final TextView scanEntrySSID = (TextView) scanResultEntry.findViewById(R.id.scan_entry_ssid);
		scanEntrySSID.setText(scanResult.SSID);
		
		final TextView scanEntryBSSID = (TextView) scanResultEntry.findViewById(R.id.scan_entry_bssid);
		scanEntryBSSID.setText(scanResult.BSSID);
		
		Integer frequency = scanResult.frequency;
		final TextView scanEntryFrequency = (TextView) scanResultEntry.findViewById(R.id.scan_entry_frequency);
		scanEntryFrequency.setText(frequency.toString());
		
		Integer level = scanResult.level;
		final TextView scanEntryLevel = (TextView) scanResultEntry.findViewById(R.id.scan_entry_level);
		scanEntryLevel.setText(level.toString());
		
		final Button scanShowMore = (Button) scanResultEntry.findViewById(R.id.scan_result_show_more);
		scanShowMore.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(getApplicationContext(), DetailedScanResultActivity.class);
				intent.putExtra(SELECTED_SSID_KEY, scanEntrySSID.getText());
				intent.putExtra(SELECTED_BSSID_KEY, scanEntryBSSID.getText());
				intent.putExtra(SELECTED_FREQ_KEY, scanEntryFrequency.getText());
				intent.putExtra(SELECTED_LEVEL_KEY, scanEntryLevel.getText());
				startActivity(intent);
			}
		});
		
		scanResultEntry.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Builder confirmationDialog = new AlertDialog.Builder(ScanActivity.this);
				
				confirmationDialog.setIcon(android.R.drawable.ic_dialog_alert);
		        confirmationDialog.setTitle(R.string.launch_hotspot_confirmation_title);
		        confirmationDialog.setMessage(String.format(getResources().getString(R.string.launch_hotspot_confirmation_message), scanResult.SSID));
		        confirmationDialog.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		                
		            	// Force close last activity to avoid conflicts ... 
		            	ScanActivity.this.finish();
		            	
		                // Launch next activity -> 
		                Intent intent = new Intent(getApplicationContext(), HostHotSpotActivity.class);
		                intent.putExtra(SELECTED_SSID_KEY, scanEntrySSID.getText());
						intent.putExtra(SELECTED_BSSID_KEY, scanEntryBSSID.getText());
						intent.putExtra(SELECTED_FREQ_KEY, scanEntryFrequency.getText());
						intent.putExtra(SELECTED_LEVEL_KEY, scanEntryLevel.getText());
						startActivity(intent);
		            }
		        });
		        
		        confirmationDialog.setNegativeButton(R.string.dialog_no, null);
		        confirmationDialog.show();
			}
		});
		
		// Add the new wifi configuration for the stock to the TableLayout
		networkTableScrollView.addView(scanResultEntry);
	}
	
	// OnClick button handler for scan button
	public void handleScanAction(View v)
	{
		// Wifi service must be on to detect networks
	    if(!wifiManager.isWifiEnabled())
	    {
	        wifiManager.setWifiEnabled(true);          
	    }   
		
		scanButtonState.toogleText(scanActionButton);
		//scanButtonState.handleAction(this, );
		scanButtonState = scanButtonState.changeState();
		
		registerReceiver(scanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		
		//unregisterReceiver(scanResultReceiver);
	}
	
	// OnClick button handler for delete button
	public void deleteAllStocks(View v){
		networkTableScrollView.removeAllViews();
		
		SharedPreferences.Editor preferencesEditor = detectedNetworkPreferences.edit();
		preferencesEditor.clear();
		preferencesEditor.apply();
		
		updateNetworkCountHeader();
	}
	
	private void updateNetworkCountHeader(){
		Integer networkCount = networkTableScrollView.getChildCount();
		scanResultCount.setText(networkCount.toString());
	}
	
	/*private void saveStockSymbol(String newStock){
		
		// Used to check if this is a new stock
		String isTheStockNew = detectedNetworkPreferences.getString(newStock, null);
		
		// Editor is used to store a key / value pair
		// I'm using the stock symbol for both, but I could have used company
		// name or something else
		SharedPreferences.Editor preferencesEditor = detectedNetworkPreferences.edit();
		preferencesEditor.putString(newStock, newStock);
		preferencesEditor.apply();
		
	}*/
	
	
	// Set up the activity
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		
		// Retrieve saved stocks entered by the user. 
		// MODE_PRIVATE: Only accessible by your app (Most Common)
		// MODE_WORLD_READABLE: Any app can read (Can OR with Following)
		// MODE_WORLD_WRITABLE: Any app can write to this
		detectedNetworkPreferences = getSharedPreferences("networkList", MODE_PRIVATE);
		
		// Initialize ativity components
		scanButtonState = new IdleScanState();
		scanResultReceiver = new WifiScanReceiver();
		scanResultCount = (TextView) findViewById(R.id.scan_result_header_count);
		networkTableScrollView = (TableLayout) findViewById(R.id.networkTableLayout);
		wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		deleteResultsButton = (Button)findViewById(R.id.delete_scan_results_button);
		scanActionButton = (Button)findViewById(R.id.scan_action_button);
	}
}
