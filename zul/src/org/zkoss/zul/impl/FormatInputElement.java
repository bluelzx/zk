/* FormatInputElement.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jul  5 09:27:34     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul.impl;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.zkoss.lang.Objects;
import org.zkoss.util.Locales;

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;

/**
 * An input box that supports format.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
abstract public class FormatInputElement extends InputElement {
	private String _format;

	/** Returns the format.
	 * <p>Default: null (used what is defined in the format sheet).
	 */
	public String getFormat() {
		return _format;
	}
	/** Sets the format.
	 */
	public void setFormat(String format) throws WrongValueException {
		if (!Objects.equals(_format, format)) {
			final String old = _format;
			_format = format;
			smartUpdate("z:fmt", getFormat());

			try {
				smartUpdate("value", getText());
				//Yes, the value attribute is changed! (no format attr in client)
			} catch (WrongValueException ex) {
				//ignore it (safe because it will keep throwing exception)
			}
		}
	}

	/** Formats a number (Integer, BigDecimal...) into a string.
	 * If null, an empty string is returned.
	 *
	 * <p>A utility to assist the handling of numeric data.
	 *
	 * @see #toNumberOnly
	 * @param defaultFormat used if {@link #getFormat} returns null.
	 * If defaultFormat and {@link #getFormat} are both null,
	 * the system's default format is used.
	 */
	protected String formatNumber(Object value, String defaultFormat) {
		if (value == null) return "";

		final DecimalFormat df = (DecimalFormat)
			NumberFormat.getInstance(Locales.getCurrent());
		String fmt = getFormat();
		if (fmt == null) fmt = defaultFormat;
		if (fmt != null) df.applyPattern(fmt);
		return df.format(value);
	}
	/** Filters out non digit characters, such comma and whitespace,
	 * from the specified value.
	 * It is used to parse a string to numeric data.
	 * @see #formatNumber
	 */
	protected String toNumberOnly(String val) {
		if (val == null) return val;

		final DecimalFormatSymbols symbols =
			new DecimalFormatSymbols(Locales.getCurrent());
		final char GROUPING = symbols.getGroupingSeparator(),
			DECIMAL = symbols.getDecimalSeparator(),
			PERCENT = symbols.getPercent(),
			MINUS = symbols.getMinusSign();
		final String fmt = getFormat();
		StringBuffer sb = null;
		for (int j = 0, len = val.length(); j < len; ++j) {
			final char cc = val.charAt(j);

			//We don't add if cc shall be ignored (not alphanum but in fmt)
			final boolean ignore = (cc < '0' || cc > '9')
				&& cc != DECIMAL && cc != MINUS && cc != '+' && cc != PERCENT
				&& (Character.isWhitespace(cc) || cc == GROUPING
					|| (fmt != null && fmt.indexOf(cc) >= 0));
			if (ignore) {
				if (sb == null)
					sb = new StringBuffer(len).append(val.substring(0, j));
			} else {
				final char c2 = cc == MINUS ? '-':
					cc == DECIMAL ? '.': cc == PERCENT ? '%': cc;
				if (cc != c2) {
					if (sb == null)
						sb = new StringBuffer(len).append(val.substring(0, j));
					sb.append(c2);
				} else if (sb != null) {
					sb.append(c2);
				}
			}
		}
		return sb != null ? sb.toString(): val;
	}

	//-- super --//
	public String getOuterAttrs() {
		final String attrs = super.getOuterAttrs();
		final String fmt = getFormat();
		return fmt != null && fmt.length() != 0 ?
			attrs + " z:fmt=\""+fmt+'"': attrs;
	}
	protected boolean isAsapRequired(String evtnm) {
		return (Events.ON_CHANGE.equals(evtnm) && getFormat() != null)
			|| super.isAsapRequired(evtnm);
	}
}
