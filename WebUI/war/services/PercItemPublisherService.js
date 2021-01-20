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

/****
 * Service calls for the Publish page/assest.
 */
(function($)
{
    $.PercItemPublisherService =
        {
            publishItem: publishItem,
            takeDownItem: takeDownItem,
            publishToStaging: publishToStaging,
            removeFromStaging:removeFromStaging,
            getPublishActions: getPublishActions,
            setScheduleDates: setScheduleDates,
            getScheduleDates: getScheduleDates,
            isDefaultServerModified: isDefaultServerModified,

            PUBLISHER_JOB_STATUS_FORBIDDEN: "FORBIDDEN",
            PUBLISHER_JOB_STATUS_BADCONFIG: "BADCONFIG",
            PUBLISHER_JOB_STATUS_NOSTAGING_SERVERS: "NOSTAGING_SERVERS",
            PUBLISHER_JOB_STATUS_BADCONFIG_MULTIPLE_SITES: "BADCONFIGMULTIPLESITES"

        };

    /**
     * Publishes an item.
     * @param itemId (string) the id of the item to be published
     * @param itemType (string) the type of item (Page/Asset)
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function publishItem(itemId, itemType, callback)
    {
        var publishUrl = itemType == 'Page' ? $.perc_paths.PAGE_PUBLISH:$.perc_paths.RESOURCE_PUBLISH;
        publishUrl+="/" + itemId;

        _executePublishAction(publishUrl, callback);
    }

    /**
     * Takes down (unpublishes) an item.
     * @param itemId (string) the id of the item to be taken down
     * @param itemType (string) the type of item (Page/Asset)
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function takeDownItem(itemId, itemType, callback)
    {

        var findLinkedItemsUrl = $.perc_paths.ITEM_LINKED_TO_ITEM + "/" + itemId;
        var takeDownUrl = itemType == 'Page' ? $.perc_paths.PAGE_TAKEDOWN : $.perc_paths.RESOURCE_TAKEDOWN;
        takeDownUrl+="/" + itemId;

        $.PercServiceUtils.makeJsonRequest(findLinkedItemsUrl, $.PercServiceUtils.TYPE_GET, false, function(status, result) {
            if (status == $.PercServiceUtils.STATUS_ERROR) {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result);
                console.error(defaultMsg);
                // if there is an error, we proceed with previous behavior (no confirm display)
                $.PercBlockUI();
                _executePublishAction(takeDownUrl, callback, result.data.ArrayList);
            }
            else {
                if (result.data != null && result.data.ArrayList != null && result.data.ArrayList.length > 0) {
                    takeDownItemConfirm(takeDownUrl, result.data, callback);
                }else {
                    //If no associated/Linked Pages, then just publish this page
                    $.PercBlockUI();
                    _executePublishAction(takeDownUrl, callback, result.data.ArrayList);
                }
            }
        }, null);
    }

    /**
     *
     * @param {*} takeDownUrl
     * @param {*} data
     * @param {*} callback
     */
    function takeDownItemConfirm(takeDownUrl, data, callback)
    {
        var title = I18N.message("perc.ui.publish.title@Remove From Site");
        var options = {
            title: title,
            question: createDialogQuestion(data),
            cancel: function()
            {
            },
            success: function()
            {
                $.PercBlockUI();
                _executePublishAction(takeDownUrl, callback, data);
            }
        };
        $.perc_utils.confirm_dialog(options);
    }

    function createDialogQuestion(data) {
        var dialog = I18N.message("perc.ui.publish.question@Remove From Site") + '<br /><br />';
        $.each(data.ArrayList, function (index, value) {
            if (index > 9) {
                return false;
            }
            dialog += value.pagePath + '<br />';
        });
        return dialog;
    }


    /**
     * Executes a request to the given publish url.
     * @param url the url used to invoke the publish action
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function _executePublishAction(url, callback)
    {
        _executePublishAction(url,callback,null);
    }

    function _executePublishAction(url, callback, dataObj)
    {
        var serviceCallback = function(status, results){
            if(status == $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        }
        if (dataObj == null || dataObj.length <= 0) {
            $.PercServiceUtils.makeJsonRequest(url, $.PercServiceUtils.TYPE_GET, true, serviceCallback);
        } else {
            $.PercServiceUtils.makeJsonRequest(url, $.PercServiceUtils.TYPE_PUT, true, serviceCallback, dataObj);
        }
    }

    /**
     * Executes a request to the given publish url.
     * @param publishUrl the url used to invoke the publish action for an item
     * @param serviceCallback (function) callback function to be invoked when ajax call returns
     */
    function getPublishActions(itemId, callback)
    {
        var publishUrl = $.perc_paths.SITE_ITEM_PUBLISH_ACTIONS + "/" + itemId;

        var serviceCallback = function(status, results){
            if(status == $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,results.data);
            }
        }
        $.PercServiceUtils.makeRequest(publishUrl, $.PercServiceUtils.TYPE_GET, true, serviceCallback);

    }

    /**
     * Executes a request to get the set Schedule dates for an item.
     * @param getUrl the url used to get the set dates for an item
     * @param serviceCallback (function) callback function to be invoked when ajax call returns
     */
    function getScheduleDates(itemId, callback)
    {
        var getUrl = $.perc_paths.ITEM_GETDATES + "/" + itemId;

        var serviceCallback = function(status, results){
            if(status == $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,results.data);
            }
        }
        $.PercServiceUtils.makeRequest(getUrl, $.PercServiceUtils.TYPE_GET, true, serviceCallback);

    }


    /**
     * Executes a request to set the Schedule dates for an item.
     * @param setUrl the url used to save the Schedule dates for an item
     * @param serviceCallback (function) callback function to be invoked when ajax call returns
     */
    function setScheduleDates(sendDates, callback)
    {
        var setUrl = $.perc_paths.ITEM_SETDATES;
        var obj = sendDates;
        var serviceCallback = function(status, results){
            if(status == $.PercServiceUtils.STATUS_ERROR)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(false,defaultMsg);
            }
            else
            {
                $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                callback(true,results.data);
            }
        }
        $.PercServiceUtils.makeJsonRequest(setUrl, $.PercServiceUtils.TYPE_POST, true, serviceCallback, obj);

    }

    /**
     * A method to know if the default server is modified and need
     * @param {string} siteName
     * @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function isDefaultServerModified(siteName, callback)
    {
        serviceUrl = $.perc_paths.DEFAULT_SERVER_MODIFIED + siteName;
        var serviceCallback = function(status, results)
        {
            if (status == $.PercServiceUtils.STATUS_ERROR)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(false, defaultMsg);
            }
            else
            {
                callback(true, results.data == "true");
            }
        }
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    function publishToStaging(itemId, itemType, callback)
    {
        var publishUrl = itemType == 'Page' ? $.perc_paths.PAGE_PUBLISH:$.perc_paths.RESOURCE_PUBLISH;
        publishUrl+="/staging/" + itemId;

        _executePublishAction(publishUrl, callback);
    }

    function removeFromStaging(itemId, itemType, callback)
    {
        var publishUrl = itemType == 'Page' ? $.perc_paths.PAGE_TAKEDOWN:$.perc_paths.RESOURCE_TAKEDOWN;
        publishUrl+="/staging/" + itemId;

        _executePublishAction(publishUrl, callback);
    }


})(jQuery);
