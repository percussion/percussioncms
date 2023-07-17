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
           },
           'click .perc-widget-field-action-edit':function(){
               WidgetBuilderApp.showFieldEditor(this.model);
			   setRowIndexOnContentData_2();
           },
           'click .perc-widget-field-action-delete':function(){
               WidgetBuilderApp.fieldsList.remove(this.model);
               WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
			   setRowIndexOnContentData_2();
           },
		   'keydown .perc-widget-field-action-edit':function(eventHandler){
			   if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
				   WidgetBuilderApp.showFieldEditor(this.model);
				   setRowIndexOnContentData_2();
			   }
           },
           'keydown .perc-widget-field-action-delete':function(eventHandler){
			   if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
				   WidgetBuilderApp.fieldsList.remove(this.model);
				   WidgetBuilderApp.dirtyController.setDirty(true,"Widget",WidgetBuilderApp.saveOnDirty);
				   setRowIndexOnContentData_2();
				}
           },
           'drop' : function(event, index) {
                this.$el.trigger('update-sort', [this.model, index]);
           },
           'update-sort': 'updateSort',
		   "focusin": "rowFocusedContent",
		   "focusout": "rowLostFocusContent"
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
        },
		rowFocusedContent:function(eventHandler){
			$(this.el).find(".perc-widget-field-wrapper").addClass("perc-widget-field-wrapper-hover");
            $(this.el).find(".perc-widget-field-actions").show();
		},
		rowLostFocusContent:function(eventHandler){
		  $(this.el).find(".perc-widget-field-wrapper").removeClass("perc-widget-field-wrapper-hover");
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

	function setRowIndexOnContentData_2(){
		if ($('#perc-widget-fields-container').length){
		var tbl= $("#perc-widget-fields-container");
		var tabIndex=140;
		tbl.find('div').each(function (i, el) {
			var $this = $(this);
			var myClassName = this.className;
			if(myClassName == "perc-widget-field-wrapper"){
				this.setAttribute("tabindex", tabIndex++);

				$this.find('span').each(function (i, el) {
					this.setAttribute("tabindex", tabIndex++);
				});
			}
		});
	   }
	}

})(jQuery);
