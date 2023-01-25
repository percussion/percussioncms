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

    WidgetBuilderApp.WidgetHtmlView = Backbone.View.extend({
        tagName:'div',
        events: {
            "change textarea" : "updateEdits"
        },
        updateEdits: function(event){
            WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
            var curElem = event.currentTarget?event.currentTarget:event.srcElement;
            var newObj = {};
            newObj[curElem.name] = curElem.value;
            this.model.set(newObj);
        },
        initialize:function(){
            this.template = _.template($('#perc-widget-display-editor-template').html());
            this.model.on("change", this.setDirty, this);
        },
        render:function (eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        setDirty:function(){
            console.log("dirty called");
            WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
        },
        autoGenerate:function (){
            var model = this.model;
            var self = this;
            if(model.get("widgetHtml").trim().length > 0)
            {
                var settings = {
                    id: 'perc-widget-replace-confirm',
                    title: "Display HTML replace Warning",
                    question: "Are you sure you want to replace the display HTML with auto generated code?",
                    success: function(){
                        generateHtml();
                    },
                    cancel:function(){},
                    type: "CANCEL_CONTINUE",
                    width: 700
                };
                $.perc_utils.confirm_dialog(settings);                
            }
            else{
                generateHtml();
            }
            function generateHtml(){
                var dhtml = "<div>\n";
                $.each(WidgetBuilderApp.fieldsList.models, function () {
                    if(this.get("type") == "IMAGE"){
                        dhtml += "<img src=\"$" + this.get("name") + "_path\" title=\"$" + this.get("name") + "_title\" alt=\"$"  + this.get("name") + "_alt_text\"\ />\n";
                    }
                    else if(this.get("type") == "FILE" || this.get("type") == "PAGE"){
                        dhtml += "<a href=\"$" + this.get("name") + "_path\" title=\"$" + this.get("name") + "_title\"\>$" + this.get("name") + "_title</a>\n";
                    }
                    else{
                        dhtml += "<div>$" + this.get("name") + "</div>\n"; 
                    }
                }, this);
                dhtml += "</div>"; 
                model.set({"widgetHtml":dhtml});
                self.render();               
            }
        }
    });
    WidgetBuilderApp.autoGenerateHtml = function(){
        WidgetBuilderApp.widgetHtmlView.autoGenerate();
    };

})(jQuery);
