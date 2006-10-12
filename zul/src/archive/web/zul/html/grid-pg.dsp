<%--
gridpg.dsp

{{IS_NOTE
	Purpose:
		Grid for mold = paging
	Description:
		
	History:
		Mon Aug 21 14:43:31     2006, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/zk/core.dsp.tld" prefix="z" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<div id="${self.uuid}" z:type="zul.grid.Grid"${self.outerAttrs}${self.innerAttrs}>
	<div id="${self.uuid}!paging" class="grid-paging">
	<table width="100%" border="0" cellpadding="0" cellspacing="0" class="grid-btable">
	<tbody>
	${z:redraw(self.columns, null)}
	</tbody>
	${z:redraw(self.rows, null)}
	</table>
	</div>
	${z:redraw(self.paging, null)}
</div>
