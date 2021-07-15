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

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!-- main template -->
   <xsl:variable name="sysaction" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_action']"/>
   <xsl:variable name="rxwep" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='rx_wep']"/>
   <xsl:variable name="rxephox" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='rx_ephox']"/>
   <xsl:variable name="syscompare" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_compareHandler']"/>
   <xsl:variable name="sysjobHandler"        select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_jobHandler']"/>
   <xsl:variable name="sysdeployerHandler" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_deployerHandler']"/>
   <xsl:variable name="sysdeploymentHandler" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_deploymentHandler']"/>
   <xsl:variable name="syswebServicesHandler" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_webServicesHandler']"/>
   <xsl:variable name="syspsxActiveAssembly" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_psxActiveAssembly']"/>
   <xsl:variable name="sysceFieldsCataloger" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_ceFieldsCataloger']"/>
   <xsl:variable name="sysGlobalTemplateHandler" select="*/RequestHandlerDef/RequestRoots/RequestRoot [@baseName='sys_GlobalTemplateHandler']"/>
   <xsl:template match="/">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!-- copy any attribute or template, also preserve comments -->
   <xsl:template match="node()|@*" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <!-- take away the Label in the UI set -->
   <xsl:template match="RequestHandlerDefs" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
         <xsl:if test="not($sysaction)">
            <RequestHandlerDef handlerName="actionHandler" className="com.percussion.server.actions.PSActionSetRequestHandler" configFile="storedActions.xml">
               <RequestRoots>
                  <RequestRoot baseName="sys_action">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($rxwep)">
            <RequestHandlerDef handlerName="wephandler" className="com.percussion.support.handlers.PSWepHandler" configFile="rx_wep.xml" >
               <RequestRoots>
                  <RequestRoot baseName="rx_wep">
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($rxephox)">
	     <RequestHandlerDef handlerName="EphoxHandler" className="com.percussion.cms.ephox.PSEphoxHandler">
		<RequestRoots>
		   <RequestRoot baseName="rx_ephox">
		      <RequestType>GET</RequestType>
		   </RequestRoot>
		</RequestRoots>
	     </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($syscompare)">
            <RequestHandlerDef className="com.percussion.server.compare.PSCompareRequestHandler" configFile="compare.xml" handlerName="compareHandler">
               <RequestRoots>
                  <RequestRoot baseName="sys_compareHandler">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($sysjobHandler)">
            <RequestHandlerDef handlerName="jobhandler" className="com.percussion.server.job.PSJobHandler" configFile="jobhandler.xml">
               <RequestRoots>
                  <RequestRoot baseName="sys_jobHandler">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($sysdeploymentHandler)">
            <RequestHandlerDef handlerName="deploymenthandler" className="com.percussion.deploy.server.PSDeploymentHandler">
               <RequestRoots>
                  <RequestRoot baseName="sys_deploymentHandler">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
    	 <xsl:if test="not($sysdeployerHandler)">
            <RequestHandlerDef handlerName="deployerhandler" className="com.percussion.deployer.server.PSDeploymentHandler">
               <RequestRoots>
                  <RequestRoot baseName="sys_deployerHandler">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($syswebServicesHandler)">
            <RequestHandlerDef handlerName="webServicesHandler" className="com.percussion.server.webservices.PSWebServicesRequestHandler" configFile="webservices.xml">
               <RequestRoots>
                  <RequestRoot baseName="sys_webServicesHandler">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($syspsxActiveAssembly)">
            <RequestHandlerDef handlerName="activeAssemblyHandler" className="com.percussion.cms.handlers.PSActiveAssemblyRequestHandler" configFile="activeassembly.xml">
               <RequestRoots>
                  <RequestRoot baseName="sys_psxActiveAssembly">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                  </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
         <xsl:if test="not($sysceFieldsCataloger)">
            <RequestHandlerDef handlerName="sys_ceFieldsCataloger" className="com.percussion.cms.objectstore.server.PSCatalogServerObjectHandler">
               <RequestRoots>
                  <RequestRoot baseName="sys_ceFieldsCataloger">
                     <RequestType>POST</RequestType>
                     <RequestType>GET</RequestType>
                   </RequestRoot>
               </RequestRoots>
            </RequestHandlerDef>
         </xsl:if>
	 <xsl:if test="not($sysGlobalTemplateHandler)">
            <RequestHandlerDef className="com.percussion.cms.handlers.PSGlobalTemplateUpdateHandler" handlerName="sys_GlobalTemplateHandler" configFile="globaltemplates.xml">
		<RequestRoots>
			<RequestRoot baseName="sys_GlobalTemplateHandler">
				<RequestType>GET</RequestType>
			</RequestRoot>
		</RequestRoots>
	    </RequestHandlerDef>
         </xsl:if>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
