<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see https://www.gnu.org/licenses/
  -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
		  xmlns:c="http://java.sun.com/jsp/jstl/core"
		  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
		  xmlns:rxcomp="http://rhythmyx.percussion.com/components"
		  xmlns:rxb="urn:jsptagdir:/WEB-INF/tags/banner"
		  version="1.2">
	<div id="RhythmyxBanner">
		<table cellspacing='0' cellpadding='0' border='0' class="rx-banner-table">

			<tr class="rx-banner-row">
				<td valign="bottom"><rxb:tabs/></td>
				<c:if test="${rxcomp:hasComponentRole('cmp_banner','ContentRole')}">
					<td align="left" valign="top">
						<a href="../../dce/dce.jnlp">Desktop Content Explorer</a>
					</td>
				</c:if>
				<td align="right" valign="bottom">
					<rxb:status/>
				</td>
			</tr>
		</table>
	</div>
</jsp:root>