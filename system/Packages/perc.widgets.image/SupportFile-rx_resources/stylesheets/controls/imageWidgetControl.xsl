<?xml version="1.0" encoding="UTF-8" ?>
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
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:template match="/" />
<!--
     imageWidgetControl
 -->
	<psxctl:ControlMeta name="imageWidgetControl" dimension="single" choiceset="none">
		<psxctl:Description>The control for uploading images for the images Widget:</psxctl:Description>
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
			<psxctl:Param name="size" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
				<psxctl:DefaultValue>50</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="cleartext" datatype="String" paramtype="custom">
				<psxctl:Description>This parameter determines the text that will be displayed along with a checkbox when the field supports being cleared.  The default value is 'Clear'.</psxctl:Description>
				<psxctl:DefaultValue>Clear</psxctl:DefaultValue>
			</psxctl:Param>
		   <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
		      <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
		      <psxctl:DefaultValue>400</psxctl:DefaultValue>
		   </psxctl:Param>
		   <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
		      <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
		      <psxctl:DefaultValue>125</psxctl:DefaultValue>
		   </psxctl:Param>
		   <psxctl:Param name="aarenderer" datatype="String" paramtype="generic">
		      <psxctl:Description>This parameter specifies whether the field editing in Active Assembly takes place in a modal dialog or in a popup. Applicable values are MODAL, POPUP and INPLACE_TEXT, any other value is treated as POPUP. The recommended value is POPUP only.</psxctl:Description>
		      <psxctl:DefaultValue>POPUP</psxctl:DefaultValue>
		   </psxctl:Param>
		</psxctl:ParamList>
		<!-- Add Extension here
		<psxctl:Dependencies>
			<psxctl:Dependency status="readyToGo" occurrence="single">
				<psxctl:Default>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_FileInfo</name>
					</PSXExtensionCall>
				</psxctl:Default>
			</psxctl:Dependency>
		</psxctl:Dependencies>
		-->
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="imagestyle.css" type="css" mimetype="text/css">
				<psxctl:FileLocation>../rx_resources/widgets/image/css/style.css</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.imageAssetControl.css" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/widgets/image/js/jquery.imageAssetControl.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.form.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-form/jquery.form.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="jquery.caret.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.caret.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
	    <psxctl:FileDescriptor name="PercServiceUtils.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
            </psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='imageWidgetControl']" mode="psxcontrol">
	<script >
	
	$(document).ready(function() {
		$('#perc-content-form').imageAssetControl({
			maxDisplayHeight: 400,
			maxDisplayWidth: 600,
		});
	});
    </script>

 					<div id="imageWidgetPanel">
					<input type="hidden" name="jQueryFormFile" value="true"/>
 						
							<div class="image-widget-step-header">
								<span class="font_normal_07em_black">* Image</span>
								<table>
									<tr>
										<td id="perc-image-upload">Upload an Image</td>
										<td id="perc-image-resize">Size the Image</td>
										<td id="perc-image-thumbnail">Size the Thumbnail</td>
									</tr>
								</table>
							</div>
						<span class="step image_asset_upload" id="perc-upload_form">	
							<label for="perc-select-image">Select an image:</label><br />
							<div id="image_asset_upload_container">
								<input  class="required" type="file" dlg_height="125" dlg_width="440" size="50" id="perc-select-image" name="img_x"/>
								<label for="perc-select-image" id="image_asset_upload_message"></label><br />
								<label class="perc_field_error" for="perc-select-image" id="perc-upload-error-message">
									<xsl:if test="//DisplayError/Details/FieldError[@submitName='sys_title']">Must select a valid image file.</xsl:if>
								</label>
								<input  type="hidden" class="link" value="main_resize" />
							</div>
 						</span>
						<span id="main_resize" class="step image_asset_step">
							<span id="resize_warning">Warning: Images resized by this editor will be converted to png file format.</span>
							<table>
								<tr>
									<td>
										<input class="image_asset_name" type="hidden" value="img" />
										
										<div class="image_resize_wrapper">
											<div class="image_asset_rotate">
												<input   name="main_left" class="image_asset_rotate_left" type="button"  />
												<input   name="main_right" class="image_asset_rotate_right" type="button"  />
											</div>
											<div id="main_image_region" class="image_asset_image" />
										</div>
									</td>
									<td width = "30px">
									</td>
									<td valign = "top">	
											<div class="image_asset_prop_wrapper">
												<table>
													<tr>
														<td>
															<label for="main_width">Width: </label>
														</td>
													</tr>
													<tr>	
														<td class="perc-image-label">
															<input  class="image_asset_width required" name="main_width" id="asset_main_width" value="0"/>  (original: <label for="main_width" class="image_asset_orig_width">0</label><span>)</span>
														</td>
													</tr>	
													<tr>
														<td>
															<label for="main_height">Height: </label> 
														</td>
													</tr>
													<tr>	
														<td class="perc-image-label">		
															<input  class="image_asset_height required" name="main_height" id="asset_main_height" value="0"/>  (original: <label for="main_height" class="image_asset_orig_height">0</label><span>)</span>
														</td>
													</tr>	
													<tr>
														<td class = "image-checkbox perc-image-label">
															<div id="image_asset_main_scale" class="image_asset_scale" style = "display:none"><p>Scale</p></div>
															<input type="checkbox" class="image_asset_constrain" id="main_img_prop" name="constrain_props" checked="checked"/>Constrain proportions<br />
														</td>
													</tr>
		
												</table>	
											</div>
									</td>
								</tr>
							</table>	
						</span>
						<span id="thumbnail_resize" class="step image_asset_step">
						
							<table>
								<tr>
									<td>
										
										<input  class="image_asset_name" type="hidden" value="img2" />
										<input  class="image_asset_default_width" type="hidden" value="50" />
									
									
										<div class="image_resize_wrapper">
											<div class="image_asset_rotate">
												<input   name="thumb_left" class="image_asset_rotate_left" type="button"  />
												<input   name="thumb_right" class="image_asset_rotate_right" type="button"  />
											</div>
											
											<div id="thumb_image_region" class="image_asset_image" />
										</div>
									</td>
									<td width = "30px">
									</td>
									<td valign = "top">
										<div class="image_asset_prop_wrapper">
											<table>
												<tr>
													<td>
														<label for="thumb_width">Width: </label> 
													</td>
												</tr>
												<tr>		
													<td class="perc-image-label">		
														<input  class="image_asset_width required" name="thumb_width" id="main_img_width" value="0"/> (original:<label for="thumb_width" class="image_asset_orig_width">0</label><span>)</span>
													</td>
												</tr>
												<tr>	
													<td>		
														<label for="thumb_height">Height: </label> 
													</td>
												</tr>
												<tr>	
													<td class="perc-image-label">	
														<input  class="image_asset_height required" name="thumb_height" id="thumb_img_height" value="0"/> (original:<label for="thumb_height" class="image_asset_orig_height">0</label><span>)</span>
													</td>
												</tr>
												<tr>	
													<td class = "image-checkbox perc-image-label">
														<div id="image_asset_thumb_scale" class="image_asset_scale" style = "display:none;"><p>Scale</p></div>
														<input type="checkbox" class="image_asset_constrain" id="thumb_img_prop" name="constrain_props" checked="checked"/>Constrain Proportions
													</td>	
												</tr>		

												<tr>
													<td>
														<label for="perc-image-thumbprefix">* Thumbnail prefix: </label>
													</td>
												</tr>
												<tr>	
													<td>
														<input  type="text" id="perc-image-thumbprefix" class="datadisplay" maxlength="50" value=""/>
													</td>
												</tr>												
											</table>	
										</div>	
									</td>
								</tr>
							</table>		
									
						</span>
 			</div>
	</xsl:template>
	<xsl:template match="Control[@name='imageWidgetControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<style type="text/css">
		   .perc_image_preview_pane{
                       width: 600px;
		       height: 450px;
		       background-color: #ffffff;
		       overflow: auto;
		       padding: 5px 5px 5px 5px;
		   }
		</style>
		<script >
		<![CDATA[
		$(document).ready(function() {

		   function percLoadPreviewImage(isFull){
		      var prefix = isFull ? "perc_full_image" : "perc_thumb_image";
		      var infoUrl = "/Rhythmyx/user/apps/imageWidget/requestImage.do";
		      var renderUrl = "/Rhythmyx/user/apps/imageWidget/image/img";
		      var imageId = $("#" + prefix + "_id").val();

		      $.PercServiceUtils.makeJsonRequest(
                            infoUrl + "?imageKey=" + imageId,
                            $.PercServiceUtils.TYPE_GET,
                            false,
                            function(status, result){
                               if(status === $.PercServiceUtils.STATUS_SUCCESS)
                               {                  
                                  $("#" + prefix + "_height").text(result.data.height);  
				  $("#" + prefix + "_width").text(result.data.width);
				  var $container = $("#" + prefix);
				  var imgSrc = renderUrl + imageId + "." + result.data.ext;
				  var imgNode = document.createElement("img");
				  imgNode.setAttribute("width", result.data.width);
				  imgNode.setAttribute("height", result.data.height);
				  imgNode.setAttribute("src", imgSrc);
				  $container.append(imgNode);

                               }
                               else
                               {
                                  var defaultMsg = 
                                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                                  $.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
                               }
                            }
                         );	

		   }
		   percLoadPreviewImage(true);
		   percLoadPreviewImage(false);
		
	        });
		]]>
		</script>
		<div id="imageWidgetPanel">
			<input type="hidden" id="perc_full_image_id">
			   <xsl:attribute name="value"><xsl:value-of select="//DisplayField/Control[@paramName='img_id']/Value"/></xsl:attribute>
			</input>
			<label>Full image:</label><br/>
			<div id="perc_full_image" class="perc_image_preview_pane">
			</div>
			<label>Full image width:</label><br/>
			<div class="datadisplay" id="perc_full_image_width"></div><br/>
			<label>Full image height:</label><br/>
			<div class="datadisplay" id="perc_full_image_height"></div><br/>
			<input type="hidden" id="perc_thumb_image_id">
			   <xsl:attribute name="value"><xsl:value-of select="//DisplayField/Control[@paramName='img2_id']/Value"/></xsl:attribute>
			</input>
			<label>Thumbnail image</label><br/>
			<div id="perc_thumb_image"  class="perc_image_preview_pane">
			</div>
			<label>Thumbnail image width:</label><br/>
			<div class="datadisplay" id="perc_thumb_image_width"></div><br/>
			<label>Thumbnail image height:</label><br/>
			<div class="datadisplay" id="perc_thumb_image_height"></div><br/>
		</div>
	</xsl:template>
</xsl:stylesheet>
