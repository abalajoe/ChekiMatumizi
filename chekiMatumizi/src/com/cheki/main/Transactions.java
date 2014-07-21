package com.cheki.main;

import java.util.ArrayList;
import java.util.List;
import com.cheki.db.DatabaseHandler;
import com.cheki.db.Message;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;
import com.example.evernotempesa.R;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class retrieves transactions from database and renders to activity.
 * 
 * @author jabala
 * 
 */
public class Transactions extends ParentActivity implements OnItemClickListener {

	// logging
	private static final String LOG = "TRANSACTIONS";

	// notebook to store note
	private String mSelectedNotebookGuid;

	// list to store message from database
	private List<Message> smsList = new ArrayList<Message>();

	// single transaction
	private String encode;

	// ArrayList to store all messages to be displayed in view
	private ArrayList<String> messages;

	// ArrayAdapter to place messages to List
	private ArrayAdapter<String> adapter;

	// DatbaseHandler instance
	private DatabaseHandler handler;

	// ListView to display messages
	private ListView smsListView;

	// transaction type
	private String transactionType;

	// transaction result
	private String transactionResult;

	// transaction ID
	private String transactionID;

	// transaction result
	private String entity;

	// transaction type
	private String entityNumber;

	// transaction result
	private String amount;

	// transaction result
	private String date;

	// transaction type
	private String time;

	// transaction result
	private String balance;

	/**
	 * This is the callback used as a result of creating a note in a normal
	 * notebook or a linked notebook.
	 */
	private OnClientCallback<Note> mNoteCreateCallback = new OnClientCallback<Note>() {
		@SuppressWarnings("deprecation")
		@Override
		public void onSuccess(Note note) {
			Log.e(LOG, "success saving note");

			// show success alert
			Toast.makeText(getApplicationContext(), R.string.note_saved,
					Toast.LENGTH_LONG).show();
			removeDialog(DIALOG_PROGRESS);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onException(Exception exception) {
			Log.e(LOG, "Error saving note", exception);

			// show error alert
			Toast.makeText(getApplicationContext(), R.string.error_saving_note,
					Toast.LENGTH_LONG).show();
			removeDialog(DIALOG_PROGRESS);
		}
	};

	/**
	 * This method is called when activity is started
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// restore state of activity, set layout
		super.onCreate(savedInstanceState);

		// load XML resource
		this.setContentView(R.layout.transactions);

		// display pop up
		Toast.makeText(getBaseContext(), "View Transactions",
				Toast.LENGTH_SHORT).show();
		Log.i(LOG, "View Transactions");

		// ArrayList to store messages
		messages = new ArrayList<String>();

		// DbHandler object
		handler = new DatabaseHandler(this);

		// get list of messages
		smsList = handler.getMessages();

		// loop through the list and extract specific information
		for (Message message : smsList) {

			// initialize variables for message
			transactionType = message.getTransactionType();
			transactionResult = message.getTransactResult();
			transactionID = message.getTransactionID();
			entity = message.getEntity();
			entityNumber = message.getEntityNumber();
			amount = message.getAmount();
			date = message.getDate();
			time = message.getTime();
			balance = message.getBalance();

			// build message string to be displayed in list view
			encode = "Transaction Type: " + transactionType;
			encode += "\nTransaction Result: " + transactionResult;
			encode += "\nTransaction ID: " + transactionID;
			encode += "\nEntity: " + entity;
			encode += "\nEntity Number: " + entityNumber;
			encode += "\nAmount: " + amount;
			encode += "\nDate: " + date;
			encode += "\nTime: " + time;
			encode += "\nBalance: " + balance;

			// add the built message to ArrayList
			messages.add(encode);
			Log.i("encode", encode);

			// get ListView from layout
			smsListView = (ListView) findViewById(R.id.SMSList);

			adapter = new ArrayAdapter<String>(this, R.layout.style_layout,
					messages);
			
			// bind the ListView to adapter.
			// the adapter provides the ListView with data
			smsListView.setAdapter(adapter);

			// register item click events
			smsListView.setOnItemClickListener(this);

			// save note to EVERNOTE
			//saveNote(smsListView);

		}
	}

	/**
	 * This method saves text field content as note to selected notebook, or
	 * default notebook if no notebook select
	 */
	@SuppressWarnings("deprecation")
	public void saveNote(View view) {
		// note title
		String title = transactionType + " " + transactionResult;

		// note content
		String content = encode;
		
		// only save note if title and content are note empty
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
			Toast.makeText(getApplicationContext(),
					R.string.empty_content_error, Toast.LENGTH_LONG).show();
			return;
		}

		// Note instance
		Note note = new Note();
		note.setTitle(title);

		// TODO: line breaks need to be converted to render in ENML
		note.setContent(EvernoteUtil.NOTE_PREFIX + content
				+ EvernoteUtil.NOTE_SUFFIX);

		if (!mEvernoteSession.getAuthenticationResult().isAppLinkedNotebook()) {

			// If User has selected a notebook guid, assign it now
			if (!TextUtils.isEmpty(mSelectedNotebookGuid)) {
				note.setNotebookGuid(mSelectedNotebookGuid);
			}
			showDialog(DIALOG_PROGRESS);
			try {
				mEvernoteSession.getClientFactory().createNoteStoreClient()
						.createNote(note, mNoteCreateCallback);
			} catch (TTransportException exception) {
				Log.e(LOG, "Error creating notestore", exception);
				Toast.makeText(getApplicationContext(),
						R.string.error_creating_notestore, Toast.LENGTH_LONG)
						.show();
				removeDialog(DIALOG_PROGRESS);
			}
		} else {
			super.createNoteInAppLinkedNotebook(note, mNoteCreateCallback);
		}
	}

	/**
	 * This method starts a new activity upon click of transaction. It renders
	 * all the details of the clicked transaction in the launched activity.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.i(LOG, "List item clicked");

		// selected item
		String msg = ((TextView) view).getText().toString();

		// Launching new Activity on selecting single List Item
		Intent intent = new Intent(getApplicationContext(),
				SingleTransaction.class);

		// sending message details to new activity
		intent.putExtra("message", msg);

		// start the activity
		startActivity(intent);
		Log.i(LOG, "New activity started!");

	}

}
