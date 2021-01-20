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
 * A delete helper class to provide supportive methods.
 */
(function($){
    $.PercDeleteItemHelper = {
        extractDeleteErrorMessage: extractDeleteErrorMessage
    };
    function extractDeleteErrorMessage(data, name, type){
        var id, content, canForceDelete = false,
            extractError = $.PercServiceUtils.extractDefaultErrorMessage(data),
            msgKeyBase = "perc.ui.delete" + type + "dialog.warning",
            chkBoxId = "perc_delete_" + type + "_force",
            response = data.responseText,
            matches = response.match(/User: (.*) is editing the item. Failed to delete item./);

        if (matches) {
            id = 'perc-finder-delete-error-open';
            var firstChar = type.charAt(0).toUpperCase();
            var remainder = type.substr(1);
            var label = firstChar + remainder + ": ";
            var matchesSplit = matches[1].split(" ");
            content = label + name + "<br/><br/>" +
                I18N.message(msgKeyBase + "@Open", [matchesSplit[0]]);
        }
        else
        if (response.indexOf(type + ".deleteNotAuthorized") > -1) {
            id = 'perc-finder-delete-auth';
            content = I18N.message(msgKeyBase + "@Not Authorized", [name]);
        }
        else
        if (response.indexOf(type + ".deleteTemplates") > -1) {
            id = 'perc-finder-delete-templates';
            content = I18N.message(msgKeyBase + "@Templates", [name]);
        } else if (response.indexOf(type + ".recycleFolderExists") > -1) {
            id = 'perc-finder-recycle-folder-exists';
            content = I18N.message(msgKeyBase + "@Recycle Folder Exists", [name]);
        }
        else
        if (response.indexOf(type + ".deleteApprovedPages") > -1) {
            id = 'perc-finder-delete-approved';
            content = I18N.message(msgKeyBase + "@Approved Pages", [name]) +
                "<br/><br/><input type='checkbox' id='" +
                chkBoxId +
                "' style='width:15px'/> <label class='perc_dialog_label'>" +
                I18N.message(msgKeyBase + "@Approved Pages Checkbox") +
                "</label>";
            canForceDelete = true;
        }
        else
        if (extractError != "") {
            id = 'perc-finder-delete-error-open';
            content = extractError;
        }
        else {
            id = 'perc-finder-delete-error-open';
            content = I18N.message(msgKeyBase + "@GenericText");
        }
        var result = {
            "dialogid": id,
            "content": content,
            "canForceDelete": canForceDelete,
            "chkBoxId": chkBoxId
        };
        return result;
    }

})(jQuery);
