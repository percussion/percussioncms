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

$.perc_newSectionDialog = function() {
   function openDialog(site, parentId, callback) {
    var taborder = 30;
    var v;

    var dialog;
    
    dialog = $("<div>" +
                "<p class='perc-field-error' id='perc-save-error'></p><br/>" +
                "<span style='position: relative; float: right; margin-top: -44px; margin-right: -2px;'><label>" +I18N.message("perc.ui.general@Denotes Required Field") + "</label></span>" +
                "<form action='' method='GET'> " +
                "<label class='perc-required-field' for='perc-section-type'>" + I18N.message("perc.ui.newSectionDialog.label@Type") + ":</label> <br>" +
                "<select id='perc-section-type' name='perc-section-type'>" +
                    "<option value='section'>" + I18N.message("perc.ui.newSectionDialog.label@Section & landing page") + "</option>" +
                    "<option value='sectionlink'>" + I18N.message("perc.ui.newSectionDialog.label@Section link") + "</option>" +
                    "<option value='externallink'>" + I18N.message("perc.ui.newSectionDialog.label@External link") + "</option>" +
                    "<option value='convertfolder'>" + I18N.message("perc.ui.newSectionDialog.label@Convert folder") + "</option>" +
                "</select> <br>" +
                "<div id='perc-section-container'>" +
                "<label for='perc-select-template'>"+ I18N.message( "perc.ui.newSectionDialog.label@Select a template" ) + ": </label><br/>" +
                "<a class='prevPage browse left'></a>" +
                "<div class='perc-scrollable'><input type='hidden' id='perc-select-template' name='template'/>" +
                "<div class='perc-items'>" +
              "</div></div>" +
                "<a class='nextPage browse right' ></a>" +
                "<div style='float:left;'>" +
                "<fieldset>" +
                "<label for='perc-section-name' class='perc-required-field'>" + I18N.message( "perc.ui.newSectionDialog.label@Section name" ) + ":</label> <br/> " +
                "<input type='text' class='required' tabindex='" + taborder + "' id='perc-section-name' name='section_name' maxlength='512'/> <br/>" +
                "<label for='perc-section-url' class='perc-required-field'>" + I18N.message( "perc.ui.newSectionDialog.label@URL" ) + ":</label> <br/> " +
                "<input type='text' class='required' tabindex='" + taborder + "' id='perc-section-url' name='page_url' maxlength='100'/><br/> " +
                "</fieldset>" +
                "</div>" +
                "</div>" +
                "<div id='perc-external-link-container' style='display:none'>" +
                "<fieldset>" +
                "<label for='perc-external-link-text' class='perc-required-field'>" + I18N.message( "perc.ui.newSectionDialog.label@External link text" ) + ":</label> <br/> " +
                "<input type='text' class='required' tabindex='" + taborder + "' id='perc-external-link-text' name='perc-external-link-text' maxlength='100'/> <br/>" +
                "<label class='perc-required-field' for='perc-external-link-url'>" + I18N.message( "perc.ui.newSectionDialog.label@External link URL" ) + ":</label> <br/> " +
                "<input type='text' class='required' tabindex='" + taborder + "' id='perc-external-link-url' name='perc-external-link-url' maxlength='512'/><br/> " +
                "</fieldset>" +
                "</div>" +
                "<div id='perc-section-link-container' style='display:none'>" +
                "<fieldset>" +
                "<label for='perc-section-link-target' class='perc-required-field'>" + I18N.message( "perc.ui.newSectionDialog.label@Target section" ) + ":</label> <br/> " +
                "<div><input type='text' class='required' readonly='true' name='perc-section-link-target' id='perc-section-link-target'/><img id='perc-section-link-targetid-button' src='../images/images/buttonEllipse.png' class='perc-button-ellipse'/></div>" +
                "<input type='hidden' name='perc-section-link-targetid' id='perc-section-link-targetid'/>" +
                "</fieldset>" +
                "</div>" +
                "<div id='perc-convert-folder-container' style='display:none'>" +
                "<fieldset>" +
                "<label for='perc-convert-folder-path' class='perc-required-field'>" + I18N.message( "perc.ui.newSectionDialog.label@Target folder" ) + ":</label> <br/> " +
                "<div><input type='text' class='required' readonly='true' name='perc-convert-folder-path' id='perc-convert-folder-path'/><img id='perc-convert-folder-path-button' src='../images/images/buttonEllipse.png' class='perc-button-ellipse'/></div>" +
                "<label class='perc-required-field' for='perc-landing-page'>" + I18N.message("perc.ui.newSectionDialog.label@Select landing page") + ":</label> <br>" +
                "<select id='perc-landing-page' name='perc-landing-page' class='required'>" +
                    "<option value='perc-empty-landing-page'>" + I18N.message("perc.ui.newSectionDialog.label@Select a folder to see landing pages") + "</option>" +
                "</select> " +
                "<span id='pageErrorSpan' style='display : none' class='perc-field-error'>Please create page under target folder.</span>" +
                "<br>" +
                "</fieldset>" +
                "</div>" +
                "<div class='ui-layout-south'>" +
                "<div id='perc_buttons' style='z-index: 100;'></div>" +
                 "</div>" +
                "</form> </div>").perc_dialog( {
             title: I18N.message( "perc.ui.newSectionDialog.title@New Section" ),
             modal: true,
             percButtons:   {
                "Save":     {
                    click: function()   {
                       var sectionName = dialog.find('form').find('#perc-section-url');
                       var sectionNameValue = $.perc_textFilters.WINDOWS_FILE_NAME(sectionName.val().trim());
                       sectionName.val(sectionNameValue);               
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
    //Add onchange event to the type select box
    dialog.find("#perc-section-type").on("change",function(){
        $("#perc-section-container").hide();
        $("#perc-section-link-container").hide();
        $("#perc-external-link-container").hide();
        $("#perc-convert-folder-container").hide();
        var slection = $(this).val();
        switch(slection)
        {
            case "sectionlink":
                $("#perc-section-link-container").show();
                break;
            case "externallink":
                $("#perc-external-link-container").show();
                break;    
            case "convertfolder":
                $("#perc-convert-folder-container").show();
                break;
            default:
                $("#perc-section-container").show();
        }
    });
    //Add click handler to the select section eliipse button.
    dialog.find("#perc-section-link-targetid-button").on("click",function(){
        var dlgTitle = I18N.message("perc.ui.newSectionDialog.label@Target Section");
        var treeLabel = I18N.message("perc.ui.newSectionDialog.label@Select target section");
        $.PercSectionTreeDialog.open(site, null, treeLabel, dlgTitle, "Select", function(targetId,path){
            $("#perc-section-link-targetid").val(targetId);
            $("#perc-section-link-target").val(path);
        });
    });
    //Add click handler to the select target folder ellipse button.
    dialog.find("#perc-convert-folder-path-button").on("click",function(){
        var dlgTitle = I18N.message("perc.ui.newSectionDialog.label@Target Folder");
        var treeLabel = I18N.message("perc.ui.newSectionDialog.label@Select target folder");
        var updateTargetFolderPath = function(pathItem){
            dialog.find("#perc-landing-page").empty();
            var path = pathItem.path;
            //Add double slash if path doesn't start with //
            if(path.substring(0, 1) === "/" && (path.substring(0, 2) === "//"))
                path = "/" + path;
            
            //Some of the services from server are not setting the path on PathItem, if not defined get it from folderPaths
            if(!path){
                path = pathItem.folderPaths.split("$System$")[1];
            } 
            dialog.find("#perc-convert-folder-path").val(path).attr("title",path);
            var refreshPages = function(status, data){
                var selectElem = dialog.find("#perc-landing-page").empty();
                if(!status){
                    $.perc_utils.alert_dialog({"title":I18N.message("perc.ui.new.section.dialog@Error Fetching folder pages"), "message":data});
                    return;
                }
                var optionStr = "";
                var pages = Array.isArray(data.psobj)?data.psobj:[data.isobj];
                $.each(pages, function(){
                    optionStr += "<option value=\"" + this.name + "\">" + this.name + "</option>"; 
                });
                selectElem.append(optionStr);
            };
            $.PercFolderService.getFolderPagesById(pathItem.id, refreshPages);
        };
        var validator = function(pathItem){
            var errMsg = null;
            if(!pathItem)
                errMsg = I18N.message("perc.ui.new.section.dialog@Select folder");
            else if(pathItem.category === "SECTION_FOLDER")
                errMsg = I18N.message("perc.ui.new.section.dialog@Section Not Folder");
            else if(pathItem.path==="/Sites/")
                errMsg = I18N.message("perc.ui.new.section.dialog@Sites Root Not Folder");
            else if(pathItem.type==="site")
                errMsg = I18N.message("perc.ui.new.section.dialog@Site Not Folder");
            return errMsg;
        };
        var pathSelectionOptions = {
            okCallback: updateTargetFolderPath,
            dialogTitle: dlgTitle,
            rootPath:$.PercFinderTreeConstants.ROOT_PATH_SITES,
            initialPath: dialog.find("#perc-convert-folder-path" ).val(),
            selectedItemValidator:validator,
            showFoldersOnly:true
        };
        $.PercPathSelectionDialog.open(pathSelectionOptions);
    });

   var itemContainer = dialog.find('div.perc-scrollable div.perc-items');
  
   var pseudoSelect = $('#perc-select-template_perc_is');
   var selectLocalStyle = "height: 160px; width: 410px; overflow-x: scroll; overflow-y: hidden;";
   pseudoSelect.attr("style", selectLocalStyle);
                   

    function err( str ) {
        $('#perc-save-error').text( str ).effect('pulsate', {times: 1});
    }
    function _remove()  {
        dialog.remove();
        callback("cancel");
    }
    function _submit()  {
        dialog.find('form').trigger("submit");
        $("#pageErrorSpan").hide();
        if($( "#perc-landing-page" ).hasClass( "perc-field-error" )){
            $("#pageErrorSpan").show();
        }
    }

  v = dialog.find('form').validate({
                    errorClass: "perc-field-error",
                    validClass: "perc-field-success",
                    wrapper: "p",
                    validateHiddenFields: false,
                    debug: false,
            submitHandler: function(form) {
               dialog.remove();
               callback("ok", $(form).serializeArray());
            },
            errorPlacement: function(error, element) {
             if (element.attr("name") === "perc-section-link-target")
              error.insertAfter("#perc-section-link-targetid-button");
             else
              error.insertAfter(element);
          }
    });  

   var queryPath = $.perc_paths.TEMPLATES_BY_SITE + '/' + site;
    
   
    $.getJSON( queryPath, function( spec ) {
        //Load template selector
        $.each( spec.TemplateSummary, function() {
            itemContainer.append(createTemplateEntry(this));
            $("div.perc-scrollable").scrollable({
                items: ".perc-items",
                size: 4,
                keyboard: false
            });
            $(".perc-items .item .item-id").hide();
            // bind click event to each item to handle selection
            $(".perc-items .item").on('click', function(){
                var itemId = $(this).find(".item-id").text(); 
                $("#perc-select-template").val(itemId);
                $(".perc-items .item").removeClass("perc-selected-item");
                $(this).addClass("perc-selected-item");
            });
            // select first item by default
            $firstItem = $(".perc-items .item:first");
            $("#perc-select-template").val($firstItem.find(".item-id").text());
            $firstItem.addClass("perc-selected-item");
            
        });
        
        // after adding all the template entries, truncate the labels if they dont fit
        $.PercTextOverflow($("div.perc-text-overflow"), 122);
    });
           
   
    var section_name = $('#perc-section-name');
    var section_url = $('#perc-section-url');  
    
    $.perc_textAutoFill(section_name, section_url, $.perc_autoFillTextFilters.URL);
    $.perc_filterField(section_name, $.perc_textFilters.NOBACKSLASH);
    $.perc_filterField(section_url, $.perc_textFilters.URL);
       

    }// End open dialog      
    

    function check_for_dirty_page() { 
       content.confirm_if_dirty( open_new_page_dialog );
    }
    
    function createTemplateEntry(data){
        var temp = "<div class=\"item\">" +
             "<div class=\"item-id\">@ITEM_ID@</div>" +
             "     <table>" +
             "         <tr><td align='left'>" +
             "             <img style='border:1px solid #E6E6E9' height = '86px' width = '122px' src=\"@IMG_SRC@\"/>" +
             "         </td></tr>" +
             "         <tr><td>" +
             "            <div class='perc-text-overflow-container' style='text-overflow:ellipsis;width:122px;overflow:hidden;white-space:nowrap'>" +
             "                <div class='perc-text-overflow' style='float:none' title='@ITEM_TT@' alt='@ITEM_TT@'>@ITEM_LABEL@</div>" +
             "         </td></tr>" +
             "     </table>" +
             "</div>";
        return temp.replace(/@IMG_SRC@/, data.imageThumbPath)
            .replace(/@ITEM_ID@/, data.id)
            .replace(/@ITEM_LABEL@/, data.name)
            .replace(/@ITEM_TT@/g, data.name);
    }

    return {"open": openDialog};
    
};

})(jQuery);
