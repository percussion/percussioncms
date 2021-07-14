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
 * PercPageView.js
 * 
 * Handles user interaction with Edit page.
 * 
 * (*) Iframe
 * (*) Handles dirty page and confirmation when changing tabs
 * (*) Loads content and layout tabs
 *
 */
(function($) {

    
    var dirtyController = $.PercDirtyController;
    
    $.PercUserView = function() {
        
        var viewApi = {
            init                   : init,
            updateListOfUsers      : updateListOfUsers,
            updateUserNameField    : updateUserNameField,
            updateAssignedRoles    : updateAssignedRoles,
            updateAvailableRoles   : updateAvailableRoles,
            updateEmail            : updateEmail,
            resetUserDetails       : resetUserDetails,
            selectUser             : selectUser,
            showSelectedUserEditor : showSelectedUserEditor,
            showNewUserEditor      : showNewUserEditor,
            updateImportUsersDialog: updateImportUsersDialog,
            showImportWarning      : showImportWarning,
            showImportError        : showImportError,
            disableUserImport      : disableUserImport,
            alertDialog            : alertDialog
        }

        // A snippet to adjust the frame size on resizing the window.
        $(window).on("resize",function() {
            fixIframeHeight();
            fixTemplateHeight();
        });

        // UI Elements
        var importDialog          = $("#perc-users-import-users-dialog-fixed");
        
        // table listing importing users in import dialog
        var importingUsersTable   = $("#perc-users-directory-users-table");
        var searchBox             = $("#perc-users-search-input");
        var searchButton          = $("#perc-users-search-button");
        var selectAllCheckbox     = $("#perc-users-directory-users-selectall-checkbox");
        var selectAllLabel        = $("#perc-users-directory-users-selectall-label");
        var cancelButton          = $("#perc-users-directory-users-cancel-button");
        var startUserImportButton = $("#perc-users-import-users-button");
        var importButton          = $("#perc-users-directory-users-import-button");
        var selectUserLabel       = $("#perc-users-select-at-least-one-user-label");
        var narrowSearchLabel     = $("#perc-users-narrow-search");
        // TODO: move all jquery ID and class references here like above
        
        // constants
        var maxNumberOfUsers = 200;
        
        // state variables
        var directoryServiceAvailable = true;
        var currentUserList = [];
        var currentUserIds = new Object();
        var addingNewUser = false;
        
        // I18N strings
        // TODO: move all the I18N message calls here and copy them to variables

        var controller = $.PercUserController;
        controller.init(viewApi);
        
        function init() {
            resetUserDetails();
            $("#perc-users-edit-user-button").off("click").on("click",function(){
                controller.editSelectedUser();
                addingNewUser = false;
            });
            $("#perc-users-add-user-button").off("click").on("click",function(){
                dirtyController.confirmIfDirty(function(){
                	addingNewUser = true;
                    $("#perc-users-external-user-label").hide();
                    controller.addNewUser();
                });
            });
            $("#perc-users-save").off("click").on("click",function(){
                save();
                addingNewUser = false;
            });
            $("#perc-users-cancel").off("click").on("click",function(){
                controller.cancel();
                addingNewUser = false;
            });
            $("#perc-users-remove-role-button").off("click").on("click",function(){
                removeRoleFromSelectedUser();
            });
            $("#perc-users-add-role-button").off("click").on("click",function(){
                addRoleToSelectedUser();
            });
            
            importDialog.dialog({
                title          : I18N.message("perc.ui.users.import.dialogs@ImportDirectoryUsers"),
                autoOpen       : false,
                closeOnEscape  : true,
                modal          : true,
                height         : 575,
                dialogClass    : 'alert',
                width          : 694,
                resizable      : false,
                zIndex         : 5000
            });

            if(directoryServiceAvailable)
            
                // handle import users button. Display import dialog
                startUserImportButton.off("click").on("click",function(){
                    disableImportButton();
                    dirtyController.confirmIfDirty(function(){
                        
                        // reset the dialog list of users and checkbox from the last search
                        importingUsersTable.empty();
                        importDialog.dialog("open");
                        selectAllCheckbox.attr("checked", false);
                        selectUserLabel.hide();
                        narrowSearchLabel.hide();
                    });
                });
            else {
                   disableImportButton();
            }
            
            // if they click on search, controller gets the users from the service and updates the import dialog
            searchButton.on("click",function(){

                // uncheck select all checkbox 
                selectAllCheckbox.prop("checked", false);

                // get the search query and pass it to the controller to pass it to the service
                var usernameStartsWith = searchBox.val();
                controller.findDirectoryUsers(usernameStartsWith);
            });
            
            // if the select all checkbox is clicked, toggle all the user checkboxes
            selectAllCheckbox.on("click",function() {
                toggleSelectAll();
                updateImportButton();
            });
            
            selectAllLabel.on("click",function(){
                selectAllCheckbox.trigger("click");
                toggleSelectAll();
                updateImportButton();
            });

            // if cancel, dismiss dialog
            cancelButton.on("click",function() {
                importDialog.dialog('close');
            });

            // handle import button
            importButton.on("click",function() {
                if(importButton.hasClass("perc-users-directory-users-import-button-disabled"))
                   return;
                
                // get all the selected checkboxes
                var selectedUserCheckBoxes = $(".perc-users-checkboxes:checked");
                // iterate over the checkboxes and create an array with the user names which are in the id attribute
                var selectedUserNames = [];
                selectedUserCheckBoxes.each(function(){
                    selectedUserNames.push($(this).attr("id"));
                });
                
                if(selectedUserNames.length === 0) {
                    alertDialog(I18N.message("perc.ui.page.general@Warning"), I18N.message("perc.ui.users.import.dialogs@SelectOneUser"));
                    return;
                }
                
                // pass usernames to controller to use the service to add the users
                controller.importDirectoryUsers(selectedUserNames);
                importDialog.dialog('close');
            });
            
            narrowSearchLabel.html(I18N.message("perc.ui.users.import.dialogs@NarrowSearch", [maxNumberOfUsers]));
            
            //
            // maxusersLabel.html(maxNumberOfUsers);
        }
        
        /**
         * Updates the state of the import button in the import dialog.
         * If there are no users selected, then the button is disabled.
         */
        function updateImportButton() {
            var selectedUserCheckBoxes = $(".perc-users-checkboxes:checked");
            var userCheckBoxes = $(".perc-users-checkboxes");
            if(selectedUserCheckBoxes.length > 0) {
                selectUserLabel.hide();
                enableImportButton();
            } else if(userCheckBoxes.length > 0 && selectedUserCheckBoxes.length == 0){
                selectUserLabel.show();
                disableImportButton();
            }
        }

        /**
         * Decorates import button in import dialog with enabled image background
         */        
        function enableImportButton() {
            importButton.attr("title", I18N.message("perc.ui.users.import.tooltips@ClickToImport"));
            importButton.addClass("perc-users-directory-users-import-button-enabled");
            importButton.removeClass("perc-users-directory-users-import-button-disabled");
        }
        
        /**
         * Decorates import button in import dialog with disabled image background
         */        
        function disableImportButton() {
            importButton.attr("title", I18N.message("perc.ui.users.import.tooltips@SelectUsersToImport"));
            importButton.addClass("perc-users-directory-users-import-button-disabled");
            importButton.removeClass("perc-users-directory-users-import-button-enabled");
        }

        /**
         * Show list of users that were not imported because of server side errors
         * 
         * @param users {array} array of user objects containing usernames and their status.
         * Status is one of "SUCCESS" (imported), "DUPLICATE" and "ERROR" (not imported)
         */
        function showImportWarning(users) {
            
            // iterate over the list of users looking for the ones that were not imported.
            // create a list of duplicates and error. For now we only show error ones.
            var errorList = [];
            var duplicateList = [];
            for(let u=0; u<users.length; u++) {
                if(users[u].status !== "SUCCESS") {
                    if(users[u].status === "ERROR")
                       errorList.push(users[u]);
                    else if(users[u].status === "DUPLICATE")
                       duplicateList.push(users[u]);
                }
            }

            // create a table with the list of users. Right now we only show the username but we could also show reason.
            var table = "<div id='perc-users-import-warning-scrollpane'>" +
                        "<table id='perc-users-import-warning'>";            
            for(let u=0; u<errorList.length; u++) {
                // one user per row with the username
                var row = templateUserStatus.replace(/_username_/g, errorList[u].name);
                table += row;
            }
            table += "</table></div>";
            
            // build the message with plus table and display in an alert dialog            
            var message = I18N.message("perc.ui.users.import.dialogs@LdapImportFailed");
            message = $.perc_utils.replaceURLWithHTMLLinks(message);
            message += "<br/><br/>"+table;
            alertDialog(I18N.message("perc.ui.users.import.dialogs@ErrorImportingUsers"), message, 450);
        }

        function showImportError()
        {
            // build the message and display in an alert dialog            
            var message = I18N.message("perc.ui.users.import.dialogs@LdapImportFailed");
            alertDialog(I18N.message("perc.ui.users.import.dialogs@ErrorImportingUsers"), message, 450);
        }
        
        /**
         * Toggles all user checkboxes when Select All checkbox is checked/unchecked
         */
        function toggleSelectAll() {
            if(selectAllCheckbox.attr("checked")) {
                $(".perc-users-checkboxes").prop("checked",true);
            } else {
                $(".perc-users-checkboxes").prop("checked",false);
            }
        }
        
        function resetUserDetails() {
            $("#perc-users-password-block").hide();
            $("#perc-users-username-field").val("");
            $("#perc-users-password-field").val("*******");
            $("#perc-users-password-confirm-field").val("*******");
            $("#perc-users-email-label").hide();
            $("#perc-users-email-field").val("");
        
           var availableRolesSelectList = $("#perc-users-available-roles > select");
           var assignedRolesSelectList  = $("#perc-users-assigned-roles  > select");
        
            assignedRolesSelectList
                .on("click",function(){availableRolesSelectList.attr('selectedIndex', '-1'); })
                .html("");
            availableRolesSelectList
                .on("click",function(){assignedRolesSelectList.attr('selectedIndex', '-1'); })
                .html("");
           
            $("#perc-users-username-field")
                .addClass("perc-users-password-field-view-user")
                .prop("readonly",true);
            $("#perc-users-edit-user-button").show();
        }
        
        function updateListOfUsers(userArray) {
            
            currentUserList = userArray;
            currentUserIds  = {};
            
            // clear the list of users
            var $userListElement = $("#perc-username-list > ul");
            $userListElement.html("");

            // iterate over the list of users and add each user to the list of users
            var $html = "";
            var htmlLi = "";
            var ellipsis = "...";
            for(let i in userArray) {
                var username = userArray[i].toString();
                var id = "perc-users-id-" + i;
                currentUserIds[username] = id;
                // use the user item template replacing the username
                htmlLi = userLiTpl.replace(/_id_/g, id);
                ellipsis = username.length > 20 ? "..." : "";
                htmlLi = htmlLi.replace(/_username_/g, username.substring(0,20) + ellipsis);
                // append html to DOM
                $userListElement.append(htmlLi);
            }
            
            // bind select event on each user element
            $(".perc-username").off("click").on("click",function(event){
                // clicking on the user selects the user
                // element's id contains the username
                var self = this;
                dirtyController.confirmIfDirty(function(){
                    var id = $(self).attr('id');
                    var username;
                    for(username in currentUserIds)
                       if(currentUserIds[username] == id)
                           break;
                    controller.selectUser(username);
                });
            });
            
            var currentUser = controller.getCurrentUser();
            var id = currentUserIds[currentUser];
            $("#perc-username-list ul li div").addClass("perc-user-delete").removeClass("perc-user-delete-disabled").attr("title","Delete user");
            $("#perc-username-list #" + id + " #" + id).addClass("perc-user-delete-disabled").removeClass("perc-user-delete");
            

            // bind the delete button for each user element         
            $(".perc-user-delete").off("click").on("click",function(event) {
                
                // stop event because it is nested inside an element that is already bound
                event.stopPropagation();
                
                // element's id contains the username
                var id = $(this).parent().attr('id');
                var username = getUsernameFromId(id);
                
                if(username == controller.getCurrentUser()) {
                    alertDialog(I18N.message("perc.ui.page.general@Warning"), I18N.message("perc.ui.perc.user.view@Cannot User Currently Logged In"));
                    return;
                }
                
                var settings = {
                    id: 'perc-user-delete-confirm-dialog',
                    title: I18N.message("perc.ui.perc.user.view@Confirm User Deletion"),
                    question: "<span style='padding-right:11px'>" + I18N.message("perc.ui.perc.user.view@Are You Sure Remove User") + "" + username + "</span>",
                    success: function() { controller.deleteUser(username); },
                    yes: I18N.message("perc.ui.perc.user.view@Continue Anyway")
                };
                    
                $.perc_utils.confirm_dialog(settings);
                
            });
        }

        /**
         * Displays the list of users to be imported. Invoked from the controller with a list of users from directory
         * service after controller retrieves users from service when user clicks IMPORT button
         * 
         * @param users {array} of users from REST service. Format is:
         * 
         * [ {"name":"Alice"}, {"name":"Bob"}, {"name":"Charlie"} ]
         */
        function updateImportUsersDialog(users) {
            
            importingUsersTable.empty();
            
            if(users.length > maxNumberOfUsers)
                narrowSearchLabel.show();
            else
                narrowSearchLabel.hide();
                
            
            // iterate over the list of users, appending a row for each to the table
            for(i=0; i<users.length; i++) {
                if(i>=maxNumberOfUsers)
                   break;
                var row = templateUserRow.replace(/_username_/g, users[i].name);
                importingUsersTable.append(row);
            }
            
            if(users.length > 0)
                $(".perc-users-checkboxes").on("click",function() {
                    updateImportButton();
                });
            
            // open the dialog
            importDialog.dialog('open');

            updateImportButton();
        }
        
        /**
         * Disables the User Import button because the Directory Service might be unavilable.
         * This is invoked by the controller right when it loads at very beginning.
         * The controller uses service to get status
         */
        function disableUserImport() {
            directoryServiceAvailable = false;
            startUserImportButton.removeClass("perc-users-import-users-button-enabled");
            startUserImportButton.addClass   ("perc-users-import-users-button-disabled");
            startUserImportButton.attr("title", I18N.message("perc.ui.users.import.tooltips@UserImportUnavailable"));
            startUserImportButton.off();
        }
        
        function showSelectedUserEditor() {
            $("#perc-users-username-field")
                .addClass("perc-users-password-field-view-user")
                .on("change",function() {
                    dirtyController.setDirty(true, "user");
                });
           $("#perc-users-email-field")
                .removeClass("perc-users-password-field-view-user")
                .prop("readonly",false)
                .on("change", function() {
                    dirtyController.setDirty(true, "user");
                });
            $("#perc-users-email-label").show();
            showPasswordEditor(false);
            $("#perc-users-edit-user-button").hide();
        }
        
        function showNewUserEditor() {
            $("#perc-users-username-field")
                .removeClass("perc-users-password-field-view-user")
                .prop("readonly",false)
                .val("")
                .trigger("focus")
                .on("change",function() {
                    dirtyController.setDirty(true, "user");
                });
            $("#perc-users-username-label").addClass("perc-required-field");    
            showPasswordEditor(true);
            $("#perc-users-email-field")
                .removeClass("perc-users-password-field-view-user")
                .prop("readonly",false)
                .val("")
                .on("change",function() {
                    dirtyController.setDirty(true, "user");
                });
            $("#perc-users-email-label").show();
            unhighlightAllUsers();
            $("#perc-users-edit-user-button").hide();
        }
        
        function showPasswordEditor(newUser) {
            var dummyPassword = newUser ? "":"*******";
            $("#perc-users-password-block").show();
			$("#perc-users-password-label").addClass("perc-required-field"); 
			$("#perc-users-password-confirm-label").addClass("perc-required-field");
            $("#perc-users-password-field")
                .val(dummyPassword)
                .off("click")
                .on("click",function() {
                    $(this)
                        .off("change")
                        .on("change",function() {
                            dirtyController.setDirty(true, "user");
                        });
                    $("#perc-users-password-confirm-field")
                        .off("change")
                        .on("change",function() {
                            dirtyController.setDirty(true, "user");
                        });
                });
            $("#perc-users-password-confirm-field")
                .val(dummyPassword)
                .off("click")
                .on("click",function() {
                    $(this)
                        .off("change")
                        .on("change",function() {
                            dirtyController.setDirty(true, "user");
                        });
                    $("#perc-users-password-field")
                        .off("change")
                        .on("change",function() {
                            dirtyController.setDirty(true, "user");
                        });
                });
        }
        
        function updateUserNameField(username) {
            var userNameField = $("#perc-users-username-field");
            userNameField.val(username);
        }
        
        function updateEmail(email) {
            var emailField = $("#perc-users-email-field");
            emailField.val(email);
        }
        
        /**
         * Update the List of Assigned Roles for a User
         */
        function updateAssignedRoles(assignedRolesArray) {
            var $assignedRoles = $("#perc-users-assigned-roles > select");
            $assignedRoles.html("");
    
            // iterate over the list of roles and add it to the role option
            for(i in assignedRolesArray) {
                var userRole = assignedRolesArray[i];
                // use the user item template replacing the username
                var html = $("<option/>").val(userRole).html(userRole);
                // append html to DOM
                $assignedRoles.append(html);
            }
        }
    
        /**
         * Update the Available Roles Minus the Assigned Roles
         */
        function updateAvailableRoles(rolesArrayCache, assignedRoles) {
            // make sure that assigned roles is not null
            if(assignedRoles == null)
               assignedRoles = [];
               
            // get the DOM element where we are adding the roles
            var availableRoles = $("#perc-users-available-roles > select");
            availableRoles.html("");    // clear it
            var assignedIndex = 0;      // index to assignedRoles array
            
            // go through all the roles and only add them to the available roles
            // if it's not already in the assigned roles array
            for(i in rolesArrayCache) {
                var assignedRole = assignedRoles[assignedIndex];
                if(rolesArrayCache[i] != assignedRole) {
                    var html = $("<option/>").val(rolesArrayCache[i]).html(rolesArrayCache[i]);
                    availableRoles.append(html);
                } else {
                    assignedIndex++;
                }
            }
        }

        /**
         * Unhighlight all users and
         * Highlight just the current user
         */
        function selectUser(username, type) {

            var id = currentUserIds[username];
            
            $("#perc-users-username-field")
                .addClass("perc-users-password-field-view-user")
                .prop("readonly",true);
            $("#perc-users-email-field")
                .addClass("perc-users-password-field-view-user")
                .prop("readonly",true)
            if ($("#perc-users-email-field").val() == ""){
                $("#perc-users-email-label").hide();
            }else{
                $("#perc-users-email-label").show();
            }

            // unhighlight all other users
            unhighlightAllUsers();
            
            // highlight selected user
            $("#perc-username-list #" + id)
                .css("background-color","#caf589")
                .addClass("perc-users-selected");
            
            $("#perc-users-password-block").hide();
            $("#perc-users-username-label").removeClass("perc-required-field");
            
            if(type == "DIRECTORY") {
                $("#perc-users-external-user-label").show();
                $("#perc-users-edit-user-button").hide();
            } else if(type == "INTERNAL") {
                $("#perc-users-external-user-label").hide();
                $("#perc-users-edit-user-button").show();
            }
        }
        
        function unhighlightAllUsers() {
            // unhighlight all users
            $("#perc-username-list ul li")
                .css("background-color","")
                .removeClass("perc-users-selected");
            $("#perc-username-list ul li div").css("background-color","");
        }

        /**
         * Remove role from assigned roles to available roles
         */
        function removeRoleFromSelectedUser() {
            
            // get assigned role being removed
            var assignedRoleList = $("#perc-users-assigned-roles > select");
            var assignedRoleSelected = $("#perc-users-assigned-roles > select > option:selected");
            var selectedAssignedRoleValue = assignedRoleList.val();
            
            if(selectedAssignedRoleValue == "Admin" && !controller.isAddingNewUser()
            		&& controller.getSelectedUser() == controller.getCurrentUser()) {
               alertDialog(I18N.message("perc.ui.page.general@Warning"), I18N.message("perc.ui.perc.user.view@Cannot Remove Admin"));
               return;
            }
            
            if(selectedAssignedRoleValue == null)
                return;
            if(assignedRoleSelected)
                assignedRoleSelected.remove();
            
            // add role to available role list
            var availableRole = $("<option/>").val(selectedAssignedRoleValue).html(selectedAssignedRoleValue);
            var availableRoles = $("#perc-users-available-roles > select")
                .append(availableRole);

            dirtyController.setDirty(true, "user");

        }
    
        /**
         * Remove role from available roles to assigned roles
         */
        function addRoleToSelectedUser() {
            
            // get assigned role being removed
            var availableRoleList = $("#perc-users-available-roles > select");
            var availableRoleSelected = $("#perc-users-available-roles > select > option:selected");
            var selectedAvailableRoleValue = availableRoleList.val();
            if(selectedAvailableRoleValue == null)
                return;
            if(availableRoleSelected)
                availableRoleSelected.remove();
            
            // add role to available role list
            var assignedRole = $("<option/>").val(selectedAvailableRoleValue).html(selectedAvailableRoleValue);
            var assignedRoles = $("#perc-users-assigned-roles > select")
                .append(assignedRole);

            dirtyController.setDirty(true, "user");

        }
    
        function save() {
        	
            // retrieve assigned roles from select element
            var assignedRoles = new Array();
            $("#perc-users-assigned-roles > select option").each(function() {
                assignedRoles.push($(this).val());
            });
            
            var username  = $("#perc-users-username-field").val();
            // if we are adding a new user, verify that this user is not already in the list of users,
            // i.e., that it's not a duplicate
            if(addingNewUser)
                for(u=0; u<currentUserList.length; u++)
                    if(username.toString().toLowerCase() == currentUserList[u].toString().toLowerCase() ) {
                        alertDialog(I18N.message("perc.ui.labels@Error"), I18N.message("perc.ui.users.import.dialogs@UserAlreadyExists",[username,currentUserList[u]]));
                        return;
                    }
            var password1 = $("#perc-users-password-field").val();
            var password2 = $("#perc-users-password-confirm-field").val();
            var email = $("#perc-users-email-field").val();
            controller.save(username, password1, password2, assignedRoles, email);
        }
    
        function alertDialog(title, message, w) {
            if(w == null || w == undefined || w == "" || w < 1)
                w = 400;
            $.perc_utils.alert_dialog({title : title, content : message, width : w});
        }

        function getUsernameFromId(id) {
            var username;
            for(username in currentUserIds)
               if(currentUserIds[username] == id)
                   break;
            return username;
        }
		
        // templates
        // template html for each user item
        var userLiTpl = '' +
                '<li' +
                '     id="_id_"' +
                '     class="perc-username"' +
                '     title="_username_">' +
                '         _username_' +
                '     <div class="perc-user-delete">' +
                '     </div>' +
                '</li>';

        var templateUserStatus="<tr><td class='perc-users-row'>" +
                "<span>_username_</span></td><td>" +
//              "<span>_status_</span></td></td>" +
                "<span></span></td></td>" +
                "</td></tr>";

        var templateUserRow = "<tr><td>" +
                "<input class='perc-users-checkboxes' id='_username_' type='checkbox'></td><td>" +
                "<span>_username_</span>" +
                "</td></tr>";                

    };
})(jQuery);
