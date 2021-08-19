<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
        ]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:psxctl="urn:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
    <xsl:template match="/" />
    <!--
         percTagListControl
     -->
    <psxctl:ControlMeta name="percTagListControl" dimension="array" choiceset="required">
        <psxctl:Description>The tag list control.</psxctl:Description>
        <psxctl:ParamList>
            <psxctl:Param name="width" datatype="Number" paramtype="generic">
                <psxctl:Description>Defines the width of the input field</psxctl:Description>
            </psxctl:Param>
            <psxctl:Param name="maxchars" datatype="Number" paramtype="generic">
                <psxctl:Description>The field defines the maximum number of characters that are allowed</psxctl:Description>
            </psxctl:Param>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
            <psxctl:FileDescriptor name="percTagListControl.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>/rx_resources/css/percTagListControl.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="jquery.autocomplete.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.autocomplete.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="jquery.autocomplete.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.autocomplete.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="percTagListControl.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/rx_resources/js/percTagListControl.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>
    </psxctl:ControlMeta>

    <xsl:template match="Control[@name='percTagListControl']" mode="psxcontrol">

        <script >
            percTagListSource = [
            <xsl:choose>
                <xsl:when test="DisplayChoices/DisplayEntry/Value = ''">
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="DisplayChoices/DisplayEntry">&quot;<xsl:value-of select="Value"/>&quot;<xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
            ];

        </script>
        <input type="text" name="{concat(@paramName,'-display')}" id="{concat(@paramName,'-display')}" class="percTagListControl">
            <xsl:attribute name="value"><xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="Value"/><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each></xsl:attribute>
            <xsl:if test="@accessKey!=''">
                <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
            </xsl:if>
            <xsl:call-template name="parametersToAttributes">
                <xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
                <xsl:with-param name="controlNode" select="."/>
            </xsl:call-template>
        </input>
        <select type="text" style="display:none" name="{@paramName}"  id="perc-content-edit-{@paramName}" multiple="1">
        </select>
    </xsl:template>

    <xsl:template match="Control[@name='percTagListControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
        <div class="datadisplay">
            <xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']">
                <input type="text" name="{concat(@paramName,'-display')}" id="{concat(@paramName,'-display')}" class="percTagListControl">
                    <xsl:value-of select="Value"/>
                    <xsl:if test="not(position()=last())">,</xsl:if>
                </input>
            </xsl:for-each>
            <xsl:attribute name="value"><xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="Value"/><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each></xsl:attribute>
            <xsl:if test="@accessKey!=''">
                <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
            </xsl:if>
            <xsl:call-template name="parametersToAttributes">
                <xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
                <xsl:with-param name="controlNode" select="."/>
            </xsl:call-template>
            <input type="hidden" name="{concat(@paramName,'-display')}" id="{concat(@paramName,'-display')}" class="percTagListControl">
                <xsl:attribute name="value"><xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="Value"/><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each></xsl:attribute>
                <xsl:if test="@accessKey!=''">
                    <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
                </xsl:if>
                <xsl:call-template name="parametersToAttributes">
                    <xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
                    <xsl:with-param name="controlNode" select="."/>
                </xsl:call-template>
            </input>
        </div>
    </xsl:template>
</xsl:stylesheet>
