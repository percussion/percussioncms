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
