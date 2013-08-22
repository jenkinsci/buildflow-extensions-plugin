package org.jenkinsci.plugins.buildflow.concurrent.extension;

/**
 * Created with IntelliJ IDEA.
 * User: NordJ
 * Date: 21/08/13
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
public class IdentityTestHelper {

	public static Object returnSameObject(Object obj) {
		System.out.println("returnSameObject called with object with identity" + System.identityHashCode(obj));
		return obj;
	}
}
