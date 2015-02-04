package biz.senmicro.android.SenmicroDemo;

import java.util.HashMap;
import java.util.Map;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class FirebaseConnector {

	private Firebase rootRef;
	private Map<String,LEDControl> ledControls = new HashMap<String,LEDControl>();
	private Firebase pushItems;
    private Firebase geofenceItems;

	public FirebaseConnector() {
		rootRef = new Firebase("https://intense-fire-5365.firebaseio.com/");
		pushItems = rootRef.child("pushItems");
        geofenceItems = rootRef.child("geofenceItems");
		
		rootRef.child("writeItems").addChildEventListener(new ChildEventListener() {

			@Override
			public void onCancelled(FirebaseError paramFirebaseError) {
			}

			@Override
			public void onChildAdded(DataSnapshot paramDataSnapshot,
					String paramString) {
				Map<String,String> data = ((Map<String,String>)paramDataSnapshot.getValue());
				
				LEDControl control = ledControls.get(data.get("itemName"));
				if (control != null) {
					control.setState("on".equals(data.get("value")));
				}
			}

			@Override
			public void onChildChanged(DataSnapshot paramDataSnapshot,
					String paramString) {

				Map<String,String> data = ((Map<String,String>)paramDataSnapshot.getValue());
				LEDControl control = ledControls.get(data.get("itemName"));
				if (control != null) {
					control.setState("on".equals(data.get("value")));
				}
			}

			@Override
			public void onChildMoved(DataSnapshot paramDataSnapshot,
					String paramString) {
			}

			@Override
			public void onChildRemoved(DataSnapshot paramDataSnapshot) {
			}
			
		});
	}
	
	public void addLEDListener(String name, LEDControl led) {
		ledControls.put(name, led);
	}

	public void setValue(String button, Object val) {
		Map<String,Object> value = new HashMap<String,Object>();
		value.put("value", val);
		value.put("timestamp", System.currentTimeMillis());
		pushItems.child(button).setValue(value);
	}

    public void addGeofenceItem() {
        Firebase geofenceRef = geofenceItems.push();
        Map<String,Object> value = new HashMap<String,Object>();
        value.put("phonenumber", "06641234567");
        value.put("latitude", "12.34");
        value.put("longitude", "98.76");
        value.put("type", "enter geofence");
        geofenceRef.setValue(value);
    }
}
