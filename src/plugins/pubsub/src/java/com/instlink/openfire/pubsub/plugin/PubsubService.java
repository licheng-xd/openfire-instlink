package com.instlink.openfire.pubsub.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;


/**
 * the main class of pubsub plugin, process the client's asking
 * 
 * @author LC
 *
 */
public class PubsubService {

	private static final Logger Log = LoggerFactory.getLogger(PubsubService.class);
	public static final String PUBSUB_NAMESPACE = "http://jabber.org/protocol/pubsub";
	public static final String PUBSUB_ELEM_NAME = "pubsub";
	public static Map<String, PubsubNode> nodesMap = null;
	public static Map<String, PubsubSmallconf> smallconfsMap = null;
	public static ConcurrentMap<String, Long> timeMap = null;
	private String domain = null;
	private XMPPServer server = null;
	private PacketRouter router = null;
	private PubsubItemManager itemManager = null;
	private PubsubMemberManager memberManager = null;

	/**
	 *  constructor 
	 */
	public PubsubService() {

		Log.info("service constuct start");

		try {
			domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
			server = XMPPServer.getInstance();
			router = server.getPacketRouter();
			itemManager = new PubsubItemManager();
			memberManager = new PubsubMemberManager(this);
			nodesMap = new HashMap<String, PubsubNode>();
			smallconfsMap = new HashMap<String, PubsubSmallconf>();
			timeMap=new ConcurrentHashMap<String,Long>();

		} catch (Exception e) {
			Log.info("service constuct error !!!");
			return;
		}

	}


	/**
	 * process all pubsub packet
	 * 
	 * @param pubsubPacket
	 */
	public void processPacket(PubsubPacket pubsubPacket) {
		
		if (!pubsubPacket.getEvent().and(PubsubEnum.event_ERROR).isZero()) {
			// incorrect packet
			Log.info("packet's event is error");
			return;
		} 
		if (pubsubPacket.getEvent() == PubsubEnum.event_TIME_CHECK) {
			
			return;
		}
		else if (pubsubPacket.getEvent() == PubsubEnum.event_CREATE) {
			
			if(pubsubPacket.getNode().getType().equals("group")) {
				createNode(pubsubPacket);
			}
			else if(pubsubPacket.getNode().getType().equals("personal")) {
				createPersonalNode(pubsubPacket);
			}
		} 
		else if (pubsubPacket.getEvent() == PubsubEnum.event_GET_SUB_NODES) {
			
			getSubNodes(pubsubPacket);
		} 
		else if (pubsubPacket.getEvent() == PubsubEnum.event_GET_SUBSCRIBERS) {
			
			getNodeSubscribers(pubsubPacket);
		}
		else if (pubsubPacket.getEvent() == PubsubEnum.event_NODEINFO) {
			
			 getNodeInfo(pubsubPacket);
		} 
		else if (pubsubPacket.getEvent() == PubsubEnum.event_SUBER_UPT) {
			
			memberManager.nodeSubscriberUpdate(pubsubPacket);
		} 
		else if (pubsubPacket.getEvent() == PubsubEnum.event_PUBLISH 
				&& (pubsubPacket.getIQ().getType() == IQ.Type.set)) {
			
			publish(pubsubPacket);
		}
		else if(pubsubPacket.getEvent() == PubsubEnum.event_PERSONAL_PUBLISH 
				&& pubsubPacket.getIQ().getType() == IQ.Type.set) {
			
			personalPublish(pubsubPacket);
		}
		else if(pubsubPacket.getEvent() == PubsubEnum.event_GET_ALL_NODES) {
			
			getAllNodes(pubsubPacket);
		}
		else if(pubsubPacket.getEvent() == PubsubEnum.event_SUB) {
			
			subscribeNode(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_SUBED)) {
			
			subscribedNode(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_CREATE_SMALLCONF)) {
			
			createSmallconf(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_GET_SUB_SMALLCONFS)) {
			
			getSubSmallconfs(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_SMALLCONFINFO)) {
			
			getSmallconfInfo(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_GET_SMALLCONF_SUBSCRIBERS)) {
			
			getSmallconfSubscibers(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_GET_ALL_SMALLCONFS)) {
			
			getAllSmallconf(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_SUBED_SMALLCONF)) {
			
			subscribedSmallconf(pubsubPacket);
		}
		else if(pubsubPacket.getEvent().equals(PubsubEnum.event_SUB_SMALLCONF)) {
			
			subscribeSmallconf(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_SCPUBLISH)) {
			
			smallconfPublish(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_SCSUBER_UPT)) {
			
			memberManager.smallconfSubscriberUpdate(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_SUBSCRIPTION)) {
			
			nodeSubscription(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_SCSUBSCRIPTION)) {
			
			smallconfSubscription(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_DELETE)) {
			
			deleteNode(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_DELETE_SMALLCONF)) {
			
			deleteSmallconf(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_UNSUB)) {
			
			unsubscribeNode(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_UNSUB_SMALLCONF)) {
			
			unsubscribeSmallconf(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_GETCHILD)) {
			
			getChild(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_GET_CONF_ITEM_DETAIL)) {
			
			getConfItemDetail(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_GET_PER_ITEM_DETAIL)) {
			
			getPersonalItemDetail(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_GET_OFFLINE)) {
			
			getOffline(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_DELETE_ITEM)) {
			
			deleteItem(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_SET_STATUS)) {
			
			setStatus(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_SET_CONFIGURE)) {
			
//			if(pubsubPacket.getData().equals("personal")) {
//				setConfigurePersonal(pubsubPacket);
//			}
//			else {
				setConfigure(pubsubPacket);
//			}
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_SET_SCCONFIGURE)) {
			
			setSmallconfConfigure(pubsubPacket);
		}
		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_GET_HISTORY)) {
			
			getHistory(pubsubPacket);
		}
//		else if (pubsubPacket.getEvent().equals(PubsubEnum.event_LEAVE)) {
//			
//			//System.out.println("00~~~~~~~~~~~~~");
//			userPresenceChanged(pubsubPacket.getData());
//		}
	}

	/**
	 * create a new node
	 * 
	 * @param pubsubPacket
	 *            the packet that used to create node
	 */
	private void createNode(PubsubPacket pubsubPacket) {

		Log.debug("packet process - create node - start");
		PubsubNode node = pubsubPacket.getNode();
			
		// create group node
		createNodeSynchronized(node);
		
		// add node into the parent's children map
		if (node.getParent() != null) {
			node.getParent().getChildrenMap().put(node.getID(), node);
		}
		
		nodesMap.put(node.getID(), node);
		itemManager.itemTypesCreate(node, false);
		// notify client the result of create. here can be a function
		IQ resultIQ = IQ.createResultIQ(pubsubPacket.getIQ());

		Element elemPubsub = pubsubPacket.getIQ().getChildElement().createCopy();
		Element elemCreate = elemPubsub.element("create");
		if (elemCreate == null) {
			return;
		}
		Element elemPath = insertNodeParentElement(node);

		elemCreate.addAttribute("node", node.getID());
		elemCreate.add(elemPath);
		resultIQ.setChildElement(elemPubsub);
		router.route(resultIQ);
		//System.out.println("<******************reply***********************>" + resultIQ.toString());
		// add creator to subscribers
		PubsubQOS nsqos = (PubsubQOS.ncqos_TEMP_SUB.not())
				.and(node.getNcqos()).and(PubsubQOS.ncqos_ROLE.not())
				.or(PubsubQOS.ncqos_ROLE_CREATOR)
				.or(PubsubQOS.ncqos_WATCHER);
		memberManager.nodeAddSubscriber(node, node.getCreator(), nsqos,	node.getIcqos(), 0, 0);

		// send message to creator
		Message replyMsg = new Message();
		replyMsg.setTo(node.getCreator());
		replyMsg.setFrom(domain);

		replyMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elem1 = DocumentHelper.createElement("subscription");
		elem1.addAttribute("node", node.getID());
		elem1.addAttribute("jid", node.getCreator());
		elem1.addAttribute("type", node.getType());
		elem1.addAttribute("subscription", "agree");
		replyMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elem1);
		router.route(replyMsg);
		//System.out.println("<******************reply***********************>" + replyMsg.toString());
	} 

	
	/**
	 * create a new personal node
	 * 
	 * @param pubsubPacket
	 *            the packet that used to create personal node
	 */
	private void createPersonalNode(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();

		PubsubPersistenceManager.addNode(node, this);
		
		nodesMap.put(node.getID(), node);
		itemManager.itemTypesCreate(node, true);
		
		IQ resultIQ = IQ.createResultIQ(pubsubPacket.getIQ());

		Element elemPubsub = pubsubPacket.getIQ().getChildElement().createCopy();
		Element elemCreate = elemPubsub.element("create");
		if (elemCreate == null) {
			return;
		}
		elemCreate.addAttribute("node", node.getID());
		resultIQ.setChildElement(elemPubsub);
		router.route(resultIQ);
		//System.out.println("<******************reply***********************>" + resultIQ.toString());
		
		PubsubPersistenceManager.userSubscriptionsAdd(node.getCreator(), node.getID(), node.getType());
	}

	/**
	 * Guarantee the database will not be used in the same time
	 * 
	 * @param node
	 *            the new node will be created
	 */
	private synchronized void createNodeSynchronized(PubsubNode node) {

		// get maxNodeJID
		String maxNodeJID = PubsubPersistenceManager.getMaxJID(false);
		if (maxNodeJID == null) {
			maxNodeJID = "10050000@" + domain;
		}
		
		// get newNodeJID
		int i = maxNodeJID.indexOf('@');
		if (i == -1) {
			return;
		}
		String temp = maxNodeJID.substring(0, i);
		int newNodeJIDInt = Integer.parseInt(temp) + 1;
		String newNodeJID = Integer.toString(newNodeJIDInt) + "@" + domain;
		
		// store new node
		node.setID(newNodeJID);
		nodesMap.put(node.getID(), node);
		PubsubPersistenceManager.addNode(node, this);
	}

	/**
	 * Get nodes which the user have subscriber, the user is the packet from
	 * 
	 * @param pubsubPacket
	 */
	private void getSubNodes(PubsubPacket pubsubPacket) {

		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setTo(pubsubPacket.getIQ().getFrom());
		resultIQ.setType(IQ.Type.result);
		resultIQ.setFrom(domain);
		
		Element elem = resultIQ.getChildElement().element("getsubnodes");
		if(elem == null) {
			return;
		}

		ArrayList<PubsubSubNode> subNodesList = PubsubPersistenceManager.getSubNodes(resultIQ.getTo().toBareJID());
		if(subNodesList.isEmpty()) {
			Log.info("subNodesList is empty");
		}
		Iterator<PubsubSubNode> iter = subNodesList.iterator();
		while (iter.hasNext()) {
			
			Element nodeElem = DocumentHelper.createElement("node");
			PubsubSubNode subNode = iter.next();

			nodeElem.addAttribute("type", subNode.getType());
			nodeElem.addText(subNode.getJID());
			elem.add(nodeElem);
		}
		router.route(resultIQ);
		//System.out.println("<******************reply***********************>" + resultIQ.toString());
	}

	// private void getSubSmallconfs(PubsubPacket pubsubPacket) {
	//
	// IQ resultIQ = IQ.createResultIQ(pubsubPacket.getIQ());
	// Element elem = resultIQ.getChildElement().element("getsmallconfs");
	//
	// ArrayList<SubNode> subNodes = new ArrayList<SubNode>();
	// subNodes = PubsubPersistenceManager.getSubSmallconfs(resultIQ);
	// Iterator<SubNode> iter = subNodes.iterator();
	// while(iter.hasNext()) {
	// Element nodeElem = DocumentHelper.createElement("smallconf");
	// SubNode subNode = iter.next();
	// nodeElem.addAttribute("type", subNode.getType());
	// nodeElem.addText(subNode.getNodeJID());
	// elem.add(nodeElem);
	// }
	// router.route(resultIQ);
	// }

	/**
	 * Loads all nodes from the database and add them to the pubsub service
	 */
/*	
	private void loadData() {
		PubsubPersistenceManager.loadNodesMap(nodesMap, this);
	}
*/
	/**
	 * Get node by nodeJID
	 * 
	 * @param nodeJID
	 * @return the node
	 */
	public PubsubNode getNodeByJID(String nodeJID, boolean isPersonal) {

		PubsubNode node = null;
		node = nodesMap.get(nodeJID);
		if (node == null) {
			node = PubsubPersistenceManager.getNodeByJID(nodeJID, this, isPersonal);
			if(node != null) {
				nodesMap.put(node.getID(), node);
				loadNodeSubjects(node);
				loadNodeHistory(node);
			}
		}
		
		Log.info("get node by jid finish");
		return node;
	}
	
	/**
	 * Get smallconf by smallconfJID
	 * 
	 * @param smallconfJID
	 * @return the smallconf
	 */
	public PubsubSmallconf getSmallconfByJID(String JID) {
		
		PubsubSmallconf smallconf = null;
		smallconf = smallconfsMap.get(JID);
		if (smallconf == null) {
			smallconf = PubsubPersistenceManager.getSmallconfByJID(JID, this);
			if(smallconf != null) {
				smallconfsMap.put(smallconf.getID(), smallconf);
			}
		}
		
		Log.info("get smallconf by jid finish");
		return smallconf;
	}

	/**
	 * get the node's subscribers
	 * 
	 * @param pubsubPacket 
	 */
	private void getNodeSubscribers(PubsubPacket pubsubPacket) {

		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setTo(pubsubPacket.getIQ().getFrom());
		resultIQ.setID(pubsubPacket.getIQ().getID());
		resultIQ.setType(IQ.Type.result);
		resultIQ.setFrom(pubsubPacket.getIQ().getTo());

		Element elemParticipants = resultIQ.getChildElement().element("participants");

		PubsubNode node = pubsubPacket.getNode();
		HashMap<String, PubsubSubscriber> subscribersMap = null;
		
		if(node.getSubscribersMap().isEmpty()) {
			
			memberManager.nodeLoadSubscribers(node);
		}
		
		subscribersMap = node.getSubscribersMap();
		
		Set<String> keySet = subscribersMap.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {

			PubsubSubscriber subsciber = subscribersMap.get(iter.next());
			Element elemParticipant = DocumentHelper.createElement("participant");

			elemParticipant.addAttribute("jid", subsciber.getJID());
			if (subsciber.getRole().equals(PubsubEnum.state_SPONSOR)) {
				elemParticipant.addAttribute("state", "creator");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AI)) {
				elemParticipant.addAttribute("state", "ai");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AO)) {
				elemParticipant.addAttribute("state", "ao");
			} else if (subsciber.getRole().equals(PubsubEnum.state_VI)) {
				elemParticipant.addAttribute("state", "vi");
			} else if (subsciber.getRole().equals(PubsubEnum.state_VO)) {
				elemParticipant.addAttribute("state", "vo");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AIVI)) {
				elemParticipant.addAttribute("state", "aivi");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AIVO)) {
				elemParticipant.addAttribute("state", "aivo");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AOVI)) {
				elemParticipant.addAttribute("state", "aovi");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AOVO)) {
				elemParticipant.addAttribute("state", "aovo");
			} else {
				elemParticipant.addAttribute("state", "attender");
			}
			elemParticipant.addAttribute("status", subsciber.getStatus());

			if (subsciber.getRole().equals(PubsubQOS.ncqos_ROLE_CREATOR)) {
				elemParticipant.addAttribute("role", "creator");
				elemParticipant.addAttribute("state", "creator");
			} else if (subsciber.getRole().equals(PubsubQOS.ncqos_ROLE_OUTCASE)) {
				elemParticipant.addAttribute("state", "outcast");
			} else {
				elemParticipant.addAttribute("role", "member");
			}

			if (subsciber.getStatus() != null) {
				elemParticipant.addAttribute("status", subsciber.getStatus());
			}
			if (subsciber.getWatcher() != 0) {
				elemParticipant.addAttribute("watcher", "true");
			}
			if (subsciber.getEncrypt() != 0) {
				elemParticipant.addAttribute("encrypt", "true");
			}

			elemParticipants.add(elemParticipant);
		}

		router.route(resultIQ);
		//System.out.println("<**************reply****************>" + resultIQ.toString());
	}

	
	/**
	 * publish message or publish items in node
	 * 
	 * @param pubsubPacket
	 */
	private void publish(PubsubPacket pubsubPacket) {

		Log.debug("publish");
		PubsubNode node = pubsubPacket.getNode();
		Element elemPubsub = pubsubPacket.getIQ().getChildElement();
		Element elemPublish = elemPubsub.element("publish");
		Element elemItem = elemPublish.element("item");

		if (elemItem == null) {
			return;
		}
		String type = elemItem.attributeValue("type");
		if (type.length() == 0) {
			return;
		}

		PubsubItemType itemType = node.getItemTypesMap().get(type);
		if (itemType == null) {
			return;
		}

		String subid = elemItem.attributeValue("subid");
		PubsubSubject subject = itemType.getSubjectsMap().get(subid);
		if (subject == null) {
			return;
		}

		if (node.getSubscribersMap().isEmpty()) {
			memberManager.nodeLoadSubscribers(node);
		}
		PubsubSubscriber subscriber = node.getSubscribersMap().get(pubsubPacket.getIQ().getFrom().toBareJID());
		if (subscriber == null) {
			return;
		}

		PubsubQOS ItemQos = itemType.getQos();
		if (ItemQos.and(PubsubQOS.icqos_item_PUBLISH).isZero()
				&& subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).compare(PubsubQOS.ncqos_ROLE_OWNER) < 0) {

			return;
		}

		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setType(IQ.Type.result);
		resultIQ.setFrom(pubsubPacket.getIQ().getTo());
		nodeDispatchToSubscribers(node, resultIQ, itemType.getQos());
		
		if(!PubsubQOS.icqos_item_HISTORY.and(itemType.getQos()).isZero()) {
			
			String itemId = elemItem.attributeValue("id");
			if(itemId == null) {
				return;
			}
			PubsubHistory history = subject.getHistoriesMap().get(itemId);
			if(history != null) {
				// change the exist history
				
				String time = elemItem.attributeValue("time");
//				System.out.println("publish new item--time-->"+time);
				if(time != null) {
					history.setTime(time);
//					PubsubPersistenceManager.updateHistory(itemId, "time", time, false);
				}
				
				if(elemItem.element("content") != null) {
					String content = elemItem.element("content").getText();
//					System.out.println("content:" + content);
					if(content != null) {
						
						history.setContent(content);
//						PubsubPersistenceManager.updateHistory(itemId, "content", content, false);
					}
				}
				
				if(elemItem.element("body") != null) {
					String body = elemItem.element("body").getText();
					if(body != null) {
						
						history.setBody(body);
//						PubsubPersistenceManager.updateHistory(itemId, "body", body, false);
					}
				}
				PubsubPersistenceManager.updateHistory(history, false);
				return;
			}
			else {
				
//				System.out.println("publish new item");
				history = new PubsubHistory();
				history.setOwner(node.getID());
				history.setType(type);
				history.setID(itemId);
				history.setSubid(subid);
				
				String publisher = elemItem.attributeValue("publisher");
//				System.out.println("publish new item--publisher-->"+publisher);
				if(publisher != null) {
					history.setPublisher(publisher);
				}
				
				String time = elemItem.attributeValue("time");
//				System.out.println("publish new item--time-->"+time);
				if(time != null) {
					history.setTime(time);
				}
				
				String content = elemItem.element("content").getText();
				if(content != null) {
					history.setContent(content);
				}
				
				String body = elemItem.element("body").getText();
				if(body != null) {
					history.setBody(body);
				}
				
				String handler = elemItem.attributeValue("handler");
				if(handler != null) {
					history.setHandler(handler);
				}
				
				String status = elemItem.attributeValue("status");
				if(status != null) {
					history.setStatus(status);
				}

				String delay = elemItem.attributeValue("delay");
				if(delay != null) {
					history.setDelay(Long.parseLong(delay));
				}
				
				String deadline = elemItem.attributeValue("deadline");
				if(deadline != null) {
					history.setDeadline(Long.parseLong(deadline));
				}
				
				String keyword = elemItem.attributeValue("keyword");
				if(keyword != null) {
					history.setKeyword(keyword);
				}
				
				PubsubPersistenceManager.addHistory(history, false);
				itemManager.insertNewItem(node, history);
			}
		}
	}
	
	/**
	 * publish message or publish items in personal node
	 * 
	 * @param pubsubPacket
	 */
	private void personalPublish(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();

		Element elemPubsub = pubsubPacket.getIQ().getChildElement();
		Element elemPublish = elemPubsub.element("publish");
		Element elemItem = elemPublish.element("item");
		if(elemItem == null) {
			return;
		}
		
		String type = elemItem.attributeValue("type");
		if(type == null) {
			return;
		}
		PubsubItemType itemType = null;
		if(node != null) {
			
			itemType = node.getItemTypesMap().get(type);
			if(itemType == null) {
				return;
			}
		}

		
		String subid = elemItem.attributeValue("subid");
		if(subid == null) {
			return;
		}
		PubsubSubject subject = null;
		if(node != null) {
			
			subject = itemType.getSubjectsMap().get(subid);
			if(subject == null) {
				return;
			}
		}
		PubsubQOS itemQos = null;
		if(node != null) {
			
			Element elemQos = elemPublish.element("qos");
			if(elemQos != null) {
				itemQos = new PubsubQOS(elemQos.getText());
			}
			else {
				itemQos = itemType.getQos();
			}
		}
		Element elemDelay = elemPublish.element("delay");
		if(elemDelay != null) {
			
			long delay = 0;
			delay = Long.parseLong(elemDelay.getText());
			long now = System.currentTimeMillis();
			if(delay >= now) {
				// don't offer delay function any more
				// nodeStorageXml(node, pkt->nad, delay, NULL);
				return;
			}
		}
		
		// don't offer broad function any more
		
		if(!itemQos.and(PubsubQOS.icqos_item_OFFLINE).isZero()) {
			elemPublish.addAttribute("offline", "true");
		}
		
		// if from == user->jid 
		IQ resultIQ = iq.createCopy();
		resultIQ.setTo(node.getID()+"/jclient");
		resultIQ.setFrom(domain);
		router.route(resultIQ);
		//System.out.println("<*************reply**************>"+resultIQ.toString());
		
//		if (strcmp(jid_user(user->jid), jid_user(pkt->from))/* && 0 == strncmp(itype, "file", strlen("file"))*/)
//		{
//			tmp_pkt2 = pkt_create_with_pubsub("iq", "result", jid_full(pkt->from), node_mgr->sm->sub_id);  
//			pkt_id_new(tmp_pkt2);
//			nad_insert_nad(tmp_pkt2->nad, 1, pkt->nad, 2);
//			pkt_router(tmp_pkt2);
//		}
		
		if(!itemQos.and(PubsubQOS.icqos_item_NOTIFY).isZero()) {
//			long deadline = System.currentTimeMillis() + 3600*24*7;
//			IQ resultIQ1 = iq.createCopy();
//			resultIQ.setTo(node.getID()+"/jclient");
//			resultIQ.setFrom(domain+"/jclient");
//			router.route(resultIQ);
//			//System.out.println("<*************reply**************>"+resultIQ.toString());
		}
		if(!itemQos.and(PubsubQOS.icqos_item_HISTORY).isZero()) {
			
			String itemID = elemItem.attributeValue("id");
			PubsubHistory item = subject.getHistoriesMap().get(itemID);
			if(item != null) {
				// change the item
				String time = elemItem.attributeValue("time");
				if(time != null) {
					item.setTime(time);
//					PubsubPersistenceManager.updateHistory(itemID, "time", time, true);
				}
				
				Element elemContent = elemItem.element("content");
				if(elemContent != null) {
					String content = elemContent.getText();
					item.setContent(content);
//					PubsubPersistenceManager.updateHistory(itemID, "content", content, true);
				}
				Element elemBody = elemItem.element("body");
				if(elemBody != null) {
					String body = elemBody.getText();
					item.setBody(body);
//					PubsubPersistenceManager.updateHistory(itemID, "body", body, true);
				}
				PubsubPersistenceManager.updateHistory(item, true);
				return;
			}
			else {
				// publish new item
				item = new PubsubHistory();
				item.setOwner(node.getID());
				item.setType(type);
				item.setID(itemID);
				
				item.setSubid(subid);
				String publisher = elemItem.attributeValue("publisher");
				if(publisher != null) {
					item.setPublisher(publisher);
				}
				String time = elemItem.attributeValue("time");
				if(time != null) {
					item.setTime(time);
				}
				Element elemContent = elemItem.element("content");
				if(elemContent != null) {
					String content = elemContent.getText();
					item.setContent(content);
				}
				Element elemBody = elemItem.element("body");
				if(elemBody != null) {
					String body = elemBody.getText();
					item.setBody(body);
				}
				Element elemHandler = elemItem.element("handler");
				if(elemHandler != null) {
					String handler = elemHandler.getText();
					item.setHandler(handler);
				}
				Element elemStatus = elemItem.element("status");
				if(elemStatus != null) {
					String status = elemStatus.getText();
					item.setStatus(status);
				}
				Element elemDelay1 = elemItem.element("delay");
				if(elemDelay1 != null) {
					String delay = elemDelay1.getText();
					item.setDelay(Long.parseLong(delay));
				}
				Element elemSignature = elemItem.element("signature");
				if(elemSignature != null) {
					String signature = elemSignature.getText();
					item.setSignature(signature);
				}
				Element elemDeadline = elemItem.element("deadline");
				if(elemDeadline != null) {
					String deadline = elemDeadline.getText();
					item.setDeadline(Long.parseLong(deadline));
				}
				Element elemKeyword = elemItem.element("keyword");
				if(elemKeyword != null) {
					String keyword = elemKeyword.getText();
					item.setKeyword(keyword);
				}
				PubsubPersistenceManager.addHistory(item, true);
				itemManager.insertNewItem(node, item);
			}
		}
		return;
	}
	
	/**
	 * get all nodes in database, use it when instlink client serch nodes.
	 * 
	 * @param pubsubPacket
	 */
	private void getAllNodes(PubsubPacket pubsubPacket) {
		
		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setTo(pubsubPacket.getIQ().getFrom());
		resultIQ.setType(IQ.Type.result);
		Element elemGetNodes = resultIQ.getChildElement().element("getnodes");
		
		ArrayList<PubsubNode> nodesList = PubsubPersistenceManager.getAllGroupNodes(this);
		Iterator<PubsubNode> iter = nodesList.iterator();
		while(iter.hasNext()) {
			
			Element elemNode = DocumentHelper.createElement("node");
			PubsubNode node = iter.next();
			elemNode.addAttribute("id", node.getID());
			elemNode.addAttribute("creator", node.getCreator());
			if(node.getType() != null) {
				elemNode.addAttribute("type", node.getType());
			}
			if(node.getName() != null) {
				elemNode.addAttribute("name", node.getName());
			}
			if(node.getNqos() != null) {
				if(node.getNqos().and(PubsubQOS.ngqos_ACCESS_MODEL) == PubsubQOS.ngqos_ACCESS_MODEL_PASSWD) {
					
					elemNode.addAttribute("password", "true");
				}
			}
			elemGetNodes.add(elemNode);
		}
		router.route(resultIQ);
		//System.out.println("<*************reply**************>"+resultIQ.toString());
	}
	
	/**
	 * the client ask for subscribe a node, service will notify the creator of the node.
	 * 
	 * @param pubsubPacket
	 */
	private void subscribeNode(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		memberManager.nodeLoadSubscribers(node);
		
		String subscriberJID = pubsubPacket.getIQ().getFrom().toBareJID();
		PubsubSubscriber subscriber = node.getSubscribersMap().get(subscriberJID);
		Element elemSub = pubsubPacket.getIQ().getChildElement().element("subscribe");
		String nickName = elemSub.attributeValue("nickname");
		if(subscriber != null) {
			// already subscribe
			Message resultMsg = new Message();
			resultMsg.setTo(pubsubPacket.getIQ().getFrom().toBareJID());
			resultMsg.setFrom(domain);

			resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
			Element elemSubscribe = DocumentHelper.createElement("subscription");
			elemSubscribe.addAttribute("node", node.getID());
			elemSubscribe.addAttribute("jid", subscriberJID);
			if(nickName != null) {
				elemSubscribe.addAttribute("nickname", nickName);
			}
			elemSubscribe.addAttribute("type", node.getType());
			elemSubscribe.addAttribute("subscription", "subscribed");
			
			resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscribe);
			router.route(resultMsg);
			//System.out.println("<*************reply**************>"+resultMsg.toString());
			return;
		}
		
		if((node.getNgqos().and(PubsubQOS.ngqos_ACCESS_MODEL)).equals(PubsubQOS.ngqos_ACCESS_MODEL_OPEN)) {
			
			memberManager.nodeAddSubscriber(node, subscriberJID, node.getNcqos(), node.getIcqos(), 0, 0);
			
			Message resultMsg = new Message();
			resultMsg.setTo(pubsubPacket.getIQ().getFrom().toBareJID());
			resultMsg.setFrom(domain);

			resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
			Element elemSubscribe = DocumentHelper.createElement("subscription");
			elemSubscribe.addAttribute("node", node.getID());
			elemSubscribe.addAttribute("jid", subscriberJID);
			if(nickName != null) {
				elemSubscribe.addAttribute("nickname", nickName);
			}
			elemSubscribe.addAttribute("type", node.getType());
			elemSubscribe.addAttribute("subscription", "agree");
			
			resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscribe);
			router.route(resultMsg);
			//System.out.println("<*************reply**************>"+resultMsg.toString());
			return;
		}
		else if((node.getNgqos().and(PubsubQOS.ngqos_ACCESS_MODEL)).equals(PubsubQOS.ngqos_ACCESS_MODEL_AUTHORIZE)) {
			
			Message resultMsg = new Message();
			resultMsg.setTo(node.getCreator());
			resultMsg.setFrom(domain);
			resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
			Element elem = pubsubPacket.getIQ().getChildElement().element("subscribe").createCopy();
			resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elem);
			resultMsg.addChildElement("body", "");
			resultMsg.getChildElement("body", "").addText("no meaning");
			
			router.route(resultMsg);
			//System.out.println("<*************reply**************>"+resultMsg.toString());
			return;
		}
		else if((node.getNgqos().and(PubsubQOS.ngqos_ACCESS_MODEL)).equals(PubsubQOS.ngqos_ACCESS_MODEL_PASSWD)) {
			
			String password = elemSub.attributeValue("password");
			
			Message resultMsg = new Message();
			resultMsg.setTo(pubsubPacket.getIQ().getFrom());
			resultMsg.setFrom(domain);

			resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
			Element elemSubscribe = DocumentHelper.createElement("subscription");
			elemSubscribe.addAttribute("node", node.getID());
			elemSubscribe.addAttribute("jid", subscriberJID);
			
			elemSubscribe.addAttribute("type", node.getType());
			
			if(password.equals(node.getPassword())) {
				
				memberManager.nodeAddSubscriber(node, subscriberJID, node.getNcqos(), node.getIcqos(), 0, 0);
				elemSubscribe.addAttribute("subscription", "agree");
			}
			else {
				elemSubscribe.addAttribute("subscription", "reject");
			}
			
			resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscribe);
			router.route(resultMsg);
			//System.out.println("<*************reply**************>"+resultMsg.toString());
		}
	}
	
	/**
	 * the creator reject the client's ask of subscribe the node, 
	 * or the creator kick subscriber out of the node.
	 * 
	 * @param pubsubPacket
	 */
	private void unsubscribeNode(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		String userJID = pubsubPacket.getData();
		
		memberManager.nodeLoadSubscribers(node);
		PubsubSubscriber subscriber = node.getSubscribersMap().get(userJID);
		
		Message resultMsg = new Message();
		resultMsg.setTo(userJID);
		resultMsg.setFrom(domain);

		resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elemSubscription = DocumentHelper.createElement("subscription");
		elemSubscription.addAttribute("node", node.getID());
		elemSubscription.addAttribute("jid", userJID);
		
		elemSubscription.addAttribute("type", node.getType());
		
		if(subscriber == null) {
			// reject user subscribe
			elemSubscription.addAttribute("subscription", "reject");
		}
		else {
			// cancel subscribe
			elemSubscription.addAttribute("subscription", "unsubscribed");
			memberManager.nodeRemoveSubscriber(node, subscriber);
		}
		
		resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscription);
		router.route(resultMsg);
		//System.out.println("<*************reply**************>"+resultMsg.toString());
	}
	
	/**
	 * the creator reject the client's ask of subscribe the smallconf, 
	 * or the creator kick subscriber out of the smallconf.
	 * 
	 * @param pubsubPacket
	 */
	private void unsubscribeSmallconf(PubsubPacket pubsubPacket) {
		
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		String userJID = pubsubPacket.getData();
		memberManager.smallconfLoadSubscribers(smallconf);
		PubsubSubscriber subscriber = smallconf.getSubscribersMap().get(userJID);
		Message resultMsg = new Message();
//		resultMsg.setTo(pubsubPacket.getIQ().getFrom());
		resultMsg.setTo(userJID);
		resultMsg.setFrom(domain);
		resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elemSubscription = DocumentHelper.createElement("scsubscription");
		elemSubscription.addAttribute("smallconf", smallconf.getID());
		elemSubscription.addAttribute("jid", userJID);
		elemSubscription.addAttribute("type", smallconf.getType());
		if(subscriber == null) {
			// reject user subscribe
			elemSubscription.addAttribute("scsubscription", "reject");
		}
		else {
			// cancel subscribe
			elemSubscription.addAttribute("scsubscription", "unsubscribed");
			memberManager.smallconfRemoveSubscriber(smallconf, subscriber);
		}
		Element elemChild = resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE);
		if(elemChild != null) {
			elemChild.add(elemSubscription);
		}
		router.route(resultMsg);
		//System.out.println("<*************reply**************>"+resultMsg.toString());
	}
	
	/**
	 * the creator agree client subscribe of the node,
	 * or the creator put some one into the node.
	 * 
	 * @param pubsubPacket
	 */
	private void subscribedNode(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();
		if(!node.getCreator().equals(iq.getFrom().toBareJID())) {
			return;
		}
		memberManager.nodeLoadSubscribers(node);
		String subJID = iq.getChildElement().element("subscribed").attributeValue("jid");
		if(node.getSubscribersMap().get(subJID) != null) {
			return;
		}
		memberManager.nodeAddSubscriber(node, subJID, node.getNcqos(), node.getIcqos(), 0, 0);
		Message resultMsg = new Message();
		resultMsg.setTo(subJID);
		resultMsg.setFrom(domain);

		resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elemSubscribe = DocumentHelper.createElement("subscription");
		elemSubscribe.addAttribute("node", node.getID());
		elemSubscribe.addAttribute("jid", subJID);
		
		elemSubscribe.addAttribute("type", node.getType());
		
		elemSubscribe.addAttribute("subscription", "agree");
		
		resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscribe);
		router.route(resultMsg);
		//System.out.println("<*************reply**************>"+resultMsg.toString());
	}
	
	/**
	 * get the node's detail information like subscribers.
	 * 
	 * @param pubsubPacket
	 */
	private void getNodeInfo(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();
		
		IQ resultIQ = iq.createCopy();
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(domain);
		resultIQ.setType(IQ.Type.result);
		
		Element elemGetNote = resultIQ.getChildElement().element("getnode");
		elemGetNote.addAttribute("creator", node.getCreator());
		if(node.getName() != null) {
			Element elemName = DocumentHelper.createElement("name");
			elemName.addText(node.getName());
			elemGetNote.add(elemName);
		}
		if(node.getDescription() != null) {
			Element elemDescription = DocumentHelper.createElement("description");
			elemDescription.addText(node.getDescription());
			elemGetNote.add(elemDescription);
		}
		Element elemPath = insertNodeParentElement(node);
		elemGetNote.add(elemPath);
		if(node.getNqos() != null) {
			Element elemNqos = DocumentHelper.createElement("nqos");
			elemNqos.addText(node.getNqos().get().toString(16));
			elemGetNote.add(elemNqos);
		}
		if(node.getIcqos() != null) {
			Element elemIcqos = DocumentHelper.createElement("icqos");
			elemIcqos.addText(node.getIcqos().get().toString(16));
			elemGetNote.add(elemIcqos);
		}
		if(node.getSubscribersMap().isEmpty()) {
			memberManager.nodeLoadSubscribers(node);
		}
		PubsubSubscriber subscriber = node.getSubscribersMap().get(iq.getFrom().toBareJID());
		String nsqos = null;
		String isqos = null;
		if(subscriber != null) {
			
			if(subscriber.getNsqos() != null) {
				nsqos = subscriber.getNsqos().get().toString(16);
			}
			if(subscriber.getIsqos() != null) {
				isqos = subscriber.getIsqos().get().toString(16);
			}
			if(!(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).compare(PubsubQOS.ncqos_ROLE_OWNER) < 0)) {
				if(node.getDeadline() > 0) {
					Element elemDeadline = DocumentHelper.createElement("deadline");
					elemDeadline.addText(Long.toString(node.getDeadline()));
					elemGetNote.add(elemDeadline);
				}
				if(node.getPassword() != null) {
					Element elemPassword = DocumentHelper.createElement("password");
					elemPassword.addText(node.getPassword());
					elemGetNote.add(elemPassword);
				}
				if(node.getItemLifecycle() > 0) {
					Element elemItemLifeCycle = DocumentHelper.createElement("item_lifecycle");
					elemItemLifeCycle.addText(Long.toString(node.getItemLifecycle()));
					elemGetNote.add(elemItemLifeCycle);
				}
			}
		}
		if(nsqos != null) {
			Element elemNsqos = DocumentHelper.createElement("nsqos");
			elemNsqos.addText(nsqos);
			elemGetNote.add(elemNsqos);
		}
		if(isqos != null) {
			Element elemIsqos = DocumentHelper.createElement("isqos");
			elemIsqos.addText(isqos);
			elemGetNote.add(elemIsqos);
		}

		Element elemSubjects = DocumentHelper.createElement("subjects");
		if(!node.getItemTypesMap().isEmpty()) {
			Set<String> k = node.getItemTypesMap().keySet();
			Iterator<String> iter = k.iterator();
			while(iter.hasNext()) {
				
				PubsubItemType itemType = node.getItemTypesMap().get(iter.next());
				
				Set<String> kk = itemType.getSubjectsMap().keySet();
				Iterator<String> iter1 = kk.iterator();
				while(iter1.hasNext()) {
					
					PubsubSubject subject = itemType.getSubjectsMap().get(iter1.next());
					if(subject != null) {
						Element elemSubject = DocumentHelper.createElement("subject");
						
						elemSubject.addAttribute("type", subject.getType());
						elemSubject.addAttribute("subid", subject.getSubid());
						elemSubject.addAttribute("name", subject.getName());
						elemSubjects.add(elemSubject);
					}
				}
			}
		}
		elemGetNote.add(elemSubjects);
		router.route(resultIQ);
		//System.out.println("<****************reply*******************>"+resultIQ.toString());
		
	}
	
	/**
	 * if node has parent node, insert the parent node's info into the IQ,
	 * else insert the root as parent.
	 * 
	 * @param node
	 * @return the element which contains the info of parent node
	 */
	private Element insertNodeParentElement(PubsubNode node) {
		
		Element elemPath = DocumentHelper.createElement("path");
		if (node.getParent() == null) {
			// no parent node
			elemPath.addAttribute("jid", "root@"+domain);
			elemPath.addAttribute("name", "root");
		} else {
			// parent node
			elemPath.addAttribute("jid", node.getParent().getID());
			elemPath.addAttribute("name", node.getParent().getName());
			elemPath.addAttribute("type", node.getParent().getType());
			elemPath.addAttribute("description", node.getParent().getDescription());
			elemPath.addAttribute("creator", node.getCreator());
		}
		return elemPath;
	}
	
	
	/**
	 * notify the node's subscribers the info of node's change
	 * 
	 * @param node
	 * @param iq
	 * @param qos
	 */
	private void nodeDispatchToSubscribers(PubsubNode node, IQ iq, PubsubQOS qos) {
		
		if(node.getSubscribersMap().isEmpty()) {
			memberManager.nodeLoadSubscribers(node);
		}
		Set<String> keySet = node.getSubscribersMap().keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext()) {
			
			String bareJID = iter.next();   
			String fullJID = bareJID+"/jclient";   
			PubsubSubscriber subscriber = node.getSubscribersMap().get(bareJID);
			
			if(!qos.and(PubsubQOS.icqos_item_SUBSCRIBE).isZero()) {
				
				iq.setTo(fullJID);
//				if(!qos.and(PubsubQOS.icqos_item_OFFLINE).isZero()) {
				if(subscriber.getStatus().equals("offline")) {
					
					if(iq.getChildElement().element("publish") != null) {
						iq.getChildElement().element("publish").addAttribute("offline", "true");
					}
					else if(iq.getChildElement().element("retract") != null) {
						iq.getChildElement().element("retract").addAttribute("offline", "true");
					}
					else if(iq.getChildElement().element("status") != null) {
						iq.getChildElement().element("status").addAttribute("offline", "true");
					}
					
					int offlineSize = PubsubPersistenceManager.getOfflineSize(bareJID);
					if(offlineSize >= 20) {
						
						PubsubPersistenceManager.deleteEarlyOffline(bareJID);
					}
					PubsubPersistenceManager.addOffline(bareJID, node.getID(), iq.toString());
					continue;
				}
				
				router.route(iq);
				//System.out.println("<****************reply*******************>"+iq.toString());
			}
		}
	}
	
	
	/**
	 * load the node's subjects
	 * 
	 * @param node
	 */
	private void loadNodeSubjects(PubsubNode node) {
		
		ArrayList<PubsubSubject> list = PubsubPersistenceManager.getNodeSubjects(node);
		if(list == null) {
			return;
		}
		Iterator<PubsubSubject> iter = list.iterator();
		while(iter.hasNext()) {
			
			PubsubSubject subject = iter.next();
			PubsubItemType itemType = node.getItemTypesMap().get(subject.getType());
			if(itemType == null) {
				continue;
			}
			else {
				itemManager.addNewSubject(subject);
			}
		}
	}
	
	/**
	 * load the node's items(history)
	 * 
	 * @param node
	 */
	private void loadNodeHistory(PubsubNode node) {
		
		ArrayList<PubsubHistory> list = PubsubPersistenceManager.getNodeHistory(node); 
		if(list == null) {
			return;
		}
		Iterator<PubsubHistory> iter = list.iterator();
		while(iter.hasNext()) {
			
			PubsubHistory history = iter.next();
			itemManager.insertNewItem(node, history);
		}
	}
	
	/**
	 * create a new smallconf
	 * 
	 * @param pubsubPacket
	 *            the packet that used to create smallconf
	 */
	private void createSmallconf(PubsubPacket pubsubPacket) {
		
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
			
		// create smallconf
		createSmallconfSynchronized(smallconf);
		
		// add smallconf into the parent's children map
		if (smallconf.getParent() != null) {
			smallconf.getParent().getChildrenMap().put(smallconf.getID(), smallconf);
		}
		
		smallconfsMap.put(smallconf.getID(), smallconf);
		// notify client the result of create. here can be a function
		IQ resultIQ = IQ.createResultIQ(pubsubPacket.getIQ());

		Element elemPubsub = pubsubPacket.getIQ().getChildElement().createCopy();
		Element elemCreate = elemPubsub.element("sccreate");
		if (elemCreate == null) {
			return;
		}
		Element elemPath = insertSmallconfParentElement(smallconf);

		elemCreate.addAttribute("smallconf", smallconf.getID());
		elemCreate.add(elemPath);
		resultIQ.setChildElement(elemPubsub);
		router.route(resultIQ);
		//System.out.println("<******************reply***********************>" + resultIQ.toString());
		// add creator to subscribers
		PubsubQOS nsqos = (PubsubQOS.ncqos_TEMP_SUB.not())
				.and(smallconf.getNcqos()).and(PubsubQOS.ncqos_ROLE.not())
				.or(PubsubQOS.ncqos_ROLE_CREATOR)
				.or(PubsubQOS.ncqos_WATCHER);
		memberManager.smallconfAddSubscriber(smallconf, smallconf.getCreator(), nsqos,	smallconf.getIcqos());

		// send message to creator
		Message replyMsg = new Message();
		replyMsg.setTo(smallconf.getCreator());
		replyMsg.setFrom(domain);

		replyMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elem1 = DocumentHelper.createElement("scsubscription");
		elem1.addAttribute("smallconf", smallconf.getID());
		elem1.addAttribute("jid", smallconf.getCreator());
		elem1.addAttribute("type", smallconf.getType());
		elem1.addAttribute("scsubscription", "agree");
		replyMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elem1);
		router.route(replyMsg);
		//System.out.println("<******************reply***********************>" + replyMsg.toString());
	}
	
	/**
	 * Guarantee the database will not be used in the same time
	 * 
	 * @param node
	 *            the new smallconf will be created
	 */
	private synchronized void createSmallconfSynchronized(PubsubSmallconf smallconf) {

		// get maxNodeJID
		String maxSmallconfJID = PubsubPersistenceManager.getMaxJID(true);
		if (maxSmallconfJID == null) {
			maxSmallconfJID = "10080000@" + domain;
		}
		
		// get newNodeJID
		int i = maxSmallconfJID.indexOf('@');
		if (i == -1) {
			return;
		}
		String temp = maxSmallconfJID.substring(0, i);
		int newSmallconfJIDInt = Integer.parseInt(temp) + 1;
		String newSmallconfJID = Integer.toString(newSmallconfJIDInt) + "@" + domain;
		
		// store new node
		smallconf.setID(newSmallconfJID);
		smallconfsMap.put(smallconf.getID(), smallconf);
		PubsubPersistenceManager.addSmallconf(smallconf, this);
	}
	private Element insertSmallconfParentElement(PubsubSmallconf smallconf) {
		
		Element elemPath = DocumentHelper.createElement("path");
		if (smallconf.getParent() == null) {
			// no parent node
			elemPath.addAttribute("jid", "smallconfroot@"+domain);
			elemPath.addAttribute("name", "root");
		} else {
			// parent node
			elemPath.addAttribute("jid", smallconf.getParent().getID());
			elemPath.addAttribute("name", smallconf.getParent().getName());
			elemPath.addAttribute("type", smallconf.getParent().getType());
			elemPath.addAttribute("description", smallconf.getParent().getDescription());
			elemPath.addAttribute("creator", smallconf.getCreator());
		}
		return elemPath;
	}
	
	/**
	 * Get smallconfs which the user have subscriber, the user is the packet from
	 * 
	 * @param pubsubPacket
	 */
	private void getSubSmallconfs(PubsubPacket pubsubPacket) {
		
		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setTo(pubsubPacket.getIQ().getFrom());
		resultIQ.setType(IQ.Type.result);
		resultIQ.setFrom(domain);
		
		Element elem = resultIQ.getChildElement().element("getsubsmallconfs");
		if(elem == null) {
			return;
		}

		 ArrayList<PubsubSubNode> subSmallconfsList = PubsubPersistenceManager.getSubSmallconfs(resultIQ.getTo().toBareJID());
		if(subSmallconfsList.isEmpty()) {
			Log.info("subSmallconfsList is empty");
		}
		Iterator<PubsubSubNode> iter = subSmallconfsList.iterator();
		while (iter.hasNext()) {
			
			Element smallconfElem = DocumentHelper.createElement("smallconf");
			PubsubSubNode subSmallconf = iter.next();
			smallconfElem.addAttribute("type", subSmallconf.getType());
			smallconfElem.addText(subSmallconf.getJID());
			elem.add(smallconfElem);
		}
		router.route(resultIQ);
		//System.out.println("<******************reply***********************>" + resultIQ.toString());
	}
	
	/**
	 * get the smallconf's detail information like subscribers.
	 * 
	 * @param pubsubPacket
	 */
	private void getSmallconfInfo(PubsubPacket pubsubPacket) {
		
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		IQ iq = pubsubPacket.getIQ();
		
		IQ resultIQ = iq.createCopy();
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(domain);
		resultIQ.setType(IQ.Type.result);
		
		Element elemGetSmallconf = resultIQ.getChildElement().element("getsmallconf");
		elemGetSmallconf.addAttribute("creator", smallconf.getCreator());
		elemGetSmallconf.addAttribute("type", smallconf.getType());
		if(smallconf.getName() != null) {
			Element elemName = DocumentHelper.createElement("name");
			elemName.addText(smallconf.getName());
			elemGetSmallconf.add(elemName);
		}
		if(smallconf.getDescription() != null) {
			Element elemDescription = DocumentHelper.createElement("description");
			elemDescription.addText(smallconf.getDescription());
			elemGetSmallconf.add(elemDescription);
		}
		Element elemPath = insertSmallconfParentElement(smallconf);
		elemGetSmallconf.add(elemPath);
		if(smallconf.getNqos() != null) {
			Element elemNqos = DocumentHelper.createElement("nqos");
			elemNqos.addText(smallconf.getNqos().get().toString(16));
			elemGetSmallconf.add(elemNqos);
		}
		if(smallconf.getIcqos() != null) {
			Element elemIcqos = DocumentHelper.createElement("icqos");
			elemIcqos.addText(smallconf.getIcqos().get().toString(16));
			elemGetSmallconf.add(elemIcqos);
		}
		if(smallconf.getSubscribersMap().isEmpty()) {
			memberManager.smallconfLoadSubscribers(smallconf);
		}
		PubsubSubscriber subscriber = smallconf.getSubscribersMap().get(iq.getFrom().toBareJID());

		if(subscriber != null) {
			
			if(iq.getFrom().toBareJID().equals(smallconf.getCreator())) {

				if(smallconf.getPassword() != null) {
					Element elemPassword = DocumentHelper.createElement("password");
					elemPassword.addText(smallconf.getPassword());
					elemGetSmallconf.add(elemPassword);
				}
			}
		}

		router.route(resultIQ);
		//System.out.println("<****************reply*******************>"+resultIQ.toString());
	}
	
	/**
	 * get the smallconf's subscribers
	 * 
	 * @param pubsubPacket 
	 */
	private void getSmallconfSubscibers(PubsubPacket pubsubPacket) {
		
		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setTo(pubsubPacket.getIQ().getFrom());
		resultIQ.setID(pubsubPacket.getIQ().getID());
		resultIQ.setType(IQ.Type.result);
		resultIQ.setFrom(pubsubPacket.getIQ().getTo());

		Element elemParticipants = resultIQ.getChildElement().element("scparticipants");

		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		HashMap<String, PubsubSubscriber> subscribersMap = null;
		
		memberManager.smallconfLoadSubscribers(smallconf);
		subscribersMap = smallconf.getSubscribersMap();
		
		Set<String> keySet = subscribersMap.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {

			PubsubSubscriber subsciber = subscribersMap.get(iter.next());
			Element elemParticipant = DocumentHelper.createElement("scparticipant");

			elemParticipant.addAttribute("jid", subsciber.getJID());
			if (subsciber.getRole().equals(PubsubEnum.state_SPONSOR)) {
				elemParticipant.addAttribute("state", "creator");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AI)) {
				elemParticipant.addAttribute("state", "ai");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AO)) {
				elemParticipant.addAttribute("state", "ao");
			} else if (subsciber.getRole().equals(PubsubEnum.state_VI)) {
				elemParticipant.addAttribute("state", "vi");
			} else if (subsciber.getRole().equals(PubsubEnum.state_VO)) {
				elemParticipant.addAttribute("state", "vo");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AIVI)) {
				elemParticipant.addAttribute("state", "aivi");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AIVO)) {
				elemParticipant.addAttribute("state", "aivo");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AOVI)) {
				elemParticipant.addAttribute("state", "aovi");
			} else if (subsciber.getRole().equals(PubsubEnum.state_AOVO)) {
				elemParticipant.addAttribute("state", "aovo");
			} else {
				elemParticipant.addAttribute("state", "attender");
			}
			elemParticipant.addAttribute("status", subsciber.getStatus());

			if (subsciber.getRole().equals(PubsubQOS.ncqos_ROLE_CREATOR)) {
				elemParticipant.addAttribute("role", "creator");
				elemParticipant.addAttribute("state", "creator");
			} else if (subsciber.getRole().equals(PubsubQOS.ncqos_ROLE_OUTCASE)) {
				elemParticipant.addAttribute("state", "outcast");
			} else {
				elemParticipant.addAttribute("role", "member");
			}

			if (subsciber.getStatus() != null) {
				elemParticipant.addAttribute("status", subsciber.getStatus());
			}
			if (subsciber.getEncrypt() != 0) {
				elemParticipant.addAttribute("encrypt", "true");
			}

			elemParticipants.add(elemParticipant);
		}

		router.route(resultIQ);
		//System.out.println("<**************reply****************>" + resultIQ.toString());
	}
	
	/**
	 * this function will never be used after the client merge the node and smallconf.
	 * 
	 * @param pubsubPacket
	 */
	private void getAllSmallconf(PubsubPacket pubsubPacket) {
		
		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setTo(pubsubPacket.getIQ().getFrom());
		resultIQ.setType(IQ.Type.result);
		Element elemGetSmallconfs = resultIQ.getChildElement().element("getsmallconfs");
		
		ArrayList<PubsubSmallconf> smallconfsList = PubsubPersistenceManager.getAllSmallconfs(this);
		Iterator<PubsubSmallconf> iter = smallconfsList.iterator();
		while(iter.hasNext()) {
			
			Element elemSmallconf = DocumentHelper.createElement("smallconf");
			PubsubSmallconf smallconf = iter.next();
			elemSmallconf.addAttribute("id", smallconf.getID());
			elemSmallconf.addAttribute("creator", smallconf.getCreator());
			if(smallconf.getType() != null) {
				elemSmallconf.addAttribute("type", smallconf.getType());
			}
			if(smallconf.getName() != null) {
				elemSmallconf.addAttribute("name", smallconf.getName());
			}
			if(smallconf.getNqos() != null) {
				if(smallconf.getNqos().and(PubsubQOS.ngqos_ACCESS_MODEL) == PubsubQOS.ngqos_ACCESS_MODEL_PASSWD) {
					
					elemSmallconf.addAttribute("password", "true");
				}
			}
			elemGetSmallconfs.add(elemSmallconf);
		}
		router.route(resultIQ);
	}
	
	/**
	 * this function will never be used after the client merge the node and smallconf.
	 * 
	 * @param pubsubPacket
	 */
	private void subscribeSmallconf(PubsubPacket pubsubPacket) {
		
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		memberManager.smallconfLoadSubscribers(smallconf);
		
		String subscriberJID = pubsubPacket.getIQ().getFrom().toBareJID();
		PubsubSubscriber subscriber = smallconf.getSubscribersMap().get(subscriberJID);
		int count = smallconf.getSubscribersMap().size();
		Element elemSub = pubsubPacket.getIQ().getChildElement().element("subsmallconf");
		String nickName = elemSub.attributeValue("nickname");
		
		if(subscriber != null) {
			// already subscribe
			Log.debug("already subscribe");
			Message resultMsg = new Message();
			resultMsg.setTo(pubsubPacket.getIQ().getFrom().toBareJID());
			resultMsg.setFrom(domain);

			resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
			Element elemSubscribe = DocumentHelper.createElement("scsubscription");
			elemSubscribe.addAttribute("smallconf", smallconf.getID());
			elemSubscribe.addAttribute("jid", subscriberJID);
			if(nickName != null) {
				elemSubscribe.addAttribute("nickname", nickName);
			}
			elemSubscribe.addAttribute("type", smallconf.getType());
			elemSubscribe.addAttribute("scsubscription", "subscribed");
			
			resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscribe);
			router.route(resultMsg);
			return;
		}
		
		
		Message resultMsg = new Message();
		resultMsg.setTo(smallconf.getCreator());
		resultMsg.setFrom(domain);

		resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elemSubscribe = DocumentHelper.createElement("subsmallconf");
		elemSubscribe.addAttribute("smallconf", smallconf.getID());
		elemSubscribe.addAttribute("jid", subscriberJID);
		if(nickName != null) {
			elemSubscribe.addAttribute("nickname", nickName);
		}
		elemSubscribe.addAttribute("verify", smallconf.getVerify());
		elemSubscribe.addAttribute("count", Integer.toString(count));
		
		resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscribe);
		router.route(resultMsg);
		return;
		
	}
	
	/**
	 * the creator agree client subscribe of the smallconf,
	 * or the creator put some one into the smallconf.
	 * 
	 * @param pubsubPacket
	 */
	private void subscribedSmallconf(PubsubPacket pubsubPacket) {
		
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		IQ iq = pubsubPacket.getIQ();
		if(!smallconf.getCreator().equals(iq.getFrom().toBareJID())) {
			return;
		}
		memberManager.smallconfLoadSubscribers(smallconf);
		String subJID = iq.getChildElement().element("scsubscribed").attributeValue("jid");
		if(smallconf.getSubscribersMap().get(subJID) != null) {
			return;
		}
		memberManager.smallconfAddSubscriber(smallconf, subJID, smallconf.getNcqos(), smallconf.getIcqos());
		
		Message resultMsg = new Message();
		resultMsg.setTo(subJID);
		resultMsg.setFrom(domain);

		resultMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elemSubscribe = DocumentHelper.createElement("scsubscription");
		elemSubscribe.addAttribute("smallconf", smallconf.getID());
		elemSubscribe.addAttribute("jid", subJID);
		
		elemSubscribe.addAttribute("type", smallconf.getType());
		
		elemSubscribe.addAttribute("scsubscription", "agree");
		
		resultMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscribe);
		router.route(resultMsg);
		
//		memberManager.smallconfAddSubscriber(smallconf, subJID, smallconf.getNcqos(), smallconf.getIcqos());
	}
	
	/**
	 * this function will never be used after the client merge the node and smallconf.
	 * 
	 * @param pubsubPacket
	 */
	private void smallconfPublish(PubsubPacket pubsubPacket) {
		
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		Element elemPubsub = pubsubPacket.getIQ().getChildElement();
		Element elemPublish = elemPubsub.element("publish");
		Element elemItem = elemPublish.element("item");

		if (elemItem == null) {
			return;
		}
		String type = elemItem.attributeValue("type");
		if (type.length() == 0) {
			return;
		}

		if (smallconf.getSubscribersMap().isEmpty()) {
			memberManager.smallconfLoadSubscribers(smallconf);
		}
		PubsubSubscriber subscriber = smallconf.getSubscribersMap().get(pubsubPacket.getIQ().getFrom().toBareJID());
		if (subscriber == null) {
			return;
		}

		IQ resultIQ = pubsubPacket.getIQ().createCopy();
		resultIQ.setType(IQ.Type.result);
		resultIQ.setFrom(pubsubPacket.getIQ().getTo());
		smallconfDispatchToSubscribers(smallconf, resultIQ);
	}
	
	
	/**
	 * notify the smallconf's subscribers the info of node's change
	 * 
	 * @param node
	 * @param iq
	 * @param qos
	 */
	private void smallconfDispatchToSubscribers(PubsubSmallconf smallconf, IQ iq) {
		
		if(smallconf.getSubscribersMap().isEmpty()) {
			memberManager.smallconfLoadSubscribers(smallconf);
		}
		Set<String> keySet = smallconf.getSubscribersMap().keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext()) {
			
			String jid = iter.next()+"/jclient";        // 这里资源不应该写死
//			if(!qos.and(PubsubQOS.icqos_item_SUBSCRIBE).isZero()) {
				
				iq.setTo(jid);
				
				try{
					iq.getChildElement().element("publish").addAttribute("offline", "true");
				}catch(Exception e) {
					// do nothing
				}
				router.route(iq);
				//System.out.println("<****************reply*******************>"+iq.toString());
//			}
		}
	}
	
 
	/**
	 * notify client when the node's subscribers changed
	 * 
	 * @param pubsubPacket
	 */
	private void nodeSubscription(PubsubPacket pubsubPacket) {
		
		String userJID = pubsubPacket.getIQ().getFrom().toBareJID();
		
		Element elemSubscription = pubsubPacket.getIQ().getChildElement().element("subscription");
		String nodeID = elemSubscription.attributeValue("ndoe");
		if(nodeID == null) {
			return;
		}
		String type = elemSubscription.attributeValue("type");
		if(type == null) {
			return;
		}
		String status = elemSubscription.attributeValue("subscription");
		if(status == null) {
			return;
		}
		
		if(status.equals("agree")) {
			PubsubPersistenceManager.userSubscriptionsAdd(userJID, nodeID, type);
		}
		else if(status.equals("unsubscribed")) {
			PubsubPersistenceManager.userSubscriptionsDelete(userJID, nodeID);
		}
		
	}
	
	/**
	 * notify client when the smallconf's subscribers changed
	 * 
	 * @param pubsubPacket
	 */
	private void smallconfSubscription(PubsubPacket pubsubPacket) {
		
		String userJID = pubsubPacket.getIQ().getFrom().toBareJID();
		
		Element elemSubscription = pubsubPacket.getIQ().getChildElement().element("scsubscription");
		String smallconfID = elemSubscription.attributeValue("smallconf");
		if(smallconfID == null) {
			return;
		}
		String type = elemSubscription.attributeValue("type");
		if(type == null) {
			return;
		}
		String status = elemSubscription.attributeValue("scsubscription");
		if(status == null) {
			return;
		}
		
		if(status.equals("agree")) {
			PubsubPersistenceManager.userSubscriptionsAdd(userJID, smallconfID, type);
		}
		else if(status.equals("unsubscribed")) {
			PubsubPersistenceManager.userSubscriptionsDelete(userJID, smallconfID);
		}
		
	}
	
	/**
	 * delete the node
	 * 
	 * @param pubsubPacket
	 */
	private void deleteNode(PubsubPacket pubsubPacket) {
		
		IQ iq = pubsubPacket.getIQ();
		String fromJID = iq.getFrom().toBareJID();
		PubsubNode node = pubsubPacket.getNode();
		if(!fromJID.equals(node.getCreator())) {
			// not the creator 
			return;
		}
		if(!node.getChildrenMap().isEmpty()) {
			// can't delete a node which has children node
			return;
		}
		
		// notify the subscribers and creator
		memberManager.nodeLoadSubscribers(node);
		Set<String> keySet = node.getSubscribersMap().keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext()) {
			
			String jid = iter.next()+"/jclient";
			IQ resultIQ = pubsubPacket.getIQ().createCopy();
			resultIQ.setTo(jid);
			resultIQ.setFrom(domain);
			resultIQ.setType(IQ.Type.result);
			Element elemDelete =  resultIQ.getChildElement().element("delete");
			elemDelete.addAttribute("node", node.getID());
			elemDelete.addAttribute("type", node.getType());
			router.route(resultIQ);
			//System.out.println("<****************reply*******************>"+resultIQ.toString());
		}
		// notify the creator
		Message replyMsg = new Message();
		replyMsg.setTo(iq.getFrom().toBareJID());
		replyMsg.setFrom(domain);

		replyMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elemSubscription = DocumentHelper.createElement("subscription");
		elemSubscription.addAttribute("node", node.getID());
		elemSubscription.addAttribute("jid", node.getCreator());
		elemSubscription.addAttribute("type", node.getType());
		elemSubscription.addAttribute("subscription", "unsubscribed");
		replyMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscription);
		
		// node_dispath_to_all(node, resp);   seems not necessary here  
//		Iterator<String> iterM = keySet.iterator();
//		while(iter.hasNext()) {
//			
//			String jid = iterM.next()+"/jclient";
//			replyMsg.setTo(jid);
//			router.route(replyMsg);
//			//System.out.println("<****************reply*******************>"+replyMsg.toString());
//		}
		
		router.route(replyMsg);
		//System.out.println("<****************reply*******************>"+replyMsg.toString());
		
		PubsubPersistenceManager.userSubscriptionsDelete(null, node.getID());
		PubsubPersistenceManager.nodeDelete(node);
		if(node.getParent() == null || node.getParent().getID().equals("root@"+domain)) {
			
		}
		else {
			PubsubNode parentNode = node.getParent();
			parentNode.getChildrenMap().remove(node.getID());
		}
	}
	
	/**
	 * delete the smallconf
	 * 
	 * @param pubsubPacket
	 */
	private void deleteSmallconf(PubsubPacket pubsubPacket) {
		
		IQ iq = pubsubPacket.getIQ();
		String fromJID = iq.getFrom().toBareJID();
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		if(!fromJID.equals(smallconf.getCreator())) {
			// not the creator 
			return;
		}
		if(!smallconf.getChildrenMap().isEmpty()) {
			// can't delete a node which has children node
			return;
		}
		
		// notify the subscribers and creator
		memberManager.smallconfLoadSubscribers(smallconf);
		Set<String> keySet = smallconf.getSubscribersMap().keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext()) {
			
			String jid = iter.next()+"/jclient";
			IQ resultIQ = pubsubPacket.getIQ().createCopy();
			resultIQ.setTo(jid);
			resultIQ.setFrom(domain);
			resultIQ.setType(IQ.Type.result);
			Element elemDelete =  resultIQ.getChildElement().element("scdelete");
			elemDelete.addAttribute("smallconf", smallconf.getID());
			elemDelete.addAttribute("type", smallconf.getType());
			router.route(resultIQ);
			//System.out.println("<****************reply*******************>"+resultIQ.toString());
		}
		// notify the creator
		Message replyMsg = new Message();
		replyMsg.setTo(iq.getFrom().toBareJID());
		replyMsg.setFrom(domain);

		replyMsg.addChildElement("pubsub", PUBSUB_NAMESPACE);
		Element elemSubscription = DocumentHelper.createElement("scsubscription");
		elemSubscription.addAttribute("smallconf", smallconf.getID());
		elemSubscription.addAttribute("jid", smallconf.getCreator());
		elemSubscription.addAttribute("type", smallconf.getType());
		elemSubscription.addAttribute("scsubscription", "unsubscribed");
		replyMsg.getChildElement("pubsub", PUBSUB_NAMESPACE).add(elemSubscription);
		
		// smallconf_dispath_to_all(node, resp);   seems not necessary here  
//		Iterator<String> iterM = keySet.iterator();
//		while(iter.hasNext()) {
//			
//			String jid = iterM.next()+"/jclient";
//			replyMsg.setTo(jid);
//			router.route(replyMsg);
//			//System.out.println("<****************reply*******************>"+replyMsg.toString());
//		}
		
		router.route(replyMsg);
		//System.out.println("<****************reply*******************>"+replyMsg.toString());
		
		PubsubPersistenceManager.userSubscriptionsDelete(null, smallconf.getID());
		PubsubPersistenceManager.smallconfDelete(smallconf);
		if(smallconf.getParent() == null || smallconf.getParent().getID().equals("smallconfroot@"+domain)) {
			
		}
		else {
			PubsubSmallconf parentSmallconf = smallconf.getParent();
			parentSmallconf.getChildrenMap().remove(smallconf.getID());
		}
	}
	
	
	/**
	 * get the children node, seems never be used 
	 * 
	 * @param pubsubPacket
	 */
	private void getChild(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();
		
		IQ resultIQ = iq.createCopy();
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(domain);
		resultIQ.setType(IQ.Type.result);
		Element elemGetChild= resultIQ.getChildElement().element("getchild");
		elemGetChild.addAttribute("node", node.getID());
		elemGetChild.addAttribute("type", node.getType());
		
		ArrayList<PubsubNode> childList = PubsubPersistenceManager.getChildrenByParentID(node.getID(), this);
		Iterator<PubsubNode> iter = childList.iterator();
		while(iter.hasNext()) {
			
			PubsubNode childNode = iter.next();
			Element elemChild = DocumentHelper.createElement("node");
			
			elemChild.addAttribute("id", childNode.getID());
			elemChild.addAttribute("name", childNode.getName());
			elemChild.addAttribute("type", childNode.getType());
			elemChild.addAttribute("creator", childNode.getCreator());
			elemChild.addAttribute("description", childNode.getDescription());
			if(childNode.getNqos().and(PubsubQOS.ngqos_ACCESS_MODEL).equals(PubsubQOS.ngqos_ACCESS_MODEL_PASSWD)) {
				
				elemChild.addAttribute("password", "true");
			}
			elemGetChild.add(elemChild);
		}
		router.route(resultIQ);
		//System.out.println("<*************reply**************>"+resultIQ.toString());
	}
	
	
	/**
	 * get the item's info
	 * 
	 * @param pubsubPacket
	 */
	private void getConfItemDetail(PubsubPacket pubsubPacket) {
		
		IQ iq = pubsubPacket.getIQ();
		IQ resultIQ = iq.createCopy();
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(domain);
		resultIQ.setType(IQ.Type.result);
		
		String itemID = pubsubPacket.getData();
		PubsubHistory item = PubsubPersistenceManager.getNodeItem(itemID);
		Element elemGetitemdetail = resultIQ.getChildElement().element("getitemdetail");
		
		Element elemItem = DocumentHelper.createElement("item");
		elemItem.addAttribute("id", item.getID());
		elemItem.addAttribute("publisher", item.getPublisher());
		if(item.getType() != null) {
			elemItem.addAttribute("type", item.getType());
		}
		if(item.getContent() != null) {
			elemItem.addAttribute("content", item.getContent());
		}
		if(item.getSubid() != null) {
			elemItem.addAttribute("subid", item.getSubid());
		}
		if(item.getHandler() != null) {
			elemItem.addAttribute("handler", item.getHandler());
		}
		if(item.getTime() != null) {
			elemItem.addAttribute("time", item.getTime());
		}
		if(item.getBody() != null) {
			elemItem.addAttribute("body", item.getBody());
		}
		elemGetitemdetail.add(elemItem);
		router.route(resultIQ);
		//System.out.println("<*************reply**************>"+resultIQ.toString());
	}
	
	/**
	 * get the personal node info
	 * 
	 * @param pubsubPacket
	 */
	private void getPersonalItemDetail(PubsubPacket pubsubPacket) {
	
		IQ iq = pubsubPacket.getIQ();
		IQ resultIQ = iq.createCopy();
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(domain);
		resultIQ.setType(IQ.Type.result);
		
		String itemID = pubsubPacket.getData();
		PubsubHistory item = PubsubPersistenceManager.getPersonalItem(itemID);
		Element elemGetitemdetail = resultIQ.getChildElement().element("getperitemdetail");
		
		Element elemItem = DocumentHelper.createElement("item");
		elemItem.addAttribute("id", item.getID());
		elemItem.addAttribute("publisher", item.getPublisher());
		if(item.getType() != null) {
			elemItem.addAttribute("type", item.getType());
		}
		if(item.getContent() != null) {
			elemItem.addAttribute("content", item.getContent());
		}
		if(item.getSubid() != null) {
			if(!item.getSubid().equals("file") && !item.getSubid().equals("notify")) {
				elemItem.addAttribute("subid", item.getSubid());
			}
			else {
				//
			}
		}
		if(item.getHandler() != null) {
			elemItem.addAttribute("handler", item.getHandler());
		}
		if(item.getTime() != null) {
			elemItem.addAttribute("time", item.getTime());
		}
		if(item.getBody() != null) {
			elemItem.addAttribute("body", item.getBody());
		}
		elemGetitemdetail.add(elemItem);
		router.route(resultIQ);
		//System.out.println("<*************reply**************>"+resultIQ.toString());
	}
	
	/**
	 * get the conference offline messages
	 * 
	 * @param pubsubPacket
	 */
	private void getOffline(PubsubPacket pubsubPacket) {
		
		ArrayList<String> xmlList = PubsubPersistenceManager.getUserOffline(pubsubPacket.getIQ().getFrom().toBareJID());
		Iterator<String> iter = xmlList.iterator();
		while(iter.hasNext()) {
			Document doc;
			try {
				doc = DocumentHelper.parseText(iter.next());
			} catch (DocumentException e) {
				e.printStackTrace();
				return;
			}
			IQ offlineIQ = new IQ(doc.getRootElement());
			router.route(offlineIQ);
			//System.out.println("<*************reply**************>"+offlineIQ.toString());
		}
	}
	
	/**
	 * delete item(history)
	 * 
	 * @param pubsubPacket
	 */
	private void deleteItem(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();
		
		Element elemRetract = iq.getChildElement().element("retract");
		Element elemItem = elemRetract.element("item");
		if(elemItem == null) {
			return;
		}
		String itemID = elemItem.attributeValue("id");
		String type = elemItem.attributeValue("type");
		String subid = elemItem.attributeValue("subid");
		if(subid == null) {
			subid = type;
		}
		
		PubsubItemType itemType = node.getItemTypesMap().get(type);
		if(itemType == null) {
			return;
		}
		PubsubSubject subject = itemType.getSubjectsMap().get(subid);
		if(subject == null) {
			return;
		}
		PubsubHistory item = subject.getHistoriesMap().get(itemID);
		if(item == null) {
			return;
		}
		PubsubSubscriber subscriber = null;
		if(node.getType().equals("personal")) {
			
			if(!node.getCreator().equals(iq.getFrom().toBareJID()) 
					&& !item.getPublisher().equals(iq.getFrom().toBareJID())) {
				// no authority to delete
				return;
			}
		}
		else {
			
			if(node.getSubscribersMap().isEmpty()) {
				memberManager.nodeLoadSubscribers(node);
			}
			subscriber = node.getSubscribersMap().get(iq.getFrom().toBareJID());
			if(subscriber == null) {
					
				return;
			}
			if(!node.getCreator().equals(iq.getFrom().toBareJID())) {
//					&& (!subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE_OWNER).isZero())
//					&& (!item.getPublisher().equals(iq.getFrom().toBareJID())) ) {
				// no authority to delete
//				System.out.println("~~no authority to delete~~~");
				Log.info("no authority to delete file");
				return;
			}
		}
		PubsubPersistenceManager.deleteItem(itemID, node.getType());
		subject.getHistoriesMap().remove(itemID);
		
		if(node.getType().equals("personal")) {
			IQ resultIQ = iq.createCopy();
			resultIQ.setTo(iq.getFrom());
			resultIQ.setFrom(domain);
			resultIQ.setType(IQ.Type.result);
			router.route(resultIQ);
			//System.out.println("<*************reply**************>"+resultIQ.toString());
		}
		else {
			IQ resultIQ = iq.createCopy();
			resultIQ.setTo(subscriber.getJID());
			resultIQ.setFrom(domain);
			resultIQ.setType(IQ.Type.result);

			nodeDispatchToSubscribers(node, resultIQ, itemType.getQos());
		}
		return;
	}
	
	
	/**
	 * set the item status
	 * 
	 * @param pubsubPacket
	 */
	private void setStatus(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();
		Element elemStatus = iq.getChildElement().element("status");
		String itemID = elemStatus.attributeValue("id");
		String type = elemStatus.attributeValue("type");
		String subid = elemStatus.attributeValue("subid");
		if(subid == null) {
			subid = type;
		}
		
		PubsubItemType itemType = node.getItemTypesMap().get(type);
		if(itemType == null) {
			return;
		}
		PubsubSubject subject = itemType.getSubjectsMap().get(subid);
		if(subject == null) {
			return;
		}
		PubsubHistory item = subject.getHistoriesMap().get(itemID);
		if(item == null) {
			return;
		}
		String status = elemStatus.attributeValue("status");
		PubsubPersistenceManager.updateHistory(item, false);
		
		item.setStatus(status);
		if(node.getSubscribersMap().isEmpty()) {
			memberManager.nodeLoadSubscribers(node);
		}
		IQ resultIQ = iq.createCopy();
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(domain);
		resultIQ.setType(IQ.Type.result);
		nodeDispatchToSubscribers(node, resultIQ, itemType.getQos());
	}
	
	
	/**
	 * configure the node or personal node
	 * 
	 * @param pubsubPacket
	 */
	private void setConfigure(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();
		boolean isPersonal = false;
		if(!iq.getFrom().toBareJID().equals(node.getCreator())) {
			return;
		}
		Element elemConfigure = iq.getChildElement().element("configure");
		String nodeType = elemConfigure.attributeValue("type");
		if(nodeType.equals("personal")) {
			isPersonal = true;
		}
		else {
			isPersonal = false;
		}
		
		Element elemName = elemConfigure.element("name");
		if(elemName != null) {
			
			String name = elemName.getText();
			if(name != null) {
				node.setName(name);
				PubsubPersistenceManager.updateNode(node, this);
			}
		}
		Element elemDescription = elemConfigure.element("description");
		if(elemDescription != null) {
			
			String description = elemDescription.getText();
			if(description != null) {
				node.setDescription(description);
				PubsubPersistenceManager.updateNode(node, this);
			}
		}
		
		Element elemParent = elemConfigure.element("parent");
		if(elemParent != null) {
			
			String newParentID = elemParent.getText();
			if(newParentID != null) {
				PubsubNode oldParentNode = node.getParent();
				if(oldParentNode != null) {
					oldParentNode.getChildrenMap().remove(node.getID());
				}
				PubsubNode newParentNode = getNodeByJID(newParentID, false);
				if(newParentNode == null) {
					return;
				}
				newParentNode.getChildrenMap().put(node.getID(), node);
				node.setParent(newParentNode);
				PubsubPersistenceManager.updateNode(node, this);
			}
		}
		Element elemDeadline = elemConfigure.element("deadline");
		if(elemDeadline != null) {
			
			String deadline = elemDeadline.getText();
			if(deadline != null) {
				node.setDeadline(Long.parseLong(deadline));
				PubsubPersistenceManager.updateNode(node, this);
			}
		}
		Element elemItemLifecycle = elemConfigure.element("item_lifecycle");
		if(elemItemLifecycle != null) {
			
			String itemLifecycle = elemItemLifecycle.getText();
			if(itemLifecycle != null) {
				node.setItemLifecycle(Long.parseLong(itemLifecycle));
				PubsubPersistenceManager.updateNode(node, this);
			}
		}
		Element elemPassword = elemConfigure.element("password");
		if(elemPassword != null) {
			
			String password = elemPassword.getText();
			if(password != null) {
				node.setPassword(password);
				PubsubPersistenceManager.updateNode(node, this);
			}
		}
		Element elemNqos = elemConfigure.element("nqos");
		if(elemNqos != null) {
			
			String nqos = elemNqos.getText();
			if(nqos != null) {
				node.setNqos(new PubsubQOS(nqos));
				PubsubPersistenceManager.updateNode(node, this);
			}
		}
		
		Element elemAddsub = elemConfigure.element("addsub");
		if(elemAddsub != null) {
			
			String type = elemAddsub.attributeValue("type");
			if(type == null) {
				return;
			}
			String subid = elemAddsub.attributeValue("subid");
			if(subid == null) {
				return;
			}
			String name = elemAddsub.attributeValue("name");
			if(name == null) {
				name = "new_subject";
			}
			PubsubItemType itemType = node.getItemTypesMap().get(type);
			if(itemType == null) {
				return;
			}
			PubsubSubject subject = itemType.getSubjectsMap().get(subid);
			if(subject == null) {
				itemManager.createNewSubject(node, type, subid, name, isPersonal);
			}
			else {
				PubsubPersistenceManager.updateSubject(node.getID(), subid, name, isPersonal);
			}
		}
		
		Element elemDeletesub = elemConfigure.element("deletesub");
		if(elemDeletesub != null) {
			String type = elemDeletesub.attributeValue("type");
			if(type == null) {
				return;
			}
			String subid = elemDeletesub.attributeValue("subid");
			if(subid == null) {
				return;
			}
			PubsubItemType itemType = node.getItemTypesMap().get(type);
			if(itemType == null) {
				return;
			}
			PubsubSubject subject = itemType.getSubjectsMap().get(subid);
			if(subject == null) {
				return;
			}
			else {
				PubsubPersistenceManager.deleteSubject(node.getID(), subid, type, isPersonal);
				itemType.getSubjectsMap().remove(subid);
			}
		}
		IQ resultIQ = iq.createCopy();
		resultIQ.setType(IQ.Type.result);
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(iq.getTo());
		//System.out.println("<******************reply********************>"+resultIQ.toString());
		router.route(resultIQ);
		return;
		// the following seems not to be used
		// addtype
		// deletetype
	}
	
	
	/**
	 * configure smallconf 
	 * 
	 * @param pubsubPacket
	 */
	private void setSmallconfConfigure(PubsubPacket pubsubPacket) {
		
		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		IQ iq = pubsubPacket.getIQ();
		if(!iq.getFrom().toBareJID().equals(smallconf.getCreator())) {
			return;
		}
		Element elemConfigure = iq.getChildElement().element("scconfigure");
		Element elemName = elemConfigure.element("name");
		if(elemName != null) {
			
			String name = elemName.getText();
			if(name != null) {
				smallconf.setName(name);
			}
		}
		Element elemDescription = elemConfigure.element("description");
		if(elemDescription != null) {
			
			String description = elemDescription.getText();
			if(description != null) {
				smallconf.setDescription(description);
			}
		}
		Element elemParent = elemConfigure.element("parent");
		if(elemParent != null) {
			
			String newParentID = elemParent.getText();
			if(newParentID != null) {
				PubsubSmallconf oldParentSmallconf = smallconf.getParent();
				if(oldParentSmallconf != null) {
					oldParentSmallconf.getChildrenMap().remove(smallconf.getID());
				}
				PubsubSmallconf newParentSmallconf = getSmallconfByJID(newParentID);
				if(newParentSmallconf == null) {
					return;
				}
				newParentSmallconf.getChildrenMap().put(smallconf.getID(), smallconf);
				smallconf.setParent(newParentSmallconf);
			}
		}
		
		Element elemPassword = elemConfigure.element("password");
		if(elemPassword != null) {
			
			String password = elemPassword.getText();
			if(password != null) {
				smallconf.setPassword(password);
			}
		}
		Element elemNqos = elemConfigure.element("nqos");
		if(elemNqos != null) {
			
			String nqos = elemNqos.getText();
			if(nqos != null) {
				smallconf.setNqos(new PubsubQOS(nqos));
			}
		}
		PubsubPersistenceManager.updateSmallconf(smallconf, this);
	}
	
	/**
	 * get the node or personal node items(history)
	 * 
	 * @param pubsubPacket
	 */
	private void getHistory(PubsubPacket pubsubPacket) {
		
		PubsubNode node = pubsubPacket.getNode();
		IQ iq = pubsubPacket.getIQ();
		String type = node.getType();
		PubsubSubscriber subscriber = null;
		if(!type.equals("personal")) {
			if(node.getSubscribersMap().isEmpty()) {
				memberManager.nodeLoadSubscribers(node);
			}
			subscriber = node.getSubscribersMap().get(iq.getFrom().toBareJID());
			if(subscriber == null) {
				return;
			}
		}
		IQ resultIQ = iq.createCopy();
		resultIQ.setType(IQ.Type.result);
		resultIQ.setTo(iq.getFrom());
		resultIQ.setFrom(domain);
		Element elemGethistory = iq.getChildElement().element("gethistory");
		if(elemGethistory == null) {
			return;
		}
		if(elemGethistory.element("items") != null){
//			Iterator<Element> elemItemIter = elemGethistory.elementIterator("items");
//			while(elemItemIter.hasNext()) {

				Element elemItems = elemGethistory.element("items");
				String type1 = elemItems.attributeValue("type");
				if(type1 != null) {
					PubsubItemType itemType = node.getItemTypesMap().get(type1);
					if(itemType == null) {
						return;
					}
					if(type.equals("personal")) {
						insertHistoryElement(resultIQ, node, type1);
					}
					else {
						if(!itemType.getQos().and(PubsubQOS.icqos_item_SUBSCRIBE).isZero()) {
							insertHistoryElement(resultIQ, node, type1);
						}
					}
				}
//			}
		}
		else {
			
			if(type.equals("personal")) {
				Set<String> key = node.getItemTypesMap().keySet();
				Iterator<String> iter = key.iterator();
				while(iter.hasNext()) {
					
					PubsubItemType itemType = node.getItemTypesMap().get(iter.next());
					insertHistoryElement(resultIQ, node, itemType.getTypeName());
				}
			}
			else {
				if(node.getCreator().equals(iq.getFrom().toBareJID())) {
					
					Set<String> key = node.getItemTypesMap().keySet();
					Iterator<String> iter = key.iterator();
					while(iter.hasNext()) {
						String typeName = iter.next();
						if(!typeName.equals("broad") && !typeName.equals("notify")) {
							PubsubItemType itemType = node.getItemTypesMap().get(iter.next());
							insertHistoryElement(resultIQ, node, itemType.getTypeName());
						}
					}
				}
				else {
					Set<String> key = node.getItemTypesMap().keySet();
					Iterator<String> iter = key.iterator();
					while(iter.hasNext()) {
						
						PubsubItemType itemType = node.getItemTypesMap().get(iter.next());
						insertHistoryElement(resultIQ, node, itemType.getTypeName());
					}
				}
			}
		}
		
		router.route(resultIQ);
	}
	
	/**
	 * insert the items info result iq
	 * 
	 * @param iq
	 * @param node
	 * @param type
	 */
	private void insertHistoryElement(IQ iq, PubsubNode node, String type) {
		
		Element elemGethistory = iq.getChildElement().element("gethistory");
		Element elemItems = elemGethistory.element("items");
		if(elemItems == null) {
			elemItems = DocumentHelper.createElement("items");
			elemGethistory.add(elemItems);
		}
		PubsubItemType itemType = node.getItemTypesMap().get(type);
		if(itemType == null) {
			return;
		}
		Map<String, PubsubSubject> subjectsMap = itemType.getSubjectsMap();
		Set<String> key = subjectsMap.keySet();
		Iterator<String> iter = key.iterator();
		while(iter.hasNext()) {
			
			PubsubSubject subject = subjectsMap.get(iter.next());
			Set<String> key1 = subject.getHistoriesMap().keySet();
			Iterator<String> iter1 = key1.iterator();
			while(iter1.hasNext()) {
				
				PubsubHistory item = subject.getHistoriesMap().get(iter1.next());
				Element elemItem = DocumentHelper.createElement("item");
				if(item.getID() != null) {
					elemItem.addAttribute("ID", item.getID());
				}
				if(item.getSubid() != null) {
					elemItem.addAttribute("subid", item.getSubid());
				}
				if(item.getPublisher() != null) {
					elemItem.addAttribute("publisher", item.getPublisher());
				}
				if(item.getTime() != null) {
					elemItem.addAttribute("time", item.getTime());
				}
				if(item.getContent() != null) {
					Element elemContent = DocumentHelper.createElement("content");
					elemContent.setText(item.getContent());
					elemItem.add(elemContent);
				}
				if(item.getBody() != null) {
					Element elemBody = DocumentHelper.createElement("body");
					elemBody.setText(item.getBody());
					elemItem.add(elemBody);
				}
				if(item.getStatus() != null) {
					elemItem.addAttribute("status", item.getStatus());
				}
				if(item.getHandler() != null) {
					elemItem.addAttribute("handler", item.getHandler());
				}
				
				elemItems.add(elemItem);
			}
		}
	}
	
/*	
	public void userPresenceChanged(String jid) {
		
		ArrayList<PubsubSubNode> subNodesList = PubsubPersistenceManager.getSubNodes(jid);
		if(subNodesList.isEmpty()) {
			Log.debug("subNodesList is empty");
		}
		//System.out.println("1~~~~~~~~~~~~~~~~~~~~~~");
		Iterator<PubsubSubNode> iter = subNodesList.iterator();
		while (iter.hasNext()) {
			
			//System.out.println("2~~~~~~~~~~~~~~~~~~~~~~");
			PubsubSubNode subNode = iter.next();
			String nodeID = subNode.getJID();
			PubsubNode node = getNodeByJID(nodeID, false);
			if( node.getSubscribersMap().isEmpty() ) {
				memberManager.nodeLoadSubscribers(node);
			}
			PubsubSubscriber subscriber = node.getSubscribersMap().get(jid);
			if(subscriber == null) {
				continue;
			}
			subscriber.setStatus("offline");
			
//			memberManager.conferenceUpdateParticipant(node);
			HashMap<String, PubsubSubscriber> subscriberMap = node.getSubscribersMap();
			Set<String> keySet = subscriberMap.keySet();
			Iterator<String> iter1 = keySet.iterator();
			while(iter1.hasNext()) {
				
				PubsubSubscriber subscriber1 = subscriberMap.get(iter1.next());
				IQ notifyIQ = new IQ(IQ.Type.result);
				notifyIQ.setFrom(domain);
				notifyIQ.setTo(subscriber1.getJID()+"/jclient");
				
				Element elemPubsub = DocumentHelper.createElement("pubsub");
				elemPubsub.addNamespace("", PubsubService.PUBSUB_NAMESPACE);
				Element elemParticipant = DocumentHelper.createElement("participant");
				elemParticipant.addAttribute("node", subscriber1.getNode().getID());
				elemParticipant.addAttribute("jid", jid);
				elemParticipant.addAttribute("status", "offline");
				if(subscriber1.getState().equals(PubsubEnum.state_SPONSOR)) {
					elemParticipant.addAttribute("state", "creator");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_AI)) {
					elemParticipant.addAttribute("state", "ai");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_AO)) {
					elemParticipant.addAttribute("state", "ao");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_VI)) {
					elemParticipant.addAttribute("state", "vi");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_VO)) {
					elemParticipant.addAttribute("state", "vo");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_AIVI)) {
					elemParticipant.addAttribute("state", "aivi");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_AIVO)) {
					elemParticipant.addAttribute("state", "aivo");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_AOVI)) {
					elemParticipant.addAttribute("state", "aovi");
				}
				else if(subscriber.getState().equals(PubsubEnum.state_AOVO)) {
					elemParticipant.addAttribute("state", "aovo");
				}
				else {
					elemParticipant.addAttribute("state", "attender");
				}
				
				if(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).equals(PubsubQOS.ncqos_ROLE_OUTCASE)) {
					elemParticipant.addAttribute("role", "outcast");
				}
				else if(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).equals(PubsubQOS.ncqos_R0LE_OWNER)) {
					elemParticipant.addAttribute("role", "owner");
				}
				else if(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).equals(PubsubQOS.ncqos_ROLE_CREATOR)) {
					elemParticipant.addAttribute("role", "creator");
				}
				else {
					elemParticipant.addAttribute("role", "member");
				}
				
				if(subscriber.getWatcher() != 0) {
					elemParticipant.addAttribute("watcher", "true");
				}
				if(subscriber.getEncrypt() != 0) {
					elemParticipant.addAttribute("encrypt", "true");
				}
				elemPubsub.add(elemParticipant);
				notifyIQ.setChildElement(elemPubsub);
				
				router.route(notifyIQ);
				//System.out.println("<***************reply*****************>"+notifyIQ.toString());
				synchronized (PubsubService.timeMap) {
					PubsubService.timeMap.remove(jid);
				}
			}
		}
	}
*/			
	

//	public PacketRouter getRouter() {
//		return this.router;
//	}
	/**
	 * @return the server's domain
	 */
	public String getDomain() {
		return this.domain;
	}
	/**
	 * @return the server's itemManager
	 */
	public PubsubItemManager getItemManager() {
		return this.itemManager;
	}
	/**
	 * @return the server's memberManager
	 */
	public PubsubMemberManager getMemberManager() {
		return this.memberManager;
	}
	/**
	 * @return the server's PacketRouter
	 */
	public PacketRouter getPacketRouter() {
		return this.router;
	}
}
