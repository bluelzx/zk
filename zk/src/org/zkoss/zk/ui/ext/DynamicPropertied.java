/* DynamicPropertied.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Oct  3 22:03:28     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.ext;

import org.zkoss.zk.ui.WrongValueException;

/**
 * Represents a component that supports a range of properties.
 * It is used to simplify the design of a component, such that
 * developers need to implement member functions (setter and getter)
 * for each property it supports.
 *
 * @author tomyeh
 */
public interface DynamicPropertied {
	/** Returns whether a dynamic attribute is allowed.
	 */
	public boolean hasDynamicProperty(String name);
	/** Returns the property value of the specified name.
	 */
	public Object getDynamicProperty(String name);
	/** Sets an attribute with a value.
	 *
	 * <p>If a component supports only String-type values, it could
	 * use org.zkoss.lang.Objects.toString() to convert the value
	 * to a String instance.
	 */
	public void setDynamicProperty(String name, Object value)
	throws WrongValueException;
}
