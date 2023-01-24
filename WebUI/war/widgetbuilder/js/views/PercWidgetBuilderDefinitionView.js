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
