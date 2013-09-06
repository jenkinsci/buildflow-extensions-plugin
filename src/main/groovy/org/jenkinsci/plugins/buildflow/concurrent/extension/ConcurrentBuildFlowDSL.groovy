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
package org.jenkinsci.plugins.buildflow.concurrent.extension

import com.cloudbees.plugins.flow.FlowDelegate
import com.cloudbees.plugins.flow.FlowRun
import hudson.AbortException
import hudson.model.Executor
import hudson.model.Result
import hudson.model.Run

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

import java.util.TreeSet
import java.util.SortedSet

public class ConcurrentBuildFlowDSL {

	def FlowDelegate flowDelegate;

	def ConcurrentBuildFlowDSL(FlowDelegate flowDelegate) {
		this.flowDelegate = flowDelegate;
	}

	/**
	 * The block DSL ensures that only one FlowRun of a particular BuildFlow will execute the steps contained within it concurrently.
	 * It further will only allow the most recent FlowRun to proceed if multiple become blocked.
	 * It is expected that the blockName will only be used once in a Flow - using the blockname multiple times will work (however the resul
	 * may not be what the user intended).
	 * @return <code>true</code>ue if the block was executed, <code>false</code> if the block was nor run.
	 */
	def boolean block(String blockName, innerBlock) {

		// due to the nature of any jobs and scheduling the order of items inserted may not be
		// ordered by build number so use an explicit comparator.
		SortedSet waiting = getSharedState(blockName + "_WAITING_LIST",
		                            Collections.synchronizedSortedSet(new TreeSet()))
		ReentrantLock lock = getSharedState(blockName + "_LOCK", new ReentrantLock())

		waiting.add(flowDelegate.flowRun);
		// try and get the lock.
		try {
			flowDelegate.println("block ($blockName) {")
			++flowDelegate.indent
			flowDelegate.println("// waiting for lock")
			while (true) {
				boolean locked = lock.tryLock(1L, TimeUnit.SECONDS)
				FlowRun next = waiting.last();
				if (next != flowDelegate.flowRun) {
					flowDelegate.println("// [${flowDelegate.flowRun}] a more recent FlowRun ${next} is requesting the same" +
							"lock (${blockName}) - not progressing with the build")
					// if there is a newer waiting FlowRun then remove ourselves and abort
					waiting.remove(flowDelegate.flowRun)
					/*
					  // how do we abort (stop the flow progressing any further?) the build- shall we try to interrupt ourselves!
					  // should we return true/false and get the caller to handle it?
					  Thread.currentThread().interrupt();
					  break;
					  */
					if (locked) {
						lock.unlock()
					}
					def abortCause = new MoreRecentFlowAbortCause(next, blockName)
					Executor.currentExecutor().interrupt(Result.ABORTED, abortCause)
					throw new AbortException(abortCause.shortDescription)
				}
				if (locked) {
					flowDelegate.println("// block ($blockName) acquired by ${flowDelegate.flowRun}")
					// we know there is not a newer BuildFlow than us!
					// (actually there is a small race condition here - but not worth extra locking).
					innerBlock()
					waiting.remove(flowDelegate.flowRun)
					lock.unlock()
					return true
				}
			}
		}
		finally {
			--flowDelegate.indent;
			flowDelegate.println("}")
			waiting.remove(flowDelegate.flowRun)
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private getSharedState(key, value) {
		// Groovy and private don't mix - lets hope no-one calls this in their job dsl
		ConcurrentBuildFlowExtensionPlugin.INSTANCE.getSharedState(flowDelegate, key, value)
	}
}
