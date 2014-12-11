package com.instlink.openfire.pubsub.plugin;

import java.util.HashMap;

/**
 * the node's subject
 * 
 * @author LC
 *
 */
public class PubsubSubject {

	private PubsubItemType itemType = null;
	private String node = null;
	private String subid = null;
	private String name = null;
	private HashMap<String, PubsubHistory> historiesMap = new HashMap<String, PubsubHistory>();
	private String type = null;
	
	
	public PubsubItemType getItemType() {
		return itemType;
	}
	public void setItemType(PubsubItemType itemType) {
		this.itemType = itemType;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getSubid() {
		return subid;
	}
	public void setSubid(String subid) {
		this.subid = subid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public HashMap<String, PubsubHistory> getHistoriesMap() {
		return historiesMap;
	}
	public void setHistoriesMap(HashMap<String, PubsubHistory> historiesMap) {
		this.historiesMap = historiesMap;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
