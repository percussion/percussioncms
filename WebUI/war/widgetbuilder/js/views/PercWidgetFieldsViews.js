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

    // Views
    WidgetBuilderApp.FieldListView = Backbone.View.extend({

        tagName:'div',

        initialize:function () {
            this.model.on('add', this.render, this);
            this.model.on('remove', this.render, this);
            this.model.on('reset', this.render, this);
        },

        render:function (eventName) {
            $(this.el).empty();
            _.each(this.model.models, function (field) {
                $(this.el).append(new WidgetBuilderApp.FieldListItemView({model:field}).render().el);
            }, this);
            $(this.el).sortable({
                stop: function(event, ui) {
                    ui.item.trigger('drop', ui.item.index());
                }});
            return this;
        }

    });

    WidgetBuilderApp.FieldListItemView = Backbone.View.extend({

        tagName:"div",

        template:_.template($('#perc-widget-fields-collection-template').html()),
        events: {
            'mouseenter': function(){
                $(this.el).find(".perc-widget-field-wrapper").addClass("perc-widget-field-wrapper-hover");
                $(this.el).find(".perc-widget-field-actions").show();

            },
            'mouseleave':function(){
                $(this.el).find(".perc-widget-field-wrapper").removeClass("perc-widget-field-wrapper-hover");
                $(this.el).find(".perc-widget-field-actions").hide();
           },
           'click .perc-widget-field-action-edit':function(){
               WidgetBuilderApp.showFieldEditor(this.model);
           },
           'click .perc-widget-field-action-delete':function(){
               WidgetBuilderApp.fieldsList.remove(this.model);
               WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
           },
           'drop' : function(event, index) {
                this.$el.trigger('update-sort', [this.model, index]);
           },
           'update-sort': 'updateSort'
        },
        render:function (eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        updateSort: function(event, model, position) {            
            WidgetBuilderApp.fieldsList.remove(model);

            WidgetBuilderApp.fieldsList.each(function (model, index) {
                var ordinal = index;
                if (index >= position)
                    ordinal += 1;
                model.set('ordinal', ordinal);
            });            

            model.set('ordinal', position);
            WidgetBuilderApp.fieldsList.add(model, {at: position});
            this.render();
        }
    });
    
    WidgetBuilderApp.FieldEditorView = Backbone.View.extend({

        tagName:"div",

        template:_.template($('#perc-widget-field-editor-template').html()),
        render:function (eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        events: {
            "change input" : "updateEdits",
            "change select" : "updateEdits"
        },
        updateEdits: function(event){
            var curElem = event.currentTarget?event.currentTarget:event.srcElement;
            var newObj = {};
            newObj[curElem.name] = curElem.value;
            this.model.set(newObj);
        },
        showErrors:function(errors){
            var $el = $(this.el);
            $el.find(".perc_field_error").remove();
            if(errors){
                $(errors).each(function(){
                    $el.find("input[name=" + this.name + "]").parent().append("<label class=\"perc_field_error\" for=\"" + this.name + "\" generated=\"true\" style=\"display: block;\">" + this.message + "</label>");
                });
            }
        }        
    });
    
    WidgetBuilderApp.showFieldEditor = function(fieldModel){
        var origModel = fieldModel;
        fieldModel = fieldModel?fieldModel:new WidgetBuilderApp.Field();
        var dialog = $("<div id='perc-widget-field-editor'/>").perc_dialog({
            title: "Add/Update field",
            buttons: {},
            percButtons:{
                "Save":{
                    click: function(){
                        var edModel = WidgetBuilderApp.fieldEditorView.model;
                        var oldName = origModel?origModel.get("name"):"";
                        var errors = edModel.validate(oldName);
                        if(errors.length > 0){
                            WidgetBuilderApp.fieldEditorView.showErrors(errors);
                            return;                            
                        }
                        if(origModel){
                            var index = WidgetBuilderApp.fieldsList.indexOf(origModel);
                            WidgetBuilderApp.fieldsList.remove(origModel);
                            WidgetBuilderApp.fieldsList.add(edModel, {at: index});
                        }
                        else{
                            WidgetBuilderApp.fieldsList.add(edModel);
                        }
                        WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
                        dialog.remove();
                    },
                    id: "perc-widget-field-editor-save"
                },
                "Cancel":{
                    click: function(){
                        dialog.remove();
                    },
                    id: "perc-widget-field-editor-cancel"
                }
            },
            open:function(){
                WidgetBuilderApp.fieldEditorView = new WidgetBuilderApp.FieldEditorView({model:fieldModel});
                $('#perc-widget-field-editor').html(WidgetBuilderApp.fieldEditorView.render().el);
                var fieldsForm = $("form[name=perc-widget-field-editor-form]");
                $.perc_filterField(fieldsForm.find("input[name=name]"), $.perc_textFilters.ALPHA_NUMERIC, function(elem){
                    elem.trigger("change");
                });                
            },
            id: "perc-widget-field-editor-dialog",
            modal: true
        });
    }
    //This method is just written for selenium webdriver
    WidgetBuilderApp.updateFieldsModel = function(){
        $(WidgetBuilderApp.fieldEditorView).find("input, textarea, select").trigger("change");
    }

})(jQuery);
