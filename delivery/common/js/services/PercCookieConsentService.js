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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * PercCookieConsentService.js, posts consent entries to the DTS meta data service.
 */
(function($)
{
    $.PercCookieConsentService = {
        postConsentEntry : postConsentEntry
    };
    
    function postConsentEntry(opts, deliveryUrl, callback)
    {
        let serviceUrl = $.PercServiceUtils.joinURL(deliveryUrl, "/perc-metadata-services/metadata/consent/log");

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS) {
                console.debug("Success saving cookie consent entry.");
                callback(status);
            }
            else {
                console.error("Error saving cookie consent entry.");
                callback(status);
            }
        }, {siteName : opts.siteName, services : opts.services, optIn : opts.optIn, consentDate: opts.consentDate});
    }
})(jQuery);
