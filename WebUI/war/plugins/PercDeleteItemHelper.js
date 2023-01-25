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
        if (extractError !== "") {
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
