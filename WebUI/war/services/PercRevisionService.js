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
    
