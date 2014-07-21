package com.cheki.main;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.example.evernotempesa.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * This is the first activity when the application starts. This class
 * authenticates user. It prompts the user to login to Evernote account before
 * proceeding to create note or view transactions.
 * 
 * @author jabala
 * 
 */
public class MainActivity extends ParentActivity {

	// Name of this application, for logging
	private static final String LOG = "MAIN";
	
	// UI elements, login & logout buttons
	private Button loginButton;
	private Button logoutButton;
	private Button homeButton;

	// load image
	private ImageView imageView;

	/**
	 * CONSTRUCTOR
	 * 
	 * This is the method that is called when this activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/**
		 * Load main.xml layout
		 * 
		 * NB: The UI elements are declared in this XML file located in the
		 * layout directory. We then use findViewById(int) to retrieve the
		 * widget that was identified by the id attribute from the XML that was
		 * processed.
		 */

		setContentView(R.layout.main);

		// initialize image view
		imageView = (ImageView) findViewById(R.id.image);

		// initialize the buttons
		loginButton = (Button) findViewById(R.id.login);
		logoutButton = (Button) findViewById(R.id.logout);
		homeButton = (Button) findViewById(R.id.home);
		Log.i(LOG, "INITIALIZED UI");
	}

	/**
	 * This method is called when user returns to this activity.
	 */
	@Override
	public void onResume() {
		super.onResume();
		updateAuthUi();
		Log.i(LOG, "onResume called");
	}

	/**
	 * If user is logged in enable home and logout buttons, disable login button
	 * If user is logged out enable login button, disable home & logout buttons
	 * 
	 * Update the UI based on Evernote authentication state. IF authentication
	 * successful, redirect to main page.
	 */
	private void updateAuthUi() {

		// show login button if user is logged out
		loginButton.setEnabled(!mEvernoteSession.isLoggedIn());

		// Show logout button if user is logged in
		logoutButton.setEnabled(mEvernoteSession.isLoggedIn());

		// Show home button if user is logged in
		homeButton.setEnabled(mEvernoteSession.isLoggedIn());
		Log.i(LOG, "updating UI");
	}

	/**
	 * This method takes the user to home activity.
	 * 
	 * NB: This method is attached to the home button via the loaded XML file
	 * using android:onClick attribute. android:onClick is for API level 4
	 * onwards.
	 */
	public void home(View view) {
		// go to home activity
		startActivity(new Intent(getApplicationContext(), HomeMenu.class));
		Log.i(LOG, "home activity");
	}

	/**
	 * Login to Evernote account.
	 * 
	 * This method is called when the user taps log in button. It initiates the
	 * Evernote OAuth process.
	 * 
	 * NB: This method is attached to the login button via the loaded XML file
	 * using android:onClick attribute. android:onClick is for API level 4
	 * onwards.
	 */

	public void login(View view) {
		mEvernoteSession.authenticate(this);
		Log.i(LOG, "user logging in..");
	}

	/**
	 * Log out from evernote account.
	 * 
	 * This method is called when the user taps log out button. It Clears
	 * Evernote Session and logs out.
	 * 
	 * NB: This method is attached to the logout button via the loaded XML file
	 * using android:onClick attribute. android:onClick is for API level 4
	 * onwards.
	 */
	public void logout(View view) {
		try {
			mEvernoteSession.logOut(this);
		} catch (InvalidAuthenticationException e) {
			Log.e(LOG, "Tried to call logout with not logged in", e);
		}
		// After user logs out, update UI
		updateAuthUi();
	}

	/**
	 * Start HomeMenu activity.
	 * 
	 * This method is called when the control returns from an activity that we
	 * launched. If authentication is successful this method is called to launch
	 * new activity.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {

		// Update UI when oauth activity returns result
		case EvernoteSession.REQUEST_CODE_OAUTH:
			if (resultCode == Activity.RESULT_OK) {
				// if authentication is successful, start HomeMenu activity
				startActivity(new Intent(getApplicationContext(),
						HomeMenu.class));
				Log.i(LOG, "authentication successful");
			}
			break;
		}
	}
}
