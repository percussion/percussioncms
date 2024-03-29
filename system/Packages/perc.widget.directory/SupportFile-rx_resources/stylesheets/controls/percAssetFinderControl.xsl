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
                xmlns:psxctl="urn:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">

    <xsl:template match="/"/>
    <!--
     percAssetFinderControl
 -->
    <psxctl:ControlMeta name="percAssetFinderControl" dimension="single" choiceset="none">>
        <psxctl:Description>Provides ability to search through parent folders heirarchy for asset of a sleected content_type name.</psxctl:Description>
        <psxctl:ParamList>
            <psxctl:Param name="content_type" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter assigns the name of the asset type for the control to search for.</psxctl:Description>
            </psxctl:Param>
            <psxctl:Param name="field_to_display" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter field from the asset type for the control to display after locating it. This will be the Column name from the Content Type Table.</psxctl:Description>
            </psxctl:Param>
            <psxctl:Param name="id" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
            </psxctl:Param>
            <psxctl:Param name="class" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
                <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="style" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
            </psxctl:Param>
            <psxctl:Param name="columncount" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter specifies the number of column(s) displayed.</psxctl:Description>
                <psxctl:DefaultValue>1</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="columnwidth" datatype="String" paramtype="generic">
                <psxctl:Description>This parameter specifies the width of the column in pixels or percentage.</psxctl:Description>
                <psxctl:DefaultValue>100%</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
                <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
                <psxctl:DefaultValue>400</psxctl:DefaultValue>
            </psxctl:Param>
            <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
                <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
                <psxctl:DefaultValue>200</psxctl:DefaultValue>
            </psxctl:Param>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
            <psxctl:FileDescriptor name="all.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="AssetFinder.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>/rx_resources/widgets/AssetFinder/css/AssetFinder.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="AssetFinder.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/rx_resources/widgets/AssetFinder/js/AssetFinder.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_path_constants.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/plugins/perc_path_constants.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_path_manager.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/plugins/perc_path_manager.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/plugins/perc_utils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercSiteService.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/services/PercSiteService.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercFolderService.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/services/PercFolderService.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercPathService.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/services/PercPathService.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercServiceUtils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>/cm/services/PercServiceUtils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>
    </psxctl:ControlMeta>
    <xsl:template match="Control[@name='percAssetFinderControl']" mode="psxcontrol">
        <xsl:variable name="controlName" select="'percAssetFinderControl'" />
        <xsl:variable name="content_type">
            <xsl:call-template name="getParam">
                <xsl:with-param name="controlName" select="$controlName" />
                <xsl:with-param name="paramName" select="'content_type'" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="field_to_display">
            <xsl:call-template name="getParam">
                <xsl:with-param name="controlName" select="$controlName" />
                <xsl:with-param name="paramName" select="'field_to_display'" />
            </xsl:call-template>
        </xsl:variable>
        <script >
            // <![CDATA[
		(function($) {
            $(function() {
            // ]]>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'contentType'" />
                <xsl:with-param name="value" select="$content_type" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'fieldToDisplay'" />
                <xsl:with-param name="value" select="$field_to_display" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'paramName'" />
                <xsl:with-param name="value" select="@paramName" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'readonly'" />
                <xsl:with-param name="value" select="@isReadOnly='yes'" />
            </xsl:call-template>
            // <![CDATA[
                readonly = readonly == 'true' ? true : false;
                var opts = {contentType: contentType, fieldToDisplay: fieldToDisplay, paramName: paramName, readonly: readonly};
                $('#perc-asset-finder-' + paramName ).perc_AssetFinder(opts);
            });
		})(jQuery);
		// ]]>
        </script>
        <input type="hidden" name="{@paramName}" id="perc-content-value-{@paramName}" value="{Value}"/>
        <div class="perc-asset-finder" id="perc-asset-finder-{@paramName}" data-paramName="{@paramName}">
            <div class="perc-asset-finder-editor">
                <div class="perc-asset-display-container">
                    <div class="perc-asset-finder-data">
                        <input type="text" name="{@paramName}" id="perc-content-display-{@paramName}" title="#" value="#" readonly="" style="background-color: rgb(230, 230, 233); overflow: hidden; text-overflow: ellipsis; height: 15px; padding-top: 3px; padding-bottom: 5px;" class="datadisplay" size="50" dlg_width="450" dlg_height="160" aarenderer="INPLACE_TEXT" />
                    </div>
                    <div class="perc-asset-finder-error"></div>
                </div>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="Control[@name='percAssetFinderControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
        <xsl:variable name="controlName" select="'percAssetFinderControl'" />
        <xsl:variable name="content_type">
            <xsl:call-template name="getParam">
                <xsl:with-param name="controlName" select="$controlName" />
                <xsl:with-param name="paramName" select="'content_type'" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="field_to_display">
            <xsl:call-template name="getParam">
                <xsl:with-param name="controlName" select="$controlName" />
                <xsl:with-param name="paramName" select="'field_to_display'" />
            </xsl:call-template>
        </xsl:variable>
        <script >
            // <![CDATA[
		(function($) {
            $(function() {
            // ]]>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'contentType'" />
                <xsl:with-param name="value" select="$content_type" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'fieldToDisplay'" />
                <xsl:with-param name="value" select="$field_to_display" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'paramName'" />
                <xsl:with-param name="value" select="@paramName" />
            </xsl:call-template>
            <xsl:call-template name="jsVar">
                <xsl:with-param name="name" select="'readonly'" />
                <xsl:with-param name="value" select="@isReadOnly='yes'" />
            </xsl:call-template>
            // <![CDATA[
                readonly = readonly == 'true' ? true : false;
                var opts = {contentType: contentType, fieldToDisplay: fieldToDisplay, paramName: paramName, readonly: readonly};
                $('#perc-asset-finder-' + paramName ).perc_AssetFinderReadOnly(opts);
            });
		})(jQuery);
		// ]]>
        </script>
        <input type="hidden" name="{@paramName}" id="perc-content-value-{@paramName}" value="{Value}"/>
        <div class="perc-asset-finder-readonly" id="perc-asset-finder-{@paramName}" data-paramName="{@paramName}">
            <div class="perc-asset-finder-editor">
                <div class="perc-asset-display-container">
                    <div class="perc-asset-finder-data">
                        <input type="text" name="{@paramName}" id="perc-content-display-{@paramName}" title="#" value="#" readonly="" style="background-color: rgb(230, 230, 233); overflow: hidden; text-overflow: ellipsis; height: 15px; padding-top: 3px; padding-bottom: 5px;" class="datadisplay" size="50" dlg_width="450" dlg_height="160" aarenderer="INPLACE_TEXT" />
                    </div>
                    <div class="perc-asset-finder-error"></div>
                </div>
            </div>
        </div>
    </xsl:template>
</xsl:stylesheet>
