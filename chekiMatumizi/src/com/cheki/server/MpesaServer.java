package com.cheki.server;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is used to send message to thrift server.
 * 
 * @author jabala
 *
 */
public class MpesaServer extends AsyncTask<String, Void, String> {
		// log
		private static final String TAG = "MessageToServer";

		// port
		private int port = 2512;

		// address
		private String host = "162.243.30.119";
		// protocol to transport messages
		private TTransport transport;

		private TProtocol protocol;

		// class to access remote methods
		private MpesaService.Client client;
		
		// MainActivity instance
		//MainActivity activity = new MainActivity();
		
		private String transactionId;
		private String entity;
		private String entityNumber;
		private String amount;
		private String date;
		private String time;
		private String remainingBalance;
		/**
		 * initialize message+
		 * 
		 * @param transactionId
		 */
		public MpesaServer(String transactionId, String entity, String entityNumber, String amount,
				String date, String time, String remainingBalance){
			this.transactionId = transactionId;
			this.entity = entity;
			this.entityNumber = entityNumber;
			this.amount = amount;
			this.date = date;
			this.time = time;
			this.remainingBalance = remainingBalance;
		}
		
	 @Override
     protected String doInBackground(String... params) {
		 try {
				
				// open connection
				transport = new TSocket(host,port);
				transport.open();
				Log.i(TAG,"connection opened..");
				
				protocol = new TBinaryProtocol(transport);
				client = new MpesaService.Client(protocol);
				Log.i(TAG,"client initialized..");
				
				// invoke remote method
				client.message(transactionId, transactionId, transactionId, 
						entity, entityNumber, amount, date, time, remainingBalance);
				
				Log.i(TAG,"remote method invoked..");
				
				// close connection
				transport.close();
				Log.i(TAG,"connection closed");
				
			} catch (TTransportException e) {
				Log.e(TAG,"TTE -> "+e);
			} catch (TException e) {
				Log.i(TAG,"TE -> "+e);
			}
         return "";
     }

     @Override
     protected void onPostExecute(String result) {
        
     }

     @Override
     protected void onPreExecute() {}

     @Override
     protected void onProgressUpdate(Void... values) {}
}
