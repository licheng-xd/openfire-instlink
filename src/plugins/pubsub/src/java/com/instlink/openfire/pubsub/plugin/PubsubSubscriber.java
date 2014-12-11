package com.instlink.openfire.pubsub.plugin;

/**
 * the subscriber of node
 * 
 * @author LC
 *
 */
public class PubsubSubscriber {

	private String JID;
	private PubsubNode node;
	private PubsubSmallconf smallconf;
	private PubsubEnum state;
	private String status;       //与会者的在线状态。
	private PubsubQOS role; 
	
    private PubsubQOS nsqos; 
	private PubsubQOS isqos;
	private PubsubQOS ksqos;			//订阅者的qos，这里需要进一步区分
	
	private long historyRange;
	private long deadline;
	
	private int watcher;
	private int encrypt;
	
	public String getJID() {
		return this.JID;
	}
	public void setJID(String JID) {
		this.JID = JID;
	}
	public PubsubNode getNode() {
		return this.node;
	}
	public void setNode(PubsubNode node) {
		this.node = node;
	}
	public PubsubSmallconf getSmallconf() {
		return this.smallconf;
	}
	public void setSmallconf(PubsubSmallconf smallconf) {
		this.smallconf = smallconf;
	}
	public String getStatus() {
		return this.status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public PubsubEnum getState() {
		return this.state;
	}
	public void setState(PubsubEnum state) {
		this.state = state;
	}
	public PubsubQOS getRole() {
		return this.role;
	}
	public void setRole(PubsubQOS role) {
		this.role = role;
	}
	public PubsubQOS getNsqos() {
		return this.nsqos;
	}
	public void setNsqos(PubsubQOS nsqos) {
		this.nsqos = nsqos;
	}
	public PubsubQOS getIsqos() {
		return this.isqos;
	}
	public void setIsqos(PubsubQOS isqos) {
		this.isqos = isqos;
	}
	public PubsubQOS getKsqos() {
		return this.ksqos;
	}
	public void setKsqos(PubsubQOS ksqos) {
		this.ksqos = ksqos;
	}
	public long getHistoryRange() {
		return this.historyRange;
	}
	public void setHistoryRange(long historyRange) {
		this.historyRange = historyRange;
	}
	public long getDeadline() {
		return this.deadline;
	}
	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}
	public int getWatcher() {
		return this.watcher;
	}
	public void setWatcher(int watcher) {
		this.watcher = watcher;
	}
	public int getEncrypt() {
		return this.encrypt;
	}
	public void setEncrypt(int encrypt) {
		this.encrypt = encrypt;
	}
}
