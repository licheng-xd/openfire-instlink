package com.instlink.openfire.pubsub.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * all operation method to item 
 * 
 * @author LC
 *
 */
public class PubsubItemManager {
	
	private static final Logger Log = LoggerFactory.getLogger(PubsubItemManager.class);

	/**
	 * when create a new node or personal node, create the node's default itemTypes
	 * 
	 * @param node
	 * @param isPersonal
	 */
	public void itemTypesCreate(PubsubNode node, boolean isPersonal) {
		
		itemTypesNew(node);
		if(node.getIcqos().isZero()) {
			return;
		}
		if(!node.getItemTypesMap().isEmpty()) {
			
			HashMap<String, PubsubItemType> map = node.getItemTypesMap();
			Set<String> key = map.keySet();
			Iterator<String> iter = key.iterator();
			while(iter.hasNext()) {
				
				PubsubItemType itemType = map.get(iter.next());
				PubsubSubject subject = subjectNew(itemType.getNode().getID(), itemType.getTypeName(), itemType.getTypeName(), itemType.getTypeName());
				addNewSubject(subject);
				PubsubPersistenceManager.addSubject(subject, isPersonal);
				if(itemType.getTypeName().equals("file") && node.getType().equals("personal")) {
					PubsubSubject subject2 = subjectNew(itemType.getNode().getID(), itemType.getTypeName(), "file2", "file2");
					addNewSubject(subject2);
					PubsubPersistenceManager.addSubject(subject2, isPersonal);
				}
			}
		}
	}
	
	/**
	 * create a new ItemType object in the node
	 * 
	 * @param node
	 */
	public void itemTypesNew(PubsubNode node) {
		
		PubsubQOS itemQos = null;
		if(node == null) {
			return;
		}
		if (!node.getNcqos().and(PubsubQOS.ncqos_ITEM_TYPE_CONF).isZero()) {
			
			itemQos = node.getIcqos().and(PubsubQOS.icqos_LOCATION_CONF).shiftRight(56);
			addNewItemType(node, "conf", itemQos, PubsubQOS.ncqos_ITEM_TYPE_CONF, PubsubQOS.icqos_LOCATION_CONF, 1);
		}
		if (!node.getNcqos().and(PubsubQOS.ncqos_ITEM_TYPE_BROAD).isZero())	{
			
			itemQos = node.getIcqos().and(PubsubQOS.icqos_LOCATION_BROAD).shiftRight(48);
			addNewItemType(node, "broad", itemQos, PubsubQOS.ncqos_ITEM_TYPE_BROAD, PubsubQOS.icqos_LOCATION_BROAD, 2);
		}

		if (!node.getNcqos().and(PubsubQOS.ncqos_ITEM_TYPE_FILE).isZero()) {
			
			itemQos = node.getIcqos().and(PubsubQOS.icqos_LOCATION_FILE).shiftRight(40);
			addNewItemType(node, "file", itemQos, PubsubQOS.ncqos_ITEM_TYPE_FILE, PubsubQOS.icqos_LOCATION_FILE, 3);
		}

		if (!node.getNcqos().and(PubsubQOS.ncqos_ITEM_TYPE_BBS).isZero()) {
			
			itemQos = node.getIcqos().and(PubsubQOS.icqos_LOCATION_BBS).shiftRight(32);
			addNewItemType(node, "bbs", itemQos, PubsubQOS.ncqos_ITEM_TYPE_BBS, PubsubQOS.icqos_LOCATION_BBS, 4);
		}

		if (!node.getNcqos().and(PubsubQOS.ncqos_ITEM_TYPE_BLOG).isZero()) {
		
			itemQos = node.getIcqos().and(PubsubQOS.icqos_LOCATION_BLOG).shiftRight(24);
			addNewItemType(node, "blog", itemQos, PubsubQOS.ncqos_ITEM_TYPE_BLOG, PubsubQOS.icqos_LOCATION_BLOG, 5);
		}

		if (!node.getNcqos().and(PubsubQOS.ncqos_ITEM_TYPE_COMMENT).isZero()) {
		
			itemQos = node.getIcqos().and(PubsubQOS.icqos_LOCATION_COMMENT).shiftRight(16);
			addNewItemType(node, "comment", itemQos, PubsubQOS.ncqos_ITEM_TYPE_COMMENT, PubsubQOS.icqos_LOCATION_COMMENT, 6);
		}

		if (!node.getNcqos().and(PubsubQOS.ncqos_ITEM_TYPE_NOTIFY).isZero()) {
		
			itemQos = node.getIcqos().and(PubsubQOS.icqos_LOCATION_NOTIFY).shiftRight(8);
			addNewItemType(node, "notify", itemQos, PubsubQOS.ncqos_ITEM_TYPE_NOTIFY, PubsubQOS.icqos_LOCATION_NOTIFY, 7);
		}

	}
	
	/**
	 * add a new item type 
	 * 
	 * @param node
	 * @param type
	 * @param qos
	 * @param ncqosLoca
	 * @param icqosLoca
	 * @param seq
	 * @return
	 */
	public PubsubItemType addNewItemType(PubsubNode node, String type, PubsubQOS qos, PubsubQOS ncqosLoca, PubsubQOS icqosLoca, int seq) {
		
		PubsubItemType itemType = null;
		if(node == null) {
			
			return null;
		}
		if(node.getItemTypesMap() == null) {
			node.setItemTypesMap(new HashMap<String, PubsubItemType>());
		}
		itemType = node.getItemTypesMap().get(type);
		if(itemType != null) {
			// already exist in memory
			return itemType;
		}
		itemType = new PubsubItemType();
		itemType.setTypeName(type);
		itemType.setNode(node);
		itemType.setQos(qos);
		itemType.setSubjectsMap(new HashMap<String, PubsubSubject>());
		itemType.setNcqosLoca(ncqosLoca);
		itemType.setIcqosLoca(icqosLoca);
		itemType.setSeq(seq);
		node.getItemTypesMap().put(itemType.getTypeName(), itemType);
		return itemType;
	}
	
	/**
	 * create a new Subjects object
	 * 
	 * @param nodeid
	 * @param type
	 * @param subid
	 * @param name
	 * @return
	 */
	public PubsubSubject subjectNew(String nodeid, String type, String subid, String name) {
		
		PubsubSubject subject = null;
		if (null == nodeid || nodeid.equals("") || null == type || type.equals("") || null == subid || subid.equals("") || null == name || name.equals(""))
		{
			return null;
		}

		subject = new PubsubSubject();

		subject.setNode(nodeid);
		subject.setSubid(subid);
		subject.setName(name);
		subject.setType(type);

		return subject;
	}
	
	/**
	 * add a new subject in node or personal node
	 * 
	 * @param subject
	 */
	public void addNewSubject(PubsubSubject subject) {
		
		if(subject == null) {
			Log.info("information missing, insert subject error!");
			return;
		}

		// TODO fatal problem
		PubsubNode node = PubsubService.nodesMap.get(subject.getNode());
		if(node == null) {
			Log.info("can't find node!");
			return;
		}
		PubsubItemType itemType = node.getItemTypesMap().get(subject.getType());
		if(itemType == null) {
			Log.info("can't find the item type in node!");
			return;
		}
		subject.setItemType(itemType);
		itemType.getSubjectsMap().put(subject.getSubid(), subject);
//		Log.info("节点在内存中添加类型为...的主题");
	}
	
	/**
	 * insert a new item in node or personal node
	 * 
	 * @param node
	 * @param history
	 */
	public void insertNewItem(PubsubNode node, PubsubHistory history) {
		
		if(node == null || history == null) {
			return;
		}
		PubsubItemType itemType = node.getItemTypesMap().get(history.getType());
		if(itemType == null) {
			return;
		}
		PubsubSubject subject = itemType.getSubjectsMap().get(history.getSubid());
		if(subject == null) {
			Log.info("subject == null, abandon");
			return;
		}
		history.setSubject(subject);
		subject.getHistoriesMap().put(history.getID(), history);
//		Log.info("类型为"+history.getType()+"主题为"+history.getSubid()+"的新条目插入到数据结构");
	}
	
	/**
	 * create the default subjects when create a new node
	 * 
	 * @param node
	 * @param type
	 * @param subid
	 * @param name
	 * @param isPersonal
	 */
	public void createNewSubject(PubsubNode node, String type, String subid, String name, boolean isPersonal) {
		
		PubsubSubject subject = subjectNew(node.getID(), type, subid, name);
		PubsubPersistenceManager.addSubject(subject, isPersonal);
		addNewSubject(subject);
	}
}
