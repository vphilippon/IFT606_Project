package com.example.wifiapp.Tools.ButtonState;

import android.widget.Button;

public interface ScanButtonState {
	
	ScanButtonState changeState();
	void toogleText(Button b);
	void handleAction();
	
}
