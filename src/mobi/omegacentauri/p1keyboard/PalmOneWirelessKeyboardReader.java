/* Copyright (C) 2011, Kenneth Skovhede
 * http://www.hexad.dk, opensource@hexad.dk
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mobi.omegacentauri.p1keyboard;

import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

public class PalmOneWirelessKeyboardReader extends RfcommReader {

	private static final String LOG_NAME = "BluezIME:PalmOneWirelessKeyboardReader";
	public static final String DRIVER_NAME = "palmonewireless";
	public static final String DISPLAY_NAME = "PalmOne Wireless Keyboard";

	public int keys[] = new int[0x80];
	public boolean[] down = new boolean[0x80];

	public PalmOneWirelessKeyboardReader(String address, String sessionId, Context context, boolean startnotification) throws Exception {
		super(address, sessionId, context, startnotification);

		setupMap();
	}

	@Override
	public String getDriverName() {
		return DRIVER_NAME;
	}

	private void setupMap() {
		for (int i = 0; i < keys.length ; i++)
			keys[i] = -1;

		keys[0x4e] = KeyEvent.KEYCODE_MINUS;
		keys[0x0e] = KeyEvent.KEYCODE_GRAVE;
		keys[0x55] = KeyEvent.KEYCODE_EQUALS;

		keys[0x66] = KeyEvent.KEYCODE_DEL; // BACKSPACE!
		keys[0x0d] = KeyEvent.KEYCODE_TAB;
		keys[0x54] = KeyEvent.KEYCODE_LEFT_BRACKET;
		keys[0x5b] = KeyEvent.KEYCODE_RIGHT_BRACKET;
		keys[0x5d] = KeyEvent.KEYCODE_BACKSLASH;

		keys[0x4c] = KeyEvent.KEYCODE_SEMICOLON;
		keys[0x52] = KeyEvent.KEYCODE_APOSTROPHE;
		keys[0x5a] = KeyEvent.KEYCODE_ENTER;
		keys[0x48] = KeyEvent.KEYCODE_COMMA;
		keys[0x49] = KeyEvent.KEYCODE_PERIOD;
		keys[0x4a] = KeyEvent.KEYCODE_SLASH;

		keys[0x11] = KeyEvent.KEYCODE_HOME;

		keys[0x12] = KeyEvent.KEYCODE_SHIFT_LEFT;
		keys[0x59] = KeyEvent.KEYCODE_SHIFT_RIGHT;
		keys[0x29] = KeyEvent.KEYCODE_SPACE;

		keys[0x28] = KeyEvent.KEYCODE_DPAD_UP;
		keys[0x5e] = KeyEvent.KEYCODE_DPAD_LEFT;
		keys[0x60] = KeyEvent.KEYCODE_DPAD_DOWN;
		keys[0x2f] = KeyEvent.KEYCODE_DPAD_RIGHT;

		keys[0x16] = KeyEvent.KEYCODE_1;
		keys[0x1e] = KeyEvent.KEYCODE_2;
		keys[0x26] = KeyEvent.KEYCODE_3;
		keys[0x25] = KeyEvent.KEYCODE_4;
		keys[0x2e] = KeyEvent.KEYCODE_5;
		keys[0x36] = KeyEvent.KEYCODE_6;
		keys[0x3d] = KeyEvent.KEYCODE_7;
		keys[0x3e] = KeyEvent.KEYCODE_8;
		keys[0x46] = KeyEvent.KEYCODE_9;
		keys[0x45] = KeyEvent.KEYCODE_0;

		keys[0x15] = KeyEvent.KEYCODE_Q;
		keys[0x1d] = KeyEvent.KEYCODE_W;
		keys[0x24] = KeyEvent.KEYCODE_E;
		keys[0x2d] = KeyEvent.KEYCODE_R;
		keys[0x2c] = KeyEvent.KEYCODE_T;
		keys[0x35] = KeyEvent.KEYCODE_Y;
		keys[0x3c] = KeyEvent.KEYCODE_U;
		keys[0x43] = KeyEvent.KEYCODE_I;
		keys[0x44] = KeyEvent.KEYCODE_O;
		keys[0x4d] = KeyEvent.KEYCODE_P;

		keys[0x1c] = KeyEvent.KEYCODE_A;
		keys[0x1b] = KeyEvent.KEYCODE_S;
		keys[0x23] = KeyEvent.KEYCODE_D;
		keys[0x2b] = KeyEvent.KEYCODE_F;
		keys[0x34] = KeyEvent.KEYCODE_G;
		keys[0x33] = KeyEvent.KEYCODE_H;
		keys[0x3b] = KeyEvent.KEYCODE_J;
		keys[0x42] = KeyEvent.KEYCODE_K;
		keys[0x4b] = KeyEvent.KEYCODE_L;

		keys[0x1a] = KeyEvent.KEYCODE_Z;
		keys[0x22] = KeyEvent.KEYCODE_X;
		keys[0x21] = KeyEvent.KEYCODE_C;
		keys[0x2a] = KeyEvent.KEYCODE_V;
		keys[0x32] = KeyEvent.KEYCODE_B;
		keys[0x31] = KeyEvent.KEYCODE_N;
		keys[0x3a] = KeyEvent.KEYCODE_M;

		keys[0x30] = KeyEvent.KEYCODE_ALT_RIGHT;

		keys[0x03] = KeyEvent.KEYCODE_MENU; // Windows key

		// The following won't work below Android 3.0, probably
		keys[0x02] = KeyEvent.KEYCODE_FUNCTION;
		keys[0x1f] = KeyEvent.KEYCODE_FORWARD_DEL;
		keys[0x58] = KeyEvent.KEYCODE_CAPS_LOCK;
		keys[0x14] = KeyEvent.KEYCODE_CTRL_LEFT;
	}

	@Override
	protected int setupConnection(ImprovedBluetoothDevice device, byte[] readBuffer) throws Exception {
		Exception lastException = null;

		for (int retry = 0; retry < 4 ; retry++) {
			OutputStream os = null;
			m_socket = null;
			lastException = null;

			try {
				m_socket = device.createRfcommSocketToServiceRecord(UUID.fromString(ImprovedBluetoothDevice.SPP));

				m_socket.connect();
				Log.d(LOG_NAME, "Connected to " + m_address);

				// put Brainlink in irDA mode at 9600 baud and switch to bridge mode
				os = m_socket.getOutputStream();

				os.write(new byte[] { '*', 'J', '1', 'Z' } );		        
				//os.close();
			} catch(Exception e) {
				Log.e(LOG_NAME, "On attempt "+retry+" got "+e);
				lastException = e;
				if (os != null) {
					try {
						os.close();
					}
					catch (Exception e2) {
					}
				}
				if (m_socket != null) {
					try {
						m_socket.close();
					}
					catch (Exception e2) {
					}
				}
			}

			if (lastException == null)
				break;
		}

		if (lastException != null)
			throw lastException;


		m_input = m_socket.getInputStream();

		Log.d(LOG_NAME, "Have connection");

		readBuffer[0] = 0;
		return 1;
		//    	return m_input.read(readBuffer);		
	}

	@Override
	protected int parseInputData(byte[] data, int read) {
		//Log.d(LOG_NAME, "Read data: " + getHexString(data, 0, read));
		if (read < 6)
			return read;
		if (data[0] != (byte)0xFF)
			return read - 1;
		if (data[1] != (byte)0xC0 || data[5] != (byte)0xC1)  {
			Log.v(LOG_NAME, "Strange data "+getHexString(data, 0, read));

			return read - 1;
		}


		read -= 6;

		// normally data[3] = data[2] ^ 0xFF and data[4] = data[2] ^ 0x67.
		// Thus each keycode is effectively transmitted three times.  Use
		// majority to recover it in case of corruption.

		int k1 = data[2];
		int k2 = data[3] ^ (byte)0xFF;
		int k3 = data[4] ^ (byte)0x67;

		int key;

		if (k1 == k2 || k1 == k3)
			key = k1;
		else if (k2 == k3)
			key = k2;
		else {
			Log.v(LOG_NAME, "Corrupted packet "+getHexString(data, 0, read));
			return read; // unrecoverably corrupt packet
		}
		
		int action;
		boolean repeat;

		if ( (key & 0x80) != 0 ) {
			action = KeyEvent.ACTION_UP;
			key &= 0x7F;
			repeat = false;
			down[key] = false;
		}
		else {
			action = KeyEvent.ACTION_DOWN;
			repeat = down[key];
			down[key] = true;
		}

		int modifiers = 0;
		
		if (down[0x14] && action == KeyEvent.ACTION_DOWN) {
			int special = 0;

			switch(keys[key]) {
			case KeyEvent.KEYCODE_C:
				special = BluezService.SPECIAL_COPY;
				break;
			case KeyEvent.KEYCODE_V:
				special = BluezService.SPECIAL_PASTE;
				break;
			case KeyEvent.KEYCODE_X:
				special = BluezService.SPECIAL_PASTE;
				break;
			case KeyEvent.KEYCODE_A:
				special = BluezService.SPECIAL_SELECT_ALL;
				break;
			}
			
			if (special > 0) {
				if (!repeat)
					send(action, keys[key], modifiers, special);
				return read;
			}
		}
		
		if (down[0x02] && action == KeyEvent.ACTION_DOWN) {
			// FN key
			switch(keys[key]) {
			case KeyEvent.KEYCODE_3:
				send(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SEARCH, 0, 0);
				send(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SEARCH, 0, 0);
				return read;
			case KeyEvent.KEYCODE_2:
				send(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU, 0, 0);
				send(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU, 0, 0);
				return read;
			case KeyEvent.KEYCODE_1:
				send(action, keys[key], modifiers, BluezService.SPECIAL_HOME);
				return read;
			}
		}
		
		if (keys[key] == KeyEvent.KEYCODE_HOME){
			if (action == KeyEvent.ACTION_DOWN)
				send(action, keys[key], modifiers, BluezService.SPECIAL_HOME);
			return read;
		}
		
		if (keys[key] >= 0) {
			send(action, keys[key], modifiers, 0);
		}

		return read;
	}

	private void send(int action, int keyCode, int modifiers, int special) {
		keypressBroadcast.putExtra(BluezService.EVENT_KEYPRESS_ACTION, action);
		keypressBroadcast.putExtra(BluezService.EVENT_KEYPRESS_KEY, keyCode);
		keypressBroadcast.putExtra(BluezService.EVENT_KEYPRESS_MODIFIERS, modifiers);
		keypressBroadcast.putExtra(BluezService.EVENT_KEYPRESS_ANALOG_EMULATED, false);
		keypressBroadcast.putExtra(BluezService.EVENT_KEYPRESS_SPECIAL, special);
		m_context.sendBroadcast(keypressBroadcast);
	}

	@Override
	protected void validateWelcomeMessage(byte[] data, int read) {
		Log.d(LOG_NAME, "Welcome message is: " + getHexString(data, 0, read));
	}
}
