// assumed dependencies:
//  - require.js
//  - jquery

define (
    // prerequisites:
    [
        'text!widgets/ping/ping.html',
        'widgets/ping/ping.viewmodel',
        'widgel-base'
    ],
    // module:
    function (defaultView, defaultViewModel) {
        var widgetName = 'ui.ping';
        $.widGEL.baseWidget.injectCssFile('widgets.ping', requirejs.toUrl('widgets/ping/ping.css'));
        
        $.widget(widgetName, $.widGEL.baseWidget, {
            options: {
                view: defaultView,
                viewModel: defaultViewModel,
                dataAdaptor: null,  // required
                pingUri: null,      // required
                isDebug: false
            },
            
            destroy: function () { },
            
            // non-private properties of the ViewModel are part of the public widget API.
            // see Widgets/Shared/BaseWidget.Model for external usage
            _createDefaultViewModel: function () {
                var thisWidget = this;                
                var viewModel = new thisWidget.options.viewModel(thisWidget.options);
                return viewModel;
            },
            
            _create: function () {
                var thisWidget = this;
                thisWidget._checkRequiredOptions(widgetName, thisWidget.options,
                    [
                        'dataAdaptor', 'pingUri'
                    ]);
                thisWidget._initModelView();
                
                setTimeout(function () {
                    var viewModel = thisWidget._viewModel;
                    viewModel.initialized.subscribe(function (statusCode) {
                        if (statusCode == 0) {
                            console.log('viewModel.init success!');
                        }
                        else {
                            console.log('viewModel.init failed: ' + statusCode);
                        }
                    });
                    
                    viewModel.init();
                }, 10);
            }
        });
        
        return "SUCCESS: " + widgetName + " Widget Registered.";
    }
);
