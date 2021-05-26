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

(function($){
    var TABLE_STATUS_FOOTER_PADDING_TOP = 5;
    var itemsPerPage = 5;

    // grab necessary Perc APIs
    var PercPathService  = percJQuery.PercPathService;
    var PercServiceUtils = percJQuery.PercServiceUtils;
    var mdService         = percJQuery.PercMetadataService;
    var perc_utils       = percJQuery.perc_utils;
    var perc_paths       = percJQuery.perc_paths;
    var perc_filters     = percJQuery.perc_textFilters;
    
    var isLargeColumn = true;       // if gadget is on the right side (large column)
    var statusTable;
    var tableDiv;
    var globalVarData = null;
    // API for this library
    $.fn.PercGlobalVariablesGadget = function(data, rows) {
        globalVarData = data;
        // never show a scrollbar in the gadget
        $("body").css("overflow","hidden");
        // resize gadget to fit the rows
        itemsPerPage = rows;
        tableDiv = $("#perc-global-variables-table");
        $("#perc-add-variable-button").on("click", function(){
            _editVariable();
        });
        _renderGlobalVariablesTable(data);

    };

    /**
     * Edit variable method called on either double click of the row or edit menu.
     * @TODO change the dialog styles as per the style guide
     */
    function _editVariable(event)
    {
        var origName = "";
        var rowData = {varName:"",varValue:""};
        if(event)
        {
            rowData = event.data;
            origName = rowData.varName;
        }
        var readOnly = origName==""?"":"readonly=\"readonly\"";
        var readOnlyStyle = origName==""?"":"background-color:#E6E6E9; border: none; font-weight:bold; overflow:hidden; text-overflow:ellipsis;";
        var dialogHtml = '<div><div id="perc-name-label"><label for="perc_variable_name">Name:</label></div>' +
                         '<div><input type="text" style="width: 300px;'+ readOnlyStyle +'" id="perc_variable_name" maxlength="110" name="perc_variable_name" value="' + rowData.varName + '"' + readOnly + '></div>' +
                         '<div id="perc-value-label"><label for="perc_variable_value">Value:</label></div>' +
                         '<div><input type="text" style="width: 300px;" id="perc_variable_value" maxlength="512" name="perc_variable_value"></div>';
        var dialogWidth = 700;
        var dialogHeight = 300;
        var dialog = percJQuery(dialogHtml).perc_dialog({
            title: origName==""?"Add New Global Variable":"Edit Global Variable Value",
            buttons: {},
            percButtons:{
                "Save":{
                    click: function(){
                        var varName = dialog.find("#perc_variable_name").val();
                        var varValue = dialog.find("#perc_variable_value").val();
                        _saveVariable(origName, varName, varValue, dialog);
                    },
                    id: "perc-edit-global-variables-save"
                },
                "Cancel":{
                    click: function(){
                        dialog.remove();
                    },
                    id: "perc-edit-global-variables-cancel"
                }
            },
            open:function(){percJQuery(this).find("#perc_variable_value").val(rowData.varValue);},
            id: "perc-edit-global-variables-dialog",
            width: dialogWidth,
            modal: true
        });
        // Filter the input fields to accept only URL friendly characters
        percJQuery.perc_filterField(dialog.find("#perc_variable_name"), perc_filters.ID_WITH_SPACE);
    }

    /**
     * Method to save the variable.
     * @TODO Change the validation from alerts to inline validation...
     */
    function _saveVariable(origName, varName, varValue, dialog)
    {
        if(percJQuery.trim(varName) == "")
        {
            displayErrorMessage(dialog, "Name must not be blank.", "#perc_variable_name");
            return;
        }
        
        if(percJQuery.trim(varValue) == "")
        {
            displayErrorMessage(dialog, "Value must not be blank.", "#perc_variable_value");
            return;
        }
        
        var varData = {};
        if(!globalVarData)
        {
             varData[varName] = varValue;
        }
        else
        {
            var varDataStr = globalVarData.metadata.data;
            varData = JSON.parse(varDataStr);
            if(origName === "")
            {
                if(varData[varName])
                {
                    displayErrorMessage(dialog, "A variable already exists with this name, please select a different name.", "#perc_variable_name");
                    return;
                }
                varData[varName] = varValue;
            }
            else
            {
                varData[varName] = varValue;
            }
        }

        mdService.saveGlobalVariables("percglobalvariables", varData, function(status, result){
            if(status == PercServiceUtils.STATUS_SUCCESS)
            {
                dialog.remove();
                _expandNotify();
            }
            else
            {
                alertDialog("Error", "Error saving the variable.");
            }
        });
        dialog.remove();
    }

    /**
     * Deletes the variable with a confirmation.
     */
    function _deleteVariable(event)
    {
        var rowData = event.data;
        var options = {title: "Delete Global Variable",
            question:"Are you sure you want to delete the global variable '" + rowData.varName + "'?",
            type:"OK_CANCEL",
            cancel:function(){},
            success:function(){
                    var varDataStr = globalVarData.metadata.data;
                    var varData = JSON.parse(varDataStr);
                    delete varData[rowData.varName];
                    mdService.saveGlobalVariables("percglobalvariables", varData, function(status, result){
                        if(status === PercServiceUtils.STATUS_SUCCESS)
                        {
                            _expandNotify();
                        }
                        else
                        {
                            alertDialog("Error", "Error deleting the variable.");
                        }
                    });
                }
        };
        perc_utils.confirm_dialog(options);
    }
    
    /**
     * Method to render the global variable data. Data is expected to be an object of name value pairs.
     */
    function _renderGlobalVariablesTable(data)
    {
        $.PercGlobalVariableActions = { title : "", menuItemsAlign : "left", stayInsideOf : ".dataTables_wrapper",
                items : [
                    {label : "Edit",  callback : _editVariable},
                    {label : "Delete",   callback : _deleteVariable}
        ]};
        var menus = [];
        var dataRows = [];
        if(data)
        {
            var varDataStr = data.metadata.data;
            var varData = JSON.parse(varDataStr);
            $.each(varData, function(name, value){
                var dataRow = [];
                dataRow.push([{content : name, title : name}]);
                dataRow.push([{content : value, title : value}]);
                var row = {rowData:{varName:name,varValue:value},rowContent:dataRow};
                dataRows.push(row);
                menus.push($.PercGlobalVariableActions);                
            });
        }
    



        var config = {
            percRowDblclickCallback : _editVariable, 
            percStayBelow : "#perc-add-variable-button",
            percColumnWidths: ["150", "*"],
            percVisibleColumns: null,
            bPaginate: true,
            percData: dataRows,
            iDisplayLength : itemsPerPage,
            percHeaders: ["Variable name", "Variable value"],
            aoColumns: [{
                sType: "string"
            }, {
                sType: "string"
            }],
            percMenus : menus,
            oLanguage: {
                sZeroRecords: "No Global Variables Found",
                oPaginate: {
                    sFirst: "&lt;&lt;",
                    sPrevious: "&lt;",
                    sNext: "&gt;",
                    sLast: "&gt;&gt;"
                },
                sInfo: " ",
                sInfoEmpty: " "
            }
        };
        miniMsg.dismissMessage(loadingMsg);
                
        tableDiv.PercActionDataTable(config);
    }
    
     /**
     * Method to add an error message for the input field associated to the variable name.
     */
    function displayErrorMessage(dialog, message, inputField)
    {
        dialog.find("label[class='perc_field_error']").remove();
        var errorLabelHtml = "<label style='display: block;' class='perc_field_error'>" + message + "</label>";
        dialog.find(inputField).after(errorLabelHtml);
    }
    
    /**
     * Method to display a popup dialog with the suppplied message.
     */
    function alertDialog(title, message, w) 
    {
        if(w == null || w == undefined || w == "" || w < 1)
            w = 400;
        perc_utils.alert_dialog({title : title, content : message, width : w});
    }

})(jQuery);