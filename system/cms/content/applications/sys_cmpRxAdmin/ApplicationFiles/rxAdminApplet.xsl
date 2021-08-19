<?xml version='1.0' encoding='UTF-8'?>
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
<xsl:output method="html"/>
<xsl:template match="/">
<html>
<body bgcolor="#FFFFFF"  marginwidth="0" marginheight="0" text="#000000" link="#003399" vlink="#009999" alink="#0000FF">

<head>
<title>Percussion Rhythmyx Administration Console</title>
<script language="javaScript1.2" src="../sys_resources/js/browser.js"></script>
</head>

<table width="60%" height="60%" border="1" align="center" valign="middle" bgcolor="#9999CC" bordercolorlight="#000000" bordercolordark="#FFFFFF">
  <tr align="center">
    <td width="100%" align="center">
      <b><i><font size="5" color="darkblue">Percussion Rhythmyx Administration Console</font></i></b>
    </td>
  </tr>
  <tr align="center">
    <td width="100%" align="center">


	     <script language="JavaScript1.2"><![CDATA[   
	        
	        var _codebase = "]]><xsl:value-of select="//@codebase"/><![CDATA[";
	        var _classid = "]]><xsl:value-of select="//@classid"/><![CDATA[";
	        var _type = "]]><xsl:value-of select="concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)"/><![CDATA[";
	        var _pluginpage = "]]><xsl:value-of select="concat('http://java.sun.com/products/plugin/',//@implementation_version,'/plugin-install.html')"/><![CDATA[";
	        

	        var appletCaller = new AppletCaller();

	        appletCaller.addParam("name", "PSServerAdminApplet");
	        appletCaller.addParam("id", "PSServerAdminApplet");
	        appletCaller.addParam("width", "300");
	        appletCaller.addParam("height", "200");
	        appletCaller.addParam("hspace", "0");
	        appletCaller.addParam("vspace", "0");
	        appletCaller.addParam("align", "middle");
	        appletCaller.addParam("codebase", "../Administration");
	        appletCaller.addParam("archive", "jsse.jar,jcert.jar,jnet.jar,commons-lang-2.4.jar,commons-codec-1.11.jar,nis.jar,rxutils.jar,rxmisctools.jar,rxservices.jar,rhythmyx.jar,percbeans.jar,rxclient.jar,xmlParserAPIs.jar,xercesImpl.jar,saxon.jar,rxtablefactory.jar,jh.jar,help.jar,serveruicomp.jar,log4j.jar");
	        appletCaller.addParam("code", "com.percussion.E2Designer.admin.PSServerAdminApplet");
	        appletCaller.addParam("classid", _classid);
	        appletCaller.addParam("codebaseattr", _codebase);
	        appletCaller.addParam("type", _type);
	        appletCaller.addParam("scriptable", "true");
	        appletCaller.addParam("pluginspage", _pluginpage);
	        appletCaller.addParam("helpset_file", "../Docs/Rhythmyx/Server_Administrator/Rhythmyx_Server_Administrator.hs");
	        appletCaller.show();


    ]]></script>
    </td>
  </tr>
</table>

</body>
</html>
</xsl:template>
</xsl:stylesheet>

