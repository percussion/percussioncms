/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * New Section 
 */
(function($){

//Add custom validation method for the URL name.
//$.validator.addMethod( 'url_name', 
//      function(x) { return x.match( /^[a-zA-Z0-9\-]*$/ ); }, 
//       I18N.message( "perc.ui.newpagedialog.error@Url name validation error" ));

$.PercEditSectionLinksDialog = function() {
   function openDialog(sectionObj, parentPath, siteName, dlgTitle, okCallback) {
    var taborder = 30;
    var v;

    var dialog;
    
    dialog = $("<div>" +
                "<p class='perc-field-error' id='perc-save-error'></p><br/>" +
                "<span style='position: relative; float: right; margin-top: -44px; margin-right: -2px;'><label>" +I18N.message("perc.ui.general@Denotes Required Field") + "</label></span>" +
                "<form action='' method='GET'> " +
                "<div id='perc-external-link-container' style='display:none'>" +
                "<fieldset>" +
                "<label for='perc-external-link-text' class='perc-required-field'>" + I18N.message( "perc.ui.newSectionDialog.label@External link text" ) + ":</label> <br/> " +
                "<input type='text' class='required' tabindex='" + taborder + "' id='perc-external-link-text' name='perc-external-link-text' maxlength='100'/> <br/>" +
                "<label class = 'perc-required-field' for='perc-external-link-url'>" + I18N.message( "perc.ui.newSectionDialog.label@External link URL" ) + ":</label> <br/> " +
                "<input type='text' tabindex='" + taborder + "' id='perc-external-link-url' class='required url_name' name='perc-external-link-url' maxlength='512'/><br/> " +
                "<label for='perc-external-link-target'>Target Window:</label> <br>" +
                "<select id='perc-external-link-target' name='perc-external-link-target'>" +
                    "<option value=''></option>" +
                    "<option value='_blank'>New Window</option>" +
                    "<option value='_top'>Top Window</option>" +
                "</select> <br/>" +
                "<label for='perc-external-link-navigation-cssclassnames'>" + I18N.message( "perc.ui.editSectionDialog.label@Navigation class names") + ":</label> <br/> " +
                "<input type='text' tabindex='" + taborder + "' id='perc-external-link-navigation-cssclassnames' maxlength='255' name='perc-external-link-navigation-cssclassnames'/> <br/>" +
                "</fieldset>" +
                "</div>" +
                "<div id='perc-section-link-container' style='display:none'>" +
                "<fieldset>" +
                "<label for='perc-section-link-target' class='perc-required-field'>" + I18N.message( "perc.ui.newSectionDialog.label@Target section" ) + ":</label> <br/> " +
                "<div><input type='text' class='required' readonly='true' name='perc-section-link-target' id='perc-section-link-target'/><img src='../images/images/buttonEllipse.png' class='perc-button-ellipse'/></div>" +
                "<input type='hidden' name='perc-section-link-targetid' id='perc-section-link-targetid'/>" +
                "</fieldset>" +
                "</div>" +
                "<div class='ui-layout-south'>" +
                "<div id='perc_buttons' style='z-index: 100;'></div>" +
                 "</div>" +
                "</form> </div>").perc_dialog( {
             title: dlgTitle,
             open: function(){ 
                 $.Perc_SectionServiceClient.getSection(sectionObj.id,setDialogContent);
             },
             modal: true,
             percButtons:   {
                "Save":     {
                    click: function()   {
                        _submit();
                    },
                    id: "perc-new-section-save"
                },
                "Cancel":       {
                    click: function()   {
                        _remove();
                    },
                    id: "perc-new-section-cancel"
                }
             },
            id: "perc-new-section-dialog",
            width: 800
        });
    function setDialogContent(status, data) {
        if(status === $.PercServiceUtils.STATUS_ERROR)
        {
            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error") , content: data});
            return;
        }
        
        if(data.SiteSection.sectionType === $.Perc_SectionServiceClient.PERC_SECTION_TYPE.EXTERNAL_LINK)
        {
            $("#perc-section-link-container").hide();
            $("#perc-external-link-container").show();
            $("#perc-external-link-text").val(data.SiteSection.title);
            $("#perc-external-link-url").val(data.SiteSection.externalLinkUrl);
            $("#perc-external-link-target option[value='"+ data.SiteSection.target +"']").attr("selected","selected");
            // Bind the filter to the class names field and retrieve its value
            $.perc_filterFieldText($('#perc-external-link-navigation-cssclassnames'), $.perc_autoFillTextFilters.IDNAMECDATAALPHA, ' ');
            $("#perc-external-link-navigation-cssclassnames").val(data.SiteSection.cssClassNames);
        }
        else if(sectionObj.sectionType === $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION_LINK)
        {
            $("#perc-section-link-container").show();
            $("#perc-external-link-container").hide();
            
            $("#perc-section-link-target").val(parentPath);
            $("#perc-section-link-targetid").val(sectionObj.id);
        }
    }
    //Add click handler to the select section eliipse button.
    dialog.find(".perc-button-ellipse").on("click",function(){
        $.PercSectionTreeDialog.open(siteName, null, "Select target section", "Target section", "Select", function(targetId,path){
            $("#perc-section-link-targetid").val(targetId);
            $("#perc-section-link-target").val(path);
        });
    });
    function err( str ) {
        $('#perc-save-error').text( str ).effect('pulsate', {times: 1});
    }
    function _remove()  {
        dialog.remove();
    }
    function _submit()  {
        dialog.find('form').trigger("submit");
    }

  v = dialog.find('form').validate({
            errorClass: "perc-field-error",
                    validClass: "perc-field-success",
                    wrapper: "p",
                    validateHiddenFields: false,
                    debug: false,
            submitHandler: function(form) {
               dialog.remove();
               okCallback("ok", $(form).serializeArray());
            }
    });  


    }// End open dialog      

    return {"open": openDialog};
    
};

})(jQuery);
