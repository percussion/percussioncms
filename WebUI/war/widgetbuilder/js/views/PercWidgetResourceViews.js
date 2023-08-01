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

    // Views
    WidgetBuilderApp.ResourceListView = Backbone.View.extend({

        tagName:'div',

        initialize:function () {
            this.model.on('add', this.render, this);
            this.model.on('remove', this.render, this);
            this.model.on('reset', this.render, this);
        },
        events: {
            "click .perc-widget-add-resource-button" : "addResource",
        },
		addResource:function(){
			this.model.models.add(new WidgetResourceModel());
		},        
        render:function (eventName) {
            $(this.el).empty();
            _.each(this.model.models, function (resource) {
                $(this.el).append(new WidgetBuilderApp.WidgetResourceView({model:resource}).render().el);
            }, this);
            return this;
        },
		toStringArray:function(){
            var resArray = [];
            _.each(this.model.models, function (resource) {
                var resName = resource.get("name").trim();
				if(resName !== '')
				    resArray.push(resName);
            }, this);
			return resArray;
        }
    });
	WidgetBuilderApp.addNewResource = function(type){
		if(type==="JS"){
			WidgetBuilderApp.jsResList.add(new WidgetBuilderApp.WidgetResourceModel());
			setTabIndexForJsResources();
		}
		else if(type==="CSS"){
            WidgetBuilderApp.cssResList.add(new WidgetBuilderApp.WidgetResourceModel());
			setTabIndexForCssResources();
		}
	};

    WidgetBuilderApp.WidgetResourceView = Backbone.View.extend({
        tagName:'div',
        events: {
            "change input" : "updateEdits",
			"click .perc-resource-delete" : "deleteResource",
			"keydown .perc-resource-delete" : "deleteResourceOnKeydown",
			"keydown .datadisplay" : "avoidOnEnter"

        },
        updateEdits: function(event){
            WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
            var value = $(this.el).find("input.datadisplay.perc-resource-entry-field").val();
            this.model.set({"name":value});
        },
        initialize:function(){
            this.template = _.template($('#perc-widget-resource-item-editor-template').html());
        },
        render:function (eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
		deleteResource:function(){
            WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
			this.model.destroy();
		},
		deleteResourceOnKeydown:function(eventHandler){
			if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
				WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
				this.model.destroy();
			}
		},
		avoidOnEnter:function(eventHandler){
			if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
				return false;
			}
		}
    });    
	function setTabIndexForJsResources(){
		var tabindexCounter = 172;
		var tagCol = "";
		$('#perc-widget-js-resources-container').find('*').each(function(){
			var tagName = this.tagName;
			tagCol = tagCol+", "+tagName;
			if(tagName == "INPUT"){
				this.setAttribute("tabindex", tabindexCounter++);
			}
			if(tagName == "SPAN"){
				this.setAttribute("tabindex", tabindexCounter++);
			}
		});
	}

	function setTabIndexForCssResources(){
		var tabindexCounter = 192;
		var tagCol = "";
		$('#perc-widget-css-resources-container').find('*').each(function(){
			var tagName = this.tagName;
			tagCol = tagCol+", "+tagName;
			if(tagName == "INPUT"){
				this.setAttribute("tabindex", tabindexCounter++);
			}
			if(tagName == "SPAN"){
				this.setAttribute("tabindex", tabindexCounter++);
			}
		});
	}

})(jQuery);
