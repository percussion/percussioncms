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
		}
		else if(type==="CSS"){
            WidgetBuilderApp.cssResList.add(new WidgetBuilderApp.WidgetResourceModel());
		}
	};

    WidgetBuilderApp.WidgetResourceView = Backbone.View.extend({
        tagName:'div',
        events: {
            "change input" : "updateEdits",
			"click .perc-resource-delete" : "deleteResource"
        },
        updateEdits: function(event){
            WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
            var value = $(this.el).find("input").val();
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
		}
    });    

})(jQuery);