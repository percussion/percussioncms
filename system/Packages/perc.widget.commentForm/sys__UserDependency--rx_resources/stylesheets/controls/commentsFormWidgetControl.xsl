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
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:psxctl="urn:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="xalan://com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
<xsl:template match="/" />
<!--
     formWidgetControl 
 -->
    <psxctl:ControlMeta name="commentsFormWidgetControl" dimension="single" choiceset="none">
        <psxctl:Description>Provides UI for creating the comments form.</psxctl:Description>
        <psxctl:ParamList>
        </psxctl:ParamList>
        <psxctl:AssociatedFileList>
        <!-- CSS -->
            <psxctl:FileDescriptor name="perc-comments-form-widget.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../rx_resources/widgets/commentsForm/css/perc-comments-form-widget.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>                
        <!-- JavaScript -->            
            <psxctl:FileDescriptor name="jquery-ui.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/libraries/jquery-ui/jquery-ui.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>        
        <!-- TinyMce -->             
            <psxctl:FileDescriptor name="jquery.tinymce.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../sys_resources/tinymce/js/tinymce/jquery.tinymce.min.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>  
            <psxctl:FileDescriptor name="tiny_mce.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../sys_resources/tinymce/js/tinymce/tinymce.min.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="TinymceConfig.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/commentsForm/js/TinymceConfig.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>                
        <!-- Form Control JS-->    
            <psxctl:FileDescriptor name="PercCommentsFormController.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/commentsForm/js/PercCommentsFormController.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>            
            <psxctl:FileDescriptor name="PercCommentsFormView.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/commentsForm/js/PercCommentsFormView.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>            
                     <psxctl:FileDescriptor name="PercContentBrowserWidget.js" type="script" mimetype="text/javascript">
                            <psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-jeditable/jquery.jeditable.js</psxctl:FileLocation>
                            <psxctl:Timestamp/>
            </psxctl:FileDescriptor>                     
                     <psxctl:FileDescriptor name="PSJSUtils.js" type="script" mimetype="text/javascript">
                            <psxctl:FileLocation>../../cm/jslib/profiles/3x/jquery/plugins/jquery-percutils/jquery.percutils.js</psxctl:FileLocation>
                            <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercUtilService.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/services/PercUtilService.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercServiceUtils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_path_constants.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_path_constants.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_save_as.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/widgets/perc_save_as.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_path_manager.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_path_manager.js</psxctl:FileLocation>
                            <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
        </psxctl:AssociatedFileList>
    </psxctl:ControlMeta>
    
    <xsl:template match="Control[@name='commentsFormWidgetControl']" mode="psxcontrol">
        <!-- PercFormView.js file actually renders the form. -->
        <input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
        <div id="{@paramName}" class="PercFormWidget">
            <div id = "perc-form-top-row">
                <div id = "perc-control-menu-button"><img src = "../rx_resources/widgets/commentsForm/images/control.png" title = "Form controls menu"/> </div>
                <div id = "perc-control-menu-wrapper">
                      <img src = "../rx_resources/widgets/form/images/control-top.png"/>
                      <ul id = "perc-control-menu-container">
                         <li class = "perc-control-label form-text-label">Text</li>
                         <li class = "perc-control-label form-title-label">Title</li>
                         <li class = "perc-control-label form-username-label">User Name</li>
                         <li class = "perc-control-label form-honeypot-label">Honeypot Filter</li>
                         <li class = "perc-control-label form-email-label">Email</li>
                         <li class = "perc-control-label form-website-label">Website URL</li>
                      </ul>
                </div>
            </div>       
                <div class = "perc-form-fields-col">
                </div>
                <div id = "perc-metadata-content">
                </div>
              </div>
    </xsl:template>
    <xsl:template match="Control[@name='commentsFormWidgetControl' and @isReadOnly='yes']" priority='10' mode="psxcontrol">
        <input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
        <div id="{@paramName}" class="PercFormWidgetReadOnly">
            <div id = "perc-form-top-row">
                <div class = "perc-form-fields-col">
                </div>
            </div>
       </div>
    </xsl:template>
</xsl:stylesheet>
