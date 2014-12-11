package com.instlink.openfire.pubsub.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the class operate the database of pubsub, all method is static. 
 * 
 * @author LC
 *
 */
public class PubsubPersistenceManager {
	
	private static final Logger Log = LoggerFactory.getLogger(PubsubPersistenceManager.class);
/*
	private static final String LOAD_NODES = 
			"SELECT collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos, deadline, item_lifecycle FROM pubsub_node";
*/
	private static final String GET_MAX_NODE_JID = 
			"SELECT MAX(collectionowner) FROM pubsub_node" ;
	
	private static final String GET_MAX_SMALLCONF_JID = 
			"SELECT MAX(collectionowner) FROM pubsub_smallconf" ;
	
	private static final String GET_NODE_BY_JID = 
			"SELECT collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos, deadline, item_lifecycle FROM pubsub_node " + 
			"WHERE collectionowner=?";
	
	private static final String GET_PERSONAL_NODE_BY_JID = 
			"SELECT collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos, deadline, item_lifecycle FROM pubsub_personal " + 
			"WHERE collectionowner=?";
	
	private static final String GET_SMALLCONF_BY_JID = 
			"SELECT collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos FROM pubsub_smallconf " + 
			"WHERE collectionowner=?";
	
	private static final String ADD_GROUP_NODE = 
			"INSERT INTO pubsub_node (collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos, deadline, item_lifecycle) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String ADD_PERSONAL_NODE = 
			"INSERT INTO pubsub_personal (collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos, deadline, item_lifecycle) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String ADD_SMALLCONF = 
			"INSERT INTO pubsub_smallconf (collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos) VALUES (?,?,?,?,?,?,?,?,?)";
//	private static final String GET_NODE_SUBSCRIPTION_BY_JID = 
//			"SELECT subscriber FROM subscriptions WHERE collectionowner=?";
	
	private static final String NODE_ADD_SUBSCRIBER = 
			"INSERT INTO subscriptions (collectionowner, subscriber, nsqos, isqos, history_range, deadline) " +
			"VALUES (?,?,?,?,?,?)" ;
	
	private static final String SMALLCONF_ADD_SUBSCRIBER = 
			"INSERT INTO smallconf_subscriptions (collectionowner, subscriber, nsqos, isqos) " +
			"VALUES (?,?,?,?)" ;
	
	private static final String USER_SUBSCRIPTIONS_ADD = 
			"INSERT INTO user_subscriptions (collectionowner, node, type) " +
			"VALUES (?,?,?)" ;
	
	private static final String USER_SUBSCRIPTIONS_DELETE = 
			"DELETE FROM user_subscriptions WHERE collectionowner=? AND node=?";
	
	private static final String USER_SUBSCRIPTIONS_NODE_DELETE = 
			"DELETE FROM user_subscriptions WHERE node=?";
	
	private static final String TEST_STRING = 
			"INSERT INTO user_subscriptions (collectionowner, node, type) " +
			"VALUES (?,?,?)" ;
	
	private static final String GET_SUB_NODES=
			"SELECT collectionowner, node, type FROM user_subscriptions WHERE collectionowner=?";
	
	private static final String ADD_SUBJECT = 
			"INSERT INTO subjects (collectionowner, subid, type, name) " +
			"VALUES (?,?,?,?)";
	
	private static final String ADD_PERSONAL_SUBJECT = 
			"INSERT INTO personal_subjects (collectionowner, subid, type, name) " +
			"VALUES (?,?,?,?)" ;
			
	private static final String GET_NODE_SUBSCRIBERS =
			"SELECT collectionowner, subscriber, nsqos, isqos, history_range, deadline FROM subscriptions " +
			"WHERE collectionowner=?";
	
	private static final String GET_SMALLCONF_SUBSCRIBERS =
			"SELECT collectionowner, subscriber, nsqos, isqos FROM smallconf_subscriptions " +
			"WHERE collectionowner=?";
	
	private static final String UPDATE_SUBSCRIPTIONS_NSQOS = 
			"UPDATE subscriptions SET nsqos=? WHERE collectionowner=? AND subscriber=?";
	
	private static final String UPDATE_SMALLCONF_SUBSCRIPTIONS_NSQOS = 
			"UPDATE smallconf_subscriptions SET nsqos=? WHERE collectionowner=? AND subscriber=?";
	
	private static final String GET_ALL_GROUP_NODES = 
			"SELECT collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos, deadline, item_lifecycle FROM pubsub_node";
	
	private static final String GET_ALL_SMALLCONFS = 
			"SELECT collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos FROM pubsub_smallconf";
			
	private static final String GET_GROUP_NODE_SUBJECTS = 
			"SELECT collectionowner, subid, type, name FROM subjects WHERE collectionowner=?";
	
	private static final String GET_PERSONAL_NODE_SUBJECTS = 
			"SELECT collectionowner, subid, type, name FROM personal_subjects WHERE collectionowner=?";
	
	private static final String GET_GROUP_NODE_HISTORY = 
			"SELECT collectionowner, ID, publisher, content, time, status, handler, subid, " +
			"type, name, body, iqos, delay, deadline FROM history WHERE collectionowner=?" ;
	
	private static final String GET_PERSONAL_NODE_HISTORY = 
			"SELECT collectionowner, ID, publisher, content, time, status, handler, subid, " +
			"type, name, body, iqos, delay, deadline FROM personal_history WHERE collectionowner=?" ;
	
	private static final String HISTORY_UPDATE= 
			"UPDATE history SET content=?, time=?, status=?, body=? WHERE ID=?";
	
	private static final String PERSONAL_HISTORY_UPDATE = 
			"UPDATE personal_history SET content=?, time=?, status=?, body=? WHERE ID=?";
	
	private static final String ADD_HISTORY = 
			"INSERT INTO history (collectionowner, ID, publisher, content, time, status, handler, " +
			"subid, type, body) VALUES (?,?,?,?,?,?,?,?,?,?)";
	
	private static final String ADD_PERSONAL_HISTORY = 
			"INSERT INTO personal_history (collectionowner, ID, publisher, content, time, status, handler, " +
			"subid, type, body) VALUES (?,?,?,?,?,?,?,?,?,?)";
	
	private static final String DELETE_GROUP_NODE = 
			"DELETE FROM pubsub_node WHERE collectionowner=?";
	
	private static final String DELETE_PERSONAL_NODE = 
			"DELETE FROM pubsub_personal WHERE collectionowner=?";
	
	private static final String DELETE_SMALLCONF = 
			"DELETE FROM pubsub_smallconf WHERE collectionowner=?";
	
	private static final String SUBSCRIPTIONS_DELETE = 
			"DELETE FROM subscriptions WHERE collectionowner=? AND subscriber=?";
	
	private static final String SMALLCONF_SUBSCRIPTIONS_DELETE = 
			"DELETE FROM smallconf_subscriptions WHERE collectionowner=? AND subscriber=?";
	
	private static final String GET_CHILD = 
			"SELECT collectionowner, creator, name, type, description, parent, " +
			"password, nqos, icqos, deadline, item_lifecycle FROM pubsub_personal " + 
			"WHERE parent=?";
	
	private static final String GET_NODE_ITEM = 
			"SELECT collectionowner, ID, publisher, content, time, status, handler, subid, " +
			"type, name, body, iqos, delay, deadline FROM history WHERE ID=?" ;
	
	private static final String GET_PERSONAL_ITEM = 
			"SELECT collectionowner, ID, publisher, content, time, status, handler, subid, " +
			"type, name, body, iqos, delay, deadline FROM personal_history WHERE ID=?" ;
	
	private static final String GET_OFFLINE = 
			"SELECT sequence,xml FROM offline WHERE collectionowner=?" ;
			//"SELECT xml FROM offline WHERE node=?" ;
	
	private static final String DELETE_OFFLINE = 
			"DELETE FROM offline WHERE sequence=?" ;
	
	private static final String DELETE_GROUP_ITEM = 
			"DELETE FROM history WHERE ID=?" ;

	private static final String DELETE_PERSONAL_ITEM = 
			"DELETE FROM personal_history WHERE ID=?" ;
	
	private static final String UPDATE_NODE = 
			"UPDATE pubsub_node SET name=?, type=?, description=?, parent=?, password=?, nqos=?, " +
			"icqos=?, deadline=?, item_lifecycle=? WHERE collectionowner=?" ;
	
	private static final String UPDATE_PERSONAL_NODE = 
			"UPDATE pubsub_personal SET name=?, type=?, description=?, parent=?, password=?, nqos=?, " +
			"icqos=?, deadline=?, item_lifecycle=? WHERE collectionowner=?" ;
	
	private static final String UPDATE_SMALLCONF = 
			"UPDATE pubsub_smallconf SET name=?, type=?, description=?, parent=?, password=?, nqos=?, " +
			"icqos=? WHERE collectionowner=?" ;
	
	private static final String UPDATE_SUBJECT = 
			"UPDATE subjects SET name=? WHERE collectionowner=?, subid=?" ;
	
	private static final String UPDATE_PERSONAL_SUBJECT = 
			"UPDATE personal_subjects SET name=? WHERE collectionowner=?, subid=?" ;
	
	private static final String DELETE_PERSONAL_SUBJECT = 
			"DELETE FROM personal_subjects WHERE collectionowner=? AND subid=? AND type=?" ;
	
	private static final String DELETE_SUBJECT = 
			"DELETE FROM subjects WHERE collectionowner=? AND subid=? AND type=?" ;
	
	private static final String ADD_OFFLINE = 
			"INSERT INTO offline (collectionowner, node, xml) VALUES(?,?,?)" ;
	
	private static final String GET_OFFLINE_SIZE = 
			"SELECT COUNT(*) FROM offline WHERE collectionowner=?" ;
	
	private static final String GET_OFFLINE_BY_JID = 
			"SELECT sequence FROM offline WHERE collectionowner=?" ;
	
	/**
	 * get the max nodeJID in database, 
	 * 
	 * @return the max nodeJID
	 */
	public static String getMaxJID(boolean isSmallconf) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		String maxNodeJID = null;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(isSmallconf == true) {
				pstmt = con.prepareStatement(GET_MAX_SMALLCONF_JID);
			}
			else {
				pstmt = con.prepareStatement(GET_MAX_NODE_JID);
			}
			
//			pstmt.setString(1, "collectionowner");
			rs = pstmt.executeQuery();
			while(rs.next()) {
				maxNodeJID = rs.getString(1);
			}
		
		} catch (SQLException e) {
			Log.info("getMaxNodeJID failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return maxNodeJID;
	}
	
	/**
	 * add the node data into database
	 * 
	 * @param node the node need to be store
	 */
	public static void addSmallconf(PubsubSmallconf smallconf, PubsubService service) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(ADD_SMALLCONF);
			
			pstmt.setString(1, smallconf.getID());
			pstmt.setString(2, smallconf.getCreator());
			pstmt.setString(3, smallconf.getName());
			pstmt.setString(4, smallconf.getType());
			pstmt.setString(5, smallconf.getDescription());
			if (smallconf.getParent() != null) {
				pstmt.setString(6, smallconf.getParent().getID());
			}
			else {
				pstmt.setString(6, "smallconfroot@"+service.getDomain());
			}
			pstmt.setString(7, smallconf.getPassword());
			pstmt.setString(8, smallconf.getNqos().get().toString(16));
			pstmt.setString(9, smallconf.getIcqos().get().toString(16));

			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			Log.info("addGroupNode failed");
			e.printStackTrace();
			abortTransaction = true;
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	} 
	
	public static void addNode(PubsubNode node, PubsubService service) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(node.getType().equals("group")) {
				pstmt = con.prepareStatement(ADD_GROUP_NODE);
			}
			else if(node.getType().equals("personal")) {
				pstmt = con.prepareStatement(ADD_PERSONAL_NODE);
			}
			
			pstmt.setString(1, node.getID());
			pstmt.setString(2, node.getCreator());
			pstmt.setString(3, node.getName());
			pstmt.setString(4, node.getType());
			pstmt.setString(5, node.getDescription());
			if (node.getParent() != null) {
				pstmt.setString(6, node.getParent().getID());
			}
			else {
				pstmt.setString(6, "root@"+service.getDomain());
			}
			pstmt.setString(7, node.getPassword());
			pstmt.setString(8, node.getNqos().get().toString(16));
			pstmt.setString(9, node.getIcqos().get().toString(16));
			pstmt.setLong(10, node.getDeadline());
			pstmt.setLong(11, node.getItemLifecycle());

			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			Log.info("addGroupNode failed");
			e.printStackTrace();
			abortTransaction = true;
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	} 
	/**
	 * Loads all nodes from the database and adds them to the pubsub service. 
	 * 
	 * @param nodesMap the hashmap of pubsub service that is hosting nodes
	 */
/*
	public static void loadNodesMap(Map<String, PubsubNode> nodesMap, PubsubService service) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(LOAD_NODES);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				PubsubNode node = getNode(rs, service);
				if (node.getType().equals("group")) {
					getNodeSubscriber(node.getID(), node.getSubscribersMap());
				}
				else {
					
				}
				nodesMap.put(node.getName(), node);
			}
			
		} catch (SQLException e) {
			Log.info("loadNodeMap failed");
			abortTransaction = true;
			e.printStackTrace();
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
*/
	/**
	 * Get the node from ResultSet
	 * 
	 * @param rs
	 * @param service
	 * @return
	 */
	private static PubsubNode getNode(ResultSet rs, PubsubService service) {
		
		PubsubNode node = new PubsubNode();
		try {
			String nodeJID = rs.getString(1);
			String creator = rs.getString(2);
			String name = rs.getString(3);
			String type = rs.getString(4);
			String description = rs.getString(5);
			String parentJID = rs.getString(6);
			String password = rs.getString(7);
			String strNqos = rs.getString(8);
			String strIcqos = rs.getString(9);
			long deadline = rs.getInt(10);
			long itemLifecycle = rs.getInt(11);
			
			
			node.setID(nodeJID);
			node.setCreator(creator);
			node.setName(name);
			node.setType(type);
			node.setDescription(description);
			if(!parentJID.equals("root@"+service.getDomain())) {     
				node.setParent(service.getNodeByJID(parentJID, false));
			}
			node.setPassword(password);
			node.setNqos(new PubsubQOS(strNqos));
			if(strNqos != null) {
				
				node.setNgqos(node.getNqos().and(PubsubQOS.ngqos_NODE_GLOBLE_QOS));
				node.setNcqos(node.getNqos().and(PubsubQOS.ncqos_NODE_CUSTOM_QOS));
			}
			node.setIcqos(new PubsubQOS(strIcqos));
			
			node.setDeadline(deadline);
			node.setItemLifecycle(itemLifecycle);
			
			if(node.getNcqos() != null) {
				service.getItemManager().itemTypesNew(node);
			}
			
		} catch (SQLException e) {
			Log.info("getNode failed");
			e.printStackTrace();
			
		} 
		return node;
	}
	
	/**
	 * get the node's subscribers by node jid
	 * 
	 * @param nodeJID the node jid
	 * @return the map of node's subscribers
	 */
/*	
	private static void getNodeSubscriber(String nodeJID, HashMap<String, PubsubSubscriber> map) {
		//TODO getNodeSubscriber
	}
*/
	/**
	 * get the node by nodeJID 
	 * 
	 * @param nodeJID
	 * @return the node get by nodeJID, if don't exist, return null
	 */
	public static PubsubNode getNodeByJID(String nodeJID, PubsubService service, boolean isPersonal) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		PubsubNode node = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(isPersonal) {
				// personal
				pstmt = con.prepareStatement(GET_PERSONAL_NODE_BY_JID);
			}
			else {
				// group
				pstmt = con.prepareStatement(GET_NODE_BY_JID);
			}
			pstmt.setString(1, nodeJID);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				node = getNode(rs, service);
			}
			
		} catch (SQLException e) {
			Log.info("getNodeByJID failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		
		return node;
	}
	
	public static PubsubSmallconf getSmallconfByJID(String smallconfJID, PubsubService service) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		PubsubSmallconf smallconf = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_SMALLCONF_BY_JID);

			pstmt.setString(1, smallconfJID);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				smallconf = getSmallconf(rs, service);
			}
			
		} catch (SQLException e) {
			Log.info("getSmallconfByJID failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		
		return smallconf;
	}
	/**
	 * add data to table subsriptions in database
	 * 
	 * @param subscriber the data need to add
	 */
	public static void nodeAddSubscriber(PubsubSubscriber subscriber) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(NODE_ADD_SUBSCRIBER);

			pstmt.setString(1, subscriber.getNode().getID());

			
//			JID jid = new JID(subscriber.getJID());
			pstmt.setString(2, subscriber.getJID());
			
			pstmt.setString(3, subscriber.getNsqos().get().toString(16));
			pstmt.setString(4, subscriber.getIsqos().get().toString(16));
			pstmt.setString(5, Long.toString(subscriber.getHistoryRange()));
			pstmt.setLong(6, subscriber.getDeadline());
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			Log.info("addSubscriber failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void smallconfAddSubscriber(PubsubSubscriber subscriber) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(SMALLCONF_ADD_SUBSCRIBER);

			pstmt.setString(1, subscriber.getSmallconf().getID());
			
//			JID jid = new JID(subscriber.getJID());
			pstmt.setString(2, subscriber.getJID());
			
			pstmt.setString(3, subscriber.getNsqos().get().toString(16));
			pstmt.setString(4, subscriber.getIsqos().get().toString(16));

			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			Log.info("addSubscriber failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	/**
	 * add data to table user_subscriptions in database
	 * 
	 * @param JID  the user jid
	 * @param nodeID  the node jid witch be subscribered
	 * @param type  the node's type
	 */
	public static void userSubscriptionsAdd(String JID, String nodeID, String type) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(USER_SUBSCRIPTIONS_ADD);
			pstmt.setString(1, JID);
			pstmt.setString(2, nodeID);
			pstmt.setString(3, type);
			
			pstmt.execute();
			
		} catch (SQLException e) {
			Log.info("userSubscriptionsAdd failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void userSubscriptionsDelete(String JID, String nodeID) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(JID != null) {
				pstmt = con.prepareStatement(USER_SUBSCRIPTIONS_DELETE);
				pstmt.setString(1, JID);
				pstmt.setString(2, nodeID);
			}
			else {
				pstmt = con.prepareStatement(USER_SUBSCRIPTIONS_NODE_DELETE);
				pstmt.setString(1, nodeID);
			}
			
			pstmt.execute();
			
		} catch (SQLException e) {
			Log.info("userSubscriptionsDelete failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	/**
	 *  just for test
	 */
	public static void testDatabase() {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(TEST_STRING);
			pstmt.setString(1, "shit");
			pstmt.setString(2, "holyshit");
			pstmt.setString(3, "group");
			pstmt.execute();
			
		} catch (SQLException e) {
			Log.info("testDatabase failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	/**
	 * get the nodes which user have subscriber
	 * 
	 * @param userJID  the user's jid
	 * @return the list of nodes
	 */
	public static ArrayList<PubsubSubNode> getSubNodes(String userJID) {

		ArrayList<PubsubSubNode> subNodesList = new ArrayList<PubsubSubNode>();
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_SUB_NODES);
			pstmt.setString(1, userJID);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				
				String nodeJID = rs.getString(2);
				String type = rs.getString(3);
				if(type.equals("group") || type.equals("personal")) {
					
					PubsubSubNode subNode = new PubsubSubNode(nodeJID, type);
					subNodesList.add(subNode);
				}
			}
			
		} catch (SQLException e) {
			Log.info("getSubNodes failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return subNodesList;
	}
	
	public static ArrayList<PubsubSubNode> getSubSmallconfs(String userJID) {

		ArrayList<PubsubSubNode> subSmallconfsList = new ArrayList<PubsubSubNode>();
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_SUB_NODES);
			pstmt.setString(1, userJID);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				
				String nodeJID = rs.getString(2);
				String type = rs.getString(3);
				if(type.equals("smallconf")) {
					
					PubsubSubNode subNode = new PubsubSubNode(nodeJID, type);
					subSmallconfsList.add(subNode);
				}
			}
			
		} catch (SQLException e) {
			Log.info("getSubNodes failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return subSmallconfsList;
	}
	/**
	 * add data to table subjects or personal_subjects in database
	 * 
	 * @param subject the subject need to be storage
	 * @param isPersonal true when the subject is for personal, false for group
	 */
	public static void addSubject(PubsubSubject subject, boolean isPersonal) {
		
		if(subject.getNode() == null) {
			return;
		}
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(isPersonal) {
				pstmt = con.prepareStatement(ADD_PERSONAL_SUBJECT);
			}
			else {
				pstmt = con.prepareStatement(ADD_SUBJECT);
			}
			pstmt.setString(1, subject.getNode());
			pstmt.setString(2, subject.getSubid());
			pstmt.setString(3, subject.getType());
			pstmt.setString(4, subject.getName());
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			Log.info("addSubject failed");
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static HashMap<String, PubsubSubscriber> getNodeSubscribers(PubsubNode node, PubsubService service) {
		
		HashMap<String, PubsubSubscriber> map = new HashMap<String, PubsubSubscriber>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_NODE_SUBSCRIBERS);
			pstmt.setString(1, node.getID());
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				
				 PubsubSubscriber subsciber = service.getMemberManager().subscriberNew(node, rs.getString(2)
						 													, new PubsubQOS(rs.getString(3))
				 															, new PubsubQOS(rs.getString(4))
																			, Long.parseLong(rs.getString(5))
																			, rs.getInt(6));
				map.put(subsciber.getJID(), subsciber);
			}
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return map;
	}
	
	public static HashMap<String, PubsubSubscriber> getSmallconfSubscribers(PubsubSmallconf smallconf, PubsubService service) {
		
		HashMap<String, PubsubSubscriber> map = new HashMap<String, PubsubSubscriber>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_SMALLCONF_SUBSCRIBERS);
			pstmt.setString(1, smallconf.getID());
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				
				 PubsubSubscriber subsciber = service.getMemberManager().smallconfSubscriberNew(smallconf, rs.getString(2)
						 													, new PubsubQOS(rs.getString(3))
				 															, new PubsubQOS(rs.getString(4)));
											
				map.put(subsciber.getJID(), subsciber);
			}
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return map;
	}
	
	public static void subscriptionsUpdate(PubsubSubscriber subscriber) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(UPDATE_SUBSCRIPTIONS_NSQOS);
			pstmt.setString(1, subscriber.getNsqos().get().toString(16));
			pstmt.setString(2, subscriber.getNode().getID());
			pstmt.setString(3, subscriber.getJID());
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void smallconfSubscriptionsUpdate(PubsubSubscriber subscriber) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(UPDATE_SMALLCONF_SUBSCRIPTIONS_NSQOS);
			pstmt.setString(1, subscriber.getNsqos().get().toString(16));
			pstmt.setString(2, subscriber.getSmallconf().getID());
			pstmt.setString(3, subscriber.getJID());
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static ArrayList<PubsubNode> getAllGroupNodes(PubsubService service) {
		
		ArrayList<PubsubNode> list = new ArrayList<PubsubNode>();
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_ALL_GROUP_NODES);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				
				PubsubNode node = getNode(rs, service);
				list.add(node);
			}
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return list;
	}
	
	public static ArrayList<PubsubSubject> getNodeSubjects(PubsubNode node) {
		
		if(node == null) {
			return null;
		}
		ArrayList<PubsubSubject> list = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(node.getType().equals("group")) {
				pstmt = con.prepareStatement(GET_GROUP_NODE_SUBJECTS);
			}
			else {
				pstmt = con.prepareStatement(GET_PERSONAL_NODE_SUBJECTS);
			}
			
			pstmt.setString(1, node.getID());
			rs = pstmt.executeQuery();
			
			list = new ArrayList<PubsubSubject>();
			while(rs.next()) {
				
				PubsubSubject subject = new PubsubSubject();
				subject.setNode(rs.getString(1));
				subject.setSubid(rs.getString(2));
				subject.setType(rs.getString(3));
				subject.setName(rs.getString(4));
				list.add(subject);
			}
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("getNodeSubjects failed");
		
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return list;
	}
	
	public static ArrayList<PubsubHistory> getNodeHistory(PubsubNode node) {
		
		if(node == null) {
			return null;
		}
		ArrayList<PubsubHistory> list = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(node.getType().equals("group")) {
				pstmt = con.prepareStatement(GET_GROUP_NODE_HISTORY);
			}
			else {
				pstmt = con.prepareStatement(GET_PERSONAL_NODE_HISTORY);
			}
			pstmt.setString(1, node.getID());
			rs = pstmt.executeQuery();
			list = new ArrayList<PubsubHistory>();
			while(rs.next()) {
				
				PubsubHistory history = new PubsubHistory();
				history.setOwner(rs.getString(1));
				history.setID(rs.getString(2));
				history.setPublisher(rs.getString(3));
				history.setContent(rs.getString(4));
				history.setTime(rs.getString(5));
				history.setStatus(rs.getString(6));
				history.setHandler(rs.getString(7));
				history.setSubid(rs.getString(8));
				history.setType(rs.getString(9));
				history.setBody(rs.getString(11));
				history.setDelay(rs.getInt(13));
				history.setDeadline(rs.getInt(14));
				
				list.add(history);
			}
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return list;
	}
	
	public static void updateHistory(PubsubHistory item, boolean isPersonal) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(isPersonal) {
				pstmt = con.prepareStatement(PERSONAL_HISTORY_UPDATE);
			}
			else {
				pstmt = con.prepareStatement(HISTORY_UPDATE);
			}
			pstmt.setString(1, item.getContent());
			pstmt.setString(2, item.getTime());
			pstmt.setString(3, item.getStatus());
			pstmt.setString(4, item.getBody());
			pstmt.setString(5, item.getID());
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
			DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
		}
	}
	
	public static void addHistory(PubsubHistory history, boolean isPersonal) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(isPersonal) {
				pstmt = con.prepareStatement(ADD_PERSONAL_HISTORY);
			}
			else {
				pstmt = con.prepareStatement(ADD_HISTORY);
			}
			pstmt.setString(1, history.getOwner());
			pstmt.setString(2, history.getID());
			pstmt.setString(3, history.getPublisher());
			pstmt.setString(4, history.getContent());
			pstmt.setString(5, history.getTime());
			pstmt.setString(6, history.getStatus());
			pstmt.setString(7, history.getHandler());
			pstmt.setString(8, history.getSubid());
			pstmt.setString(9, history.getType());
			pstmt.setString(10, history.getBody());
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
			DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
		}
	}

	public static ArrayList<PubsubSmallconf> getAllSmallconfs(PubsubService service) {
		
		ArrayList<PubsubSmallconf> list = new ArrayList<PubsubSmallconf>();
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_ALL_SMALLCONFS);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				
				PubsubSmallconf smallconf = getSmallconf(rs, service);
				list.add(smallconf);
			}
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return list;
	}
	
	private static PubsubSmallconf getSmallconf(ResultSet rs, PubsubService service) {
		
		PubsubSmallconf smallconf = new PubsubSmallconf();
		try {
			String nodeJID = rs.getString(1);
			String creator = rs.getString(2);
			String name = rs.getString(3);
			String type = rs.getString(4);
			String description = rs.getString(5);
			String parentJID = rs.getString(6);
			String password = rs.getString(7);
			String strNqos = rs.getString(8);
			String strIcqos = rs.getString(9);
			
			
			smallconf.setID(nodeJID);
			smallconf.setCreator(creator);
			smallconf.setName(name);
			smallconf.setType(type);
			smallconf.setDescription(description);
			if(!parentJID.equals("smallconfroot@"+service.getDomain())) {     
				smallconf.setParent(service.getSmallconfByJID(parentJID));
			}
			smallconf.setPassword(password);
			smallconf.setNqos(new PubsubQOS(strNqos));
			if(strNqos != null) {
				
				smallconf.setNgqos(smallconf.getNqos().and(PubsubQOS.ngqos_NODE_GLOBLE_QOS));
				smallconf.setNcqos(smallconf.getNqos().and(PubsubQOS.ncqos_NODE_CUSTOM_QOS));
			}
			smallconf.setIcqos(new PubsubQOS(strIcqos));
			
		} catch (SQLException e) {
			Log.info("getNode failed");
			e.printStackTrace();
			
		} 
		return smallconf;
	}
	
	public static void nodeDelete(PubsubNode node) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(node.getType().equals("group")) {
				pstmt = con.prepareStatement(DELETE_GROUP_NODE);
			}
			else if(node.getType().equals("personal")) {
				pstmt = con.prepareStatement(DELETE_PERSONAL_NODE);
			}
			pstmt.setString(1, node.getID());
			pstmt.executeUpdate();
						
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	public static void smallconfDelete(PubsubSmallconf smallconf) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(DELETE_SMALLCONF);
			pstmt.setString(1, smallconf.getID());
			pstmt.executeUpdate();
						
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("delete smallconf failed");
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	public static void subscriptionsDelete(String nodeID, String subscriber) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(SUBSCRIPTIONS_DELETE);
			pstmt.setString(1, nodeID);
			pstmt.setString(2, subscriber);
			pstmt.executeUpdate();
						
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("subscriptionsDelete failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	public static void smallconfSubscriptionsDelete(String smallconfID, String subscriber) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(SMALLCONF_SUBSCRIPTIONS_DELETE);
			pstmt.setString(1, smallconfID);
			pstmt.setString(2, subscriber);
			pstmt.executeUpdate();
						
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("smallconfSubscriptionsDelete failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	public static ArrayList<PubsubNode> getChildrenByParentID(String parentID, PubsubService service) {
		
		ArrayList<PubsubNode> list = new ArrayList<PubsubNode>();
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_CHILD);
			pstmt.setString(1, parentID);
			
			rs = pstmt.executeQuery();
			while(rs.next()) {
				PubsubNode node = getNode(rs,service);
				if(node != null) {
					list.add(node);
				}
			}
						
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("getChildrenByParentID failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return list;
	}
	
	public static PubsubHistory getNodeItem(String itemID) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		PubsubHistory item = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_NODE_ITEM);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				
				item = new PubsubHistory();
				item.setOwner(rs.getString(1));
				item.setID(rs.getString(2));
				item.setPublisher(rs.getString(3));
				item.setContent(rs.getString(4));
				item.setTime(rs.getString(5));
				item.setStatus(rs.getString(6));
				item.setHandler(rs.getString(7));
				item.setSubid(rs.getString(8));
				item.setType(rs.getString(9));
				item.setBody(rs.getString(11));
				item.setDelay(rs.getInt(13));
				item.setDeadline(rs.getInt(14));
				
			}
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("getNodeItem failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return item;
	}
	public static PubsubHistory getPersonalItem(String itemID) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		PubsubHistory item = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_PERSONAL_ITEM);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				
				item = new PubsubHistory();
				item.setOwner(rs.getString(1));
				item.setID(rs.getString(2));
				item.setPublisher(rs.getString(3));
				item.setContent(rs.getString(4));
				item.setTime(rs.getString(5));
				item.setStatus(rs.getString(6));
				item.setHandler(rs.getString(7));
				item.setSubid(rs.getString(8));
				item.setType(rs.getString(9));
				item.setBody(rs.getString(11));
				item.setDelay(rs.getInt(13));
				item.setDeadline(rs.getInt(14));
				
			}
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("getPersonalItem failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return item;
	}
	
	public static ArrayList<String> getUserOffline(String userJID) {
		
		ArrayList<String> list = new ArrayList<String>();
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_OFFLINE);
			pstmt.setString(1, userJID);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				String xml = rs.getString(2);
				int sequence = rs.getInt(1);
				list.add(xml);
				deleteOffline(sequence);
			}
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("getUserOffline failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
		return list;
	}
	
	private static void deleteOffline(int sequence) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(DELETE_OFFLINE);
			pstmt.setInt(1, sequence);
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("deleteOffline failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void deleteItem(String itemID, String type) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(!type.equals("personal")) {
				pstmt = con.prepareStatement(DELETE_GROUP_ITEM);
			}
			else {
				pstmt = con.prepareStatement(DELETE_PERSONAL_ITEM);
			}
			pstmt.setString(1, itemID);
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("deleteItem failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void updateNode(PubsubNode node, PubsubService service) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(node.getType().equals("personal")) {
				pstmt = con.prepareStatement(UPDATE_PERSONAL_NODE);
			}
			else {
				pstmt = con.prepareStatement(UPDATE_NODE);
			}
			pstmt.setString(1, node.getName());
			pstmt.setString(2, node.getType());
			pstmt.setString(3, node.getDescription());
			if (node.getParent() != null) {
				pstmt.setString(4, node.getParent().getID());
			}
			else {
				pstmt.setString(4, "root@"+service.getDomain());
			}
			pstmt.setString(5, node.getPassword());
			pstmt.setString(6, node.getNqos().get().toString(16));
			pstmt.setString(7, node.getIcqos().get().toString(16));
			pstmt.setLong(8, node.getDeadline());
			pstmt.setLong(9, node.getItemLifecycle());
			pstmt.setString(10, node.getID());
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("updateNode failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void updateSmallconf(PubsubSmallconf smallconf, PubsubService service) {
	
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(UPDATE_SMALLCONF);
			pstmt.setString(1, smallconf.getName());
			pstmt.setString(2, smallconf.getType());
			pstmt.setString(3, smallconf.getDescription());
			if (smallconf.getParent() != null) {
				pstmt.setString(4, smallconf.getParent().getID());
			}
			else {
				pstmt.setString(4, "smallconfroot@"+service.getDomain());
			}
			pstmt.setString(5, smallconf.getPassword());
			pstmt.setString(6, smallconf.getNqos().get().toString(16));
			pstmt.setString(7, smallconf.getIcqos().get().toString(16));

			pstmt.setString(8, smallconf.getID());
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("updateSmallconf failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void updateSubject(String nodeID, String subid, String name, boolean isPersonal) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(isPersonal == false) {
				pstmt = con.prepareStatement(UPDATE_SUBJECT);
			}
			else {
				pstmt = con.prepareStatement(UPDATE_PERSONAL_SUBJECT);
			}
			pstmt.setString(1, name);
			pstmt.setString(2, nodeID);
			pstmt.setString(3, subid);
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("updateSubject failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void deleteSubject(String nodeID, String subid, String type, boolean isPersonal) {
	
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			if(isPersonal) {
				pstmt = con.prepareStatement(DELETE_PERSONAL_SUBJECT);
			}
			else {
				pstmt = con.prepareStatement(DELETE_SUBJECT);
			}
			pstmt.setString(1, nodeID);
			pstmt.setString(2, subid);
			pstmt.setString(3, type);
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("deleteSubject failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
	
	public static void addOffline(String jid, String nodeID, String xml) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(ADD_OFFLINE);
			
			pstmt.setString(1, jid);
			pstmt.setString(2, nodeID);
			pstmt.setString(3, xml);
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("addOffline failed");
			
		} finally {
			DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
		}
	}
	
	public static int getOfflineSize(String jid) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		int size = 0;
		
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_OFFLINE_SIZE);
			pstmt.setString(1, jid);
			
			rs = pstmt.executeQuery();
			size = rs.getFetchSize(); 
			
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("getOfflineSize failed");
			
		} finally {
			DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
		}
		return size;
	}
	
	public static void deleteEarlyOffline(String jid) {
		
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(GET_OFFLINE_BY_JID);
			pstmt.setString(1, jid);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				
				int sequence = rs.getInt(1);
				deleteOffline(sequence);
				return;
			}
		} catch (SQLException e) {
			abortTransaction = true;
			e.printStackTrace();
			Log.info("deleteEarlyOffline failed");
			
		} finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(con, abortTransaction);
        }
	}
}
