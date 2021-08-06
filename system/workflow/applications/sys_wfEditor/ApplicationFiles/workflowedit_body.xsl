<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
   <xsl:variable name="statelookup" select="document(//statelookupurl)//sys_Lookup"/>
   <xsl:variable name="initialstateid" select="//initialstate"/>
   <xsl:template mode="workfloweditbody" match="*">
      <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
         <form name="UpdateWorkflow" action="UpdateWorkflow.htm" method="get" onsubmit="return save_onclick()">
            <!--   BEGIN Banner and Login Details   -->
            <tr class="outerboxcell">
               <td align="right" valign="top" colspan="2">
                  <span class="outerboxcellfont">
                     <a>
                        <xsl:attribute name="href"><xsl:value-of select="workflowlink"/></xsl:attribute>
							 Workflows
						</a>
						  &gt;<xsl:apply-templates select="workflowname"/>
                  </span>
               </td>
            </tr>
            <tr class="headercell">
               <td valign="top" colspan="2"> &nbsp;         <!--   View Start   -->
               </td>
            </tr>
            <tr class="headercell">
               <td valign="top" colspan="2">
                  <!--   View Start   -->
                  <input name="workflowid" type="hidden">
                     <xsl:attribute name="value"><xsl:value-of select="workflowid"/></xsl:attribute>
                  </input>
                  <input name="sys_componentname" type="hidden">
                     <xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
                  </input>
                  <input name="rxorigin" type="hidden" value="editwf"/>
                  <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
                     <xsl:apply-templates select="workflowEdit" mode="mode17"/>
                  </table>
               </td>
            </tr>
            <tr class="headercell">
               <td valign="top" align="center">
                  <table width="100%" cellpadding="0" cellspacing="1" border="0">
                     <tr>
                        <td colspan="10" class="outerboxcell" align="right">
                           <span class="outerboxcellfont">States</span>
                        </td>
                     </tr>
                     <xsl:apply-templates select="document($extstates)" mode="statelist"/>
                  </table>
               </td>
            </tr>
            <tr class="headercell">
               <td valign="top">
                  <table width="100%" cellpadding="0" cellspacing="1" border="0">
                     <tr>
                        <td colspan="10" class="outerboxcell" align="right">
                           <span class="outerboxcellfont">Roles</span>
                        </td>
                     </tr>
                     <xsl:apply-templates select="document($extroles)" mode="rolelist"/>
                  </table>
               </td>
            </tr>
            <tr class="headercell">
               <td valign="top">
                  <table width="100%" cellpadding="0" cellspacing="1" border="0">
                     <tr>
                        <td colspan="10" class="outerboxcell" align="right">
                           <span class="outerboxcellfont">Notifications</span>
                        </td>
                     </tr>
                     <xsl:apply-templates select="document($extstatenotifs)" mode="statenotiflist"/>
                  </table>
               </td>
            </tr>
            <tr class="headercell">
               <td height="100%">&nbsp;</td>
               <!--   Fill down to the bottom   -->
            </tr>
         </form>
      </table>
   </xsl:template>
   <xsl:template match="workflowEdit" mode="mode17">
      <xsl:for-each select=".">
         <tr class="datacell1">
            <td align="left" class="datacell1font" width="30%">
               <font class="reqfieldfont">*</font>Name:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
            </td>
            <td width="100%" align="left" class="datacell1font">
               <input type="text" name="requiredname" size="30">
                  <xsl:attribute name="value"><xsl:value-of select="Name"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell1font">Administrator:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
            </td>
            <td width="100%" align="left" class="datacell1font">
               <input type="text" name="administrator" size="30">
                  <xsl:attribute name="value"><xsl:value-of select="Admin"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell1">
            <td align="left" class="datacell1font">Initial State:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
            </td>
            <td width="100%" align="left" class="datacell1font">
               <select name="initialstate">
                  <xsl:for-each select="$statelookup/PSXEntry">
                     <option value="{Value}">
                        <xsl:if test="Value=''">
                           <xsl:attribute name="value">1</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="Value=$initialstateid">
                           <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="PSXDisplayText"/>
                        <xsl:if test="PSXDisplayText=''">
                           <xsl:text>Set after adding states</xsl:text>
                        </xsl:if>
                     </option>
                  </xsl:for-each>
               </select>
            </td>
         </tr>
         <tr class="datacell2">
            <td align="left" class="datacell1font">Description:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
            </td>
            <td width="100%" align="left" class="datacell1font">
               <input type="text" name="description" size="40">
                  <xsl:attribute name="value"><xsl:value-of select="Description"/></xsl:attribute>
               </input>
            </td>
         </tr>
         <tr class="datacell1">
            <td colspan="2" align="left" class="datacell1font">
               <input type="hidden" class="nav_body" name="DBActionType" value="Update"/>
               <input type="button" class="nav_body" name="DBActionType" value="Save" onclick="javascript:save_onclick();"/>&nbsp;
               <input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>
