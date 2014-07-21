package com.cheki.db;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class provides an abstract layer between the underlying SQLite database
 * and the activity class. Activity class calls this class to interact with the
 * database.
 * 
 * @author jabala
 * 
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	// declare constants for database name, table name, database version
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "DBmessage.db";
	public static final String TABLE_MESSAGES = "messages";

	// declare constants for table columns
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_TRANSACTIONTYPE = "transactionType";
	public static final String COLUMN_TRANSACTIONRESULT = "transactionResult";
	public static final String COLUMN_ENTITY = "entity";
	public static final String COLUMN_TRANSACTIONID = "transactionID";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_ENTITYNUMBER = "entityNumber";
	public static final String COLUMN_TRANSACTIONDATE = "date";
	public static final String COLUMN_TRANSACTIONTIME = "time";
	public static final String COLUMN_BALANCE = "balance";
	// initialize context
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Create message table when database is first created.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_MESSAGE_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_TRANSACTIONTYPE
				+ " TEXT," + COLUMN_TRANSACTIONRESULT + " TEXT,"
				+ COLUMN_TRANSACTIONID + " VARCHAR," + COLUMN_ENTITY + " TEXT,"
				+ COLUMN_ENTITYNUMBER + " VARCHAR," + COLUMN_AMOUNT
				+ " VARCHAR," + COLUMN_TRANSACTIONDATE + " VARCHAR,"
				+ COLUMN_TRANSACTIONTIME + " VARCHAR," + COLUMN_BALANCE
				+ " VARCHAR" + ")";
		db.execSQL(CREATE_MESSAGE_TABLE);
	}

	/**
	 * This method removes old database and creates new one when handler is
	 * invoked with greater database version than the one currently used.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
		onCreate(db);
	}

	/**
	 * This method adds a message object to database.
	 * 
	 * @param message
	 */
	public void addMessage(Message message) {

		// ContentValues object to for storing values
		ContentValues values = new ContentValues();

		// add values to set
		values.put(COLUMN_TRANSACTIONTYPE, message.getTransactionType());
		values.put(COLUMN_TRANSACTIONRESULT, message.getTransactResult());
		values.put(COLUMN_TRANSACTIONID, message.getTransactionID());
		values.put(COLUMN_ENTITY, message.getEntity());
		values.put(COLUMN_ENTITYNUMBER, message.getEntityNumber());
		values.put(COLUMN_AMOUNT, message.getAmount());
		values.put(COLUMN_TRANSACTIONDATE, message.getDate());
		values.put(COLUMN_TRANSACTIONTIME, message.getTime());
		values.put(COLUMN_BALANCE, message.getBalance());
		
		String msg = message.getTransactionType()
				+ " " +message.getTransactResult()
				+ " " +message.getTransactionID()
				+ " " + message.getEntity()
				+ " " + message.getEntityNumber()
				+ " " + message.getAmount()
				+ " " + message.getDate()
				+ " " + message.getTime()
				+ " " + message.getBalance();
		Log.i("DATABASE", msg);
				
		// open database for writing
		SQLiteDatabase db = this.getWritableDatabase();

		// insert row into database table
		db.insert(TABLE_MESSAGES, null, values);

		// close database connection
		db.close();
	}
	
	/**
	 * This method gets all messages from database.
	 * 
	 * @return
	 */
	public List<Message> getMessages() {

		// initialize ArrayList variable
		ArrayList<Message> messageList = new ArrayList<>();

		// get all messages from specified table
		String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES;

		// open database for reading
		SQLiteDatabase db = this.getWritableDatabase();

		// run query and return result set
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to ArrayList
		if (cursor.moveToFirst()) {
			do {
				// Message object
				Message message = new Message();

				/* set the id, transaction type, transaction result
				 * transaction id, entity, entity nimber, amount
				 * date, time and balance
				 */
				message.setId(Integer.parseInt(cursor.getString(0)));
				message.setTransactionType(cursor.getString(1));
				message.setTransactResult(cursor.getString(2));
				message.setTransactionID(cursor.getString(3));
				message.setEntity(cursor.getString(4));
				message.setEntityNumber((cursor.getString(5)));
				message.setAmount(cursor.getString(6));
				message.setDate(cursor.getString(7));
				message.setTime(cursor.getString(8));
				message.setBalance(cursor.getString(9));

				// Adding message to ArrayList
				messageList.add(message);
			} while (cursor.moveToNext());
		}

		// return ArrayList containing the message object
		return messageList;
	}
	
	// Deleting single contact
    public void deleteContact(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, COLUMN_ID + " = ?", new String[] { String.valueOf(message.getId()) });
        db.close();
    }
    
    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }
    
    public void delete_byID(int id){
    	SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
		sqLiteDatabase.delete(TABLE_MESSAGES, COLUMN_ID+"="+id, null);
	}
    
    public void deleteMessage(int messageId) {
    	
    	String query = "Select * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_ID + " =  \"" + messageId + "\"";

    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	Cursor cursor = db.rawQuery(query, null);
    	
    	Message message = new Message();
    	
    	if (cursor.moveToFirst()) {
    		message.setId(Integer.parseInt(cursor.getString(0)));
    		db.delete(TABLE_MESSAGES, COLUMN_ID + " = ?", new String[] { String.valueOf(message.getId()) });
    		cursor.close();
    	}
            db.close();
    	
    }
    
}
