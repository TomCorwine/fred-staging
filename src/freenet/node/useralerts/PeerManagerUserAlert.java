package freenet.node.useralerts;

import freenet.node.Node;

public class PeerManagerUserAlert implements UserAlert {

	final Node n;
	public int conns = 0;
	public int peers = 0;
	boolean isValid=true;
	int bwlimitDelayTime = 1;
	int nodeAveragePingTime = 1;
	long oldestNeverConnectedPeerAge = 0;
	
	/** How many connected peers we need to not get alert about not enough */
	static final int MIN_CONN_THRESHOLD = 3;
	
	/** How many connected peers we can have without getting alerted about too many */
	static final int MAX_CONN_THRESHOLD = 30;
	
	/** How many disconnected peers we can have without getting alerted about too many */
	static final int MAX_DISCONN_PEER_THRESHOLD = 20;
	
	/** How many peers we can have without getting alerted about too many */
	static final int MAX_PEER_THRESHOLD = 50;
	
	/** How high can bwlimitDelayTime be before we alert (in milliseconds)*/
	static final int MAX_BWLIMIT_DELAY_TIME_THRESHOLD = 1000;
	
	/** How high can nodeAveragePingTime be before we alert (in milliseconds)*/
	static final int MAX_NODE_AVERAGE_PING_TIME_THRESHOLD = 1500;
	
	/** How high can oldestNeverConnectedPeerAge be before we alert (in milliseconds)*/
	static final long MAX_OLDEST_NEVER_CONNECTED_PEER_AGE_THRESHOLD = ((long) 2)*7*24*60*60*1000;  // 2 weeks
	
	public PeerManagerUserAlert(Node n) {
		this.n = n;
	}
	
	public boolean userCanDismiss() {
		return false;
	}

	public String getTitle() {
		if(peers == 0)
			return "No peers found";
		if(conns == 0)
			return "No open connections";
		if(conns < MIN_CONN_THRESHOLD) {
			String suff = "";
			if (conns > 1) suff = "s";
			return "Only "+conns+" open connection"+suff;
		}
		if((peers - conns) > MAX_DISCONN_PEER_THRESHOLD)
			return "Too many disconnected peers";
		if(conns > MAX_CONN_THRESHOLD)
			return "Too many open connections";
		if(peers > MAX_PEER_THRESHOLD)
			return "Too many peers";
		if(bwlimitDelayTime > MAX_BWLIMIT_DELAY_TIME_THRESHOLD)
			return "bwlimitDelayTime too high";
		if(nodeAveragePingTime > MAX_NODE_AVERAGE_PING_TIME_THRESHOLD)
			return "nodeAveragePingTime too high";
		if(oldestNeverConnectedPeerAge > MAX_OLDEST_NEVER_CONNECTED_PEER_AGE_THRESHOLD)
			return "Never connected peer(s) too old";
		else throw new IllegalArgumentException("Not valid");
	}
	
	public String getText() {
		String s;
		if(peers == 0) {
			s = "This node has no peers to connect to, therefore it will not " +
			"be able to function normally. Ideally you should connect to peers run by people you know " +
			"(if you are paranoid, then people you trust; if not, then at least people you've talked to)";
			String end = " log on to irc.freenode.net channel #freenet-refs and ask around for somebody to connect to";
			if(n.isTestnetEnabled())
				s += ", but since this is a testnet node, we suggest that you " + end + ".";
			else
				s += ". You could " + end + ", but remember that you are vulnerable to " +
				"those you are directly connected to. (This is especially true in this early alpha of Freenet 0.7...)<br/>BE SURE THAT THE OTHER PERSON HAS ADDED YOUR REFERENCE, TOO, AS ONE-WAY CONNECTIONS WON'T WORK!";
		}else if(conns == 0) {
			s = "This node has not been able to connect to any other nodes so far; it will not be able to function normally. " +
			"Hopefully some of your peers will connect soon; if not, try to get some more peers.";
		} else if(conns == 1) {
			s = "This node only has one connection. Performance will be impaired, and you have no anonymity nor even plausible deniability if that one person is malicious. " +
			"Your node is attached to the network like a 'leaf' and does not contribute to the network's health. " +
			"Try to get at least 3 connected peers at any given time.";
		} else if(conns == 2) {
			s = "This node has only two connections. Performance and security will not be very good, and your node is not doing any routing for other nodes. " +
			"Your node is embedded like a 'chain' in the network and does not contribute to the network's health. " +
			"Try to get at least 3 connected peers at any given time.";
		} else if((peers - conns) > MAX_DISCONN_PEER_THRESHOLD){ 
			s = "This node has too many disconnected peers ("+(peers - conns)+" > "+MAX_DISCONN_PEER_THRESHOLD+"). This will have a impact your performance as disconnected peers also consume bandwidth and CPU. Consider \"cleaning up\" your peer list.";
		} else if(conns > MAX_CONN_THRESHOLD) {
			s = "This node has too many connections ("+conns+" > "+MAX_CONN_THRESHOLD+"). We don't encourage such a behaviour; Ubernodes are hurting the network.";
		} else if(peers > MAX_PEER_THRESHOLD) {
			s = "This node has too many peers ("+peers+" > "+MAX_PEER_THRESHOLD+"). This will impact your performance as all peers (connected or not) consume bandwidth and CPU. Consider \"cleaning up\" your peer list.";
		} else if(bwlimitDelayTime > MAX_BWLIMIT_DELAY_TIME_THRESHOLD) {
			s = "This node has to wait too long for available bandwidth ("+bwlimitDelayTime+" > "+MAX_BWLIMIT_DELAY_TIME_THRESHOLD+").  Increase your output bandwidth limit and/or remove some peers to improve the situation.";
		} else if(nodeAveragePingTime > MAX_NODE_AVERAGE_PING_TIME_THRESHOLD) {
			s = "This node is having trouble talking with it's peers quickly enough ("+nodeAveragePingTime+" > "+MAX_NODE_AVERAGE_PING_TIME_THRESHOLD+").  Decrease your output bandwidth limit and/or remove some peers to improve the situation.";
		} else if(oldestNeverConnectedPeerAge > MAX_OLDEST_NEVER_CONNECTED_PEER_AGE_THRESHOLD) {
			s = "One or more of your node's peers have never connected in the two weeks since they were added.  Consider removing them since they are affecting performance.";
		} else throw new IllegalArgumentException("Not valid");
		return s;
	}

	public short getPriorityClass() {
		if(peers == 0 ||
				conns == 0 ||
				(peers - conns) > MAX_DISCONN_PEER_THRESHOLD ||
				conns > MAX_CONN_THRESHOLD ||
				peers > MAX_PEER_THRESHOLD ||
				bwlimitDelayTime > MAX_BWLIMIT_DELAY_TIME_THRESHOLD ||
				nodeAveragePingTime > MAX_NODE_AVERAGE_PING_TIME_THRESHOLD)
			return UserAlert.CRITICAL_ERROR;
		return UserAlert.ERROR;
	}

	public boolean isValid() {
		// only update here so we don't get odd behavior with it fluctuating
		bwlimitDelayTime = (int) n.getBwlimitDelayTime();
		nodeAveragePingTime = (int) n.getNodeAveragePingTime();
		oldestNeverConnectedPeerAge = (int) n.getOldestNeverConnectedPeerAge();
		return (peers == 0 ||
				conns < 3 ||
				(peers - conns) > MAX_DISCONN_PEER_THRESHOLD ||
				conns > MAX_CONN_THRESHOLD ||
				peers > MAX_PEER_THRESHOLD ||
				bwlimitDelayTime > MAX_BWLIMIT_DELAY_TIME_THRESHOLD ||
				nodeAveragePingTime > MAX_NODE_AVERAGE_PING_TIME_THRESHOLD ||
				oldestNeverConnectedPeerAge > MAX_OLDEST_NEVER_CONNECTED_PEER_AGE_THRESHOLD) &&
				isValid;
	}
	
	public void isValid(boolean b){
		if(userCanDismiss()) isValid=b;
	}
	
	public String dismissButtonText(){
		return "Hide";
	}
	
	public boolean shouldUnregisterOnDismiss() {
		return false;
	}
}
