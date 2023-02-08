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
    };

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
            if(status === $.PercServiceUtils.STATUS_ERROR){
                var errorMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback(false, errorMsg);
                return;
            }
            callback(true, result.data);
        };
        $.PercServiceUtils.makeJsonRequest(url,type,false, srvCallback,data);
    }
})(jQuery);
