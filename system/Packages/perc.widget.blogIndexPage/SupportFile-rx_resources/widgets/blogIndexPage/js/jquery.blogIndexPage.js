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
 *    jquery.blogIndexPage.js
 *
 *    Java Script code for adjusting the CSS of the dialog on the fly and binding the click event to RSS enable checkbox
 **     
 */

    /*
     * jQuery code for the control
     */    
    (function($) {

        $(document).ready(function(){
            $("#perc-feed-name").attr("maxlength", 500);
            $("#perc-feed-title").attr("maxlength", 2000);
            $("#perc-feed-description").attr("maxlength", 4000);
            $.perc_textAutoFill($('#perc-feed-title'), $('#perc-feed-name'), $.perc_autoFillTextFilters.URL);
            $.perc_filterField($('#perc-feed-name'), $.perc_autoFillTextFilters.URL);
            $("#perc-content-edit-metadata-link").removeClass('perc-spacer').css({'margin-top':'-7px','pointer':'default','color':'black','font-weight':'bold'});
            $("#perc-content-edit-metadata-link").addClass('noClick');
            $(".perc-content-edit-data").css('padding-bottom', '23px');
            $('.ui-dialog-buttonpane').css('height', '50px');
                        
            var metaFields = $("#perc-feed-name, #perc-feed-title, #perc-feed-description");
            metaFields.css('width', 315);

            //If there is error make sure the chekcbox is checked on re-redering the form.
            if($('#perc-content-edit-errors').length>0) {
                $('#perc-enable-feed').attr("checked", true);          
            }
            $("#perc-feed-title").after("<div class = 'perc-disabled datadisplay' style = 'padding-bottom:4px;'>" + $("#perc-feed-title").val() + "</div>");
            $("#perc-feed-name").after("<div class = 'perc-disabled datadisplay' style = 'padding-bottom:4px;'>" + $("#perc-feed-name").val() + "</div>");
            $("#perc-feed-description").after("<div class = 'perc-disabled datadisplay' style = 'width:310px; height:77px;padding-top:0px'>" + $("#perc-feed-description").val() + "</div>");
            
            if(!($('#perc-enable-feed').is(':checked'))) {
                metaFields.addClass('perc-rss-disabled');
                $('.perc-content-edit-data').css('padding-bottom', '19px');                 
                 metaFields.hide();                                    
            }
            else {
                $(".perc-disabled").hide();
                 metaFields.show();
            }

        // Bind the click event to the 'Enable RSS feed' checkbox. 
        // If its check the meta-data field will be eidtable and if unchecked
        // meta-data field will become disabled.
        });
        $(document).on("click","#perc-enable-feed", function () {
            var metaFields = $("#perc-feed-name, #perc-feed-title, #perc-feed-description");
            metaFields.toggleClass('perc-rss-disabled');
            if($("#perc-feed-name").hasClass('perc-rss-disabled')){                
                 metaFields.hide();
                  $(".perc-disabled").show();
            }    
            else {
                $(".perc-disabled").hide();
                 metaFields.show();
            }
        });

        
    })(jQuery);     
