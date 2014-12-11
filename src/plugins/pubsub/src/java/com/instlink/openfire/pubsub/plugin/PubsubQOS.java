package com.instlink.openfire.pubsub.plugin;

import java.math.BigInteger;

/**
 * all QOS of pubsub be defined here
 * 
 * @author LC
 *
 */
public class PubsubQOS extends InstlinkHex {
	

	/**
	 * ngQOS
	 */
	public static final PubsubQOS ngqos_NODE_GLOBLE_QOS = new PubsubQOS("0xFF000000");
	
	public static final PubsubQOS ngqos_TEMP = new PubsubQOS("0x80000000");
	
	public static final PubsubQOS ngqos_ACCESS_MODEL = new PubsubQOS("0x0C000000");
	public static final PubsubQOS ngqos_ACCESS_MODEL_PASSWD = new PubsubQOS("0x04000000");
	public static final PubsubQOS ngqos_ACCESS_MODEL_AUTHORIZE = new PubsubQOS("0x08000000");
	public static final PubsubQOS ngqos_ACCESS_MODEL_OPEN = new PubsubQOS("0x0C000000");
	
	public static final PubsubQOS ngqos_TYPE = new PubsubQOS("0x03000000");
	public static final PubsubQOS ngqos_TYPE_GROUP = new PubsubQOS("0x01000000");
	public static final PubsubQOS ngqos_TYPE_PERSONAL = new PubsubQOS("0x02000000");
	
	/**
	 * ncQOS
	 */
	public static final PubsubQOS ncqos_NODE_CUSTOM_QOS = new PubsubQOS("0x00FFFFFF");
	
	public static final PubsubQOS ncqos_TREE = new PubsubQOS("0x00800000");
	public static final PubsubQOS ncqos_TEMP_SUB = new PubsubQOS("0x00400000");
	public static final PubsubQOS ncqos_SECURITY = new PubsubQOS("0x00200000");
	public static final PubsubQOS ncqos_WATCHER = new PubsubQOS("0x00080000");
	
	public static final PubsubQOS ncqos_ROLE = new PubsubQOS("0x00030000");
	public static final PubsubQOS ncqos_ROLE_MEMBER = new PubsubQOS("0x00000000");
	public static final PubsubQOS ncqos_ROLE_OUTCASE = new PubsubQOS("0x00010000");
	public static final PubsubQOS ncqos_ROLE_OWNER = new PubsubQOS("0x00020000");
	public static final PubsubQOS ncqos_ROLE_CREATOR = new PubsubQOS("0x00030000");
	
	public static final PubsubQOS ncqos_ITEM_TYPE = new PubsubQOS("0x0000FFFF");
	public static final PubsubQOS ncqos_ITEM_TYPE_CONF = new PubsubQOS("0x00008000");
	public static final PubsubQOS ncqos_ITEM_TYPE_BROAD = new PubsubQOS("0x00004000");
	public static final PubsubQOS ncqos_ITEM_TYPE_FILE = new PubsubQOS("0x00002000");
	public static final PubsubQOS ncqos_ITEM_TYPE_BBS = new PubsubQOS("0x00001000");
	public static final PubsubQOS ncqos_ITEM_TYPE_BLOG = new PubsubQOS("0x00000800");
	public static final PubsubQOS ncqos_ITEM_TYPE_COMMENT = new PubsubQOS("0x00000400");
	public static final PubsubQOS ncqos_ITEM_TYPE_NOTIFY = new PubsubQOS("0x00000200");

	/**
	 * icQOS
	 */
	public static final PubsubQOS icqos_LOCATION_CONF = new PubsubQOS("0xFF00000000000000");
	public static final PubsubQOS icqos_LOCATION_BROAD = new PubsubQOS("0x00FF000000000000");
	public static final PubsubQOS icqos_LOCATION_FILE = new PubsubQOS("0x0000FF0000000000");
	public static final PubsubQOS icqos_LOCATION_BBS = new PubsubQOS("0x000000FF00000000");
	public static final PubsubQOS icqos_LOCATION_BLOG = new PubsubQOS("0x00000000FF000000");
	public static final PubsubQOS icqos_LOCATION_COMMENT = new PubsubQOS("0x0000000000FF0000");
	public static final PubsubQOS icqos_LOCATION_NOTIFY = new PubsubQOS("0x000000000000FF00");
	
	public static final PubsubQOS icqos_item_COMMENT = new PubsubQOS("0x200");
	public static final PubsubQOS icqos_item_NOTIFY = new PubsubQOS("0x100");
	public static final PubsubQOS icqos_item_HISTORY = new PubsubQOS("0x80");
	public static final PubsubQOS icqos_item_OFFLINE = new PubsubQOS("0x40");
	public static final PubsubQOS icqos_item_PUBLISH = new PubsubQOS("0x20");
	public static final PubsubQOS icqos_item_SUBSCRIBE = new PubsubQOS("0x10");
	public static final PubsubQOS icqos_item_SECURITY = new PubsubQOS("0x08");
	public static final PubsubQOS icqos_item_FEEDBACK = new PubsubQOS("0x04");
	public static final PubsubQOS icqos_item_DELAY = new PubsubQOS("0x02");
	public static final PubsubQOS icqos_item_TEMP = new PubsubQOS("0x01");
	
	// constructor
	public PubsubQOS(String value) {
		super(value);
	}

	public PubsubQOS(BigInteger value) {
		super(value);
	}

	// methods
	public BigInteger get() {
		return m_value;
	}
	
	public PubsubQOS and(InstlinkHex dst) {
		return new PubsubQOS(m_value.and(dst.m_value));
	}
	
	public PubsubQOS or(InstlinkHex dst) {
		return new PubsubQOS(m_value.or(dst.m_value));
	}
	
	public PubsubQOS not() {
		return new PubsubQOS(m_value.not());
	}

	public PubsubQOS shiftRight(int n) {
		return new PubsubQOS(m_value.shiftRight(n));
	}
}
