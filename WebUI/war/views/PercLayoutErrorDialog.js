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
