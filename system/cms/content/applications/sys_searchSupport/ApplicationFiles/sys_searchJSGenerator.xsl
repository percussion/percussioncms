<?xml version="1.0" encoding="UTF-8"?>
<!-- This style sheet is used for the generation of javascript block for keyword support.
   The generated javascript will look like the following one.
   For each keyword data a javascript array will be created.
   var fieldname_parentfieldnames_ids = new Array();
   For the default choices fieldname_id and fieldname_value arrays will be created.
   Javascript functions will be created to handle the onchange events.
-->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">
   <xsl:template match="KeywordDependencies" mode="kwjs1">
      <xsl:call-template name="StartScript"/>
      <xsl:for-each select="KeywordField">
         <!-- Create XSL variable named keyname with name of the KeywordField and ParentFields seperated by _.-->
         <xsl:variable name="keyname">
            <xsl:value-of select="@name"/>
            <xsl:for-each select="ParentField">
               <xsl:text>_</xsl:text>
               <xsl:value-of select="."/>
            </xsl:for-each>
         </xsl:variable>
         <!-- Create javascript array variables with keyname and Key -->
         <xsl:for-each select="KeywordData">
            <xsl:value-of select="concat('psSearch.',$keyname)"/>
            <xsl:for-each select="Key">
               <xsl:text>_</xsl:text>
               <xsl:value-of select="."/>
            </xsl:for-each>
            <xsl:text> = new Array(</xsl:text>
            <xsl:for-each select="DisplayChoices/DisplayEntry">
               <xsl:text>"</xsl:text>
               <xsl:value-of select="Value"/>
               <xsl:text>"</xsl:text>
               <xsl:if test="position()!=last()">
                  <xsl:text>,</xsl:text>
               </xsl:if>
            </xsl:for-each>
            <xsl:text>);
            </xsl:text>
         </xsl:for-each>
         <!-- Create a javascript array of default choice values-->
         <xsl:value-of select="concat('psSearch.',@name,'_values')"/>
         <xsl:text> = new Array(</xsl:text>
         <xsl:for-each select="DefaultChoices/DisplayChoices/DisplayEntry">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="Value"/>
            <xsl:text>"</xsl:text>
            <xsl:if test="position()!=last()">
               <xsl:text>,</xsl:text>
            </xsl:if>
         </xsl:for-each>
         <xsl:text>);
         </xsl:text>
         <!-- Create a javascript associative array of default choice names-->
         <xsl:value-of select="concat('psSearch.',@name,'_names')"/>
         <xsl:text> = new Array();
         </xsl:text>
         <xsl:variable name="temp" select="concat('psSearch.',@name,'_names')"/>
         <xsl:for-each select="DefaultChoices/DisplayChoices/DisplayEntry">
            <xsl:value-of select="$temp"/>
            <xsl:text>["</xsl:text>
            <xsl:value-of select="Value"/>
            <xsl:text>"] = "</xsl:text>
            <xsl:value-of select="DisplayLabel"/>
            <xsl:text>";
            </xsl:text>
         </xsl:for-each>
         <!-- Create function with KeywordData_change-->
         <xsl:value-of select="concat('psSearch.',@name,'_change = ')"/>
         <xsl:text>function()</xsl:text>
         <xsl:text>{</xsl:text>
         <!-- Create a selList variable with selected values-->
         <xsl:text>var selList = new Array();
         </xsl:text>
         <xsl:text>var counter = 0;
         </xsl:text>
         <xsl:text>for(i=0;i</xsl:text>
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
         <xsl:text>document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options.length;i++)
            {
         </xsl:text>
         <xsl:text>if(document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options[i].selected)
            {
         </xsl:text>
         <xsl:text>selList[counter] = document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options[i].value;
            counter++;
            }
            }
         </xsl:text>
         <!-- Create a valList variable with the union of available values-->
         <xsl:text>var valList = new Array();
         </xsl:text>
         <xsl:for-each select="ParentField">
            <xsl:text>for(i</xsl:text>
            <xsl:value-of select="position()"/>
            <xsl:text>=0;i</xsl:text>
            <xsl:value-of select="position()"/>
            <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
            <xsl:text>document.searchQuery.</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text>_1.options.length;i</xsl:text>
            <xsl:value-of select="position()"/>
            <xsl:text>++)
               {
            </xsl:text>
            <xsl:text>if(document.searchQuery.</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text>_1.options[i</xsl:text>
            <xsl:value-of select="position()"/>
            <xsl:text>].selected)
               {
            </xsl:text>
            <xsl:if test="position()=last()">
               <xsl:text>if(eval("psSearch.</xsl:text>
               <xsl:value-of select="$keyname"/>
               <xsl:text>"</xsl:text>
               <xsl:for-each select="../ParentField">
                  <xsl:text> + "_"  +  </xsl:text>
                  <xsl:text>document.searchQuery.</xsl:text>
                  <xsl:value-of select="."/>
                  <xsl:text>_1.options[i</xsl:text>
                  <xsl:value-of select="position()"/>
                  <xsl:text>].value</xsl:text>
               </xsl:for-each>
               <xsl:text>)  instanceof Array)
                  {
               </xsl:text>
               <xsl:text>valList = valList.concat(eval("psSearch.</xsl:text>
               <xsl:value-of select="$keyname"/>
               <xsl:text>"</xsl:text>
               <xsl:for-each select="../ParentField">
                  <xsl:text> + "_"  +  </xsl:text>
                  <xsl:text>document.searchQuery.</xsl:text>
                  <xsl:value-of select="."/>
                  <xsl:text>_1.options[i</xsl:text>
                  <xsl:value-of select="position()"/>
                  <xsl:text>].value</xsl:text>
               </xsl:for-each>
               <xsl:text>));
               </xsl:text>
               <xsl:text>
                  }
               </xsl:text>
            </xsl:if>
         </xsl:for-each>
         <xsl:for-each select="ParentField">
            <xsl:text>
               }
               }
            </xsl:text>
         </xsl:for-each>
         <xsl:text>for (var i=0;i</xsl:text>
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
         <xsl:text>valList.length;i++)
            {
         </xsl:text>
         <xsl:text>var src=valList[i];</xsl:text>
         <xsl:text>for (var j=i+1;j</xsl:text>
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
         <xsl:text>valList.length;j++)
            if (valList[j]==src) valList.splice(j--,1);
         </xsl:text>
         <xsl:text>
            }
         </xsl:text>
         <xsl:text>
            if (document.searchQuery.</xsl:text>
         <xsl:value-of select="ParentField"/>
         <xsl:text>_1.selectedIndex == -1)
            {
            valList = </xsl:text>
         <xsl:value-of select="concat('psSearch.',@name)"/>
         <xsl:text>_values;
            }
         </xsl:text>
         
         <xsl:text>document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options.length</xsl:text>
         <xsl:text> = valList.length;
         </xsl:text>
         <xsl:text>for(i=0; i</xsl:text>
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
         <xsl:text>valList.length; i++)
            {
         </xsl:text>
         <xsl:text>document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options[i].value=valList[i];</xsl:text>
         <xsl:text>document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options[i].text=</xsl:text>
         <xsl:value-of select="concat('psSearch.',@name)"/>
         <xsl:text>_names[valList[i]];
         </xsl:text>
         <xsl:text>document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options[i].selected=false;</xsl:text>
         <xsl:text>}
         </xsl:text>
         <xsl:text>for(i=0;i</xsl:text>
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
         <xsl:text>selList.length</xsl:text>
         <xsl:text>;i++)
            {
         </xsl:text>
         <xsl:text>for(j=0;j</xsl:text>
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
         <xsl:text>document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options.length;j++)
            {
         </xsl:text>
         <xsl:text>if(document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options[j].value == selList[i])
            {
         </xsl:text>
         <xsl:text>document.searchQuery.</xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:text>_1.options[j].selected=true;
            break;
         </xsl:text>
         <xsl:text>
            }
            }
            }
         </xsl:text>
         <xsl:variable name="kname" select="@name"/>
         <xsl:for-each select="../KeywordField[ParentField=$kname]">
            <xsl:value-of select="@name"/>
            <xsl:text>_change();</xsl:text>
         </xsl:for-each>
         <xsl:text>
            }
         </xsl:text>
      </xsl:for-each>
      <xsl:call-template name="EndScript"/>
   </xsl:template>
   <xsl:template match="KeywordDependencies" mode="kwjs2">
      <xsl:call-template name="StartScript"/>
      <xsl:for-each select="KeywordField">
         <!-- Call function with KeywordData_change-->
         <xsl:value-of select="concat('psSearch.',@name,'_change();')"/>
      </xsl:for-each>
      <xsl:call-template name="EndScript"/>
   </xsl:template>
   <xsl:template name="StartScript">
      <xsl:if test="not($generationMode and $generationMode='aaJS')">
         <xsl:value-of disable-output-escaping="yes" select="'&lt;script&gt;'"/>           
      </xsl:if>
   </xsl:template>
   <xsl:template name="EndScript">
      <xsl:if test="not($generationMode and $generationMode='aaJS')">
         <xsl:value-of disable-output-escaping="yes" select="'&lt;/script&gt;'"/>           
      </xsl:if>
   </xsl:template>
</xsl:stylesheet>
