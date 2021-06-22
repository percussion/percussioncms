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
 * [PercUtilService.js]
 * A repository for general functions
 */
(function($){

    $.PercUtilService = {
        getPrivateKeys: getPrivateKeys,
        encryptString: encryptString,
        encryptStrings: encryptStrings,
        decryptString: decryptString,
        logToServer:logToServer
    };

    function getPrivateKeys(callback, async=true){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.UTIL_GET_PRIVATE_KEYS,
            $.PercServiceUtils.TYPE_GET,
            !async,
            function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }

    /**
     * Encrypt a string
     * @param str {string} the string to be encrypt.
     * @param callback {function} the callback function that will be called upon find
     * completion. The callback will be passed the following args:
     * <pre>
     *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
     *   result = if succes then return the string encrypted
     *   if error then result will contain the error message
     * </pre>
     * @type string
     */
    function encryptString(str, callback, async=true){
        var object = {"psmap":{"entries":{"entry":{"key":"string","value":str}}}};
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.UTIL_ENCRYPT_STRING,
            $.PercServiceUtils.TYPE_POST,
            !async,
            function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data.psmap.entries.entry.value);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            object
        );
    }

    /**
     * Calls the server to encrypt the supplied array of strings.
     * @param values [string] the array of strings to be encrypted.
     * @param callback {function} the callback function that will be called upon ajax call
     * completion. The callback will be passed the following args:
     * <pre>
     *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
     *   result = if succes then return associated array of supplied string and encrypted value
     *   if error then result will contain the error message
     * </pre>
     * @type string
     */
    function encryptStrings(values, callback, async=true){
        var object = {"pslist":{"list":[]}};
        object.pslist.list = values;
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.UTIL_ENCRYPT_STRINGS,
            $.PercServiceUtils.TYPE_POST,
            !async,
            function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, convertMapToArray(result.data.psmap.entries));
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            object
        );
    }

    /**
     * Helper method that converts the psmap entires in to an associated array.
     * @param {Object} mapEntries expected to be psmap entries.
     */
    function convertMapToArray(mapEntries)
    {
        var tempArray = [];
        var result = [];

        if(mapEntries.entry ===  undefined){
            $.each(mapEntries,function( k, v ) {
                result[k] = v;
            });
        }else{
            if(!Array.isArray(mapEntries.entry))
                tempArray.push(mapEntries.entry);
            else
                tempArray = mapEntries.entry;

            $(tempArray).each(function(){
                result[this.key] = this.value;
            });
        }





        return result;
    }

    /**
     * Decrypt a string
     * @param str {string} the string to be decrypt.
     * @param callback {function} the callback function that will be called upon find
     * completion. The callback will be passed the following args:
     * <pre>
     *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
     *   result = if succes then return the string decrypted
     *   if error then result will contain the error message
     * </pre>
     * @type string
     */
    function decryptString(str, callback, async=true){
        var object = {"psmap":{"entries":{"entry":{"key":"string","value":str}}}};
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.UTIL_DECRYPT_STRING,
            $.PercServiceUtils.TYPE_POST,
            !async,
            function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data.psmap.entries.entry.value);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            object
        );
    }

    /**
     * Logs supplied mesage to client and then server.
     * @param {Object} type if debug, error otherwise set to info.
     * @param {Object} category the category name to recognize the error on server
     * @param {Object} message the message that needs to be logged must be a valid string if not no logging happens.
     */
    function logToServer(type, category, message){
        if(!message)
            return;
        var logData = {"LogData":{"type":type, "category": category, "message" : message}};
        $.PercServiceUtils.makeJsonRequest($.perc_paths.UTIL_LOG_DATA,$.PercServiceUtils.TYPE_POST,
            false,$.noop,logData,$.noop);
    }
})(jQuery);
