package com.cheki.db;

/**
 * This class holds database entry data passed between handler and activity. The
 * class holds transaction type, transaction result, transaction id, entity,
 * entity number, amount, date, time, balance
 * 
 * @author jabala
 * 
 */
public class Message {

	// create predefined transaction type constants
	public static enum TransactionType {
		BUY_AIRTIME, SEND_AIRTIME, SEND_MONEY, RECEIVE_MONEY, CHECK_BALANCE
	};

	// create predefined transaction result constants
	public static enum TransactionResult {
		FAILED, SUCCESS
	};

	// declare message variables
	private int id;
	private String transactionType;
	private String transactionResult;
	private String transactionID;
	private String entity;
	private String entityNumber;
	private String amount;
	private String date;
	private String time;
	private String balance;

	/**
	 * default constructor
	 */
	public Message() {
		super();
	}
	
	/**
	 * get id
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * set value of id
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * get entity
	 * 
	 * @return
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * set entity
	 * 
	 * @param entity
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}

	/**
	 * get amount
	 * 
	 * @return
	 */
	public String getAmount() {
		return amount;
	}

	/**
	 * set amount
	 * 
	 * @param amount
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}

	/**
	 * get entity number
	 * 
	 * @return
	 */
	public String getEntityNumber() {
		return entityNumber;
	}

	/**
	 * set entity number
	 * 
	 * @param entityNumber
	 */
	public void setEntityNumber(String entityNumber) {
		this.entityNumber = entityNumber;
	}

	/**
	 * get balance
	 * 
	 * @return
	 */
	public String getBalance() {
		return balance;
	}

	/**
	 * set balance
	 * 
	 * @param balance
	 */
	public void setBalance(String balance) {
		this.balance = balance;
	}

	/**
	 * get transaction type
	 * 
	 * @return
	 */
	public String getTransactionType() {
		return transactionType;
	}

	/**
	 * set transaction type
	 * 
	 * @param transactionType
	 */
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * get transaction result
	 * 
	 * @return
	 */
	public String getTransactResult() {
		return transactionResult;
	}

	/**
	 * set transaction result
	 * 
	 * @param transactResult
	 */
	public void setTransactResult(String transactResult) {
		this.transactionResult = transactResult;
	}

	/**
	 * get transaction id
	 * 
	 * @return
	 */
	public String getTransactionID() {
		return transactionID;
	}

	/**
	 * set transaction id
	 * 
	 * @param transactionID
	 */
	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * get date
	 * 
	 * @return
	 */
	public String getDate() {
		return date;
	}

	/**
	 * set date
	 * 
	 * @param date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * get time
	 * 
	 * @return
	 */
	public String getTime() {
		return time;
	}

	/**
	 * set time
	 * 
	 * @param time
	 */
	public void setTime(String time) {
		this.time = time;
	}

}
