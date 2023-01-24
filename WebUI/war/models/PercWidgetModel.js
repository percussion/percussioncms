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
        this.relationshipId = -1;
        
        //
        // setters and getters
        //
                
        this.setId = function(id)
        {
            this.id = id;
        };
        
        this.getId = function()
        {
            return this.id;
        };
        
        this.setHtml = function(html)
        {
            this.html = html;
        };
        
        this.getHtml = function()
        {
            return this.html;
        };
        
        this.setCss = function(css)
        {
            this.css = css;
        };
        
        this.getCss = function()
        {
            return this.css;
        };
        
        this.setDefinitionId = function(definitionId)
        {
            this.definitionId = definitionId;
        };
        
        this.getDefinitionId = function()
        {
            return this.definitionId;
        };
        
        this.setProperties = function(properties)
        {
            this.properties = properties;
        };
        
        this.getProperties = function()
        {
            return this.properties;
        };
        
        this.setName = function(name)
        {
            this.name = name;
        };
        
        this.getName = function()
        {
            return this.name;
        };
        
        this.setAssetIds = function(assetIds)
        {
            this.assetIds = assetIds;
        };
        
        this.getAssetIds = function()
        {
            return this.assetIds;
        };
        
        this.setRelationshipId = function(relationshipId)
        {
            this.relationshipId = relationshipId;
        };
        
        this.getRelationshipId = function()
        {
            return this.relationshipId;
        };
    };
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
        };
    };
    
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
        };
    };
})(jQuery);
