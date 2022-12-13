<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
        ]>
<xsl:stylesheet version="1.1" xmlns:url="http://whatever/java/java.net.URLEncoder"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
    <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
    <!-- Frameset doctype same as xhtml strict but allows framesets -->
    <xsl:output method="html" omit-xml-declaration="yes" encoding="UTF-8" />
    <xsl:variable name="lang" select="//@lang"/>
    <xsl:variable name="rxroot">
        <xsl:choose>
            <xsl:when test="string-length(//@rxroot) > 0">..</xsl:when>
            <xsl:otherwise><xsl:value-of select="//@rxroot"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="height">
        <xsl:choose>
            <xsl:when test="string-length(//@height) = 0">
                <xsl:text>100%</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="//@height" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="width">
        <xsl:choose>
            <xsl:when test="string-length(//@width) = 0">
                <xsl:text>100%</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="//@width" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="ajaxSwingEnabled" select="(//@ajaxSwingEnabled='true' and //@sys_ui='ajaxswing')"/>
    <xsl:variable name="ajaxSwingHost" select="//@ajaxSwingHost"/>
    <xsl:variable name="ajaxSwingPort" select="//@ajaxSwingPort"/>
    <xsl:variable name="ajaxSwingRoot" select="concat('http://',$ajaxSwingHost,':',$ajaxSwingPort,'/ajaxswing/apps/CMSystem')"/>
    <xsl:variable name="ajaxswingUrl">
        <xsl:value-of disable-output-escaping="yes" select="concat($ajaxSwingRoot,'?')"/>
        <xsl:text disable-output-escaping="yes">CODEBASE=</xsl:text>
 <xsl:value-of select="url:encode(concat($rxroot,'/sys_resources/AppletJars/rxcx.jar'))" />
        <xsl:text disable-output-escaping="yes">&amp;VIEW=CX&amp;MAYSCRIPT=true&amp;NAME=ContentExplorerApplet&amp;ID=ContentExplorerApplet</xsl:text>
        <xsl:text>&amp;DEBUG=</xsl:text>
        <xsl:if test="//@debug = 'true'">
            <xsl:value-of select="//@debug" />
        </xsl:if>
        <xsl:if test="//@debug = 'false'">
            <xsl:value-of select="//@debug" />
        </xsl:if>

        <xsl:text disable-output-escaping="yes">&amp;RESTRICTSEARCHFIELDSTOUSERCOMMUNITY=</xsl:text>
        <xsl:value-of select="//@RestrictSearchFieldsToUserCommunity" />

        <xsl:text disable-output-escaping="yes">&amp;CacheSearchableFieldsInApplet=</xsl:text>
        <xsl:value-of select="//@CacheSearchableFieldsInApplet" />

        <xsl:text disable-output-escaping="yes">&amp;isManagedNavUsed=</xsl:text>
        <xsl:value-of select="//@isManagedNavUsed" />

        <xsl:text disable-output-escaping="yes">&amp;isManagedNavUsed=</xsl:text>
        <xsl:value-of select="//@isManagedNavUsed" />

        <xsl:if test="floor(//@contentid) = //@contentid">
            <xsl:text>&amp;CONTENTID=</xsl:text>
            <xsl:value-of select="//@contentid" />
        </xsl:if>
        <xsl:text disable-output-escaping="yes">&amp;OPTIONS_URL=../sys_cxSupport%2Foptions.xml</xsl:text>

        <xsl:text disable-output-escaping="yes">&amp;MENU_URL=..%2Fsys_cx%2FContentExplorerMenu.html</xsl:text>

        <xsl:text disable-output-escaping="yes">&amp;NAV_URL=..%2Fsys_cx%2FContentExplorer.html</xsl:text>

        <xsl:text disable-output-escaping="yes">&amp;helpset_file=..%2FDocs%2FRhythmyx%2FBusiness_Users%2FContent_Explorer_Help.hs</xsl:text>
        <xsl:text disable-output-escaping="yes">&amp;sys_cxinternalpath=</xsl:text>
        <xsl:value-of select="url:encode(//@cxinternalpath)" />

        <xsl:text disable-output-escaping="yes">&amp;sys_cxdisplaypath=</xsl:text>
        <xsl:value-of select="url:encode(//@cxdisplaypath)" />

        <xsl:text disable-output-escaping="yes"></xsl:text>
    </xsl:variable>
    <xsl:template match="/">
        <html lang="{$lang}">
            <head>
                <title>
                    <xsl:call-template name="getLocaleString">
                        <xsl:with-param name="key" select="'psx.sys_caSites.casites@Rhythmyx - Content Explorer'"/>
                        <xsl:with-param name="lang" select="$lang"/>
                    </xsl:call-template>
                </title>
                <meta http-equiv="Pragma" content="no-cache" />
                <meta http-equiv="Expires" content="-1" />
                <script src="../sys_resources/js/browser.js">;</script>
                <script >
                <xsl:text disable-output-escaping="yes">
                //&lt;![CDATA[
					function showWindow(url, target, style)
					{
						win = parent.window.open(url, target, style);
   				   if (win == null)
   				      alert("Popups must be enabled for the content explorer");
   				   else
      				    win.focus();
					}
                    var ajaxSwingEnabled=</xsl:text><xsl:value-of select="$ajaxSwingEnabled"/><xsl:text disable-output-escaping="yes">
                    var ajaxSwingRoot="</xsl:text><xsl:value-of select="$ajaxSwingRoot"/><xsl:text disable-output-escaping="yes">";

                    function setAjaxSwing()
                    {

                     document.getElementById('maincontent').src = "</xsl:text>
                    <xsl:value-of select="$ajaxswingUrl" disable-output-escaping="yes"/>
                    <xsl:text disable-output-escaping="yes">&amp;ran="+Math.random();
                    }
            addEvent(window,"message",function(event) {
                 // Make sure we do not intercept requests not from ajaxswing
                 if (event.origin != "</xsl:text><xsl:value-of select="concat('http://',$ajaxSwingHost,':',$ajaxSwingPort)"/><xsl:text disable-output-escaping="yes">")
                                return;
                 json = decodeURIComponent(event.data);
                 var message = JSON.parse(json);

                 if (message.cmd == "open") {
                    window.open(message.actionUrl,message.target,message.style);
                 }

                 if (message.cmd == "refresh") {
                    location.reload();
                 }
                });

                    //]]&gt;
                    </xsl:text>
                </script>
            </head>
            <frameset rows="74px,*" border="0">
                <xsl:if test="$ajaxSwingEnabled">
                    <xsl:attribute name="onload">
                        <xsl:value-of select="'setAjaxSwing()'" />
                    </xsl:attribute>
                </xsl:if>
                <frame name="banner" title="Navigation Banner and User Info" scrolling="no" src="{//bannerpageurl}"/>
                <!--  <frame name="navcontent" id="maincontent" title="Main Content" scrolling="no" src="{//cxpageurl}"/> -->
                <frame name="navcontent" id="maincontent" title="Main Content" scrolling="no">
                    <xsl:if test="not($ajaxSwingEnabled)">
                        <xsl:attribute name="src">
                            <xsl:value-of select="//cxpageurl" />
                        </xsl:attribute>
                    </xsl:if>
                </frame>
                <noframes>
                    <body class="backgroundcolor" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
                        <p>This page uses frames, but your browser doesn't support them.</p>
                    </body>
                </noframes>
            </frameset>
        </html>
    </xsl:template>
    <psxi18n:lookupkeys>
        <key name="psx.sys_mainPage.mainpage@Rhythmyx - Content Administrator">Title for Rhythmyx main page.</key>
    </psxi18n:lookupkeys>
</xsl:stylesheet>
