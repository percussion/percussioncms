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
 * Widget builder service
 */
(function($)
{
    /**
     * Service API
     */
    $.PercWidgetBuilderService = {
        getWidgetDefSummaries:_getWidgetDefSummaries,
        deleteWidgetDef:_deleteWidgetDef,
        loadWidgetDefFull:_loadWidgetDefFull,
        deployWidget:_deployWidget,
        saveWidgetDef:_saveWidgetDef,
        validate:_validate
    }

    function _getWidgetDefSummaries(callback){
        _makeServiceCall($.perc_paths.WIDGET_DEFS_SUMMARIES,$.PercServiceUtils.TYPE_GET,null,callback);
    }
    function _deleteWidgetDef(widgetDefId, callback){
        _makeServiceCall($.perc_paths.WIDGET_DEF + widgetDefId,$.PercServiceUtils.TYPE_DELETE,null,callback);
    }
    function _loadWidgetDefFull(widgetDefId, callback){
        _makeServiceCall($.perc_paths.WIDGET_DEF + widgetDefId,$.PercServiceUtils.TYPE_GET,null,callback);
    }
    function _deployWidget(widgetDefId, callback){
        _makeServiceCall($.perc_paths.WIDGET_DEF_DEPLOY + widgetDefId,$.PercServiceUtils.TYPE_POST,null,callback);
    }
    function _saveWidgetDef(widgetDefFull, callback){
        _makeServiceCall($.perc_paths.WIDGET_DEF,$.PercServiceUtils.TYPE_POST,widgetDefFull,callback);
    }
    function _validate(widgetDefFull, callback){
        _makeServiceCall($.perc_paths.WIDGET_DEF_VALIDATE,$.PercServiceUtils.TYPE_POST,widgetDefFull,callback);
    }

    function _makeServiceCall(url, type, data, callback){
        var srvCallback = function(status, result){
            if(status == $.PercServiceUtils.STATUS_ERROR){ 
                var errorMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback(false, errorMsg);
                return;
            }
            callback(true, result.data);
        }
        $.PercServiceUtils.makeJsonRequest(url,type,true,srvCallback,data);
    }
})(jQuery);