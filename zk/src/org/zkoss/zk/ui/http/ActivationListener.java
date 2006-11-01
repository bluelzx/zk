/* ActivationListener.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jul  6 10:56:48     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.http;

/**
 * A listener that will be invoked when the Web manager is created (aka.,
 * activated).
 *
 * <p>To register a listener, use {@link WebManager#addListener}.
 *
 * @author tomyeh
 */
public interface ActivationListener {
	/** Called after WebManager is created.
	 */
	public void onActivated(WebManager webman);
}
