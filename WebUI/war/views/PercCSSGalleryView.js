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

(function($,P) {
    
    P.cssGalleryView = function(controller) {
    
        // Use singleton controller to keep track of dirty status
        var dirtyController = $.PercDirtyController;
        var root = $("#perc-css-gallery");
    
        render();
    
        function _dashify(nameString) {
        	if (typeof(nameString) === 'string')
        		return nameString.replace(/ /g,"-");
        	else
        		return nameString;

        }
    
        function render() {
    
            root.empty();
    
            var galleryButtonBar = '<div class="perc-buttons" id="perc-css-gallery-button-bar">' +
                '  <form>' +
                '  </form>' +
                '</div>';
            
            root.append('<div id="perc-css-gallery-status"></div>');
            root.append(galleryButtonBar);
    
            // Get back each theme entry. data contains a DOM object. 
        
            controller.getThemeList(function(status, data) {
                if (status === true) {
                    //////////////////////////////////
                    // Iterate over each theme entry
                    //////////////////////////////////
                    var themes = data.ThemeSummary;
                    root.append("<table id='perc-themes-table'><tr id='perc-themes-table-row'></tr><table>");
                    if (themes.length > 0) {
                        $(themes).each(function() {
                            // Events to trigger for each theme entry.
                            var $nameAsEntered = this.name;
                            var $thumbUrl = this.thumbUrl;
                            var $name = _dashify($nameAsEntered);
                            $("#perc-themes-table-row").append(renderGalleryEntry($name, $thumbUrl, $nameAsEntered));
            
                            // Selecting the theme
                            $j("#theme-"+$name).click(function() {
                            	// if user clicks on any theme from the gallery, then set the dirty flag
                            	dirtyController.setDirty(true,"style",saveCSS);
                            	
                                controller.setTemplateTheme($nameAsEntered, function(success) {
                                   // And finally, reset the save button.
                                   $j("#perc-css-gallery-edit-save").removeClass("ui-state-disabled");
                                    selectTheme($name);
                                });
                            });
            
        
                        });
            
                        // And finally select the given bits
                        controller.getTemplateTheme(function(themeName) {
                            var theme =  _dashify(themeName);
                            selectTheme(theme);
                        });
                    }
                    
                } else {
                    
                    root.append('<div>' + I18N.message("perc.ui.css.galery.view@Gallery Cannot Load") + '</div>');
                }
            });
        }

        function saveCSS(callback){
            var currentView = $.PercNavigationManager.getView();
            if(currentView === $.PercNavigationManager.VIEW_EDITOR) {
                controller.save(callback);                
            }
            else if (currentView === $.PercNavigationManager.VIEW_EDIT_TEMPLATE) {
                callbackFunc = callback || function (){};
                controller.setOverrideCSS();
                controller.save(function (status, data) {
                    if (status === true) {
                        dirtyController.setDirty(false, "template");
                        callbackFunc();
                    }
                }); 
            }
        }
        
        // Send back one HTML fragment for a given gallery entry. Called from render() above.
    
        function renderGalleryEntry(name, thumbUrl, nameAsEntered) {
            var output;
    
            output = '<td><div id="theme-'+name+'-container" class="perc-css-gallery-item" style="display: table-cell"><a id="theme-'+name+'" href="#"><img src="'+thumbUrl+'" alt="perc.ui.template.layout@Theme"/><br /><span class="perc-css-gallery-item-name" style="text-align: center">'+nameAsEntered+'</span></div></a></td>';
    
            return output;
        }

        // called on click of a theme to change the UI appropriately to reflect the newly selected theme.
    
        function selectTheme(sThemeName) {
            /**  
             * Used to change the visual element of selecting a theme. Either in response to a getTemplateThemeName
             * or after a setTheme()
             */
            
            var selector = ".perc-css-gallery-item";
            var selector_selected = "#theme-"+sThemeName+"-container";
            var selector_closeLinks = ".perc-theme-clear";
            var selector_closeLink_selected = "#theme-"+sThemeName+"-clear";
    
            $j(selector).removeClass("perc-css-gallery-themeHighlighted");
            $j(selector_closeLinks).css('display','none');
            $j(selector_selected).addClass("perc-css-gallery-themeHighlighted");
            $j(selector_closeLink_selected).css('display','inline');
            
        }
    };
})(jQuery,jQuery.Percussion);