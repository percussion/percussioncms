define(['knockout'], function(ko) {
    return function PingViewModel(options) {
        var self = this;
        
        // #region public members
        self.initialized = ko.observable('');
        self.ping = ko.observable('');
        self.isDebug = ko.observable(options.isDebug);
        self.ready = ko.computed(function () {
            return self.ping().length == 0 ? false : true;
        }, self);
        
        /**
         * Initialize the view model.
         */
        self.init = function() {
            if (self.isDebug()) console.log('config pingUri : ' + options.pingUri);
            
            var reqopt = {
                url: options.pingUri,
                httpMethod: 'GET',
                dataType: 'json',
                debug: options.isDebug
            };
            
            options.dataAdaptor
                .request(reqopt)
                .done(function (data, textStatus, jqXHR) {
                    self.ping(data.ping);
                    self.initialized(0);
                })
                .fail(function (jqXhr, textstatus, errorThrown) {
                    self.initialized(jqXhr.status);
                });
        }
    }
});