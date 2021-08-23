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
   <xsl:template mode="editvariant_mainbody" match="*">
      <xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
      <xsl:variable name="contenttype" select="/*/contenttypeurl"/>
      <xsl:variable name="variantslot" select="/*/variantsloturl"/>
      <xsl:variable name="contentlookup" select="/*/contentlookupurl"/>
      <xsl:variable name="componentname" select="componentname"/>
      <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
         <tr class="outerboxcell">
            <td class="outerboxcellfont" align="right" valign="top">
         Edit Variant
      </td>
         </tr>
         <xsl:for-each select="category">
            <tr class="headercell">
               <td>
                  <table width="100%" cellpadding="0" cellspacing="1" border="0">
                     <xsl:apply-templates select="document($contenttype)" mode="mode7"/>
                     <form name="newvariant" method="get">
                        <xsl:attribute name="action"><xsl:choose><!--when variant is created --><xsl:when test="variantid=''"><xsl:choose><xsl:when test="//sys_community=0">edit_gen.html</xsl:when><xsl:otherwise>edit_comm.html</xsl:otherwise></xsl:choose></xsl:when><!--when variant is updated --><xsl:otherwise><xsl:value-of select="'update.html'"/></xsl:otherwise></xsl:choose></xsl:attribute>
                        <input name="DBActionType" type="hidden" value="UPDATE"/>
                        <xsl:apply-templates select="document($contenttype)" mode="mode8"/>
                        <input name="variantid" type="hidden">
                           <xsl:attribute name="value"><xsl:value-of select="variantid"/></xsl:attribute>
                        </input>
                        <input name="sys_componentname" type="hidden">
                           <xsl:attribute name="value"><xsl:value-of select="../componentname"/></xsl:attribute>
                        </input>
                        <input name="sys_pagename" type="hidden">
                           <xsl:attribute name="value"><xsl:value-of select="../pagename"/></xsl:attribute>
                        </input>
                        <tr class="headercell2">
                           <td width="30%" align="center" class="headercell2font">
                              <!-- begin XSL -->
                              <xsl:if test="string-length(variantid)"> Variant 
                  Name(id):&nbsp;<xsl:value-of select="variantname"/>(<xsl:value-of select="variantid"/>) 
                  </xsl:if> 
                  &nbsp;
               </td>
                           <td width="70%" align="center" class="headercell2font">&nbsp;</td>
                        </tr>
                        <tr class="datacell1">
                           <td align="left" class="datacell1font">
                              <font class="reqfieldfont">*</font>Name</td>
                           <td align="left" class="datacell1font">
                              <input size="45" name="desc">
                                 <xsl:attribute name="value"><xsl:value-of select="variantdesc"/></xsl:attribute>
                              </input>
                           </td>
                        </tr>
                        <tr class="datacell1">
                           <td align="left" class="datacell1font">Description</td>
                           <td align="left" class="datacell1font">
                              <input size="45" name="description">
                                 <xsl:attribute name="value"><xsl:value-of select="description"/></xsl:attribute>
                              </input>
                           </td>
                        </tr>
                        <tr class="datacell2">
                           <td align="left" class="datacell1font">Style Sheet</td>
                           <td align="left" class="datacell1font">
                              <input size="45" name="stylesheet">
                                 <xsl:attribute name="value"><xsl:value-of select="variantstylesheet"/></xsl:attribute>
                              </input>
                           </td>
                        </tr>
                        <tr class="datacell1">
                           <td align="left" class="datacell1font">
                              <font class="reqfieldfont">*</font>URL</td>
                           <td align="left" class="datacell1font">
                              <input size="45" name="url">
                                 <xsl:attribute name="value"><xsl:value-of select="varianturl"/></xsl:attribute>
                              </input>
                           </td>
                        </tr>
                        <tr class="datacell1">
			   <td align="left" class="datacell1font">Location Prefix</td>
			   <td align="left" class="datacell1font">
			      <input size="10" name="locprefix">
				 <xsl:attribute name="value"><xsl:value-of select="locprefix"/></xsl:attribute>
			      </input>
			   </td>
                        </tr>
                        <tr class="datacell1">
			   <td align="left" class="datacell1font">Location Suffix</td>
			   <td align="left" class="datacell1font">
			      <input size="10" name="locsuffix">
				 <xsl:attribute name="value"><xsl:value-of select="locsuffix"/></xsl:attribute>
			      </input>
			   </td>
                        </tr>
                        <tr class="datacell2">
                           <td align="left" class="datacell1font">Output Form</td>
                           <td align="left" class="datacell1font">
                              <input type="radio" name="outputform" value="2">
                                 <xsl:if test="variantoutputform=&apos;2&apos; or variantoutputform=''">
                                    <xsl:attribute name="checked"/>
                                 </xsl:if>
                              </input>
                  Snippet
                  <input type="radio" name="outputform" value="1">
                                 <xsl:if test="variantoutputform=&apos;1&apos;">
                                    <xsl:attribute name="checked"/>
                                 </xsl:if>
                              </input>
                  Page
               </td>
                        </tr>
                        <tr class="datacell1">
                           <td align="left" class="datacell1font">Active Assembly Format</td>
                           <td align="left" class="datacell1font">
                              <select name="type">
                                 <option value="0">
                                    <xsl:if test="type='0' or type=''">
                                       <xsl:attribute name="selected"/>
                                    </xsl:if>
							  Normal
							</option>
                                 <option value="1">
                                    <xsl:if test="type='1'">
                                       <xsl:attribute name="selected"/>
                                    </xsl:if>
							  Auto Index
							</option>
                                 <option value="2">
                                    <xsl:if test="type='2'">
                                       <xsl:attribute name="selected"/>
                                    </xsl:if>
							  Non-HTML
							</option>
                              </select>
                           </td>
                        </tr>
                        <tr class="datacell1">
                           <td align="left" class="datacell1font">Publish When</td>
                           <td align="left" class="datacell1font">
                              <select name="publishwhen">
                                 <option value="">&nbsp;</option>
                                 <option value="d">
                                    <xsl:if test="publishwhen='d'">
                                       <xsl:attribute name="selected"/>
                                    </xsl:if>
							  Default
							</option>
                                 <option value="a">
                                    <xsl:if test="publishwhen='a'">
                                       <xsl:attribute name="selected"/>
                                    </xsl:if>
							  Always
							</option>
                                 <option value="n">
                                    <xsl:if test="publishwhen='n'">
                                       <xsl:attribute name="selected"/>
                                    </xsl:if>
							  Never
							</option>
                              </select>
                           </td>
                        </tr>
                        <tr class="datacell1">
                           <td align="left" class="datacell1font" colspan="2">
                              <input type="button" value="Save" name="save" language="javascript" onclick="return save_onclick()"/>&nbsp;
                  <input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
                              <input type="hidden" name="doccancelurl">
                                 <xsl:attribute name="value"><xsl:value-of select="../cancelurl"/></xsl:attribute>
                              </input>
                           </td>
                        </tr>
                     </form>
                     <xsl:if test="string-length(variantid)">
                        <tr class="headercell">
                           <td class="headercellfont" colspan="2">&nbsp;</td>
                        </tr>
                        <tr class="headercell">
                           <!--   Repeats once per category   -->
                           <td valign="top" align="left" class="headercellfont">Slots</td>
                           <td valign="top" align="right" class="headercellfont">
                              <a>
                                 <xsl:attribute name="href"><xsl:value-of select="addsloturl"/></xsl:attribute>
                  Add Slot
                </a>
                           </td>
                        </tr>
                        <tr>
                           <td colspan="2">
                              <table width="100%" cellpadding="0" cellspacing="1" border="0">
                                 <tr class="headercell2">
                                    <td width="5%" align="center" class="headercell2font">&nbsp;</td>
                                    <td width="95%" align="left" class="headercell2font">Slot Type(id)</td>
                                 </tr>
                                 <xsl:apply-templates select="document(/*/variantsloturl)/*/item" mode="mode1"/>
                              </table>
                           </td>
                        </tr>
                        <tr class="headercell">
                           <td class="headercellfont" colspan="2">&nbsp;</td>
                        </tr>
                        <tr class="headercell">
                           <!--   Repeats once per category   -->
                           <td valign="top" align="left" class="headercellfont">Sites</td>
                           <td valign="top" align="right" class="headercellfont">
                              <a>
                                 <xsl:attribute name="href"><xsl:value-of select="addsiteurl"/></xsl:attribute>
                  Add Site
                </a>
                           </td>
                        </tr>
                        <tr>
                           <td colspan="2">
                              <table width="100%" cellpadding="0" cellspacing="1" border="0">
                                 <tr class="headercell2">
                                    <td width="5%" align="center" class="headercell2font">&nbsp;</td>
                                    <td width="95%" align="left" class="headercell2font">Site(id)</td>
                                 </tr>
                                 <xsl:apply-templates select="document(/*/variantsiteurl)/*/item" mode="mode10"/>
                              </table>
                           </td>
                        </tr>
                     </xsl:if>
                  </table>
               </td>
            </tr>
         </xsl:for-each>
         <tr class="headercell">
            <td height="100%">&nbsp;</td>
            <!--   Fill down to the bottom   -->
         </tr>
      </table>
   </xsl:template>
   <xsl:template match="*" mode="mode7">
      <xsl:for-each select=".">
         <tr class="headercell">
            <!--   Repeats once per category   -->
            <td valign="top" align="left" class="headercellfont" colspan="2">Edit
          <xsl:apply-templates select="contenttitle"/>&nbsp;Variant
        </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode8">
      <xsl:for-each select=".">
         <input name="contenttypeid" type="hidden">
            <xsl:attribute name="value"><xsl:value-of select="contentid"/></xsl:attribute>
         </input>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="variantid" mode="mode9">
      <xsl:for-each select=".">
         <input name="variantid" type="hidden">
            <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
         </input>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="item" mode="mode1">
      <xsl:if test="string-length(slotid)">
         <tr class="datacell1">
            <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
            <td align="center" width="5%" valign="middle">
               <a href="javascript:delConfirm('{slotdelete}');">
                  <img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
               </a>
            </td>
            <td align="left" width="95%" class="datacell1font">
               <xsl:value-of select="slotname"/>(<xsl:value-of select="slotid"/>)</td>
         </tr>
      </xsl:if>
   </xsl:template>
   <xsl:template match="item" mode="mode10">
      <xsl:if test="string-length(siteid)">
         <tr class="datacell1">
            <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
            <td align="center" width="5%" valign="middle">
               <a href="javascript:delConfirm('{sitedelete}');">
                  <img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
               </a>
            </td>
            <td align="left" width="95%" class="datacell1font">
               <xsl:value-of select="sitename"/>(<xsl:value-of select="siteid"/>)</td>
         </tr>
      </xsl:if>
   </xsl:template>
</xsl:stylesheet>
