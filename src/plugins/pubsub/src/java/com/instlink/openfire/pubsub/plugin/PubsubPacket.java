package com.instlink.openfire.pubsub.plugin;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;


/**
 * the pubsub packet received from client 
 * 
 * @author LC
 *
 */
public class PubsubPacket {

	private static final Logger Log = LoggerFactory.getLogger(PubsubPacket.class);

	private IQ iq;
	private PubsubEnum event = PubsubEnum.event_NONE;
	private PubsubNode node = null;
	private PubsubSmallconf smallconf = null; 
	private String data = null;
	private PubsubService service = null;

	public PubsubPacket(PubsubService service, IQ iq) {

//		Log.info("packet parse start");

		this.service = service;
		this.iq = iq;

		// is this packet contain pubsub element
		Element element = iq.getChildElement();
		if (element == null || !element.getName().equals("pubsub")) {
			event = PubsubEnum.event_ERROR_BAD_REQUEST;
			////System.out.println("error");
			return;
		}

		// packet - create
		if (element.element("timecheck") != null) {
			
			String userJID = iq.getFrom().toBareJID();
			long time = System.currentTimeMillis();
			synchronized (PubsubService.timeMap) {
				PubsubService.timeMap.put(userJID, time);
			}
//			event = PubsubEnum.event_TIME_CHECK;
			return;
			
		} else if(element.element("leave") != null) {
			
//			System.out.println("<-------leave--------->" + iq.toString());
			String jid = iq.getFrom().toBareJID();
			service.getMemberManager().conferenceUpdateParticipantOffline(jid, "offline");
			service.getMemberManager().smallconfUpdateParticipantOffline(jid, "offline");
			synchronized (PubsubService.timeMap) {
				PubsubService.timeMap.remove(jid);
			}
			return;
			
		} else if (element.element("create") != null) {
			////System.out.println("<-------create--------->" + iq.toString());
			try {
				Element elem = element.element("create");
				event = PubsubEnum.event_CREATE;
				node = new PubsubNode();
				// get node creator
				node.setCreator(iq.getFrom().toBareJID());

				// get node type
				String nodeType = elem.attributeValue("type");
				node.setType(nodeType);

				// personal info space's id is creator's jid
				if (nodeType.equals("personal")) {
					node.setID(node.getCreator());
				}

				// get node name
				node.setName(elem.elementText("name"));

				// get node description
				if (elem.element("description") != null) {
					node.setDescription(elem.elementText("description"));
				}

				// get configuration
				Element nodeConfig = elem.element("configure");
				if (nodeConfig == null) {
					throw new Exception();
				}

				// get parent
				if (nodeConfig.element("collection") != null) {

					PubsubNode nodeParent = service.getNodeByJID(nodeConfig.elementText("collection"), false);
					if (nodeParent != null) {
						node.setParent(nodeParent);
					} else {
						// parent node not found
						event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
						throw new Exception();
					}
				} else {
					// default parent: root@realm
					if (this.service.getDomain() != null) {
						String root = String.format("root@%s",this.service.getDomain());
						PubsubNode nodeParent = this.service.getNodeByJID(root, false);
						if (nodeParent != null) {
							node.setParent(nodeParent);
						}
					}
				}
				// get nqos
				PubsubQOS nqos = new PubsubQOS(nodeConfig.elementText("nqos"));
				node.setNqos(nqos);
				node.setNgqos(nqos.and(PubsubQOS.ngqos_NODE_GLOBLE_QOS));
				node.setNcqos(nqos.and(PubsubQOS.ncqos_NODE_CUSTOM_QOS));

				// get icqos
				node.setIcqos(new PubsubQOS(nodeConfig.elementText("icqos")));

				// get password
				if (nodeConfig.element("password") != null) {
					node.setPassword(nodeConfig.elementText("password"));
				}

				// get deadline
				if (nodeConfig.element("deadline") != null) {
					node.setDeadline(Long.parseLong(
							nodeConfig.elementText("deadline"), 16));
				}

				// get deadline
				if (nodeConfig.element("item_lifecycle") != null) {
					node.setItemLifecycle(Long.parseLong(
							nodeConfig.elementText("item_lifecycle"), 16));
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - create");
				return;
			}

		} else if (element.element("getsubnodes") != null) {
			// get subscribed nodes
			////System.out.println("<-------getsubnodes--------->" + iq.toString());
			try {
				Element elem = element.element("getsubnodes");
				event = PubsubEnum.event_GET_SUB_NODES;
				data = elem.attributeValue("type", "normal");
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getsubnodes");
				return;
			}

		} else if (element.element("getsubsmallconfs") != null) {
			////System.out.println("<-------getsubsmallconfs--------->"	+ iq.toString());
			
			try{
				Element elem = element.element("getsubsmallconfs");
				event = PubsubEnum.event_GET_SUB_SMALLCONFS;
				data = elem.attributeValue("type", "normal");
				return;
				
			} catch(Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getsmallconfs");
				return;
			}
			
		} else if (element.element("getnode") != null) {
			// get specific node information
			////System.out.println("<-------getnode--------->" + iq.toString());
			try {
				Element elem = element.element("getnode");
				event = PubsubEnum.event_NODEINFO;

				// get nodeid and node type
				String nodeID = elem.attributeValue("node");
				String type = elem.attributeValue("type");
				if (nodeID == null || type == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				// get node info
				if (type.equals("group")) {
					node = service.getNodeByJID(nodeID, false);
				} else if (type.equals("personal")) {
					node = service.getNodeByJID(nodeID, true);
				}

				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getnode");
				return;
			}

		} else if (element.element("sccreate") != null) {
			////System.out.println("<-------sccreate--------->" + iq.toString());
			try {
				Element elem = element.element("sccreate");
				
				smallconf = new PubsubSmallconf();
				// get smallconf creator
				smallconf.setCreator(iq.getFrom().toBareJID());

				// get smallconf type
				String smallconfType = elem.attributeValue("type");
				smallconf.setType(smallconfType);

				// get smallconf name
				smallconf.setName(elem.elementText("name"));

				// get node description
				if (elem.element("description") != null) {
					smallconf.setDescription(elem.elementText("description"));
				}

				// get configuration
				Element smallconfConfig = elem.element("configure");
				if (smallconfConfig == null) {
					throw new Exception();
				}

				// get parent
				if (smallconfConfig.element("collection") != null) {

					PubsubSmallconf smallconfParent = service.getSmallconfByJID(smallconfConfig.elementText("collection"));
					if (smallconfParent != null) {
						smallconf.setParent(smallconfParent);
					} else {
						// parent not found
						event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
						throw new Exception();
					}
				} else {
					// default parent: smallconfroot@realm
					if (this.service.getDomain() != null) {
						String root = String.format("smallconfroot@%s",this.service.getDomain());
						PubsubSmallconf smallconfParent = this.service.getSmallconfByJID(root);
						if (smallconfParent != null) {
							smallconf.setParent(smallconfParent);
						}
					}
				}
				// get nqos
				PubsubQOS nqos = new PubsubQOS(smallconfConfig.elementText("nqos"));
				smallconf.setNqos(nqos);
				smallconf.setNgqos(nqos.and(PubsubQOS.ngqos_NODE_GLOBLE_QOS));
				smallconf.setNcqos(nqos.and(PubsubQOS.ncqos_NODE_CUSTOM_QOS));

				// get icqos
				smallconf.setIcqos(new PubsubQOS(smallconfConfig.elementText("icqos")));

				// get password
				if (smallconfConfig.element("password") != null) {
					node.setPassword(smallconfConfig.elementText("password"));
				}

				event = PubsubEnum.event_CREATE_SMALLCONF;
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - sccreate");
				return;
			}
			
		} else if (element.element("getnodes") != null) {
			////System.out.println("<-------getnodes--------->" + iq.toString());
			// get all nodes
			try {
				event = PubsubEnum.event_GET_ALL_NODES;
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getnodes");
				return;
			}
			
		} else if (element.element("getsmallconfs") != null) {
			////System.out.println("<-------getsmallconfs--------->" + iq.toString());
			try {
				event = PubsubEnum.event_GET_ALL_SMALLCONFS;
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getsmallconfs");
				return;
			}
			
		} else if (element.element("getsmallconf") != null) {
			////System.out.println("<-------getsmallconf--------->" + iq.toString());
			
			try {
				Element elem = element.element("getsmallconf");
				event = PubsubEnum.event_SMALLCONFINFO;

				// get smallconfid and node type
				String smallconfID = elem.attributeValue("smallconf");
				if (smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				// get smallconf info
				smallconf = service.getSmallconfByJID(smallconfID);

				if (smallconf == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getsmallconf");
				return;
			}
			
		} else if (element.element("subscribe") != null) {
			// subscribe a node
			//System.out.println("<-------subscribe--------->" + iq.toString());
			try {
				Element elem = element.element("subscribe");
				event = PubsubEnum.event_SUB;

				String nodeID = elem.attributeValue("node");
				if (nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				node = service.getNodeByJID(nodeID, false);
				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - subscribe");
				return;
			}
			
		} else if (element.element("subsmallconf") != null) {
			////System.out.println("<-------subsmallconf--------->" + iq.toString());
			try {
				Element elem = element.element("subsmallconf");
				event = PubsubEnum.event_SUB_SMALLCONF;

				String smallconfID = elem.attributeValue("smallconf");
				if (smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				smallconf = service.getSmallconfByJID(smallconfID);
				if (smallconf == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				else {
					String verify = elem.attributeValue("verify");
					if(verify != null) {
						smallconf.setVerify(verify);
					}
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - subsmallconf");
				return;
			}
			
		} else if (element.element("subscribed") != null) {
			////System.out.println("<-------subscribed--------->" + iq.toString());
			// node creator accept user's subscribe request
			try {
				Element elem = element.element("subscribed");
				event = PubsubEnum.event_SUBED;
				
				String nodeID = elem.attributeValue("node");
				if (nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				node = service.getNodeByJID(nodeID, false);
				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				
				String JID = elem.attributeValue("jid");
				if (JID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				data = JID;
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - subscribed");
				return;
			}
			
		} else if (element.element("scsubscribed") != null) {
			//System.out.println("<-------scsubscribed--------->" + iq.toString());
			try {
				Element elem = element.element("scsubscribed");
				event = PubsubEnum.event_SUBED_SMALLCONF;
				
				String smallconfID = elem.attributeValue("smallconf");
				if (smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				smallconf = service.getSmallconfByJID(smallconfID);
				if (smallconf == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				
				String JID = elem.attributeValue("jid");
				if (JID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				data = JID;
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - scsubscribed");
				return;
			}
			
		} else if (element.element("participants") != null) {
			// get node's other subscribers
			////System.out.println("<-------participants--------->"	+ iq.toString());
			try {
				Element elem = element.element("participants");
				event = PubsubEnum.event_GET_SUBSCRIBERS;

				String nodeID = elem.attributeValue("node");
				if (nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				node = service.getNodeByJID(nodeID, false);
				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - participants");
				return;
			}
		} else if (element.element("scparticipants") != null) {
			//System.out.println("<-------scparticipants--------->" + iq.toString());
			
			try {
				Element elem = element.element("scparticipants");
				event = PubsubEnum.event_GET_SMALLCONF_SUBSCRIBERS;

				String smallconfID = elem.attributeValue("smallconf");
				if (smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				smallconf = service.getSmallconfByJID(smallconfID);
				if (smallconf == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - scparticipants");
				return;
			}
			
		} else if (element.element("participant") != null) {
			// subscriber's state update
			////System.out.println("<-------participant--------->" + iq.toString());
			try {
				Element elem = element.element("participant");
				event = PubsubEnum.event_SUBER_UPT;

				String nodeID = elem.attributeValue("node");
				if (nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				node = service.getNodeByJID(nodeID, false);
				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}

				// store element index string
				data = "participant";
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - participant");
				return;
			}
		} else if (element.element("scparticipant") != null) {
			//System.out.println("<-------scparticipant--------->" + iq.toString());
			try {
				Element elem = element.element("scparticipant");
				event = PubsubEnum.event_SCSUBER_UPT;

				String smallconfID = elem.attributeValue("smallconf");
				if (smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				smallconf = service.getSmallconfByJID(smallconfID);
				if (smallconf == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}

				// store element index string
				data = "scparticipant";
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - scparticipant");
				return;
			}
			
		} else if (element.element("publish") != null) {
			// publish item
			//System.out.println("<-------publish--------->" + iq.toString());
			try {
				Element elem = element.element("publish");

				String nodeID = elem.attributeValue("node");
				String type = elem.attributeValue("type");
				if (nodeID == null || type == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				if (type.equals("personal")) {

					event = PubsubEnum.event_PERSONAL_PUBLISH;
					node = service.getNodeByJID(nodeID, true);
					if (node == null) {
						event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
						throw new Exception();
					}
				} else if (type.equals("group")) {
					node = service.getNodeByJID(nodeID, false);
					if (node == null) {
						event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
						throw new Exception();
					}
					event = PubsubEnum.event_PUBLISH;
				}
				else if(type.equals("smallconf")) {
					smallconf = service.getSmallconfByJID(nodeID);
					if(smallconf == null) {
						event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
						throw new Exception();
					}
					event = PubsubEnum.event_SCPUBLISH;
				}
				else {
					throw new Exception();
				}
				data = "publish";
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - publish");
				return;
			}
		} else if (element.element("gethistory") != null) {
			// get history items
			//System.out.println("<-------gethistory--------->" + iq.toString());
			try {
				Element elem = element.element("gethistory");
				event = PubsubEnum.event_GET_HISTORY;

				String nodeID = elem.attributeValue("node");
				if (nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				data = elem.attributeValue("type", "normal");

				node = service.getNodeByJID(nodeID, false);
				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}

				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - gethistory");
				return;
			}
		} else if (element.element("subscription") != null) {
			////System.out.println("<-------subscription--------->" + iq.toString());
			
			event = PubsubEnum.event_SUBSCRIPTION;
			return;
			
		} else if (element.element("scsubscription") != null) {
			//System.out.println("<-------scsubscription--------->" + iq.toString());
			
			event = PubsubEnum.event_SCSUBSCRIPTION;
			return;
			
		} else if (element.element("delete") != null) {
			////System.out.println("<-------delete--------->" + iq.toString());
			
			try {
				Element elem = element.element("delete");
				event = PubsubEnum.event_DELETE;

				String nodeID = elem.attributeValue("node");
				if (nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}

				node = service.getNodeByJID(nodeID, false);
				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}

				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - delete");
				return;
			}
			
		} else if (element.element("scdelete") != null) {
			////System.out.println("<-------scdelete--------->" + iq.toString());
			
			try {
				Element elem = element.element("scdelete");
				event = PubsubEnum.event_DELETE_SMALLCONF;
	
				String smallconfID = elem.attributeValue("smallconf");
				if (smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
	
				smallconf = service.getSmallconfByJID(smallconfID);
				if (smallconf == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
	
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - scdelete");
				return;
			}
			
		} else if (element.element("unsubscribe") != null) {
			////System.out.println("<-------unsubscribe--------->" + iq.toString());
			try {
				Element elem = element.element("unsubscribe");
				event = PubsubEnum.event_UNSUB;
	
				String nodeID = elem.attributeValue("node");
				if (nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
	
				node = service.getNodeByJID(nodeID, false);
				if (node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				
				data = elem.attributeValue("jid");
				if (data == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - unsubscribe");
				return;
			}
			
		} else if (element.element("scunsubscribe") != null) {
			//System.out.println("<-------scunsubscribe--------->" + iq.toString());
			try {
				Element elem = element.element("scunsubscribe");
				event = PubsubEnum.event_UNSUB_SMALLCONF;
	
				String smallconfID = elem.attributeValue("smallconf");
				if (smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
	
				smallconf = service.getSmallconfByJID(smallconfID);
				if (smallconf == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				
				data = elem.attributeValue("jid");
				if (data == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				return;
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - unsubscribe");
				return;
			}
			
		} else if (element.element("getchild") != null) {
			////System.out.println("<-------getchild--------->" + iq.toString());
			try {
				event = PubsubEnum.event_GETCHILD;
				Element elem = element.element("getchild");
				String nodeID = elem.attributeValue("node");
				if(nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				node = service.getNodeByJID(nodeID, false);
				if(node == null) {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
				
			} catch (Exception e) {
				// if event is not error, set error
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getchild");
				return;
			}
			
		} else if (element.element("getitemdetail") != null) {
			////System.out.println("<-------getitemdetail--------->" + iq.toString());
			try {
				Element elem = element.element("getitemdetail");
				String itemID = elem.attributeValue("itemid");
				if(itemID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				data = itemID;
				String filter = elem.attributeValue("filter");
				if(filter.equals("2")) {
					// no more this here
					event = PubsubEnum.event_GET_KEYWORD_ITEM_DETAIL;
				}
				else if(filter.equals("1")) {
					event = PubsubEnum.event_GET_CONF_ITEM_DETAIL;
				}
				return;
				
			} catch (Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getitemdetail");
				return;
			}
			
		} else if (element.element("searchperitem") != null) {
			////System.out.println("<-------searchperitem--------->" + iq.toString());
			// do not use it any more 
			try{
				event = PubsubEnum.event_SEARCH_PER_ITEM;
				Element elem = element.element("searchperitem");
				String keyword = elem.attributeValue("keyword");
				if(keyword == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				else {
					data = keyword;
				}
				return;
			}catch (Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - searchperitem");
				return;
			}
			
		} else if (element.element("getoffline") != null) {
			////System.out.println("<-------getoffline--------->" + iq.toString());
			try {
				event = PubsubEnum.event_GET_OFFLINE;
				Element elem = element.element("getoffline");
				String nodeID = elem.attributeValue("node");
				if(nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				else {
					data = nodeID;
				}
				return;
				
			} catch (Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - getoffline");
				return;
			}
			
		} else if (element.element("retract") != null) {
			////System.out.println("<-------retract--------->" + iq.toString());
			try {
				Element elem = element.element("retract");
				String nodeID = elem.attributeValue("node");
				if(nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				node = service.getNodeByJID(nodeID, false);
				if(node == null) {
					node = service.getNodeByJID(nodeID, true);
				}
				if(node != null) {
					event = PubsubEnum.event_DELETE_ITEM;
				}
				else {
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
				
			} catch (Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - retract");
				return;
			}
		} else if (element.element("status") != null) {
			////System.out.println("<-------status--------->" + iq.toString());
			try {
				event = PubsubEnum.event_SET_STATUS;
				Element elem = element.element("status");
				String nodeID = elem.attributeValue("node");
				if(nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				node = service.getNodeByJID(nodeID, false);
				if(node == null) {
					
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				return;
				
			} catch (Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - status");
				return;
			}
			
		} else if (element.element("configure") != null) {
			////System.out.println("<-------configure--------->" + iq.toString());
			try {
				Element elem = element.element("configure");
				String nodeID = elem.attributeValue("node");
				if(nodeID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				node = service.getNodeByJID(nodeID, false);
				if(node == null) {
					service.getNodeByJID(nodeID, true);
					data = "personal";
				}
				else {
					data = "group";
				}
				if(node == null) {
					
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				event = PubsubEnum.event_SET_CONFIGURE;
				return;
				
			} catch (Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - configure");
				return;
			}
			
		}  else if (element.element("scconfigure") != null) {
			////System.out.println("<-------scconfigure--------->" + iq.toString());
			try {
				Element elem = element.element("scconfigure");
				String smallconfID = elem.attributeValue("smallconf");
				if(smallconfID == null) {
					event = PubsubEnum.event_ERROR_BAD_REQUEST;
					throw new Exception();
				}
				smallconf = service.getSmallconfByJID(smallconfID);
				if(smallconf == null) {
					
					event = PubsubEnum.event_ERROR_NODE_NOT_EXIST;
					throw new Exception();
				}
				event = PubsubEnum.event_SET_SCCONFIGURE;
				return;
				
			} catch (Exception e) {
				if (event.and(PubsubEnum.event_ERROR).isZero()) {
					event = PubsubEnum.event_ERROR;
				}
				Log.info("packet parse error - scconfigure");
				return;
			}
			
		} else if (element.element("merge") != null) {
			////System.out.println("<-------merge--------->" + iq.toString());
			// not use 
			return;
		} else if (element.element("searchitem") != null) {
			////System.out.println("<-------searchitem--------->" + iq.toString());
			// not use
			return;
		} else if (element.element("getperitemdetail") != null) {
			////System.out.println("<-------getperitemdetail--------->" + iq.toString());
			// not use
//			try {
//				event = PubsubEnum.event_GET_PER_ITEM_DETAIL;
//				Element elem = element.element("getperitemdetail");
//				String itemID = elem.attributeValue("itemid");
//				if(itemID == null) {
//					event = PubsubEnum.event_ERROR_BAD_REQUEST;
//					throw new Exception();
//				}
//				data = itemID;
//				
//				return;
//				
//			} catch (Exception e) {
//				if (event.and(PubsubEnum.event_ERROR).isZero()) {
//					event = PubsubEnum.event_ERROR;
//				}
//				Log.info("packet parse error - getperitemdetail");
//				return;
//			}
			return;
		} else if (element.element("rollcall") != null) {
			
			String to = iq.getTo().toString();
			if(!to.contains("/")) {
				to = to+"/jclient";
			}
			IQ rollcallIQ = iq.createCopy();
			rollcallIQ.setTo(to);
			service.getPacketRouter().route(rollcallIQ);
			////System.out.println("<---------------rollcall--------------->"+rollcallIQ.toString());	
			return;
		
		} else if (element.element("deletefile") != null) {
			String to = iq.getTo().toString();
			if(!to.contains("/")) {
				to = to+"/jclient";
			}
			IQ deletefileIQ = iq.createCopy();
			deletefileIQ.setTo(to);
			service.getPacketRouter().route(deletefileIQ);
			////System.out.println("<---------------deletefile--------------->"+deletefileIQ.toString());	
			return;
			
		} else if (element.element("getfile") != null) {
			String to = iq.getTo().toString();
			if(!to.contains("/")) {
				to = to+"/jclient";
			}
			IQ getfileIQ = iq.createCopy();
			getfileIQ.setTo(to);
			service.getPacketRouter().route(getfileIQ);
			////System.out.println("<---------------getfile--------------->"+getfileIQ.toString());	
			return;
		}
		
//		Log.info("packet parse success");
	}

	public IQ getIQ() {
		return this.iq;
	}

	public PubsubEnum getEvent() {
		return event;
	}

	public void setEvent(PubsubEnum event) {
		this.event = event;
	}

	public PubsubNode getNode() {
		return node;
	}

	public void setNode(PubsubNode node) {
		this.node = node;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public PubsubSmallconf getSmallconf() {
		return smallconf;
	}

	public void setSmallconf(PubsubSmallconf smallconf) {
		this.smallconf = smallconf;
	}
	
}
