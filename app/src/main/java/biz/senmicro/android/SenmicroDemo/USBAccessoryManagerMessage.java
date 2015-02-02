package biz.senmicro.android.SenmicroDemo;

import android.hardware.usb.UsbAccessory;

public class USBAccessoryManagerMessage {
	/* Types of messages that can be sent */
	public enum MessageType {
		READ,
		ERROR,
		CONNECTED,
		DISCONNECTED,
		READY
	};

	/* The MessageType for this message instance */
	public MessageType type;
	/* Any text information that needs to be sent with data */
	public String text = null;
	/* Data send in the read MessageType */
	public byte[] data = null;
	/* A USB accessory that attached */
	public UsbAccessory accessory = null;
	
	/** Creates new message of specified type
	 * 
	 * @param type The type of this message
	 */
	public USBAccessoryManagerMessage(MessageType type) {
		this.type = type;
	}
	
	/** Creates a new message of specified type with specified data
	 * 
	 * @param type The type of this message
	 * @param data The data associated with this message
	 */
	public USBAccessoryManagerMessage(MessageType type, byte[] data) {
		this.type = type;
		this.data = data;
	}
	
	/** Creates a new message of specified type with specified data
	 * 
	 * @param type The type of this message
	 * @param data The data associated with this message
	 * @param accessory The accessory associated with this message
	 */
	public USBAccessoryManagerMessage(MessageType type, byte[] data, UsbAccessory accessory) {
		this.type = type;
		this.data = data;
		this.accessory = accessory;
	}
	
	/** Creates a new message of specified type with specified data
	 * 
	 * @param type The type of this message
	 * @param accessory The accessory associated with this message
	 */
	public USBAccessoryManagerMessage(MessageType type, UsbAccessory accessory) {
		this.type = type;
		this.accessory = accessory;
	}
}
