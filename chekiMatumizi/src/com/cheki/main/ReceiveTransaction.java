package com.cheki.main;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import com.cheki.db.DatabaseHandler;
import com.cheki.db.Message;
import com.cheki.db.Message.TransactionResult;
import com.cheki.db.Message.TransactionType;
import com.cheki.server.MpesaServer;
import com.evernote.client.android.AsyncLinkedNoteStoreClient;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;
import com.example.evernotempesa.R;

/**
 * This class listens for transactions and writes them to EVERNOTE.
 * 
 * @author jabala
 * 
 */
public class ReceiveTransaction extends BroadcastReceiver {

	// logging
	private static final String LOGTAG = "ReceiveTransaction";

	// declare variables for received message details
	private String messageBody;
	private String entity;
	private String entityNumber;
	private String date;
	private String time;
	private String messageAddress;
	private String amount;
	private String transactionID;
	private String remainingBalance;

	// logging
	private static final String LOG = "RECEIVE_TRANSACTIONS";

	// notebook to store note
	private String mSelectedNotebookGuid;

	// ConfigurationReader to populate configurations
	private ConfigurationReader configurationReader;

	// properties
	private Properties properties;

	// EVERNOTE API key & secret
	private String consumerKey;
	private String consumerSecret;

	// EVERNOTE testing service
	private EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

	// Set this to true if you want to allow linked notebooks for accounts that
	// can only access a single notebook
	private boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;

	// EVERNOTE web service API session
	protected EvernoteSession mEvernoteSession;

	// application context
	private Context context;

	// MessageMpesa instance
	private Message message = new Message();

	// declare key for sms
	public static final String SMS_EXTRA_NAME = "pdus";

	/**
	 * This method captures incoming sms once they hit device
	 */
	public void onReceive(Context context, Intent intent) {
		this.context = context;

		// ConfigurationReader instance
		configurationReader = new ConfigurationReader(context);

		// load configuration resource
		properties = configurationReader.getProperties("config.properties");
		Log.i(LOG, "configuration file loaded");

		// get consumer key & secret
		consumerKey = properties.getProperty("consumerkey");
		consumerSecret = properties.getProperty("consumersecret");
		Log.i(LOG, consumerKey + " " + consumerSecret);

		// EVERNOTE session authentication
		mEvernoteSession = EvernoteSession.getInstance(context, consumerKey,
				consumerSecret, EVERNOTE_SERVICE, SUPPORT_APP_LINKED_NOTEBOOKS);

		// fetch data from intent
		Bundle extras = intent.getExtras();
		String message;

		// proceed if data available
		if (extras != null) {

			// get sms received
			Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

			// loop through the number of available messages
			for (int i = 0; i < smsExtra.length; ++i) {

				// get the message received in bytes
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

				// retrieve contents of message
				messageBody = sms.getMessageBody().toString();
				messageAddress = sms.getOriginatingAddress();

				// only receive mpesa messages
				if (messageAddress.equals("MPESA")) {

					// execute this block if message body contains the specified
					// words
					if (messageBody.contains("Failed")
							&& messageBody.contains("airtime")) {

						// split period to get transaction id
						String periodSplit[] = messageBody.split("\\. ");
						transactionID = periodSplit[0];
						Log.i("failMsg", "failMsg");

						// split buy
						String buySplit[] = messageBody.split(" buy ");
						String splitPeriod = buySplit[1];

						// split period to get amount
						String airtime[] = splitPeriod.split("\\. ");
						amount = airtime[0];
						String splitBalance = airtime[1];

						// split balance
						String balanceSplit[] = splitBalance.split("balance");
						String splitIs = balanceSplit[1];

						// split is to get remaining balance
						String isSplit[] = splitIs.split("is ");
						remainingBalance = isSplit[1];

						// store sms to database
						buyAirtimeFail(sms, context);

					}

					// execute this block if message body contains the specified
					// words
					else if (messageBody.contains("Failed")
							&& messageBody.contains("send")) {

						// split enough
						String enoughSplit[] = messageBody.split(" enough ");
						String splitPeriod = enoughSplit[0];

						// split period to get transaction id
						String transaction[] = splitPeriod.split("\\.");
						transactionID = transaction[0];
						String splitSend = enoughSplit[1];

						// split send
						String sendSplit[] = splitSend.split(" send ");
						String splitYou = sendSplit[1];

						// split you to get amount
						String youSplit[] = splitYou.split("\\. You");
						amount = youSplit[0];

						// store sms to database
						sendMoneyFail(sms, context);

					}

					// execute this block if message body contains the specified
					// words
					else if (messageBody.contains("Confirmed")
							&& messageBody.contains("sent")) {

						// split Confirmed. to get transaction id
						String splitConfirmed[] = messageBody
								.split("Confirmed. ");
						transactionID = splitConfirmed[0];
						String splitSent = splitConfirmed[1];

						// split sent to get amount
						String confirmedSplit[] = splitSent.split("sent");
						amount = confirmedSplit[0];
						String splitTo = confirmedSplit[1];

						// split to
						String toSplit[] = splitTo.split("to");
						String splitOn = toSplit[1];

						// split on
						String onSplit[] = splitOn.split("on");
						String splitZero = onSplit[0];

						// split 0 to get entity and entity number
						String zeroSplit[] = splitZero.split(" 0");
						entity = zeroSplit[0];
						entityNumber = zeroSplit[1];
						String splitNew = onSplit[1];

						// split New
						String newSplit[] = splitNew.split("New");
						String splitAt = newSplit[0];

						// split at to get date and time
						String atSplit[] = splitAt.split("at");
						date = atSplit[0];
						time = atSplit[1];
						String splitIs = newSplit[1];

						// split is
						String isSplit[] = splitIs.split("is");
						String splitPin = isSplit[1];

						// split .PIN to get remaining balance
						String pinSplit[] = splitPin.split(".PIN");
						remainingBalance = pinSplit[0];

						// store to db
						sendMoneySuccess(sms, context);

					}

					// execute this block if message body contains the specified
					// words
					else if (messageBody.contains("Confirmed")
							&& messageBody.contains("received")) {

						// split Confirmed. to get transaction id
						String confirmSplit[] = messageBody
								.split(" Confirmed.");
						transactionID = confirmSplit[0];
						String splitReceived = confirmSplit[1];

						// split received
						String receivedSplit[] = splitReceived
								.split("received");
						String splitFrom = receivedSplit[1];

						// split from to get amount
						String fromSplit[] = splitFrom.split("from");
						amount = fromSplit[0];
						String splitOn = fromSplit[1];

						// split on
						String onSplit[] = splitOn.split("on");
						String splitTwo = onSplit[0];

						// split 2 to get entity and entity number
						String twoSplit[] = splitTwo.split("2");
						entity = twoSplit[0];
						entityNumber = twoSplit[1];
						String splitNew = onSplit[1];

						// split New
						String newSplit[] = splitNew.split("New");
						String splitAt = newSplit[0];

						// split at to get date and time
						String atSplit[] = splitAt.split("at");
						date = atSplit[0];
						time = atSplit[1];
						String splitIs = newSplit[1];

						// split is
						String isSplit[] = splitIs.split("is");
						String splitPin = isSplit[1];

						// split .PIN to get remaining balance
						String pinSplit[] = splitPin.split(".PIN");
						remainingBalance = pinSplit[0];

						receiveMoneySuccessful(sms, context);

					}

					// execute this block if message body contains the specified
					// words
					else if (messageBody.contains("Confirmed")
							&& messageBody.contains("Double")) {

						// split Confirmed. to get transaction id
						String splitConfirmed[] = messageBody
								.split(" Confirmed.");
						transactionID = splitConfirmed[0];
						String splitWas = splitConfirmed[1];

						// split was
						String wasSplit[] = splitWas.split("was");
						String splitOn = wasSplit[1];

						// split on to get remaining balance
						String onSplit[] = splitOn.split("on");
						remainingBalance = onSplit[0];
						String splitDouble = onSplit[1];

						// split .Double
						String doubleSplit[] = splitDouble.split(".Double");
						String splitAt = doubleSplit[0];

						// split at to get the date and time
						String atSplit[] = splitAt.split("at");
						date = atSplit[0];
						time = atSplit[1];

						checkBalance(sms, context);

					}

					// execute this block if message body contains the specified
					// words
					else if (messageBody.contains("confirmed")
							&& messageBody.contains("Save")) {

						// split confirmed. to get transaction id
						String splitConfirmed[] = messageBody
								.split(" confirmed.");
						transactionID = splitConfirmed[0];
						String splitBought = splitConfirmed[1];

						// split bought
						String boughtSplit[] = splitBought.split("bought");
						String splitOf = boughtSplit[1];

						// split of to get amount
						String ofSplit[] = splitOf.split("of");
						amount = ofSplit[0];
						String splitOn = ofSplit[1];

						// split on
						String onSplit[] = splitOn.split("on");
						String splitNew = onSplit[1];

						// split New
						String newSplit[] = splitNew.split("New");
						String splitAt = newSplit[0];

						// split at to get date and time
						String atSplit[] = splitAt.split("at");
						date = atSplit[0];
						time = atSplit[1];
						String splitIs = newSplit[1];

						// split is
						String isSplit[] = splitIs.split("is");
						String splitSave = isSplit[1];

						// split .Save to get remaining balance
						String saveSplit[] = splitSave.split(".Save");
						remainingBalance = saveSplit[0];

						buyAirtimesSuccesful(sms, context);

					}

					// execute this block if message body contains the specified
					// words
					else if (messageBody.contains("confirmed")
							&& messageBody.contains("for")) {

						// split confirmed. to get transaction id
						String splitConfirmed[] = messageBody
								.split(" confirmed.");
						transactionID = splitConfirmed[0];
						String splitBought = splitConfirmed[1];

						// split bought
						String boughtSplit[] = splitBought.split("bought");
						String splitOf = boughtSplit[1];

						// split of to get amount
						String ofSplit[] = splitOf.split("of");
						amount = ofSplit[0];
						String splitFor = ofSplit[1];

						// split for
						String forSplit[] = splitFor.split("for");
						String splitOn = forSplit[1];

						// split on to get entity number
						String onSplit[] = splitOn.split("on");
						entityNumber = onSplit[0];
						String splitNew = onSplit[1];

						// split New
						String newSplit[] = splitNew.split("New");
						String splitAt = newSplit[0];

						// split at to get date and time
						String atSplit[] = splitAt.split("at");
						date = atSplit[0];
						time = atSplit[1];
						String splitIs = newSplit[1];

						// split is to get remaining balance
						String isSplit[] = splitIs.split("is");
						remainingBalance = isSplit[1];

						sendAirtimeSuccessful(sms, context);

					}

					// execute this block if message body contains the specified
					// words
					else if (messageBody.contains("Failed")
							&& messageBody.contains("AirTime")) {

						// split Not to get transaction id
						String splitNot[] = messageBody.split(" Not");
						transactionID = splitNot[0];
						String splitBuy = splitNot[1];

						// split buy
						String buySplit[] = splitBuy.split("buy");
						String splitAirtime = buySplit[1];

						// split AirTime to get amount
						String airtimeSplit[] = splitAirtime.split("AirTime");
						amount = airtimeSplit[0];
						String splitIs = airtimeSplit[1];

						// split is to get remaining balance
						String isSplit[] = splitIs.split("is");
						remainingBalance = isSplit[1];

						sendAirtimeFail(sms, context);
					}

					// build notification message
					message = "\nFrom : " + messageAddress;
					message += "\nMessage: " + messageBody;

					// received sms notification
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

					// create note
					saveNote();

					// send message to server
					MpesaServer server = new MpesaServer(transactionID,
							entity, entityNumber, amount, date, time,
							remainingBalance);
					server.execute("");
					Toast.makeText(context, "message sent to server",
							Toast.LENGTH_SHORT).show();
				}

			}
		}
	}

	/**
	 * This method writes the received message to EVERNOTE.
	 */
	public void saveNote() {
		// note title
		String title = messageAddress;

		// note content
		String content = messageBody;

		// pop up
		Toast.makeText(context, content, Toast.LENGTH_LONG).show();

		// only save note if title and content are note empty
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
			Toast.makeText(context, R.string.empty_content_error,
					Toast.LENGTH_LONG).show();
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
			try {
				mEvernoteSession.getClientFactory().createNoteStoreClient()
						.createNote(note, mNoteCreateCallback);
			} catch (TTransportException exception) {
				Log.e(LOG, "Error creating notestore", exception);
				Toast.makeText(context, R.string.error_creating_notestore,
						Toast.LENGTH_LONG).show();
			}
		} else {
			createNoteInAppLinkedNotebook(note, mNoteCreateCallback);
		}
	}

	/**
	 * This is the callback used as a result of creating a note in a normal
	 * notebook or a linked notebook.
	 */
	private OnClientCallback<Note> mNoteCreateCallback = new OnClientCallback<Note>() {
		@Override
		public void onSuccess(Note note) {
			Log.e(LOG, "success saving note");

			// show success alert
			Toast.makeText(context, R.string.note_saved, Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onException(Exception exception) {
			Log.e(LOG, "Error saving note", exception);

			// show error alert
			Toast.makeText(context, R.string.error_saving_note,
					Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * Helper method for apps that have access to a single notebook, and that
	 * notebook is a linked notebook ... find that notebook, gets access to it,
	 * and calls back to the caller.
	 * 
	 * @param callback
	 *            invoked on error or with a client to the linked notebook
	 */
	protected void invokeOnAppLinkedNotebook(
			final OnClientCallback<Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>> callback) {
		try {
			// We need to get the one and only linked notebook
			mEvernoteSession
					.getClientFactory()
					.createNoteStoreClient()
					.listLinkedNotebooks(
							new OnClientCallback<List<LinkedNotebook>>() {
								@Override
								public void onSuccess(
										List<LinkedNotebook> linkedNotebooks) {
									// We should only have one linked notebook
									if (linkedNotebooks.size() != 1) {
										Log.e(LOGTAG,
												"Error getting linked notebook - more than one linked notebook");
										callback.onException(new Exception(
												"Not single linked notebook"));
									} else {
										final LinkedNotebook linkedNotebook = linkedNotebooks
												.get(0);
										mEvernoteSession
												.getClientFactory()
												.createLinkedNoteStoreClientAsync(
														linkedNotebook,
														new OnClientCallback<AsyncLinkedNoteStoreClient>() {
															@Override
															public void onSuccess(
																	AsyncLinkedNoteStoreClient asyncLinkedNoteStoreClient) {
																// Finally
																// create the
																// note in the
																// linked
																// notebook
																callback.onSuccess(new Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>(
																		asyncLinkedNoteStoreClient,
																		linkedNotebook));
															}

															@Override
															public void onException(
																	Exception exception) {
																callback.onException(exception);
															}
														});
									}
								}

								@Override
								public void onException(Exception exception) {
									callback.onException(exception);
								}
							});
		} catch (TTransportException exception) {
			callback.onException(exception);
		}
	}

	/**
	 * Creates the specified note in an app's linked notebook. Used when an app
	 * only has access to a single notebook, and that notebook is a linked
	 * notebook.
	 * 
	 * @param note
	 *            the note to be created
	 * @param createNoteCallback
	 *            called on success or failure
	 */

	protected void createNoteInAppLinkedNotebook(final Note note,
			final OnClientCallback<Note> createNoteCallback) {
		invokeOnAppLinkedNotebook(new OnClientCallback<Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>>() {
			@Override
			public void onSuccess(
					final Pair<AsyncLinkedNoteStoreClient, LinkedNotebook> pair) {
				// Rely on the callback to dismiss the dialog
				pair.first.createNoteAsync(note, pair.second,
						createNoteCallback);
			}

			@Override
			public void onException(Exception exception) {
				Log.e(LOGTAG, "Error creating linked notestore", exception);
				Toast.makeText(context, R.string.error_creating_notestore,
						Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * This method returns formatted date-time
	 * 
	 * @return
	 */
	public String getDate(Long time) {

		// get calendar using specified time zone and locale
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);

		// set current time
		cal.setTimeInMillis(time);

		// format the date
		String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
		return date;
	}

	/**
	 * create note
	 */
	/*
	 * public void createNote() { NoteCreate create = new NoteCreate();
	 * create.setMessage(messageNote); HomeMenu homeMenu = new HomeMenu(create);
	 * }
	 */

	/**
	 * buy airtime operation successful
	 * 
	 * @param sms
	 * @param context
	 */
	private void buyAirtimesSuccesful(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.BUY_AIRTIME.name());
		message.setTransactResult(TransactionResult.SUCCESS.name());
		message.setTransactionID(transactionID);
		message.setAmount(amount);
		message.setDate(date);
		message.setTime(time);
		message.setBalance(remainingBalance);

		// store message to db
		handler.addMessage(message);
	}

	/**
	 * buy airtime operation fail
	 * 
	 * @param sms
	 * @param
	 */
	private void buyAirtimeFail(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.BUY_AIRTIME.name());
		message.setTransactResult(TransactionResult.FAILED.name());
		message.setAmount(amount);
		message.setBalance(remainingBalance);

		// store message to db
		handler.addMessage(message);
	}

	/**
	 * send airtime operation successful
	 * 
	 * @param sms
	 * @param context
	 */
	private void sendAirtimeSuccessful(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.SEND_AIRTIME.name());
		message.setTransactResult(TransactionResult.SUCCESS.name());
		message.setTransactionID(transactionID);
		message.setEntityNumber(entityNumber);
		message.setAmount(amount);
		message.setDate(date);
		message.setTime(time);
		message.setBalance(remainingBalance);

		// store message to db
		handler.addMessage(message);
	}

	/**
	 * send airtime operation fail
	 * 
	 * @param sms
	 * @param context
	 */
	private void sendAirtimeFail(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.SEND_AIRTIME.name());
		message.setTransactResult(TransactionResult.FAILED.name());
		message.setAmount(amount);
		message.setBalance(remainingBalance);

		// store message to db
		handler.addMessage(message);
	}

	/**
	 * send money operation successful
	 * 
	 * @param sms
	 * @param context
	 */
	private void sendMoneySuccess(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.SEND_MONEY.name());
		message.setTransactResult(TransactionResult.SUCCESS.name());
		message.setTransactionID(transactionID);
		message.setEntity(entity);
		message.setEntityNumber(entityNumber);
		message.setAmount(amount);
		message.setDate(date);
		message.setTime(time);
		message.setBalance(remainingBalance);

		// store message to db
		handler.addMessage(message);
	}

	/**
	 * send money operation fail
	 * 
	 * @param sms
	 * @param context
	 */
	private void sendMoneyFail(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.SEND_MONEY.name());
		message.setTransactResult(TransactionResult.FAILED.name());
		message.setAmount(amount);
		message.setBalance(remainingBalance);

		// store message to db
		handler.addMessage(message);
	}

	/**
	 * receive money operation successful
	 * 
	 * @param sms
	 * @param context
	 */
	private void receiveMoneySuccessful(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.RECEIVE_MONEY.name());
		message.setTransactResult(TransactionResult.SUCCESS.name());
		message.setTransactionID(transactionID);
		message.setEntity(entity);
		message.setEntityNumber(entityNumber);
		message.setAmount(amount);
		message.setDate(date);
		message.setTime(time);
		message.setBalance(remainingBalance);
		// store message to db
		handler.addMessage(message);
	}

	/**
	 * check balance operation
	 * 
	 * @param sms
	 * @param context
	 */
	private void checkBalance(SmsMessage sms, Context context) {

		// DbHandler object
		DatabaseHandler handler = new DatabaseHandler(context);

		// set values
		message.setTransactionType(TransactionType.CHECK_BALANCE.name());
		message.setTransactResult(TransactionResult.SUCCESS.name());
		message.setTransactionID(transactionID);
		message.setDate(date);
		message.setTime(time);
		message.setBalance(remainingBalance);

		// store message to db
		handler.addMessage(message);
	}

}
