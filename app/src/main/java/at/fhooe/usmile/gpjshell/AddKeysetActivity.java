/*******************************************************************************
 * Copyright (c) 2014 Michael Hölzl <mihoelzl@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Hölzl <mihoelzl@gmail.com> - initial implementation
 *     Thomas Sigmund - data base, key set, channel set selection and GET DATA integration
 ******************************************************************************/
package at.fhooe.usmile.gpjshell;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.fhooe.usmile.gpjshell.db.KeysetDataSource;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class AddKeysetActivity extends Activity {
	private EditText editID;
	private EditText editVersion;
	private EditText editName;
	private EditText editMAC;
	private EditText editENC;
	private EditText editKEK;
	private Button mPositive;
	private Button mNegative;
	private static final int GP_KEY_HEX_LENGTH = 32;
	private static final String NXP_J3R452_MAC = "926cd4d4030b9bd778fd4e888bebcc19";
	private static final String NXP_J3R452_ENC = "92373832e706a169d7e4225ce865280c";
	private static final String NXP_J3R200_MAC = "9bef06c71b136ce96297f433efc0e07a";
	private static final String NXP_J3R200_ENC = "0fc1253f5faa2f51c7a434c86c7c5945";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_keyset);
		editID = (EditText) findViewById(R.id.edit_keyset_id);
		editVersion = (EditText) findViewById(R.id.edit_keyset_version);
		editName = (EditText) findViewById(R.id.edit_keyset_name);
		editMAC = (EditText) findViewById(R.id.edit_keyset_mac);
		editENC = (EditText) findViewById(R.id.edit_keyset_enc);
		editKEK = (EditText) findViewById(R.id.edit_keyset_kek);

		mPositive = (Button) findViewById(R.id.btn_install_applet);
		mPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = editName.getText().toString().trim();
				String idText = editID.getText().toString().trim();
				String versionText = editVersion.getText().toString().trim();
				String mac = normalizeHex(editMAC.getText().toString());
				String enc = normalizeHex(editENC.getText().toString());
				String kek = normalizeHex(editKEK.getText().toString());

				if (name.length() == 0) {
					Toast.makeText(AddKeysetActivity.this, "Please enter a keyset name", Toast.LENGTH_LONG).show();
					return;
				}

				if (idText.length() == 0 || versionText.length() == 0) {
					Toast.makeText(AddKeysetActivity.this, "Please enter valid ID and Version", Toast.LENGTH_LONG).show();
					return;
				}

				if (!isValidKey(mac) || !isValidKey(enc) || !isValidKey(kek)) {
					Toast.makeText(AddKeysetActivity.this, "MAC, ENC and KEK must be 16-byte hex values (32 hex characters)", Toast.LENGTH_LONG).show();
					return;
				}

				if (looksLikeMacEncSwapped(mac, enc)) {
					Toast.makeText(AddKeysetActivity.this, "MAC and ENC look swapped. Put 926c... in MAC and 9237... in ENC for J3R452.", Toast.LENGTH_LONG).show();
					return;
				}

				try {
					//set unique id to -1. it will be set by DB later
					GPKeyset keyset = new GPKeyset(-1, name,
							Integer.valueOf(idText), Integer.valueOf(versionText),
							mac, enc, kek, null);
					Intent intent = new Intent();
					intent.putExtra(GPKeyset.KEYSET, keyset);
					setResult(RESULT_OK, intent);
					
					KeysetDataSource source = new KeysetDataSource(AddKeysetActivity.this);
					source.open();
					boolean containsKey = source.containsKeyset(keyset.getName(), getReaderName(), keyset.getID());
					source.close();
					
					if (containsKey)
						createDialog().show();
					else
						finish();
					
				} catch (NumberFormatException e) {
					Toast.makeText(AddKeysetActivity.this, "Please enter valid ID and Version", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		
		

		mNegative = (Button) findViewById(R.id.btn_list_applets);
		mNegative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
	
	public Dialog createDialog() {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.keyset_dialog_title);
        builder.setMessage(R.string.keyset_dialog_ask_overwrite)
               .setPositiveButton(R.string.keyset_positive, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       AddKeysetActivity.this.finish();
                   }
               })
               .setNegativeButton(R.string.keyset_negative, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   dialog.cancel();
                   }
               });
        return builder.create();
    }

	private String getReaderName() {
		Bundle extras = getIntent().getExtras();
		if (extras == null)
			return null;
		return extras.getString("readername");
	}

	private String normalizeHex(String value) {
		return value.replace("0x", "")
				.replace("0X", "")
				.replace(" ", "")
				.replace(":", "")
				.replace("-", "")
				.trim()
				.toLowerCase();
	}

	private boolean isValidKey(String value) {
		if (value.length() != GP_KEY_HEX_LENGTH)
			return false;

		for (int i = 0; i < value.length(); i++) {
			if (Character.digit(value.charAt(i), 16) < 0)
				return false;
		}
		return true;
	}

	private boolean looksLikeMacEncSwapped(String mac, String enc) {
		return (NXP_J3R452_ENC.equals(mac) && NXP_J3R452_MAC.equals(enc))
				|| (NXP_J3R200_ENC.equals(mac) && NXP_J3R200_MAC.equals(enc));
	}
}
