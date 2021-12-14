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

<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
        xmlns:wadl="http://wadl.dev.java.net/2009/02"
        xmlns="http://www.w3.org/1999/xhtml"
>

<!-- Global variables -->
<xsl:variable name="g_resourcesBase" select="wadl:application/wadl:resources/@base"/>

<!-- Template for top-level doc element -->
<xsl:template match="wadl:application">
    <html>
    <head>
        <meta charset="UTF-8" />
        <xsl:call-template name="getStyle"/>
        <title><xsl:call-template name="getTitle"/></title>
    </head>
    <body>
    <h1><xsl:call-template name="getTitle"/></h1>
    <xsl:call-template name="getDoc">
        <xsl:with-param name="base" select="$g_resourcesBase"/>
    </xsl:call-template>
    
    <!-- Summary -->
    <h2>Summary</h2>
    <table>
        <tr>
            <th>Resource</th>
            <th>Method</th>
            <th>Description</th>
        </tr>
        <xsl:for-each select="wadl:resources/wadl:resource">
            <xsl:call-template name="processResourceSummary">
                <xsl:with-param name="resourceBase" select="$g_resourcesBase"/>
                <xsl:with-param name="resourcePath" select="@path"/>
                <xsl:with-param name="lastResource" select="position() = last()"/>
            </xsl:call-template>
        </xsl:for-each>
    </table>
    <p></p>
    
    <!-- Grammars -->
    <xsl:if test="wadl:grammars/wadl:include">
        <h2>Grammars</h2>
        <p>
            <xsl:for-each select="wadl:grammars/wadl:include">
                <xsl:variable name="href" select="@href"/>
                <a href="{$href}"><xsl:value-of select="$href"/></a>
                <xsl:if test="position() != last()"><br/></xsl:if>  <!-- Add a spacer -->
            </xsl:for-each>
        </p>
    </xsl:if>

    <!-- Detail -->
    <h2>Resources</h2>
    <xsl:for-each select="wadl:resources">
        <xsl:call-template name="getDoc">
            <xsl:with-param name="base" select="$g_resourcesBase"/>
        </xsl:call-template>
        <br/>
    </xsl:for-each>
    
    <xsl:for-each select="wadl:resources/wadl:resource">
        <xsl:call-template name="processResourceDetail">
            <xsl:with-param name="resourceBase" select="$g_resourcesBase"/>
            <xsl:with-param name="resourcePath" select="@path"/>
        </xsl:call-template>
    </xsl:for-each>

    </body>
    </html>
</xsl:template>

<!-- Supporting templates (functions) -->

<xsl:template name="processResourceSummary">
    <xsl:param name="resourceBase"/>
    <xsl:param name="resourcePath"/>
    <xsl:param name="lastResource"/>

    <xsl:if test="wadl:method">
        <tr>
            <!-- Resource -->
            <td class="summary">
                <xsl:variable name="id"><xsl:call-template name="getId"/></xsl:variable>
                <a href="#{$id}">
                    <xsl:call-template name="getFullResourcePath">
                        <xsl:with-param name="base" select="$resourceBase"/>                
                        <xsl:with-param name="path" select="$resourcePath"/>                
                    </xsl:call-template>
                </a>
            </td>
            <!-- Method -->
            <td class="summary">
                <xsl:for-each select="wadl:method">
                    <xsl:variable name="name" select="@name"/>
                    <xsl:variable name="id2"><xsl:call-template name="getId"/></xsl:variable>
                    <a href="#{$id2}"><xsl:value-of select="$name"/></a>
                    <xsl:for-each select="wadl:doc"><br/></xsl:for-each>
                    <xsl:if test="position() != last()"><br/></xsl:if>  <!-- Add a spacer -->
                </xsl:for-each>
                <br/>
            </td>
            <!-- Description -->
            <td class="summary">
                <xsl:for-each select="wadl:method">
                    <xsl:call-template name="getDoc">
                        <xsl:with-param name="base" select="$resourceBase"/>
                    </xsl:call-template>
                    <br/>
                    <xsl:if test="position() != last()"><br/></xsl:if>  <!-- Add a spacer -->
                </xsl:for-each>
            </td>
        </tr>
        <!-- Add separator if not the last resource -->
        <xsl:if test="wadl:method and not($lastResource)">
            <tr><td class="summarySeparator"></td><td class="summarySeparator"/><td class="summarySeparator"/></tr>
        </xsl:if>
    </xsl:if>   <!-- wadl:method -->

    <!-- Call recursively for child resources -->
    <xsl:for-each select="wadl:resource">
        <xsl:variable name="base">
            <xsl:call-template name="getFullResourcePath">
                <xsl:with-param name="base" select="$resourceBase"/>                
                <xsl:with-param name="path" select="$resourcePath"/>           
            </xsl:call-template>
        </xsl:variable>
        <xsl:call-template name="processResourceSummary">
            <xsl:with-param name="resourceBase" select="$base"/>
            <xsl:with-param name="resourcePath" select="@path"/>
            <xsl:with-param name="lastResource" select="$lastResource and position() = last()"/>
        </xsl:call-template>
    </xsl:for-each>

</xsl:template>

<xsl:template name="processResourceDetail">
    <xsl:param name="resourceBase"/>
    <xsl:param name="resourcePath"/>

    <xsl:if test="wadl:method">
        <h3>
            <xsl:variable name="id"><xsl:call-template name="getId"/></xsl:variable>
            <a name="{$id}">
                <xsl:call-template name="getFullResourcePath">
                    <xsl:with-param name="base" select="$resourceBase"/>                
                    <xsl:with-param name="path" select="$resourcePath"/>                
                </xsl:call-template>
            </a>
        </h3>
        <p>
            <xsl:call-template name="getDoc">
                <xsl:with-param name="base" select="$resourceBase"/>
            </xsl:call-template>
        </p>

        <h5>Methods</h5>

        <div class="methods">
            <xsl:for-each select="wadl:method">
            <div class="method">
                <table class="methodNameTable">
                    <tr>
                        <td class="methodNameTd" style="font-weight: bold">
                            <xsl:variable name="name" select="@name"/>
                            <xsl:variable name="id2"><xsl:call-template name="getId"/></xsl:variable>
                            <a name="{$id2}"><xsl:value-of select="$name"/></a>
                        </td>
                        <td class="methodNameTd" style="text-align: right">
                            <xsl:if test="@id">
                                <xsl:value-of select="@id"/>() 
                            </xsl:if>
                        </td>
                    </tr>
                </table>
                <p>
                    <xsl:call-template name="getDoc">
                        <xsl:with-param name="base" select="$resourceBase"/>
                    </xsl:call-template>
                </p>

                <!-- Request -->
                <h6>request</h6>
                <div style="margin-left: 2em">  <!-- left indent -->
                <xsl:choose>
                    <xsl:when test="wadl:request">
                        <xsl:for-each select="wadl:request">
                            <xsl:call-template name="getParamBlock">
                                <xsl:with-param name="style" select="'template'"/>
                            </xsl:call-template>
                    
                            <xsl:call-template name="getParamBlock">
                                <xsl:with-param name="style" select="'matrix'"/>
                            </xsl:call-template>
                    
                            <xsl:call-template name="getParamBlock">
                                <xsl:with-param name="style" select="'header'"/>
                            </xsl:call-template>
                    
                            <xsl:call-template name="getParamBlock">
                                <xsl:with-param name="style" select="'query'"/>
                            </xsl:call-template>
                    
                            <xsl:call-template name="getRepresentations"/>
                        </xsl:for-each> <!-- wadl:request -->
                    </xsl:when>
    
                    <xsl:when test="not(wadl:request) and (ancestor::wadl:*/wadl:param)">
                        <xsl:call-template name="getParamBlock">
                            <xsl:with-param name="style" select="'template'"/>
                        </xsl:call-template>
                
                        <xsl:call-template name="getParamBlock">
                            <xsl:with-param name="style" select="'matrix'"/>
                        </xsl:call-template>
                
                        <xsl:call-template name="getParamBlock">
                            <xsl:with-param name="style" select="'header'"/>
                        </xsl:call-template>
                
                        <xsl:call-template name="getParamBlock">
                            <xsl:with-param name="style" select="'query'"/>
                        </xsl:call-template>
                
                        <xsl:call-template name="getRepresentations"/>
                    </xsl:when>
            
                    <xsl:otherwise>
                        unspecified
                    </xsl:otherwise>
                </xsl:choose>
                </div>  <!-- left indent for request -->
                                
                <!-- Response -->
                <h6>responses</h6>
                <div style="margin-left: 2em">  <!-- left indent -->
                <xsl:choose>
                    <xsl:when test="wadl:response">
                        <xsl:for-each select="wadl:response">
                            <div class="h8">status: </div>
                            <xsl:choose>
                                <xsl:when test="@status">
                                    <xsl:value-of select="@status"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    200 - OK
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:for-each select="wadl:doc">
                                <xsl:if test="@title">
                                    - <xsl:value-of select="@title"/>
                                </xsl:if>
                                <xsl:if test="text()">
                                    - <xsl:value-of select="text()"/>
                                </xsl:if>
                            </xsl:for-each>
                            
                            <!-- Get response headers/representations -->
                            <xsl:if test="wadl:param or wadl:representation">
                                <div style="margin-left: 2em"> <!-- left indent -->
                                <xsl:if test="wadl:param">
                                    <div class="h7">headers</div>
                                    <table>
                                        <xsl:for-each select="wadl:param[@style='header']">
                                            <xsl:call-template name="getParams"/>
                                        </xsl:for-each>
                                    </table>
                                </xsl:if>
    
                                <xsl:call-template name="getRepresentations"/>
                                </div>  <!-- left indent for response headers/representations -->
                            </xsl:if>
                        </xsl:for-each> <!-- wadl:response -->
                    </xsl:when>
                    <xsl:otherwise>
                        unspecified
                    </xsl:otherwise>
                </xsl:choose>                
                </div>  <!-- left indent for responses -->

            </div>  <!-- class=method -->
            </xsl:for-each> <!-- wadl:method  -->
        </div> <!-- class=methods -->

    </xsl:if>   <!-- wadl:method -->

    <!-- Call recursively for child resources -->
    <xsl:for-each select="wadl:resource">
        <xsl:variable name="base">
            <xsl:call-template name="getFullResourcePath">
                <xsl:with-param name="base" select="$resourceBase"/>                
                <xsl:with-param name="path" select="$resourcePath"/>           
            </xsl:call-template>
        </xsl:variable>
        <xsl:call-template name="processResourceDetail">
            <xsl:with-param name="resourceBase" select="$base"/>
            <xsl:with-param name="resourcePath" select="@path"/>
        </xsl:call-template>
    </xsl:for-each> <!-- wadl:resource -->
</xsl:template>

<xsl:template name="getFullResourcePath">
    <xsl:param name="base"/>
    <xsl:param name="path"/>
    <xsl:choose>
        <xsl:when test="substring($base, string-length($base)) = '/'">
            <xsl:value-of select="$base"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="concat($base, '/')"/>
        </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
        <xsl:when test="starts-with($path, '/')">
            <xsl:value-of select="substring($path, 2)"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$path"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="getDoc">
    <xsl:param name="base"/>
    <xsl:for-each select="wadl:doc">
        <xsl:if test="position() > 1"><br/></xsl:if>
        <xsl:if test="@title and local-name(..) != 'application'">
            <xsl:value-of select="@title"/>:
        </xsl:if>
        <xsl:variable name="content" select="."/>
        <xsl:choose>
            <xsl:when test="@title = 'Example'">
                <xsl:variable name="url">
                    <xsl:choose>
                        <xsl:when test="string-length($base) > 0">
                            <xsl:call-template name="getFullResourcePath">
                                <xsl:with-param name="base" select="$base"/>                
                                <xsl:with-param name="path" select="text()"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise><xsl:value-of select="text()"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <a href="{$url}"><xsl:value-of select="$url"/></a>
            </xsl:when>
            <xsl:when test="contains($content, 'Example')">
               	<div style="white-space:pre-wrap"><pre><xsl:value-of select="."/></pre></div>
            </xsl:when>
            <xsl:otherwise>
               	<xsl:value-of select="$content"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
</xsl:template>

<xsl:template name="getId">
    <xsl:choose>
        <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="generate-id()"/></xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="getParamBlock">
    <xsl:param name="style"/>
    <xsl:if test="ancestor-or-self::wadl:*/wadl:param[@style=$style]">
        <div class="h7"><xsl:value-of select="$style"/> params</div>
        <table>
            <xsl:for-each select="ancestor-or-self::wadl:*/wadl:param[@style=$style]">
                <xsl:call-template name="getParams"/>
            </xsl:for-each>
        </table>
        <p/>
    </xsl:if>
</xsl:template>

<xsl:template name="getParams">
    <tr>
        <td style="font-weight: bold"><xsl:value-of select="@name"/></td>
            <td>
                <xsl:if test="not(@type) and not(@fixed)">
                    unspecified type
                </xsl:if>
                <xsl:call-template name="getHyperlinkedElement">
                    <xsl:with-param name="qname" select="@type"/>
                </xsl:call-template>
                <xsl:if test="@required = 'true'"><br/>(required)</xsl:if>
                <xsl:if test="@repeating = 'true'"><br/>(repeating)</xsl:if>
                <xsl:if test="@default"><br/>default: <tt><xsl:value-of select="@default"/></tt></xsl:if>
                <xsl:if test="@type and @fixed"><br/></xsl:if>
                <xsl:if test="@fixed">fixed: <tt><xsl:value-of select="@fixed"/></tt></xsl:if>
                <xsl:if test="wadl:option">
                    <br/>options:
                    <xsl:for-each select="wadl:option">
                        <xsl:choose>
                            <xsl:when test="@mediaType">
                                <br/><tt><xsl:value-of select="@value"/> (<xsl:value-of select="@mediaType"/>)</tt>
                            </xsl:when>
                            <xsl:otherwise>
                                <tt><xsl:value-of select="@value"/></tt>
                                <xsl:if test="position() != last()">, </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:if>
            </td>
        <xsl:if test="wadl:doc">
            <td><xsl:value-of select="wadl:doc"/></td>
        </xsl:if>
    </tr>
</xsl:template>

<xsl:template name="getHyperlinkedElement">
    <xsl:param name="qname"/>
    <xsl:variable name="prefix" select="substring-before($qname,':')"/>
    <xsl:variable name="ns-uri" select="./namespace::*[name()=$prefix]"/>
    <xsl:variable name="localname" select="substring-after($qname, ':')"/>
    <xsl:choose>
        <xsl:when test="$ns-uri='http://www.w3.org/2001/XMLSchema' or $ns-uri='http://www.w3.org/2001/XMLSchema-instance'">
            <a href="http://www.w3.org/TR/xmlschema-2/#{$localname}"><xsl:value-of select="$localname"/></a>
        </xsl:when>
        <xsl:when test="$ns-uri and starts-with($ns-uri, 'http://www.w3.org/XML/') = false">
            <a href="{$ns-uri}#{$localname}"><xsl:value-of select="$localname"/></a>
        </xsl:when>
        <xsl:when test="$qname">
            <xsl:value-of select="$qname"/>
        </xsl:when>
    </xsl:choose>
</xsl:template>



<xsl:template name="getRepresentations">
    <xsl:if test="wadl:representation">
        <div class="h7">representations</div>
        <table>
            <xsl:for-each select="wadl:representation">
                <tr>
                    <td style="font-weight: bold"><xsl:value-of select="@mediaType" /></td>
			        <xsl:choose>
	                    <xsl:when test="@href or @element">
	                        <td>
	                            <xsl:variable name="href" select="@href" />
	                            <xsl:choose>
	                                <xsl:when test="@href">
										<xsl:variable name="localname" select="substring-after(@element, ':')"/>
	                                    <a href="{$href}"><xsl:value-of select="$localname" /></a>
	                                </xsl:when>
	                                <xsl:otherwise>
						                <xsl:call-template name="getHyperlinkedElement">
						                    <xsl:with-param name="qname" select="@element" />
						                </xsl:call-template>
	                                </xsl:otherwise>
	                            </xsl:choose>
	                        </td>
	                    </xsl:when>
	                    <xsl:otherwise><td/></xsl:otherwise>
					</xsl:choose>  
					                  
                    <xsl:if test="wadl:doc">
                        <td>
                            <xsl:call-template name="getDoc">
                                <xsl:with-param name="base" select="''" />
                            </xsl:call-template>
                        </td>
                    </xsl:if>
                </tr>
                <xsl:call-template name="getRepresentationParamBlock">
                    <xsl:with-param name="style" select="'template'" />
                </xsl:call-template>
        
                <xsl:call-template name="getRepresentationParamBlock">
                    <xsl:with-param name="style" select="'matrix'" />
                </xsl:call-template>
        
                <xsl:call-template name="getRepresentationParamBlock">
                    <xsl:with-param name="style" select="'header'" />
                </xsl:call-template>
        
                <xsl:call-template name="getRepresentationParamBlock">
                    <xsl:with-param name="style" select="'query'" />
                </xsl:call-template>
            </xsl:for-each>
        </table>
    </xsl:if> 
</xsl:template><xsl:template name="getRepresentationParamBlock">
    <xsl:param name="style"/>
    <xsl:if test="wadl:param[@style=$style]">
        <tr>
            <td style="padding: 0em, 0em, 0em, 2em">
                <div class="h7"><xsl:value-of select="$style"/> params</div>
                <table>
                    <xsl:for-each select="wadl:param[@style=$style]">
                        <xsl:call-template name="getParams"/>
                    </xsl:for-each>
                </table>
                <p/>
            </td>
        </tr>
    </xsl:if>
</xsl:template>

<xsl:template name="getStyle">
     <style type="text/css">
        body {
            font-family: lucida, sans-serif;
            font-size: 0.85em;
            margin: 2em 2em;
        }
        .methods {
            margin-left: 2em; 
            margin-bottom: 2em;
        }
        .method {
            background-color: #eef;
            border: 1px solid #DDDDE6;
            padding: .5em;
            margin-bottom: 1em;
            width: 50em
        }
        .methodNameTable {
            width: 100%;
            border: 0px;
            border-bottom: 2px solid white;
            font-size: 1.4em;
        }
        .methodNameTd {
            background-color: #eef;
        }
        h1 {
            font-size: 2m;
            margin-bottom: 0em;
        }
        h2 {
            border-bottom: 1px solid black;
            margin-top: 1.5em;
            margin-bottom: 0.5em;
            font-size: 1.5em;
           }
        h3 {
            color: #FF6633;
            font-size: 1.35em;
            margin-top: .5em;
            margin-bottom: 0em;
        }
        h5 {
            font-size: 1.2em;
            color: #99a;
            margin: 0.5em 0em 0.25em 0em;
        }
        h6 {
            color: #700000;
            font-size: 1em;
            margin: 1em 0em 0em 0em;
        }
        .h7 {
            margin-top: .75em;
            font-size: 1em;
            font-weight: bold;
            font-style: italic;
            color: blue;
        }
        .h8 {
            margin-top: .75em;
            font-size: 1em;
            font-weight: bold;
            font-style: italic;
            color: black;
        }
        tt {
            font-size: 1em;
        }
        table {
            margin-bottom: 0.5em;
            border: 1px solid #E0E0E0;
        }
        th {
            text-align: left;
            font-weight: normal;
            font-size: 1em;
            color: black;
            background-color: #DDDDE6;
            padding: 3px 6px;
            border: 1px solid #B1B1B8;
        }
        td {
            padding: 3px 6px;
            vertical-align: top;
            background-color: #F6F6FF;
            font-size: 0.85em;
        }
        p {
            margin-top: 0em;
            margin-bottom: 0em;
        }
        td.summary {
            background-color: white;
        }
        td.summarySeparator {
            padding: 1px;
        }
    </style>
</xsl:template>

<xsl:template name="getTitle">
    <xsl:choose>
        <xsl:when test="wadl:doc/@title">
            <xsl:value-of select="wadl:doc/@title"/>
        </xsl:when>
        <xsl:otherwise>
            Web Application
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
