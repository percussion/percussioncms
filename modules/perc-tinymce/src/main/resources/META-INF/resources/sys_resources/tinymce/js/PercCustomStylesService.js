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

/**
 * Service to handle custom styles in TinyMCE
 * This is deprecated as styles can now be better defined in rx_resources/tinymce/config/customer_config_override.json
 */

(function($)
{
    //Public API
    $.PercCustomStyleService = 
    {
		getCustomStyles : getCustomStyles
    };
    
    /**
     * Makes a call to the server and calls the supplied callback with status and result. See $.PercServiceUtils.makeJsonRequest
     * for more details.
     */
    function getCustomStyles(callback)
    {
		var parentJq = window.parent.jQuery;
		if(typeof parentJq.perc_paths === 'undefined'){
			callback(true, tinyMCEDefaultStyles);
			return;
		}
		var url = parentJq.perc_paths.RICHTEXT_CUSTOM_STYLES;
        parentJq.PercServiceUtils.makeJsonRequest(url,parentJq.PercServiceUtils.TYPE_GET,false,function(status, result){
			/**
			*If the status returns an error the function calls back with false and the error message generated
			*/
			if(status === parentJq.PercServiceUtils.STATUS_ERROR)
			{
				var errorMessage = parentJq.PercServiceUtils.extractDefaultErrorMessage(result.request);
				callback(false, errorMessage);
			}
			/**
			*If the status returns success, the function calls back with true and a json object created from the custom style file
			*/
			else
			{
				var styleFormats = tinyMCEDefaultStyles;
				var customStyles = {
                            title: "Custom Styles",
                            items:  []
                        };
				parentJq.each(result.data.RichTextCustomStyle, function(){
					var cf = {};
					cf.title = this.classLabel;
					cf.classes = this.className;
					/**
					Currently applies styles to span class. Will have to be altered to accommodate further style modification
					*/
					cf.inline = "span";
					customStyles.items.push(cf);
				});
				styleFormats.push(customStyles);
				callback(true, styleFormats);
			}
		});
	}
    //  made empty as should pull default styles by using "style_formats_merge": true
	var tinyMCEDefaultStyles = [];
})(jQuery);
    
