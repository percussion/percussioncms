<?xml version='1.0' encoding='UTF-8'?>
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
		<!ENTITY % w3centities-f PUBLIC
				"-//W3C//ENTITIES Combined Set//EN//XML"
				"http://www.w3.org/2003/entities/2007/w3centities-f.ent"
				>
		%w3centities-f;
		]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
 <xsl:variable name="publishableValues" select="document(/*/PublishableLookupUrl)/*"/>
 <xsl:template mode="workflowstateeditbody" match="*">
	<xsl:variable name="workflowinfo" select="document(/*/workflowlinfourl)/*"/>
	  <table width="100%" height="100%" cellpadding="0" cellspacing="3" border="0">
	  <form name="UpdateState" action="UpdateState.htm" method="get" onsubmit="return save_onclick()">            <!--   BEGIN Banner and Login Details   -->
      <tr>
        <td valign="top" class="headercell">          <!--   Main View Area Start   -->
          <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
				<tr class="outerboxcell">
				  <td align="right" valign="top">
					 <span class="outerboxcellfont">
						<a>
						  <xsl:attribute name="href">
							 <xsl:value-of select="$workflowinfo/workflowslink"/>
						  </xsl:attribute>
							Workflows
						</a> &gt;
						<a>
						  <xsl:attribute name="href">
							 <xsl:value-of select="$workflowinfo/workflowlink"/>
						  </xsl:attribute>
						  <xsl:apply-templates select="$workflowinfo/@workflowname"/>
						</a>  &gt;
						<xsl:choose>
							<xsl:when test="workflowStateEdit/Name=''">New State</xsl:when>
							<xsl:otherwise><xsl:apply-templates select="workflowStateEdit/Name"/></xsl:otherwise>
						</xsl:choose>
					 </span>
				  </td>
				</tr>
				<tr>
              <td width="100%" class="headercell">&nbsp;
              </td>
            </tr>
				<tr class="headercell">
				  <td valign="top">          <!--   View Start   -->
					<input name="workflowid" type="hidden">
					  <xsl:attribute name="value">
						 <xsl:value-of select="workflowid"/>
					  </xsl:attribute>
					</input>
					<input name="sys_componentname" type="hidden">
					  <xsl:attribute name="value">
						 <xsl:value-of select="componentname"/>
					  </xsl:attribute>
					</input>
						<input name="stateid" type="hidden">
						  <xsl:attribute name="value">
							 <xsl:value-of select="stateid"/>
						  </xsl:attribute>
						</input>
					 <input name="rxorigin" type="hidden" value="editstate"/>
					 <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
						<xsl:apply-templates select="workflowStateEdit" mode="mode16"/>
					 </table>

				  </td>

				</tr>
				<xsl:if test="not(stateid='')">
				<tr>
				  <td class="headercell">
					 <table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr>
						  <td colspan="10" class="outerboxcell" align="right">
							 <span class="outerboxcellfont">Transitions</span>
						  </td>
						</tr>
						 <xsl:apply-templates select="document($exttransitions)" mode="transitionlist"/>
					 </table>
				  </td>
				</tr>
				<tr>
				  <td class="headercell">
					 <table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr>
						  <td colspan="10" class="outerboxcell" align="right">
							 <span class="outerboxcellfont">Aging Transitions</span>
						  </td>
						</tr>
						 <xsl:apply-templates select="document($extagingtransitions)" mode="agingtransitionlist"/>
					 </table>
				  </td>
				</tr>
				<tr>
				  <td class="headercell">
					 <table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr>
						  <td colspan="10" class="outerboxcell" align="right">
							 <span class="outerboxcellfont">Assigned Roles</span>
						  </td>
						</tr>
						 <xsl:apply-templates select="document($extstateroles)" mode="staterolelist"/>
					 </table>
				  </td>
				</tr>
				<!--
				<tr>
				  <td class="headercell">
					 <table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr>
						  <td colspan="10" class="outerboxcell" align="right">
							 <span class="headercellfont">Notifications</span>
						  </td>
						</tr>
						 <xsl:apply-templates select="document($extstatenotifs)" mode="statenotiflist"/>
					 </table>
				  </td>
				</tr>
				-->
				</xsl:if>
          </table>
        </td>
      </tr>
		<tr class="headercell">
		  <td height="100%">&nbsp;</td>
		</tr>
		</form>
     </table>
  </xsl:template>
  <xsl:template match="*" mode="mode16">
    <xsl:for-each select=".">
				<tr class="datacell1">
				  <td align="left" class="datacell1font" width="30%"><font class="reqfieldfont">*</font>Name:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				  </td>
				  <td width="100%" align="left" class="datacell1font">
					<input type="text" name="requiredname" size="30">
					  <xsl:attribute name="value">
						 <xsl:value-of select="Name"/>
					  </xsl:attribute>
					</input>
				  </td>
				</tr>
				<tr class="datacell2">
				  <td align="left" class="datacell1font">Description:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				  </td>
				  <td width="100%" align="left" class="datacell1font">
					<input type="text" size="40" name="description">
					  <xsl:attribute name="value">
						 <xsl:value-of select="Description"/>
					  </xsl:attribute>
					</input>
				  </td>
				</tr>
				<tr class="datacell2">
				  <td align="left" class="datacell1font">Sort Order:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				  </td>
				  <td width="100%" align="left" class="datacell1font">
					<input type="text" size="40" name="sortorder">
					  <xsl:attribute name="value">
						 <xsl:value-of select="sortorder"/>
					  </xsl:attribute>
					</input>
				  </td>
				</tr>
				<tr class="datacell1">
				  <td align="left" class="datacell1font">Publishable:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				  </td>
               <td width="100%" align="left" class="datacell1font">
                  <select name="publishable">
                     <xsl:variable name="selectedValue" select="publishable"/>
                     <xsl:attribute name="selected"><xsl:value-of select="$selectedValue"/></xsl:attribute>
                     <xsl:for-each select="$publishableValues/PSXEntry">
                        <option>
                           <xsl:variable name="optionValue" select="./Value"/>
                           <xsl:attribute name="value"><xsl:value-of select="$optionValue"/></xsl:attribute>
                           <xsl:if test="$selectedValue=$optionValue">
                              <xsl:attribute name="selected"/>
                           </xsl:if>
                           <xsl:value-of select="./PSXDisplayText"/>
                        </option>
                     </xsl:for-each>
                  </select>
               </td>
				</tr>
            <tr class="datacell2">
              <td colspan="2" align="left" class="datacell1font">
                <input type="hidden" class="nav_body" name="DBActionType" value="Update"/>
                <input type="button" class="nav_body" name="DBActionType" value="Save" onclick="javascript:save_onclick();"/>&nbsp;
                <input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
              </td>
            </tr>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
