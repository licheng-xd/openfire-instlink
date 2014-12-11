package com.instlink.openfire.pubsub.plugin;

import java.util.Map;


/**
 * the node or personal_node's item type
 * 
 * @author LC
 *
 */
public class PubsubItemType {

	private PubsubNode node;
	private String typeName;
	private PubsubQOS qos;            //条目类型的qos
	private Map<String, PubsubSubject> subjectsMap;       //主题
	
	private PubsubQOS ncqosLoca;
	private PubsubQOS icqosLoca;
	private int seq;
	
	
	public PubsubNode getNode() {
		return node;
	}
	public void setNode(PubsubNode node) {
		this.node = node;
	}

	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public PubsubQOS getQos() {
		return qos;
	}
	public void setQos(PubsubQOS qos) {
		this.qos = qos;
	}

	public Map<String, PubsubSubject> getSubjectsMap() {
		return subjectsMap;
	}
	public void setSubjectsMap(Map<String, PubsubSubject> subjectsMap) {
		this.subjectsMap = subjectsMap;
	}

	public PubsubQOS getNcqosLoca() {
		return ncqosLoca;
	}
	public void setNcqosLoca(PubsubQOS ncqosLoca) {
		this.ncqosLoca = ncqosLoca;
	}
	public PubsubQOS getIcqosLoca() {
		return icqosLoca;
	}
	public void setIcqosLoca(PubsubQOS icqosLoca) {
		this.icqosLoca = icqosLoca;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
}
