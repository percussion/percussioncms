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

(function($) {
    // Module public API
    $.perc_social_config_gadget = {
        showConfigurationWindow: showConfigurationWindow
    };

    var _dialog;

    function showConfigurationWindow(callback) {
        $.PercLicenseService.getModuleLicense('REDIRECT', function(status, data) {
            if (status) {
                var clientIdentity = {
                    id: data.moduleLicense.key,
                    type: 'CM1',
                    token: data.moduleLicense.handshake
                };

                var url = data.moduleLicense.uiProvider + '/configure/?auth=' + JSON.stringify(clientIdentity);

                var dialogContent = "<div>";
                dialogContent += "  <iframe id='perc-website-config-frame' src='" + url + "' frameborder='no' style='width: 100%; height: 100%'>";
                dialogContent += "    <p>Your browser doesn't support iframes.</p>";
                dialogContent += "  </iframe>";
                dialogContent += "</div>";
                dialogContent = percJQuery(dialogContent);

                _dialog = dialogContent.perc_dialog({
                    'id' : 'perc-website-config-gadget-dialog',
                    'dialogClass': 'perc-website-config-gadget-frame',
                    'width': 1020,
                    'height': 696,
                    'resizable': true,
                    'modal': true,
                    'title' : 'Website Configuration',
                    close: function () {
                        _dialog.empty();
                        _dialog.remove();
                        _dialog = undefined;
                        parent.window.removeEventListener('message', messageEventHandler);
                    }
                });

                percJQuery('.perc-website-config-gadget-frame').css('padding', '0');
                var widget = percJQuery('.perc-website-config-gadget-frame').dialog('widget');
                widget.find('.ui-dialog-titlebar').css('display', 'none');
                widget.find('.ui-dialog-content').css('padding', '0');

                parent.window.addEventListener('message', messageEventHandler, false);
            }

            callback(status);
        });
    }

    function getSites(callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SITES_ALL + '/?includePubInfo=true',
            $.PercServiceUtils.TYPE_GET,
            false,
            function (status, result) {
                var success = (status == $.PercServiceUtils.STATUS_SUCCESS);
                var data = [];
                if (success) {
                    data = result.data.SiteSummary;
                }
                callback(success, data);
            }
        );
    }

    // send a message to the current iframe
    var sendMessageToIFrame = function (frameId, module, message) {
        var $frame = percJQuery('#' + frameId);
        if ($frame.length > 0) {
            // send payload according to spec: percussion.module.stuff,
            var payload = {
                percussion: { }
            };
            payload.percussion[module] = message;

            // send the payload to iframe
            var win = $frame[0].contentWindow;
            if (win) {
                win.postMessage(payload, "*");
            }
        }
    };

    function messageEventHandler(evt) {
        console.log('gotEvent ', evt.data);

        if (_dialog &&
            evt.data.percussion &&
            evt.data.percussion.redirect) {

            var redirect = evt.data.percussion.redirect;
            if (redirect.cmd === 'sites-req') {
                getSites(function (success, sites) {
                    if (success) {
                        var result = {
                            type: 'sites-resp',
                            data: sites
                        };
                        sendMessageToIFrame('perc-website-config-frame', 'redirect', result);
                    }
                });
            }
            else if (redirect.cmd === 'close') {
                _dialog.dialog('close');
            }
        }
    }
})(jQuery);
