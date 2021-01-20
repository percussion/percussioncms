/**
 * Module dependencies.
 */

var express = require('express'),
    http = require('http'),
    url = require('url'),
    path = require('path');
    
var app = express();
app.configure(function() {
    app.set('port', process.env.PORT || 2342);
    app.set('views', __dirname + '/public/pages');
    app.set('view engine', 'jade');
    app.use(express.favicon());
    app.use(express.logger('dev'));
    app.use(express.bodyParser({ limit: 1024 * 1024 * 5 }));    
    app.use(express.methodOverride());
    app.use(app.router);
    app.use(express.static(path.join(__dirname, 'public')));
});

app.configure('development', function() {
    app.use(express.errorHandler());
});

// Configure routes
app.get('/', function (req, res, next) {
    var query = url.parse(req.url, true).query;
    res.render('ping', {
        title: 'Ping',
        pingUri: 'http://localhost:' + app.get('port') + '/ping',
        isDebug: query.debug
    });
});
app.get('/ping', function (req, res, next) {
    var result = { ping: 'pong' };
    res.send(result);
});

// Start the server
http.createServer(app).listen(app.get('port'), function() {
    console.log('Express server listening on port ' + app.get('port'));
});
