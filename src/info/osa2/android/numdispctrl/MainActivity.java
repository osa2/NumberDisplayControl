package info.osa2.android.numdispctrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final String HIDE_NUMBER = "184";
	private static final String DISPLAY_NUMBER = "186";

    List<List<ContactInfo>> childrenInfo = new ArrayList<List<ContactInfo>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		String sortKey = null;
		String phoneName = null;
		String phoneNumber = null;
		
		Map<String, ContactInfo> contacts = new HashMap<String, ContactInfo>();
		List<String> conKeys = new ArrayList<String>();
		
		Cursor c = managedQuery(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		while (c.moveToNext()) {
			String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
			String name = c.getString(c.getColumnIndex("display_name"));
			
			
			String familyName = "";
			String givenName = "";
			Cursor yomi = managedQuery(
					ContactsContract.Data.CONTENT_URI, null,
					ContactsContract.Data.CONTACT_ID + " = " + id,
					null, null);
			if (yomi.moveToFirst()) {
				familyName = yomi.getString(yomi.getColumnIndex(ContactsContract.Data.DATA9));
				if (familyName == null) {
					familyName = "";
				}
				givenName = yomi.getString(yomi.getColumnIndex(ContactsContract.Data.DATA7));
				if (givenName == null) {
					givenName = "";
				}
			}
						
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
				
				if ("".equals(familyName + givenName)) {
					sortKey = phoneNumber;
				}
				else {
    				sortKey = familyName + givenName + new Integer(i).toString();
				}
				conKeys.add(sortKey);
				
				ContactInfo ci = new ContactInfo();
				ci.setPhoneName(phoneName);
				ci.setPhoneNumber(phoneNumber);
				ci.setFamilyName(familyName);
				ci.setGivenName(givenName);
				contacts.put(sortKey, ci);
				
				i++;
			}

		}
		c.close();
		
        List<Map<String, Object>> parentsList = new ArrayList<Map<String, Object>>();
        List<List<Map<String, Object>>> childrenList = new ArrayList<List<Map<String, Object>>>();

        
        Map<String, Object> parentData;
        Map<String, Object> childData;
        List<Map<String, Object>> childList;
        List<ContactInfo> childInfo;
        
        Map<String, String> kanaIndex = new HashMap<String, String>();
        makeKanaIndex(kanaIndex);
		
        String kanaLabel = "";
        String kanaBack = "";
        String conKey = "";
        parentData = new HashMap<String, Object>();
        childList = new ArrayList<Map<String, Object>>();
        childInfo = new ArrayList<ContactInfo>();

        Collections.sort(conKeys);
		Iterator<String> itr = conKeys.iterator();
		while (itr.hasNext()) {
			conKey = itr.next();
			String srch = String.valueOf(conKey.charAt(0));
            kanaLabel = kanaIndex.get(srch);
            if (kanaLabel == null) {
            	kanaLabel = "その他";
            }
            if (!kanaLabel.equals(kanaBack)) {
            	// ブレーク処理
            	if (!"".equals(kanaBack)) {
            	    parentsList.add(parentData);
            	    childrenList.add(childList);
            	    childrenInfo.add(childInfo);
            	}
            	parentData = new HashMap<String, Object>();
            	parentData.put("parent_text", kanaLabel);
            	childList = new ArrayList<Map<String, Object>>();
                childInfo = new ArrayList<ContactInfo>();
            }
            kanaBack = kanaLabel;

			ContactInfo ci = contacts.get(conKey);
			childData = new HashMap<String, Object>();
			childData.put("child_text", ci.getRowTextDebug());
			childList.add(childData);
			childInfo.add(ci);
		}
	    parentsList.add(parentData);
	    childrenList.add(childList);
	    childrenInfo.add(childInfo);

        SimpleExpandableListAdapter adapter
        = new SimpleExpandableListAdapter
            (this,
            		parentsList,
            		android.R.layout.simple_expandable_list_item_1,
            		new String [] {"parent_text"},
            		new int [] {android.R.id.text1},
            		childrenList,
            		R.layout.raw,
            		new String [] {"child_text"},
            		new int [] {R.id.child_text}
            		);
    
        ExpandableListView listView = (ExpandableListView)findViewById(R.id.listView01);
		listView.setAdapter(adapter);
		
		listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View view,
					int parentPosition, int childPosition, long id) {
				Log.d("onChildClick", "parentPosition: " + String.valueOf(parentPosition));
				Log.d("onChildClick", "childPosition : " + String.valueOf(childPosition));
				
				TextView textView01 = (TextView) findViewById(R.id.textView01);
				textView01.setText(childrenInfo.get(parentPosition).get(childPosition).getPhoneName());
				TextView textView02 = (TextView) findViewById(R.id.textView02);
				textView02.setText(childrenInfo.get(parentPosition).get(childPosition).getPhoneNumber());

				return false;
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

		if (!"".equals(phoneNumber)) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:" + prefix + phoneNumber));
			startActivity(intent);
		}
	}
	
	private Map<String, String> makeKanaIndex(Map<String, String> kanaIndex) {
		
        kanaIndex.put("#", "クイック");

        kanaIndex.put("ｱ", "あ行");
        kanaIndex.put("ｲ", "あ行");
        kanaIndex.put("ｳ", "あ行");
        kanaIndex.put("ｴ", "あ行");
        kanaIndex.put("ｵ", "あ行");
        kanaIndex.put("あ", "あ行");
        kanaIndex.put("い", "あ行");
        kanaIndex.put("う", "あ行");
        kanaIndex.put("え", "あ行");
        kanaIndex.put("お", "あ行");
        kanaIndex.put("ア", "あ行");
        kanaIndex.put("イ", "あ行");
        kanaIndex.put("ウ", "あ行");
        kanaIndex.put("ヴ", "あ行");
        kanaIndex.put("エ", "あ行");
        kanaIndex.put("オ", "あ行");
        
        kanaIndex.put("ｶ", "か行");
        kanaIndex.put("ｷ", "か行");
        kanaIndex.put("ｸ", "か行");
        kanaIndex.put("ｹ", "か行");
        kanaIndex.put("ｺ", "か行");
        kanaIndex.put("か", "か行");
        kanaIndex.put("き", "か行");
        kanaIndex.put("く", "か行");
        kanaIndex.put("け", "か行");
        kanaIndex.put("こ", "か行");
        kanaIndex.put("カ", "か行");
        kanaIndex.put("キ", "か行");
        kanaIndex.put("ク", "か行");
        kanaIndex.put("ケ", "か行");
        kanaIndex.put("コ", "か行");
        kanaIndex.put("が", "か行");
        kanaIndex.put("ぎ", "か行");
        kanaIndex.put("ぐ", "か行");
        kanaIndex.put("げ", "か行");
        kanaIndex.put("ご", "か行");
        kanaIndex.put("ガ", "か行");
        kanaIndex.put("ギ", "か行");
        kanaIndex.put("グ", "か行");
        kanaIndex.put("ゲ", "か行");
        kanaIndex.put("ゴ", "か行");
        
        kanaIndex.put("ｻ", "さ行");
        kanaIndex.put("ｼ", "さ行");
        kanaIndex.put("ｽ", "さ行");
        kanaIndex.put("ｾ", "さ行");
        kanaIndex.put("ｿ", "さ行");
        kanaIndex.put("さ", "さ行");
        kanaIndex.put("し", "さ行");
        kanaIndex.put("す", "さ行");
        kanaIndex.put("せ", "さ行");
        kanaIndex.put("そ", "さ行");
        kanaIndex.put("サ", "さ行");
        kanaIndex.put("シ", "さ行");
        kanaIndex.put("ス", "さ行");
        kanaIndex.put("セ", "さ行");
        kanaIndex.put("ソ", "さ行");
        kanaIndex.put("ざ", "さ行");
        kanaIndex.put("じ", "さ行");
        kanaIndex.put("ず", "さ行");
        kanaIndex.put("ぜ", "さ行");
        kanaIndex.put("ぞ", "さ行");
        kanaIndex.put("ザ", "さ行");
        kanaIndex.put("ジ", "さ行");
        kanaIndex.put("ズ", "さ行");
        kanaIndex.put("ゼ", "さ行");
        kanaIndex.put("ゾ", "さ行");
        
        kanaIndex.put("ﾀ", "た行");
        kanaIndex.put("ﾁ", "た行");
        kanaIndex.put("ﾂ", "た行");
        kanaIndex.put("ﾃ", "た行");
        kanaIndex.put("ﾄ", "た行");
        kanaIndex.put("た", "た行");
        kanaIndex.put("ち", "た行");
        kanaIndex.put("つ", "た行");
        kanaIndex.put("て", "た行");
        kanaIndex.put("と", "た行");
        kanaIndex.put("タ", "た行");
        kanaIndex.put("チ", "た行");
        kanaIndex.put("ツ", "た行");
        kanaIndex.put("テ", "た行");
        kanaIndex.put("ト", "た行");
        kanaIndex.put("だ", "た行");
        kanaIndex.put("ぢ", "た行");
        kanaIndex.put("づ", "た行");
        kanaIndex.put("で", "た行");
        kanaIndex.put("ど", "た行");
        kanaIndex.put("ダ", "た行");
        kanaIndex.put("ヂ", "た行");
        kanaIndex.put("ヅ", "た行");
        kanaIndex.put("デ", "た行");
        kanaIndex.put("ド", "た行");

        kanaIndex.put("ﾅ", "な行");
        kanaIndex.put("ﾆ", "な行");
        kanaIndex.put("ﾇ", "な行");
        kanaIndex.put("ﾈ", "な行");
        kanaIndex.put("ﾉ", "な行");
        kanaIndex.put("な", "な行");
        kanaIndex.put("に", "な行");
        kanaIndex.put("ぬ", "な行");
        kanaIndex.put("ね", "な行");
        kanaIndex.put("の", "な行");
        kanaIndex.put("ナ", "な行");
        kanaIndex.put("ニ", "な行");
        kanaIndex.put("ヌ", "な行");
        kanaIndex.put("ネ", "な行");
        kanaIndex.put("ノ", "な行");

        kanaIndex.put("ﾊ", "は行");
        kanaIndex.put("ﾋ", "は行");
        kanaIndex.put("ﾌ", "は行");
        kanaIndex.put("ﾍ", "は行");
        kanaIndex.put("ﾎ", "は行");
        kanaIndex.put("は", "は行");
        kanaIndex.put("ひ", "は行");
        kanaIndex.put("ふ", "は行");
        kanaIndex.put("へ", "は行");
        kanaIndex.put("ほ", "は行");
        kanaIndex.put("ハ", "は行");
        kanaIndex.put("ヒ", "は行");
        kanaIndex.put("フ", "は行");
        kanaIndex.put("ヘ", "は行");
        kanaIndex.put("ホ", "は行");
        kanaIndex.put("ば", "は行");
        kanaIndex.put("び", "は行");
        kanaIndex.put("ぶ", "は行");
        kanaIndex.put("べ", "は行");
        kanaIndex.put("ぼ", "は行");
        kanaIndex.put("バ", "は行");
        kanaIndex.put("ビ", "は行");
        kanaIndex.put("ブ", "は行");
        kanaIndex.put("ベ", "は行");
        kanaIndex.put("ボ", "は行");
        kanaIndex.put("ぱ", "は行");
        kanaIndex.put("ぴ", "は行");
        kanaIndex.put("ぷ", "は行");
        kanaIndex.put("ぺ", "は行");
        kanaIndex.put("ぽ", "は行");
        kanaIndex.put("パ", "は行");
        kanaIndex.put("ピ", "は行");
        kanaIndex.put("プ", "は行");
        kanaIndex.put("ペ", "は行");
        kanaIndex.put("ポ", "は行");

        kanaIndex.put("ﾏ", "ま行");
        kanaIndex.put("ﾐ", "ま行");
        kanaIndex.put("ﾑ", "ま行");
        kanaIndex.put("ﾒ", "ま行");
        kanaIndex.put("ﾓ", "ま行");
        kanaIndex.put("ま", "ま行");
        kanaIndex.put("み", "ま行");
        kanaIndex.put("む", "ま行");
        kanaIndex.put("め", "ま行");
        kanaIndex.put("も", "ま行");
        kanaIndex.put("マ", "ま行");
        kanaIndex.put("ミ", "ま行");
        kanaIndex.put("ム", "ま行");
        kanaIndex.put("メ", "ま行");
        kanaIndex.put("モ", "ま行");

        kanaIndex.put("ﾔ", "や行");
        kanaIndex.put("ﾕ", "や行");
        kanaIndex.put("ﾖ", "や行");
        kanaIndex.put("や", "や行");
        kanaIndex.put("ゆ", "や行");
        kanaIndex.put("よ", "や行");
        kanaIndex.put("ヤ", "や行");
        kanaIndex.put("ユ", "や行");
        kanaIndex.put("ヨ", "や行");

        kanaIndex.put("ﾗ", "ら行");
        kanaIndex.put("ﾘ", "ら行");
        kanaIndex.put("ﾙ", "ら行");
        kanaIndex.put("ﾚ", "ら行");
        kanaIndex.put("ﾛ", "ら行");
        kanaIndex.put("ら", "ら行");
        kanaIndex.put("り", "ら行");
        kanaIndex.put("る", "ら行");
        kanaIndex.put("れ", "ら行");
        kanaIndex.put("ろ", "ら行");
        kanaIndex.put("ラ", "ら行");
        kanaIndex.put("リ", "ら行");
        kanaIndex.put("ル", "ら行");
        kanaIndex.put("レ", "ら行");
        kanaIndex.put("ロ", "ら行");

        kanaIndex.put("ﾜ", "わ行");
        kanaIndex.put("わ", "わ行");
        kanaIndex.put("ワ", "わ行");

		return kanaIndex;
	}
}

class ContactInfo {
	private String phoneNumber;
	private String phoneName;
	private String familyName;
	private String givenName;
	
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	public String getFullName() {
		return this.familyName + " " + this.givenName;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoneName() {
		return phoneName;
	}
	public void setPhoneName(String phoneName) {
		this.phoneName = phoneName;
	}
	public String getRowText() {
		return this.getPhoneName() + "\n" + this.phoneNumber;
	}
	public String getRowTextDebug() {
		return this.getPhoneName() + " [" + this.familyName + this.givenName + "]\n" + this.phoneNumber;
	}
}