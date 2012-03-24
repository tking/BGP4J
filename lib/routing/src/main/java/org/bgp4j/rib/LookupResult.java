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
 * File: org.bgp4j.rib.LookupResult.java 
 */
package org.bgp4j.rib;

import java.util.Collection;

import org.bgp4j.net.NetworkLayerReachabilityInformation;
import org.bgp4j.net.NextHop;
import org.bgp4j.net.attributes.PathAttribute;

/**
 * Result bean for a NLRI prefix lookup in a RIB 
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class LookupResult {

	private NetworkLayerReachabilityInformation nlri;
	private Collection<PathAttribute> pathAttributes;
	private NextHop nextHop;
	
	LookupResult(NetworkLayerReachabilityInformation nlri, Collection<PathAttribute> pathAttributes, NextHop nextHop) {
		this.nlri = nlri;
		this.pathAttributes = pathAttributes;
		this.nextHop = nextHop;
	}

	/**
	 * @return the nlri
	 */
	public NetworkLayerReachabilityInformation getNlri() {
		return nlri;
	}

	/**
	 * @return the pathAttributes
	 */
	public Collection<PathAttribute> getPathAttributes() {
		return pathAttributes;
	}

	/**
	 * @return the nextHop
	 */
	public NextHop getNextHop() {
		return nextHop;
	}
}
