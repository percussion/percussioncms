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
