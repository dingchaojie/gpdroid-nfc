package net.sourceforge.gpj.cardservices.interfaces;

import java.io.IOException;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import at.fhooe.usmile.gpjshell.GPConnection;
import at.fhooe.usmile.gpjshell.MainActivity;
import at.fhooe.usmile.gpjshell.objects.GPAppletData;

public class NfcTerminal extends GPTerminal {

	private IsoDep mAvailableTag = null;
	private Context mContext = null;
	private static NfcTerminal _INSTANCE = null;
	
	
	public static NfcTerminal getInstance(Context con) {
		synchronized (NfcTerminal.class) {
			if (_INSTANCE == null) {
				_INSTANCE = new NfcTerminal(con);
			}
			return _INSTANCE;
		}
	}

	private NfcTerminal(Context con) {
		mContext = con;
	}
	
	public Card connect(String unused) throws CardException {
		if(mAvailableTag != null) {
			return new NfcSmartcard(mAvailableTag, this);
		}
		throw new CardException("NFC card not present");
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCardPresent() throws CardException {
		return mAvailableTag != null;
	}

	@Override
	public boolean waitForCardPresent(long timeout) throws CardException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForCardAbsent(long timeout) throws CardException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getReader() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setReader(int mReader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// Keep the current NFC tag while Android temporarily pauses this activity
		// for file picking or other UI overlays. Stale tags are cleared on I/O failure
		// or when a new IsoDep tag is received.
		
	}

	@Override
	public boolean isConnected() {
		return mAvailableTag != null;
	}


	public boolean passTag(Tag tag) {
		IsoDep isoDep = IsoDep.get(tag);
		if (isoDep == null) {
			return false;
		}
		clearTag();
		mAvailableTag = isoDep;
		return true;
	}

	public void clearTag() {
		if (mAvailableTag != null) {
			try {
				if (mAvailableTag.isConnected()) {
					mAvailableTag.close();
				}
			} catch (IOException e) {
				// Ignore cleanup errors; a failed close means the tag is already gone.
			}
		}
		mAvailableTag = null;
	}

}
