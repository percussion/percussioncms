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
