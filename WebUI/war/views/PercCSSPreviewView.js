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

    P.CSSPreviewView = function( iframe, model ) 
    {
        //An array object of widget definition summaries, See PSWidgetSummary.java for the fields.
        //Filled in populateWidgetDefs() method during the initialization.
        var widgetDefs = {};
        
        // Use singleton controller to keep track of dirty status
        var dirtyController = $.PercDirtyController;
        
	/**
	 * Widget menus
	 */
	var widgetMenu = 
        [
            {   name:'configure', img: function (elem){
                  var imgSrc =  '/cm/images/images/buttonConfigStandalone';
                  if(!widgetDefs[elem.attr('widgetdefid')].hasCssPrefs)
                     imgSrc = '/cm/images/images/buttonConfigStandaloneInactive';
                  return imgSrc;
                },
                callback: function(elem){
                     if(widgetDefs[elem.attr('widgetdefid')].hasCssPrefs)
                        editWidgetCssProperties(elem)
                },
                tooltip: I18N.message("perc.ui.css.preview.view@Configure")
         }
        ];
	  
	var widgetDecorator = P.decorationController( allWidgets, 'perc-widget-puff', 'perc-region-selected', widgetMenu);

	initWidgetEditDialog();
	populateWidgetDefs();
	initRender();
	
        /**
         * Listen for asset delete events and refresh view
         */
        $.perc_finder().addActionListener(function(action, data) {
            
            // verify that we are deleting an asset
            if(action == $.perc_finder().ACTIONS.DELETE && (data.type == 'asset' || data.type == 'page')) {
                if (typeof(data.isOpen) != 'undefined' && data.isOpen)
                {
                    // current item is open for edit, no need to refresh as it will be cleared
                    return;
                }
                
                // find out where we are, what view and what tab within that view
                var currentView     = $.PercNavigationManager.getView();
                if(currentView == $.PercNavigationManager.VIEW_DESIGN) {
                    var currentTabIndex = $("#tabs").tabs('option', 'selected');
                    if(currentTabIndex == 3) {
                    	dirtyController.setDirty(true, "template", saveCSS); 
                    	initRender();
                    }
                } else if(currentView == $.PercNavigationManager.VIEW_EDITOR) {
                    var currentTabIndex = $("#perc-pageEditor-tabs").tabs('option', 'selected');
                    if(currentTabIndex == 2) {
                        dirtyController.setDirty(true, "page", saveCSS);
                    	initRender();
                    }
                }
            }
        });

        function saveCSS(callback){
            var currentView = $.PercNavigationManager.getView();
            var cssController = P.cssController( model, $("#frame"), P.CSSPreviewView( $("#frame"), model) );
            if(currentView == $.PercNavigationManager.VIEW_EDITOR) {
                cssController.save(callback);                
            }
            else if (currentView == $.PercNavigationManager.VIEW_EDIT_TEMPLATE) {
                callbackFunc = callback || function (){};
                cssController.setOverrideCSS();
                cssController.save(function (status, data) {
                    if (status == true) {
                        dirtyController.setDirty(false, "template");
                        callbackFunc();
                    }
                }); 
            }
        }
        
        /**
         * Bind the click event to the JavaScript Off menu. Checks whether the view is dirty or not, if dirty then
         * warns the user. Sets the JavaScriptOff to true or false depending on the current status. Calls the
         * initRender to reinitialize the view.
         */
        $("#perc-style-menu a.perc-dropdown-option-DisableJavaScript").unbind().click(function() {
            var __this = this;
            var handleScripts = function()
            {
                var scriptOff = I18N.message( "perc.ui.menu@JavaScript Off" );
                var scriptOn = I18N.message( "perc.ui.menu@JavaScript On" );
                if($(__this).text() == scriptOff)
                {
                    $(__this).text(scriptOn);
                    $(__this).attr("title", I18N.message("perc.ui.content.view@Turns On JavaScript"));
                    model.setJavaScriptOff(true);
                    initRender();
                }
                else
                {
                    $(__this).text(scriptOff);
                    $(__this).attr("title", I18N.message("perc.ui.content.view@Turns Off JavaScript"));
                    model.setJavaScriptOff(false);
                    initRender();
                }
                dirtyController.setDirty(false, "style");
            };
            if(dirtyController.isDirty())
            {
            	//TODO: I18N Below
                var msg = model.isTemplate?"This template" : "This page";
                msg += I18N.mssage("perc.ui.css.preview.view@Unsaved Changes") +
                    I18N.message("perc.ui.css.preview.view@Dont Save");
                var options = {
                    question: msg
                };
               
                // if dirty, then show a confirmation dialog
                dirtyController.confirmIfDirty(
                    function() { handleScripts(); }, 
                    function(){},
                    options
                );         
            }
            else
            {
                handleScripts();
            }
        });


	return { afterRender: afterRender, initRender: initRender };
    /**
     * Fill the widget library with the various widget toots
     */
    function populateWidgetDefs() {
        $.ajax(
        {
            url: $.perc_paths.WIDGETS_ALL,
            type: "GET",
            dataType: 'json',
            success: function(data, textStatus)
            {
                $.each( data['WidgetSummary'], function( ) {
                    widgetDefs[this.id] = this;
                });
            },
            error: function(request, textStatus, error)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: defaultMsg});
            }
        });
    }

    function editWidgetCssProperties(elem)
    {
            var widgetId = elem.attr('widgetid'), widgetDefinitionId = elem.attr('widgetdefid');
            var setProp = function(name,value) {
                model.editWidget( widgetId, function() {
                    this.cssProperties[ name ] = value;
                });

                // If user clicks ok in Widget Properties dialog, mark style as dirty
                // It is then reset if style is saved or cancelled
                dirtyController.setDirty(true, "style", saveCSS);
            };
            var cssProperties;
            model.editWidget( widgetId, function(){ cssProperties = this.cssProperties; } );

            P.widgetPropertiesDialog( setProp, cssProperties, widgetDefinitionId, refreshRender, 'CssPref', model.getWidgetByName);
    }
	
    /**
     * @TODO JB: Doing a bare minimum for widget CSS properties. This needs to be fixed.
     */
    function refreshRender()
    {
         
    }
    
    function initRender()
	{
	    /** 
	     * for now, it just calls the standard render callback, but this might be 
	     * extended later.
	     */
	    model.renderAll(iframe, function() {
	    	
			afterRender();
			$("#frame").attr("perc-view-type","style");
            var frwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-frame');
            if(frwrapper != null)
                frwrapper.handleComponentProgress('perc-ui-component-editor-frame', "complete");
            var tbwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-toolbar');
            if(tbwrapper != null)
                tbwrapper.handleComponentProgress('perc-ui-component-editor-toolbar', "complete");
	    });
	}

            /**
            * HTML fixes when in style mode
            */       
            function sanitizeHtml()
            {
               $.perc_utils.handleLinks(iframe);
               $.perc_utils.handleObjects(iframe);
            }
    
	    function afterRender() 
	    {
	        iframe.contents().find("div").unbind();   // kill the editable events.
	        widgetDecorator.refresh();
                sanitizeHtml();
	        iframe.contents().find("body").css("z-index","-1000").unbind().click(function() {
	            widgetDecorator.unselectAll();
	        });
	    }

        /**
         * Initialize the region editing dialog
         */
        function initWidgetEditDialog() {
	    // NEEDS TO BE IMPLEMENTED
        }

        /**
         * Callback to update the region edit dialog and bind its save button
         * This is invoked when the edit button is clicked on the top right of a region
         * The event is bound and invoked from the region decoration controller
         */
        function editWidgetProperties( region ) {
	    // NEEDS TO BE IMPLEMENTED
        }

        /**
         * Saving region properties to the model when region dialog is dismissed
         */
        function saveWidgetProperties( region ) {
	    // NEEDS TO BE IMPLEMENTED
        }

        /**
         * Retrieves all the regions in the iframe.
         * This method is passed to the region decorator to iterate and
         * decorate all the regions
         * It is also used here locally to iterate over the regions in addDropsSortingResizing()
         */
        function allWidgets() {
            // lock widgets that have been defined by the template
            // add class perc-locked so that decoration controller
            // will remove the edit icons and add the locked icon
            var specialWidgets = model.getSpecialWidgets("Layout");
            if(specialWidgets)
                iframe.contents().find('.perc-widget').each(function() {
                    var widgetid = $(this).attr("widgetid");
                    if(widgetid in specialWidgets.LockedWidgets)
                    	$(this).addClass("perc-locked");
                    if(widgetid in specialWidgets.TransperantWidgets)
                        $(this).addClass("perc-widget-transperant");						
                });
            return iframe.contents().find(".perc-widget");
        }
    }

})(jQuery,jQuery.Percussion);
