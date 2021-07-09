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
 * define the template manager functions, to interface with services on the server side.
 * currently only CSS stuff in here atm. -thom
 *
 * These are mostly AJAX calls, with two callbacks for success and failure respectively.
 */


// This is an XML serializer which uses the Browser's XML functions to take a 
// DOM object, and subsequently spits out the resulting XML document for a 
// POST request.
jQuery.fn.toXML = function () {
    var out = '';
    if (this.length > 0) {
        if (typeof XMLSerializer == 'function' ||
            typeof XMLSerializer == 'object')
        {
            var xs = new XMLSerializer();
            this.each(function() {
                out += xs.serializeToString(this);
            });
        } else if (this[0].xml !== undefined) {
            this.each(function() {
                out += this.xml;
            });
        } else {
            // TODO: Manually serialize DOM here,
            // for browsers that support neither
            // of two methods above.
        }
    }
    return out;
};

(function($) {

    /**
     * get_template_css - Get the CSS Override for a given Template ID
     *
     * @var string templateID The Template ID
     *
     * @var function k The callback for successful retrieval.
     *
     * @var function err The callback for error.
     */
    function get_template_css( templateID, k, err )
    {
	var $cssOverride;
	var $templateData;
	if (!$.perc_fakes.template_service.get_template_css)
	    {
		// Actually do it
		$.ajax({
			headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
			url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateID,
			    type: "GET",
			    success: function(data, textstatus)
			    {
				$templateObject = $(data);
				
				$cssOverride = $templateObject.find("Template cssOverride");
				k($cssOverride.text());

			    },
			    error: function(request, textstatus, error)
			    {
				err(error);
			    }
		    });
	    }
	else
	    {
		// Send back fake data instead.
	    }

    }

    /**
     * set_template_theme - Set the template theme for a given template
     *
     * @var templateID The Template ID
     *
     * @var function k The callback for successful retrieval.
     *
     * @var function err The Callback for error.
     */

    function set_template_theme(templateID, newTheme, k, err, update_preview, preview_only)
    {

	var $templateObject;
	var $cssTheme;

	if (!$.perc_fakes.template_service.set_template_css)
	    {
		// Actually do it. Grab the template object first.... 
	       
		$.ajax({
			headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
			url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateID,
			    type: "GET",
			    success: function(data, textstatus)
			    {
				

				$templateObject = $(data);
				$cssTheme = $templateObject.find("Template theme");
			        
				if ($cssTheme.length === 0) // There is no CSS override, make one.
				    {
					$templateObject.append("<theme>"+newCSS+"</theme>");
				    } 
				else
				    {
					$cssTheme.text(newTheme);
				    }

                               update_preview( $templateObject.toXML() );
                               if( !preview_only ) {
				$.ajax({
					 headers: { 
						'Accept': 'application/xml',
						'Content-Type': 'application/xml' 
					},
					url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/",
					    type: "POST",
					    data: $templateObject.toXML(),
					    processData: false,
					    success: function(data, textstatus)
					    {
						k(newTheme);
					    },
					    error: function(data, textstatus)
					    {
						err(textstatus);
					    }
				    });
                               }

			    },
			    error: function(request, textstatus, error)
			    {
				alert(I18N.message("perc.ui.template.manager@Error Grabbing Template"));
			    }
		    });

	        

	    }

    }

    /**
     * get_theme_list - Get the list of available themes in the installation.
     *
     * @var k - The callback for Success
     *
     * @var err - The callback for error.
     */

    function get_theme_list(k, err)
    {
	var $themeSummaryObject;
	
	if (!$.perc_fakes.template_service.get_template_css)
	    {
		// Actually do it
		$.ajax({
			headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
			url: $.perc_paths.THEME_SUMMARY_ALL, // replace with constant.
			    type: "GET",
			    success: function(data, textstatus)
			    {
				$templateObject = $(data);
				
				$themeSummaryObjects = $templateObject.find("ThemeSummarys ThemeSummary");
			        k($themeSummaryObjects);				
			      
			    },
			    error: function(request, textstatus, error)
			    {
				err(error);
			    }
		    });
	    }
	else
	    {
		// Send back fake data instead.
	    }
	
    }	 

    /**
     * get_theme_css_data - Get the CSS for a given theme
     *
     * @var string themeName - The theme name from which to get its CSS.
     *
     * @var callback k - The Callback to call on success.
     *
     * @var callback err - The callback to call on error.
     *
     */
    function get_theme_css_data(themeName, k, err)
    {

	var $dataObject;
	
	if (!$.perc_fakes.template_service.get_template_css)
	    {
		// Actually do it
		$.ajax({
			headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
			url: $.perc_paths.THEME_CSS+"/"+themeName, // replace with constant.
			    type: "GET",
			    success: function(data, textstatus)
			    {
				$dataObject = $(data);
				
				$themeCSS = $dataObject.find("ThemeCSS CSS");
				// console.log($themeCSS);
			        k($themeCSS);				
			      
			    },
			    error: function(request, textstatus, error)
			    {
				err(error);
			    }
		    });
	    }
	else
	    {
		// Send back fake data instead.
	    }
	
	
    }

    /**
     * set_template_css - Set the CSS Override stylesheet for a given template.
     *
     * @var string templateID - The Internal Template ID to change override for
     * 
     * @var string newCSS - The CSS text, which is used to replace any old CSS text in
     *               the template object.
     *
     * @var function k - The Callback for success
     * 
     * @var function err - The Callback for error.
     */
    function set_template_css( templateID, newCSS, k, err, update_preview, preview_only )
    {
	
	var $templateObject;
	var $cssOverride;

	if (!$.perc_fakes.template_service.set_template_css)
	    {
		// Actually do it. Grab the template object first.... 
	       
		$.ajax({
			headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
			url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateID,
			    type: "GET",
			    success: function(data, textstatus)
			    {
				
				// console.log(data);

				$templateObject = $(data);
				$cssOverride = $templateObject.find("Template cssOverride");

				// console.log($cssOverride.length);
				// console.log($cssOverride);
			        
				if ($cssOverride.length === 0) // There is no CSS override, make one.
				    {
					$templateObject.append("<cssOverride>"+newCSS+"</cssOverride>");
					// console.log($templateObject.toXML());
				    } 
				else
				    {
					$cssOverride.text(newCSS);
				    }

                               update_preview( $templateObject.toXML() );
                               if( !preview_only ) {
				$.ajax({
					 headers: { 
						'Accept': 'application/xml',
						'Content-Type': 'application/xml' 
					},
					url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/",
					    type: "POST",
					    data: $templateObject.toXML(),
					    processData: false,
					    success: function(data, textstatus)
					    {
						k(data);
					    },
					    error: function(data, textstatus)
					    {
						err(data);
					    }
				    });
                               }

			    },
			    error: function(request, textstatus, error)
			    {
				alert(I18N.message("perc.ui.template.manager@Error Grabbing Template"));
			    }
		    });

	        

	    }
    }

    /**
     * get_theme_for_theme - Get the theme for a template
     *
     * @var templateID - The Template ID
     *
     * @var k - The callback for success.
     *
     * @var err - The callback for error.
     */
    function get_theme_for_template(templateID, k, err)
    {
	
	var currentTheme;
	var $templateObject;
	var $cssTheme;

	$.ajax({
		headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
		url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateID,
		    type: "GET",
		    success: function(data, textstatus)
		    {
			
			$templateObject = $(data);
			$cssTheme = $templateObject.find("Template theme");
			k($cssTheme);
		    },
		    error: function(request, textstatus, error)
		    {
			err(error);
		    }
	    });

    }

    /**
     * set_theme_for_template - Set the given theme for a template.
     *
     * @var templateID - The Template ID
     *
     * @var templateName - The Template Name to use, typically gotten from the theme summary.
     *
     * @var k - The callback for success
     *
     * @var err - The callback for an error.
     */
    function set_theme_for_template(templateID, themeName, k, err, update_preview, preview_only)
    {
	$.ajax({
		headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
		url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateID,
		    type: "GET",
		    success: function(data, textstatus)
		    {
			
			// console.log(data);
			
			$templateObject = $(data);
			$cssTheme = $templateObject.find("Template theme");
			
			// console.log($cssOverride.length);
			// console.log($cssOverride);
			
			if ($cssTheme.length === 0) // There is no CSS override, make one.
			    {
				$templateObject.append("<theme>"+themeName+"</theme>");
				// console.log($templateObject.toXML());
			    } 
			else
			    {
				$cssTheme.text(themeName);
			    }
			
                       update_preview( $templateObject.toXML() );
                       if( !preview_only ) {
			$.ajax({
				 headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
				url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/",
				    type: "POST",
				    data: $templateObject.toXML(),
				    processData: false,
				    success: function(data, textstatus)
				    {
					k(themeName);
				    },
				    error: function(data, textstatus)
				    {
					$.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: $.PercServiceUtils.extractDefaultErrorMessage(data)});
				    }
			    });
                       }
			
		    },
		    error: function(request, textstatus, error)
		    {
			alert(I18N.message("perc.ui.template.manager@Error Grabbing Template"));
		    }
	    });
	
	
	
    }

    function preview_template( obj, k ) {
/* Preview is disabled for now --JHP
       $.ajax({
             url: "/Rhythmyx/services/pagemanagement/render/templateAll",
             contentType: "text/xml",
             type: "POST",
             success: k,
             data: obj });
*/
    }

    function load_template( template_id, callback ){ 
       $.ajax({
	     url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + template_id,
	     type: "GET",
	     dataType: 'text',
		accepts: {
					text: "application/xml"
				 },
		 success: callback,
	     error: function(data, textstatus, error){
           $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: $.PercServiceUtils.extractDefaultErrorMessage(data)});
        }
	     });
    }

    function save_template( template_id, requireMigration, obj, callback) {
        // Call different save method if template changes are related to design inspection with page content in it.
        if(requireMigration) {
            var saveUrl = $.perc_paths.TEMPLATE_LOAD_SAVE + "/page/" + memento.pageId;
        }
        else {
            saveUrl = $.perc_paths.TEMPLATE_LOAD_SAVE + "/";
        }    
       $.ajax({
		   headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
	     url: saveUrl,
	     type: "POST",
             data: obj,
	     success: callback,
	     error: function(data, textstatus, error){
           $.unblockUI();
           $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: $.PercServiceUtils.extractDefaultErrorMessage(data)});
        }
	     });
    }

    function render_region( region_id, template, pageId, callback ) {
       var srvUrl = $.perc_paths.TEMPLATE_RENDER + "/" + region_id;
	   if(pageId)
	   {
	   		srvUrl =  $.perc_paths.TEMPLATE_RENDER + "/" + pageId + "/" + region_id;
	   }
	   $.ajax({
		   
		   headers: { 
				'Accept': 'application/xml',
				'Content-Type': 'application/xml' 
			},
	     url: srvUrl,
	     type: "POST",
	     data: template,
	     processData: false,
	     success: callback,
        error: function(data, textstatus, error){
           $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: $.PercServiceUtils.extractDefaultErrorMessage(data)});
        }
        });
    }

    $.perc_templatemanager = {
       load_template: load_template,
       save_template: save_template,
       render_region: render_region,
       preview_template: preview_template,
	get_template_css : get_template_css,
	set_template_css : set_template_css,
	get_theme_list : get_theme_list,
	set_theme_for_template : set_theme_for_template,
	get_theme_for_template : get_theme_for_template,
	get_theme_css_data : get_theme_css_data
    };

})(jQuery);
