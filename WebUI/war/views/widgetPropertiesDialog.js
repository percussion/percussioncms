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

//Note; this view was created to edit user properties initially, but has been changed it for editing any properties that
// are of type AbstractUserPref, some of the variable and function names in this file may refer to user pref, treat it as any pref.
//@TODO rename the variable and method names.
function countProperties(obj) {
    var count = 0;
    for(var prop in obj) {
        if(obj.hasOwnProperty(prop))
            ++count;
    }
    return count;
}

(function($, P) {
    P.widgetPropertiesDialog = function( setWidgetProperty, widgetProperties, widgetDefinitionId, postCallback, propertyType, getWidgetByName ) {
        $.perc_widget_definition_client.restGetWidgetDefinition(widgetDefinitionId, propertyType, function(widgetDef) {
            if (widgetProperties){
                if (typeof(widgetProperties.sys_perc_name) != "undefined" && typeof(widgetProperties.sys_perc_description) != "undefined" ){
                    widgetDef.userPrefDef["sys_perc_name"] = (new $.perc_sys_pref("perc_sys_name","","Name","sys_perc_name"));
                    widgetDef.userPrefDef["sys_perc_description"] = (new $.perc_sys_pref("perc_sys_description","","Description","sys_perc_description"));
                }
                widgetDef.setValuesFromWidgetProperties( widgetProperties );
            }
            var dialogOptions = {
                modal : true,
                width : 500,
                title : I18N.message("perc.ui.widget.properties.dialog@Configure Widget Properties"),
                zIndex : 100000,
                percButtons : {
                    'Ok' : {click: saveProperties, id: 'perc-widget-properties-ok'},
                    'Cancel': {click: function(){ $(this).remove(); } , id:'perc-widget-properties-cancel'}
                },
                id: "perc_edit_widget_properties"
            };
            var dialog = $('#perc_edit_widget_properties');
            //If there are no field groups then add height accordingly.
            var numOfFields = countProperties(widgetDef.userPrefDef);
            var dlgHeight = "auto";
            if (numOfFields > 10)
                dlgHeight = "700";
            if ($('#perc_edit_widget_properties > #perc-section-system-container').size() == 0)
                dialogOptions["height"] = dlgHeight;
            $("<div/>").append( widgetDef.render() ).perc_dialog(dialogOptions);
            _addFieldGroups();
        });

        function saveProperties(){

            requiredFieldsValid = _checkRequiredFields(this);

            // Only process the form if all required fields are entered
            if(requiredFieldsValid == true) {
                //Check the uniqueness of the widget name.
                var widget = $(this).find('[name=sys_perc_name]');
                var widgetName = $.trim(widget.val()).toUpperCase();
                var originalName = $.trim(widget.attr('originalValue')).toUpperCase();
                if (typeof(widgetName) != "undefined" && widgetName != "" && widgetName != originalName && getWidgetByName(widgetName) != null ){
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.widget.properties.dialog@Widget Name") + widgetName + I18N.message("perc.ui.widget.properties.dialog@Widget Name Already Used")});
                    return;
                }

                $(this).find('input').each( function(){
                    var value = $.trim($(this).val());
                    if($(this).attr('type')=='checkbox')
                    {
                        value = $(this).is(':checked')?'true':'false';
                    }
                    setWidgetProperty( $(this).attr('name'), value );
                });
                $(this).find('select').each(function(){
                    setWidgetProperty( $(this).attr('name'), $(this).val() );
                });
                $(this).find('textarea').each(function(){
                    setWidgetProperty( $(this).attr('name'), $(this).val() );
                });

                postCallback();
                $(this).remove();
            }

            else {
                $('#percRequiredFieldWarning').text(I18N.message("perc.ui.general@Required Fields Warning"));
            }

        }

        // A private helper method to check required fields
        function _checkRequiredFields(formObject) {
            var isValid = true;
            $(formObject).find('input,textarea,select').each(function(){
                if($(this).prop('required') && $(this).val() === ''){
                    isValid = false;
                    return false;
                }
            });
            return isValid;
        }

        // A private helper method to group the fields and create collapsible sections
        function _addFieldGroups() {
            var dialog = $('#perc_edit_widget_properties');

            //Identify is we will use grouping style or not.
            if (dialog.find('#perc-section-system-container').size() > 0)
                dialog.find('.ui-dialog-content.ui-widget-content').addClass('group-style');

            var fieldGroups = [
                { groupName : "perc-section-system-container", groupLabel : I18N.message("perc.ui.widget.properties.dialog@Widget Summary")}
                , { groupName : "perc-section-properties-container", groupLabel : I18N.message("perc.ui.widget.properties.dialog@Properties")}
            ];
            $.each(fieldGroups, function(index) {
                // Create HTML markup with the groupName minimizer/maximizer and
                // insert it before the 1st field in each group
                var minmaxClass = (index == 0) ? "perc-items-minimizer" : "perc-items-maximizer";
                var groupHtml =
                    "<div class='perc-section-header'>" +
                    "<div class='perc-section-label' groupName='" + this.groupName + "'>" +
                    "<span  class='perc-min-max " + minmaxClass + "' ></span>" + this.groupLabel +
                    "</div>" +
                    "</div>";
                dialog.find('#' + this.groupName).before(groupHtml);
                // The first group will be the only one expanded (hide all others)
                index != 0 && dialog.find('#' + this.groupName).hide();
            });

            // Bind collapsible event
            dialog.find(".perc-section-label").unbind().click(function() {
                var self = $(this);
                self.find(".perc-min-max")
                    .toggleClass('perc-items-minimizer')
                    .toggleClass('perc-items-maximizer');
                dialog.find('#' + self.attr('groupName')).toggle();
            });
        }
    };
    /**
     *  Widget Definition Model.
     *  Represents a set of widget user preference properties
     */
    $.perc_widget_definition_model = function()
    {
        // original xml coming from service
        // will be parsed out into an array of user preferences
        this.widgetDefinitionXml = null;
        // contains all the user preferences for this type of widget
        this.userPrefDef = null;

        this.setValuesFromWidgetProperties = function(widgetProperties)
        {
            for(p in this.userPrefDef)
            {
                var propertyName = this.userPrefDef[p].name;
                this.userPrefDef[p].realValue = widgetProperties[propertyName];
            }
        };

        // getter for userPrefDef
        this.getUserPrefDef = function()
        {
            return this.userPrefDef;
        };

        // initialize widget definition model
        this.init = function(xml, propertyType)
        {
            var self = this;
            // store original xml
            this.widgetDefinitionXml = xml;
            // convert xml to a jQuery object so we can traverse
            var $widgetDefinitionXml = $(this.widgetDefinitionXml);
            // get all the User Pref elements
            var $userPreferences = $widgetDefinitionXml.find(propertyType);
            //Initialice userPref Object
            this.userPrefDef = new Object();

            //finish if we dont have userPref to process
            if($userPreferences.length == 0)
                return;

            // iterate over all the User Pref elements
            $userPreferences.each(function()
            {
                // get the attributes common to all User Prefs
                var datatype = $(this).attr('datatype');
                var default_value = $(this).attr('default_value');
                var display_name = $(this).attr('display_name');
                var name = $(this).attr('name');
                var required_field = $(this).attr('required');
                var enumValues = null;

                // if its enum get the Enum Values
                if(datatype == '' || datatype == null)
                {
                    datatype = 'string';
                }
                else if(datatype == 'enum')
                {
                    enumValues = new Object();
                    // iterate over all the Enum Values
                    $(this).find('EnumValue').each(function()
                    {
                        // get the attributes for each enumerated value
                        var display_value = $(this).attr('display_value');
                        var value = $(this).attr('value');
                        enumValues[value] = display_value;
                    });
                }

                // create an instance of the user preferences and add them to the model in an array
                self.userPrefDef[name] = (new $.perc_user_pref
                (
                    datatype,
                    default_value,
                    display_name,
                    name,
                    required_field,
                    enumValues
                ));
            });
        };

        // generates an HTML table with property names and input fields
        // for the user prefs depending on their datatype
        this.render = function()
        {
            var html = $("<div/>");
            var systemContainer = $("<div id='perc-section-system-container' />");
            var propertiesContainer = $("<div id='perc-section-properties-container' />");
            var sysProperties = ""
            var properties = "";
            for(u in this.userPrefDef)
            {
                if((this.userPrefDef[u].name == "sys_perc_name" || this.userPrefDef[u].name == "sys_perc_description") &&
                    (this.userPrefDef[u].datatype == "perc_sys_name" || this.userPrefDef[u].datatype == "perc_sys_description"))
                    sysProperties += "\t"+this.userPrefDef[u].render();
                else
                    properties += "\t"+this.userPrefDef[u].render();
            };
            if (sysProperties != "" && properties != ""){
                systemContainer.append($(sysProperties))
                html.append($("<div class='fieldGroup' />").append(systemContainer));
                propertiesContainer.append($(properties));
                html.append($("<div class='fieldGroup' />").append(propertiesContainer));
            }
            else if (properties == "")
                html.append($(sysProperties));
            else
                html.append($(properties));

            // Prepend required field warning
            html.prepend('<div id="percRequiredFieldWarning"><p>&nbsp;</p></div>');

            // Append required field key only if there is a required field in the widget
            if($(html).find('input,textarea,select').filter('[required]').length > 0) {
                html.append('<div><p>' + I18N.message("perc.ui.general@Denotes Required Field") + '</p></div>');
            }

            $.perc_filterField(html.find('[name=sys_perc_name]'), $.perc_textFilters.ID_WITH_SPACE);
            $.perc_filterField(html.find('[name=sys_perc_description]'), $.perc_textFilters.DESCRIPTION);
            return html;
        };
    };

    /**
     *  Widget Definition Client
     *  Retrieves widget definition from REST Service and creates a widget definition model
     */

    $.perc_widget_definition_client = new function()
    {
        this.restGetWidgetDefinition = function(widgetDefinitionId, propertyType, callback)
        {
            $.ajax(
                {
                    headers: {
                        'Accept': 'application/xml',
                        'Content-Type': 'application/xml'
                    },
                    url: $.perc_paths.WIDGET_FULL + "/" + widgetDefinitionId,
                    type: "GET",
                    success: function(xml, textstatus)
                    {
                        var model = new $.perc_widget_definition_model(xml);
                        model.init(xml, propertyType);
                        callback(model);
                    },
                    error : function()
                    {
                        alert(I18N.message("perc.ui.widget.properties.dialog@Unable To Retrieve Widget Definition") + widgetDefinitionId);
                    }
                });
        };
        this.restGetWidgetPrefs = function(widgetDefinitionId, callback)
        {
            $.ajax(
                {
                    headers: {
                        'Accept': 'application/xml',
                        'Content-Type': 'application/xml'
                    },
                    url: $.perc_paths.WIDGET_FULL + "/" + widgetDefinitionId,
                    type: "GET",
                    success: function(xml, textstatus)
                    {
                        var $widgetDefinitionXml = $(xml);
                        var $widgetPrefs = $widgetDefinitionXml.find("WidgetPrefs");
                        callback($widgetPrefs);
                    },
                    error : function()
                    {
                        alert(I18N.message("perc.ui.widget.properties.dialog@Unable To Retrieve Widget Definition") + widgetDefinitionId);
                    }
                });
        };
    };

    $.perc_sys_pref = function(datatype, default_value, display_name, name, required_field)
    {
        this.datatype = datatype;
        this.default_value = default_value;
        this.display_name = display_name;
        this.name = name;
        this.required_field = required_field;
        this.realValue = null;

        this.render = function()
        {
            var buff = "";
            if(datatype == 'perc_sys_name'){
                var value = (typeof(this.realValue) != "undefined") ? this.realValue : this.default_value;
                buff =  '<tr>\n';
                buff += '   <td><label for="' + this.name + '">' + this.display_name+'</label>: </td>\n'+ '</tr>\n';
                buff += '   <td><input class="perc-widget-property" name="'+this.name+'" type="text" value="'+value+'" originalValue="'+value+'" maxlength="30"></td>\n'+ '</tr>\n';
                buff += '</tr>\n';
                return buff;
            }

            if(datatype == 'perc_sys_description'){
                var value = (typeof(this.realValue) != "undefined") ? this.realValue : this.default_value;
                buff =  '<tr>\n';
                buff += '   <td><label for="' + this.name + '">' + this.display_name+'</label>: </td>\n'+ '</tr>\n';
                buff += '   <td><textarea style="resize: none; border: 1px inset #F0F0F0; padding: 2px; height: 40px" class="perc-widget-property" name="'+this.name+'" type="text" maxlength="100">' + value + '</textarea></td>\n'+ '</tr>\n';
                buff += '</tr>\n';
                return buff;
            }

            return buff;
        }
    }

    $.perc_user_pref = function(datatype,default_value,display_name,name,required_field,enumValues)
    {
        this.datatype = datatype;
        this.default_value = default_value;
        this.display_name = display_name;
        this.name = name;
        this.required_field = required_field;
        this.enumValues = enumValues;
        this.realValue = null;

        if(this.required_field === 'true') {
            this.display_name_prepend = '* ';
            this.required_attr = 'aria-required="true" required';
            this.required_class = ' class="perc-required-field" ';
        }
        else {
            this.display_name_prepend = '';
            this.required_attr = '';
            this.required_class = '';
        }

        // generates HTML table row with property name and input field
        // for this user pref's datatype
        this.render = function()
        {

            var buff = '<tr>\n';
            var datatype = this.datatype;
            var value = undefined;

            if(typeof(this.realValue)!== 'boolean')
            {
                if(this.realValue === undefined)
                {
                    value = this.default_value;
                } else
                {
                    value = (this.realValue === "0") ? this.default_value : this.realValue;
                }
            } else
            {
                if(this.realValue === undefined)
                {
                    value = this.default_value;
                } else
                {
                    value = this.realValue;
                }
            }

            if (value === undefined)
                value = "";

            if(datatype == 'string' || datatype == 'number' )
            {
                buff += '   <td><label' + this.required_class + 'for="' + this.name + '">' + this.display_name+'</label>: </td>\n'+ '</tr>\n';
                buff += '   <td><input class="perc-widget-property" name="'+this.name+'" type="text" value="'+value+'"' + this.required_attr +'></td>\n'+ '</tr>\n';
            }
            else if(datatype == 'bool' )
            {
                var checked = (value==true || value=='true'||value=='on')?'CHECKED':'';
                buff += '   <td class = "checkbox-size"><input class="perc-widget-property" name="'+this.name+'" type="checkbox" '+checked+ " " + this.required_attr+'> <label for="' + this.name + '">' + this.display_name+'</label></td>\n'+ '</tr>\n';

            }
            else if(datatype == 'enum')
            {
                buff += '   <td><label' + this.required_class + 'for="' + this.name + '">' + this.display_name+'</label>: </td>\n'+ '</tr>\n';
                buff += '   <td>\n';
                buff += '   <select class="perc-widget-property" name="'+this.name+'" value="'+value+'"'+this.required_attr+'>\n';
                for(v in enumValues)
                {
                    var selected = (v == value) ? 'SELECTED' : '';
                    buff += '       <option value="'+v+'" '+selected+'> '+enumValues[v]+'   </option>\n' ;
                }
                buff += '   </select>\n';
                buff += '</td>\n'+ '</tr>\n';
            }
            else if(datatype == 'list')
            {
                //  var values = JSON.parse(value); // not sure why this does not work
                var values = eval(value);

                buff += '   <td><label' + this.required_class + 'for="' + this.name + '">' + this.display_name+'</label>: </td>\n'+ '</tr>\n';
                buff += '   <td><textarea class="perc-widget-property" name="'+this.name+'">\n';
                for(v in values)
                {
                    buff += values[v]+'\n';
                }
                buff += '</textarea>\n';
            }
            buff += '</tr>\n';
            return buff;

        };
    };

})(jQuery, jQuery.Percussion);
