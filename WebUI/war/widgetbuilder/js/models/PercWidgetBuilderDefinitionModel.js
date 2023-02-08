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
 * Widget definition model.
 */
(function($)
{
    WidgetBuilderApp.WidgetDefinitionModel = Backbone.Model.extend({
        defaults:{
            "widgetId":"",
            "widgetname":"",
            "description":"",
            "author":"",
            "publisherUrl":"",
            "version":"",
            "prefix":"",
            "responsive":false,
            "widgetTrayCustomizedIconPath":"",
            "toolTipMessage":"",
            "fieldsList": {
                "fields": []
            },
            "widgetHtml":"",
            "jsFileList":{
                "resourceList":[]
            },
            "cssFileList":{
                "resourceList":[]
            }
        },
        convertToServerObject:function(){
            var dataObj = {};
            if(this.get("widgetId"))
                dataObj.widgetId = this.get("widgetId");
            dataObj.prefix = this.get("prefix");
            dataObj.author = this.get("author");
            dataObj.label = this.get("widgetname");
            dataObj.publisherUrl = this.get("publisherUrl");
            dataObj.description = this.get("description");
            dataObj.version = this.get("version");
            dataObj.responsive = this.get("responsive");
            dataObj.widgetTrayCustomizedIconPath = this.get("widgetTrayCustomizedIconPath");
            dataObj.toolTipMessage = this.get("toolTipMessage");
            dataObj = {"WidgetBuilderDefinitionData":dataObj};
            return dataObj;
        },
        convertFromServerObject:function(serverObject){
            var newObj = {
                "widgetId":serverObject.widgetId,
                "widgetname":serverObject.label,
                "description":serverObject.description,
                "author":serverObject.author,
                "publisherUrl":serverObject.publisherUrl,
                "version":serverObject.version,
                "prefix":serverObject.prefix,
                "widgetTrayCustomizedIconPath":serverObject.widgetTrayCustomizedIconPath,
                "toolTipMessage":serverObject.toolTipMessage,
                "responsive":serverObject.responsive
            };
            return newObj;
        }
        
    });
})(jQuery);
