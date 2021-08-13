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

<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:psxctl="URN:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">
	<xsl:template match="/"/>
	<!--
     percSocialButtons
 -->
	<psxctl:ControlMeta name="percSocialButtonsControl" dimension="single" choiceset="none">>
		<psxctl:Description>Provides UI for configuring social buttons</psxctl:Description>
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
            <psxctl:DefaultValue>600</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>200</psxctl:DefaultValue>
         </psxctl:Param>
        </psxctl:ParamList>
		<psxctl:AssociatedFileList>
        	<psxctl:FileDescriptor name="all.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/libraries/fontawesome/css/all.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="percSocialButtons.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/widgets/percSocialButtons/js/percSocialButtons.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="percSocialButtons.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../rx_resources/widgets/percSocialButtons/css/percSocialButtons.css</psxctl:FileLocation>
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
	<xsl:template match="Control[@name='percSocialButtonsControl']" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<label for="perc-social-button-setup-toggle"></label>
		<div class="perc-social-buttons" id="{@paramName}">
			<h2>Social Button Content Configuration</h2>
			<ul>
			<li><h5>This widget references icons from the Fontawesome library. Please be sure to include the Fontawesome library in your page or template.</h5></li>
			<li><h5>The Google Tag Manager script must be placed in your template for the data push to work properly. View <a href="https://support.google.com/tagmanager/answer/6103696" target="_blank" rel="noopener noreferrer">this page</a> for more information.</h5></li>
		</ul>
			<!--<span id="perc-help-toggle" class="perc-toggle-help fa fa-question-circle" title="Click to toggle help"></span><label for="perc-help-toggle">Click to toggle help.</label>
				<div class="perc-help">
				    Social Button Help Section
				</div> -->
			<div id="perc-social-buttons-setup-editor" class="perc-social-buttons-setup-editor">
				<table class="perc-table">
					<tbody>
						<tr>
							<td>
								<p><b>Button Functionality</b></p>
								<p>
									<select id="perc-social-button-type">
							        <option value="share">Share current page</option>
							        <option value="page">Link to your social media pages</option>
							    </select>
								</p>
							</td>
						</tr>
					</tbody>
				</table>
				<div class="hide perc-social-button-error">Invalid Configuration.<br/>When using social media page links, ensure that all enabled buttons have an assigned a valid social media link.</div>
				<table class="perc-table">
					<tbody>
						<tr class="perc-social-header">
							<td class="perc-hidden-input"></td>
							<td class="perc-input-header perc-social-button-td perc-platform-name">Platform</td>
							<td class="perc-input-header perc-social-button-td">Enable Button</td>
							<td class="perc-input-header perc-social-button-td">Enable GTM Push</td>
							<td class="perc-input-header perc-social-button-td perc-social-media-link">Social Media URL</td>
						</tr>
						<tr class="perc-social-button-row perc-facebook-row" role="presentation">
							<td class="perc-hidden-input">
								<input name="platform" class="datadisplay perc-social-button-input perc-social-platform" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" value="facebook" type="hidden"></input>
							</td>
							<td class="perc-social-button-td perc-platform-name">
								<div class="perc-social-button-ui"><i class="fab fa-fw fa-facebook" aria-hidden="true" aria-label="Facebook"></i></div>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableButton" class="datadisplay perc-social-button-input perc-social-platform-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableDataPush" class="datadisplay perc-social-button-input perc-social-data-push-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td perc-social-media-link">
								<input name="socialLink" class="datadisplay perc-social-button-input perc-social-page-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" placeholder="http://www.facebook.com/PercussionSoftware"></input>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
						</tr>
						<tr class="perc-social-button-row perc-twitter-row" role="presentation">
							<td class="perc-hidden-input">
								<input name="platform" class="datadisplay perc-social-button-input perc-social-platform" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" value="twitter" type="hidden"></input>
							</td>
							<td class="perc-social-button-td perc-platform-name">
								<div class="perc-social-button-ui"><i class="fab fa-fw fa-twitter" aria-hidden="true" aria-label="Twitter"></i></div>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableButton" class="datadisplay perc-social-button-input perc-social-platform-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableDataPush" class="datadisplay perc-social-button-input perc-social-data-push-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td perc-social-media-link">
								<input name="socialLink" class="datadisplay perc-social-button-input perc-social-page-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" placeholder="http://www.twitter.com/percussion"></input>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
						</tr>
						<tr class="perc-social-button-row perc-linkedin-row" role="presentation">
							<td class="perc-hidden-input">
								<input name="platform" class="datadisplay perc-social-button-input perc-social-platform" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" value="linkedin" type="hidden"></input>
							</td>
							<td class="perc-social-button-td perc-platform-name">
								<div class="perc-social-button-ui"><i class="fab fa-fw fa-linkedin" aria-hidden="true" aria-label="LinkedIn"></i></div>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableButton" class="datadisplay perc-social-button-input perc-social-platform-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableDataPush" class="datadisplay perc-social-button-input perc-social-data-push-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td perc-social-media-link">
								<input name="socialLink" class="datadisplay perc-social-button-input perc-social-page-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" placeholder="http://www.linkedin.com/company/"></input>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
						</tr>
						<tr class="perc-social-button-row perc-pinterest-row" role="presentation">
							<td class="perc-hidden-input">
								<input name="platform" class="datadisplay perc-social-button-input perc-social-platform" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" value="pinterest" type="hidden"></input>
							</td>
							<td class="perc-social-button-td perc-platform-name">
								<div class="perc-social-button-ui"><i class="fab fa-fw fa-pinterest" aria-hidden="true" aria-label="Pinterest"></i></div>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableButton" class="datadisplay perc-social-button-input perc-social-platform-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableDataPush" class="datadisplay perc-social-button-input perc-social-data-push-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td perc-social-media-link">
								<input name="socialLink" class="datadisplay perc-social-button-input perc-social-page-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" placeholder="http://www.pinterest.com/PercussionCM1" ></input>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
						</tr>
						<tr class="perc-social-button-row perc-youtube-row" role="presentation">
							<td class="perc-hidden-input">
								<input name="platform" class="datadisplay perc-social-button-input perc-social-platform" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" value="youtube" type="hidden"></input>
							</td>
							<td class="perc-social-button-td perc-platform-name">
								<div class="perc-social-button-ui"><i class="fab fa-fw fa-youtube" aria-hidden="true" aria-label="YouTube"></i></div>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableButton" class="datadisplay perc-social-button-input perc-social-platform-enabled perc-social-enable-youtube-checkbox" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableDataPush" class="datadisplay perc-social-button-input perc-social-data-push-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td perc-social-media-link">
								<input name="socialLink" class="datadisplay perc-social-button-input perc-social-page-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" placeholder="http://www.youtube.com/user/PercussionSoftware" ></input>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
						</tr>
						<tr class="perc-social-button-row perc-whatsapp-row" role="presentation">
							<td class="perc-hidden-input">
								<input name="platform" class="datadisplay perc-social-button-input perc-social-platform" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" value="whatsapp" type="hidden"></input>
							</td>
							<td class="perc-social-button-td perc-platform-name">
								<div class="perc-social-button-ui"><i class="fab fa-fw fa-whatsapp" aria-hidden="true" aria-label="WhatsApp"></i></div>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableButton" class="datadisplay perc-social-button-input perc-social-platform-enabled perc-social-enable-whatsapp-checkbox" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableDataPush" class="datadisplay perc-social-button-input perc-social-data-push-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td perc-social-media-link">
								<input name="socialLink" class="datadisplay perc-social-button-input perc-social-page-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
						</tr>
						<tr class="perc-social-button-row perc-email-row" role="presentation">
							<td class="perc-hidden-input">
								<input name="platform" class="datadisplay perc-social-button-input perc-social-platform" style="height: 15px;padding-top: 3px;padding-bottom: 5px;" value="email" type="hidden"></input>
							</td>
							<td class="perc-social-button-td perc-platform-name">
								<div class="perc-social-button-ui"><i class="fas fa-fw fa-envelope" aria-hidden="true" aria-label="Email"></i></div>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableButton" class="datadisplay perc-social-button-input perc-social-platform-enabled perc-social-enable-email-checkbox" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td">
								<input type="checkbox" name="enableDataPush" class="datadisplay perc-social-button-input perc-social-data-push-enabled" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-input-td perc-social-button-td perc-social-media-link">
								<input name="socialLink" class="datadisplay perc-social-button-input perc-social-page-link" style="height: 15px;padding-top: 3px;padding-bottom: 5px;"></input>
							</td>
							<td class="perc-move-button-container"><div role="button" tabindex="0" id="move-up" class="perc-move-button perc-move-button-up"><i class="fa fa-caret-up" aria-hidden="true"></i></div><div role="button" tabindex="0" id="move-down" class="perc-move-button perc-move-button-down"><i class="fa fa-caret-down" aria-hidden="true"></i></div></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</xsl:template>
	<xsl:template match="Control[@name='percSocialButtonsControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<div class="perc-social-buttons-readonly" id="{@paramName}">
			<div style="display:none;">TODO: Finish ME!</div>
		</div>
	</xsl:template>
</xsl:stylesheet>
