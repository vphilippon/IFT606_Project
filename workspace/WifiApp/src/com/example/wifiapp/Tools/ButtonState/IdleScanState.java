package com.example.wifiapp.Tools.ButtonState;

import android.widget.Button;

public class IdleScanState implements ScanButtonState {

	@Override
	public ScanButtonState changeState() {
		return new StopScanState();
	}

	@Override
	public void toogleText(Button b) {
		b.setText("Stop ??");
	}

	@Override
	public void handleAction() {
		// TODO Auto-generated method stub
		
	}
	
}
