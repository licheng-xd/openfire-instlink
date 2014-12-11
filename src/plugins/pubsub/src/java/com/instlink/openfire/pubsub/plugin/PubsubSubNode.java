package com.instlink.openfire.pubsub.plugin;

/**
 * the node's brief info, used when client get subnodes
 * 
 * @author LC
 *
 */
public class PubsubSubNode {

	private String JID;
	private String type;
	
	public PubsubSubNode(String JID, String type) {
		this.JID = JID;
		this.type = type;
	}
	public String getJID() {
		return JID;
	}
	public void setJID(String JID) {
		this.JID = JID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
