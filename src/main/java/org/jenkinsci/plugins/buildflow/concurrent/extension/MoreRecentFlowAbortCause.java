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

import com.cloudbees.plugins.flow.FlowRun;
import jenkins.model.CauseOfInterruption;

/**
 * The cause of interruption when we abort the build if a more recent flow requests the same block.
 *
 * @author James Nord
 */
public class MoreRecentFlowAbortCause extends CauseOfInterruption {

	/**
	 * The number of the later build.
	 */
	private int newer;
	/**
	 * The block that was being entered.
	 */
	private String block;

	/**
	 * Construct a new MoreRecentFlowAbortCause to specify the reason for the abort was due to the fact that a
	 * more recent FlowRun was attempting to enter the same block.
	 *
	 * @param newer the FlowRun that is later than ours that was also attempting to enter the block
	 * @param block the block that was attempted to enter
	 */
	public MoreRecentFlowAbortCause(FlowRun newer, String block) {
		this.newer = newer.getNumber();
		this.block = block;
	}

	@Override
	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("a more recent FlowRun build#");
		sb.append(newer);
		sb.append(" was requesting the same block (");
		sb.append(block);
		sb.append(")");
		return sb.toString();
	}
}
