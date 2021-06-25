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
    
