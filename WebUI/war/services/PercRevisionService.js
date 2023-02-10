/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Service to handle the item revision related actions.
 */

(function($)
{
    //Public API
    $.PercRevisionService = 
    {
            getRevisionDetails : getRevisionDetails,
            restoreRevision : restoreRevision,
            getLastComment : getLastComment
    };
    
    /**
     * Makes a call to the server and calls the supplied callback with status and result. See $.PercServiceUtils.makeJsonRequest
     * for more details.
     */
    function getRevisionDetails(itemId, callback)
    {
        var url = $.perc_paths.ITEM_REVISIONS + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
        /* Test Data****
        var result = [{revId:1,lastModifiedDate:"Jul 21, 2010 1:33:13 PM",lastModifier:"Admin", status:"Live"},
                      {revId:2,lastModifiedDate:"Jul 22, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:3,lastModifiedDate:"Jul 24, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:4,lastModifiedDate:"Jul 22, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:5,lastModifiedDate:"Jul 24, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:6,lastModifiedDate:"Jul 22, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:7,lastModifiedDate:"Jul 24, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:8,lastModifiedDate:"Jul 22, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:9,lastModifiedDate:"Jul 24, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:10,lastModifiedDate:"Jul 22, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:11,lastModifiedDate:"Jul 24, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:12,lastModifiedDate:"Jul 22, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:13,lastModifiedDate:"Jul 24, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:14,lastModifiedDate:"Jul 22, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:15,lastModifiedDate:"Jul 24, 2010 1:33:13 PM",lastModifier:"Editor", status:"Pending"},
                      {revId:16,lastModifiedDate:"Jul 25, 2010 1:33:13 PM",lastModifier:"Admin", status:"Live"}];
        callback($.PercServiceUtils.STATUS_SUCCESS,result);
        */
    }
    
    /**
     * Makes a json request to restore a revision and calls the supplied callback with the results.
     * @param itemId, the guid representation of the id of page or asset. 16777215-101-709
     * @param revId, the id of the revision that needs to be restored.
     * @param callback the callback function that gets called from PercServiceUtils#makeJsonRequest,
     * Please see that method for the description of the arguments with which the function is called.
     */
    function restoreRevision(itemId, revId, callback)
    {
        //Replace the revision in item id
        var ida = itemId.split("-");
        ida[0] = revId;
        itemId = ida.join("-");
        var url = $.perc_paths.ITEM_PROMOTE_REVISION + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }
    
    function getLastComment(itemId, callback){
        var url = $.perc_paths.ITEM_LAST_COMMENT + "/" + itemId;
        $.PercServiceUtils.makeRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }
})(jQuery);
    
