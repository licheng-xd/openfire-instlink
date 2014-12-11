package com.instlink.openfire.pubsub.plugin;

import java.math.BigInteger;

/**
 * the super class of pubsub_Enum and pubsub_QOS
 * 
 * @author LC
 *
 */
public class InstlinkHex {
	
	protected BigInteger m_value = null;
	
	private static final InstlinkHex zero = new InstlinkHex("0x00");

	// constructor
	public InstlinkHex(BigInteger value) {
		m_value = value;
	}

	public InstlinkHex(String value) {
		
		if (value == null) {
			return;
		}
		if (value.startsWith("0x") || value.startsWith("0X")) {
			m_value = new BigInteger(value.substring(2), 16);
		}
		else {
			m_value = new BigInteger(value, 16);
		}
	}

	// methods
	public BigInteger get() {
		return m_value;
	}
	
	public InstlinkHex and(InstlinkHex dst) {
		return new InstlinkHex(m_value.and(dst.m_value));
	}

	public InstlinkHex or(InstlinkHex dst) {
		return new InstlinkHex(m_value.or(dst.m_value));
	}
	
	public InstlinkHex not() {
		return new InstlinkHex(m_value.not());
	}

	public InstlinkHex shiftRight(int n) {
		return new InstlinkHex(m_value.shiftRight(n));
	}

	public boolean equals(InstlinkHex dst) {
		return m_value.equals(dst.get());
	}
	
	public boolean isZero() {
		return m_value.equals(zero.get());
	}
	
	public int compare(InstlinkHex dst) {
		return m_value.compareTo(dst.m_value);
	}
}
