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

/**
 *	perc_template_controller
 *	Author: Jose Annunziato
 *	Content:
 *	<ul>
 *	<li> perc_layout_controller - handles events from perc_template_layout_widget.js. Delegates on clients below
 *	<li> perc_widget_definition_client - gets widget definition for a widget type. TODO: refactor into services/PercWidgetService.js
 *	<li> perc_widget_library_client - gets the library of widgets. TODO: refactor into services/PercWidgetService.js
 *	<li> perc_widget_library_model - holds list of widgets types
 *	<li> perc_widget_summary_model - holds details of widget
 *	<li> perc_widget_definition_model - holds list of user preferences for a widget
 *	<li> perc_user_pref - represents a user preference to configure a widget
 *	</ul>
 *	TODO:
 *	Refactor perc_widget_*_client to services/PercWidgetService.js
 *	Refactor perc_widget_*_model  to models/PercWidgetModel.js
 */


/**
 *  Layout Controller
 *
 *  Handles all events generated from layout widget,
 *  modifies the model and prepares for rendering
 *
 */
(function($)
{
    $.perc_layout_controller = function()
    {
        this.widget = null;
        this.widgetDefinitions = new Array();
        this.currentWidgetDefinition = null;
        this.templateXml = null;
        this.pageXml = null;
        this.helper = null;

        /**
         *  Helper is a reference to perc_template_layout_helper
         *  Most of the functionality in the helper is being refactored into this controller
         */

        this.setHelper = function(helper)
        {
            this.helper = helper;
        };
        
        this.getCurrentWidgetDefinition = function()
        {
            return this.currentWidgetDefinition;
        };
        
        /**
         *  Invoked when user clicks on edit button on a widget
         */
        this.editWidgetStart = function(widget, callback)
        {
            this.widget = widget;
            
            // check to see if we already have the widget definition
            this.currentWidgetDefinition = this.widgetDefinitions[widget.getWidgetDefinitionId()];

            // if we dont have a widget definition, get it from the REST service
            var self = this;
            if(this.currentWidgetDefinition == null)
            {
                // use the REST client to retrieve the widget defintion for the widget type
                // the client returns an object representing the widget definition
                $.perc_widget_definition_client.restGetWidgetDefinition(widget.getWidgetDefinitionId(), function(restWidgetDefinition)
                {
                    if(restWidgetDefinition.getUserPrefDef() == null)
                    {
                        alert(I18N.message("perc.ui.layout.controller@Widget Properties"));
                        return;
                    }

                    // the client parses the service response into a model
                    self.widgetDefinitions[widget.getWidgetDefinitionId()] = restWidgetDefinition;
                    self.currentWidgetDefinition = restWidgetDefinition;                    

                    // override the default definition values with the widget values parsed when loading widgets from template or page object
                    self.currentWidgetDefinition.setValuesFromWidgetProperties(widget.properties);
                    // display dialog
                    $('#perc-widget-edit').dialog('open');
                });
            }
            else
            {
                // override the default definition values with the widget values parsed when loading widgets from template or page object
                self.currentWidgetDefinition.setValuesFromWidgetProperties(widget.properties);
                // display dialog
                $('#perc-widget-edit').dialog('open');
            }
        };
        
        this.editWidgetClientValidate = function()
        {
            // TODO: validate on the client
            // return true if all ok, false otherwise with feedback
            return true;
        };
        
        /**
         *  Invoked when user clicks on ok button on a widget
         */
        this.editWidgetOk = function(callback)
        {
            var self = this;
            
            // iterate over all the user preferences defined in the widget definition
            var userPrefs = this.currentWidgetDefinition.getUserPrefDef();
            for(var d in userPrefs)
            {
                // use the name of the property to reference fields in the HTML dialog
                var selector = '.perc-widget-property[name="'+d+'"]';

                // get the value from the field
                var value = $(selector).val();

                // if property is of type bool, then see if checkbox is checked
                if(userPrefs[d].datatype === 'bool')
                    value = $(selector).is(':checked');

                // if property is of type list, get text from textarea
                // and parse each line as a separate value
                if(userPrefs[d].datatype === 'list')
                {
                    values = value.replace(/^\s+|\s+$/g,"") .split('\n');
                    value = new Array();
                    for(var v in values)
                        value.push(values[v]);
                    value = JSON.stringify(value);
                }

                // store the property and value in the widget object
                this.widget.setProperty(d, value);
            }

            // TODO: validate with server. if everything ok, continue, otherwise notify user
            this.helper.templateModelToTemplateXml();
            
            var parentRegion = this.widget.getParentRegion();
            this.helper.getRegionHtmlFromRest(parentRegion.getId(), function(html)
            {
                $(html).find('.perc-widget').each(function()
                {
                    var widgetId = $(this).attr('widgetId');
                    var id = $(this).attr('id');
    //              widget = parentRegion.getWidget($(this).attr('widgetId'));
                    widget = parentRegion.getWidget(id);
                    widget.setHtml($(this).html());
                });
                callback();
            });
        };
        
        this.editWidgetCommit = function()
        {
            alert(I18N.message("perc.ui.layout.controller@Widget Commit"));
        };

        this.getWidgetLibrary = function(callback)
        {
            $.perc_widget_library_client.restGetWidgetLibrary(callback);
        };

        this.init = function()
        {
        };
    };
})(jQuery);

/**
 *  REST Clients
 *	TODO: Refactor this into services/PercWidgetService.js
 *	containing functions getWidgetLibrary(), getWidgetDefinition()
 */

(function($)
{
    /**
     *  Widget Library Client
     *  Retrieves widget summaries from REST Service
     *	TODO: Move this into services/PercWidgetService.js
     */

    $.perc_widget_library_client = function()
    {
        this.restGetWidgetLibrary = function(callback)
        {
            var scvurl = "/Rhythmyx/services/pagemanagement/widget";
            $.ajax(
            {
            //  url: $.perc_paths.WIDGET_ALL + "/",
                url: scvurl,
                dataType: "xml",
                type: "GET",
                success: function(xml, textstatus)
                {
                    var widgetLibrary = new $.perc_widget_library_model(xml);
                    widgetLibrary.init();
                    callback(widgetLibrary);
                },
                error : function()
                {
                    alert(I18N.message("perc.ui.layout.controller@Unable To Retrieve Widget Lib"));
                }
            });
            
        };
    };

    /**
     *  Widget Definition Client
     *  Retrieves widget definition from REST Service and creates a widget definition model
     *	TODO: Move this into services/PercWidgetService.js
     */
    /*
    $.perc_widget_definition_client = new function()
    {
        this.restGetWidgetDefinition = function(widgetDefinitionId, callback)
        {
            $.ajax(
            {
                url: $.perc_paths.WIDGET_FULL + "/" + widgetDefinitionId,
                dataType: "xml",
                type: "GET",
                success: function(xml, textstatus)
                {
                    var model = new $.perc_widget_definition_model(xml);
                    model.init(xml);
                    callback(model);
                },
                error : function()
                {
                    alert(I18N.message("perc.ui.layout.controller@Unable To Retrieve Widget Def") + widgetDefinitionId);
                }
            });
        }
    }
    */
})(jQuery);

/**
 *  Widget Related Models.
 */

(function($)
{
    /**
     *  Widget Library Model represents a list of Widget Summaries
     *  Parses XML from REST but delegates to Widget Summary Model to parse their own piece
     */
    $.perc_widget_library_model = function(widgetLibraryXml)
    {
        this.widgetLibraryXml = widgetLibraryXml;
        this.widgetSummaries = new Array();
        
        // parses widget summary instances from XML, delegates to widget summary to parse details
        this.init = function()
        {
            var self = this;
            var $widgetLibraryXml = $(this.widgetLibraryXml);
            var $summaries = $widgetLibraryXml.find('WidgetSummarys WidgetSummary');
            $summaries.each(function()
            {
                var summary = new $.perc_widget_summary_model($(this));
                summary.init();
                self.widgetSummaries.push(summary);
            });
        };
        
        this.getWidgetSummaries = function()
        {
            return this.widgetSummaries;
        };
        
        // TODO: refactor out of here into a view
        this.html = function()
        {
            var buff = '<div class="perc-header" style="float:none;"> ' + I18N.message("perc.ui.layout.controller@Drag and Drop Widgets") + ' </div>\n';
            for(var s in this.widgetSummaries)
            {
                buff += this.widgetSummaries[s].html();
            }
            return buff;
        };
    };
    
    /**
     *  Widget Summary
     *  Parses its own XML piece from widget summary to retrieve the icon, id, label, and name
     *	TODO: consider moving parsing to controller or service instead. Leave just data here.
     */
    $.perc_widget_summary_model = function($widgetSummaryXml)
    {
        this.$widgetSummaryXml = $widgetSummaryXml;
        this.icon = null;
        this.id = null;
        this.label = null;
        this.name = null;
        
        this.init = function()
        {
            this.icon   = this.$widgetSummaryXml.children('icon').text();
            this.id     = this.$widgetSummaryXml.children('id').text();
            this.label  = this.$widgetSummaryXml.children('label').text();
            this.name   = this.$widgetSummaryXml.children('name').text();
        };
        
        this.alert = function()
        {
            alert([this.icon, this.id, this.label, this.name, this.widgetSummaryXml]);
        };
        
        // TODO: refactor out of here into a view
        this.html = function()
        {
            var buff = "";
            buff += "<div class='perc-toolbar-item' id='"+this.id+"' name='"+this.name+"'>\n";
            buff += "   <img src='../../Rhythmyx"+this.icon+"' ";
            buff += "       onMouseOver='this.style.cursor=\"pointer\";' >\n";
            buff += "       <div>"+this.label+"</div>\n";
            buff += "</div>\n";
            return buff;
        };
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
            for(var p in this.userPrefDef)
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
     	// TODO: consider moving parsing to controller or service instead. Leave just data here.
        this.init = function(xml)
        {
            var self = this;

            // store original xml
            this.widgetDefinitionXml = xml;

            // convert xml to a jQuery object so we can traverse
            var $widgetDefinitionXml = $(this.widgetDefinitionXml);

            // get all the User Pref elements
            var $userPreferences = $widgetDefinitionXml.find('Widget UserPref');

            // iterate over all the User Pref elements
            if($userPreferences.length > 0)
            {
                this.userPrefDef = new Object();
                $userPreferences.each(function()
                {
                    // get the attributes common to all User Prefs
                    var datatype = $(this).attr('datatype');
                    var default_value = $(this).attr('default_value');
                    var display_name = $(this).attr('display_name');
                    var name = $(this).attr('name');
                    var enumValues = null;
    
                    // default datatype is string
                    if(datatype === '' || datatype == null)
                    {
                        datatype = 'string';
                    }
                    // if its enum get the Enum Values
                    else if(datatype === 'enum')
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
                        enumValues
                    ));
                });
            }
        };
        
        // generates an HTML table with property names and input fields
        // for the user prefs depending on their datatype
        // TODO: move this out of here into a view
        this.render = function()
        {
            var html = "<table>\n";
            for(var u in this.userPrefDef)
            {
                html += "\t"+this.userPrefDef[u].render();
            }
            html += "</table>\n";
            return html;
        };
    };
})(jQuery);

/**
 *
 * User pref class represents a single widget user preference property.
 * Can generate an HTML form to edit each type of user preference.
 *
 */
(function($){
    $.perc_user_pref = function(datatype,default_value,display_name,name,enumValues)
    {
        this.datatype = datatype;
        this.default_value = default_value;
        this.display_name = display_name;
        this.name = name;
        this.enumValues = enumValues;
        this.realValue = null;
        
        // generates HTML table row with property name and input field
        // for this user pref's datatype
        // TODO: move this out of here into a view
        this.render = function()
        {
            var buff = '<tr>\n';
            var datatype = this.datatype;
            var value = (this.realValue == null || this.realValue === "") ? this.default_value : this.realValue;
            
            if(datatype === 'string' || datatype === 'number' )
            {
                buff += '   <td>' + this.display_name+': </td>\n';
                buff += '   <td><input class="perc-widget-property" name="'+this.name+'" type="text" value="'+value+'"></td>\n';
            }
            else if(datatype === 'bool' )
            {
                var checked = (value==='true'||value==='on')?'CHECKED':'';
                buff += '   <td>' + this.display_name+': </td>\n';
                buff += '   <td><input class="perc-widget-property" name="'+this.name+'" type="checkbox" '+checked+'></td>\n';
            }
            else if(datatype === 'enum')
            {
                buff += '   <td>' + this.display_name+': </td>\n';
                buff += '   <td>\n';
                buff += '   <select class="perc-widget-property" name="'+this.name+'" value="'+value+'">\n';
                for(var v in enumValues)
                {
                    var selected = (v === value) ? 'SELECTED' : '';
                    buff += '       <option value="'+v+'" '+selected+'> '+enumValues[v]+'   </option>\n';
                }
                buff += '   </select>\n';
                buff += '</td>\n';
            }
            else if(datatype === 'list')
            {
            //  var values = JSON.parse(value); // not sure why this does not work
                var values = eval(value);
                
                buff += '   <td>' + this.display_name+': </td>\n';
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
})(jQuery);