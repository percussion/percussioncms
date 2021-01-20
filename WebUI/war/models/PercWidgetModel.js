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

(function($)
{
    $.PercWidgetModel = function()
    {
        // region id this widget is in
        this.id = null;
        
        // whether this widget is locked
        this.locked = false;
        
        // html from service
        this.html = null;
        
        // css object model
        this.ccs = null;
        
        // defines wether this is a Raw HTML widget or a Rich Text widget or one of many other types of widgets
        this.definitionId = null;
        
        //
        this.name = null;

        // user defined properties
        this.properties = null;
        
        // 
        this.assetIds = null;
        
        //the current relationship Id
        this.relationshipId = -1
        
        //
        // setters and getters
        //
                
        this.setId = function(id)
        {
            this.id = id;
        }
        
        this.getId = function()
        {
            return this.id;
        }
        
        this.setHtml = function(html)
        {
            this.html = html;
        }
        
        this.getHtml = function()
        {
            return this.html;
        }
        
        this.setCss = function(css)
        {
            this.css = css;
        }
        
        this.getCss = function()
        {
            return this.css;
        }
        
        this.setDefinitionId = function(definitionId)
        {
            this.definitionId = definitionId;
        }
        
        this.getDefinitionId = function()
        {
            return this.definitionId;
        }
        
        this.setProperties = function(properties)
        {
            this.properties = properties;
        }
        
        this.getProperties = function()
        {
            return this.properties;
        }
        
        this.setName = function(name)
        {
            this.name = name;
        }
        
        this.getName = function()
        {
            return this.name;
        }
        
        this.setAssetIds = function(assetIds)
        {
            this.assetIds = assetIds;
        }
        
        this.getAssetIds = function()
        {
            return this.assetIds;
        }
        
        this.setRelationshipId = function(relationshipId)
        {
            this.relationshipId = relationshipId;
        }
        
        this.getRelationshipId = function()
        {
            return this.relationshipId;
        }
    }
    // JGA
    // Data model for Asset Drop Criteria which contains
    // information about a widget such as whether a widget
    // is locked or not and its owner: pageid or templateid
    $.PercAssetDropCriteriaModel = function(   widgetId,
                                               appendSupport,
                                               locked,
                                               multiItemSupport,
                                               ownerId,
                                               supportedContentTypes,
                                               assetShared,
                                               relationshipId)
    {
        this.widgetId              = widgetId;
        this.appendSupport         = appendSupport;
        this.locked                = locked;
        this.multiItemSupport      = multiItemSupport;
        this.ownerId               = ownerId;   // templateId or pageId that owns this widget
        this.supportedContentTypes = supportedContentTypes;
        this.assetShared           = assetShared;
        this.relationshipId        = relationshipId;
        this.log = function()
        {
            console.log("widgetId              = " + this.widgetId);
            console.log("appendSupport         = " + this.appendSupport);
            console.log("locked                = " + this.locked);
            console.log("multiItemSupport      = " + this.multiItemSupport);
            console.log("ownerId               = " + this.ownerId);
            console.log("supportedContentTypes = " + this.supportedContentTypes);
        }
    }
    
    // JGA
    // holds data for rendering an asset editor icon
    // and invoking its URL to open up the editor in
    // a separate dialog or in the iframe    
    $.PercAssetEditorModel = function( icon,
                                        title,
                                        url,
                                        workflowId)
    {
        this.icon         = icon;
        this.title        = title;
        this.url          = url;
        this.workflowId   = workflowId;
        this.log   = function()
        {
            console.log("icon       = " + this.icon);
            console.log("title      = " + this.title);
            console.log("url        = " + this.url);
            console.log("workFlowId = " + this.workflowId);
        }
    }
})(jQuery);