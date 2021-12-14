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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

(function($)
{
    WidgetBuilderApp.WidgetDefinitionGeneralView = Backbone.View.extend({
        tagName:'div',
        events: {
            "change input[type='text']" : "updateEdits",
            "change textarea" : "updateEdits",
            "click :checkbox" : "toggleCheckStatus"
        },
        updateEdits: function(event){
            WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
            var curElem = event.currentTarget?event.currentTarget:event.srcElement;
            var newObj = {};
            newObj[curElem.name] = curElem.value;
            this.model.set(newObj);
        },
        toggleCheckStatus: function(event){
            var curElem = event.currentTarget?event.currentTarget:event.srcElement;
            var newObj = {};
            newObj[curElem.name] = curElem.checked;
            this.model.set(newObj);
        },
        initialize:function(){
            this.template = _.template($('#perc-widget-general-tab-template').html());
        },
        render:function (eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        showErrors: function(errors) {
            var $el = $(this.el);
            $el.find(".perc_field_error").remove();
            if(errors){
                $(errors).each(function(){
                    var name = this.name;
                    if(name == "label")
                        name = "widgetname";
                    $el.find("input[name=" + name + "]").parent().append("<label class=\"perc_field_error\" for=\"" + name + "\" generated=\"true\" style=\"display: block;\">" + this.message + "</label>");
                });
            } 
        }
    });
})(jQuery);
