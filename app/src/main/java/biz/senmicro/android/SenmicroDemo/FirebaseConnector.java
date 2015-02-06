package biz.senmicro.android.SenmicroDemo;

import android.text.format.DateFormat;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * FirebaseConnector as Singleton
 */
public class FirebaseConnector {

    private static FirebaseConnector instance;

    private Firebase rootRef;
	private Map<String,LEDControl> ledControls = new HashMap<String,LEDControl>();
    private Map<String,TextView> transitionControls = new HashMap<String,TextView>();
	private Firebase inputItems;
    private Firebase outputItems;
    private Firebase transitionItems;

    public static FirebaseConnector getInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new FirebaseConnector();
        }

        return instance;
    }

    private FirebaseConnector()
    {
   		rootRef = new Firebase("https://intense-fire-5365.firebaseio.com/");

        inputItems = rootRef.child("inputItems");
        outputItems = rootRef.child("outputItems");
        transitionItems = rootRef.child("transitionItems");

        inputItems.addChildEventListener(new ChildEventListener() {
            @Override
            public void onCancelled(FirebaseError paramFirebaseError) {
            }

            @Override
            public void onChildAdded(DataSnapshot paramDataSnapshot,
                                     String paramString) {
            }

            @Override
            public void onChildChanged(DataSnapshot paramDataSnapshot,
                                       String paramString) {
                Map<String, String> data = ((Map<String, String>) paramDataSnapshot.getValue());


                if ("TI0".equalsIgnoreCase(data.get("itemName"))) {
                    TextView textviewToUpdate;
                    LinearLayout layoutToUpdate;

                    textviewToUpdate = transitionControls.get("state");
                    layoutToUpdate = (LinearLayout)textviewToUpdate.getParent();
                    if (transitionControls.size() > 0) {
                        if ("true".equalsIgnoreCase(String.valueOf(data.get("value")))) {
                            transitionControls.get("state").setText(R.string.in_geofence);
                            transitionControls.get("date").setText(data.get("timestamp"));
                            layoutToUpdate.setBackgroundResource(R.color.button_pressed);
                        } else {
                            transitionControls.get("state").setText(R.string.out_geofence);
                            transitionControls.get("date").setText("");
                            layoutToUpdate.setBackgroundResource(0);
                        }
                    }
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


        outputItems.addChildEventListener(new ChildEventListener() {

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

    public void addTransitionListener(String name,TextView view) {
        transitionControls.put(name, view);
    }

    /*public void setOutputValue(String name, Object val) {
        Map<String,Object> value = new HashMap<String,Object>();
        value.put("itemName", name);
        value.put("value", val);
        value.put("timestamp", System.currentTimeMillis());
       outputItems.child(name).setValue(value);
    }*/

	public void setInputValue(String name, Object val) {
		Map<String,Object> value = new HashMap<String,Object>();
        value.put("itemName", name);
		value.put("value", val);
		value.put("timestamp", System.currentTimeMillis());
        inputItems.child(name).setValue(value);
	}

    public void setInputValue(String name, Object val, Object timestamp) {
        Map<String,Object> value = new HashMap<String,Object>();
        value.put("itemName", name);
        value.put("value", val);
        value.put("timestamp", timestamp);
        inputItems.child(name).setValue(value);
    }

    public void addTransition(String deviceId, String transitionMessage) {
        Firebase geofenceRef = transitionItems.push();
        long timestamp = System.currentTimeMillis();
        String date = getDate(timestamp);
        Map<String,Object> value = new HashMap<String,Object>();
        value.put("deviceId", deviceId);
        value.put("transitionMessage", transitionMessage);
        value.put("timestamp", timestamp);
        // ToDo Fox  Datum hinzufügen
        geofenceRef.setValue(value);

        setInputValue("TI0", transitionMessage.startsWith("Entered"), date);
    }

    public static String getDate(long time)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss z", cal).toString();
        return date;
    }
    /*public void setTransition(boolean value) {
        Map<String,Object> value1 = new HashMap<String,Object>();
        value1.put("itemName", "TI0");
        value1.put("value", value);
        value1.put("timestamp", System.currentTimeMillis());
        inputItems.child("TI0").setInputValue(value1);
    }*/
}
