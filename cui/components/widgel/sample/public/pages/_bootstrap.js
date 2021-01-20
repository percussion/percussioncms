var requireJsConfig = {
    baseUrl: '/',
    //urlArgs: "bust=build-1.2.34",
    paths: {
        'components': 'components',
        'text': 'components/requirejs-text/text',
        'jquery': 'components/jquery/dist/jquery.min',
        'jquery-ui': '/components/jquery-ui/ui/minified/jquery-ui.custom.min',
        'knockout': 'components/knockoutjs/dist/knockout',
        'widgel-base': 'components/widgel/dist/widgel-base',
        'event': 'components/widgel/dist/extras/event',
        'widgets': 'widgets',
        'modules': 'modules'
    },
    shim: {
        'knockout': { 
            exports: 'ko'
        },
		'jquery-ui': {
            exports: '$',
            deps: ['jquery']
        }
    }
}

if (typeof(exports) !== 'undefined' && exports !== null) {
	exports.requireJsConfig = requireJsConfig;
}
