
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
 * [PercMetadataService.js]
 * A general metadata storage service.
 */
(function($){

   $.PercMetadataService = {
      find: find,
      findByPrefix: findByPrefix,
      deleteEntry: deleteEntry,
      deleteEntryByPrefix: deleteEntryByPrefix,
      save: save,
      saveGlobalVariables: saveGlobalVariables
   };
   
   /**
    * Finds a metadata object from the repository based on the specified key.
    * @param key {string}the unique key used to retrieve the metadata object. Cannot
    * be <code>null</code> or empty.
    * @param callback {function} the callback function that will be called upon find
    * completion. The callback will be passed the following args:
    * <pre>
    *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
    *   result = if succes then the following meta data object will be returned:
    *       {metadata: {
    *          key: "somekey",
    *          data: "somedata"
    *       }}
    *   if error then result will contain the error message
    * </pre>
    * @return may be <code>null</code> if no matching entry was found in the
    * database.
    * @type object
    */
   function find(key, callback){
      var self = this;
      $.PercServiceUtils.makeJsonRequest(
         $.perc_paths.METADATA_FIND + "/" + key,
         $.PercServiceUtils.TYPE_GET,
         false,
         function(status, result){
            if(status == $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
             {
                var defaultMsg = 
                   $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
             }
          });
   }
   
   /**
    * Locate all metadata objects by a key prefix. So to retrieve all of the objects with the
    * following keys: 'user.profile.john', 'user.profile.dave', use the prefix 'user.profile.'.
    * @param prefix {string} the key prefix, cannot be <code>null</code> or empty.
    * @param callback {function} the callback function that will be called upon find
    * completion. The callback will be passed the following args:
    * <pre>
    *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
    *   result = if succes then the following meta data object will be returned:
    *       {metadata: [{
    *          key: "somekey",
    *          data: "somedata"
    *       }]}
    *   if error then result will contain the error message
    * </pre>
    * @return an array containing all of the located metadata objects, will
    * be empty if none were found. Never <code>null</code>.
    * @type array
    */
   function findByPrefix(prefix, callback){
      var self = this;
      $.PercServiceUtils.makeJsonRequest(
         $.perc_paths.METADATA_FIND_BY_PREFIX + "/" + prefix,
         $.PercServiceUtils.TYPE_GET,
         false,
         function(status, result){
            if(status == $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
             {
                var defaultMsg = 
                   $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
             }
          });
   }
   
   /**
    * Deletes the metadata object specified by the passed in key if it exists.
    * @param key {string} the unique key used to delete the metadata object. Cannot
    * be <code>null</code> or empty. 
    * @param callback {function} the callback function that will be called upon delete
    * completion. The callback will be passed the following args:
    * <pre>
    *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
    *   errorMsg = the error message, only present if the status is error
    * </pre>
    */
   function deleteEntry(key, callback){
      var self = this;
      $.PercServiceUtils.makeJsonRequest(
         $.perc_paths.METADATA_DELETE + "/" + key,
         $.PercServiceUtils.TYPE_DELETE,
         false,
         function(status, result){
            if(status == $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
             {
                var defaultMsg = 
                   $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
             }
          });
   }
   
   /**
    * Deletes all metadata objects who's keys start with the specified prefix.
    * @param prefix {string} the key prefix, cannot be <code>null</code> or empty.
    * @param callback {function} the callback function that will be called upon delete
    * completion. The callback will be passed the following args:
    * <pre>
    *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
    *   errorMsg = the error message, only present if the status is error
    * </pre>
    */
   function deleteEntryByPrefix(prefix, callback){
      var self = this;
      $.PercServiceUtils.makeJsonRequest(
         $.perc_paths.METADATA_DELETE_BY_PREFIX + "/" + prefix,
         $.PercServiceUtils.TYPE_DELETE,
         false,
         function(status, result){
            if(status == $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
             {
                var defaultMsg = 
                   $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
             }
          });
   }
   
   /**
    * Saves the passed in metadata object to the repository, replacing any existing
    * entry that uses the same key or creating a new entry if one does not yet exist.
    * @param data the metadata object to be saved, cannot be <code>null</code>. This can be
    * a string or a javascript object. If it is a javascript object, it will be turned into
    * a json string before it is sent to the metadata service.
    * @param callback {function} the callback function that will be called upon save
    * completion. The callback will be passed the following args:
    * <pre>
    *   status = either $.PercServiceUtils.STATUS_SUCCESS or $.PercServiceUtils.STATUS_ERROR
    *   errorMsg = the error message, only present if the status is error
    * </pre>
    */
   function save(key, data, callback){
      var self = this;
      var rData = typeof(data) == 'object' ? JSON.stringify(data) : data;
      var metadataObj = {
         "metadata": {
            "key": key,
            "data": rData
         }
      };
      $.PercServiceUtils.makeJsonRequest(
         $.perc_paths.METADATA_SAVE + "/",
         $.PercServiceUtils.TYPE_POST,
         false,
         function(status, result){
            if(status == $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = 
                   $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
         },
         metadataObj);
   }
   
   /**
   * Function to save the global variables.
   */
   function saveGlobalVariables(key, data, callback){
      var self = this;
      var rData = typeof(data) == 'object' ? JSON.stringify(data) : data;
      var metadataObj = {
         "metadata": {
            "key": key,
            "data": rData
         }
      };
      $.PercServiceUtils.makeJsonRequest(
         $.perc_paths.METADATA_SAVE_GLOBAL_VARIABLES + "/",
         $.PercServiceUtils.TYPE_POST,
         false,
         function(status, result){
            if(status == $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = 
                   $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
         },
         metadataObj);
   }
   

})(jQuery);