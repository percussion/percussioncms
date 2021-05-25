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
 * PercRoleView.js
 * @author Luis Mendez
 *
 */
(function($) {

    var dirtyController = $.PercDirtyController;

    $.PercRoleView = function() {

        var viewApi = {
            init                   : init,
            updateListOfRoles      : updateListOfRoles,
            updateRoleNameField    : updateRoleNameField,
            updateAssignedUsers    : updateAssignedUsers,
            updateDescriptionField : updateDescriptionField,
            updateHomepageField    : updateHomepageField,
            resetRoleDetails       : resetRoleDetails,
            selectRole             : selectRole,
            showSelectedRoleEditor : showSelectedRoleEditor,
            showNewRoleEditor      : showNewRoleEditor,
            alertDialog            : alertDialog,
            getDescription         : getDescription
        }

        // A snippet to adjust the frame size on resizing the window.
        $(window).on("resize",function() {
            fixIframeHeight();
            fixTemplateHeight();
        });

        var container = $("#perc-roles-list");
        var editingRole = false;
        var addingRole = false;
        var deletingRole = false;
        var controller = $.PercRoleController;
        controller.init(viewApi);

        /**
         * Confirm dialog to delete Role
         */
        function deleteRole(rolename){
            // retrieve assigned users
            var assignedUsers = new Array();
            $(".perc-roles-assigned-users-list span").each(function() {
                assignedUsers.push($(this).html());
            });
            var roleObj = {"Role":{"name":rolename, "description":getDescription, "users":assignedUsers}};
            controller.validateAndDeleteRole(roleObj);
        }

        function init() {
            resetRoleDetails();
            $("#perc-roles-edit-role-button").off("click").on("click", function(evt){
                editingRole = true;
                controller.editSelectedRole();
                disableButtons();
                unhighlightAllUsers();
            });

            //Bind Add Users to Role event
            $(".perc-roles-addusers-button").off("click").on("click", addUsers);
            //Bind remove Users from Role event
            $(".perc-roles-removeusers-button").off("click").on("click", removeUsers);

            //Bind Save event
            $("#perc-roles-save").off("click").on("click", function(evt){
                save();
            });

            //Bind Cancel event
            $("#perc-roles-cancel").off().on("click", function(evt){
                controller.cancel();
            });

            var config = {
                listItem: [],
                title: I18N.message("perc.ui.perc.role.view@Roles"),
                addTitle: I18N.message("perc.ui.perc.role.view@Add Role"),
                deleteTitle: I18N.message("perc.ui.perc.role.view@Delete Role"),
                //enableDelete: false,
                createItem: addNewRole,
                deleteItem: deleteRole,
                selectedItem: controller.selectRole
            }

            $.PercDataList.init(container, config);
        }

        function addUsers(){
            var assignedUsers = new Array();
            $(".perc-roles-assigned-users-list span").each(function() {
                assignedUsers.push($(this).html());
            });
            controller.getAvailableUsers(assignedUsers);
        }

        function removeUsers(){
            if (!addingRole){
                if (!deletingRole) {
                    deletingRole = true;
                    var remainUsers = new Array();
                    var selectedUsers = new Array();
                    //Get a list of selected users and the remaining.
                    $(".perc-roles-assigned-users-list li").each(function() {
                        var userRow = $(this);
                        var user = userRow.find("span").html();
                        if (userRow.is(".perc-assigned-user-selected"))
                            selectedUsers.push(user); // users to be validated before delete
                        else
                            remainUsers.push(user);
                    });
                    controller.validateAndRemoveUsers(selectedUsers, remainUsers);
                }
            }
            else{
                $(".perc-roles-assigned-users-list .perc-assigned-user-selected").remove();
                disableRemoveUsers();
            }
        }

        function addNewRole(){
            addingRole = true;
            controller.addNewRole();
            disableButtons();
            disableRemoveUsers();
            $(".perc-required-label").show();
            $("#perc-roles-edit-role-button").hide();
        }

        function resetRoleDetails() {
            $("#perc-orig-roles-name-field").val("");
            $("#perc-roles-name-field").val("");
            $("#perc-roles-description-field").val("");
            $("#perc-roles-homepage-field").val("");
            $(".perc-roles-assigned-users-list").html("");

            $("#perc-roles-name-field")
                .addClass("perc-roles-field-readonly")
                .attr("readonly","readonly");
            $("#perc-roles-description-label").hide();
            $("#perc-role-save-cancel-block").hide();
            $("#perc-roles-edit-role-button").show();
        }

        function updateListOfRoles(roleArray) {
            $.PercDataList.updateList(container, roleArray);
        }

        function showSelectedRoleEditor() {
            if (controller.getSelectedRole() != "Admin" && controller.getSelectedRole() != "Designer"){
                $("#perc-roles-name-field")
                    .removeClass("perc-roles-field-readonly")
                    .removeAttr("readonly")
                    .change(function() {
                        dirtyController.setDirty(true, "role");
                    });
            }

            $("#perc-roles-description-field")
                .removeClass("perc-roles-field-readonly")
                .removeAttr("readonly")
                .attr("style", "height: 55px;width: 100%;")
                .change(function() {
                    dirtyController.setDirty(true, "role");
                });
            $("#perc-roles-description-label").show();

            $("#perc-roles-homepage-field")
                .show()
                .change(function() {
                    dirtyController.setDirty(true, "role");
                });
            $("#perc-roles-homepage-label").show();
            $("#perc-roles-homepage-field-readonly").hide();
            $("#perc-roles-edit-role-button").hide();
            $("#perc-role-save-cancel-block").show();
            $("#perc-roles-description-field").focus();
        }

        function showNewRoleEditor() {
            $("#perc-roles-name-field")
                .removeClass("perc-roles-field-readonly")
                .removeAttr("readonly")
                .val("")
                .focus()
                .change(function() {
                    dirtyController.setDirty(true, "role");
                });
            $("#perc-roles-name-label").addClass("perc-required-field");
            $("#perc-roles-description-field")
                .removeClass("perc-roles-field-readonly")
                .removeAttr("readonly")
                .attr("style", "height: 55px;width: 100%;")
                .val("")
                .change(function() {
                    dirtyController.setDirty(true, "role");
                });
            $("#perc-roles-description-label").show();

            $("#perc-roles-homepage-field")
                .show()
                .change(function() {
                    dirtyController.setDirty(true, "role");
                }).val("Home");
            $("#perc-roles-homepage-label").show();
            $("#perc-roles-homepage-field-readonly").hide();
            unhighlightAllRoles();
            $("#perc-roles-edit-role-button").hide();
            $("#perc-role-save-cancel-block").show();
        }

        function updateRoleNameField(name) {
            var roleOrigNameField = $("#perc-orig-roles-name-field");
            roleOrigNameField.val(name);
            var roleNameField = $("#perc-roles-name-field");
            roleNameField.val(name);
        }

        function updateDescriptionField(description) {
            var descriptionField = $("#perc-roles-description-field");
            descriptionField.val(description);
        }

        function updateHomepageField(homepage) {
            $("#perc-roles-homepage-field").val(homepage);
            $("#perc-roles-homepage-field-readonly").text(homepage);
        }

        /**
         * Update the List of Assigned user of a role
         */
        function updateAssignedUsers(assignedUsersArray) {
            var $assignedUsers = $(".perc-roles-assigned-users-list");
            var currentUser = controller.getCurrentUser();
            var ulUsers = $("<ul class='perc-assigned-users' />");
            $assignedUsers.html("");

            // iterate over the list of users and add it to the user option
            for(i in assignedUsersArray) {
                var userName = assignedUsersArray[i];
                var liUser = $("<li class='perc-assigned-user-entry'/>")
                    .append(
                        $("<span />")
                            .html(userName)
                            .attr("title", userName)
                    )
                //When Admin role is selected, if the list of users will contain the logged in user, then that user name will be disabled.
                //Note: This ensures there is always at least one user in the Admin role.
                if (controller.getSelectedRole() == "Admin" && (currentUser == userName)){
                    liUser.find("span").css("color", "#000000");
                }
                else{
                    liUser.on("click", selectUser);
                }
                ulUsers.append(liUser);
            }
            // append html to DOM
            $assignedUsers.append(ulUsers);
        }

        function selectUser(event){
            if (!editingRole){
                var currentUser = controller.getCurrentUser();
                var userRow = $(this);
                var userSelected = userRow.find("span").html();
                if (userRow.is(".perc-assigned-user-selected"))
                    userRow.removeClass("perc-assigned-user-selected");
                else
                    userRow.addClass("perc-assigned-user-selected");
                if($(".perc-roles-assigned-users-list .perc-assigned-user-selected span").length > 0)
                    enableRemoveUsers();
                else
                    disableRemoveUsers();
            }
        }

        /**
         * Unhighlight all roles and
         * Highlight just the current role
         */
        function selectRole(rolename) {
            deletingRole = false;
            if (editingRole || addingRole){
                editingRole = false;
                addingRole = false;
                enableButtons();
                $(".perc-required-label").hide();
                $("#perc-roles-edit-role-button").show();
            }
            if (rolename == "Admin" || rolename == "Designer"){
                $.PercDataList.disableDeleteButton(container);
            }
            else{
                $.PercDataList.enableDeleteButton(container);
            }
            disableRemoveUsers();

            $.PercDataList.selectItem(container, rolename);
            $("#perc-roles-name-field")
                .addClass("perc-roles-field-readonly")
                .attr("readonly","readonly");
            var descriptionLabel = $("#perc-roles-description-label");
            var descriptionField = $("#perc-roles-description-field");
            descriptionField
                .addClass("perc-roles-field-readonly")
                .attr("readonly","readonly")
            if (descriptionField.val() == ""){
                descriptionLabel.hide();
            }else{
                descriptionLabel.show();
            }

            $("#perc-roles-homepage-field").hide();
            $("#perc-roles-homepage-field-readonly").show();
            $("#perc-roles-homepage-label").show();

            $("#perc-roles-name-label").removeClass("perc-required-field");
            $("#perc-roles-edit-role-button").show();
            $("#perc-role-save-cancel-block").hide();
        }

        function unhighlightAllRoles() {
            // unhighlight all roles
            $("#perc-rolename-list ul li")
                .css("background-color","")
                .removeClass("perc-roles-selected");
            $("#perc-rolename-list ul li div").css("background-color","");
        }

        function unhighlightAllUsers() {
            // unhighlight all users
            $(".perc-roles-assigned-users-list .perc-assigned-user-selected")
                .removeClass("perc-assigned-user-selected")
        }

        /**
         * On Edit or Create we disable add/delete Role and add/remove users.
         */
        function disableButtons(){
            $.PercDataList.disableButtons(container);
            if (editingRole){
                $(".perc-roles-addusers-button")
                    .addClass("perc-item-disabled")
                    .off();
                disableRemoveUsers();
            }
        }

        function enableButtons(){
            $.PercDataList.enableButtons(container);
            $(".perc-roles-addusers-button")
                .removeClass("perc-item-disabled")
                .off("click")
                .on("click", addUsers);
        }

        function enableRemoveUsers(){
            $(".perc-roles-removeusers-button")
                .removeClass("perc-item-disabled")
                .off("click")
                .on("click", removeUsers);
        }

        function disableRemoveUsers(){
            $(".perc-roles-removeusers-button")
                .addClass("perc-item-disabled")
                .off();
        }

        function save() {
            // retrieve assigned users
            var assignedUsers = new Array();
            $(".perc-roles-assigned-users-list span").each(function() {
                assignedUsers.push($(this).html());
            });

            var rolename  = $("#perc-roles-name-field").val().trim();
            var origRoleName  = $("#perc-orig-roles-name-field").val().trim();

            var description = $("#perc-roles-description-field").val();
            var homepage = $("#perc-roles-homepage-field").val();
            controller.save(rolename, origRoleName, description, homepage, assignedUsers);
        }

        function alertDialog(title, message, w) {
            if(w == null || w == undefined || w == "" || w < 1)
                w = 400;
            $.perc_utils.alert_dialog({
                title: title,
                content: message,
                width: w,
                okCallBack: function()
                {
                    controller.updateListOfRoles(function()
                    {
                        $("#perc-roles-list [data-id='perc-item-id-0']").trigger("click");
                    });
                }
            });
        }

        function getDescription(){
            return $("#perc-roles-description-field").val();
        }

    };
})(jQuery);
