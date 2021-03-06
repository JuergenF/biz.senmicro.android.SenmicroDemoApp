package biz.senmicro.android.SenmicroDemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;


import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends FragmentActivity {
	private final static int USBAccessoryWhat = 0;

	public static final int UPDATE_LED_SETTING		 	= 1;
	public static final int PUSHBUTTON_STATUS_CHANGE	= 2;
	public static final int POT_STATUS_CHANGE			= 3;
	public static final int APP_CONNECT					= (int)0xFE;
	public static final int APP_DISCONNECT				= (int)0xFF;
	
	public static final int LED_0_ON					= 0x01;
	public static final int LED_1_ON					= 0x02;
	public static final int LED_2_ON					= 0x04;
	public static final int LED_3_ON					= 0x08;
	public static final int LED_4_ON					= 0x10;
	public static final int LED_5_ON					= 0x20;
	public static final int LED_6_ON					= 0x40;
	public static final int LED_7_ON					= 0x80;
	
	public static final int BUTTON_1_PRESSED			= 0x01;
	public static final int BUTTON_2_PRESSED			= 0x02;
	public static final int BUTTON_3_PRESSED			= 0x04;
	public static final int BUTTON_4_PRESSED			= 0x08;
	
	public static final int POT_UPPER_LIMIT				= 100;
	public static final int POT_LOWER_LIMIT				= 0;
	
    private boolean deviceAttached = false;
	
	private int firmwareProtocol = 0;
	
	private String TAG = "SENMICRO";
	
	private enum ErrorMessageCode {
		ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING,
		ERROR_FIRMWARE_PROTOCOL
	};
	
	private USBAccessoryManager accessoryManager;

	private FirebaseConnector firebaseConnector;

    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;

    // Store the current request
    private GeofenceUtils.REQUEST_TYPE mRequestType;

    // Store the current type of removal
    private GeofenceUtils.REMOVE_TYPE mRemoveType;


    // Persistent storage for geofences
    private SimpleGeofenceStore mPrefs;

    // Store a list of geofences to add
    List<Geofence> mCurrentGeofences;

    /*
     * Internal lightweight geofence objects for geofence 1
     */
    private SimpleGeofence mUIGeofence1;

    // decimal formats for latitude, longitude, and radius
    private DecimalFormat mLatLngFormat;
    private DecimalFormat mRadiusFormat;

    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;

    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    // Store the list of geofences to remove
    private List<String> mGeofenceIdsToRemove;

    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;
    // Remove geofences handler
    private GeofenceRemover mGeofenceRemover;

    // Handle to geofence 1 latitude in the UI
    private EditText mLatitude1;

    // Handle to geofence 1 longitude in the UI
    private EditText mLongitude1;

    // Handle to geofence 1 radius in the UI
    private EditText mRadius1;

    // Handle position data in the UI
    private TextView mActualPositionLat;
    private TextView mActualPositionLong;
    public TextView mActualIMEI;
    private Location mActualPosition;

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    
		try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	        Log.d(TAG, "Info:" + info.packageName + "\n" + info.versionCode + "\n" + info.versionName); 
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
		firebaseConnector = FirebaseConnector.getInstance();

       	accessoryManager = new USBAccessoryManager(this.getApplicationContext(), handler, USBAccessoryWhat);

        // Attach to the main UI
        setContentView(R.layout.main);

        // Add leds
        try {
    		//Set the link to the message handler for this class
        	LEDControl ledControl;

			ledControl = ((LEDControl)findViewById(R.id.DO7));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO7", ledControl);

			ledControl = ((LEDControl)findViewById(R.id.DO6));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO6", ledControl);

			ledControl = ((LEDControl)findViewById(R.id.DO5));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO5", ledControl);

			ledControl = ((LEDControl)findViewById(R.id.DO4));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO4", ledControl);

			ledControl = ((LEDControl)findViewById(R.id.DO3));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO3", ledControl);

			ledControl = ((LEDControl)findViewById(R.id.DO2));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO2", ledControl);

			ledControl = ((LEDControl)findViewById(R.id.DO1));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO1", ledControl);

			ledControl = ((LEDControl)findViewById(R.id.DO0));
			ledControl.setHandler(handler);
			firebaseConnector.addLEDListener("DO0", ledControl);

        } catch (Exception e) {
        }

        // Add transition listener for button
        try {
            //Set the link to the message handler for this class

            TextView ti0 = ((TextView)findViewById(R.id.TI0));
            TextView dateView = ((TextView)findViewById(R.id.geofence_date));
            //ledControl.setHandler(handler);
            firebaseConnector.addTransitionListener("state", ti0);
            firebaseConnector.addTransitionListener("date", dateView);
        } catch (Exception e) {
        }

        //Restore UI state from the savedInstanceState
        //  If the savedInstanceState Bundle exists, then there is saved data to
        //  restore.
        if (savedInstanceState != null) {
        	try {
        		//Restore the saved data for each of the LEDs.
        		LEDControl ledControl;
        		ProgressBar progressBar;

        		updateButton(R.id.DI3,savedInstanceState.getBoolean("DI3"));
        		updateButton(R.id.DI2,savedInstanceState.getBoolean("DI2"));
        		updateButton(R.id.DI1,savedInstanceState.getBoolean("DI1"));
        		updateButton(R.id.DI0,savedInstanceState.getBoolean("DI0"));
        		
        		progressBar = (ProgressBar)findViewById(R.id.AI0);
        		progressBar.setProgress(savedInstanceState.getInt("AI0"));
        		firebaseConnector.setInputValue("AI0", 0);
        		
        		ledControl = (LEDControl)findViewById(R.id.DO7);
				ledControl.setState(savedInstanceState.getBoolean("DO7"));
        		
				ledControl = (LEDControl)findViewById(R.id.DO6);
				ledControl.setState(savedInstanceState.getBoolean("DO6"));

				ledControl = (LEDControl)findViewById(R.id.DO5);
				ledControl.setState(savedInstanceState.getBoolean("DO5"));
				
				ledControl = (LEDControl)findViewById(R.id.DO4);
				ledControl.setState(savedInstanceState.getBoolean("DO4"));
				
				ledControl = (LEDControl)findViewById(R.id.DO3);
				ledControl.setState(savedInstanceState.getBoolean("DO3"));
				
				ledControl = (LEDControl)findViewById(R.id.DO2);
				ledControl.setState(savedInstanceState.getBoolean("DO2"));
				
				ledControl = (LEDControl)findViewById(R.id.DO1);
				ledControl.setState(savedInstanceState.getBoolean("DO1"));
				
				ledControl = (LEDControl)findViewById(R.id.DO0);
				ledControl.setState(savedInstanceState.getBoolean("DO0"));
						
        	} catch (Exception e) {
        		//Just in case there is some way for the savedInstanceState to exist but for a single
        		//  item not to exist, lets catch any exceptions that might come.
        	}
        }

        /// Geofence Part
        // Set the pattern for the latitude and longitude format
        String latLngPattern = getString(R.string.lat_lng_pattern);

        // Set the format for latitude and longitude
        mLatLngFormat = new DecimalFormat(latLngPattern);

        // Localize the format
        mLatLngFormat.applyLocalizedPattern(mLatLngFormat.toLocalizedPattern());

        // Set the pattern for the radius format
        String radiusPattern = getString(R.string.radius_pattern);

        // Set the format for the radius
        mRadiusFormat = new DecimalFormat(radiusPattern);

        // Localize the pattern
        mRadiusFormat.applyLocalizedPattern(mRadiusFormat.toLocalizedPattern());

        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceSampleReceiver();

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Instantiate a new geofence storage area
        mPrefs = new SimpleGeofenceStore(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);

        // Get handles to the Geofence editor fields in the UI
        mLatitude1 = (EditText) findViewById(R.id.value_latitude_1);
        mLongitude1 = (EditText) findViewById(R.id.value_longitude_1);
        mRadius1 = (EditText) findViewById(R.id.value_radius_1);

        mActualPositionLat = (TextView) findViewById(R.id.value_actual_position_lat);
        mActualPositionLong = (TextView) findViewById(R.id.value_actual_position_long);

        mActualIMEI = (TextView) findViewById(R.id.value_actual_imei);
        TelephonyManager tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        mActualIMEI.setText(String.valueOf(tMgr.getDeviceId()));

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.w("SenmicroDemoApp", "location changed");
                mActualPositionLat.setText(String.valueOf(location.getLatitude()));
                mActualPositionLong.setText(String.valueOf(location.getLongitude()));
                mActualPosition = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.w("SenmicroDemoApp", "location status changed");
            }

            public void onProviderEnabled(String provider) {
                Log.w("SenmicroDemoApp", "location provider enabled");
            }

            public void onProviderDisabled(String provider) {
                Log.w("SenmicroDemoApp", "location provider disabled");
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        // Set last known location
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mActualPositionLat.setText(String.valueOf(lastLocation.getLatitude()));
        mActualPositionLong.setText(String.valueOf(lastLocation.getLongitude()));
        mLatitude1.setText(String.valueOf(lastLocation.getLatitude()));
        mLongitude1.setText(String.valueOf(lastLocation.getLongitude()));
        mActualPosition = lastLocation;
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
	    if(checkForOpenAccessoryFramework() == false){
	    	showErrorPage(ErrorMessageCode.ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING);
	    	return;
	    } 
	    
		this.setTitle("Senmicro Demo App: Device not connected.");
	}

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     * calls
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(mCurrentGeofences);

                            // If the request was to remove geofences
                        } else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType ){

                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(
                                        mGeofenceRequester.getRequestPendingIntent());

                                // If the removal was by a List of geofence IDs
                            } else {

                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }
                        }
                        break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(GeofenceUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }

    @Override
    public void onResume() {
    	super.onResume();
    	
	    if(checkForOpenAccessoryFramework() == false){
	    	showErrorPage(ErrorMessageCode.ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING);
	    	return;
	    } 
	    
        accessoryManager.enable(this, getIntent());

        // Geofence
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
        /*
         * Get existing geofences from the latitude, longitude, and
         * radius values stored in SharedPreferences. If no values
         * exist, null is returned.
         */
        mUIGeofence1 = mPrefs.getGeofence("1");
        /*
         * If the returned geofences have values, use them to set
         * values in the UI, using the previously-defined number
         * formats.
         */
        
         if (mUIGeofence1 != null) {
            mLatitude1.setText(
                    String.valueOf(mUIGeofence1.getLatitude()));
            mLongitude1.setText(
                    String.valueOf(mUIGeofence1.getLongitude()));
            mRadius1.setText(
                    String.valueOf(mUIGeofence1.getRadius()));
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	if(deviceAttached == false){
    		return;
    	}
    	
	    if(checkForOpenAccessoryFramework() == false){
	    	showErrorPage(ErrorMessageCode.ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING);
			//Call the super function that we are over writing now that we have saved our data.
			super.onSaveInstanceState(savedInstanceState);
	    	return;
	    } 
	    
    	//Save the UI state into the savedInstanceState Bundle.
		//  We only need to save the state of the LEDs since they are the only control.
    	//  The state of the potentiometer and push buttons can be read and restored
    	//  from their current hardware state.
    	
		savedInstanceState.putBoolean("DO0", ((LEDControl)findViewById(R.id.DO0)).getState());
		savedInstanceState.putBoolean("DO1", ((LEDControl)findViewById(R.id.DO1)).getState());
		savedInstanceState.putBoolean("DO2", ((LEDControl)findViewById(R.id.DO2)).getState());
		savedInstanceState.putBoolean("DO3", ((LEDControl)findViewById(R.id.DO3)).getState());
		savedInstanceState.putBoolean("DO4", ((LEDControl)findViewById(R.id.DO4)).getState());
		savedInstanceState.putBoolean("DO5", ((LEDControl)findViewById(R.id.DO5)).getState());
		savedInstanceState.putBoolean("DO6", ((LEDControl)findViewById(R.id.DO6)).getState());
		savedInstanceState.putBoolean("DO7", ((LEDControl)findViewById(R.id.DO7)).getState());
		
		savedInstanceState.putInt("AI0", ((ProgressBar)findViewById(R.id.AI0)).getProgress());
		
		savedInstanceState.putBoolean("DI0", isButtonPressed(R.id.DI0));
		savedInstanceState.putBoolean("DI1", isButtonPressed(R.id.DI1));
		savedInstanceState.putBoolean("DI2", isButtonPressed(R.id.DI2));
		savedInstanceState.putBoolean("DI3", isButtonPressed(R.id.DI3));
		
		//Call the super function that we are over writing now that we have saved our data.
		super.onSaveInstanceState(savedInstanceState);
    }
    
    private boolean checkForOpenAccessoryFramework(){
	    try {
	    	@SuppressWarnings({ "unused", "rawtypes" })
			Class s = Class.forName("com.android.future.usb.UsbManager");
	    	s = Class.forName("com.android.future.usb.UsbAccessory");
	    } catch (ClassNotFoundException e) {
	    	Log.d("ClassNotFound",e.toString());
	    	return false;
	    }
	    return true;
    }
    
    @Override
    public void onPause() {
	    if(checkForOpenAccessoryFramework() == false){
	    	showErrorPage(ErrorMessageCode.ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING);
	    	super.onPause();
	    	return;
	    } 
	    
	    switch(firmwareProtocol) {
		    case 2:
				byte[] commandPacket = new byte[2];
				commandPacket[0] = (byte) APP_DISCONNECT;
				commandPacket[1] = 0;
				accessoryManager.write(commandPacket);	
				break;
	    }
	    
		try {
			while(accessoryManager.isClosed() == false) {
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    	super.onPause();
    	accessoryManager.disable(this);
    	disconnectAccessory();

        mPrefs.setGeofence("1", mUIGeofence1);
    }
   
    
    /** Resets the demo application when a device detaches 
     */
    public void disconnectAccessory() {
    	if(deviceAttached == false) {
    		return;
    	}
    	
    	Log.d(TAG,"disconnectAccessory()");
    	
    	this.setTitle("Senmicro Demo App: Device not connected.");
    	
		LEDControl ledControl;
		ProgressBar progressBar;

		updateButton(R.id.DI3,false);
		updateButton(R.id.DI2,false);
		updateButton(R.id.DI1,false);
		updateButton(R.id.DI0,false);
		
		progressBar = (ProgressBar)findViewById(R.id.AI0);
		progressBar.setProgress(0);
		
		ledControl = (LEDControl)findViewById(R.id.DO7);
		ledControl.setState(false);
		
		ledControl = (LEDControl)findViewById(R.id.DO6);
		ledControl.setState(false);

		ledControl = (LEDControl)findViewById(R.id.DO5);
		ledControl.setState(false);
		
		ledControl = (LEDControl)findViewById(R.id.DO4);
		ledControl.setState(false);
		
		ledControl = (LEDControl)findViewById(R.id.DO3);
		ledControl.setState(false);
		
		ledControl = (LEDControl)findViewById(R.id.DO2);
		ledControl.setState(false);
		
		ledControl = (LEDControl)findViewById(R.id.DO1);
		ledControl.setState(false);
		
		ledControl = (LEDControl)findViewById(R.id.DO0);
		ledControl.setState(false);
		
		LEDButtonEnable(false);
    }
    
    /** 
     * Handler for receiving messages from the USB Manager thread or
     *   the LED control modules
     */
    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		byte[] commandPacket = new byte[2];

			switch(msg.what)
			{				
			case UPDATE_LED_SETTING:
				if(accessoryManager.isConnected() == false) {
					return;
				}
				
				commandPacket[0] = UPDATE_LED_SETTING;
				commandPacket[1] = 0;
				
				if(((LEDControl)findViewById(R.id.DO0)).getState()) {
					commandPacket[1] |= LED_0_ON;
				}
				
				if(((LEDControl)findViewById(R.id.DO1)).getState()) {
					commandPacket[1] |= LED_1_ON;
				}
				
				if(((LEDControl)findViewById(R.id.DO2)).getState()) {
					commandPacket[1] |= LED_2_ON;
				}
				
				if(((LEDControl)findViewById(R.id.DO3)).getState()) {
					commandPacket[1] |= LED_3_ON;
				}
				
				if(((LEDControl)findViewById(R.id.DO4)).getState()) {
					commandPacket[1] |= LED_4_ON;
				}
				
				if(((LEDControl)findViewById(R.id.DO5)).getState()) {
					commandPacket[1] |= LED_5_ON;
				}
				
				if(((LEDControl)findViewById(R.id.DO6)).getState()) {
					commandPacket[1] |= LED_6_ON;
				}
				
				if(((LEDControl)findViewById(R.id.DO7)).getState()) {
					commandPacket[1] |= LED_7_ON;
				}
				
				accessoryManager.write(commandPacket);			
				break;
			
			case USBAccessoryWhat:
				switch(((USBAccessoryManagerMessage)msg.obj).type) {
				case READ:
					if(accessoryManager.isConnected() == false) {
						return;
					}
					
					while(true) {
						if(accessoryManager.available() < 2) {
							//All of our commands in this example are 2 bytes.  If there are less
							//  than 2 bytes left, it is a partial command
							break;
						}
					
						accessoryManager.read(commandPacket);
						
						switch(commandPacket[0]) {
						case POT_STATUS_CHANGE:
							ProgressBar progressBar = (ProgressBar) findViewById(R.id.AI0);
							
							if((commandPacket[1] >= 0) && (commandPacket[1] <= progressBar.getMax())) {
								firebaseConnector.setInputValue("AI0", commandPacket[1]);

								progressBar.setProgress(commandPacket[1]);	
							}
							break;
						case PUSHBUTTON_STATUS_CHANGE:
			    			updateButton(R.id.DI0, ((commandPacket[1] & BUTTON_1_PRESSED) == BUTTON_1_PRESSED)?true:false);
			    			updateButton(R.id.DI1, ((commandPacket[1] & BUTTON_2_PRESSED) == BUTTON_2_PRESSED)?true:false);
			    			updateButton(R.id.DI2, ((commandPacket[1] & BUTTON_3_PRESSED) == BUTTON_3_PRESSED)?true:false);
			    			updateButton(R.id.DI3, ((commandPacket[1] & BUTTON_4_PRESSED) == BUTTON_4_PRESSED)?true:false);
			    			break;
						}
						
					}
					break;
				case CONNECTED:
					break;
				case READY:
					setTitle("Senmicro Demo App: Device connected.");
					
					Log.d(TAG, "Senmicro Demo App:Handler:READY");

					LEDButtonEnable(true);
			    	
					String version = ((USBAccessoryManagerMessage)msg.obj).accessory.getVersion();
					firmwareProtocol = getFirmwareProtocol(version);
					
					switch(firmwareProtocol){
						case 1:
							deviceAttached = true;
							break;
						case 2:
							deviceAttached = true;
							commandPacket[0] = (byte) APP_CONNECT;
							commandPacket[1] = 0;
							Log.d(TAG,"sending connect message.");
							accessoryManager.write(commandPacket);
							Log.d(TAG,"connect message sent.");
							break;
						default:
							showErrorPage(ErrorMessageCode.ERROR_FIRMWARE_PROTOCOL);
							break;
					}
					break;
				case DISCONNECTED:
					disconnectAccessory();
					break;
				}				
				
   				break;
			default:
				break;
			}	//switch
    	} //handleMessage
    }; //handler
    
    private int getFirmwareProtocol(String version) {
    	
    	String major = "0";
    	
    	int positionOfDot;
    	
    	positionOfDot = version.indexOf('.');
    	if(positionOfDot != -1) {
    		major = version.substring(0, positionOfDot);
    	}
    	
    	return new Integer(major).intValue();
    }
    
    private void updateButton(int id, boolean pressed) {
		TextView textviewToUpdate;
		LinearLayout layoutToUpdate;
		
		textviewToUpdate = (TextView)findViewById(id);
		layoutToUpdate = (LinearLayout)textviewToUpdate.getParent();
		
		String button="DI0";
		if (id==R.id.DI1) {
			button="DI1";
		} else if (id==R.id.DI2) {
			button="DI2";
		} else if (id==R.id.DI3) {
			button="DI3";
		}
		firebaseConnector.setInputValue(button, pressed);
		
		if(pressed)
		{
			textviewToUpdate.setText(R.string.pressed);
			layoutToUpdate.setBackgroundResource(R.color.button_pressed);	
		} else {
			textviewToUpdate.setText(R.string.not_pressed);
			layoutToUpdate.setBackgroundResource(0);
		}
    }
    
    private boolean isButtonPressed(int id)
    {
		TextView buttonTextView;
		String buttonText;
		
		buttonTextView = ((TextView)findViewById(id));
		buttonText = buttonTextView.getText().toString();
		return buttonText.equals(getString(R.string.pressed));
    }
	
	private void LEDButtonEnable(boolean enabled) {
		// Set the link to the message handler for this class
		LEDControl ledControl;

		ledControl = ((LEDControl) findViewById(R.id.DO0));
		ledControl.setEnabled(enabled);

		ledControl = ((LEDControl) findViewById(R.id.DO1));
		ledControl.setEnabled(enabled);

		ledControl = ((LEDControl) findViewById(R.id.DO2));
		ledControl.setEnabled(enabled);

		ledControl = ((LEDControl) findViewById(R.id.DO3));
		ledControl.setEnabled(enabled);

		ledControl = ((LEDControl) findViewById(R.id.DO4));
		ledControl.setEnabled(enabled);

		ledControl = ((LEDControl) findViewById(R.id.DO5));
		ledControl.setEnabled(enabled);

		ledControl = ((LEDControl) findViewById(R.id.DO6));
		ledControl.setEnabled(enabled);

		ledControl = ((LEDControl) findViewById(R.id.DO7));
		ledControl.setEnabled(enabled);
	}

    
    private void showErrorPage(ErrorMessageCode error){
    	setContentView(R.layout.error);
    	
    	TextView errorMessage = (TextView)findViewById(R.id.error_message);
    	
    	switch(error){
	    	case ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING:
	    		errorMessage.setText(getResources().getText(R.string.error_missing_open_accessory_framework));
	    		break;
	    	case ERROR_FIRMWARE_PROTOCOL:
	    		errorMessage.setText(getResources().getText(R.string.error_firmware_protocol));
	    		break;
    		default:
    			errorMessage.setText(getResources().getText(R.string.error_default));
    			break;
    	}
    }



    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            //Toast.makeText(context, "onReceive ..." +  action,
            //        Toast.LENGTH_SHORT).show();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

                // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                            ||
                            TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

                // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

                // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, context.getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {
            // Notify user that previous request hasn't finished.
            //Toast.makeText(context, "Geofence Status change",
            //        Toast.LENGTH_SHORT).show();
        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
            // Notify user that previous request hasn't finished.
            Toast.makeText(context, "Geofence Transition occoured!!!",
                    Toast.LENGTH_SHORT).show();
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(GeofenceUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);

            }
            return false;
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    /**
     * Check all the input values and flag those that are incorrect
     * @return true if all the widget values are correct; otherwise false
     */
    private boolean checkInputFields() {
        // Start with the input validity flag set to true
        boolean inputOK = true;

        /*
         * Latitude, longitude, and radius values can't be empty. If they are, highlight the input
         * field in red and put a Toast message in the UI. Otherwise set the input field highlight
         * to black, ensuring that a field that was formerly wrong is reset.
         */
        if (TextUtils.isEmpty(mLatitude1.getText())) {
            mLatitude1.setBackgroundColor(Color.RED);
            Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_SHORT).show();

            // Set the validity to "invalid" (false)
            inputOK = false;
        } else {

            mLatitude1.setBackgroundColor(Color.BLACK);
        }

        if (TextUtils.isEmpty(mLongitude1.getText())) {
            mLongitude1.setBackgroundColor(Color.RED);
            Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_SHORT).show();

            // Set the validity to "invalid" (false)
            inputOK = false;
        } else {

            mLongitude1.setBackgroundColor(Color.BLACK);
        }
        if (TextUtils.isEmpty(mRadius1.getText())) {
            mRadius1.setBackgroundColor(Color.RED);
            Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_SHORT).show();

            // Set the validity to "invalid" (false)
            inputOK = false;
        } else {

            mRadius1.setBackgroundColor(Color.BLACK);
        }

        /*
         * If all the input fields have been entered, test to ensure that their values are within
         * the acceptable range. The tests can't be performed until it's confirmed that there are
         * actual values in the fields.
         */
        if (inputOK) {

            /*
             * Get values from the latitude, longitude, and radius fields.
             */
            double lat1 = Double.valueOf(mLatitude1.getText().toString());
            double lng1 = Double.valueOf(mLongitude1.getText().toString());
            double lat2 = Double.valueOf(mLatitude1.getText().toString());
            double lng2 = Double.valueOf(mLongitude1.getText().toString());
            float rd1 = Float.valueOf(mRadius1.getText().toString());

            /*
             * Test latitude and longitude for minimum and maximum values. Highlight incorrect
             * values and set a Toast in the UI.
             */

            if (lat1 > GeofenceUtils.MAX_LATITUDE || lat1 < GeofenceUtils.MIN_LATITUDE) {
                mLatitude1.setBackgroundColor(Color.RED);
                Toast.makeText(
                        this,
                        R.string.geofence_input_error_latitude_invalid,
                        Toast.LENGTH_SHORT).show();

                // Set the validity to "invalid" (false)
                inputOK = false;
            } else {

                mLatitude1.setBackgroundColor(Color.BLACK);
            }

            if ((lng1 > GeofenceUtils.MAX_LONGITUDE) || (lng1 < GeofenceUtils.MIN_LONGITUDE)) {
                mLongitude1.setBackgroundColor(Color.RED);
                Toast.makeText(
                        this,
                        R.string.geofence_input_error_longitude_invalid,
                        Toast.LENGTH_SHORT).show();

                // Set the validity to "invalid" (false)
                inputOK = false;
            } else {

                mLongitude1.setBackgroundColor(Color.BLACK);
            }

            if (rd1 < GeofenceUtils.MIN_RADIUS) {
                mRadius1.setBackgroundColor(Color.RED);
                Toast.makeText(
                        this,
                        R.string.geofence_input_error_radius_invalid,
                        Toast.LENGTH_SHORT).show();

                // Set the validity to "invalid" (false)
                inputOK = false;
            } else {

                mRadius1.setBackgroundColor(Color.BLACK);
            }
        }

        // If everything passes, the validity flag will still be true, otherwise it will be false.
        return inputOK;
    }

    /**
     * Called when the user clicks the "Register geofences" button.
     * Get the geofence parameters for each geofence and add them to
     * a List. Create the PendingIntent containing an Intent that
     * Location Services sends to this app's broadcast receiver when
     * Location Services detects a geofence transition. Send the List
     * and the PendingIntent to Location Services.
     */
    public void onRegisterGeofenceClicked(View view) {

        // first remove existing geoGeofence
        // removeGeofence();

        /*
         * Record the request as an ADD. If a connection error occurs,
         * the app can automatically restart the add request if Google Play services
         * can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        /*
         * Check that the input fields have values and that the values are with the
         * permitted range
         */
        if (!checkInputFields()) {
            return;
        }

        /*
         * Create a version of geofence 1 that is "flattened" into individual fields. This
         * allows it to be stored in SharedPreferences.
         */
        mUIGeofence1 = new SimpleGeofence(
                "1",
                // Get latitude, longitude, and radius from the UI
                Double.valueOf(mLatitude1.getText().toString()),
                Double.valueOf(mLongitude1.getText().toString()),
                Float.valueOf(mRadius1.getText().toString()),
                // Set the expiration time
                GEOFENCE_EXPIRATION_IN_MILLISECONDS,
                //  detect entry and exit transitions
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        // Store this flat version in SharedPreferences
        mPrefs.setGeofence("1", mUIGeofence1);

        /*
         * Add Geofence objects to a List. toGeofence()
         * creates a Location Services Geofence object from a
         * flat object
         */
        mCurrentGeofences.clear();
        mCurrentGeofences.add(mUIGeofence1.toGeofence());

        // Start the request. Fail if there's already a request in progress
        try {
            // Try to add geofences
            mGeofenceRequester.addGeofences(mCurrentGeofences);
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.add_geofences_already_requested_error,
                    Toast.LENGTH_SHORT).show();
        }

        // Notify user that previous request hasn't finished.
        Toast.makeText(this, "Geofence hinzugefügt",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the user clicks the "Remove geofence 1" button
     * @param view The view that triggered this callback
     */
    public void onUnregisterGeofenceClicked(View view) {
        removeGeofence();

        updateButton(R.id.TI0,false);

        // Notify user that previous request hasn't finished.
        Toast.makeText(this, "Geofence gelöscht",
                Toast.LENGTH_SHORT).show();
    }


    private void removeGeofence() {
        /*
         * Remove the geofence by creating a List of geofences to
         * remove and sending it to Location Services. The List
         * contains the id of geofence 1 ("1").
         * The removal happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done.
         */

        // Create a List of 1 Geofence with the ID "1" and store it in the global list
        mGeofenceIdsToRemove = Collections.singletonList("1");

        /*
         * Record the removal as remove by list. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        // Try to remove the geofence
        try {
            mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

            // Catch errors with the provided geofence IDs
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the user clicks the "Set to here" button
     * @param view The view that triggered this callback
     */
    public void onSetToHereClicked(View view) {
        if (mActualPosition != null) {
            mLatitude1.setText(
                    String.valueOf(mActualPosition.getLatitude()));
            mLongitude1.setText(
                    String.valueOf(mActualPosition.getLongitude()));

            // Notify user that previous request hasn't finished.
            Toast.makeText(this, "Aktuelle Position gesetzt",
                    Toast.LENGTH_SHORT).show();
        }
    }

} //Class definition SenmicroDemoApp