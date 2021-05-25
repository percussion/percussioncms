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
 *  Contains the code related to the dialog to edit region properties.
 * This dialog allows to change the name of the region, change the css properties related to the region,
 * change the css classes assigned and the 4 main override properties: width, heigh, margin, padding.
 */
(function ($) {
    var _layoutModel = null;
    var oTableOverrides;
    var oTableAttributes;
    var tempRegion;
    var tempRegionId;
    var attributes;

    var _iframe = null;
    var _layoutFunctions = null;
    var previousRegionId = null;

    $.PercEditRegionPropertiesDialog = function () {
        var percEditRegionPropertiesDialogApi = {
            initEditRegionPropertiesDialog: initEditRegionPropertiesDialog,
            editRegionProperties: editRegionProperties
        };

        /**
         * Initializes the code for the dialog and the variables.
         * @param layoutFunctions (object) - the functions exposed in PercLayoutView.js to work with the layout view
         * @param model (object) - the template model
         * @param iframe (string) - the iframe containing the template's editor html.
         */
        function initEditRegionPropertiesDialog(layoutFunctions, model, iframe) {
            _layoutModel = model;
            _layoutFunctions = layoutFunctions;
            _iframe = iframe;
            if (!$('#perc-region-edit').data('dialog')) {
                // dialog for configuring region
                $.perc_filterField($('#perc-region-name'), $.perc_textFilters.NOSPACES);
                $.perc_filterField($('#perc-region-name'), $.perc_textFilters.ID);
                $('#perc-region-edit').perc_dialog({
                    modal: true,
                    zIndex: 100000,
                    autoOpen: false,
                    width: 500,
                    resizable: false,
                    buttons: {},
                    percButtons: {
                        "Ok": {
                            click: function () {
                                $('#perc-region-edit').dialog("close");
                            },
                            id: 'perc-region-properties-ok'
                        },
                        "Cancel": {
                            click: function () {
                                $('#perc-region-edit').dialog("close");
                            },
                            id: 'perc-region-edit-cancel'
                        }
                    }
                });
                addRegionPropertiesFieldGroups();
            }
            initRenameRegionConfirmDialog();
        }

        /**
         * Initializes the code for the rename region confirm dialog.
         * This dialog is used to confirm overriding of region css properties when
         * a rule for the new named region already exists in perc-region.css file.
         */
        function initRenameRegionConfirmDialog() {
            if (!$("#perc-region-edit-rename-confirm-dialog").data('dialog')) {
                $("#perc-region-edit-rename-confirm-dialog").perc_dialog({
                    modal: true,
                    zIndex: 100000,
                    autoOpen: false,
                    width: 500,
                    resizable: false,
                    buttons: {},
                    percButtons: {
                        "Ok": {
                            click: function () {
                                renameRegionConfirmDialogOK();
                            },
                            id: 'perc-rename-region-confirm-ok'
                        },
                        "Cancel": {
                            click: function () {
                                closeRenameRegionConfirmDialog(true);
                            },
                            id: 'perc-rename-region-confirm-cancel'
                        }
                    }
                });
            }
        }

        /**
         * Opens the region properties dialog and populates the values for the fields (if needed).
         * It also initializes the region css handler model (to perform operations for regions css changes.
         * @param region (object) - the region object (retrieved from the model) to perform operations on
         */
        function editRegionProperties(region) {
            var regionWidth = "";
            var regionHeight = "";
            var regionPadding = "";
            var regionMargin = "";
            $("#perc-css-overrides-disable").removeAttr('checked');

            _layoutModel.editRegion(region.attr('id'), function () {

                var cssClass = this.cssClass ? this.cssClass : "";
                previousRegionId = this.regionId;
                $('#perc-region-name').val(this.regionId);
                $('#perc-region-cssClass').val(cssClass);
                regionWidth = this.width;
                regionHeight = this.height;
                regionPadding = this.padding;
                regionMargin = this.margin;
                attributes = this.attributes;
                // update auto resize checkbox based on model value
                this.noAutoResize = this.noAutoResize === undefined ? false : this.noAutoResize;
                $('#perc-region-auto-resize').attr("checked", this.noAutoResize);
                // update field's disabled attribute
                updateSizeFields(this.noAutoResize);
            });

            $.PercRegionCSSHandler.init(_layoutFunctions, _layoutModel, _iframe, region);

            var percDataOverrides = [];

            if (regionWidth === "")
                regionWidth = I18N.message("perc.ui.edit.region.properties.dialog@Enter Width");
            if (regionHeight === "")
                regionHeight = I18N.message("perc.ui.edit.region.properties.dialog@Enter Height");
            if (regionPadding === "")
                regionPadding = I18N.message("perc.ui.edit.region.properties.dialog@Enter Padding");
            if (regionMargin === "")
                regionMargin = I18N.message("perc.ui.edit.region.properties.dialog@Enter Margin");
            if (!_layoutModel.isResponsiveBaseTemplate()) {
                percDataOverrides.push({rowContent: ["Width", regionWidth]});
                percDataOverrides.push({rowContent: ["Height", regionHeight]});
            }
            percDataOverrides.push({rowContent: ["Padding", regionPadding]});
            percDataOverrides.push({rowContent: ["Margin", regionMargin]});
            var percPlaceHolderValues = [];
            if (!_layoutModel.isResponsiveBaseTemplate()) {
                percPlaceHolderValues.push(["", "Enter width"]);
                percPlaceHolderValues.push(["", "Enter height"]);
            }
            percPlaceHolderValues.push(["", "Enter padding"]);
            percPlaceHolderValues.push(["", "Enter margin"]);

            var configOverrides = {
                percColumnWidths: ["300", "300"],
                percNoTableHeaders: true,
                percHeaders: ["", ""],
                percPlaceHolderValues: percPlaceHolderValues,
                percEditableCols: [false, true],
                percData: percDataOverrides,
                percShowValuesPlaceholders: true,
                percDeleteRow: false,
                percAddRowElementId: null,
                aoColumns: [{sType: "string"}, {sType: "string"}],
                bDestroy: true
            };

            oTableOverrides = $.PercInlineEditDataTable.init($('#perc-css-overrides-table'), configOverrides);
            var percAttributeData = [];

            if (attributes) {
                for (i = 0; i < attributes.length; i++) {
                    percAttributeData.push({rowContent: [attributes[i]["name"], attributes[i]["value"]]});
                }
            }

            var regionAttributesConfig = {
                percColumnWidths: ["300", "300"],
                percHeaders: ["Region HTML Attribute", "Value"],
                percEditableCols: [true, true],
                percData: percAttributeData,
                percDeleteRow: true,
                percAddRowElementId: "perc-new-attribute",
                percNewRowDefaultValues: ['', ''],
                percPlaceHolderValues: ['Enter attribute name', 'Enter attribute value'],
                aoColumns: [{sType: "string"}, {sType: "string"}],
                bDestroy: true
            };

            oTableAttributes = $.PercInlineEditDataTable.init($('#perc-region-attributes-table'), regionAttributesConfig);

            $("#perc-css-overrides-disable").on("click", function () {
                _toggleCssOverride(this.checked);
            });

            function _toggleCssOverride(state) {
                $.PercInlineEditDataTable.enableTable($('#perc-css-overrides-table'), !state);
            }

            $('#perc-region-edit').parent().find('.perc-ok').off("click").on("click", function () {
                saveRegionProperties(region);
            });
        }

        function updateSizeFields(enabled) {
            if (enabled) {
                $('#perc-region-width')
                    .attr('readonly', true)
                    .addClass("perc-field-disabled");
                $('#perc-region-height')
                    .attr('readonly', true)
                    .addClass("perc-field-disabled");
                $('#perc-region-padding')
                    .attr('readonly', true)
                    .addClass("perc-field-disabled");
                $('#perc-region-margin')
                    .attr('readonly', true)
                    .addClass("perc-field-disabled");
            } else {
                $('#perc-region-width')
                    .attr('readonly', false)
                    .removeClass("perc-field-disabled");
                $('#perc-region-height')
                    .attr('readonly', false)
                    .removeClass("perc-field-disabled");
                $('#perc-region-padding')
                    .attr('readonly', false)
                    .removeClass("perc-field-disabled");
                $('#perc-region-margin')
                    .attr('readonly', false)
                    .removeClass("perc-field-disabled");
            }
        }

        // A private helper method to group the fields and create collapsible sections
        function addRegionPropertiesFieldGroups() {
            var dialog = $('#perc-region-edit');

            if (dialog.find(".perc-section-header").length > 0)
                return;

            var fieldGroups = [
                {groupName: "perc-region-edit-properties-container", groupLabel: "Region based CSS"},
                {groupName: "perc-region-edit-attributes-container", groupLabel: "Region HTML attributes"},
                {groupName: "perc-region-edit-css-container'", groupLabel: "Use additional CSS classes"},
                {groupName: "perc-region-edit-cssoverrides-container", groupLabel: "CSS overrides"}
            ];
            $.each(fieldGroups, function (index) {
                // Create HTML markup with the groupName minimizer/maximizer and 
                // insert it before the 1st field in each group
                var minmaxClass = (index === 0) ? "perc-items-minimizer" : "perc-items-maximizer";
                var groupHtml =
                    "<div class='perc-section-header'>" +
                    "<div class='perc-section-label' groupName='" + this.groupName + "'>" +
                    "<span  class='perc-min-max " + minmaxClass + "' ></span>" + this.groupLabel +
                    "</div>" +
                    "</div>";
                dialog.find('#' + this.groupName).before(groupHtml);
                // The first group will be the only one expanded (hide all others)
                index !== 0 && dialog.find('#' + this.groupName).hide();
            });

            // Bind collapsible event
            dialog.find(".perc-section-label").off("click").on("click", function () {
                var self = $(this);
                self.find(".perc-min-max")
                    .toggleClass('perc-items-minimizer')
                    .toggleClass('perc-items-maximizer');
                dialog.find('#' + self.attr('groupName')).toggle();
                if (!(('placeholder' in $('<input>')[0] || 'placeHolder' in $('<input>')[0]))) {
                    dialog.find("#perc-region-cssClass").placeHolder({hideOnFocus: false});
                }
            });
        }

        /**
         * Closes the rename region css confirmation dialog
         * @param closeOnly (boolean) - if true closed the dialog without refreshing the iframe content.
         */
        function closeRenameRegionConfirmDialog(closeOnly) {
            $("#perc-region-edit-rename-confirm-dialog").dialog("close");
            if (!closeOnly) {
                _layoutFunctions.setLayoutDirty(true);
                _layoutFunctions.refreshRender();
            }
        }

        /**
         * Callback of the OK button for the rename region css confirmation dialog. It checks the value
         * of the radio button and performs one of the following actions:
         * - Overrides region css original rule with the new properties sent.
         * - Does nothing and leaves the current region css properties assigned to the region.
         */
        function renameRegionConfirmDialogOK() {
            var selectedVal = "replaceedits";
            var selected = $("#perc-region-edit-rename-confirm-dialog").find("input[type='radio']:checked");
            if (selected.length > 0)
                selectedValue = selected.val();

            if (selectedValue === "replaceedits") {
                $.PercRegionCSSHandler.saveRegionCSS(tempRegionId);
                saveRegionEditProperties(tempRegion, tempRegionId);
            }
            else {
                saveRegionEditProperties(tempRegion, tempRegionId);
            }
            closeRegionEditDialog(true);
            closeRenameRegionConfirmDialog(false);
        }

        /**
         * Closes the region edit dialog
         * @param closeOnly (boolean) - if true closed the dialog without refreshing the iframe content.
         */
        function closeRegionEditDialog(closeOnly) {
            $('#perc-region-edit').dialog("close");
            if (!closeOnly) {
                _layoutFunctions.setLayoutDirty(true);
                _layoutFunctions.refreshRender();
            }
        }

        /**
         * Checks the properties to be saved in the model (when closing the region edit dialog and performs the changes.
         * @param region (object) - the region object (retrieved from the model) to perform operations on
         * @param newRegionId (string) - the name of the new region (if name field was changed)
         */
        function saveRegionEditProperties(region, newRegionId) {
            _layoutModel.isResponsiveBaseTemplate() ? _saveResponsiveRegionEditProperties(region, newRegionId) : _saveBaseRegionEditProperties(region, newRegionId);
        }

        /**
         * Checks the properties to be saved in the model (when closing the region edit dialog and performs the changes.
         * @param region (object) - the region object (retrieved from the model) to perform operations on
         * @param newRegionId (string) - the name of the new region (if name field was changed)
         */
        function _saveBaseRegionEditProperties(region, newRegionId) {
            var dataTableOverrides = oTableOverrides.fnGetData();
            var dataTableAttributes = oTableAttributes.fnGetData();

            var attributes = [];
            for (i = 0; i < dataTableAttributes.length; i++) {
                var name = $(dataTableAttributes[i][0]).text().trim();
                var value = $(dataTableAttributes[i][1]).text().trim();
                if (name.length > 0) {attributes.push({name: name, value: value});}
            }

            var regionWidth = $(dataTableOverrides[0][1]).text().trim();
            var regionHeight = $(dataTableOverrides[1][1]).text().trim();
            var regionPadding = $(dataTableOverrides[2][1]).text().trim();
            var regionMargin = $(dataTableOverrides[3][1]).text().trim();

            if (regionWidth === I18N.message("perc.ui.edit.region.properties.dialog@Enter Width"))
                regionWidth = "";
            if (regionHeight === I18N.message("perc.ui.edit.region.properties.dialog@Enter Height"))
                regionHeight = "";
            if (regionPadding === I18N.message("perc.ui.edit.region.properties.dialog@Enter Padding"))
                regionPadding = "";
            if (regionMargin === I18N.message("perc.ui.edit.region.properties.dialog@Enter Margin"))
                regionMargin = "";

            if (oTableOverrides.find("td").hasClass("perc-disabled")) {
                regionWidth = "";
                regionHeight = "";
                regionPadding = "";
                regionMargin = "";
            }

            var cssClass = $('#perc-region-cssClass').val();
            var width = regionWidth;
            var height = regionHeight;
            var padding = regionPadding;
            var margin = regionMargin;
            var noAutoResize = $('#perc-region-auto-resize').is(":checked");
            noAutoResize = noAutoResize === undefined || noAutoResize === "" ? false : noAutoResize;

            // TODO: make sure that the values are valid
            // automatically convert numbers to pixels
            // convert percent to pixels here?

            _layoutModel.editRegion(region.attr('id'), function () {

                var oldClass = this.cssClass;
                /*
                Context is in dialog and does not get region dom,
                update is done when template saved and region render is done

                $("#" + region.attr('id')).removeClass(oldClass);
                $("#" + region.attr('id')).addClass(cssClass);
                for (i=0; i< attributes.length; i++)
                {
                        $("#" + region.attr('id')).attr(attributes[i][0],attributes[i][1]);
                }
                */
                this.regionId = newRegionId;
                this.width = width;
                this.cssClass = cssClass;
                this.height = height;
                this.padding = padding;
                this.margin = margin;
                this.fixed = width.toLowerCase() !== 'auto';
                this.noAutoResize = noAutoResize;
                this.attributes = attributes;
            });
        }

        /**
         * Checks the properties to be saved in the model (when closing the region edit dialog and performs the changes.
         * @param region (object) - the region object (retrieved from the model) to perform operations on
         * @param newRegionId (string) - the name of the new region (if name field was changed)
         */
        function _saveResponsiveRegionEditProperties(region, newRegionId) {
            var dataTableOverrides = oTableOverrides.fnGetData();
            var dataTableAttributes = oTableAttributes.fnGetData();
            var attributes = [];
            for (i = 0; i < dataTableAttributes.length; i++) {
                var name = $(dataTableAttributes[i][0]).text().trim();
                var value = $(dataTableAttributes[i][1]).text().trim();

                if (name.length > 0) {attributes.push({name: name, value: value});
                }
            }

            var regionPadding = $(dataTableOverrides[0][1]).text().trim();
            var regionMargin = $(dataTableOverrides[1][1]).text().trim();

            if (regionPadding === I18N.message("perc.ui.edit.region.properties.dialog@Enter Padding"))
                regionPadding = "";
            if (regionMargin === I18N.message("perc.ui.edit.region.properties.dialog@Enter Margin"))
                regionMargin = "";

            if (oTableOverrides.find("td").hasClass("perc-disabled")) {
                regionPadding = "";
                regionMargin = "";
            }

            var cssClass = $('#perc-region-cssClass').val();
            var padding = regionPadding;
            var margin = regionMargin;
            var noAutoResize = $('#perc-region-auto-resize').is(":checked");
            noAutoResize = noAutoResize === undefined || noAutoResize === "" ? false : noAutoResize;

            // TODO: make sure that the values are valid
            // automatically convert numbers to pixels
            // convert percent to pixels here?

            _layoutModel.editRegion(region.attr('id'), function () {

                var oldClass = this.cssClass;
                $("#" + region.attr('id')).removeClass(oldClass);
                $("#" + region.attr('id')).addClass(cssClass);
                this.regionId = newRegionId;
                this.cssClass = cssClass;
                this.padding = padding;
                this.margin = margin;
                this.noAutoResize = noAutoResize;
                this.attributes = attributes;
            });
        }

        /**
         * Saving region properties to the model when region dialog is dismissed
         * @param region (object) - the region object (retrieved from the model) to perform operations on
         */
        function saveRegionProperties(region) {
            var id = $('#perc-region-name').val();

            // verify that the region ID is not blank
            if (id === "") {
                var message = "Region Name can not be blank.";
                $.perc_utils.alert_dialog({title: 'Region Name Error', content: message, width: '595px'});
                return;
            }

            // to fix bug cms#2947 code start here
            var dataTableAttributes = oTableAttributes.fnGetData();
            var invalidAttributeMessage='';
            for (i = 0; i < dataTableAttributes.length; i++) {
                var name = $(dataTableAttributes[i][0]).text().trim().toLowerCase();
                if(name==='id' || name==='title' || name==='href' || name==='name'){
                    invalidAttributeMessage += invalidAttributeMessage.length===0?name+' ':','+name+' ';
                }
            }
            if(invalidAttributeMessage){
                var message = "Invalid attributes found, Cannot use reserved attribute(s) : "+invalidAttributeMessage;
                $.perc_utils.alert_dialog({title: 'Invalid Attributes Error', content: message, width: '595px'});
                return;
            }
            // end cms#2947
            // verify that the region ID has changed and that it is not already in use
            if (previousRegionId !== id && _layoutModel.containsRegionId(id)) {
                // if id is already in use, alert user and return without updating region properties
                var message = "Region Name '" + id + "' is already in use.\nPlease use another name.";
                $.perc_utils.alert_dialog({title: 'Region Name Already Exists', content: message, width: '595px'});
                return;
            }

            if (previousRegionId !== id) {
                $.PercRegionCSSHandler.getRegionCSS(id, renameCheckCallback);
            }
            else {
                $.PercRegionCSSHandler.saveRegionCSS(id);
                saveRegionEditProperties(region, id);
                closeRegionEditDialog(false);
            }

            function renameCheckCallback(status, data, id) {
                if (status === $.PercServiceUtils.STATUS_SUCCESS) {
                    if (data.RegionCSS.properties != null) {
                        tempRegion = region;
                        tempRegionId = id;
                        $("#perc-region-edit-rename-confirm-dialog-id-label").text(id);
                        $("#perc-region-edit-rename-confirm-dialog").dialog('open');
                        return;
                    }
                    else {
                        $.PercRegionCSSHandler.saveRegionCSS(id);
                        saveRegionEditProperties(region, id);
                        closeRegionEditDialog(false);
                    }
                }
                else {
                    $.PercRegionCSSHandler.saveRegionCSS(id);
                    saveRegionEditProperties(region, id);
                    closeRegionEditDialog(false);
                }
            }
        }

        return percEditRegionPropertiesDialogApi;
    };

})(jQuery);
