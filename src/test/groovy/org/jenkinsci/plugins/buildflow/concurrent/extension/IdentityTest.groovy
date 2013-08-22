package org.jenkinsci.plugins.buildflow.concurrent.extension

import org.junit.Assert
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: NordJ
 * Date: 21/08/13
 * Time: 17:08
 * To change this template use File | Settings | File Templates.
 */
class IdentityTest {

	@Test
	public void checkThatObjectsReturnedFromJavaAreIdentical() {
		TreeSet ts1 = new TreeSet()
		TreeSet ts2 = IdentityTestHelper.returnSameObject(ts1)
		System.out.println("ts1 identity : " + System.identityHashCode(ts1))
		System.out.println("ts2 identity : " + System.identityHashCode(ts2))
		Assert.assertSame("objects not identical", ts1, ts2)
	}
}
