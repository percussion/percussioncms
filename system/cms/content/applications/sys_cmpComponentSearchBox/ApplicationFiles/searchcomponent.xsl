<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
<xsl:output method="xml"/>
<xsl:template match="/"> 
<xsl:variable name="pagename" select="/*/pagename"/>
<html>
<head>
   <title>Rhythmyx System Administrator - Search Box</title>
</head>
<body>
<script>
<![CDATA[
function search_onclick() {
	if(document.componentsearch.cmpname_display.value!=''){
		document.componentsearch.cmpname.value = '%' + document.componentsearch.cmpname_display.value + '%';
	}
	if(document.componentsearch.componentdisplayname_display.value!=''){
		document.componentsearch.componentdisplayname.value = '%' + document.componentsearch.componentdisplayname_display.value + '%';
	}
	if(document.componentsearch.componenturl_display.value!=''){
		document.componentsearch.componenturl.value = '%' + document.componentsearch.componenturl_display.value + '%';
	}
	if(document.componentsearch.componentdesc_display.value!=''){
		document.componentsearch.componentdesc.value = '%' + document.componentsearch.componentdesc_display.value + '%';
	}
	document.componentsearch.componenttype.value = document.componentsearch.componenttype_display[document.componentsearch.componenttype_display.selectedIndex].value;
	document.componentsearch.componentid.value = document.componentsearch.componentid_display.value;
}
]]>
</script>
<form name="componentsearch" action="../sys_cmpComponents/components.html" method="get">
   <input type="hidden" name="sys_componentname" value="sys_compbyname"/>
	<input type="hidden" name="sys_pagename" value="sys_compbyname"/>
   <input type="hidden" name="cmpname" value=""/>
   <input type="hidden" name="componentid" value=""/>
   <input type="hidden" name="componentdesc" value=""/>
   <input type="hidden" name="componenturl" value=""/>
   <input type="hidden" name="componenttype" value=""/>
   <input type="hidden" name="componentdisplayname" value=""/>
  <table width="225" height="100%" cellspacing="0" cellpadding="0" border="0">
    <tr> 
         <td align="center" class="outerboxcellfont">&nbsp;
         </td>
    </tr>
    <tr> 
         <td align="center" class="outerboxcellfont">Search
         </td>
    </tr>
    <tr>
      <td valign="top"> 
        <table width="100%" cellpadding="0" cellspacing="0" border="0">
             <tr class="datacell1" border="0"> 
                 <td align="center" class="datacell1font" >By Name</td>
             </tr>
             <tr class="datacell1"  border="0"> 
               <td align="center" class="datacell2font"> 
                 <input type="text" size="20" name="cmpname_display" class="monospace"/>
               </td>
             </tr>
             <tr class="datacell1" border="0"> 
                 <td align="center" class="datacell1font" >By Display Name</td>
             </tr>
             <tr class="datacell1"  border="0"> 
               <td align="center" class="datacell2font"> 
                 <input type="text" size="20" name="componentdisplayname_display" class="monospace"/>
               </td>
             </tr>
             <tr class="datacell1" border="0"> 
                 <td align="center" class="datacell1font" >By Description</td>
             </tr>
             <tr class="datacell1"  border="0"> 
               <td align="center" class="datacell2font"> 
                 <input type="text" size="20" name="componentdesc_display" class="monospace"/>
               </td>
             </tr>
             <tr class="datacell1" border="0"> 
                 <td align="center" class="datacell1font" >By URL</td>
             </tr>
             <tr class="datacell1"  border="0"> 
               <td align="center" class="datacell2font"> 
                 <input type="text" size="20" name="componenturl_display" class="monospace"/>
               </td>
             </tr>
             <tr class="datacell1" border="0"> 
                 <td align="center" class="datacell1font" >By ID</td>
             </tr>
             <tr class="datacell1"  border="0"> 
               <td align="center" class="datacell2font"> 
                 <input type="text" size="20" name="componentid_display" class="monospace"/>
               </td>
             </tr>
             <tr class="datacell1" border="0"> 
                 <td align="center" class="datacell1font" >By Component Type</td>
             </tr>
             <tr class="datacell1"  border="0"> 
               <td align="center" class="datacell2font"> 
                 <select name="componenttype_display">
						<option value="">&nbsp;</option>
						<option value="1">Page Component</option>
						<option value="2">Page</option>
					  </select>
               </td>
             </tr>
				 <tr class="headercell2"> 
               <td align="center" class="headercell2font"> <br/>
                 <input type="submit" name="Submit" value="Search" class="nav_body" onclick="return search_onclick();" />
                 &#160; 
                 <input type="reset" name="Reset" value="Reset" class="nav_body" />
                 <br />
                 &#160; </td>
            </tr>
        </table>
      </td>
    </tr>
    <tr> 
      <td height="100%" class="outerboxcell">
      &nbsp;
      </td>
      <!-- Fill down to the bottom -->
    </tr>
  </table>
</form>
<script>
	parseAndDisplayFormFiledsFromUrl('componentsearch');
</script>
</body>
</html>
</xsl:template> 
</xsl:stylesheet> 
