<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
%HTMLlat1;
<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
%HTMLsymbol;
<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" extension-element-prefixes="saxon"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="langlist" select="document(//@langurl)/*/lang"/>
   <xsl:variable name="lang">
      <xsl:choose>
         <xsl:when test="//@xml:lang='' or not(//@xml:lang = $langlist/langstring)">en-us</xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="//@xml:lang"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="bouncetourl">
      <xsl:choose>
         <xsl:when test="//@sys_defaulthomepageurl!=''">
            <xsl:value-of select="//@sys_defaulthomepageurl"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="//@caurl"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="itemsurl" select="document(/*/@itemsurl)/*/Community"/>
   <xsl:variable name="communities_enabled" select="$itemsurl/../@communities_enabled"/>
   <xsl:variable name="communitypage" select="/*/@communitypage"/>
   <xsl:variable name="logincommunity" select="/*/@logincommunity"/>
   <xsl:variable name="communityid">
      <xsl:choose>
         <xsl:when test="count($itemsurl)=1 and $itemsurl/@commid!=''">
            <xsl:value-of select="$itemsurl/@commid"/>
         </xsl:when>
         <xsl:when test="$logincommunity!='' and $logincommunity=$itemsurl/@commid">
            <xsl:value-of select="$logincommunity"/>
         </xsl:when>
         <xsl:when test="//@sys_defaultcommunityid!='' and //@sys_defaultcommunityid=$itemsurl/@commid">
            <xsl:value-of select="//@sys_defaultcommunityid"/>
         </xsl:when>
      </xsl:choose>
   </xsl:variable>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 4.0"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_welcome.communitylogin@Community  and Language Selection Home Page'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <meta http-equiv="Content-Type" content="text/html; UTF-8"/>
            <script language="javascript">
               function redirectTo(redirurl,bouncetourl,communities_enabled,communityid, sys_lang)
               {
               bouncetourl = escape(bouncetourl);
               if(redirurl.indexOf("?")==-1)
               {
               redirurl += "?";
               }
               else
               {
               redirurl += "&amp;";
               }
               redirurl += "bouncetourl=" + 	bouncetourl;
               if(communities_enabled="yes")
               redirurl += "&amp;sys_community=" + communityid + "&amp;sys_lang=" + sys_lang;
               document.loginform.action = redirurl;
               document.loginform.submit();
               }
               function onclickGo(redirurl,bouncetourl,communities_enabled)
               {
               var communityid = document.loginform.community[document.loginform.community.selectedIndex].value;
               if(document.loginform.sys_lang.selectedIndex)
               var sys_lang= document.loginform.sys_lang[document.loginform.sys_lang.selectedIndex].value;
               else
               var sys_lang= document.loginform.sys_lang.value;
               redirectTo(redirurl,bouncetourl,communities_enabled,communityid, sys_lang)
               }
            </script>
            <link rel="stylesheet" href="../sys_resources/css/rxcx.css" type="text/css" media="screen"/>
         </head>
         <body>
            <xsl:if test="($communities_enabled!='yes' or $communityid!='') and $communitypage!='yes'">
               <xsl:attribute name="onload">javascript:redirectTo("<xsl:value-of select="//@loginurl"/>","<xsl:value-of select="$bouncetourl"/>","<xsl:value-of select="$communities_enabled"/>","<xsl:value-of select="$communityid"/>","<xsl:value-of select="$lang"/>");</xsl:attribute>
            </xsl:if>
            <table class="RxLogin" cellpadding="0" cellspacing="0" border="0">
               <tr>
                  <td colspan="2">
                     <table cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                           <td width="25">
                              <img height="25" src="{concat('../rx_resources/images/',$lang,'/','rhythmyx_login_topleft.gif')}" width="25"/>
                           </td>
                           <td class="rhythmyx_login_topbkgd">
                              <img height="25" src="{concat('../rx_resources/images/',$lang,'/','blank-pixel.gif')}" width="25"/>
                           </td>
                           <td width="25">
                              <img height="25" src="{concat('../rx_resources/images/',$lang,'/','rhythmyx_login_topright.gif')}" width="25"/>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td class="RightShadow">
                     <img src="{concat('../rx_resources/images/',$lang,'/','shadow-topright.gif')}" width="9" height="25"/>
                  </td>
               </tr>
               <tr>
                  <td colspan="2" class="BannerCell">
                     <img height="50" src="{concat('../rx_resources/images/',$lang,'/','rhythmyx_login_banner.jpg')}" width="516">
                        <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_welcome.communitylogin.alt@Rhythmyx Content Manager'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                     </img>
                  </td>
                  <td class="RightShadow">&nbsp;</td>
               </tr>
               <tr>
                  <td class="grayBKGD" colspan="2">
                     <form name="loginform" id="loginform" method="post" class="Login-ConsoleForm" target="_top">
                        <table cellspacing="0" cellpadding="0" border="0" width="450" style="margin-left:24px; margin-right:24;">
                           <tr>
                              
                           </tr>
                           <tr>
                              <td width="450" bgcolor="#e8e3da">
                                 <table cellspacing="1" cellpadding="0" width="100%" border="0">
                                    <tr class="whiteBKGD" valign="middle">
                                       <td bgcolor="#e8e3da">
                                          <table cellspacing="1" cellpadding="0" width="100%" border="0">
                                             <xsl:choose>
                                                <xsl:when test="count($itemsurl) &gt; 1 or not($itemsurl/@commid = '')">
                                                   <tr class="whiteBKGD" valign="middle">
                                                      <td height="30" align="right">
                                                         <img src="../sys_resources/images/spacer.gif" height="17" width="10"/>
                                                         <span class="SelectText">
                                                            <label for="community">
                                                               <xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_welcome.communitylogin.mnemonic.Select a Community@M'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                                                               <xsl:call-template name="getMnemonicLocaleString">
                                                                  <xsl:with-param name="key" select="'psx.sys_welcome.communitylogin@Select a Community'"/>
                                                                  <xsl:with-param name="mnemonickey" select="'psx.sys_welcome.communitylogin.mnemonic.Select a Community@M'"/>
                                                                  <xsl:with-param name="lang" select="$lang"/>
                                                               </xsl:call-template>
                                                            </label>
                                                         </span>
                                                         <img src="../sys_resources/images/spacer.gif" height="17" width="10"/>
                                                      </td>
                                                      <td>
                                                         <img src="../sys_resources/images/spacer.gif" height="17" width="10"/>
                                                         <label for="community" accesskey="M"/>
                                                         <select class="ComboText" id="community" name="community" tabindex="1">
                                                            <xsl:for-each select="$itemsurl">
                                                               <option value="{@commid}">
                                                                  <xsl:if test="@commid=$logincommunity">
                                                                     <xsl:attribute name="selected"/>
                                                                  </xsl:if>
                                                                  <xsl:value-of select="."/>
                                                               </option>
                                                            </xsl:for-each>
                                                         </select>
                                                      </td>
                                                   </tr>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <tr>
                                                      <td>You do not belong to any community. Please consult your Rhythmyx administrator to proceed further.</td>
                                                   </tr>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:choose>
                                                <xsl:when test="count($langlist)&gt;1">
                                                   <tr class="whiteBKGD" valign="middle">
                                                      <td height="30" align="right">
                                                         <img src="../sys_resources/images/spacer.gif" height="17" width="10"/>
                                                         <span class="SelectText">
                                                            <label for="sys_lang">
                                                               <xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_welcome.communitylogin.mnemonic.Locale@L'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                                                               <xsl:call-template name="getMnemonicLocaleString">
                                                                  <xsl:with-param name="key" select="'psx.sys_welcome.communitylogin@Select a Language'"/>
                                                                  <xsl:with-param name="mnemonickey" select="'psx.sys_welcome.communitylogin.mnemonic.Locale@L'"/>
                                                                  <xsl:with-param name="lang" select="$lang"/>
                                                               </xsl:call-template>
                                                            </label>
                                                         </span>
                                                         <img src="../sys_resources/images/spacer.gif" height="17" width="10"/>
                                                      </td>
                                                      <td>
                                                         <img src="../sys_resources/images/spacer.gif" height="17" width="10"/>
                                                         <label for="sys_lang">
                                                            <xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_welcome.communitylogin.mnemonic.Locale@L'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                                                         </label>
                                                         <select class="ComboText" id="sys_lang" name="sys_lang" tabindex="2">
                                                            <xsl:for-each select="$langlist">
                                                               <option value="{langstring}">
                                                                  <xsl:if test="langstring=$lang">
                                                                     <xsl:attribute name="selected"/>
                                                                  </xsl:if>
                                                                  <xsl:value-of select="name"/>
                                                               </option>
                                                            </xsl:for-each>
                                                         </select>
                                                      </td>
                                                   </tr>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <input type="hidden" name="sys_lang" value="{$langlist/langstring}"/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                          </table>
                                       </td>
                                       <xsl:choose>
                                          <xsl:when test="count($itemsurl) &gt; 1 or not($itemsurl/@commid = '')">
                                             <td class="button" align="center" width="49">
                                                <img src="../sys_resources/images/spacer.gif" height="17" width="3"/>
                                                <input type="image" height="17" alt="Logon" width="43" src="{concat('../rx_resources/images/',$lang,'/','go.gif')}" border="0" name="Go" tabindex="3">
                                                   <xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_welcome.communitylogin.mnemonic.Go@G'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                                                   <xsl:attribute name="onclick">javascript:onclickGo("<xsl:value-of select="//@loginurl"/>","<xsl:value-of select="$bouncetourl"/>","<xsl:value-of select="$communities_enabled"/>");</xsl:attribute>
                                                </input>
                                                <img src="../sys_resources/images/spacer.gif" height="17" width="3"/>
                                             </td>
                                          </xsl:when>
                                       </xsl:choose>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                        </table>
                     </form>
                  </td>
                  <td class="RightShadow">&nbsp;</td>
               </tr>
               <tr>
                  <td colspan="2" class="BottomShadow">&nbsp;</td>
                  <td>
                     <img src="{concat('../rx_resources/images/',$lang,'/','shadow-bottomright.gif')}" width="9" height="9"/>
                  </td>
               </tr>
            </table>
            <div class="copyright">&copy; Copyright Percussion Software @COPYRIGHTYEAR@</div>
            <script>
               if(document.getElementById("community"))
                  document.getElementById("community").focus();
            </script>
         </body>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_welcome.communitylogin@Community and Language Selection Home Page">The title for Community and Language selection home page.</key>
      <key name="psx.sys_welcome.communitylogin@Select a Community">The community selection list box label.</key>
      <key name="psx.sys_welcome.communitylogin@Select a Language">The language selection list box label.</key>
      <key name="psx.sys_welcome.communitylogin@Go">The go button label.</key>
      <key name="psx.sys_welcome.communitylogin.alt@Rhythmyx Content Manager">Alt text for Rhythmyx Content Manager image on community login page.</key>
      <key name="psx.sys_welcome.communitylogin.alt@Welcome to Rhythmyx">Alt text for Welcome to Rhythmyx image on community login page.</key>
      <key name="psx.sys_welcome.communitylogin.mnemonic.Select a Community@M">Mnemonic for label &quot;Select a Community&quot;.</key>
      <key name="psx.sys_welcome.communitylogin.mnemonic.Locale@L">Mnemonic for label &quot;Locale&quot;.</key>
      <key name="psx.sys_welcome.communitylogin.mnemonic.Go@G">Mnemonic for label &quot;Go&quot;.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
