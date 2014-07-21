package com.cheki.main;

import com.example.evernotempesa.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This class gives you the option to view transactions, create a new note or go
 * back to home page.
 * 
 * @author jabala
 * 
 */
public class HomeMenu extends Activity {

	// logging
	private static final String LOG = "Home Menu";

	// list view & adapter to hold list values
	private ListView listView;
	private ArrayAdapter<String> mAdapter;

	/**
	 * Listen to clicks on list view and start activity
	 */
	private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position) {
			// if user presses first item on the list, start specified activity
			case 0:
				startActivity(new Intent(getApplicationContext(),
						Transactions.class));
				break;
			// if user presses second item on list, start specified activity
			case 1:
				startActivity(new Intent(getApplicationContext(), NewNote.class));
				break;
			// if user presses third item on list, start specified activity
			case 2:
				startActivity(new Intent(getApplicationContext(),
						MainActivity.class));
				break;
			}

		}
	};

	/**
	 * This method is called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// load XML resource
		setContentView(R.layout.home);

		// initialize list view & adapter for storing list items
		listView = (ListView) findViewById(R.id.list);
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				getResources().getStringArray(R.array.main_list));
		Log.i(LOG, "INITIALIZED UI");

		// bind list to adapter,to get data.listen for events
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(clickListener);
		Log.i(LOG, "LISTENING TO EVENTS");
	}

}
