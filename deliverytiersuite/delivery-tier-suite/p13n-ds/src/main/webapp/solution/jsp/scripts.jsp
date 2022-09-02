<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<script type="text/javascript" 
	src="${scripts}/jquery.min.js"></script>
<script type="text/javascript" 
	src="${scripts}/jquery-treeview/lib/jquery.cookie.js"></script>
<script type="text/javascript" 
	src="${scripts}/jquery-treeview/jquery.treeview.js"></script>
<script type="text/javascript" 
	src="${scripts}/jquery-form/jquery.form.js"></script>
<c:choose>
	<c:when test="${empty param.debug}">
		<script type="text/javascript" 
			src="${scripts}/log4javascript/log4javascript_stub.js"></script>
	</c:when>
	<c:otherwise>
		<script type="text/javascript" 
			src="${scripts}/log4javascript/log4javascript.js"></script>
	</c:otherwise>
</c:choose>
<script type="text/javascript" 
	src="${scripts}/p13n/perc_p13n_profile.js"></script>

<script type="text/javascript">
	var p13nInsideRhythmyx = ${insideRx};
</script>