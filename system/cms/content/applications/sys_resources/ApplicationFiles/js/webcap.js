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
console.log('Opening page: ' + system.args[1]);
page.open(system.args[1], function (status) {
    if (status !== 'success') {
        console.log('Unable to load the address!');
        phantom.exit();
    } else {
        window.setTimeout(function () {
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
 		}, 5000);
    }
});