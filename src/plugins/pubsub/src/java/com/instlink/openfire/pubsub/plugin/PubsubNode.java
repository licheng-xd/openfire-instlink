package com.instlink.openfire.pubsub.plugin;

import java.util.HashMap;


/**
 * the group node, or personal node.
 * 
 * @author LC
 *
 */
public class PubsubNode {

	private String id = null;
	private String name = null;
	private String creator = null;
	private PubsubNode parent = null;
	private String description = null;
	private String type = null;
	private PubsubQOS nqos = null;
	private PubsubQOS ngqos = null;
	private PubsubQOS ncqos = null;
	private PubsubQOS icqos = null;     
	private long deadline = 0;
	private long itemLifecycle = 0;
	private String password = null;
	
	private HashMap<String, PubsubItemType> itemTypesMap = new HashMap<String, PubsubItemType>();
	private HashMap<String, PubsubSubscriber> subscribersMap = new HashMap<String, PubsubSubscriber>();
//	private ArrayList<PubsubSubscriber> subscribersList = new ArrayList<PubsubSubscriber>();
	
	private HashMap<String, PubsubNode> childrenMap = new HashMap<String, PubsubNode>();
	
	//nodeID
	public String getID() {
		return id;
	}
	public void setID(String nodeID) {
		this.id = nodeID;
	}
	
	//nodeName
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	//creator
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	//parent
	public PubsubNode getParent() {
		return parent;
	}
	public void setParent(PubsubNode parent) {
		this.parent = parent;
	}
	
	//description
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	//type
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	//nqos
	public PubsubQOS getNqos() {
		return nqos;
	}
	public void setNqos(PubsubQOS nqos) {
		this.nqos = nqos;
	}
	
	//ngqos
	public PubsubQOS getNgqos() {
		return ngqos;
	}
	public void setNgqos(PubsubQOS ngqos) {
		this.ngqos = ngqos;
	}
	
	//ncqos
	public PubsubQOS getNcqos() {
		return ncqos;
	}
	public void setNcqos(PubsubQOS ncqos) {
		this.ncqos = ncqos;
	}

	//icqos
	public PubsubQOS getIcqos() {
		return icqos;
	}
	public void setIcqos(PubsubQOS icqos) {
		this.icqos = icqos;
	}
	
	//deadline
	public long getDeadline() {
		return deadline;
	}
	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}
	
	//itemLifecycle
	public long getItemLifecycle() {
		return itemLifecycle;
	}
	public void setItemLifecycle(long itemLifecycle) {
		this.itemLifecycle = itemLifecycle;
	}
	
	//password
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	//itemTypesMap
	public HashMap<String, PubsubItemType> getItemTypesMap() { 
		return itemTypesMap;
	}
	public void setItemTypesMap(HashMap<String, PubsubItemType> itemTypesMap) {
		this.itemTypesMap = itemTypesMap;
	}
	//subscribersMap
	public HashMap<String, PubsubSubscriber> getSubscribersMap() { 
		return subscribersMap;
	}
	public void setSubscribersMap(HashMap<String, PubsubSubscriber> subscribersMap) {
		this.subscribersMap = subscribersMap;
	}
	//childrenMap
	public HashMap<String, PubsubNode> getChildrenMap() { 
		return childrenMap;
	}
	public void setChildrenMap(HashMap<String, PubsubNode> childrenMap) {
		this.childrenMap = childrenMap;
	}
}
