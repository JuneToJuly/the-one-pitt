package routing;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import core.Application;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SettingsError;
import core.SimClock;
import core.SimError;
import core.Tuple;

public abstract class MessageBroadcastRouter extends MessageRouter
{
    public MessageBroadcastRouter(Settings s)
    {
    	super(s);
    }

	/**
	 * This method should be called (on the receiving host) after a message
	 * was successfully transferred. The transferred message is put to the
	 * message buffer unless this host is the final recipient of the message.
	 * @param id Id of the transferred message
	 * @param from Host the message was from (previous hop)
	 * @return The message that this host received
	 */
	public Message messageTransferred(String id, DTNHost from) {
		Message incoming = removeFromIncomingBuffer(id, from);
		boolean isFinalRecipient;
		boolean isFirstDelivery; // is this first delivered instance of the msg


		if (incoming == null) {
			throw new SimError("No message with ID " + id + " in the incoming "+
					                   "buffer of " + getHost());
		}

		incoming.setReceiveTime(SimClock.getTime());

		// Pass the message to the application (if any) and get outgoing message
		Message outgoing = incoming;
		for (Application app : getApplications(incoming.getAppID())) {
			// Note that the order of applications is significant
			// since the next one gets the output of the previous.
			outgoing = app.handle(outgoing, getHost());
			if (outgoing == null) break; // Some app wanted to drop the message
		}

		Message aMessage = (outgoing==null)?(incoming):(outgoing);

		// If the application re-targets the message (changes 'to')
		// then the message is not considered as 'delivered' to this host.
		// Is alw
		isFinalRecipient = true;

		isFirstDelivery = isFinalRecipient &&
				!isDeliveredMessage(aMessage);

		// Always add message to buffer
		addToMessages(aMessage, false);
		// ALways add like the message was delivered
		this.deliveredMessages.put(id, aMessage);

		for (MessageListener ml : this.mListeners) {
			ml.messageTransferred(aMessage, from, getHost(),
			                      true); // Message is never delivered
		}



//		// Old version
//		Message incoming = removeFromIncomingBuffer(id, from);
//		boolean isFinalRecipient;
//		boolean isFirstDelivery; // is this first delivered instance of the msg
//
//
//		if (incoming == null) {
//			throw new SimError("No message with ID " + id + " in the incoming "+
//					                   "buffer of " + getHost());
//		}
//
//		incoming.setReceiveTime(SimClock.getTime());
//
//		// Pass the message to the application (if any) and get outgoing message
//		Message outgoing = incoming;
//		for (Application app : getApplications(incoming.getAppID())) {
//			// Note that the order of applications is significant
//			// since the next one gets the output of the previous.
//			outgoing = app.handle(outgoing, getHost());
//			if (outgoing == null) break; // Some app wanted to drop the message
//		}
//
//		Message aMessage = (outgoing==null)?(incoming):(outgoing);
//
//		// If the application re-targets the message (changes 'to')
//		// then the message is not considered as 'delivered' to this host.
//		isFinalRecipient = aMessage.getTo() == getHost();
////		isFinalRecipient = true;
//
//		isFirstDelivery = isFinalRecipient &&
//				!isDeliveredMessage(aMessage);
//
//		if (!isFinalRecipient && outgoing!=null) {
//			// not the final recipient and app doesn't want to drop the message
//			// -> put to buffer
//			addToMessages(aMessage, false);
//		}
//		else if (isFirstDelivery) {
//			this.deliveredMessages.put(id, aMessage);
//		}
//
//		for (MessageListener ml : this.mListeners) {
//			ml.messageTransferred(aMessage, from, getHost(),
//			                      isFirstDelivery);
//		}

		return aMessage;

	}

	protected MessageBroadcastRouter(MessageBroadcastRouter r) {
		super(r);
	}

	/**
	 * Initializes the router; i.e. sets the host this router is in and
	 * message listeners that need to be informed about message related
	 * events etc.
	 * @param host The host this router is in
	 * @param mListeners The message listeners
	 */
	public void initialize(DTNHost host, List<MessageListener> mListeners) {
		super.initialize(host, mListeners);
	}


	/**
	 * Updates router.
	 * This method should be called (at least once) on every simulation
	 * interval to update the status of transfer(s).
	 */
	public void update(){
		super.update();
	}

	/**
	 * Informs the router about change in connections state.
	 * @param con The connection that changed
	 */
//	public abstract void changedConnection(Connection con);

	/**
	 * Returns a message by ID.
	 * @param id ID of the message
	 * @return The message
	 */
	protected Message getMessage(String id) {
		return getMessage(id);
	}

	/**
	 * Checks if this router has a message with certain id buffered.
	 * @param id Identifier of the message
	 * @return True if the router has message with this id, false if not
	 */
	protected boolean hasMessage(String id) {
		return super.hasMessage(id);
	}

	/**
	 * Returns true if a full message with same ID as the given message has been
	 * received by this host as the <strong>final</strong> recipient
	 * (at least once).
	 * @param m message we're interested of
	 * @return true if a message with the same ID has been received by
	 * this host as the final recipient.
	 */
	protected boolean isDeliveredMessage(Message m) {
		return super.isDeliveredMessage(m);
	}

	/**
	 * Returns a reference to the messages of this router in collection.
	 * <b>Note:</b> If there's a chance that some message(s) from the collection
	 * could be deleted (or added) while iterating through the collection, a
	 * copy of the collection should be made to avoid concurrent modification
	 * exceptions.
	 * @return a reference to the messages of this router in collection
	 */
	public Collection<Message> getMessageCollection() {
		return super.getMessageCollection();
	}

	/**
	 * Returns the number of messages this router has
	 * @return How many messages this router has
	 */
	public int getNrofMessages() {
		return super.getNrofMessages();
	}

	/**
	 * Returns the size of the message buffer.
	 * @return The size or Integer.MAX_VALUE if the size isn't defined.
	 */
	public int getBufferSize() {
		return super.getBufferSize();
	}

	/**
	 * Returns the amount of free space in the buffer. May return a negative
	 * value if there are more messages in the buffer than should fit there
	 * (because of creating new messages).
	 * @return The amount of free space (Integer.MAX_VALUE if the buffer
	 * size isn't defined)
	 */
	public int getFreeBufferSize() {
		return super.getFreeBufferSize();
	}

	/**
	 * Returns the host this router is in
	 * @return The host object
	 */
	protected DTNHost getHost() {
		return super.getHost();
	}

	/**
	 * Start sending a message to another host.
	 * @param id Id of the message to send
	 * @param to The host to send the message to
	 */
	public void sendMessage(String id, DTNHost to) {
		super.sendMessage(id, to);
	}

	/**
	 * Requests for deliverable message from this router to be sent trough a
	 * connection.
	 * @param con The connection to send the messages trough
	 * @return True if this router started a transfer, false if not
	 */
	public boolean requestDeliverableMessages(Connection con) {
		return super.requestDeliverableMessages(con);
	}

	/**
	 * Try to start receiving a message from another host.
	 * @param m Message to put in the receiving buffer
	 * @param from Who the message is from
	 * @return Value zero if the node accepted the message (RCV_OK), value less
	 * than zero if node rejected the message (e.g. DENIED_OLD), value bigger
	 * than zero if the other node should try later (e.g. TRY_LATER_BUSY).
	 */
	public int receiveMessage(Message m, DTNHost from) {
		return super.receiveMessage(m, from);
	}


	/**
	 * Puts a message to incoming messages buffer. Two messages with the
	 * same ID are distinguished by the from host.
	 * @param m The message to put
	 * @param from Who the message was from (previous hop).
	 */
	protected void putToIncomingBuffer(Message m, DTNHost from) {
		super.putToIncomingBuffer(m,from);
	}

	/**
	 * Removes and returns a message with a certain ID from the incoming
	 * messages buffer or null if such message wasn't found.
	 * @param id ID of the message
	 * @param from The host that sent this message (previous hop)
	 * @return The found message or null if such message wasn't found
	 */
	protected Message removeFromIncomingBuffer(String id, DTNHost from) {
		return super.removeFromIncomingBuffer(id, from);
	}

	/**
	 * Returns true if a message with the given ID is one of the
	 * currently incoming messages, false if not
	 * @param id ID of the message
	 * @return True if such message is incoming right now
	 */
	protected boolean isIncomingMessage(String id) {
		return super.isIncomingMessage(id);
	}

	/**
	 * Adds a message to the message buffer and informs message listeners
	 * about new message (if requested).
	 * @param m The message to add
	 * @param newMessage If true, message listeners are informed about a new
	 * message, if false, nothing is informed.
	 */
	protected void addToMessages(Message m, boolean newMessage) {
		super.addToMessages(m, newMessage);
	}

	/**
	 * Removes and returns a message from the message buffer.
	 * @param id Identifier of the message to remove
	 * @return The removed message or null if message for the ID wasn't found
	 */
	protected Message removeFromMessages(String id) {
		return super.removeFromMessages(id);
	}

	/**
	 * This method should be called (on the receiving host) when a message
	 * transfer was aborted.
	 * @param id Id of the message that was being transferred
	 * @param from Host the message was from (previous hop)
	 * @param bytesRemaining Nrof bytes that were left before the transfer
	 * would have been ready; or -1 if the number of bytes is not known
	 */
	public void messageAborted(String id, DTNHost from, int bytesRemaining) {
		super.messageAborted(id, from, bytesRemaining);
	}

	/**
	 * Creates a new message to the router.
	 * @param m The message to create
	 * @return True if the creation succeeded, false if not (e.g.
	 * the message was too big for the buffer)
	 */
	public boolean createNewMessage(Message m) {
		return super.createNewMessage(m);
	}

	/**
	 * Deletes a message from the buffer and informs message listeners
	 * about the event
	 * @param id Identifier of the message to delete
	 * @param drop If the message is dropped (e.g. because of full buffer) this
	 * should be set to true. False value indicates e.g. remove of message
	 * because it was delivered to final destination.
	 */
	public void deleteMessage(String id, boolean drop) {
		super.deleteMessage(id, drop);
	}

	/**
	 * Sorts/shuffles the given list according to the current sending queue
	 * mode. The list can contain either Message or Tuple<Message, Connection>
	 * objects. Other objects cause error.
	 * @param list The list to sort or shuffle
	 * @return The sorted/shuffled list
	 */
	@SuppressWarnings(value = "unchecked") /* ugly way to make this generic */
	protected List sortByQueueMode(List list) {
		return super.sortByQueueMode(list);
	}

	/**
	 * Gives the order of the two given messages as defined by the current
	 * queue mode
	 * @param m1 The first message
	 * @param m2 The second message
	 * @return -1 if the first message should come first, 1 if the second
	 *          message should come first, or 0 if the ordering isn't defined
	 */
	protected int compareByQueueMode(Message m1, Message m2) {
		return super.compareByQueueMode(m1,m2);
	}

	/**
	 * Returns routing information about this router.
	 * @return The routing information.
	 */
	public RoutingInfo getRoutingInfo() {
		return super.getRoutingInfo();
	}

	/**
	 * Adds an application to the attached applications list.
	 *
	 * @param app	The application to attach to this router.
	 */
	public void addApplication(Application app) {
		super.addApplication(app);
	}

	/**
	 * Returns all the applications that want to receive messages for the given
	 * application ID.
	 *
	 * @param ID	The application ID or <code>null</code> for all apps.
	 * @return		A list of all applications that want to receive the message.
	 */
	public Collection<Application> getApplications(String ID) {
		return super.getApplications(ID);
	}

	/**
	 * Creates a replicate of this router. The replicate has the same
	 * settings as this router but empty buffers and routing tables.
	 * @return The replicate
	 */
	//public abstract MessageRouter replicate();

	/**
	 * Returns a String presentation of this router
	 * @return A String presentation of this router
	 */
	public String toString() {
		return super.toString();
	}

}
