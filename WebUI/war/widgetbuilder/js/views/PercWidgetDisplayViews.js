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
            if($.trim(model.get("widgetHtml")).length > 0)
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
    }

})(jQuery);