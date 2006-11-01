/* RenderCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sun Oct  2 14:24:42     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.impl;

import java.util.Set;

import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.ext.RenderOnDemand;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;

/**
 * Used only by {@link AuRequest} to implement the render
 * relevant command.
 * 
 * @author tomyeh
 */
public class RenderCommand extends Command {
	public RenderCommand(String evtnm, int flags) {
		super(evtnm, flags);
	}

	//-- super --//
	protected void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);

		final Set items = Commands.convertToItems(request);
		((RenderOnDemand)comp).renderItems(items);
			//Note: it is not part of getExtraCtrl, since it is accessible
			//to app developers, too.
	}
}
