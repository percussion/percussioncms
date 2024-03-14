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
 * Welcome gadget javascript code.
 */
(function($){
    var perc_utils = percJQuery.perc_utils;
    //On document ready replace the bookmarklet href with generated bookmarklet code and add the click event to the more info link.
    $(document).ready(function() {
        document.getElementById('perc-gadget-welcome-link-1').innerText=I18N.message("perc.ui.welcome.gadget@Documentation");
        document.getElementById('perc-gadget-welcome-link-2').innerText=I18N.message("perc.ui.welcome.gadget@Creation");
        document.getElementById('perc-gadget-welcome-link-3').innerText=I18N.message("perc.ui.welcome.gadget@Percussion Community");
        document.getElementById('perc-gadget-welcome-link-4').innerText=I18N.message("perc.ui.welcome.gadget@Ask Questions");
        document.getElementById('perc-gadget-welcome-link-5').innerText=I18N.message("perc.ui.welcome.gadget@Add Percussion Linkback");
        document.getElementById('perc-gadget-welcome-link-6').innerText=I18N.message("perc.ui.welcome.gadget@Percussion Linkback Explanation");
        gadgets.window.setTitle(I18N.message("perc.ui.gadgets.welcome@WELCOME"));
        $("#perc_linkback_anchor").attr("href",generateBookMarklet()).on("click",function(event){event.preventDefault();});
        $("#perc_linkback_moreinfo_link").on("click",function(){displayMoreInfoDialog();});
    });

    /**
     * Helper method that generates bookmarklet link.
     */
    function generateBookMarklet()
    {
        var linkHref = "javascript:var idelem=document.getElementById('perc_linkback_id');if(idelem){location.href='" + location.protocol + "//" + location.host + "/cm/app/?view=editor&perc_linkback_id=' + idelem.value;}else{alert('Sorry, Percussion CMS linkback is not supported for this page.')}";
        return linkHref;
    }

    /**
     * Method to display more info dialog.
     */
    function displayMoreInfoDialog()
    {
        closeButtonText = I18N.message("perc.ui.common.label@Close");
        var infoDialogHtml = $("#perc-bookmark-container").clone();
        infoDialogHtml.find("#perc_linkback_moreinfo_text").show();
        infoDialogHtml.find("#perc_linkback_moreinfo_link").hide();
        infoDialogHtml = infoDialogHtml.wrap("<div></div>").parent().html();
        var dialog = percJQuery(infoDialogHtml).perc_dialog({
            title: I18N.message("perc.ui.gadgets.welcome@Add Bookmarklet"),
            buttons: {},
            percButtons:{
                closeButtonText:{
                    click: function(){
                        dialog.remove();
                    },
                    id: "perc-linkback-moreinfo-close"
                }
            },
            open:function(){
                $(this).find("#perc_linkback_anchor").on("click",function(event){event.preventDefault();});
            },
            id: "perc-linkback-moreinfo-dialog",
            modal: true
        });
    }
})(jQuery);
