<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2021 Percussion Software, Inc.
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
		<!ENTITY % HTMLlat1 SYSTEM "../../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "../../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "../../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
		]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:template match="/"/>
	<!--
     percImageSliderControl
 -->
	<psxctl:ControlMeta name="percImageSliderControl" dimension="single" choiceset="none">>
		<psxctl:Description>Provides UI for adding Google Calendars</psxctl:Description>
		<psxctl:ParamList>
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
			<psxctl:FileDescriptor name="PercImageSelectionControl.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../sys_resources/js/PercImageSelectionControl.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercPageSelectionControl.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../sys_resources/js/PercPageSelectionControl.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="all" type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/libraries/fontawesome/css/all.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="percImageSlider.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/widgets/percImageSlider/js/percImageSlider.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="percImageSlider.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../rx_resources/widgets/percImageSlider/css/percImageSlider.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_path_constants.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_path_constants.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_path_manager.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_path_manager.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercSiteService.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/services/PercSiteService.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="PercServiceUtils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>

	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='percImageSliderControl']" mode="psxcontrol">
		<xsl:variable name="lang" select="//@xml:lang"/>
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<label for="perc-image-slider-setup-toggle"></label>
		<div class="perc-image-slider" id="{@paramName}">
			<h2 ><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Add Slide'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></h2>
			<h4 id="slideConfigurationAlert" class="hidden perc-slider-warning"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Slider Warning'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></h4>
			<!--<span id="perc-help-toggle" class="perc-toggle-help fa fa-question-circle" title="Click to toggle help"></span><label for="perc-help-toggle">Click to toggle help.</label>
				<div class="perc-help">
				    Image Slider Help Section
				</div> -->
			<div id="perc-image-slider-setup-editor" class="perc-image-slider-setup-editor">
				<span class="perc-table-add fa fa-plus-square"></span>
				<table class="perc-table">
					<tbody>
						<tr class="perc-header-row">
							<td class="perc-input-header perc-slider-image-preview-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Preview'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></td>
							<td class="perc-input-header perc-slider-image-preview-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Size'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></td>
							<td class="perc-input-header perc-slider-image-path-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Image'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></td>
							<td class="perc-input-header perc-slider-image-caption-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Slide Text'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></td>
							<td class="perc-input-header perc-slider-image-caption-2-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Slide Text'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template> 2</td>
							<td class="perc-input-header perc-slider-image-link-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Internal Link'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></td>
							<td class="perc-input-header perc-slider-image-link-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@External Link'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></td>
							<td class="perc-input-header perc-slider-image-link-td"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Link Type'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></td>
							<td></td>
						</tr>
						<!-- This is the template table row -->
						<tr class="hide perc-image-slider-row" role="presentation">
							<td id="perc-content-image-thumbnail" class="perc-thumbnail-td">
								&nbsp;
							</td>
							<td id="perc-content-image-size" class="perc-image-size-td">
								<i class="fa fa-spinner fa-pulse fa-fw"></i><span class="sr-only"></span>
							</td>
							<td class="perc-input-td perc-slider-image-path-td">
								<input readonly="readonly" id="perc-content-edit-imageInputField" data-perc-widget-event-source="imageSlider" data-perc-image-content-id="" data-perc-image-path-link-id="" type="text" name="imageInputField" class="datadisplay perc-slider-image-path"></input>
								<input alt="Browse Images" id="perc-image-slider-browse-button" class="perc-select-button perc-image-field-select-button" type="button" for="perc-content-edit-imageInputField" value="&#xf002;"></input>
							</td>
							<td class="perc-input-td perc-slider-image-caption-td">
								<input name="imageCaption" class="datadisplay perc-slider-image-caption" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td>
								<input name="imageCaption2" class="datadisplay perc-slider-image-caption-2" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-slider-image-link-td">
								<input readonly="readonly" id="perc-content-edit-imageLinkField" data-perc-page-content-id="" data-perc-page-path-link-id="" type="text" name="imageLinkField" class="datadisplay perc-slider-image-link"></input>
								<input alt="Browse Internal Pages" id="perc-image-slider-browse-button" class="perc-select-button perc-page-field-select-button" type="button" for="perc-content-edit-imageLinkField" value="&#xf002;"></input>
							</td>
							<td class="perc-input-td perc-slider-image-external-link-td">
								<input name="imageExternalLinkField" class="datadisplay perc-slider-image-external-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" placeholder="https://www.percussion.com"></input>
							</td>
							<td class="perc-input-td perc-slider-image-link-setting-td">
								<select id="perc-slider-image-link-setting" class="perc-slider-image-link-setting-select" name="imageLinkSettingField">
									<option value="none"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@None'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></option>
									<option value="internal"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Internal'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></option>
									<option value="external"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@External'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></option>
								</select>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
							<td>
								<span class="perc-table-remove fa fa-minus"></span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div id="slideConfigurationAlertDetails" class="hidden">
				<h4><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Slider Warning Details'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></h4>
				<p id="sliderWarningDetails" class="hidden"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Slider Warning Details Message'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></p>
				<p id="sliderErrorDetails" class="hidden"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'perc.ui.control.imageSlider@Slider Error Details'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></p>
			</div>
		</div>
	</xsl:template>
	<xsl:template match="Control[@name='percImageSliderControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<div class="perc-image-slider-readonly" id="{@paramName}">
			<div style="display:none;">TODO: Finish ME!</div>
		</div>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="perc.ui.control.imageSlider@Add Slide">Add Slide</key>
		<key name="perc.ui.control.imageSlider@Preview">Preview Slide</key>
		<key name="perc.ui.control.imageSlider@Size">Size</key>
		<key name="perc.ui.control.imageSlider@Image">Image</key>
		<key name="perc.ui.control.imageSlider@Slide Text">Slider Text</key>
		<key name="perc.ui.control.imageSlider@Internal Link">Internal Link</key>
		<key name="perc.ui.control.imageSlider@External Link">External Link</key>
		<key name="perc.ui.control.imageSlider@Link Type">Link Type</key>
		<key name="perc.ui.control.imageSlider@Slider Warning">Slider Warning</key>
		<key name="perc.ui.control.imageSlider@None">None</key>
		<key name="perc.ui.control.imageSlider@Internal">Internal</key>
		<key name="perc.ui.control.imageSlider@External">External</key>
		<key name="perc.ui.control.imageSlider@Slider Warning Details">Slider Warning Details</key>
		<key name="perc.ui.control.imageSlider@Slider Warning Details Message">Slider Warning Details Message</key>
		<key name="perc.ui.control.imageSlider@Slider Error Details">Slider Error Message</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
