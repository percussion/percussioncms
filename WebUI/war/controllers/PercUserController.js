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
 * PercUserController.js
 * @author Jose Annunziato
 * 
 */
(function($) {

    // interface
    $.PercUserController = {    
        init                : init,
        editSelectedUser    : editSelectedUser,
        addNewUser          : addNewUser,
        findDirectoryUsers  : findDirectoryUsers,
        importDirectoryUsers: importDirectoryUsers,
        selectUser          : selectUser,
        deleteUser          : deleteUser,
        getSelectedUser     : getSelectedUser,
        getCurrentUser      : getCurrentUser,
        setUserChanged      : setUserChanged,
        save                : save,
        cacheAllRoles       : cacheAllRoles,     
        cancel              : cancel,
        isAddingNewUser     : isAddingNewUser
    };

    var dirtyController = $.PercDirtyController;
    var userService     = $.PercUserService;

    // default values
    var defaultDirectoryQuery = "";
    var defaultIndex = 0;
    var defaultCount = 500;

    // state constants
    var STATE_START = 0;
    var STATE_VIEWING_CURRENT_USER = 1;
    var STATE_EDITING_CURRENT_USER = 2;
    var STATE_ADDING_NEW_USER = 3;
    var STATE_IMPORTING_USERS = 4;

    // local variables
    var selectedUserIndex = 0;
    var rolesArrayCache = [];
    var usersArrayCache = [];
    var selectedUser = null;
    var currentUser = null;
    var currentState = STATE_START;
    var view;
    var userChanged = false;

    /**
     * When the view is first instantiated, it gets a refrence to this controller
     * and passes a reference of itself to this controller.
     * The controller can then issue commands to update the view.
     */
    function init(vew) {
        
        // the view this controller is controlling
        view = vew;
        
        // put the view in an initial state
        view.init();
        
        // load all the roles and store it in a cache
        cacheAllRoles();
        
        // load the current user
        cacheCurrentUser(function(){
            // load all the users and pass them to the view to render
            updateListOfUsers(function() {
                // select first user
                selectUser(usersArrayCache[selectedUserIndex]);
            });
        });

        // check user service status
        userService.getDirectoryStatus(function(status, userServiceStatus) {
            
            var errorMessage = null;
            
            // if we cant even get the status show a generic error message
            if(status == userService.STATUS_ERROR)
                errorMessage = I18N.message("perc.ui.user.controller@Unable to retrieve Directory Service status");
            
            // if we do get a status, if it's not ENABLED then show a more specific error message.
            // Response from service:
            // {"DirectoryServiceStatus":{"status":"ENABLED"}}
            else if(userServiceStatus.DirectoryServiceStatus.status != "ENABLED")
                errorMessage = I18N.message("perc.ui.user.controller@Directory Service Unavailable");
            
            if(errorMessage != null) {
                view.disableUserImport();
                return;
            }
        });
    }

    // load the user currently logged in from the server
    // and cache it locally
    function cacheCurrentUser(callback) {
    	currentUser = $.PercNavigationManager.getUserName();
        callback()
    }

    /**
     * Clear the list of users, repopulate it, and bind all the events
     */
    function updateListOfUsers(callback) {
        
        // Use the service to get the list of users
        userService.getUsers(function(status, usersJson) {
            if(status == userService.STATUS_ERROR) {
                view.alertDialog(I18N.message("perc.ui.user.controller@Error while loading users"), usersJson);
                return;
            }

            var users = $.perc_utils.convertCXFArray(usersJson.UserList.users);
            usersArrayCache = users;
            view.updateListOfUsers(usersArrayCache);
            
            if(callback)
                callback();
        })    
    }
    
    /**
     * Handles user selection events
     */
    function selectUser(username) {
        state = STATE_VIEWING_CURRENT_USER;
        for(u in usersArrayCache)
           if(username == usersArrayCache[u])
               selectedUserIndex = u;
        selectedUser = username;
        updateUserDetails(username);
    }
    
    /**
     * Handles user deletion events
     */
    function deleteUser(username) {      
        userService.deleteUser(username, function(status, response) {
            if(status == userService.STATUS_ERROR) {
                view.alertDialog(I18N.message("perc.ui.user.controller@Error deleting available roles") + ' ' + username, response);
                return;
            }
            
            for(u in usersArrayCache)
               if(username == usersArrayCache[u])
                   selectedUserIndex = u;
                   
            // after deleting a user, select the user at the same index or last
            updateListOfUsers(function(){
                if(selectedUserIndex > usersArrayCache.length - 1)
                   selectedUserIndex--;
                selectUser(usersArrayCache[selectedUserIndex]);
            });
        });
    }

    function editSelectedUser() {
        if(selectedUser == null || state == STATE_ADDING_NEW_USER) {
            view.alertDialog(I18N.message("perc.ui.user.controller@Select a user to edit"), I18N.message("perc.ui.user.controller@Select a user to edit"));
            return;
        }
        state = STATE_EDITING_CURRENT_USER;
        view.showSelectedUserEditor();
    }
    
    function addNewUser() {
        state = STATE_ADDING_NEW_USER;
        view.showNewUserEditor();
        view.updateAvailableRoles(rolesArrayCache,[]);
        view.updateAssignedRoles([]);
    }

    /**
     * Retrieves Directory Service users from service and passes users to dialog if no errors or error dialog with errors
     * 
     * @param usernameStartsWith {string} part or whole of a username to match on.
     * Can be empty string in which case service returns all users (with some max)
     */
    function findDirectoryUsers(usernameStartsWith) {
        
        // pass query string to service
        $.PercBlockUI();
        userService.findDirectoryUsers(usernameStartsWith, function(status, data) {
            $.unblockUI();
            if(status == $.PercServiceUtils.STATUS_SUCCESS) {

                var externalUsers = data.ExternalUser;
                var filteredExternalUsers = [];

                // filter out users that are already in CM1
                for(u=0; u<externalUsers.length; u++) {
                    
                    var keep = true;
                    for(c=0; c<usersArrayCache.length; c++)
                        if(String(externalUsers[u].name).toLowerCase() == String(usersArrayCache[c]).toLowerCase()) {
                            keep = false;
                            break;
                        }
                    if(keep)
                        filteredExternalUsers.push(externalUsers[u]);
                }
                
                
                // if no errors, data contains array of users in ExternalUsers
                view.updateImportUsersDialog(filteredExternalUsers);
            } else
            
                // if there's an error data contains the error message
                view.alertDialog(I18N.message("perc.ui.user.controller@Error"), data);
        });
    }
    
    /**
     * Uses user service to import list of usernames
     * 
     * @param usernames {array} of user names to import 
     */
    function importDirectoryUsers(usernames) {

        if(usernames == null || usernames.length == 0)
           return;
        
        // build JSON object to post to service
        var importUsersObj = {"ImportUsers":{"externalUsers":[]}};
        for(u=0; u<usernames.length; u++) {
               var userObj = {"name":usernames[u]};
               importUsersObj.ImportUsers.externalUsers.push(userObj);
        }

        // pass selected users JSON obj to service
        $.PercBlockUI();
        userService.importDirectoryUsers(importUsersObj, function(status, data) {
            $.unblockUI();
            if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                // if no errors, data contains array of imported users in ImportedUser
                // Note that ImportedUser object contains a list of users that have and have not been imported
                // and the reason why they were not imported following this pattern:
                // {"ImportedUser":[{"name":"a","status":"SUCCESS"},{"name":"b","status":"DUPLICATE"},
                //                  {"name":"c","status":"ERROR"}]}
                
                // if there were any users that were not imported, show a warning 
                var importedUsers = data.ImportedUser;
                var showImportWarningFlag = false;
                for(u=0; u<importedUsers.length; u++)
                {
                    if (importedUsers[u].status == "ERROR")
                    {
                        showImportWarningFlag = false;
                        view.showImportError();
                        break;
                    }
                    
                    if (importedUsers[u].status == "DUPLICATE")
                    {
                       showImportWarningFlag = true;
                    }
                }
                if (showImportWarningFlag)
                    view.showImportWarning(importedUsers);

                // and refresh the list of users and select the previously selected user
                updateListOfUsers(function() {
                    if(selectedUser != null)
                        selectUser(selectedUser);
                });
            } else
                // if there's an error data contains the error message
                view.alertDialog(I18N.message("perc.ui.user.controller@Error"), data);
        });
    }
    
    /**
     * Renders the user details on the right hand side
     */
    function updateUserDetails(username) {
        userService.findUser(username, function(status, userJson) {
            if(status == userService.STATUS_ERROR) {
                view.alertDialog(I18N.message("perc.ui.user.controller@Error retrieving user") + ' ' + username, usersJson);
                return;
            }
            var assignedRolesArray = $.perc_utils.convertCXFArray(userJson.User.roles);
            assignedRolesArray.sort();
            view.updateUserNameField(username);
            view.updateEmail(userJson.User.email);           
            view.updateAssignedRoles(assignedRolesArray);
            view.updateAvailableRoles(rolesArrayCache, assignedRolesArray);
            view.selectUser(username, userJson.User.providerType);
        });
    }

    function getSelectedUser() {
        return selectedUser;
    }
    
    function getCurrentUser() {
        return currentUser;
    }
    
    function setUserChanged(changed) {
        userChanged = changed;
    }

    /**
     * This save method is used for two purposes.
     * For saving changes to an existing user and
     * for saving a new user.
     * When saving an existing user, the username passed in is ignored
     * instead, the current user is used.
     * The username passed in is only used when creating a new user.
     * In either case, the passwords must match and
     * the user must have a least one role.
     */
    function save(username, password1, password2, roles, email) {
        if(dirtyController.isDirty()){
	        if($.perc_utils.isBlankString(username)) {
	            view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), I18N.message( "perc.ui.user.controller@User name cannot be blank" ));
	            return;
	        }
	      
            var usernamePattern = /^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){2,18}[a-zA-Z0-9]$/;
	        if(username!="" && !usernamePattern.test(username)){
                view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), I18N.message( "perc.ui.user.controller@User name invalid pattern" ));
                return;
            }
            if(password1!=="" && password1.length < 6) {
	            view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), I18N.message( "perc.ui.user.controller@Password requirements" ));
	            return;
	        }
	        
	        if(username == "PercussionAdmin") {
	            view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), I18N.message( "perc.ui.user.controller@User name restricted" ));
	            return;
	        }
	        
	        if(password1 != password2) {
	            view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), I18N.message( "perc.ui.user.controller@Passwords must be the same" ));
	            return;
	        }
	        var emailPattern =  /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i;
	        if(email != "" && !emailPattern.test(email)) {
	            view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), I18N.message( "perc.ui.user.controller@Invalid Email address format" ));
	            return;
	        }
	        if(roles == null || roles == "undefined" || roles.length == 0) {
	            view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), I18N.message( "perc.ui.user.controller@User must be in at least one role" ));
	            return;
	        }
	        
	        // if they left the dummy password in, they dont really intend to change the password
	        // so we just pass a blank password to the server which means: do not change password
	        if(password1 == "*******") {
	            password1 = password2 = "";
	        }
	
            if(state != STATE_ADDING_NEW_USER) {
	            
	            var updatedUserObj = {"User":{"name":username,"password":password1,"roles":roles,"email":email}};
	            userService.updateUser(updatedUserObj, function(status, createdUserObj) {
	                
	                if(status != $.PercServiceUtils.STATUS_SUCCESS) {
	                    view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), createdUserObj);
	                    return;
	                }
	                dirtyController.setDirty(false);
	                view.init();
	                // load all the users and pass them to the view to render
	                updateListOfUsers(function() {
	                    selectUser(username);
	                });
	            });
	            
            } else {
	            
	            var newUserObj = {"User":{"name":username,"password":password1,"roles":roles,"email":email}};
	            userService.createUser(newUserObj, function(status, createdUserObj) {
	                
	                if(status != $.PercServiceUtils.STATUS_SUCCESS) {
	                    view.alertDialog(I18N.message( "perc.ui.user.controller@Error" ), createdUserObj);
	                    return;
	                }
	                dirtyController.setDirty(false);
	                view.init();
	                // load all the users and pass them to the view to render
	                updateListOfUsers(function() {
	                    selectUser(username);
	                });
	            });
	            
	        }
    	}
        return;
    }
    
    function cancel() {
        dirtyController.setDirty(false);
        view.init();
        // load all the users and pass them to the view to render
        updateListOfUsers(function() {
            if(selectedUser != null)
                selectUser(selectedUser);
        });
    }

    function getUserIndex(username) {
        for(u in usersArrayCache)
           if(username == usersArrayCache[u]) {
               selectedUserIndex = u;
               return u;
           }
    }

    // load all the roles and cache them locally so we dont need to get them over and over
    function cacheAllRoles() {
        userService.getRoles(function(status, rolesJson) {
            if(status == $.PercServiceUtils.STATUS_ERROR) {
                view.alertDialog(I18N.message( "perc.ui.user.controller@Error loading available roles" ), rolesJson);
                return;
            }
            rolesArrayCache = rolesJson.RoleList.roles;
            rolesArrayCache.sort();
        });
    }
    
    /**
     * Returns true if a new user is being added.
     */
    function isAddingNewUser() {
        return state == STATE_ADDING_NEW_USER;
    }

})(jQuery);
