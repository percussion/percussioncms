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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
* Edit Folder Properties
*/
(function($){
    $.PercFolderPropertiesDialog = function() 
    {
        var currentAllowedSites;
        var folderSysPathName = "";
        var folderSysPath = "";
        var itemType = "";
        /**
          * The main method of this class, makes an gets the folder properties and displays them in a dialog.
          * Saves the properties handling the validation errors inline. 
          */
        function openDialog(pathItem, callback) 
        {
            //make an Ajax request to get the folder properties and call the createDiaog method by suppling that data.
            $.PercPathService.getFolderProperties(pathItem.id, createDialog);    

            var taborder = 30;
            var v;
            
            var dialog;
            // Droplist component holding the allowed sites, if editing of an Asset root folder
            var allowedSitesMultiselect = undefined;
            
            
            /**
            * Creates the dialog and sets the field values from the supplied result.data object. 
            */
            function createDialog(status, result)
            {
                var self = this;
                
                if(status === $.PercServiceUtils.STATUS_ERROR)
                { 
                	//TODO: TEST ME I18N
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.folder.properties.dialog@Folder Not Found") + pathItem.path});
                    callback("",false);
                    return;
                }
                
                var splitPath = pathItem.path.split('/');
                
                //preserve the original folder path.
                if(!folderSysPathName)
                {
                    folderSysPathName = pathItem.folderPath;
                    folderSysPath = pathItem.folderPaths;
                    itemType = pathItem.type;
                }
                
                // If the path is two levels deep and begins with /Assets, then it's a root level asset folder (/Assets/rootAssetFolder/).
                var isRootAssetFolder = (splitPath !== undefined && splitPath.length===4 && splitPath[1] === 'Assets');

                //Initializes the permission to ADMIN ($.perc_finder().FOLDER_PERMISSIONS.PERMISSION_ADMIN)
                var permission = $.PercFolderHelper().PERMISSION_ADMIN;
                var fProps = result.data.FolderProperties;
                
                // get the writePrincipals from the JSON object comming from the server
                // make sure it's an array even if it's a single object
                var writePrincipals = [];
                if(fProps.permission) {
                    permission = fProps.permission.accessLevel;
                    if(fProps.permission.writePrincipals) {
                        writePrincipals = fProps.permission.writePrincipals;
                        writePrincipals = Array.isArray(writePrincipals) ? writePrincipals : [writePrincipals];
                    }
                }
                var checkStatus = [];
                checkStatus[0] = permission === $.PercFolderHelper().PERMISSION_READ?" selected='true' ":"";
                checkStatus[1] = permission === $.PercFolderHelper().PERMISSION_WRITE?" selected='true' ":"";
                checkStatus[2] = permission === $.PercFolderHelper().PERMISSION_ADMIN?" selected='true' ":"";
                
                dialog = $(
                "<div>" +
                     "<p class='perc-field-error' id='perc-save-error'></p>" +
                     "<div style='background: #E6E6E9; padding-top: 5px; padding-right: 10px; text-align:right;'><label>" + I18N.message("perc.ui.general@Denotes Required Field") + "</label></div>" +
                     "<div class='fieldGroup'>" +
                         "<div id='perc-folder-general-properties-container'>" +
                             "<div>" +
                                 "<label for='perc-folder-name'class='perc-required-field'>" + I18N.message( "perc.ui.folderPropsDialog.label@Name" ) + ":</label> <br/> " +
                                 "<input type='text' class='required' tabindex='" + taborder + "' id='perc-folder-name' name='folder_name' maxlength='50' value=\""+ fProps.name +"\"/> <br/>" +
                                 "<label id='perc_folder_duplicate_error' style='display: none;' class='perc_field_error'></label><br/>" +
                             "</div>" +
                         "</div>" +
                     "</div>" +
                    
                     "<div class='fieldGroup'>" +
                         "<div id='perc-user-permissions-container'>"  +
                             "<label for='perc-folder-permission'>" + I18N.message( "perc.ui.folderPropsDialog.label@Permission" ) + ":</label> <br/>" +
                            
                                // refactored from radio buttons to drop downs
                                "<select name='perc-folder-permission' id='perc-folder-permission'>" +
                                "     <option id='perc-folder-permission-option-read'  " + checkStatus[0] + " value='" + $.PercFolderHelper().PERMISSION_READ  + "'>" + I18N.message( "perc.ui.folderPropsDialog.permissionValue@Read"  ) + "</option>" +
                                "     <option id='perc-folder-permission-option-write' " + checkStatus[1] + " value='" + $.PercFolderHelper().PERMISSION_WRITE + "'>" + I18N.message( "perc.ui.folderPropsDialog.permissionValue@Write" ) + "</option>" +
                                "     <option id='perc-folder-permission-option-admin' " + checkStatus[2] + " value='" + $.PercFolderHelper().PERMISSION_ADMIN + "'>" + I18N.message( "perc.ui.folderPropsDialog.permissionValue@Admin" ) + "</option>" +
                                "</select>" +
                                    
                             "<input type='hidden' id='perc-folder-id' value='" + fProps.id + "'></input>" +
                             "<input type='hidden' id='perc-folder-oldname' value=\"" + fProps.name + "\"></input>" +

                            // render the list editor widget for folder permission users in the following div
                             "<div id='perc-folder-permission-users'/>" +
                         "</div>" +
                     "</div>" +

                     "<div class='fieldGroup' id='perc-asset-folder-security' style='display:none;'>" +
                         "<div id='perc-asset-folder-sites-container'>" +
                             "<label>Assets in this folder are available to:<br/></label>" +
                            // The Droplist is generated dynamically later
                         "</div>" +
                     "</div>" +

                     "<div class='ui-layout-south'>" +
                     "<div id='perc_buttons' style='z-index: 100;'></div>" +
                      "</div>" +
                     "</div>").perc_dialog( {
                    resizable : false,
                    title: I18N.message( "perc.ui.folderPropsDialog.title@Folder Properties" ),
                    modal: true,
                    dragStart:function() {
                        $("div.ac_results").hide();
                    },
                    closeOnEscape : false,
                    percButtons:{
                        "Save":{
                            click: function(){

                                // get users from the list widget and build
                                // a writePrincipals array of objects to pass to server
                                var writePrincipals = new Array();
                                if(self.listEdit.isEnabled()) {
                                    var users = self.listEdit.getListItems();
                                    for(u=0; u<users.length; u++)
                                        writePrincipals[u] = { name : users[u], type : "USER" };
                                }

                                _saveFolderProps(writePrincipals);
                            },
                            id: "perc-folder-props-save"
                        },
                        "Cancel":{
                            click: function(){
                                _remove();
                            },
                            id: "perc-folder-props-cancel"
                        }
                    },
                    id: "perc-folder-props-dialog",
                    width: "500px"
                });

                // build an array of users from the principals to populate the list editor widget
                var users = [];
                for(u=0; u<writePrincipals.length; u++)
                    users[u] = writePrincipals[u].name;

                $.PercUserService.getUsers(function(status, usersJson) {
                    if(status === $.PercUserService.STATUS_ERROR) {
                    	//TODO: TEST ME I18N
                       $.PercUserView.alertDialog(I18N.message("perc.ui.folder.properties.dialog@Error Loading Users"), usersJson);
                       return;
                    }
            
                    // render the list editor widget in the div declared earlier in the dialog
                    self.listEdit = $.PercListEditorWidget({

                        // the DIV where this component will render
                        "container" : "perc-folder-permission-users",

                        // list of initial users to display
                        "items"     : users,
                        "results"   : $.perc_utils.convertCXFArray(usersJson.UserList.users),
                        // element that will toggle enable/disable of this component
                        "toggler"   : $("#perc-folder-permission"),

                        // values of toggler that enable this component
                        "toggleron" : [$.PercFolderHelper().PERMISSION_READ],

                        // values of toggler that disable this component
                        "toggleroff": [$.PercFolderHelper().PERMISSION_WRITE, $.PercFolderHelper().PERMISSION_ADMIN],
                    
                        "title1" : I18N.message( "perc.ui.folderPropsDialog.title@User Properties" )+":",
                        "title2" : I18N.message( "perc.ui.folderPropsDialog.permissionValue@Write" )
                    });

                    _addFieldGroups();

                    //Only render the sites allowed for asset folder if the folder is a root asset folder.  
                    if(isRootAssetFolder)
                    {
                        // Make an AJAX request to get the list of all sites
                        $.PercServiceUtils.makeJsonRequest(
                            $.perc_paths.SITES_ALL + "/",
                            $.PercServiceUtils.TYPE_GET,
                            false,
                            function(status, result){
                                if (status === $.PercServiceUtils.STATUS_SUCCESS) {
                                    // Un-hide Security collapsible panel
                                    $('#perc-asset-folder-security').show();
                                    
                                    // Append a select to the panel with all available sites
                                    var securityPanel = dialog.find('#perc-asset-folder-sites-container');
                                    var allowedSitesSelect = $("<select name='allowed-sites' id='allowed-sites' multiple='multiple'></select>");
                                    securityPanel.append(allowedSitesSelect);
                                    // The list of allowed sites for the folder is a comma-sepparated 
                                    // string or a number (if only one site was allowed)
                                    var allowedSites = [];
                                    if (fProps.allowedSites !== undefined) {
                                        if (typeof(fProps.allowedSites) == 'number') {
                                            allowedSites.push(fProps.allowedSites.toString());
                                        }
                                        else {
                                            allowedSites = fProps.allowedSites.split(',');
                                        }
                                    }
                                    
                                    // save values to compare on save
                                    currentAllowedSites = allowedSites.sort().join(',');
                                    
                                    // Iterate throug all sites and mark as selected the corresponding allowedSites
                                    var sites = result.data.SiteSummary;
                                    for (var s = 0; s < sites.length; s++) {
                                        var selected = ($.inArray(sites[s].siteId.toString(), allowedSites) >= 0);
                                        var option = $("<option/>")
                                            .attr("value",sites[s].siteId)
                                            .attr("title",sites[s].name)
                                            .text(sites[s].name);
                                        if (selected) option.attr("selected", "selected");
                                        allowedSitesSelect.append(option);
                                    }
                                    

                                    // Generate the droplist: apply the multiselect plugin to the recently generated select
                                    //TODO: TEST ME I18N
                                    allowedSitesMultiselect = $("#allowed-sites").multiselect({
                                        height: 175,
                                        minWidth: 225,
                                        checkAllText: I18N.message("perc.ui.folder.properties.dialog@Select All"),
                                        uncheckAllText: I18N.message("perc.ui.folder.properties.dialog@Deselect All"),
                                        noneSelectedText: I18N.message("perc.ui.folder.properties.dialog@All sites allowed"),
                                        selectedText: function(numChecked, numTotal, checkedItems){
                                            if(numChecked===0)
                                                return I18N.message("perc.ui.folder.properties.dialog@All sites allowed");
                                            else
                                                return I18N.message("perc.ui.folder.properties.dialog@Some sites allowed");
                                        }
                                    });
                                    
                                    // Do manual bindings for the multiselect component, 
                                    // since the binding provided by the plugin doesn't work properly for some events.
                                    
                                    //refresh message when a checkbox is selected
                                    $('input[name=multiselect_allowed-sites]').off('click')
                                        .on('click',function( e ){
                                                $("#allowed-sites").multiselect('update');
                                        });
                                    
                                    //select all link (check all)
                                    $('a.ui-multiselect-all').off('click.multiselect')
                                        .on('click.multiselect', function( e ){
                                            $("#allowed-sites").multiselect('checkAll');
                                         });
                                    
                                    //Deselect all link (uncheck all)
                                    $('a.ui-multiselect-none').off('click.multiselect')
                                        .on('click.multiselect', function( e ){
                                        $("#allowed-sites").multiselect('uncheckAll');
                                    });
                                }
                                else {
                                    // Retrieving the list of all sites was unsuccessful
                                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                                    $.perc_utils.alert_dialog({
                                        title: I18N.message("perc.ui.publish.title@Error"),
                                        content: defaultMsg
                                    });
                                }
                            }
                        );
                    }
                    else
                    {
                        $('#perc-asset-folder-security').hide();
                    }
                });
            }

            //Helper function to remove the dialog and call the callback with false.
            function _remove()  
            {
                dialog.remove();
            }
            //Saves the folder properties, if name is empty shows error.
            //Calls the _saveCallBack to handle the server response on save.
            function _saveFolderProps(newWritePrincipals)  
            {
                if ($("#perc-folder-name").val().trim().length < 1) {
                    $("#perc-folder-name").val("");
                    $("#perc_folder_duplicate_error").text(I18N.message("perc.ui.folder.properties.dialog@Field Required"));
                    $("#perc_folder_duplicate_error").show();
                    return;
                }
                var folderName = $("#perc-folder-name").val().trim();
                folderName =  $.perc_textFilters.WINDOWS_FILE_NAME(folderName);
                var folderProps =  {
                    FolderProperties : {
                       name:folderName,id:$("#perc-folder-id").val(),
                       permission : {
                           accessLevel:$("#perc-folder-permission").val(),
                           writePrincipals : newWritePrincipals
                       }
                   }
                };

                // If there was any allowedSites, add their IDs into the corresponding property
                if (allowedSitesMultiselect !== undefined) {
                    var allowedSites = [];
                    $.each(allowedSitesMultiselect.multiselect('getChecked'), function(index) {
                        allowedSites.push($(this).val());
                    });
                    if (allowedSites.length > 0) {
                        // The elements must be a comma-sepparated string
                        folderProps.FolderProperties.allowedSites = allowedSites.sort().join(',');
                    }
                    else {
                        folderProps.FolderProperties.allowedSites = '';   
                    }
                }
                
                if (currentAllowedSites !== undefined && folderProps.FolderProperties.allowedSites !== currentAllowedSites)
                {
                    var options = {
                                    id       : "perc-folder-properties-changed-allowed-sites-warning",
                                    title    : I18N.message("perc.ui.page.general@Warning"),
                                    content  : I18N.message("perc.ui.folder.properties.dialog@Content Already Queued"),
                                    okCallBack : function(){
                                        $.PercPathService.saveFolderProperties(folderProps, _saveCallBack);
                                    }
                            };
                            $.perc_utils.alert_dialog(options);
                }
                else
                {
                    $.PercPathService.saveFolderProperties(folderProps, _saveCallBack);
                }
                
            }
            //If status is success removes the dialog and calls the call back with save option, shows the errors to the user otherwise.
            function _saveCallBack(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    // Save was successful close the dialog and invoke callback with no arguments
                    var newName = $("#perc-folder-name").val().trim();
                    dialog.remove();
                   if(folderSysPathName.match("^/Sites/") || folderSysPathName.match("^//Sites/")){
                       $.PercRedirectHandler.createRedirect(folderSysPathName,folderSysPath + "/" + newName,"folder")
                       .fail(function(errMsg){
                            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.folder.properties.dialog@Redirect Creation"), content: errMsg, okCallBack: function(){
                                callback(newName);
                            }});
                       })
                       .done(function(){
                            callback(newName);
                       });
                   }
                   else{
                       callback(newName);
                   }
                }
                else
                {
                    var defaultMsg = 
                    $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    var code = $.PercServiceUtils.extractFieldErrorCode(result.request);
                    if(code === "`FolderProperties#name")
                    {
                        var msg = I18N.message("perc.ui.folder.properties.dialog@Cannot Rename Folder") + $("#perc-folder-oldname").val() +
                         I18N.message("perc.ui.folder.properties.dialog@To") + $("#perc-folder-name").val().trim() +
                         I18N.message("perc.ui.folder.properties.dialog@Object With Same Name");
                        $("#perc_folder_duplicate_error").text(msg)
                        .show();
                    }
                    else if(code === "saveFolderProperties#reservedName")
                    {
                        $("#perc_folder_duplicate_error").text(defaultMsg);
                        $("#perc_folder_duplicate_error").show();
                    }                                        
                    else
                    {
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: defaultMsg});
                    }
                }
            }
            
            // A private helper method to group the fields and create collapsible sections
            function _addFieldGroups() {
                var dialog = $('#perc-folder-props-dialog');
                var fieldGroups = [
                    { groupName : "perc-folder-general-properties-container", groupLabel : I18N.message("perc.ui.folder.properties.dialog@General")},
                     { groupName : "perc-user-permissions-container", groupLabel : I18N.message("perc.ui.folder.properties.dialog@Permissions")},
                     { groupName : "perc-asset-folder-sites-container", groupLabel : I18N.message("perc.ui.folder.properties.dialog@Security")}
                ];
        
                $.each(fieldGroups, function(index) {
                    // Create HTML markup with the groupName minimizer/maximizer and
                    // insert it before the 1st field in each group
                    var minmaxClass = (index === 0) ? "perc-section-items-minimizer" : "perc-section-items-maximizer";
                    var groupHtml =
                        "<div class='perc-section-header'>" +
                            "<div class='perc-section-label' groupName='" + this.groupName + "'>" +
                                "<span class='perc-min-max " + minmaxClass + "' ></span>" + this.groupLabel +
                            "</div>" +
                        "</div>";

                    dialog.find('#' + this.groupName).before(groupHtml);

                    // The first group will be the only one expanded (hide all others)
                    index !== 0 && dialog.find('#' + this.groupName).hide();
                });

                // Bind collapsible event
                dialog.find(".perc-section-label").off("click").on("click",function() {
                    var self = $(this);
                    self.find(".perc-min-max")
                        .toggleClass('perc-section-items-minimizer')
                        .toggleClass('perc-section-items-maximizer');
            
                    dialog.find('#' + self.attr('groupName')).toggle();
                });
            }           

            
            $.perc_filterField($('#perc-folder-name'), $.perc_textFilters.URL);
        
        }// End open dialog      
        
        return {"open": openDialog};
    
    };

})(jQuery);
