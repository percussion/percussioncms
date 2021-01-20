<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" > 
<xsl:template name="searchBox"> 
<form name="Admin_SearchResults" action="Admin_SearchResults.html" method="post">
  <table width="150" height="100%" cellspacing="0" cellpadding="4" border="0">
    <tr> 
      <td class="outerboxcell" valign="top"> 
        <table width="100%" cellpadding="0" cellspacing="0" border="0">
          <tr> 
            <td align="center" valign="top" colspan="2" width="150" class="outerboxcell"><span class="outerboxcellfont">Search</span></td>
          </tr>
          <tr> 
            <td class="backgroundcolour" valign="top"> 
              <table width="150" border="0" cellspacing="1" cellpadding="0" class="backgroundcolour">
                <tr class="outerboxcell"> 
                  <td colspan="2"> 
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr class="datacell1"> 
                        <td colspan="2" align="center" class="datacell1font">By 
                          Title</td>
                      </tr>
                      <tr class="datacell2"> 
                        <td colspan="2" align="center" class="datacell2font"> 
                          <input type="text" size="9" name="title" class="datadisplay" />
                        </td>
                      </tr>
                      <tr class="datacell1"> 
                        <td colspan="2" align="center" class="datacell1font">By 
                          Published Date</td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <tr> 
                  <td class="headercell" align="center"><span class="headercellfont">Before:</span></td>
                  <td class="outerboxcell"> 
                    <input type="text" name="enddate" size="4" class="datadisplay" />
                    &#160; <a href="javascript:onmousedown=getMonth_and_Date(document.Admin_SearchResults,'enddate');putcal(document.Admin_SearchResults,'enddate')"> 
                    <img height="20" alt="Calendar Pop-up" src="../rx_resources/images/cal.gif" width="20" border="0" /></a> 
                  </td>
                </tr>
                <tr> 
                  <td align="center" class="headercell"><span class="headercellfont">After:</span></td>
                  <td class="outerboxcell"> 
                    <input type="text" name="startdate" size="4" class="datadisplay" />
                    &#160; <a href="javascript:onmousedown=getMonth_and_Date(document.Admin_SearchResults,'startdate');putcal(document.Admin_SearchResults,'startdate')"> 
                    <img height="20" alt="Calendar Pop-up" src="../rx_resources/images/cal.gif" width="20" border="0" /></a> 
                  </td>
                </tr>
                <tr class="datacell2"> 
                  <td colspan="2" align="center"> <br/>
                    <input type="button" name="Submit" value="Search" class="nav_body" onClick="return Submit_onclick()" />
                    &#160; 
                    <input type="reset" name="Reset" value="Reset" class="nav_body" />
                    <br />
                    &#160; </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>
    <tr> 
      <td height="100%" class="outerboxcell">&nbsp;</td>
      <!-- Fill down to the bottom -->
    </tr>
  </table>
</form>
</xsl:template> </xsl:stylesheet> 
