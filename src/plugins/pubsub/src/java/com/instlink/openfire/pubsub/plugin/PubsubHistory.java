package com.instlink.openfire.pubsub.plugin;


/**
 * the item in node or personal_node, history means item in this plugin 
 * 
 * @author LC
 *
 */
public class PubsubHistory {

	private PubsubSubject subject;
	private String owner;
	private String ID;
	private String publisher;
	private String content;
	private String signature;

	private String time;
	private String status;
	private String handler;
	private String keyword;

	private String subid;
	private String type;
	private String body;

	private long delay;
	private long deadline;
	
	
	public PubsubSubject getSubject() {
		return subject;
	}
	public void setSubject(PubsubSubject subject) {
		this.subject = subject;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getHandler() {
		return handler;
	}
	public void setHandler(String handler) {
		this.handler = handler;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getSubid() {
		return subid;
	}
	public void setSubid(String subid) {
		this.subid = subid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public long getDelay() {
		return delay;
	}
	public void setDelay(long delay) {
		this.delay = delay;
	}
	public long getDeadline() {
		return deadline;
	}
	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}
}
