package com.instlink.openfire.pubsub.plugin;

import java.util.HashMap;

/**
 * the smallconf
 * 
 * @author LC
 *
 */
public class PubsubSmallconf {
	private String id = null;
	private String name = null;       //�ڵ���������֡���ѡ��
	private String creator = null;            //�ڵ�Ĵ����ߡ�bare jid
	private String description = null;        //�ڵ������
	private String type = null;                //�ڵ����ͣ�����Ҫ��
	private PubsubQOS nqos = null;
	private PubsubQOS ngqos = null;       //�ڵ��ȫ��QoS
	private PubsubQOS ncqos = null;       //�ڵ�Ŀɶ���QoS      
	private PubsubQOS icqos = null;              //��ĿQoS
	private String verify = null;
	private String password = null;
	private PubsubSmallconf parent = null;
	private HashMap<String, PubsubSubscriber> subscribersMap = new HashMap<String, PubsubSubscriber>();  
	private HashMap<String, PubsubSmallconf> childrenMap = new HashMap<String, PubsubSmallconf>();
	private HashMap<String, PubsubHistory> historiesMap = new HashMap<String, PubsubHistory>();	//�ؼ��ֵ���ʷ��Ŀ����������ʱ�����ڴ���ʹ��
	
	
	public String getID() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public PubsubQOS getNqos() {
		return nqos;
	}
	public void setNqos(PubsubQOS nqos) {
		this.nqos = nqos;
	}
	public PubsubQOS getNgqos() {
		return ngqos;
	}
	public void setNgqos(PubsubQOS ngqos) {
		this.ngqos = ngqos;
	}
	public PubsubQOS getNcqos() {
		return ncqos;
	}
	public void setNcqos(PubsubQOS ncqos) {
		this.ncqos = ncqos;
	}
	public PubsubQOS getIcqos() {
		return icqos;
	}
	public void setIcqos(PubsubQOS icqos) {
		this.icqos = icqos;
	}
	public String getVerify() {
		return verify;
	}
	public void setVerify(String verify) {
		this.verify = verify;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public PubsubSmallconf getParent() {
		return parent;
	}
	public void setParent(PubsubSmallconf parent) {
		this.parent = parent;
	}
	public HashMap<String, PubsubSubscriber> getSubscribersMap() {
		return subscribersMap;
	}
	public void setSubscribersMap(HashMap<String, PubsubSubscriber> subscribersMap) {
		this.subscribersMap = subscribersMap;
	}
	public HashMap<String, PubsubSmallconf> getChildrenMap() {
		return childrenMap;
	}
	public void setChildrenMap(HashMap<String, PubsubSmallconf> childrenMap) {
		this.childrenMap = childrenMap;
	}
	public HashMap<String, PubsubHistory> getHistoriesMap() {
		return historiesMap;
	}
	public void setHistoriesMap(HashMap<String, PubsubHistory> historiesMap) {
		this.historiesMap = historiesMap;
	}
	
}
