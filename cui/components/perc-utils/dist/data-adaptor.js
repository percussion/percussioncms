/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
        options.dataType = (options.dataType == 'undefined') ? undefined : options.dataType;
        
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
