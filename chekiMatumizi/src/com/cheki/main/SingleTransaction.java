package com.cheki.main;

import com.example.evernotempesa.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This activity is triggered when the user clicks on a message. It renders the
 * details of the message.
 * 
 * @author jabala
 * 
 */
public class SingleTransaction extends Activity {

	// message from intent, contains message details
	private String message;

	/**
	 * This method is called when activity is started
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// restore state of activity, set layout
		super.onCreate(savedInstanceState);

		// load XML resource
		this.setContentView(R.layout.transactions);

		// get text view
		TextView txtProduct = (TextView) findViewById(R.id.product_label);

		// get intent for this context
		Intent i = getIntent();

		// getting attached intent data
		message = i.getStringExtra("message");

		// displaying selected message
		txtProduct.setText(message);

	}

}
