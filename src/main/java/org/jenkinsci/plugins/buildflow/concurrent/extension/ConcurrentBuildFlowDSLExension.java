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

import com.cloudbees.plugins.flow.BuildFlowDSLExtension;
import com.cloudbees.plugins.flow.FlowDelegate;
import hudson.Extension;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConcurrentBuildFlowDSL implements the DSL extensions of the BuildFlow plugin.
 */
@Extension
public class ConcurrentBuildFlowDSLExension extends
                BuildFlowDSLExtension {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public Object createExtension(String s, FlowDelegate flowDelegate) {
		// This will be called for every flow run when an extension is looked up
		logger.log(Level.FINE, "createExtension({0}, {1}) called.", new Object[] {s,
		                                                                         flowDelegate.getFlowRun()
		                                                                         .getFullDisplayName()});
		if ("build-flow-concurrent-extensions".equals(s)) {
			logger.log(Level.FINE, "returning new Block");
			return new ConcurrentBuildFlowDSL(flowDelegate);
		}
		logger.log(Level.FINE, "Ignoring as not our extension");
		return null;
	}

}
