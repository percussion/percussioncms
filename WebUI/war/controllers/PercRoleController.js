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
 * PercRoleController.js
 * @author Luis Mendez
 *
 */
(function($) {

    // interface
    $.PercRoleController = {
        init                   : init,
        editSelectedRole       : editSelectedRole,
        addNewRole             : addNewRole,
        selectRole             : selectRole,
        validateAndDeleteRole  : validateAndDeleteRole,
        getSelectedRole        : getSelectedRole,
        save                   : save,
        getAvailableUsers      : getAvailableUsers,
        cancel                 : cancel,
        validateAndRemoveUsers : validateAndRemoveUsers,
        getCurrentUser         : getCurrentUser,
        updateListOfRoles      : updateListOfRoles

    };

    var dirtyController = $.PercDirtyController;
    var roleService     = $.PercRoleService;
    var userService     = $.PercUserService;

    // state constants
    var STATE_START = 0;
    var STATE_VIEWING_CURRENT_ROLE = 1;
    var STATE_EDITING_CURRENT_ROLE = 2;
    var STATE_ADDING_NEW_ROLE = 3;

    // local variables
    var selectedRoleIndex = 0;
    var rolesArrayCache = [];
    var currentUser = null;
    var selectedRole = null;
    var currentAssignedUsers = [];
    var currentState = STATE_START;
    var view;
    // Build an absolute URL as a workaround for IE issue (popup security warning)
    // See details at http://support.microsoft.com/kb/925014/en-us?fr=1
    var baseUrl = window.location.protocol + "//" + window.location.host + "/cm";

    /**
     * When the view is first instantiated, it gets a refrence to this controller
     * and passes a reference of itself to this controller.
     * The controller can then issue commands to update the view.
     */
    function init(vew) {

        // the view this controller is controlling
        view = vew;

        // load the current user
        cacheCurrentUser();

        // put the view in an initial state
        view.init();

        // load all the roles and pass them to the view to render
        updateListOfRoles(function() {
            // select first role
            selectRole(rolesArrayCache[selectedRoleIndex]);
        });
    }

    /**
     * Clear the list of roles, repopulate it, and bind all the events
     */
    function updateListOfRoles(callback) {

        // Use the service to get the list of roles
        userService.getRoles(function(status, rolesJson) {
            if(status == $.PercServiceUtils.STATUS_ERROR) {
                view.alertDialog(I18N.message("perc.ui.role.controller@Error Loading Roles") , rolesJson);
                return;
            }
            var rolesArray = $.perc_utils.convertCXFArray(rolesJson.RoleList.roles);
            rolesArrayCache = rolesArray;
            view.updateListOfRoles(rolesArray);

            if(callback)
                callback();
        })
    }

    /**
     * Handles role selection events
     */
    function selectRole(rolename) {
        state = STATE_VIEWING_CURRENT_ROLE;
        for(u in rolesArrayCache)
           if(rolename == rolesArrayCache[u])
               selectedRoleIndex = u;
        selectedRole = rolename;
        updateRoleDetails(rolename);
    }

    /**
     * Delete a role
     * @roleName the name of the role.
     */
    function deleteRole(roleName) {
        userService.deleteRole(roleName, function(status, response) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message("perc.ui.role.controller@Error Deleting Role") + ' \''+roleName+'\'.', response);
                return;
            }
            updateListOfRoles(function() {
                if(selectedRoleIndex > rolesArrayCache.length - 1)
                    selectedRoleIndex--;
                selectRole(rolesArrayCache[selectedRoleIndex]);
            });
        });
    }

     /**
     * Handles role edit events
     */
    function editSelectedRole() {
        if(selectedRole == null || state == STATE_ADDING_NEW_ROLE) {
            view.alertDialog(I18N.message("perc.ui.role.controller@Select Role to Edit"), I18N.message("perc.ui.role.controller@Select Role to Edit"));
            return;
        }
        state = STATE_EDITING_CURRENT_ROLE;
        view.showSelectedRoleEditor();
    }

    /**
     * Handles role add events
     */
    function addNewRole() {
        state = STATE_ADDING_NEW_ROLE;
        view.showNewRoleEditor();
        view.updateAssignedUsers([]);
    }

    /**
     * Renders the role details on the right hand side
     */
    function updateRoleDetails(rolename) {
        userService.findRole(rolename, function(status, roleJson) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message("perc.ui.role.controller@Error Retrieving Role") + " " + rolename, roleJson);
                return;
            }
            var assignedUsersArray = $.perc_utils.convertCXFArray(roleJson.Role.users);
            view.updateRoleNameField(rolename);
            view.updateDescriptionField(roleJson.Role.description);
            view.updateHomepageField(roleJson.Role.homepage);
            view.updateAssignedUsers(assignedUsersArray);
            view.selectRole(rolename);
        });
    }

    function getSelectedRole() {
        return selectedRole;
    }

    /**
     * This save method is used for two purposes.
     * For saving changes to an existing role and
     * for saving a new role.
     * When saving an existing role, the rolename passed in is ignored
     * instead, the current role is used.
     * The username passed in is only used when creating a new role.
     * In either case, the passwords must match and
     * the role must have a least one user.
     */
    function save(rolename, origrolename, description, homepage, users) {

        if($.perc_utils.isBlankString(rolename)) {
            view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Role Name Cannot Be Blank"));
            return;
        }

        if(rolename.indexOf("??") != -1) {
            view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Role Contains Invalid Character Sequence"));
            return;
        }

        if(origrolename.indexOf("??") != -1) {
            view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Original Role Contains Invalid Character Sequence") + " '??'.");
            return;
        }

        if($.perc_utils.isBlankString(homepage)) {
            view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Homepage Cannot Be Blank"));
            return;
        }

        //IE don't allow maxLength in TextAreas
        if(description.length > 255) {
            view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Maximum Length Role Description"));
            return;
        }

        if(description.indexOf("??") != -1) {
            view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Role Description Contains Invalid Character Sequence") + " '??'.");
            return;
        }

        if(state == STATE_EDITING_CURRENT_ROLE || state == STATE_VIEWING_CURRENT_ROLE) {
            var roleObj = {"Role":{"name":rolename, "oldName":origrolename ,"description":description,"homepage":homepage,"users":users}};
            updateRole(roleObj);
        } else if(state == STATE_ADDING_NEW_ROLE) {
            var newRoleObj = {"Role":{"name":rolename,"description":description,"homepage":homepage,"users":users}};
            userService.createRole(newRoleObj, function(status, createdRoleObj) {
                if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                    if(createdRoleObj=="invalid_character"){
                        view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Role Contains Invalid Character Sequence"));
                    }else if(createdRoleObj.startsWith("already_exist") ){
                        var savedRoleName = "";
                        try {
                            savedRoleName =    createdRoleObj.split(":")[1];
                        }catch (e) {}
                        view.alertDialog(I18N.message("perc.ui.role.controller@Error"), I18N.message("perc.ui.role.controller@Role name already exist", [savedRoleName]));
                    }else{
                        view.alertDialog(I18N.message("perc.ui.role.controller@Error"), createdRoleObj);
                    }
                    return;
                }
                dirtyController.setDirty(false);
                // load all the users and pass them to the view to render
                updateListOfRoles(function() {
                    selectRole(rolename);
                });
            });

        }
        return;
    }

    function cancel() {
        dirtyController.setDirty(false);
        // load all the roles and pass them to the view to render
        updateListOfRoles(function() {
            if(selectedRole != null)
                selectRole(selectedRole);
        });
    }

    /**
     * Retrieve the users that will be showed in the add users dialog.
     * if the context is create Role we get all users.
     * if the context is add Roles to an existing Role, we only show the available users.
     * @assignedUsers current assigned users, used to filter with all users to not show the already assigned users in consecutives add.
    */
    function getAvailableUsers(assignedUsers) {
        currentAssignedUsers = assignedUsers;
        if(state == STATE_VIEWING_CURRENT_ROLE) {
            var roleObj = {"Role":{"name":selectedRole,"users":currentAssignedUsers}};
            //Get availables users.
            userService.getAvailableUsers(roleObj ,function(status, usersJson) {
                if(status == $.PercServiceUtils.STATUS_ERROR) {
                    view.alertDialog(I18N.message("perc.ui.role.controller@Error Loading Available Users"), usersJson);
                    return;
                }
                var userList = $.perc_utils.convertCXFArray(usersJson.UserList.users);
                //Display a warning if we don't have more users to add.
                if (userList.length > 0){
                    $.PercAddItemDialog.open(true, userList, I18N.message("perc.role.controller@Add Users to Role") , addUsers);
                }
                else{
                     view.alertDialog(I18N.message("perc.ui.role.controller@Add Users to Role"), I18N.message("perc.ui.role.controller@No Users Available") + selectedRole + I18N.message("perc.ui.role.controller@Users Already Added"));
                }
            })
        }
        else{
            //Get all users.
            userService.getUsers(function(status, usersJson) {
                if(status == $.PercServiceUtils.STATUS_ERROR) {
                    view.alertDialog(I18N.message("perc.ui.role.controller@Error Loading Available Users"), usersJson);
                    return;
                }
                var userList = $.perc_utils.convertCXFArray(usersJson.UserList.users);
                //Get the difference between current assigned users and all users.
                for(i in assignedUsers){
                    userList.splice(userList.indexOf(assignedUsers[i]), 1);
                }
                //Display a warning if we don't have more users to add.
                if (userList.length > 0){
                    $.PercAddItemDialog.open(false, userList, I18N.message("perc.ui.users@Add Users To Role") , addUsers);
                }
                else{
                     view.alertDialog(I18N.message("perc.ui.users@Add Users To Role"), I18N.message("perc.ui.role.controller@No Users Available") + selectedRole + I18N.message("perc.ui.role.controller@Users Already Added"));
                }
            })
        }
    }

    /**
     * Depending on the context (show and create), we want to save the users selected in the add users dialog to an existing Role
     * or just add the selected Roles in the add users dialog to the list that will be saved later with the create Role event.
     */
    function addUsers(userList){
        //Add the selected users to the current list of assigned users.
        var newList = currentAssignedUsers.concat(userList);
        newList.sort();
        if(state == STATE_EDITING_CURRENT_ROLE || state == STATE_ADDING_NEW_ROLE) {
            view.updateAssignedUsers(newList);
        }
        else if(state == STATE_VIEWING_CURRENT_ROLE) {
            //Save the users to the selected Role.
            var roleObj = {"Role":{"name":selectedRole,"description":view.getDescription(),"users":newList}};
            updateRole(roleObj);
        }
    }

    /**
     * Remove the users from a role
     * @userList users to be removed can't be null
     * @confirmed used to confirm the remove.
     */
    function removeUsers(userList){
        var roleObj = {"Role":{"role":selectedRole, "description":view.getDescription(), "users":userList}};
        updateRole(roleObj);
    }

    function getCurrentUser() {
        return currentUser;
    }

     /**
      * Load the user currently logged in from the server
      * and cache it locally
      */
    function cacheCurrentUser() {
    	currentUser = $.PercNavigationManager.getUserName();
    }

    /**
     * Update a Role.
     * @roleObj the role object updated. {"Role":{"name":rolename, "description":description, "users":users}}
     */
    function updateRole(roleObj){
        userService.updateRole(roleObj, function(status, roleJson) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message("perc.ui.publish.title@Error"), roleJson);
                return;
            }
            dirtyController.setDirty(false);
            // load all the roles and pass them to the view to render
            updateListOfRoles(function() {
                selectRole(roleJson.Role.name);
            });
        });
    }

    /**
     * Validate a Role before to delete.
     * @roleObj the role object to be deleted. {"Role":{"name":rolename, "users":users}}
     */
    function validateAndDeleteRole(roleObj){
        userService.validateDeleteRole(roleObj, function(status, validationMsg) {
            if (validationMsg != null || roleObj.Role.users.length > 0)
            {
                var htmlQuestion = "<p id='perc-delete-dialog-warning'>" +I18N.message("perc.ui.role.controller@Warning Title") + "</p>" +
                                   "<p id='perc-warning-red'> " + I18N.message("perc.ui.role.controller@About To Delete Role") + selectedRole + "'.</p> <br/>" +
                                   (validationMsg!=null?"<strong>" + validationMsg + "</strong><br/><br/>":"") +
                                   "<p id='perc-delete-warn-msg'>" +I18N.message("perc.ui.role.controller@Are You Sure Delete Role") + "</p>";
                var settings = {
                    id: 'perc-role-delete-confirm',
                    title: I18N.message("perc.ui.role.controller@Delete Role"),
                    question: htmlQuestion,
                    success: function() { deleteRole(roleObj.Role.name); },
                    yes: I18N.message("perc.ui.page.confirmpublish@Continue Anyway"),
                    type: "YES_NO",
                    open: function(){alert("open");},
                    width: 500
                };

                $.perc_utils.confirm_dialog(settings);
                $("#perc-role-delete-confirm")
                    .find("#perc-confirm-generic-yes")
                    .css({"background-color": "#133c55", "border-color": "#133c55","color": "#ffffff"})
                    .off('mouseenter mouseleave')
                    .on('mouseenter',
                        function() {
                            $(this).css({"background-color": "#d22f12","border-color": "#d22f12","color": "#ffffff"});
                        })
                    .on('mouseleave',
                        function() {
                            $(this).css({"background-color": "#133c55", "border-color": "#133c55","color": "#ffffff"});
                        }
                    );
            }
            else
            {
                deleteRole(roleObj.Role.name);
            }
        });
    }

    /**
     * Validate the users to be romeved from the Role.
     * @usersListToDelete List a users to be deleted.
     * @usersList List of the remaining users assigned to the Role.
     */
    function validateAndRemoveUsers(usersListToDelete, usersList){
        var userList = {"UserList":{"users":usersListToDelete}};
        var roleObj = {"Role":{"name":selectedRole, "description":view.getDescription(), "users":usersList}};
        userService.validateDeleteUsers(userList, function(status, validationMsg) {
            if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                updateRole(roleObj);
            }
            else {
                var formatedText = usersListToDelete.join(", ");
                var htmlQuestion = "<p id='perc-delete-dialog-warning'>Warning</p><br/>" +
                                   "<strong>" + validationMsg + "</strong><br/><br/>" +
                                   "<p id='perc-delete-warn-msg'>" + I18N.message("perc.ui.role.controller@Are You Sure Remove Users From Role") + selectedRole + "'?</p>";
                var settings = {
                    id: 'perc-roles-removeusers-confirm',
                    title: I18N.message("perc.ui.role.controller@Remove Users From Role"),
                    question: htmlQuestion,
                    success: function() { updateRole(roleObj);},
                    yes: I18N.message("perc.ui.page.confirmpublish@Continue Anyway"),
                    type: "YES_NO",
                    width: 500
                };
                $.perc_utils.confirm_dialog(settings);
                $("#perc-roles-removeusers-confirm")
                .find("#perc-confirm-generic-yes")
                .css({"background-color": "#133c55", "border-color": "#133c55","color": "#ffffff"})
                .off('mouseenter mouseleave')
                .on('mouseenter',
                    function() {
                        $(this).css({"background-color": "#d22f12","border-color": "#d22f12","color": "#ffffff"});
                    })
                    .on('mouseleave',   function() {
                        $(this).css({"background-color": "#133c55", "border-color": "#133c55","color": "#ffffff"});
                    }
                );
            }
        });

    }

})(jQuery);
