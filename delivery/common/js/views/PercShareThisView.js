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

(function($) {

	function initShareThisWidget() {
		// Required for sharethis.com
		var switchTo5x = true;
		// Public key for sharethis.com
		stLight.options({
			publisher : '9b8ea0ea-c2c0-4a43-a107-df0615eaa6eb'
		});

		// Facebook
		$('.perc-share-this-widget-facebook-button').addClass(
				'st_facebook_custom');

		// Twitter
		$('.perc-share-this-widget-twitter-button').addClass(
				'st_twitter_custom');

		// Digg
		$('.perc-share-this-widget-digg-button').addClass(
				'st_digg_custom');
	}

	$(document).ready(function() {
		if(0 < $(".perc-share-this-widget").length){
		$("head").append("<script src='/web_resources/cm/jslib/shareThis.js' type='text/javascript'></script>");
		initShareThisWidget();
		}
	});
})(jQuery);
