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

var system = require('system');
if (system.args.length < 5 ) {
    console.log('Usage: phantomjs.exe webcap.js <url> <imagePath> <width> <height> [<userAgent>]');
    phantom.exit();
}

var page = require('webpage').create();
page.viewportSize = { width: system.args[3], height: system.args[4] };
page.clipRect = { top: 0, left: 0, width: system.args[3], height: system.args[4] };

if (system.args.length === 6) {
    console.log('user agent: ' + system.args[5]);
    page.settings.userAgent = system.args[5];
}

page.onLoadFinished = function(status) {
    console.log('Status: ' + status);
    console.log('Rendering image: ' + system.args[2]);
    page.evaluate(function() {
        var head = document.querySelector('head');
        style = document.createElement('style');
        text = document.createTextNode('body { background: #fff }');
        style.setAttribute('type', 'text/css');
        style.appendChild(text);
        head.insertBefore(style, head.firstChild);
    });
    page.render(system.args[2]);
    phantom.exit();
};


console.log('Opening page: ' + system.args[1]);
page.open(system.args[1], function (status) {
    if (status !== 'success') {
        console.log('Unable to load the address!');
        phantom.exit();
    }
});