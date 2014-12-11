package com.instlink.openfire.pubsub.plugin;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;

/**
 * define the packet that this plugin should process
 * 
 * @author LC
 *
 */
public class PubsubIQHandler extends IQHandler {

	private static final Logger Log = LoggerFactory
			.getLogger(PubsubIQHandler.class);

	private IQHandlerInfo info = null;
	private PubsubService service = null;

	public PubsubIQHandler(String moduleName, PubsubService service) {
		super(moduleName);

		org.jivesoftware.util.Log.setDebugEnabled(true);

		info = new IQHandlerInfo(PubsubService.PUBSUB_ELEM_NAME, PubsubService.PUBSUB_NAMESPACE);
		this.service = service;
		Log.info("Pubsub plugin initial finish");
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {

		if (packet != null) {

			// parse pubsub packet
			PubsubPacket psPacket = new PubsubPacket(service, packet);
			// process correct packet
			if(psPacket.getEvent().equals(PubsubEnum.event_NONE)) {
				return null;
			}
//			Log.info("---------------receive packet----------------"+packet.toString());
			service.processPacket(psPacket);
		}
		return null;
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

}
