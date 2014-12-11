package com.instlink.openfire.pubsub.plugin;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * pubsub plugin
 * 
 * @author LC
 *
 */
public class PubsubPlugin implements Plugin ,Runnable{

	private static final Logger Log = LoggerFactory.getLogger(PubsubPlugin.class);
	private PubsubIQHandler pubsubHandler = null;
	private PubsubService service = null;
	private volatile boolean isRunning = true;
	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		
		service = new PubsubService();
		pubsubHandler = new PubsubIQHandler("pubsub handler", service);
		XMPPServer.getInstance().getIQRouter().addHandler(pubsubHandler);
		new Thread(this).start();
		Log.info("Pubsub load success");
		System.out.println("<--------Pubsub for Instlink load success-------->");
		// System.out.println("  Pubsub load success test test");
		// ����syso,����־�ļ�����־�ֵȼ�info, warn, error
		// log д���, ����д
	}

	@Override
	public void destroyPlugin() {
		isRunning = false;
		// û�õ�TODOɾ��
		XMPPServer.getInstance().getIQRouter().removeHandler(pubsubHandler);
		// ������ɾ��
	}

	@Override
	public void run() {
		while(isRunning)
		{
			synchronized (PubsubService.timeMap){
				long now = System.currentTimeMillis();
				
				Set<String> keySet = PubsubService.timeMap.keySet();
				Iterator<String> iter = keySet.iterator();
				while(iter.hasNext()) {
					
					try{
						String userJID = iter.next();
						long time = PubsubService.timeMap.get(userJID);
						
						if( (now - time) > 60*3000 ) {
							service.getMemberManager().conferenceUpdateParticipantOffline(userJID, "offline");
							service.getMemberManager().smallconfUpdateParticipantOffline(userJID, "offline");
							//PubsubService.timeMap.remove(userJID);
							iter.remove();
						}
					}
					catch(Exception e) {
						e.printStackTrace();
						Log.info("the time check thread error!");
						Log.info(e.toString());
						PubsubService.timeMap.clear();
						break;
					}
				}
			}
		}
	}
}
