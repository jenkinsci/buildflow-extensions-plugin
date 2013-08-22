package org.jenkinsci.plugins.buildflow.concurrent.extension

import com.cloudbees.plugins.flow.BuildFlow
import com.cloudbees.plugins.flow.FlowRun

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created with IntelliJ IDEA.
 * User: NordJ
 * Date: 21/08/13
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
class FlowRunComparatorTest {

	void testCompare() {
		FlowRun fr1 = createFlowRunMock(1);
		FlowRun fr2 = createFlowRunMock(2);
		FlowRun fr3 = createFlowRunMock(3);
		FlowRun fr4 = createFlowRunMock(4);

		assertThat(fr1, lessThanOrEqualTo(fr2));
	}

	private FlowRun createFlowRunMock(int buildNumber) {
		FlowRun fr = mock(FlowRun.class);
		when (fr.getNumber()).thenReturn(buildNumber);
		return fr;
	}
}
