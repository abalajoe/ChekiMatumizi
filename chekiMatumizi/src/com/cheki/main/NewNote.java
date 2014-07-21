package com.cheki.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;
import com.example.evernotempesa.R;
import java.util.List;

/**
 * This class prompts a user to fill in title and content 
 * and saves them to EVERNOTE.
 * 
 * @author jabala
 * 
 */
public class NewNote extends ParentActivity {

	// logging
	private static final String LOG = "NewNote";

	// title & content fields
	private EditText titleText;
	private EditText contentText;

	// save and notebook select buttons
	private Button saveButton;
	private Button selectButton;

	// select notebook
	private String mSelectedNotebookGuid;

	/**
	 * Callback used as a result of creating a note in a normal notebook or a
	 * linked notebook
	 */
	private OnClientCallback<Note> mNoteCreateCallback = new OnClientCallback<Note>() {
		@SuppressWarnings("deprecation")
		@Override
		public void onSuccess(Note note) {
			Toast.makeText(getApplicationContext(), R.string.note_saved,
					Toast.LENGTH_LONG).show();
			removeDialog(DIALOG_PROGRESS);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onException(Exception exception) {
			Log.e(LOG, "Error saving note", exception);
			Toast.makeText(getApplicationContext(), R.string.error_saving_note,
					Toast.LENGTH_LONG).show();
			removeDialog(DIALOG_PROGRESS);
		}
	};

	/**
	 * This method is called when activity is created.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// load XML resource
		setContentView(R.layout.new_note);

		// initialize UI
		titleText = (EditText) findViewById(R.id.text_title);
		contentText = (EditText) findViewById(R.id.text_content);
		selectButton = (Button) findViewById(R.id.select_button);
		saveButton = (Button) findViewById(R.id.save_button);

	}

	/**
	 * This method saves text field content as note to selected notebook, or
	 * default notebook if no notebook select
	 */
	@SuppressWarnings("deprecation")
	public void saveNote(View view) {
		// get the title and content
		String title = titleText.getText().toString();
		String content = contentText.getText().toString();

		// only save when title and content are not empty
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
	 * This method selects notebook. create AlertDialog to pick notebook guid
	 */
	@SuppressWarnings("deprecation")
	public void selectNotebook(View view) {
		if (mEvernoteSession.isAppLinkedNotebook()) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.CANT_LIST_APP_LNB), Toast.LENGTH_LONG)
					.show();
			return;
		}

		try {
			mEvernoteSession.getClientFactory().createNoteStoreClient()
					.listNotebooks(new OnClientCallback<List<Notebook>>() {
						int mSelectedPos = -1;

						@Override
						public void onSuccess(final List<Notebook> notebooks) {
							CharSequence[] names = new CharSequence[notebooks
									.size()];
							int selected = -1;
							Notebook notebook = null;
							for (int index = 0; index < notebooks.size(); index++) {
								notebook = notebooks.get(index);
								names[index] = notebook.getName();
								if (notebook.getGuid().equals(
										mSelectedNotebookGuid)) {
									selected = index;
								}
							}

							AlertDialog.Builder builder = new AlertDialog.Builder(
									NewNote.this);

							builder.setSingleChoiceItems(names, selected,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mSelectedPos = which;
										}
									})
									.setPositiveButton(
											R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													if (mSelectedPos > -1) {
														mSelectedNotebookGuid = notebooks
																.get(mSelectedPos)
																.getGuid();
													}
													dialog.dismiss();
												}
											}).create().show();
						}

						@Override
						public void onException(Exception exception) {
							Log.e(LOG, "Error listing notebooks", exception);
							Toast.makeText(getApplicationContext(),
									R.string.error_listing_notebooks,
									Toast.LENGTH_LONG).show();
							removeDialog(DIALOG_PROGRESS);
						}
					});
		} catch (TTransportException exception) {
			Log.e(LOG, "Error creating notestore", exception);
			Toast.makeText(getApplicationContext(),
					R.string.error_creating_notestore, Toast.LENGTH_LONG)
					.show();
			removeDialog(DIALOG_PROGRESS);
		}
	}
}
