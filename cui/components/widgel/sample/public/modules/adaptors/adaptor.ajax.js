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
        options.dataType = options.dataType || 'json';
        options.dataType = (options.dataType == 'undefined') ? undefined : options.dataType;
        
        var jqxhr = $.ajax({
            url: options.url,
            type: options.httpMethod,
            dataType: options.dataType,
            data: options.data,
            beforeSend: function (jqXHR) {
                if (options.auth) {
                    jqXHR.setRequestHeader('Authorization', options.auth);
                }
            }
        })
        .done(function (data, textStatus, jqXHR) {
            if (options.debug) {
                console.log(options.httpMethod + ' ' + options.url + ' DONE ' + jqXHR.status);
                console.log(JSON.stringify(data));
            }
        })
        .fail(function (jqXHR, textstatus, errorThrown) {
            if (options.debug) {
                console.log(options.httpMethod + ' ' + options.url + ' FAIL ' + jqXHR.status);
            }
        })
        .always(function (data_jqXHR, textStatus, jqXHR_errorThrown) {
            if (options.debug) {
                console.log(options.httpMethod + ' ' + options.url + ' COMPLETE');
            }
        });
        
        return jqxhr;
    };

    return {
        request: request
    };
});
