<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
  <xsl:variable name="this" select="/"/>
  <xsl:variable name="CreateContentlookup" select="/*/contenttypesurl"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="Percussion XSpLit Version 3.5"/>
        <meta http-equiv="Content-Type" content="text/html; UTF-8"/>
        <meta name="generator" content="Percussion XSpLit Version 3.5"/>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
        <title>Rhythmyx - Content Search</title>
        <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
        <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
        <script src="../sys_resources/js/calPopup.js" language="javascript">
          <![CDATA[;]]></script>
        <script id="clientEventHandlersJS" language="javascript" >
<![CDATA[
   var fromSearch = 0; 
   function Search_onclick() 
   {
      fromSearch = 0;
      if (document.forms[0].sys_contenttype[document.forms[0].sys_contenttype.selectedIndex].value=="null")
      {
         alert("Please select a content type!")
         return;
      }

      var str = "";
      if(document.forms[0].sys_author.value!=""){
			document.forms[0].sys_author.value = "%25" + document.forms[0].sys_author.value + "%25";
		}
      if(document.forms[0].sys_contenttitle.value!=""){
			document.forms[0].sys_contenttitle.value="%25"+document.forms[0].sys_contenttitle.value+"%25";
		}
	for(var i=0; i<document.forms[0].length; i++)
      {

         if(document.forms[0][i].type == "text" || document.forms[0][i].type == "select-one")
         { 
         // special for Netscape:
              if(document.forms[0][i].name == "sys_contenttype")
              {
                 str += document.forms[0][i].name + "=" + document.forms[0].sys_contenttype[document.forms[0].sys_contenttype.selectedIndex].value+ "&";
              } 
              else
              {
                  str += document.forms[0][i].name + "=" + document.forms[0][i].value + "&";      
              }
         }
       }
      fromSearch = 1;
         window.returnValue = str;
          this.location.href =  top.opener.INLINE_RETURN_PAGE + str;

   }

   function Reset_onclick() 
   { 
      document.forms[0].sys_contenttitle.value=""
      document.forms[0].sys_author.value=""
      document.forms[0].sys_contenttype.value="null"
      document.forms[0].sys_enddate.value=""
      document.forms[0].sys_startdate.value=""
		fromSearch = 0;
   }

   function Cancel_onclick() 
   {
		if(fromSearch!=1){
			window.returnValue = "cancel";
			self.close();
		} 
}




]]></script>
      </head>

      <body onload="javascript:self.focus()" onUnload="javascript:Cancel_onclick()">        
      <!--   psx-docalias="CreateContentlookup" psx-docref="psx-contenttypesurl"   -->
        <br xsplit="yes"/>
        <form name="updatesearch" method="post">
          <table align="center" width="75%" border="0" cellspacing="1" cellpadding="0" class="headercell">
            <tr class="headercell">
              <td colspan="2" align="left" class="headercellfont">Search Parameters</td>
            </tr>

            <xsl:apply-templates select="document($CreateContentlookup)/*" mode="mode2">
					<xsl:with-param name="ctype" select="/*/contenttype"/>
				</xsl:apply-templates>

            <tr class="datacell2">
              <td align="left" class="datacell1font">
                <span class="datacell1font">Author:</span>
              </td>
              <td class="datacell1font">
               <input size="15" type="text" name="sys_author" class="datacell1font">
                 <xsl:attribute name="value">
                   <xsl:value-of select="/*/author"/>
                 </xsl:attribute>
               </input>
              </td>
            </tr>

            <tr class="datacell1">
              <td align="left" class="datacell1font">
                <span class="datacell1font">Content Title:</span>
              </td>

              <td class="datacell1font">
               <input size="15" type="text" name="sys_contenttitle" class="datacell1font">
                 <xsl:attribute name="value">
                   <xsl:value-of select="/*/contenttitle"/>
                 </xsl:attribute>
               </input>
              </td>
            </tr>
            
            <tr class="datacell2">
              <td class="datacell1font" align="left">
                <span class="datacell1font">Created Before:</span>
              </td>
              <td class="datacell1font">
               <input type="text" name="sys_enddate" size="12" class="datacell1font">
                 <xsl:attribute name="value">
                   <xsl:value-of select="/*/enddate"/>
                 </xsl:attribute>
               </input>
                &nbsp;
                <a href="#" onclick="showCalendar(document.forms[0].sys_enddate);">
                  <img height="20" alt="Calendar Pop-up" src="../sys_resources/images/cal.gif" width="20" border="0"/>
                </a>
              </td>
            </tr>
            
            <tr class="datacell1">
              <td align="left" class="datacell1font">
                <span class="datacell1font">Created After:</span>
              </td>

              <td class="datacell1font">
               <input type="text" name="sys_startdate" size="12" class="datacell1font">
                 <xsl:attribute name="value">
                   <xsl:value-of select="/*/startdate"/>
                 </xsl:attribute>
               </input>
                &nbsp;
                <a href="#" onclick="showCalendar(document.forms[0].sys_startdate);">
                  <img height="20" alt="Calendar Pop-up" src="../sys_resources/images/cal.gif" width="20" border="0"/>
                </a>
              </td>
            </tr>
            
            <tr class="datacell2">
              <td colspan="2" align="center">
                <br id="XSpLit"/>
                <input type="button" name="Search" value="Search" language="javascript" onclick="return Search_onclick()"/>&nbsp;
                <input type="button" name="Cancel" value="Close" language="javascript" onclick="return Cancel_onclick()"/>
                <br id="XSpLit"/>&nbsp;
              </td>

            </tr>

          </table>

        </form>

      </body>

    </html>

  </xsl:template>

  <xsl:template match="*">
    <xsl:choose>
      <xsl:when test="text()">
        <xsl:choose>
          <xsl:when test="@no-escaping">
            <xsl:value-of select="." disable-output-escaping="yes"/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>

        </xsl:choose>

      </xsl:when>

      <xsl:otherwise>&nbsp;</xsl:otherwise>

    </xsl:choose>

    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

  <xsl:template match="attribute::*">
    <xsl:value-of select="."/>
    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>

  </xsl:template>

  <xsl:template match="item" mode="mode0">
      <option>
        <xsl:attribute name="value">
          <xsl:value-of select="id"/>
        </xsl:attribute>
        <xsl:value-of select="display"/>
      </option>
  </xsl:template>

  <xsl:template match="*" mode="mode2">
  <xsl:param name="ctype"/>
      <tr class="datacell1">
        <td class="datacell1font" align="left">
          <span class="datacell1font">Content Type:</span>
        </td>
        <td class="datacell1font">
         <select name="sys_contenttype" class="datacell1font">
			  <option value="null"><xsl:if test="$ctype=''"><xsl:attribute name="selected"/></xsl:if>-- Choose --</option>
			  <xsl:for-each select="item">
				<option>
				  <xsl:attribute name="value">
					 <xsl:value-of select="id"/>
				  </xsl:attribute>
					<xsl:if test="$ctype=id"><xsl:attribute name="selected"/></xsl:if>
				  <xsl:value-of select="display"/>
				</option>
			  </xsl:for-each>
         </select>
        </td>
      </tr>
  </xsl:template>

</xsl:stylesheet>
