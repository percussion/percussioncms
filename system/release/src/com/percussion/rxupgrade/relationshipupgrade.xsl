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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!-- main template -->
   <xsl:template match="/">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!-- copy any attribute -->
   <xsl:template match="@*" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <!-- replace all of the existing PSXRelationshipConfig elements with the given ones -->
   <xsl:template match="PSXRelationshipConfigSet" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <PSXRelationshipConfig category="rs_copy" label="New Copy" name="New Copy" type="system">
            <PSXCloneOverrideFieldList id="0">
               <PSXCloneOverrideField id="0" name="sys_title">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/relationship/sys_CloneTitle</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>Copy ($clone_count) of {0}</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.TITLE</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_communityid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/generic/sys_Literal</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.COMMUNITYID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_workflowid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/generic/sys_Literal</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>WORKFLOWAPPS.WORKFLOWAPPID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
            </PSXCloneOverrideFieldList>
            <PSXPropertySet>
               <PSXProperty locked="yes" name="rs_allowcloning">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies whether or not this relationship can be cloned. This property can be overridden in the request if associated locked property is not checked.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_useownerrevision">
                  <Value type="Boolean">yes</Value>
                  <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usedependentrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_expirationtime">
                  <Value type="Date"/>
                  <Description>Specifies the expiry date and time if this relationship can expire. Do not specify a value if the relationship should not be expired.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_useserverid">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_islocaldependency">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies if this relationship must be packaged and deployed with the owner. If checked, the relationship must be deployed, if not, it is optional.</Description>
               </PSXProperty>
            </PSXPropertySet>
            <ProcessChecks>
               <PSXProcessCheck context="relationship" name="rs_cloneshallow" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
               <PSXProcessCheck context="relationship" name="rs_clonedeep" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
            </ProcessChecks>
            <Explanation/>
         </PSXRelationshipConfig>
         <PSXRelationshipConfig category="rs_promotable" label="Promotable Version" name="Promotable Version" type="system">
            <PSXCloneOverrideFieldList id="0">
               <PSXCloneOverrideField id="0" name="sys_title">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/relationship/sys_CloneTitle</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>PV Copy ($clone_count) of {0}</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.TITLE</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_communityid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/generic/sys_Literal</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.COMMUNITYID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_workflowid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/generic/sys_Literal</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.WORKFLOWAPPID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
            </PSXCloneOverrideFieldList>
            <EffectSet>
               <PSXConditionalEffect activationEndpoint="dependent">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_Promote</name>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
            </EffectSet>
            <PSXPropertySet>
               <PSXProperty locked="yes" name="rs_allowcloning">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies whether or not this relationship can be cloned.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_useownerrevision">
                  <Value type="Boolean">yes</Value>
                  <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usedependentrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_expirationtime">
                  <Value type="Date"/>
                  <Description>Specifies the expiry date and time if this relationship can expire. Do not specify a value if the relationship should not be expired.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_useserverid">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_islocaldependency">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies if this relationship must be packaged and deployed with the owner. If checked, the relationship must be deployed, if not, it is optional.</Description>
               </PSXProperty>
            </PSXPropertySet>
            <ProcessChecks>
               <PSXProcessCheck context="relationship" name="rs_cloneshallow" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
               <PSXProcessCheck context="relationship" name="rs_clonedeep" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
            </ProcessChecks>
            <Explanation/>
         </PSXRelationshipConfig>
         <PSXRelationshipConfig category="rs_activeassembly" label="Active Assembly" name="Active Assembly" type="system">
            <PSXCloneOverrideFieldList id="0"/>
            <PSXPropertySet>
               <PSXProperty locked="yes" name="rs_allowcloning">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies whether or not this relationship can be cloned.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_useownerrevision">
                  <Value type="Boolean">yes</Value>
                  <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usedependentrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_expirationtime">
                  <Value type="Date"/>
                  <Description>Specifies the expiry date and time if this relationship can expire. Do not specify a value if the relationship should not be expired.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_useserverid">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_islocaldependency">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies if this relationship must be packaged and deployed with the owner. If checked, the relationship must be deployed, if not, it is optional.</Description>
               </PSXProperty>
            </PSXPropertySet>
            <UserPropertySet>
               <PSXProperty locked="no" name="sys_slotid">
                  <Value type="String"/>
                  <Description>The slot used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_sortrank">
                  <Value type="String">1</Value>
                  <Description>The sorting rank.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_variantid">
                  <Value type="String"/>
                  <Description>The variant used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_folderid">
                  <Value type="String"/>
                  <Description>The folder id used, optional.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_siteid">
                  <Value type="String"/>
                  <Description>The site id used, optional.</Description>
               </PSXProperty>
            </UserPropertySet>
            <ProcessChecks>
               <PSXProcessCheck context="relationship" name="rs_cloneshallow" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                     <PSXRule boolean="and">
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>category</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>rs_promotable</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>OR</boolean>
                        </PSXConditional>
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>category</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>rs_copy</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
               <PSXProcessCheck context="relationship" name="rs_clonedeep" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                     <PSXRule boolean="and">
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>category</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>rs_translation</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
            </ProcessChecks>
            <Explanation/>
         </PSXRelationshipConfig>
         <PSXRelationshipConfig category="rs_activeassembly" label="Active Assembly - Mandatory" name="Active Assembly - Mandatory" type="system">
            <PSXCloneOverrideFieldList id="0"/>
            <EffectSet>
               <PSXConditionalEffect activationEndpoint="owner">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_PublishMandatory</name>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>no</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
               <PSXConditionalEffect activationEndpoint="owner">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_UnpublishMandatory</name>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>no</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
            </EffectSet>
            <PSXPropertySet>
               <PSXProperty locked="yes" name="rs_allowcloning">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies whether or not this relationship can be cloned.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_useownerrevision">
                  <Value type="Boolean">yes</Value>
                  <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usedependentrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_expirationtime">
                  <Value type="Date"/>
                  <Description>Specifies the expiry date and time if this relationship can expire. Do not specify a value if the relationship should not be expired.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_useserverid">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_islocaldependency">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies if this relationship must be packaged and deployed with the owner. If checked, the relationship must be deployed, if not, it is optional.</Description>
               </PSXProperty>
            </PSXPropertySet>
            <UserPropertySet>
               <PSXProperty locked="no" name="sys_slotid">
                  <Value type="String"/>
                  <Description>The slot used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_sortrank">
                  <Value type="String">1</Value>
                  <Description>The sorting rank.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_variantid">
                  <Value type="String"/>
                  <Description>The variant used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_folderid">
                  <Value type="String"/>
                  <Description>The folder id used, optional.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="sys_siteid">
                  <Value type="String"/>
                  <Description>The site id used, optional.</Description>
               </PSXProperty>
            </UserPropertySet>
            <ProcessChecks>
               <PSXProcessCheck context="relationship" name="rs_cloneshallow" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                     <PSXRule boolean="and">
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>category</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>rs_promotable</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>OR</boolean>
                        </PSXConditional>
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>category</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>rs_copy</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
               <PSXProcessCheck context="relationship" name="rs_clonedeep" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                     <PSXRule boolean="and">
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>category</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>rs_translation</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
            </ProcessChecks>
            <Explanation/>
         </PSXRelationshipConfig>
         <PSXRelationshipConfig category="rs_translation" label="Translation" name="Translation" type="system">
            <PSXCloneOverrideFieldList id="0">
               <PSXCloneOverrideField id="0" name="sys_title">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/relationship/sys_CloneTitle</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>[{0}] Copy ($clone_count) of {1}</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.TITLE</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_communityid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/cms/sys_cloneOverrideField</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>../sys_trFieldOverride/TranslationFieldOverride.xml</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>CommunityId</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_contentid</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.CONTENTID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_lang</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_workflowid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/cms/sys_cloneOverrideField</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>../sys_trFieldOverride/TranslationFieldOverride.xml</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>WorkflowId</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_contentid</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.CONTENTID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_lang</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_lang">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/generic/sys_Literal</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
            </PSXCloneOverrideFieldList>
            <ExtensionSet/>
            <PSXPropertySet>
               <PSXProperty locked="yes" name="rs_allowcloning">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies whether or not this relationship can be cloned.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_useownerrevision">
                  <Value type="Boolean">yes</Value>
                  <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usedependentrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_expirationtime">
                  <Value type="Date"/>
                  <Description>Specifies the expiry date and time if this relationship can expire. Do not specify a value if the relationship should not be expired.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_useserverid">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_islocaldependency">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies if this relationship must be packaged and deployed with the owner. If checked, the relationship must be deployed, if not, it is optional.</Description>
               </PSXProperty>
            </PSXPropertySet>
            <ProcessChecks>
               <PSXProcessCheck context="relationship" name="rs_cloneshallow" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
               <PSXProcessCheck context="relationship" name="rs_clonedeep" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
            </ProcessChecks>
            <Explanation/>
            <EffectSet>
               <PSXConditionalEffect activationEndpoint="owner">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_isCloneExists</name>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
               <PSXConditionalEffect activationEndpoint="owner">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_AttachTranslatedFolder</name>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
            </EffectSet>
         </PSXRelationshipConfig>
         <PSXRelationshipConfig category="rs_translation" label="Translation - Mandatory" name="Translation - Mandatory" type="system">
            <PSXCloneOverrideFieldList id="0">
               <PSXCloneOverrideField id="0" name="sys_title">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/relationship/sys_CloneTitle</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>[{0}] Copy ($clone_count) of {1}</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.TITLE</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text/>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_communityid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/cms/sys_cloneOverrideField</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>../sys_trFieldOverride/TranslationFieldOverride.xml</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>CommunityId</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_contentid</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.CONTENTID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_lang</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_workflowid">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/cms/sys_cloneOverrideField</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>../sys_trFieldOverride/TranslationFieldOverride.xml</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>WorkflowId</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_contentid</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXContentItemStatus id="0">
                                 <name>CONTENTSTATUS.CONTENTID</name>
                              </PSXContentItemStatus>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>sys_lang</text>
                              </PSXTextLiteral>
                           </value>
                        </PSXExtensionParamValue>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
               <PSXCloneOverrideField id="0" name="sys_lang">
                  <value>
                     <PSXExtensionCall id="0">
                        <name>Java/global/percussion/generic/sys_Literal</name>
                        <PSXExtensionParamValue id="0">
                           <value>
                              <PSXSingleHtmlParameter id="0">
                                 <name>sys_lang</name>
                              </PSXSingleHtmlParameter>
                           </value>
                        </PSXExtensionParamValue>
                     </PSXExtensionCall>
                  </value>
                  <Conditionals/>
               </PSXCloneOverrideField>
            </PSXCloneOverrideFieldList>
            <ExtensionSet/>
            <EffectSet>
               <PSXConditionalEffect activationEndpoint="owner">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_isCloneExists</name>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
               <PSXConditionalEffect activationEndpoint="dependent">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_PublishMandatory</name>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>no</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
               <PSXConditionalEffect activationEndpoint="dependent">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_UnpublishMandatory</name>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>no</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
               <PSXConditionalEffect activationEndpoint="owner">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_AttachTranslatedFolder</name>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
            </EffectSet>
            <PSXPropertySet>
               <PSXProperty locked="yes" name="rs_allowcloning">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies whether or not this relationship can be cloned.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_useownerrevision">
                  <Value type="Boolean">yes</Value>
                  <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usedependentrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_expirationtime">
                  <Value type="Date"/>
                  <Description>Specifies the expiry date and time if this relationship can expire. Do not specify a value if the relationship should not be expired.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_useserverid">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_islocaldependency">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies if this relationship must be packaged and deployed with the owner. If checked, the relationship must be deployed, if not, it is optional.</Description>
               </PSXProperty>
            </PSXPropertySet>
            <ProcessChecks>
               <PSXProcessCheck context="relationship" name="rs_cloneshallow" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
               <PSXProcessCheck context="relationship" name="rs_clonedeep" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                     <PSXRule boolean="and">
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>category</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>rs_promotable</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
            </ProcessChecks>
            <Explanation/>
         </PSXRelationshipConfig>
         <PSXRelationshipConfig category="rs_folder" label="Folder Content" name="Folder Content" type="system">
            <PSXCloneOverrideFieldList id="0"/>
            <EffectSet>
               <PSXConditionalEffect activationEndpoint="owner">
                  <PSXExtensionCall id="0">
                     <name>Java/global/percussion/relationship/effect/sys_TouchParentFolderEffect</name>
                  </PSXExtensionCall>
               </PSXConditionalEffect>
            </EffectSet>
            <PSXPropertySet>
               <PSXProperty locked="yes" name="rs_allowcloning">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies whether or not this relationship can be cloned.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_useownerrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usedependentrevision">
                  <Value type="Boolean">no</Value>
                  <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_expirationtime">
                  <Value type="Date"/>
                  <Description>Specifies the expiry date and time if this relationship can expire. Do not specify a value if the relationship should not be expired.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_useserverid">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>
               </PSXProperty>
               <PSXProperty locked="no" name="rs_islocaldependency">
                  <Value type="Boolean">no</Value>
                  <Description>Specifies if this relationship must be packaged and deployed with the owner. If checked, the relationship must be deployed, if not, it is optional.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_usecommunityfilter">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies if this relationship will be filtered by community id.</Description>
               </PSXProperty>
               <PSXProperty locked="yes" name="rs_skippromotion">
                  <Value type="Boolean">yes</Value>
                  <Description>Specifies if this relationship should be skipped when an item is promoted.</Description>
               </PSXProperty>
            </PSXPropertySet>
            <ProcessChecks>
               <PSXProcessCheck context="relationship" name="rs_cloneshallow" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>2</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
               <PSXProcessCheck context="relationship" name="rs_clonedeep" sequence="1">
                  <Conditions>
                     <PSXRule boolean="and">
                        <PSXConditional id="1">
                           <variable>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>1</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                     <PSXRule boolean="and">
                        <PSXConditional id="0">
                           <variable>
                              <PSXOriginatingRelationshipProperty id="0">
                                 <name>name</name>
                              </PSXOriginatingRelationshipProperty>
                           </variable>
                           <operator>=</operator>
                           <value>
                              <PSXTextLiteral id="0">
                                 <text>Translation - Mandatory</text>
                              </PSXTextLiteral>
                           </value>
                           <boolean>AND</boolean>
                        </PSXConditional>
                     </PSXRule>
                  </Conditions>
               </PSXProcessCheck>
            </ProcessChecks>
            <Explanation/>
         </PSXRelationshipConfig>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
