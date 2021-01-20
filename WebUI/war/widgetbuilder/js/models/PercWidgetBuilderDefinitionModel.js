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
