package com.instlink.openfire.pubsub.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;


/**
 * all operation method to group or smallconf's subscriber  
 * 
 * @author LC
 *
 */
public class PubsubMemberManager {
	
	private static final Logger Log = LoggerFactory.getLogger(PubsubMemberManager.class);
	private PubsubService service = null;
	public PubsubMemberManager(PubsubService service) {
		this.service = service;
	}

	/**
	 * node add a new subscriber
	 * 
	 * @param node 
	 * @param subscriberJID
	 * @param nsqos
	 * @param isqos
	 * @param history_range
	 * @param deadline
	 */
	public void nodeAddSubscriber(PubsubNode node, String subscriberJID,
			PubsubQOS nsqos, PubsubQOS isqos, long history_range, long deadline) {
		
		nodeLoadSubscribers(node);
		
		PubsubSubscriber subscriber = subscriberNew(node, subscriberJID, nsqos, isqos, history_range, deadline);
		if(subscriber == null) {
			return;
		}
		node.getSubscribersMap().put(subscriber.getJID(),subscriber);
		PubsubPersistenceManager.nodeAddSubscriber(subscriber);
		PubsubPersistenceManager.userSubscriptionsAdd(subscriberJID, node.getID(), node.getType());
		
//		conferenceUpdateParticipant(subscriberJID, node, subscriber.getStatus());
	}
	
	/**
	 * create a new subscriber object
	 * 
	 * @param node
	 * @param subscriberJID
	 * @param nsqos
	 * @param isqos
	 * @param history_range
	 * @param deadline
	 * @return
	 */
	public PubsubSubscriber subscriberNew(PubsubNode node, String subscriberJID,
			PubsubQOS nsqos, PubsubQOS isqos, long history_range, long deadline) {
		
		PubsubSubscriber subscriber = new PubsubSubscriber();
		subscriber.setJID(subscriberJID);
		subscriber.setNode(node);
		
		if ((nsqos.and(PubsubQOS.ncqos_ROLE)).equals(PubsubQOS.ncqos_ROLE_CREATOR)) {
			
			subscriber.setState(PubsubEnum.state_SPONSOR);
		}
		else {
			
			subscriber.setState(PubsubEnum.state_ATTENDER);
		}
		subscriber.setStatus("offline");
		subscriber.setRole(nsqos.and(PubsubQOS.ncqos_ROLE));
		subscriber.setNsqos(nsqos);
		subscriber.setIsqos(isqos);
		if (!(nsqos.and(PubsubQOS.ncqos_WATCHER).isZero())) {
			
			subscriber.setWatcher(1);
		}
		return subscriber;
	}
	/**
	 * notify all this node subscribers the change
	 * 
	 * @param node
	 */
	public void conferenceUpdateParticipantOffline(String jid, String status) {
		
		ArrayList<PubsubSubNode> subNodesList = PubsubPersistenceManager.getSubNodes(jid);
		if(subNodesList.isEmpty()) {
			Log.info("subNodesList is empty");
		}
		Iterator<PubsubSubNode> iter = subNodesList.iterator();
		while (iter.hasNext()) {
			
			PubsubSubNode subNode = iter.next();
			String nodeID = subNode.getJID();
			PubsubNode node = service.getNodeByJID(nodeID, false);
			if(node == null) {
				return;
			}
			if( node.getSubscribersMap().isEmpty() ) {
				nodeLoadSubscribers(node);
			}
			PubsubSubscriber subscriber = node.getSubscribersMap().get(jid);
			if(subscriber == null) {
				continue;
			}
			subscriber.setStatus(status);
			
//			memberManager.conferenceUpdateParticipant(node);
			HashMap<String, PubsubSubscriber> subscriberMap = node.getSubscribersMap();
			Set<String> keySet = subscriberMap.keySet();
			Iterator<String> iter1 = keySet.iterator();
			while(iter1.hasNext()) {
				
				PubsubSubscriber subscriber1 = subscriberMap.get(iter1.next());
				
				if(subscriber1.getStatus().equals("offline")) {
					continue;
				}
				IQ notifyIQ = new IQ(IQ.Type.result);
				notifyIQ.setFrom(service.getDomain());
				notifyIQ.setTo(subscriber1.getJID()+"/jclient");
				
				Element elemPubsub = DocumentHelper.createElement("pubsub");
				elemPubsub.addNamespace("", PubsubService.PUBSUB_NAMESPACE);
				Element elemParticipant = DocumentHelper.createElement("participant");
				elemParticipant.addAttribute("node", subscriber1.getNode().getID());
				elemParticipant.addAttribute("jid", jid);
				elemParticipant.addAttribute("status", status);
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
				else if(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).equals(PubsubQOS.ncqos_ROLE_OWNER)) {
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
				
				service.getPacketRouter().route(notifyIQ);
				//System.out.println("<***************reply*****************>"+notifyIQ.toString());
			}
		}
	}
	
	/**
	 * update the subscriber's status and notify the node's subscribers
	 * 
	 * @param jid
	 * @param node
	 * @param status
	 */
	public void conferenceUpdateParticipant(String jid, PubsubNode node, String status) {
		
//		String status = "online";
		if( node.getSubscribersMap().isEmpty() ) {
			nodeLoadSubscribers(node);
		}
		PubsubSubscriber subscriber = node.getSubscribersMap().get(jid);
		if(subscriber == null) {
			return;
		}
		subscriber.setStatus(status);
		
//		memberManager.conferenceUpdateParticipant(node);
		HashMap<String, PubsubSubscriber> subscriberMap = node.getSubscribersMap();
		Set<String> keySet = subscriberMap.keySet();
		Iterator<String> iter1 = keySet.iterator();
		while(iter1.hasNext()) {
			
			PubsubSubscriber subscriber1 = subscriberMap.get(iter1.next());
			
			if(subscriber1.getStatus().equals("offline")) {
				continue;
			}
			IQ notifyIQ = new IQ(IQ.Type.result);
			notifyIQ.setFrom(service.getDomain());
			notifyIQ.setTo(subscriber1.getJID()+"/jclient");
			
			Element elemPubsub = DocumentHelper.createElement("pubsub");
			elemPubsub.addNamespace("", PubsubService.PUBSUB_NAMESPACE);
			Element elemParticipant = DocumentHelper.createElement("participant");
			elemParticipant.addAttribute("node", subscriber1.getNode().getID());
			elemParticipant.addAttribute("jid", jid);
			elemParticipant.addAttribute("status", status);
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
			else if(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).equals(PubsubQOS.ncqos_ROLE_OWNER)) {
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
			
			service.getPacketRouter().route(notifyIQ);
			//System.out.println("<***************reply*****************>"+notifyIQ.toString());
		}
		
	}
	
	/**
	 * smallconf add a new subscriber
	 * 
	 * @param smallconf
	 * @param subscriberJID
	 * @param nsqos
	 * @param isqos
	 */
	public void smallconfAddSubscriber(PubsubSmallconf smallconf, String subscriberJID, 
			PubsubQOS nsqos, PubsubQOS isqos) {
		
		smallconfLoadSubscribers(smallconf);
		PubsubSubscriber subscriber = smallconfSubscriberNew(smallconf, subscriberJID, nsqos, isqos);
		if(subscriber == null) {
			return;
		}
		smallconf.getSubscribersMap().put(subscriber.getJID(), subscriber);
		PubsubPersistenceManager.smallconfAddSubscriber(subscriber);
		PubsubPersistenceManager.userSubscriptionsAdd(subscriberJID, smallconf.getID(), smallconf.getType());
		
//		smallconfUpdateParticipant(subscriberJID, smallconf, subscriber.getStatus());
	}
	
	
	/**
	 * load the smallconf's subscribers 
	 * 
	 * @param smallconf
	 */
	public void smallconfLoadSubscribers(PubsubSmallconf smallconf) {

		if(!smallconf.getSubscribersMap().isEmpty()) {
			return;
		}
		smallconf.setSubscribersMap(PubsubPersistenceManager.getSmallconfSubscribers(smallconf, service));
	}

	/**
	 * create a new smallconf subscriber object 
	 * 
	 * @param smallconf
	 * @param subscriberJID
	 * @param nsqos
	 * @param isqos
	 * @return
	 */
	public PubsubSubscriber smallconfSubscriberNew(PubsubSmallconf smallconf,
			String subscriberJID, PubsubQOS nsqos, PubsubQOS isqos) {

		PubsubSubscriber subscriber = new PubsubSubscriber();
		subscriber.setJID(subscriberJID);
		subscriber.setSmallconf(smallconf);
		
		if ((nsqos.and(PubsubQOS.ncqos_ROLE)).equals(PubsubQOS.ncqos_ROLE_CREATOR)) {
			
			subscriber.setState(PubsubEnum.state_SPONSOR);
		}
		else {
			
			subscriber.setState(PubsubEnum.state_ATTENDER);
		}
		subscriber.setStatus("offline");
		subscriber.setRole(nsqos.and(PubsubQOS.ncqos_ROLE));
		subscriber.setNsqos(nsqos);
		subscriber.setIsqos(isqos);
		if (!(nsqos.and(PubsubQOS.ncqos_WATCHER).isZero())) {
			
			subscriber.setWatcher(1);
		}
		return subscriber;
	}
	
	/**
	 * a subscriber leave or exit and notify the smallconf's subscribers
	 * 
	 * @param jid
	 * @param status
	 */
	public void smallconfUpdateParticipantOffline(String jid, String status) {
		
		ArrayList<PubsubSubNode> subSmallconfsList = PubsubPersistenceManager.getSubSmallconfs(jid);
		if(subSmallconfsList.isEmpty()) {
			Log.info("subNodesList is empty");
		}
		Iterator<PubsubSubNode> iter = subSmallconfsList.iterator();
		while(iter.hasNext()) {
			
			PubsubSubNode subSmallconf = iter.next();
			
			String smallconfID = subSmallconf.getJID();
			PubsubSmallconf smallconf = service.getSmallconfByJID(smallconfID);
			if(smallconf == null) {
				return;
			}
			if( smallconf.getSubscribersMap().isEmpty() ) {
				smallconfLoadSubscribers(smallconf);
			}
			PubsubSubscriber subscriber = smallconf.getSubscribersMap().get(jid);
			if(subscriber == null) {
				continue;
			}
			subscriber.setStatus(status);
			
			HashMap<String, PubsubSubscriber> subscriberMap = smallconf.getSubscribersMap();
			Set<String> keySet = subscriberMap.keySet();
			Iterator<String> iter1 = keySet.iterator();
			while(iter1.hasNext()) {
				
				PubsubSubscriber subscriber1 = subscriberMap.get(iter1.next());
				if(subscriber1.getStatus().equals("offline")) {
					continue;
				}
				
				IQ notifyIQ = new IQ(IQ.Type.result);
				notifyIQ.setTo(subscriber1.getJID()+"/jclient");
				Element elemPubsub = DocumentHelper.createElement("pubsub");
				elemPubsub.addNamespace("", PubsubService.PUBSUB_NAMESPACE);
				Element elemParticipant = DocumentHelper.createElement("scparticipant");
				elemParticipant.addAttribute("smallconf", subscriber.getSmallconf().getID());
				elemParticipant.addAttribute("jid", jid);
				elemParticipant.addAttribute("status", status);
				if(subscriber.getState().equals(PubsubEnum.state_SPONSOR)) {
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
				else if(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).equals(PubsubQOS.ncqos_ROLE_OWNER)) {
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
				
				service.getPacketRouter().route(notifyIQ);
				//System.out.println("<***************reply*****************>"+notifyIQ.toString());
			}
			
		}
	}
	
	
	/**
	 * update the the subscriber's status and notify the smallconf's subscribers
	 * 
	 * @param jid
	 * @param smallconf
	 * @param status
	 */
	public void smallconfUpdateParticipant(String jid, PubsubSmallconf smallconf, String status) {
		
//		String status = "online"; 
		if( smallconf.getSubscribersMap().isEmpty() ) {
			Log.info("here should not be empty!!");
			smallconfLoadSubscribers(smallconf);
		}
		PubsubSubscriber subscriber = smallconf.getSubscribersMap().get(jid);
		if(subscriber == null) {
			return;
		}
		subscriber.setStatus(status);
		
		HashMap<String, PubsubSubscriber> subscriberMap = smallconf.getSubscribersMap();
		Set<String> keySet = subscriberMap.keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext()) {
			
			PubsubSubscriber subscriber1 = subscriberMap.get(iter.next());
			if(subscriber1.getStatus().equals("offline")) {
				continue;
			}
			
			IQ notifyIQ = new IQ(IQ.Type.result);
			notifyIQ.setTo(subscriber1.getJID()+"/jclient");
			Element elemPubsub = DocumentHelper.createElement("pubsub");
			elemPubsub.addNamespace("", PubsubService.PUBSUB_NAMESPACE);
			Element elemParticipant = DocumentHelper.createElement("scparticipant");
			elemParticipant.addAttribute("smallconf", subscriber.getSmallconf().getID());
			elemParticipant.addAttribute("jid", jid);
			elemParticipant.addAttribute("status", status);
			if(subscriber.getState().equals(PubsubEnum.state_SPONSOR)) {
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
			else if(subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).equals(PubsubQOS.ncqos_ROLE_OWNER)) {
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
			
			service.getPacketRouter().route(notifyIQ);
			//System.out.println("<***************reply*****************>"+notifyIQ.toString());
		}
	}
	
	/**
	 * load the node's subscribers
	 * 
	 * @param node
	 */
	public void nodeLoadSubscribers(PubsubNode node) {
		
		if(!node.getSubscribersMap().isEmpty()) {
			return;
		}
		node.setSubscribersMap(PubsubPersistenceManager.getNodeSubscribers(node, service));
	}
	
	
	/**
	 * update the subscriber's state
	 * 
	 * @param subscriber
	 * @param state
	 */
	public void subscriberUpdateState(PubsubSubscriber subscriber, PubsubEnum state) {
		
		if(!subscriber.getNode().getType().equals("group")) {
			return;
		}
		if(state == null || state == subscriber.getState()) {
			return;
		}
		subscriber.setState(state);
	}
	
	/**
	 * update the subscriber's state
	 * 
	 * @param subscriber
	 * @param state
	 */
	public void smallconfSubscriberUpdateState(PubsubSubscriber subscriber, PubsubEnum state) {
		
		if(!subscriber.getSmallconf().getType().equals("smallconf")) {
			return;
		}
		if(state == null || state == subscriber.getState()) {
			return;
		}
		subscriber.setState(state);
	}
	
	/**
	 * update the subscriber's presence in node
	 * 
	 * @param subscriber
	 * @param status
	 */
	public void subscriberUpdatePresence(PubsubSubscriber subscriber, String status) {
		
		if(!subscriber.getNode().getType().equals("group")) {
			return;
		}
		if(status == null || status == subscriber.getStatus()) {
			return;
		}
		subscriber.setStatus(status);
	}
	
	/**
	 * update the subscriber's presence in smallconf
	 * 
	 * @param subscriber
	 * @param status
	 */
	public void smallconfSubscriberUpdatePresence(PubsubSubscriber subscriber, String status) {
		
		if(!subscriber.getSmallconf().getType().equals("smallconf")) {
			return;
		}
		if(status == null || status == subscriber.getStatus()) {
			return;
		}
		subscriber.setStatus(status);
	}
	
	/**
	 * 
	 * 
	 * @param role
	 * @param roleQOS
	 * @return
	 */
	public int affiliationStrToEnum(String role, PubsubQOS roleQOS) {
		
		if(role == null) {
			return -1;
		}
		if(role.equals("creator")) {
			roleQOS = PubsubQOS.ncqos_ROLE_CREATOR;
		}
		else if(role.equals("owner")) {
			roleQOS = PubsubQOS.ncqos_ROLE_OWNER;
		}
		else if(role.equals("outcast")) {
			roleQOS = PubsubQOS.ncqos_ROLE_OUTCASE;
		}
		else if(role.equals("member")) {
			roleQOS = PubsubQOS.ncqos_ROLE_MEMBER;
		}
		else {
			return -1;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param subscriber
	 * @param roleQOS
	 */
	public void subscriberUpdateAffiliation(PubsubSubscriber subscriber, PubsubQOS roleQOS) {
		
		PubsubQOS nsqos = (subscriber.getNsqos().and((PubsubQOS.ncqos_ROLE.not()))).or(roleQOS);
		subscriber.setNsqos(nsqos);
		PubsubPersistenceManager.subscriptionsUpdate(subscriber);
	}
	
	/**
	 * 
	 * @param subscriber
	 * @param roleQOS
	 */
	public void smallconfSubscriberUpdateAffiliation(PubsubSubscriber subscriber, PubsubQOS roleQOS) {
		
		PubsubQOS nsqos = (subscriber.getNsqos().and((PubsubQOS.ncqos_ROLE.not()))).or(roleQOS);
		subscriber.setNsqos(nsqos);
		PubsubPersistenceManager.smallconfSubscriptionsUpdate(subscriber);
	}
	/**
	 * 
	 * @param subscriber
	 */
	public void subscriberUpdateWatcher(PubsubSubscriber subscriber) {
		
		if(subscriber.getWatcher() != 0) {
			return;
		}
		PubsubNode node = subscriber.getNode();
		Set<String> keySet = node.getSubscribersMap().keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext()) {
			
			PubsubSubscriber sub = node.getSubscribersMap().get(iter.next());
			if(sub.getWatcher() != 0 
					|| sub.getJID().equals(subscriber.getJID())) {
				
				if(sub.getJID().equals(subscriber.getJID())) {
					sub.setNsqos( sub.getNsqos().or(PubsubQOS.ncqos_WATCHER));
					sub.setWatcher(1);
				}
				else {
					sub.setNsqos( sub.getNsqos().or(PubsubQOS.ncqos_WATCHER.not()));
					sub.setWatcher(0);
				}
				PubsubPersistenceManager.subscriptionsUpdate(sub);
			}
		}
	}
	/**
	 * 
	 * @param subscriber
	 */
	public void smallconfSubscriberUpdateWatcher(PubsubSubscriber subscriber) {
		
		if(subscriber.getWatcher() != 0) {
			return;
		}
		PubsubSmallconf smallconf = subscriber.getSmallconf();
		Set<String> keySet = smallconf.getSubscribersMap().keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext()) {
			
			PubsubSubscriber sub = smallconf.getSubscribersMap().get(iter.next());
			if(sub.getWatcher() != 0 
					|| sub.getJID().equals(subscriber.getJID())) {
				
				if(sub.getJID().equals(subscriber.getJID())) {
					
					sub.setNsqos( sub.getNsqos().or(PubsubQOS.ncqos_WATCHER));
					sub.setWatcher(1);
				}
				else {
					sub.setNsqos( sub.getNsqos().or(PubsubQOS.ncqos_WATCHER.not()));
					sub.setWatcher(0);
				}
				PubsubPersistenceManager.smallconfSubscriptionsUpdate(sub);
			}
		}
	}
	/**
	 * update the subscriber's info
	 * 
	 * @param pubsubPacket
	 */
	public void nodeSubscriberUpdate(PubsubPacket pubsubPacket) {

		
		PubsubNode node = pubsubPacket.getNode();
		if (!node.getType().equals("group")) {
			return;
		}
		
		Element elemParticipant = pubsubPacket.getIQ().getChildElement().element("participant");
		String JID = elemParticipant.attributeValue("jid");
		nodeLoadSubscribers(node);
		PubsubSubscriber subscriber = node.getSubscribersMap().get(JID);
		
		if (subscriber == null) {
			Log.info("can't find this subscriber");
			return;
		}

		String state = elemParticipant.attributeValue("state");

		if (state !=null) {

			PubsubEnum stateEnum = PubsubEnum.state_ATTENDER;
			if (state.equals("ai")) {
				stateEnum = PubsubEnum.state_AI;
			} else if (state.equals("ao")) {
				stateEnum = PubsubEnum.state_AO;
			} else if (state.equals("vi")) {
				stateEnum = PubsubEnum.state_VI;
			} else if (state.equals("vo")) {
				stateEnum = PubsubEnum.state_VO;
			} else if (state.equals("aivi")) {
				stateEnum = PubsubEnum.state_AIVI;
			} else if (state.equals("aivo")) {
				stateEnum = PubsubEnum.state_AIVO;
			} else if (state.equals("aovi")) {
				stateEnum = PubsubEnum.state_AOVI;
			} else if (state.equals("aovo")) {
				stateEnum = PubsubEnum.state_AOVO;
			} else if (state.equals("attender")) {
				stateEnum = PubsubEnum.state_ATTENDER;
			} else if (subscriber.getRole() == PubsubQOS.ncqos_ROLE_CREATOR) {
				stateEnum = PubsubEnum.state_SPONSOR;
			} else
				stateEnum = PubsubEnum.state_ATTENDER;

			subscriberUpdateState(subscriber, stateEnum);
		}

		String status = elemParticipant.attributeValue("status");
		if (status != null) {

			if (status.equals("offline")) {

				subscriber.setState(PubsubEnum.state_ATTENDER);

				if (subscriber.getRole() == PubsubQOS.ncqos_ROLE_CREATOR) {

					subscriber.setState(PubsubEnum.state_SPONSOR);
				}
			}
			if(subscriber.getStatus().equals("online")) {
				
				conferenceUpdateParticipantOffline(JID, "offline");
			}
			subscriberUpdatePresence(subscriber, status);
		}

		String role = elemParticipant.attributeValue("role");
		if (role != null) {
			PubsubQOS roleQOS = null;
			if (affiliationStrToEnum(role, roleQOS) != 0) {
				return;
			}
			PubsubSubscriber fromSubscriber = node.getSubscribersMap().get(pubsubPacket.getIQ().getFrom().toBareJID());
			
			if (fromSubscriber == null) {
				return;
			}
			if ((subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE) == PubsubQOS.ncqos_ROLE_CREATOR)
					|| (fromSubscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).compare(PubsubQOS.ncqos_ROLE_OWNER)) < 0) {

				return;
			}

			subscriberUpdateAffiliation(subscriber, roleQOS);
		}

		String watcher = elemParticipant.attributeValue("watcher");
		if (watcher != null) {

			PubsubSubscriber fromSubscriber = node.getSubscribersMap().get(
					pubsubPacket.getIQ().getFrom().toBareJID());
			if (fromSubscriber == null) {
				return;
			}
			if ((fromSubscriber.getWatcher() == 0)
					&& ((fromSubscriber.getNsqos().and(PubsubQOS.ncqos_ROLE))
							.compare(PubsubQOS.ncqos_ROLE_OWNER)) < 0) {
				//
			} else {
				subscriberUpdateWatcher(fromSubscriber);
			}
		}

		String encrypt = elemParticipant.attributeValue("encrypt");
		if (encrypt != null) {
			subscriber.setEncrypt(1);
		} else {
			subscriber.setEncrypt(2);
		}
		if(status.equals("online")) {
			conferenceUpdateParticipant(JID, node, "online");
		} else {
			conferenceUpdateParticipantOffline(JID, status);
		}
	}

	/**
	 * update the subscriber's info
	 * 
	 * @param pubsubPacket
	 */
	public void smallconfSubscriberUpdate(PubsubPacket pubsubPacket) {

		PubsubSmallconf smallconf = pubsubPacket.getSmallconf();
		if (!smallconf.getType().equals("smallconf")) {
			return;
		}
		
		Element elemParticipant = pubsubPacket.getIQ().getChildElement().element("scparticipant");
		String JID = elemParticipant.attributeValue("jid");
		smallconfLoadSubscribers(smallconf);
		PubsubSubscriber subscriber = smallconf.getSubscribersMap().get(JID);
		
		if (subscriber == null) {
			Log.info("can't find this subscriber");
			return;
		}

		String state = elemParticipant.attributeValue("state");

		if (state !=null) {

			PubsubEnum stateEnum = PubsubEnum.state_ATTENDER;
			if (state.equals("ai")) {
				stateEnum = PubsubEnum.state_AI;
			} else if (state.equals("ao")) {
				stateEnum = PubsubEnum.state_AO;
			} else if (state.equals("vi")) {
				stateEnum = PubsubEnum.state_VI;
			} else if (state.equals("vo")) {
				stateEnum = PubsubEnum.state_VO;
			} else if (state.equals("aivi")) {
				stateEnum = PubsubEnum.state_AIVI;
			} else if (state.equals("aivo")) {
				stateEnum = PubsubEnum.state_AIVO;
			} else if (state.equals("aovi")) {
				stateEnum = PubsubEnum.state_AOVI;
			} else if (state.equals("aovo")) {
				stateEnum = PubsubEnum.state_AOVO;
			} else if (state.equals("attender")) {
				stateEnum = PubsubEnum.state_ATTENDER;
			} else if (subscriber.getRole() == PubsubQOS.ncqos_ROLE_CREATOR) {
				stateEnum = PubsubEnum.state_SPONSOR;
			} else
				stateEnum = PubsubEnum.state_ATTENDER;

			smallconfSubscriberUpdateState(subscriber, stateEnum);
		}

		String status = elemParticipant.attributeValue("status");
		if (status != null) {

			if (status.equals("offline")) {

				subscriber.setState(PubsubEnum.state_ATTENDER);

				if (subscriber.getRole() == PubsubQOS.ncqos_ROLE_CREATOR) {

					subscriber.setState(PubsubEnum.state_SPONSOR);
				}
			}
			
			if(subscriber.getStatus().equals("online")) {
				
				smallconfUpdateParticipantOffline(subscriber.getJID(), "offline");
			}
			smallconfSubscriberUpdatePresence(subscriber, status);
		}

		String role = elemParticipant.attributeValue("role");
		if (role != null) {
			PubsubQOS roleQOS = null;
			if (affiliationStrToEnum(role, roleQOS) != 0) {
				return;
			}
			PubsubSubscriber fromSubscriber = smallconf.getSubscribersMap().get(pubsubPacket.getIQ().getFrom().toBareJID());
			
			if (fromSubscriber == null) {
				return;
			}
			if ((subscriber.getNsqos().and(PubsubQOS.ncqos_ROLE) == PubsubQOS.ncqos_ROLE_CREATOR)
					|| (fromSubscriber.getNsqos().and(PubsubQOS.ncqos_ROLE).compare(PubsubQOS.ncqos_ROLE_OWNER)) < 0) {

				return;
			}

			smallconfSubscriberUpdateAffiliation(subscriber, roleQOS);
		}

		String watcher = elemParticipant.attributeValue("watcher");
		if (watcher != null) {

			PubsubSubscriber fromSubscriber = smallconf.getSubscribersMap().get(pubsubPacket.getIQ().getFrom().toBareJID());
			if (fromSubscriber == null) {
				return;
			}
			if ((fromSubscriber.getWatcher() == 0)
					&& ((fromSubscriber.getNsqos().and(PubsubQOS.ncqos_ROLE)).compare(PubsubQOS.ncqos_ROLE_OWNER)) < 0) {
				//
			} else {
				smallconfSubscriberUpdateWatcher(fromSubscriber);
			}
		}

		String encrypt = elemParticipant.attributeValue("encrypt");
		if (encrypt != null) {
			subscriber.setEncrypt(1);
		} else {
			subscriber.setEncrypt(2);
		}

		if(subscriber.getStatus().equals("online")) {
			smallconfUpdateParticipant(subscriber.getJID(), smallconf, "online");
		}else {
			smallconfUpdateParticipantOffline(subscriber.getJID(), subscriber.getStatus());
		}
	}
	
	/**
	 * remove a subscriber out of node
	 * 
	 * @param node
	 * @param subscriber
	 */
	public void nodeRemoveSubscriber(PubsubNode node, PubsubSubscriber subscriber) {
		
		subscriberUpdatePresence(subscriber, "exit");
		conferenceUpdateParticipant(subscriber.getJID(), node, subscriber.getStatus());
		
		PubsubPersistenceManager.subscriptionsDelete(node.getID(), subscriber.getJID());
		PubsubPersistenceManager.userSubscriptionsDelete(subscriber.getJID(), node.getID());
		node.getSubscribersMap().remove(subscriber.getJID());
	}
	
	/**
	 * remove a subscriber out of smallconf
	 * 
	 * @param smallconf
	 * @param subscriber
	 */
	public void smallconfRemoveSubscriber(PubsubSmallconf smallconf, PubsubSubscriber subscriber) {
		
		smallconfSubscriberUpdatePresence(subscriber, "exit");
		smallconfUpdateParticipant(subscriber.getJID(), smallconf, subscriber.getStatus());
		PubsubPersistenceManager.smallconfSubscriptionsDelete(smallconf.getID(), subscriber.getJID());
		PubsubPersistenceManager.userSubscriptionsDelete(subscriber.getJID(), smallconf.getID());
		smallconf.getSubscribersMap().remove(subscriber.getJID());
	}
}
