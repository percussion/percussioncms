<?xml version="1.0"?>



<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">

  <!-- Output method -->
  <xsl:output encoding="iso-8859-1"
              indent="no"/>

  <xsl:template match="status">
    <html>
    <head>
    	<TITLE>Tomcat Status</TITLE>
		<STYLE type="text/css">
			body, table, tr, td, a, div, span {
				vertical-align : top;
			}
		</STYLE>
    </head>
    <body>
      <div style='font-size:20px;'>Tomcat Status</div>

      <xsl:apply-templates select="jvm"/>
      <xsl:apply-templates select="connector"/>
     </body>
    </html>
  </xsl:template>

  <xsl:template match="jvm">
   <xsl:apply-templates select="memory"/>
  </xsl:template>

  <xsl:template match="memory">
    <table><tr>
    		 <td><b>JVM:</b></td>
    		 <td><b>free:</b> <xsl:value-of select="@free"/></td>
    		 <td><b>total:</b> <xsl:value-of select="@total"/></td>
    		 <td><b>max:</b> <xsl:value-of select="@max"/></td>
    	   </tr>
    </table><hr />
  </xsl:template>

  <xsl:template match="connector">
	 <b>Connector -- </b> <xsl:value-of select="@name"/><br />

  	<xsl:apply-templates select="threadInfo"/>
  	<xsl:apply-templates select="requestInfo"/>
  	<xsl:apply-templates select="workers"/>
  </xsl:template>

  <xsl:template match="threadInfo">
    <table><tr>
    		 <td><b>threadInfo </b></td>
    		 <td><b>maxThreads:</b> <xsl:value-of select="@maxThreads"/></td>
    		 <td><b>minSpareThreads:</b> <xsl:value-of select="@minSpareThreads"/></td>
    		 <td><b>maxSpareThreads:</b> <xsl:value-of select="@maxSpareThreads"/></td>
    		 <td><b>currentThreadCount:</b> <xsl:value-of select="@currentThreadCount"/></td>
    		 <td><b>currentThreadsBusy:</b> <xsl:value-of select="@currentThreadsBusy"/></td>
    	   </tr>
    </table><hr />
  </xsl:template>

  <xsl:template match="requestInfo">
    <table><tr>
    		 <td><b>requestInfo </b></td>
    		 <td><b>maxTime:</b> <xsl:value-of select="@maxTime"/></td>
    		 <td><b>processingTime:</b> <xsl:value-of select="@processingTime"/></td>
    		 <td><b>requestCount:</b> <xsl:value-of select="@requestCount"/></td>
    		 <td><b>errorCount:</b> <xsl:value-of select="@errorCount"/></td>
    		 <td><b>bytesReceived:</b> <xsl:value-of select="@bytesReceived"/></td>
    		 <td><b>bytesSent:</b> <xsl:value-of select="@bytesSent"/></td>
    	   </tr>
    </table><hr />
  </xsl:template>

  <xsl:template match="workers">
   <table>
    <tr><th>Stage</th><th>Time</th><th>B Sent</th><th>B Recv</th><th>Client</th><th>VHost</th><th>Request</th></tr>
  	<xsl:apply-templates select="worker"/>

   </table><hr />
  </xsl:template>

  <xsl:template match="worker">
   <tr>
    <td><xsl:value-of select="@stage"/></td>
    <td><xsl:value-of select="@requestProcessingTime"/></td>
    <td><xsl:value-of select="@requestBytesSent"/></td>
    <td><xsl:value-of select="@requestBytesReceived"/></td>
    <td><xsl:value-of select="@remoteAddr"/></td>
    <td><xsl:value-of select="@virtualHost"/></td>
    <td><xsl:value-of select="@method"/> <xsl:value-of select="@currentUri"/>?<xsl:value-of select="@currentQueryString"/> <xsl:value-of select="@protocol"/></td>
   </tr>
  </xsl:template>

</xsl:stylesheet>
