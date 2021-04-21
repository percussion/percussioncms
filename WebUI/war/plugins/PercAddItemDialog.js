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
 * PercAddItemDialog.js
 * @author Luis Mendez
 *
 * Show a dialog with a items list with Checkbox to be selected.
 */
(function($){
    //Public API
    $.PercAddItemDialog = {
        open: openDialog
    };

    /*
    * @buttonSave boolean, determinate if we want show the button "Save" instead button "Ok"
    * @itemList string array, ["item1","item2","item3","item4"]
    * @dialogTitle Title for the dialog box.
    * @callback function that will be called with an string array of the selected items
    */
    function openDialog(buttonSave, itemList, dialogTitle, callback)
    {
        // Build an absolute URL as a workaround for IE issue (popup security warning)
        // See details at http://support.microsoft.com/kb/925014/en-us?fr=1
        var baseUrl = window.location.protocol + "//" + window.location.host + "/cm";
        var dialog = null;
        var dialogHTML = createDialog(itemList);
        var buttons = { };
        var buttonSaveOk = {
            click: function(){save(dialog, callback);},
            id: "perc-addItem-dialog-save"
        };
        var buttonCancel = {
            click: function(){
                dialog.remove();
            },
            id: "perc-addItem-dialog-cancel"
        };

        if(buttonSave) {
            buttons.Save = buttonSaveOk;
        } else {
            buttons.Ok = buttonSaveOk;
        }

        buttons.Cancel = buttonCancel;

        dialog = $(dialogHTML).perc_dialog( {
            resizable : false,
            title: dialogTitle,
            modal: true,
            closeOnEscape : true,
            percButtons: buttons,
            id: "perc-addItem-dialog",
            open: function(){
                // var backgroundImage = "url(" + baseUrl + "/images/images/buttonSaveInactive.png)"
                // if (!buttonSave)
                //     backgroundImage = "url(" + baseUrl + "/images/images/buttonOkInactive.png)"
                $("#perc-addItem-dialog-save")
                    .off('click')
                    .off('mouseenter mouseleave');
                //  .css("background-image", backgroundImage)
                $(this).find('input').click(function(event){enableDisableSaveButton(buttonSave, dialog, callback); event.stopPropagation(); });
                $(this).find('.perc-item-entry').click(function(event){clickRow(event, buttonSave, dialog, callback);});
            },
            width: 400
        });

        //Dialog Html
        function createDialog(itemList){
            var htmlList =   $('<div class="perc-multicheck-list" />')
                .append($('<div class="perc-items-container" />'));
            var container = htmlList.find('.perc-items-container');
            for(i in itemList) {
                var itemName = itemList[i];
                var html = $("<div class='perc-item-entry'/>")
                    .append(
                        $("<input type='checkbox'/>")
                            .val(itemName)
                    )
                    .append(
                        $("<span />")
                            .html(itemName)
                            .attr("title", itemName)
                    );
                // append html to DOM
                container.append(html);
            }
            var dialogHtml = $('<div/>').append(htmlList);
            return dialogHtml;
        }

        function clickRow(event, buttonSave, dialog, callback){
            $(event.currentTarget).find('input').attr('checked', !$(event.currentTarget).find('input').attr('checked') );
            enableDisableSaveButton(buttonSave, dialog, callback)
        }

        //Enable the "Save"/"Ok" button only if we select at least one item.
        function enableDisableSaveButton(buttonSave, dialog, callback){

            // Build an absolute URL as a workaround for IE issue (popup security warning)
            // See details at http://support.microsoft.com/kb/925014/en-us?fr=1
            var baseUrl = window.location.protocol + "//" + window.location.host + "/cm";

            var buttonSaveOk = $("#perc-addItem-dialog-save");

            if ($(".perc-multicheck-list input:checked").length > 0){
                buttonSaveOk
                    .off('click')
                    .click(function(){save(dialog, callback);});

            }
            else {
                buttonSaveOk
                    .off('click')
                    .off('mouseenter mouseleave');
            }
        }

        function save(dialog, callback){
            var selectedItems = getItemsSelected();
            if (selectedItems.length > 0 && typeof(callback) === "function") {
                callback(selectedItems);
            }
            dialog.remove();
        }

        function getItemsSelected(){
            var ItemsSelected = [];
            $(".perc-multicheck-list input:checked").each(function(){
                ItemsSelected.push($(this).val());
            });
            return ItemsSelected;
        }


    }// End open dialog
})(jQuery);