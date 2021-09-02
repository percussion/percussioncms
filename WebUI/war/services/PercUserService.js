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

(function($){

    /**
     * Constant for no permission
     */
    var ACCESS_NONE = "NONE";

    /**
     * Constant for read permission
     */
    var ACCESS_READ = "READER";

    /**
     * Constant for write permission
     */
    var ACCESS_WRITE = "ASSIGNEE";

    /**
     * Constant for admin permission
     */
    var ACCESS_ADMIN = "ADMIN";

    $.PercUserService =  {
        ACCESS_NONE         : ACCESS_NONE,
        ACCESS_READ         : ACCESS_READ,
        ACCESS_WRITE        : ACCESS_WRITE,
        ACCESS_ADMIN        : ACCESS_ADMIN,
        getUsers            : getUsers,
        findUser            : findUser,
        findDirectoryUsers  : findDirectoryUsers,
        importDirectoryUsers: importDirectoryUsers,
        getDirectoryStatus  : getDirectoryStatus,
        getRoles            : getRoles,
        createUser          : createUser,
        deleteUser          : deleteUser,
        updateUser          : updateUser,
        findRole            : findRole,
        deleteRole          : deleteRole,
        updateRole          : updateRole,
        createRole          : createRole,
        getAvailableUsers   : getAvailableUsers,
        getAccessLevel      : getAccessLevel,
        validateDeleteRole  : validateDeleteRole,
        validateDeleteUsers : validateDeleteUsers,
        changePassword      : changePassword
    };

    /**
     * Get list of usernames
     * @param callback function to be called when list of users is retrieved.
     * response status and list of users is passed back to the callback.
     *
     * Response user list JSON:
     *
     * {"UserList":{"users":["Admin","Contributor"]}}
     *
     */
    function getUsers(callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_USERS,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    //Convert all users values to string.
                    result.data.UserList.users = convertToString(result.data.UserList.users);
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }

    /**
     * Find a user by username
     * @param username username of user to be found.
     * @param callback function to be called when user is retrieved.
     * response status and userObj is passed back to the callback.
     *
     * Response userObj JSON:
     *
     * {"User":{"name":"","roles":["Admin","Contributor"]}}
     *
     */
    function findUser(username, callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_FIND + "/" + username,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    result.data.User.name = result.data.User.name.toString();//CXF return a numeric value if the name contains just numbers
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }

    /**
     * Change password of current user only
     * @param userObj user object which contains username and new password.
     * @param callback function to be called when user is retrieved.
     * response status and userObj is passed back to the callback.
     *
     * Response userObj JSON:
     *
     * {"User":{"name":""}}
     *
     */
    function changePassword(userObj, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.CHANGEPW,
            $.PercServiceUtils.TYPE_PUT,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            userObj
        );
    }

    /**
     * Find directory users by usernameStartsWith
     *
     * @param usernameStartsWith {string} can be empty string
     * @param callback {function} function to be called when user is retrieved.
     * Response status and usersObj is passed back to the callback.
     *
     * Response usersObj JSON:
     *
     * {"ExternalUsers" : [ {"name":"Alice"}, {"name":"Bob"}, {"name":"Charlie"} ] }
     *
     */
    function findDirectoryUsers(usernameStartsWith, callback){

        if(usernameStartsWith === null) {
            callback($.PercServiceUtils.STATUS_ERROR, I18N.message("perc.ui.user.service@Null String"));
            return;
        }
        usernameStartsWith = usernameStartsWith.replace(/%+$/, "");
        var urlfindExternalUsernamesThatStartwith = $.perc_paths.USER_EXTERNAL_FIND + "/" + encodeURIComponent(usernameStartsWith) + "*";

        $.PercServiceUtils.makeJsonRequest(
            urlfindExternalUsernamesThatStartwith,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {

                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);

                } else {

                    try {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                    } catch (err) {
                        callback($.PercServiceUtils.STATUS_ERROR, I18N.message("perc.ui.user.service@Unable To Retrieve Users"));
                    }
                }
            }
        );
    }

    /**
     * Imports list of users.
     * @param usersJSON {object} containing list of users to be imported with the following data structure
     *
     * {"ImportUsers":{"externalUsers":[{"name":"a"},{"name":"b"},{"name":"c"}]}}
     *
     * @param callback {function} the callback function to be called when the request completes. Passes back the list of users that
     * have and have not been imported and the reason why they were not imported. The data structure of response
     *
     * {"ImportedUser":[{"name":"a","status":"SUCCESS"},{"name":"b","status":"DUPLICATE"},{"name":"c","status":"ERROR"}]}
     *
     */
    function importDirectoryUsers(usersJSON, callback){

        if(usersJSON === null || usersJSON === undefined || usersJSON.ImportUsers.externalUsers.length === 0) {
            callback($.PercServiceUtils.STATUS_ERROR, I18N.message("perc.ui.user.service@Null or Empty List of Users"));
            return;
        }

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_EXTERNAL_IMPORT,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {

                if(status === $.PercServiceUtils.STATUS_SUCCESS) {

                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);

                } else {

                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);

                }
            },
            usersJSON);
    }


    /**
     * Get the status of the directory service
     *
     * @param callback {function} to be called when user is retrieved.
     * response status and usersObj is passed back to the callback.
     *
     * Response
     *
     * {"DirectoryServiceStatus":{"status":"ENABLED"}}
     *
     */
    function getDirectoryStatus(callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_EXTERNAL_STATUS,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {

                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);

                } else {

                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);

                }
            }
        );
    }

    /**
     * Get list of roles
     * @param callback function to be called when list of roles is retrieved.
     * response status and list of roles is passed back to the callback.
     *
     * Response looks as follows:
     *
     * {"RoleList":{"roles":["Admin","Contributor","Editor"]}}
     *
     */
    function getRoles(callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_ROLES,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    //Convert all roles values to string.
                    result.data.RoleList.roles = convertToString(result.data.RoleList.roles);
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }


    /**
     * Create a new user. This is a JSON only call and returns json in
     * the callback.
     * @param userObj the user object to be created. Cannot be <code>null</code>.
     * contains username, password and list of roles. Password can be left blank.
     * @param callback the callback function to be called when the request completes.
     *
     * Response userObj JSON:
     *
     * {"User":{"name":"username", "password":"p@$$w0rd", "roles":["Admin","Contributor"]}}
     *
     */
    function createUser(userObj, callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_CREATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {

                if(status === $.PercServiceUtils.STATUS_SUCCESS) {

                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);

                } else {

                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            userObj);
    }


    /**
     * Delete a user by username
     * @param username username of user to be found.
     * @param callback function to be called when user is retrieved.
     * response status and userObj is passed back to the callback.
     */
    function deleteUser(username, callback) {
        $.PercServiceUtils.makeDeleteRequest(
            $.perc_paths.USER_DELETE + "/" + username,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {

                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);

                } else {

                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }

    /**
     * Update An Existing User. This is a JSON only call and returns json in
     * the callback.
     * @param userObj the user object to be updated. Cannot be <code>null</code>.
     * contains new password and new list of roles. If password is null or blank, password stays unchanged
     * if list of roles is empty, user will be removed from all roles.
     * If password is empty, password will not be changed on the server.
     * Username will be ignored by the server.
     * @param callback the callback function to be called when the request completes.
     *
     * Response userObj JSON:
     *
     * {"User":{"name":"username", "password":"p@$$w0rd", "roles":["Admin","Contributor"]}}
     *
     */
    function updateUser(userObj, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_UPDATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {

                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);

                } else {

                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);

                }
            },
            userObj
        );
    }

    /*ROLE SECTION*/
    /**
     * Find a role using rolename
     * @param rolename name of role to be found.
     * @param callback function to be called when user is retrieved.
     * response status and roleObj is passed back to the callback.
     *
     * Response roleObj JSON:
     *
     * {"Role":{"name":"","description":"","users":["Admin","Admin2"]}}
     *
     */
    function findRole(rolename, callback){
        var strObj = {"psstring":{"value":rolename}};
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ROLE_FIND,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    result.data.Role.name = result.data.Role.name.toString(); //CXF return a numeric value if the name contains just numbers
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            strObj
        );
    }

    /**
     * Delete a role by rolename
     * @param rolename name of role to be delete.
     * @param callback function to be called when user is retrieved.
     * response status and roleObj is passed back to the callback.
     */
    function deleteRole(rolename, callback){
        var strObj = {"psstring":{"value":rolename}};
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ROLE_DELETE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            strObj
        );
    }

    /**
     * Create a new role. This is a JSON only call and returns json in
     * the callback.
     * @param roleObj the role object to be created. Cannot be <code>null</code>.
     * contains rolename, description and list of users.
     * @param callback the callback function to be called when the request completes.
     *
     * Response roleObj JSON:
     *
     * {"Role":{"name":"rolename","description":"Description text", "users":["user1","user2"]}}
     *
     */
    function createRole(roleObj, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ROLE_CREATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            roleObj
        );
    }

    /**
     * Update An Existing Role. This is a JSON only call and returns json in
     * the callback.
     * @param roleObj the role object to be updated. Cannot be <code>null</code>.
     * contains new description and new list of users.
     * if list of users is empty, role will be removed from all users.
     * Rolename will be ignored by the server.
     * @param callback the callback function to be called when the request completes.
     *
     * Response roleObj JSON:
     *
     * {"Role":{"name":"rolename", "description":"new Description Text", "users":["user1","userNew"]}}
     *
     */
    function updateRole(roleObj, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ROLE_UPDATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            roleObj
        );
    }

    /**
     * Get the available users by an existing Role. This is a JSON only call and returns json in
     * the callback.
     * @param roleObj the role object. Cannot be <code>null</code>.
     * contains the role name. {"Role":{"name":"RoleName"}}
     * @param callback the callback function to be called when the request completes.
     *
     * Response roleObj JSON:
     *
     * {"Role":{"name":"rolename", "users":["user1","userNew"]}}
     *
     */
    function getAvailableUsers(roleObj, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ROLE_AVAILABLE_USERS,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    //Convert all users values to string.
                    result.data.UserList.users = convertToString(result.data.UserList.users);
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            roleObj
        );
    }

    /**
     * Finds the access level for the current user. This returns json in the callback.
     * @param type the default workflow of the specified content type will be used.
     * @param itemId the id of the item selected.
     * @param parentFolderPath the path of the folder to check the workflow id if needed.
     * @param sync Optional param to make the ajax call synchronous, false by default
     * @callback (Function), callback function with status and result. if status is $.PercServiceUtils.STATUS_SUCCESS the
     *           result would be one of ACCESS_XXX value. if the status is $.PercServiceUtils.STATUS_ERROR then result
     *           would be the error message.
     */
    function getAccessLevel(type, itemId, callback, parentFolderPath){
        var reqObj;
        if (type != null)
        {
            reqObj = {"AccessLevelRequest":{"type":type, "itemId":itemId, "parentFolderPath": parentFolderPath}};
        }
        else
        {
            reqObj = {"AccessLevelRequest":{"itemId":itemId, "parentFolderPath": parentFolderPath}};
        }

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.USER_ACCESS_LEVEL,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data.AccessLevel.accessLevel);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            reqObj
        );
    }

    /**
     * Validate a Role before delete it. This is a JSON only call and returns json in
     * the callback.
     * @param roleObj the role object to be validate. Cannot be <code>null</code>.
     * contains the role name and the assigned users list.
     * @param callback the callback function to be called when the request completes.
     *
     * roleObj:
     *
     * {"Role":{"name":"rolename", "users":["user1","user2"]}}
     *
     */
    function validateDeleteRole(roleObj, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ROLE_DELETE_VALIDATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            roleObj
        );
    }

    /**
     * Validate the user list to be removed from a Role. This is a JSON only call and returns json in
     * the callback.
     * @param UserList Users List to be validated. Cannot be <code>null</code>.
     * @param callback the callback function to be called when the request completes.
     *
     * UserList:
     *
     * {"UserList":{"users":["Admin","Contributor"]}}
     *
     */
    function validateDeleteUsers(userList, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ROLE_REMOVE_USERS_VALIDATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            userList
        );
    }

    /**
     * Convert all array values to string.
     * CXF return a mixed array (numeric and string values if some of the values contains just numbers).
     */
    function convertToString(itemList){
        itemList = $.perc_utils.convertCXFArray(itemList);
        $.each(itemList, function(k,v){
            itemList[k] = v.toString();
        });
        return itemList;
    }

})(jQuery);
