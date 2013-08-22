/*
 * The MIT License
 *
 * Copyright (c) 2013, Cisco Systems, Inc., a California corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.buildflow.concurrent.extension;

import com.cloudbees.plugins.flow.BuildFlow;
import com.cloudbees.plugins.flow.FlowDelegate;
import hudson.Extension;
import hudson.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConcurrentBuildFlowExtensionPlugin provides some DSL extensions to the BuildFlowPlugin.
 */
public class ConcurrentBuildFlowExtensionPlugin extends Plugin {

	@Extension
	public static final ConcurrentBuildFlowExtensionPlugin INSTANCE = new ConcurrentBuildFlowExtensionPlugin();

	private static final Logger logger = Logger.getLogger(ConcurrentBuildFlowExtensionPlugin.class.getName());

	public ConcurrentBuildFlowExtensionPlugin() {
	}

	/**
	 * BuildFlow at the moment doesn't support restart - so there is no point saving this.
	 * NB: in the future we will need to be careful what we save here also as a Lock although serializable will not be
	 * held by the
	 * FlowRun on de-serialisation
	 */
	private transient Map<BuildFlow, Map<String, Object>> sharedState = new HashMap<BuildFlow, Map<String, Object>>();

	/**
	 * Gets (or saves if it doesn't exist) the shared state for the delegates BuildFlow project with the given key.
	 *
	 * @param flowDelegate the flowdelegate whose BuildFlow contains the shared state.
	 * @param key          the key to use for the lookup.
	 * @param defaultValue the default value to set (and return) if the value has not yet been set.
	 *                     Care should be taken with the type of objects set in this map,
	 *                     for example although a @{link java.util.ReentrantLock ReentrantLock} may be used after a
	 *                     restart of Jenkins no lock would be held.
	 * @return the current value of the shared state for the given key.
	 */
	public Object getSharedState(FlowDelegate flowDelegate, String key, Object defaultValue) {
		synchronized (sharedState) {
			Map<String, Object> flowMap = getOrSet(sharedState, flowDelegate.getFlowRun().getBuildFlow(),
			                                      new HashMap<String, Object>());
			Object retVal = getOrSet(flowMap, key, defaultValue);
			logWithIdentity("returning value with identity {0} for key {1}", retVal, key);
			return retVal;
		}
	}

	private <K, T> T getOrSet(Map<K, T> map, K key, T defaultIfNotSet) {
		T retVal = map.get(key);
		if (retVal == null) {
			map.put(key, defaultIfNotSet);
			retVal = defaultIfNotSet;
			logWithIdentity("map does not contain an entry for {1} returning default ({0})", retVal, key);
		}
		else {
			logWithIdentity("returning previous stored value for {1} ({0})", retVal, key);
		}
		return retVal;
	}

	private void logWithIdentity(String format, Object identity, Object arg) {
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, format, new Object[] {System.identityHashCode(identity), arg});
		}
	}
	/**
	 * Removed any shared state that is associated to the flow.
	 * @param flow the flow whose state should be removed
	 */
	void removeSharedState(BuildFlow flow) {
		synchronized (sharedState) {
			sharedState.remove(flow);
		}
	}
}
