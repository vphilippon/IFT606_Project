package com.example.wifiapp.ConnectedClient;

//Source :
//http://stackoverflow.com/questions/16998095/list-connected-devices-to-android-phone-working-as-wifi-access-point
public class AccessPointClient {

		private String ipAddr;
		private String hwAddr;
		private String device;
		private boolean isReachable;
		private boolean isHost;

		public AccessPointClient(final String ipAddr, final String hWAddr, final String device, final boolean isReachable, final boolean isHost) {
		    super();
		    this.ipAddr = ipAddr;
		    this.hwAddr = hWAddr;
		    this.device = device;
		    this.isReachable = isReachable;
		    this.isHost = isHost;
		}

		public String getIpAddr() {
		    return ipAddr;
		}

		public void setIpAddr(String ipAddr) {
		    this.ipAddr = ipAddr;
		}

		public String getHwAddr() {
		    return hwAddr;
		}

		public void setHWAddr(String hWAddr) {
		    this.hwAddr = hWAddr;
		}

		public String getDevice() {
		    return device;
		}

		public void setDevice(String device) {
		    this.device = device;
		}

		public void setReachable(boolean isReachable) {
		    this.isReachable = isReachable;
		}

		public boolean isReachable() {
		    return isReachable;
		}
		
		public boolean isHost(){
			return isHost;
		}
}
