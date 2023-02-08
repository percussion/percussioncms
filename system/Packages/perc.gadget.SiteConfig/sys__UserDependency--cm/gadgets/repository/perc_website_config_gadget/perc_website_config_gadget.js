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
