/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.bgp4j.netty;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineException;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class MockChannelSink implements ChannelSink {
	@Inject Logger log;
	
	private List<ChannelEvent> events = new LinkedList<ChannelEvent>();
	
	@Override
	public void eventSunk(ChannelPipeline pipeline, ChannelEvent e) throws Exception {
		events.add(e);
		
		if(e instanceof MessageEvent)
			Channels.fireWriteComplete(e.getChannel(), 1);
	}

	@Override
	public void exceptionCaught(ChannelPipeline pipeline, ChannelEvent e,
			ChannelPipelineException cause) throws Exception {
		log.error("caught exception", cause);
		throw cause;
	}

	/**
	 * @return the events
	 */
	public List<ChannelEvent> getEvents() {
		return events;
	}


	@SuppressWarnings("unchecked")
	public <T extends ChannelEvent> T nextEvent() {
		if(!events.isEmpty())
			return (T)events.remove(0);
		else
			throw new IllegalStateException("empty event queue");
	}
	
	public int getWaitingEventNumber() {
		return this.events.size();
	}
}
