function createUri(options) {
    var host = 'http://localhost:2342';
    if (options.host) {
        host = options.host;
    }
    
    return host;
}

casper.test.begin('Ping Pong', 2, function suite(test) {
    var uri = createUri(casper.cli.options);
    test.comment(uri);
    
    // Load the URI
    casper.start(uri);
    
    // Wait until the .result block is visible (this means the view model is ready)
    casper.waitUntilVisible('.result', function then() {
        test.assertExists('span.result');
        test.assert(this.fetchText('span.result').length > 0, 'Ping response not empty');
    }, function timeout() {
        test.fail('Timeout loading ping content');
    });
    
    casper.run(function() {
        test.done();
    });
});
