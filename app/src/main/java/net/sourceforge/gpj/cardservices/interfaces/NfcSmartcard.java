package net.sourceforge.gpj.cardservices.interfaces;

import java.io.IOException;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import android.nfc.tech.IsoDep;

public class NfcSmartcard extends Card {

	private IsoDep mIsoDep = null;
	private NfcTerminal mTerminal = null;
	
	public NfcSmartcard(IsoDep isoDep, NfcTerminal terminal) {
		mIsoDep = isoDep;
		mTerminal = terminal;
	}
	
	@Override
	public ATR getATR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CardChannel getBasicChannel() {
		return new NfcSmartcardChannel(this);
	}

	@Override
	public CardChannel openLogicalChannel() throws CardException {
		return new NfcSmartcardChannel(this);
	}

	@Override
	public void beginExclusive() throws CardException {
		// TODO Auto-generated method stub		
	}

	@Override
	public void endExclusive() throws CardException {
		// TODO Auto-generated method stub
	}

	@Override
	public byte[] transmitControlCommand(int controlCode, byte[] command)
			throws CardException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect(boolean reset) throws CardException {
		// Keep the tapped IsoDep handle available while the card remains in the field.
		// Android invalidates it on real I/O failure, which is handled in transmit/connect.
		
	}

	public ResponseAPDU transmit(CommandAPDU cmd) throws IOException, CardException {
		connect();
		try {
			return new ResponseAPDU(mIsoDep.transceive(cmd.getBytes()));
		} catch (IOException e) {
			clearTag();
			throw e;
		}
		
	}
	
	protected void connect() throws CardException {
		if (mIsoDep == null) {
			throw new CardException("No tag to connect to");
		}
		if (!mIsoDep.isConnected()) {
			try {
				mIsoDep.connect();
			} catch (IOException e) {
				clearTag();
				throw new CardException("Error connecting to tag");
			}
			mIsoDep.setTimeout(30000);
		}
	}

	private void clearTag() {
		if (mTerminal != null) {
			mTerminal.clearTag();
		}
		mIsoDep = null;
	}

	public boolean supportsExtendedLengthApdus() {
		if (mIsoDep == null) {
			return false;
		}
		return mIsoDep.isExtendedLengthApduSupported();
	}
	
}
