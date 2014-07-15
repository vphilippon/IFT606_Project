package com.example.wifiapp.Tools.ButtonState;

import android.widget.Button;

public class StopScanState implements ScanButtonState {

	@Override
	public ScanButtonState changeState() {
		return new IdleScanState();
	}

	@Override
	public void toogleText(Button b) {
		b.setText("Start ??");
	}

	@Override
	public void handleAction() {
		// TODO Auto-generated method stub
		
	}

}
