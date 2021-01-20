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
 * Welcome gadget javascript code.
 */
(function($){
    var perc_utils = percJQuery.perc_utils;
    //On document ready replace the bookmarklet href with generated bookmarklet code and add the click event to the more info link.
    $(document).ready(function() {
        $("#perc_linkback_anchor").attr("href",generateBookMarklet()).click(function(event){event.preventDefault();});
        $("#perc_linkback_moreinfo_link").click(function(){displayMoreInfoDialog();});
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
                $(this).find("#perc_linkback_anchor").click(function(event){event.preventDefault();});
            },
            id: "perc-linkback-moreinfo-dialog",
            modal: true
        });
    }
})(jQuery);
