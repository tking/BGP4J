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
 * File: org.bgp4j.netty.handlers.InboundOpenCapabilitiesProcessorTest.java 
 */
package org.bgp4j.netty.handlers;

import java.util.UUID;

import junit.framework.Assert;

import org.bgp4j.net.AddressFamily;
import org.bgp4j.net.SubsequentAddressFamily;
import org.bgp4j.net.capabilities.AutonomousSystem4Capability;
import org.bgp4j.net.capabilities.MultiProtocolCapability;
import org.bgp4j.netty.BGPv4Constants;
import org.bgp4j.netty.LocalChannelBGPv4TestBase;
import org.bgp4j.netty.MessageRecordingChannelHandler;
import org.bgp4j.netty.protocol.open.BadPeerASNotificationPacket;
import org.bgp4j.netty.protocol.open.OpenPacket;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class InboundOpenCapabilitiesProcessorTest extends LocalChannelBGPv4TestBase {

	@Before
	public void before() {
		messageRecorder = obtainInstance(MessageRecordingChannelHandler.class);
		
		LocalAddress codecOnlyAddress = new LocalAddress(UUID.randomUUID().toString());
		
		serverBootstrap = buildLocalServerBootstrap(new ChannelPipelineFactory() {
			
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new ChannelHandler[] { 
						obtainInstance(InboundOpenCapabilitiesProcessor.class), 
						messageRecorder });
			}
		});
		serverChannel = serverBootstrap.bind(codecOnlyAddress);

		clientBootstrap = buildLocalClientBootstrap(Channels.pipeline(new ChannelHandler[] {
				messageRecorder 
				}));
		clientChannel = clientBootstrap.connect(codecOnlyAddress).getChannel();		
	}
	
	@After
	public void after() {
		if(clientChannel != null)
			clientChannel.close();
		if(serverChannel != null)
			serverChannel.close();
		clientChannel = null;
		clientBootstrap.releaseExternalResources();
		clientBootstrap = null;
		serverBootstrap.releaseExternalResources();
		serverBootstrap = null;
	}

	private MessageRecordingChannelHandler messageRecorder;
	
	private ServerBootstrap serverBootstrap;
	private ClientBootstrap clientBootstrap;
	private Channel clientChannel;
	private Channel serverChannel;
	
	@Test
	public void testTwoOctetASNumberNoASCap() throws Exception {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(64172);
		
		clientChannel.write(open);
		
		Assert.assertEquals(0, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		OpenPacket consumed = safeDowncast(safeExtractChannelEvent(messageRecorder.nextEvent(serverChannel)), OpenPacket.class);

		Assert.assertEquals(64172, consumed.getAutonomousSystem());	
	}

	@Test
	public void testFourOctetASNumberAS4Cap() throws Exception {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(BGPv4Constants.BGP_AS_TRANS);
		open.getCapabilities().add(new AutonomousSystem4Capability(641723));
		
		clientChannel.write(open);
		
		Assert.assertEquals(0, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		OpenPacket consumed = safeDowncast(safeExtractChannelEvent(messageRecorder.nextEvent(serverChannel)), OpenPacket.class);

		Assert.assertEquals(BGPv4Constants.BGP_AS_TRANS, consumed.getAutonomousSystem());	
		
		AutonomousSystem4Capability as4cap = consumed.findCapability(AutonomousSystem4Capability.class);
		
		Assert.assertNotNull(as4cap);
		Assert.assertEquals(641723, as4cap.getAutonomousSystem());
	}
	
	@Test
	public void testTwoOctetASNumberMatchingASCap() throws Exception {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(64172);
		open.getCapabilities().add(new AutonomousSystem4Capability(64172));
		
		clientChannel.write(open);
		
		Assert.assertEquals(0, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		OpenPacket consumed = safeDowncast(safeExtractChannelEvent(messageRecorder.nextEvent(serverChannel)), OpenPacket.class);

		Assert.assertEquals(64172, consumed.getAutonomousSystem());
		
		AutonomousSystem4Capability as4cap = consumed.findCapability(AutonomousSystem4Capability.class);
		
		Assert.assertNotNull(as4cap);
		Assert.assertEquals(64172, as4cap.getAutonomousSystem());
	}
	
	@Test
	public void testTwoOctetASNumberMismatchingASCap() throws Exception {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(64172);
		open.getCapabilities().add(new AutonomousSystem4Capability(64173));
		
		clientChannel.write(open);
		
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		Assert.assertEquals(BadPeerASNotificationPacket.class, safeExtractChannelEvent(messageRecorder.nextEvent(clientChannel)).getClass());
		assertNotificationEvent(BadPeerASNotificationPacket.class, messageRecorder.nextEvent(serverChannel));
	}

	@Test
	public void testTwoOctetASNumberMismatchingAS4Cap() throws Exception {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(64172);
		open.getCapabilities().add(new AutonomousSystem4Capability(641720));
		
		clientChannel.write(open);
		
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		Assert.assertEquals(BadPeerASNotificationPacket.class, safeExtractChannelEvent(messageRecorder.nextEvent(clientChannel)).getClass());
		assertNotificationEvent(BadPeerASNotificationPacket.class, messageRecorder.nextEvent(serverChannel));
	}
	
	public void testIPv4MissingCapabilty() {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(64172);

		clientChannel.write(open);
		
		Assert.assertEquals(0, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		OpenPacket consumed = safeDowncast(safeExtractChannelEvent(messageRecorder.nextEvent(serverChannel)), OpenPacket.class);

		Assert.assertTrue(consumed.getCapabilities().contains(new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING)));
	}
	
	public void testIPv4AvailableCapabilty() {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(64172);
		open.getCapabilities().add(new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING));
		
		clientChannel.write(open);
				
		Assert.assertEquals(0, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		OpenPacket consumed = safeDowncast(safeExtractChannelEvent(messageRecorder.nextEvent(serverChannel)), OpenPacket.class);

		Assert.assertTrue(consumed.getCapabilities().contains(new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING)));
	}

	
	public void testIPv4AnycastAvailableCapabilty() {
		OpenPacket open = new OpenPacket();
		
		open.setAutonomousSystem(64172);
		open.getCapabilities().add(new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_MULTICAST_FORWARDING));
		
		clientChannel.write(open);
				
		Assert.assertEquals(0, messageRecorder.getWaitingEventNumber(clientChannel));
		Assert.assertEquals(1, messageRecorder.getWaitingEventNumber(serverChannel));
	
		OpenPacket consumed = safeDowncast(safeExtractChannelEvent(messageRecorder.nextEvent(serverChannel)), OpenPacket.class);

		Assert.assertTrue(consumed.getCapabilities().contains(new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING)));
		Assert.assertTrue(consumed.getCapabilities().contains(new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_MULTICAST_FORWARDING)));
	}
}
