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

import com.cloudbees.plugins.flow.BuildFlow
import hudson.model.ParametersDefinitionProperty
import hudson.model.Result
import hudson.model.RunParameterDefinition
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.jvnet.hudson.test.JenkinsRule

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class BlockTest {

	@Rule
	public JenkinsRule j = new JenkinsRule()

	@Rule
	public TestName name = new TestName()

	@Test
	void testSingleBlock() {
		// we need enough executors that we can run all jobs concurrently.
		j.instance.numExecutors = 8
		j.instance.reload()

		File f1 = new File("job1.block")
		// this will prevent firstJob from completing.
		f1.createNewFile()

		File f2 = new File("job2.block")
		// this will prevent the secondJob from completing.
		f2.createNewFile()

		def job1 = createParameterisedBlockingConcurrentJob("job1", f1)
		def job2 = createParameterisedBlockingConcurrentJob("job2", f2)

		BuildFlow flow = new BuildFlow(j.instance, name.getMethodName())
		flow.concurrentBuild = true

		flow.dsl = """
		       def concExt = extension.'build-flow-concurrent-extensions'

             build("job1", param1:build)
             concExt.block("TEST") {
                build("job2", param1:build)
             }
        """
		def sfr1 = flow.scheduleBuild2(0)
		def fr1 = sfr1.waitForStart()

		def sfr2 = flow.scheduleBuild2(0)
		def fr2 = sfr2.waitForStart()

		def sfr3 = flow.scheduleBuild2(0)
		def fr3 = sfr3.waitForStart()

		// wait for all job1s to be running (rather than just in the queue..
		while (job1.builds.size() != 3) {
			Thread.sleep(10L);
		}

		// release job1
		// need to make sure that job1 is actually started and blocking...
		println("releasing job 1")
		f1.delete()
		// wait for all job1's to complete
		while (true) {
			if (job1.inQueue || job1.building) {
				Thread.sleep(10L)
			} else {
				break
			}
		}

		while (job2.builds.size() < 1) {
			Thread.sleep(10L);
		}

		println("releasing job 2")
		f2.delete()

		// wait for all the flows to finish.
		sfr1.get()
		sfr2.get()
		sfr3.get()

		println("fr1 log:")
		println(fr1.getLog(Integer.MAX_VALUE))
		println("fr2 log:")
		println(fr2.getLog(Integer.MAX_VALUE))
		println("fr3 log:")
		println(fr3.getLog(Integer.MAX_VALUE))

		println("fr1: ${fr1}  status ${fr1.result}")
		println("fr2: ${fr2}  status ${fr2.result}")
		println("fr3: ${fr3}  status ${fr3.result}")

		println("job1")
		println(job1.builds)
		println("job2")
		println(job2.builds)

		println("flow")
		println(flow.builds)

		assertEquals("job1 should have 3 runs", 3, job1.builds.size())
		// there is a slight timing issue in that the block for entering job2
		// may have entered before any other FlowRuns enter the run
		// depending on how fast the machine is.
		// However this should always result in only 1 or 2 builds for job2
		// and never 3.
		assertTrue("job2 should have 1 or 2 runs", job2.builds.size() > 0 && job2.builds.size() < 3);

		assertEquals("job2 last run should only have a single cause", 1, job2.builds.lastBuild.causes.size())
		assertEquals("job2 last trigger should be third flow run", flow.builds.lastBuild, job2.builds.lastBuild.causes[0]
				.flowRun)
		assertEquals("FlowRun#2 should have been aborted.", Result.ABORTED, fr2.getResult());
		assertEquals("FlowRun#3 should have completed successfully.", Result.SUCCESS, fr3.getResult());
	}

	def createParameterisedBlockingConcurrentJob(String name, File file) {
		def proj = j.createFreeStyleProject(name)
		proj.getBuildersList().add(new BlockingBuilder(file))
		proj.concurrentBuild = true
		proj.properties.put(ParametersDefinitionProperty.DescriptorImpl,
				new ParametersDefinitionProperty(new RunParameterDefinition("param1", proj.fullName, "-1",
						"ignored description")))
		return proj
	}
}
