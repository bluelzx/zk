/* AuScrollBy.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jul 28 15:39:34     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au;

/**
 * A response to ask the client to scroll the desktop (aka., the browser window)
 * relatively (in pixels).
 *
 * <p>data[0]: x<br/>
 * data[1]: y
 * 
 * @author tomyeh
 */
public class AuScrollBy extends AuResponse {
	public AuScrollBy(int x, int y) {
		super("scrollBy", new String[] {Integer.toString(x), Integer.toString(y)});
	}
}
