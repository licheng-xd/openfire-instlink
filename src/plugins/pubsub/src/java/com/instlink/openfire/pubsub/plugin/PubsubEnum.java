package com.instlink.openfire.pubsub.plugin;

import java.math.BigInteger;

/**
 * all event in pubsub plugin
 * 
 * @author LC
 *
 */
public class PubsubEnum extends InstlinkHex {

	/**
	 * pubsub event
	 */
	public static final PubsubEnum event_NONE = new PubsubEnum("0x00");
	public static final PubsubEnum event_CREATE = new PubsubEnum("0x01");
	public static final PubsubEnum event_GET_ALL_NODES = new PubsubEnum("0x02");
	public static final PubsubEnum event_SUB = new PubsubEnum("0x03");
	public static final PubsubEnum event_SUBED = new PubsubEnum("0x04");
	public static final PubsubEnum event_UNSUB = new PubsubEnum("0x05");
	public static final PubsubEnum event_NODEINFO = new PubsubEnum("0x06");
	public static final PubsubEnum event_GET_SUBSCRIBERS = new PubsubEnum("0x07");
	public static final PubsubEnum event_SUBER_UPT = new PubsubEnum("0x08");
	public static final PubsubEnum event_GET_SUB_NODES = new PubsubEnum("0x09");
	public static final PubsubEnum event_PUBLISH = new PubsubEnum("0x0A");
	public static final PubsubEnum event_DELETE = new PubsubEnum("0x0B");
	public static final PubsubEnum event_GET_HISTORY = new PubsubEnum("0x0C");
	public static final PubsubEnum event_DELETE_ITEM = new PubsubEnum("0x0D");
	public static final PubsubEnum event_HANDLE_ITEM = new PubsubEnum("0x0E");
	public static final PubsubEnum event_HANDLED_ITEM = new PubsubEnum("0x0F");
	public static final PubsubEnum event_GET_NODE_TREE = new PubsubEnum("0x10");
	public static final PubsubEnum event_NODE_NAME_UPT = new PubsubEnum("0x11");// 更改节点的名字
	public static final PubsubEnum event_NODE_PARENT_UPT = new PubsubEnum("0x12");// 更改父节点
	public static final PubsubEnum event_GETCHILD = new PubsubEnum("0x13");
	public static final PubsubEnum event_GET_OFFLINE = new PubsubEnum("0x14");
	public static final PubsubEnum event_SET_AFFILIATION = new PubsubEnum("0x15");
	public static final PubsubEnum event_SET_STATUS = new PubsubEnum("0x16");
	public static final PubsubEnum event_SET_CONFIGURE = new PubsubEnum("0x17");
	public static final PubsubEnum event_SET_SUBCONFIG = new PubsubEnum("0x18");
	public static final PubsubEnum event_MERGE_NODE = new PubsubEnum("0x19");
	public static final PubsubEnum event_DELETE_KEYWORD_ITEM = new PubsubEnum("0x20");
	public static final PubsubEnum event_GET_ALL_KEYWORDS = new PubsubEnum("0x100");
	public static final PubsubEnum event_KEYWORDINFO = new PubsubEnum("0x101");
	public static final PubsubEnum event_SET_KWCONFIGURE = new PubsubEnum("0x102");
	public static final PubsubEnum event_CREATE_KEYWORD = new PubsubEnum("0x103");
	public static final PubsubEnum event_SUB_KEYWORD = new PubsubEnum("0x104");
	public static final PubsubEnum event_SUBED_KEYWORD = new PubsubEnum("0x105");
	public static final PubsubEnum event_UNSUB_KEYWORD = new PubsubEnum("0x106");
	public static final PubsubEnum event_DELETE_KEYWORD = new PubsubEnum("0x107");
	public static final PubsubEnum event_GET_KEYWORD_SUBSCRIBERS = new PubsubEnum("0x108");
	public static final PubsubEnum event_GET_KEYWORD_HISTORY = new PubsubEnum("0x109");
	public static final PubsubEnum event_GET_KEYWORD_ITEM_DETAIL = new PubsubEnum("0x110");
	public static final PubsubEnum event_GET_CONF_ITEM_DETAIL = new PubsubEnum("0x103");
	public static final PubsubEnum event_GET_SEARCH_ITEM = new PubsubEnum("0x111");
	public static final PubsubEnum event_PUBLISH_KEYWORD = new PubsubEnum("0x112");
	public static final PubsubEnum event_CREATE_SMALLCONF = new PubsubEnum("0x113");
	public static final PubsubEnum event_CREATE_BROADCONF = new PubsubEnum("0x114");
	public static final PubsubEnum event_GET_ALL_SMALLCONFS = new PubsubEnum("0x115");
	public static final PubsubEnum event_GET_ALL_BROADCONFS = new PubsubEnum("0x116");
	public static final PubsubEnum event_SUB_SMALLCONF = new PubsubEnum("0x117");
	public static final PubsubEnum event_SUB_BROADCONF = new PubsubEnum("0x118");
	public static final PubsubEnum event_SMALLCONFINFO = new PubsubEnum("0x119");
	public static final PubsubEnum event_BROADCONFINFO = new PubsubEnum("0x120");
	public static final PubsubEnum event_UNSUB_SMALLCONF = new PubsubEnum("0x121");
	public static final PubsubEnum event_UNSUB_BROADCONF = new PubsubEnum("0x122");
	public static final PubsubEnum event_DELETE_SMALLCONF = new PubsubEnum("0x123");
	public static final PubsubEnum event_DELETE_BROADCONF = new PubsubEnum("0x124");
	public static final PubsubEnum event_GET_SMALLCONF_SUBSCRIBERS = new PubsubEnum("0x125");
	public static final PubsubEnum event_GET_BROADCONF_SUBSCRIBERS = new PubsubEnum("0x126");
	public static final PubsubEnum event_SET_SCCONFIGURE = new PubsubEnum("0x127");
	public static final PubsubEnum event_SET_BCCONFIGURE = new PubsubEnum("0x128");
	public static final PubsubEnum event_SUBED_SMALLCONF = new PubsubEnum("0x129");
	public static final PubsubEnum event_SUBED_BROADCONF = new PubsubEnum("0x130");
	public static final PubsubEnum event_SCPUBLISH = new PubsubEnum("0x131");
	public static final PubsubEnum event_SCSUBER_UPT = new PubsubEnum("0x132");
	public static final PubsubEnum event_BCSUBER_UPT = new PubsubEnum("0x133");
	public static final PubsubEnum event_BCPUBLISH = new PubsubEnum("0x134");
	public static final PubsubEnum event_SUBSCRIPTION = new PubsubEnum("0x135");
	public static final PubsubEnum event_KWSUBSCRIPTION = new PubsubEnum("0x136");
	public static final PubsubEnum event_GET_PER_ITEM_DETAIL = new PubsubEnum("0x137");
	public static final PubsubEnum event_SEARCH_PER_ITEM = new PubsubEnum("0x138");
	public static final PubsubEnum event_BCSUBSCRIPTION = new PubsubEnum("0x139");
	public static final PubsubEnum event_SCSUBSCRIPTION = new PubsubEnum("0x140");
	public static final PubsubEnum event_GET_SUB_SMALLCONFS = new PubsubEnum("0x141");
	public static final PubsubEnum event_GET_SUB_BROADCONFS = new PubsubEnum("0x142");
	public static final PubsubEnum event_PERSONAL_PUBLISH = new PubsubEnum("0x143");
	public static final PubsubEnum event_ERROR = new PubsubEnum("0x200");
	public static final PubsubEnum event_ERROR_BAD_REQUEST = new PubsubEnum("0x201");// 不符合定义的协议格式
	public static final PubsubEnum event_ERROR_ALREADAY_EXIST = new PubsubEnum("0x202");// 创建节点的时候，该节点已经存在
	public static final PubsubEnum event_ERROR_NODE_NOT_EXIST = new PubsubEnum("0x203");// 指定的节点不存在
	public static final PubsubEnum event_TIME_CHECK = new PubsubEnum("0x204");
	public static final PubsubEnum event_LEAVE = new PubsubEnum("0x205");

	/**
	 * state
	 */
	public static final PubsubEnum state_NO = new PubsubEnum("0x00");
	public static final PubsubEnum state_SPONSOR = new PubsubEnum("0x01");
	public static final PubsubEnum state_ATTENDER = new PubsubEnum("0x02");
	public static final PubsubEnum state_AI = new PubsubEnum("0x03");
	public static final PubsubEnum state_AO = new PubsubEnum("0x04");
	public static final PubsubEnum state_VI = new PubsubEnum("0x05");
	public static final PubsubEnum state_VO = new PubsubEnum("0x06");
	public static final PubsubEnum state_AIVI = new PubsubEnum("0x07");
	public static final PubsubEnum state_AIVO = new PubsubEnum("0x08");
	public static final PubsubEnum state_AOVI = new PubsubEnum("0x09");
	public static final PubsubEnum state_AOVO = new PubsubEnum("0x0A");

	// constructor
	public PubsubEnum(String value) {
		super(value);
	}

	public PubsubEnum(BigInteger value) {
		super(value);
	}
	
	// methods
	public BigInteger get() {
		return m_value;
	}

	public PubsubEnum and(InstlinkHex dst) {
		return new PubsubEnum(m_value.and(dst.m_value));
	}

	public PubsubEnum or(InstlinkHex dst) {
		return new PubsubEnum(m_value.or(dst.m_value));
	}

	public PubsubEnum not() {
		return new PubsubEnum(m_value.not());
	}

	public PubsubEnum shiftRight(int n) {
		return new PubsubEnum(m_value.shiftRight(n));
	}

}
