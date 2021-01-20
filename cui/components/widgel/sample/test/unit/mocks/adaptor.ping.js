define(['jquery'], function($) {
    /**
     * Make an HTTP request.
     * @param {object} options - A set of key/value pairs that configure the request.
     *                           {string} url - A string containing the URL to which the request is sent.
     *                           {string} httpMethod - HTTP request type ('GET', 'POST', etc.).
     *                           {object} data - Data to be sent to the server.
     *                           {string} dataType - The type of data that you're expecting back from the server.
     *                           {string} auth - Authorization token.
     *                           {bool} debug - Log debug messages.
     * @returns {object} - The jQuery XMLHttpRequest (see http://api.jquery.com/jQuery.ajax/)
     */
    var request = function(options) {
        var req = $.Deferred();
        
        setTimeout(function () {
            var result = { ping: 'pong' };
            var jqXHR = { status: 200 };
            req.resolve(result, 'OK', jqXHR);
        }, 500);
        
        return req;
    };
    
    return {
        request: request
    };
});
