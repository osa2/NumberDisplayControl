package info.osa2.android.numdispctrl;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final String HIDE_NUMBER = "184";
	private static final String DISPLAY_NUMBER = "186";

	Vector<String> phoneNumbers = new Vector<String>();
	Vector<String> phoneNames = new Vector<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		String rowData = null;
		String phoneName = null;
		String phoneNumber = null;

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		Cursor c = managedQuery(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		while (c.moveToNext()) {
			String id = c.getString(c
					.getColumnIndex(ContactsContract.Contacts._ID));
			String name = c.getString(c.getColumnIndex("display_name"));
			Cursor phones = managedQuery(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ id, null, null);

			int i = 1;
			while (phones.moveToNext()) {
				phoneNumber = phones
						.getString(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				phoneNumber = phoneNumber.replace("-", "");
				
				if (i == 1) {
					phoneName = name;
				} else {
					phoneName = name + " (" + i + ")";
				}
				
				rowData = id + " " + phoneName + "\n" + phoneNumber;
				adapter.add(rowData);
				phoneNumbers.add(phoneNumber);
				phoneNames.add(phoneName);
				i++;
			}

		}
		c.close();

		ListView listView = (ListView) findViewById(R.id.listView01);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("onItemClick", "position: " + String.valueOf(position));
				TextView textView01 = (TextView) findViewById(R.id.textView01);
				textView01.setText(phoneNames.get(position));
				TextView textView02 = (TextView) findViewById(R.id.textView02);
				textView02.setText(phoneNumbers.get(position));
			}
		});

		Button button01 = (Button) findViewById(R.id.button01);
		button01.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				phoneCall(DISPLAY_NUMBER);
			}
		});

		Button button02 = (Button) findViewById(R.id.button02);
		button02.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				phoneCall(HIDE_NUMBER);
			}
		});
	}

	private void phoneCall(String prefix) {
		TextView textView02 = (TextView) findViewById(R.id.textView02);
		String phoneNumber = textView02.getText().toString();

		if (!"000-0000-0000".equals(phoneNumber)) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:" + prefix + phoneNumber));
			startActivity(intent);
		}
	}
}