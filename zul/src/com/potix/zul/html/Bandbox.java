/* Bandbox.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Mar 20 12:14:46     2006, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package com.potix.zul.html;

import com.potix.lang.Objects;
import com.potix.xml.HTMLs;

import com.potix.zk.ui.Component;
import com.potix.zk.ui.UiException;
import com.potix.zk.ui.WrongValueException;
import com.potix.zk.ui.event.Events;
import com.potix.zk.au.AuScript;

/**
 * A band box. A bank box consists of an input box ({@link Textbox} and
 * a popup window {@link Bandpopup}.
 * It is similar to {@link Combobox} except the popup window could have
 * any kind of children. For example, you could place a textbox in
 * the popup to let user search particular items.
 *
 * <p>Default {@link #getSclass}: bandbox.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public class Bandbox extends Textbox { //note: it does NOT implement Openable to avoid redudant roundtrip
	private transient Bandpopup _drop;
	private String _img = "~./zul/img/bandbtn.gif";
	private boolean _autodrop;

	public Bandbox() {
		setSclass("bandbox");
	}
	public Bandbox(String value) throws WrongValueException {
		this();
		setValue(value);
	}

	/** Returns the dropdown window belonging to this band box.
	 */
	public Bandpopup getDropdown() {
		return _drop;
	}
	/** Closes the popup ({@link #getDropdown}).
	 */
	public void closeDropdown() {
		response("close", new AuScript(this, 
			"zkCmbox.close('" + getUuid() + "!pp',true)"));
	}

	/** Returns whether to automatically drop the list if users is changing
	 * this text box.
	 * <p>Default: false.
	 */
	public boolean isAutodrop() {
		return _autodrop;
	}
	/** Sets whether to automatically drop the list if users is changing
	 * this text box.
	 */
	public void setAutodrop(boolean autodrop) {
		if (_autodrop != autodrop) {
			_autodrop = autodrop;
			smartUpdate("zk_adr", autodrop);
		}
	}

	/** Returns the image URI that is displayed as the button to open
	 * {@link Bandpopup}.
	 * <p>Default: "~./zul/img/bandbtn.gif".
	 */
	public String getImage() {
		return _img;
	}
	/** Sets the image URI that is displayed as the button to open
	 * {@link Bandpopup}.
	 *
	 * @param img the image URI. If null or empty, it is reset to
	 * the default value: "~./zul/img/bandbtn.gif".
	 */
	public void setImage(String img) {
		if (img != null && img.length() == 0) img = "~./zul/img/bandbtn.gif";
		if (!Objects.equals(_img, img)) {
			_img = img;
			invalidate(INNER);
			//NOTE: Tom Yeh: 20051222
			//It is possible to use smartUpdate if we always generate
			//an image (with an ID) in getImgTag.
			//However, it is too costly by making HTML too big, so
			//we prefer to invalidate (it happens rarely)
		}
	}

	//-- super --//
	public void setMultiline(boolean multiline) {
		if (multiline)
			throw new UnsupportedOperationException("multiline");
	}
	public void setRows(int rows) throws WrongValueException {
		if (rows != 1)
			throw new UnsupportedOperationException("rows");
	}

	public String getOuterAttrs() {
		final String attrs = super.getOuterAttrs();
		final boolean adr = isAutodrop();
		if (!isAsapRequired(Events.ON_OPEN) && !adr)
			return attrs;

		final StringBuffer sb = new StringBuffer(64).append(attrs);
		appendAsapAttr(sb, Events.ON_OPEN);
		if (adr) HTMLs.appendAttribute(sb, "zk_adr", "true");
		return sb.toString();
	}
	public String getInnerAttrs() {
		final String attrs = super.getInnerAttrs();
		final String style = getInnerStyle();
		return style.length() > 0 ? attrs+" style=\""+style+'"': attrs;
	}
	private String getInnerStyle() {
		final StringBuffer sb = new StringBuffer(32)
			.append(HTMLs.getTextRelevantStyle(getRealStyle()));
		HTMLs.appendStyle(sb, "width", getWidth());
		HTMLs.appendStyle(sb, "height", getHeight());
		return sb.toString();
	}
	/** Returns RS_NO_WIDTH|RS_NO_HEIGHT.
	 */
	protected int getRealStyleFlags() {
		return super.getRealStyleFlags()|RS_NO_WIDTH|RS_NO_HEIGHT;
	}

	//-- Component --//
	public boolean insertBefore(Component newChild, Component refChild) {
		if (!(newChild instanceof Bandpopup))
			throw new UiException("Unsupported child for Bandbox: "+newChild);
		if (_drop != null)
			throw new UiException("At most one bandpopup is allowed, "+this);
		if (super.insertBefore(newChild, refChild)) {
			invalidate(INNER);
			_drop = (Bandpopup)newChild;
			return true;
		}
		return false;
	}
	/** Childable. */
	public boolean isChildable() {
		return true;
	}
	public void onChildRemoved(Component child) {
		super.onChildRemoved(child);
		if (child == _drop) _drop = null; //just in case
	}

	//Cloneable//
	public Object clone() {
		final Bandbox clone = (Bandbox)super.clone();
		if (clone._drop != null) clone.afterUnmarshal();
		return clone;
	}
	private void afterUnmarshal() {
		_drop = (Bandpopup)getChildren().get(0);
	}

	//Serializable//
	private synchronized void readObject(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		if (!getChildren().isEmpty()) afterUnmarshal();
	}
}
