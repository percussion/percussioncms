define(function (require) {
    describe('Ping Tests', function() {
        var viewModel;
        
        beforeEach(function(done) {
            var vm = require('widgets/ping/ping.viewmodel');
            expect(vm).toBeDefined();
            
            var da = require('test/unit/mocks/adaptor.ping');
            expect(da).toBeDefined();
            
            var options = {
                dataAdaptor: da,
                pingUri: '/ping',
                isDebug: false
            };
            
            viewModel = new vm(options);
            
            viewModel.initialized.subscribe(function (statusCode) {
                expect(statusCode).toEqual(0);
                done();
            });
            
            viewModel.init();
        });

        it('ping', function (done) {
            expect(viewModel.ping()).toEqual('pong');
            done();
        });
    });
});
