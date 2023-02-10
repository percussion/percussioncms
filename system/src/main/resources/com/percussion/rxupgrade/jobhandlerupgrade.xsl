<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!-- main template -->
   <xsl:variable name="deployer" select="*/PSXJobHandlerConfiguration/Categories/Category [@name='deployer']"/>
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
   <xsl:template match="Categories" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
         <xsl:if test="not($deployer)">
            <Category name="deployer">
               <InitParams>
                  <InitParam name="role" value="admin"/>
               </InitParams>
               <Jobs>
                  <Job jobType="export" className="com.percussion.deployer.server.PSExportJob">
                     <InitParams/>
                  </Job>
                  <Job jobType="import" className="com.percussion.deployer.server.PSImportJob">
                     <InitParams/>
                  </Job>
                  <Job jobType="validation" className="com.percussion.deployer.server.PSValidationJob">
                     <InitParams/>
                  </Job>
               </Jobs>
            </Category>
         </xsl:if>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
