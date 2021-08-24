<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
		<!ENTITY % w3centities-f PUBLIC
				"-//W3C//ENTITIES Combined Set//EN//XML"
				"http://www.w3.org/2003/entities/2007/w3centities-f.ent"
				>
		%w3centities-f;
		]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
	<xsl:include href="file:sys_Variants/copyvariant_body.xsl"/>
	<xsl:include href="file:sys_resources/stylesheets/viewpaging.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - System Administrator</title>
				<link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
				<script src="../sys_resources/js/formValidation.js"><![CDATA[
          ]]></script>
				<script id="clientEventHandlersJS" language="javascript"><![CDATA[
         function save_onclick() {
				if(!(reqField(document.copyvariant.desc.value,"Name"))){
					return false;
				}
            if(document.copyvariant.srcvariant.options.length<1)
            {
                  alert("Please select a source variant.");
                  return false;
            }
            document.copyvariant.action = document.copyvariant.srcvariant[document.copyvariant.srcvariant.selectedIndex].value;
            document.copyvariant.action += 
            document.copyvariant.submit();
				return true;          
         }
               function variant(id, name, updatelink)
               {
                   this.id = id;
                   this.name = name;
                   this.updatelink = updatelink;
               }

               function contenttype(id, name, variants)
               {
                   this.id = id;
                   this.name = name;
                   this.variants = variants;
               }
               function onFormLoad()
               {
                  document.copyvariant.srccontenttype.options.length = contenttypes.length;

                  for(i=0; i<contenttypes.length; i++)
                  {
                     document.copyvariant.srccontenttype.options[i].value=contenttypes[i].id;
                     document.copyvariant.srccontenttype.options[i].text=contenttypes[i].name;
                  }
                  contenttype_onchange();
               }
 
               function contenttype_onchange() 
               {
                  if(contenttypes.length < 1)
                     return;

                  index = document.copyvariant.srccontenttype.selectedIndex;
                  variants = contenttypes[index].variants;

                  document.copyvariant.srcvariant.options.length = variants.length;
                  for(i=0; i<variants.length; i++)
                  {
                     document.copyvariant.srcvariant.options[i].value=variants[i].updatelink;
                     document.copyvariant.srcvariant.options[i].text=variants[i].name;
                  }
                  document.copyvariant.srcvariant.selectedIndex=0;
               }
               ]]></script>
					<!-- begin XSL -->
					<xsl:element name="script">
						<xsl:attribute name="language">javascript</xsl:attribute>
						<xsl:text>contenttypes = new Array(</xsl:text>
						<xsl:for-each select="//contenttype">
							<xsl:text>new contenttype(</xsl:text>
							<xsl:value-of select="id"/>
							<xsl:text>, &quot;</xsl:text>
							<xsl:value-of select="name"/>
							<xsl:text>&quot;, new Array(</xsl:text>
							<xsl:for-each select="variant">
								<xsl:text>new variant(</xsl:text>
								<xsl:value-of select="id"/>
								<xsl:text>,&quot;</xsl:text>
								<xsl:call-template name="escape-quotes">
								   <xsl:with-param name="text" select="name"/>								
								</xsl:call-template>
								<xsl:text>&quot;</xsl:text>
								<xsl:text>,&quot;</xsl:text>
								<xsl:value-of select="updatelink"/>
								<xsl:text>&quot; )</xsl:text>
								<xsl:if test="not(position() = last())">
									<xsl:text>,</xsl:text>
								</xsl:if>
							</xsl:for-each>))
                     <xsl:if test="not(position() = last())">
								<xsl:text>,</xsl:text>
							</xsl:if>
						</xsl:for-each>
						<xsl:text>);</xsl:text>
					</xsl:element>
					<!-- end XSL -->
			</head>
			<body class="backgroundcolor" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<!--   BEGIN Banner and Login Details   -->
				<xsl:call-template name="bannerAndUserStatus"/>
				<!--   END Banner and Login Details   -->
				<table width="100%" cellpadding="0" cellspacing="1" border="0">
					<tr>
						<td align="middle" valign="top" width="150" height="100%" class="outerboxcell">
							<!--   start left nav slot   -->
							<!-- begin XSL -->
							<xsl:for-each select="document($relatedlinks)/*/component[@slotname='slt_sys_nav']">
								<xsl:copy-of select="document(url)/*/body/*"/>
							</xsl:for-each>
							<!-- end XSL -->
							<!--   end left nav slot   -->
						</td>
						<td align="middle" width="100%" valign="top" height="100%" class="outerboxcell">
							<!--   start main body slot   -->
							<xsl:apply-templates mode="copyvariants_mainbody"/>
							<!--   end main body slot   -->
						</td>
					</tr>
				</table>
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
	<xsl:template name="escape-quotes">
		<xsl:param name="text"/>
		<xsl:choose>
			<xsl:when test="contains($text, '&quot;' )">
				<xsl:value-of select="concat(substring-before($text, '&quot;'), '\&quot;')"/>
				<xsl:call-template name="escape-quotes">
					<xsl:with-param name="text" select="substring-after($text, '&quot;')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
