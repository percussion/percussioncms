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

define(['jquery'], function($) {

    /**
     * Make an HTTP request.
     * @param {object} options - A set of key/value pairs that configure the request.
     *                           {string} url - A string containing the URL to which the request is sent.
     *                           {string} httpMethod - HTTP request type ('GET', 'POST', etc.).
     *                           {object} data - Data to be sent to the server.
     *                           {string} dataType - The type of data that you're expecting back from the server.
     *                           {string} auth - Authorization token.
     *                           {string} authHeaderName - Name to use in the header for the auth token (default 'Authorization')
     *                           {bool} debug - Log debug messages.
     * @returns {object} - A jQuery XMLHttpRequest (see http://api.jquery.com/jQuery.ajax/)
     */
    var request = function(options) {
        options.dataType = options.dataType || 'json';
        options.dataType = (options.dataType === 'undefined') ? undefined : options.dataType;
        
        var req = $.ajax({
            url: options.url,
            type: options.httpMethod,
            dataType: options.dataType,
            data: options.data,
            beforeSend: function (jqXHR) {
                if (options.auth) {
                    var name = options.authHeaderName || 'Authorization';
                    jqXHR.setRequestHeader(name, options.auth);
                }
            }
        });
        
        if (options.debug) {
            req.done(function (data, textStatus, jqXHR) {
                console.log(options.httpMethod + ' ' + options.url + ' DONE ' + jqXHR.status);
                console.log('> ' + JSON.stringify(data));
            })
            .fail(function (jqXHR, textstatus, errorThrown) {
                console.log(options.httpMethod + ' ' + options.url + ' FAIL ' + jqXHR.status);
            })
            .always(function (data_jqXHR, textStatus, jqXHR_errorThrown) {
                console.log(options.httpMethod + ' ' + options.url + ' COMPLETE');
            });
        }
        
        return req;
    };

    return {
        request: request
    };
});
