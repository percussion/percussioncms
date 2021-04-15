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
 *  Catches the javascript exception, network error etc after the page has been loaded in the Design editor (only underLayout Tab).
 */
(function($)
{

    $.PercLayoutErrorDialog = function()
    {
        var percLayoutErrorDialogApi = {
            /**
             * TO DO: Write Doc
             */
            openLayoutErrorDialog: openLayoutErrorDialog
           // getErrorCounter: getErrorCounter
        };
        
        //See API for doc.
        function openLayoutErrorDialog()
        {            
            var taborder = 30;
            var dialogHtml = _renderLayoutErrorList();
            
            // if we are in the new blog post dialog, the width is 
            var dialogWidth = 800;
            var dialogHeight = 600;
            var dialog = $(dialogHtml).perc_dialog({
                title: I18N.message("perc.ui.layout.error.dialog@Errors"),
                buttons: {},
                percButtons: {
                    "Ok": {
                        click: function()
                        {
                            _remove();
                        },
                        id: "perc-error-ok"
                    }
                },
                id: "perc-layout-error-dialog",
                resizable: false,
                width: dialogWidth,
                modal: true
            });
            
            /**
             * Get the JS error from the global variable 'percGlobalErrors'and CSS errors from document.styleSheets.
             * Loop through both arrays to render the each error.
             * 
             */
            function _renderLayoutErrorList()
            { 
                $("#perc-layout-error-dialog").find("#perc-error-wrapper").html('');        
                var percJSErrors = document.getElementById('frame').contentWindow['percGlobalErrors'];
                var listWrapper = '<ul>';       
                var iframeDoc =  document.getElementById('frame').contentWindow.document;         
                var ss = iframeDoc.styleSheets;
                var percCssErrors = [];
                percCssErrors.length = 0;        		
        		for (var i = 0; i < ss.length; i++) {
        			try
        			{
                        var myrules = ss[i].cssRules ? ss[i].cssRules : ss[i].rules;
        				var cssRulesSize = myrules.length;
                        if(cssRulesSize === 0)
                        {
                            cssError = I18N.message("perc.ui.layout.error.dialog@File Not found") + ss[i].href;
            				percCssErrors.push(cssError);
                        }
        			}
        			catch(err)
        			{
        				percCssErrors.push(I18N.message("perc.ui.layout.error.dialog@File Not found" ) + ss[i].href);
        			}
        		}
                                
                var tempindex = 1;
                var index = 0;
                
                //Loop through JS errors
                if (percJSErrors.length > 0) 
                {
                    for (errors in percJSErrors) 
                    {
                        index = tempindex + parseInt(errors);                                           
                        listWrapper += '<li class = "perc-import-error-list-item perc-js-errors" title = "'+ percJSErrors[errors] +'">' + index + '. ' + percJSErrors[errors] + '</li>';
                    }                   
                }
                
                //Loop though CSS errors
                if(percCssErrors.length > 0) {
                    for (errors in percCssErrors) 
                    {
                        cssIndex = index + tempindex + parseInt(errors);                                           
                        listWrapper += '<li class = "perc-import-error-list-item perc-css-errors" title = "'+ percCssErrors[errors] +'">' + cssIndex + '. ' + percCssErrors[errors] + '</li>';
                    }     
                }
                if (percCssErrors.length === 0 && percJSErrors.length === 0) {
                    listWrapper += '<li class = "perc-no-errors"> No errors found.</li>';
                }
                listWrapper += '</ul>';

                var wrapper = $("<div id='perc-error-wrapper' />").append(listWrapper);
                return $("<div id='perc-layout-error-table' style='max-height: 300px;' />").append(wrapper);
            }

            
            /**
             * Removes the dialog.
             */
            function _remove()
            {
                dialog.remove();
            }            
        }
        return percLayoutErrorDialogApi;
    };
    
})(jQuery);
