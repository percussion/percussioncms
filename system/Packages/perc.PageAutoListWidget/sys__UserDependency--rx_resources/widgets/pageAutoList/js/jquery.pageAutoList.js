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

/*jslint browser: true, plusplus: true */
/*global $, jQuery, alert*/

/**
 *    jquery.pageAutoList.js
 *
 *    Java Script code for the control pageAutoListWidgetControl used by the PageAutoListWidget
 *     This control used to build the JCR query for the Page Auto List widget, based on
 *    a date range, title contains field, page template multi select list and Site location selection dialogue
 *
 *
 */

//Sets the display date in SimpleDate format, based on the Widget config $date_format
function setDisplayDate(p_dateValue, p_displayDate) {
    "use strict";

    /**
     * Since the Rest service for the read the Widgets config $date_format does not exist at the time of building the widget
     * the date format is hard coded for now and the widget $date_format has been commented out in the widget file PageAutoList.xml
     */

    if (p_dateValue !== null && p_dateValue !== "") {
        var dateFormat = "M/dd/yyyy",
            dateObject = new Date(p_dateValue);
        $('[name="' + p_displayDate + '"]').val(dateObject.format(dateFormat));
    } else {
        $('[name="' + p_displayDate + '"]').val("");
    }
}

/*
    Validate controls fields
    */
function validateFields() {
    "use strict";

    var p_start_date = new Date($('[name="start_date"]').val()),
        p_end_date = new Date($('[name="end_date"]').val());

    if (p_end_date < p_start_date) {
        alert("Please enter a valid date range");
        return false;
    } else {
        return true;
    }
}

/**
 * Get array of names of all the page templates that are checked. Returns null if nothing is checked.
 */
function getPageTemplates() {
    "use strict";

    var pageTpls = [];
    $.each($(".perc-pagetemplates-chkbox"), function () {
        if (this.checked) {
            pageTpls.push(this.value);
        }
    });
    return pageTpls.length > 0 ? pageTpls : null;
}

/**
 * Get array of names of all the page templates that are checked. Returns null if nothing is checked.
 */
function getPageCategories() {
    "use strict";

    var pageCats = [];
    $.each($(".perc-category-selector-chkbox"), function () {
        if (this.checked) {
            pageCats.push(this.value);
        }
    });
    return pageCats.length > 0 ? pageCats : null;
}

/*
 * Build the JCR Query
 */
function buildQuery() {
    "use strict";

    var i, p_titlecontains, p_start_date, jxl_start_date, p_end_date, jxl_end_date, p_site_path, p_pagetemplates, p_query, queryOp, p_pagecategories, p_hide_past_results, p_hide_past_filter;

    p_titlecontains = $('[name="title_contains"]').val();
    p_start_date = $('[name="start_date"]').val();
    jxl_start_date = $.datepicker.formatDate('yy/mm/dd', new Date(p_start_date));
    p_end_date = $('[name="end_date"]').val();
    jxl_end_date = $.datepicker.formatDate('yy/mm/dd', new Date(p_end_date));
    p_site_path = $('[name="site_path"]').val();
    p_pagetemplates = getPageTemplates();
    p_pagecategories = getPageCategories();
    p_hide_past_results = ($('[name="hide_past_results"]').val() === "true");
    p_hide_past_filter = $('[name="perc-pageautolist-hide-past-results-date-type"]:checked').val();

    //Sample JCR query
    //select rx:sys_contentid, rx:sys_folderid from rx:percPage where rx:sys_contentpostdate >= '2010/01/01' and rx:sys_contentpostdate <= '2010/01/05' jcr:path like '//Sites/CorporateInvestments%'";

    p_query = "select rx:sys_contentid, rx:sys_folderid from rx:percPage ";
    queryOp = " where ";

    if (p_site_path != null && p_site_path != "") {

        //Save site_path
        $('[name="site_path"]').val(p_site_path);

        // CM-43 add the trailing slash
        if (p_site_path.substr(p_site_path.length - 1) != '/') {
            p_query += queryOp + "jcr:path like '" + p_site_path + "/%'";
        } else {
            p_query += queryOp + "jcr:path like '" + p_site_path + "%'";
        }
        queryOp = " and ";
    }
    if (p_titlecontains != null && p_titlecontains != "") {
        p_query += queryOp + "rx:resource_link_title like '%" + p_titlecontains + "%'";
        queryOp = " and ";
    }
    if (p_start_date != null && p_start_date != "") {
        p_query += queryOp + "rx:sys_contentpostdate >='" + jxl_start_date + "'";
        queryOp = " and ";
    }

    if (p_end_date != null && p_end_date != "") {
        p_query += queryOp + "rx:sys_contentpostdate <='" + jxl_end_date + "'";
        queryOp = " and ";
    }

    if (p_pagetemplates !== null) {

        //Save the seleceted  page templates to page_templates_list
        $('[name="page_templates_list"]').val(p_pagetemplates.toString());

        p_query += queryOp + " ( ";
        for (i = 0; i < p_pagetemplates.length; i++) {
            p_query += "rx:templateid='" + p_pagetemplates[i] + "'";

            if (i < (p_pagetemplates.length - 1)) {
                p_query += " or ";
            }
        }
        p_query += ") ";
    }
    
    //Page Categories
    if (p_pagecategories !== null){
        //Save the categories
        $('[name="page_category_list"]').val(p_pagecategories.toString());
    }

    //Set Query field
    $('[name="query"]').val(p_query);

    return false;
}

/*
 * jQuery code for the control
 */
(function ($) {
    "use strict";
    
    $('.perc-rss-disabled').css('border-style', 'inset');

    $.fn.pageAutoListControl = function (settings) {
        var config = {};


        if (settings) {
            $.extend(config, settings);
        }

        function populateTemplateTypes(siteName, pageTemplatesOptionsArray) {
            // has to inject the HTML tag here because $('.ui-perc-pageautolist-pagetypes').empty();
            var tplPath = $.perc_paths.TEMPLATES_USER;
            if (siteName) {
                tplPath = $.perc_paths.TEMPLATES_BY_SITE + "/" + siteName;
            }
            $('.ui-perc-pageautolist-pagetypes').empty().append($('<div id="perc-pagetemplates-container"/>'));
            $.getJSON(tplPath, function (data) {
                var tpls = data.TemplateSummary, i;
                for (i = 0; i < tpls.length; i++) {
                    var tpl = tpls[i];
                    var checked = $.inArray(tpl.id, pageTemplatesOptionsArray) == -1 ? "" : " checked ='true' ";
                    $("#perc-pagetemplates-container").append($("<div class='perc-pagetemplates-entry'><input type='checkbox' class='perc-pagetemplates-chkbox'" + checked + " value='" + tpl.id + "'></input><span title='" + tpl.name + "'>" + tpl.name + "</span></div>"));
                }
                $(".perc-pagetemplates-chkbox").change(function () {
                    var pts = getPageTemplates();
                    pts = pts ? pts : "";
                    $('[name="page_templates_list"]').val(pts);
                    buildQuery();
                    $.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
                });
            });
        }
        /*
            Multi Select list for the Page Templates
            */
        function showPageTemplates() {
            $ = $j;
            //make an array from the comma delimited options string
            var pageTemplatesOptionsArray = [];

            //Check if page_templates has been defined
            if (typeof $('[name="page_templates_list"]').val() != "undefined") {
                pageTemplatesOptionsArray = $('[name="page_templates_list"]').val().split(",");
            }

            $(document).ready(function () {
                populateTemplateTypes(getSiteFromFolderPath($('[name="site_path"]').val()), pageTemplatesOptionsArray);
                populateCategories(getSiteFromFolderPath($('[name="site_path"]').val()), pageTemplatesOptionsArray);
            });
        }
        
function getDomPath(el) {
  var stack = [];
  while ( el.parentNode != null ) {
    console.log(el.nodeName);
    var sibCount = 0;
    var sibIndex = 0;
    for ( var i = 0; i < el.parentNode.childNodes.length; i++ ) {
      var sib = el.parentNode.childNodes[i];
      if ( sib.nodeName == el.nodeName ) {
        if ( sib === el ) {
          sibIndex = sibCount;
        }
        sibCount++;
      }
    }
    if ( el.hasAttribute('id') && el.getAttribute('id') != '' ) {
      stack.unshift(el.nodeName.toLowerCase() + '#' + el.getAttribute('id'));
    } else if ( sibCount > 1 ) {
      stack.unshift(el.nodeName.toLowerCase() + ':eq(' + sibIndex + ')');
    } else {
      stack.unshift(el.nodeName.toLowerCase());
    }
    el = el.parentNode;
  }

  return stack;
}
        
        /* Multi select list for Categories */
        function populateCategories(siteName, pageCategoriesOptionsArray) {
           
                var selected = $('[name="page_category_list"]').val();
                
                var inputId = 'perc-content-edit-page_category_list';
                var paramName = 'page_category_list';
                var treeSrcUrl = '../percPageSupport/getCategories.xml?sitename='+siteName;
                var readonly = false;
                
            //readonly = readonly == 'true' ? true : false;
            var opts = {url : treeSrcUrl, selected : selected, paramName : paramName, inputId : inputId, readonly : readonly, separator : ","};
    
            $('#perc-category-selector').perc_checkboxTree(opts);
            
           
        }

        
        function showCategories() {
            $ = $j;
            //make an array from the comma delimited options string
            var pageCategoriesOptionsArray = [];

            //Check if page_templates has been defined
            if (typeof $('[name="page_category_list"]').val() != "undefined") {
                var pageCategoriesOptionsArray = $('[name="page_category_list"]').val().split(",");
            }

            $(document).ready(function() {
                populateCategories(getSiteFromFolderPath($('[name="site_path"]').val()), pageCategoriesOptionsArray);
            });
        }

        
        /*
        Site and Folder path select diagloue, which populates the content editor field site_path
        */

        function getSiteFromFolderPath(folderPath) {
            var siteName = null;
            if (folderPath && folderPath.indexOf("//Sites/") > -1) {
                siteName = folderPath.substring(("//Sites/").length);
                if (siteName.indexOf("/") > -1) {
                    siteName = siteName.substring(0, siteName.indexOf("/"));
                }
            }
            return siteName;
        }
        
        function showSites() {
            $(document).ready(function() {
                var path = $.PercFinderTreeConstants.convertFolderPathToPath($('[name="site_path"]').val());
                
                function renderComplete() {
                    window.scrollTo(0, 0);
                }
                
                function setPath(pathItem) {
                    //Reset the templates if site changes.
                    var oldPath = $('[name="site_path"]').val();
                    if (oldPath != pathItem.folderPath && getSiteFromFolderPath(oldPath) != getSiteFromFolderPath(pathItem.folderPath)) {
                        populateTemplateTypes(getSiteFromFolderPath(pathItem.folderPath), []);
                        populateCategories(getSiteFromFolderPath(pathItem.folderPath), []);
                    }
                    //Save site_path
                    $('[name="site_path"]').val(pathItem.folderPath);
                    buildQuery();
                    $.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
                }
                
                
                if (!path) {
                    path = "/site";
                }
                $("#perc-folder-selector").PercFinderTree({
                    rootPath: $.PercFinderTreeConstants.ROOT_PATH_SITES,
                    showFoldersOnly: true,
                    classNames: {
                        container: "perc-folder-selector-container",
                        selected: "perc-folder-selected-item"
                    },
                    height: "250px",
                    width: "300px",
                    initialPath: path,
                    onRenderComplete: renderComplete,
                    onClick: setPath
                });

                
            });

        }

        //Callbacks Event Code

        $('#perc-content-edit-title_contains').change(function () {
            buildQuery();
        });

        //Build query if mouse leaves the form, i.e when the user goes to click on the save button which is not part of the iframe
        $('#perc-content-form').mouseleave(function () {
            buildQuery();
        });

        // update hide_past_results hidden field and rebuild form when the checkbox is updated
        $('#perc-pageautolist-hide-past-results').on("change", function() {
            if ($(this).is(':checked')) {
                $('[name="hide_past_results"]').val(true);
            } else {
                $('[name="hide_past_results"]').val(false);
            }
            buildQuery();
        });

        // update hide_past_results_date_type hidden field and rebuild form when the checkbox is updated
        $('[name="perc-pageautolist-hide-past-results-date-type"]').on('change', function(){
            var hideFilterDateType = $('[name="perc-pageautolist-hide-past-results-date-type"]:checked').val();
            $('[name="hide_past_results_filter"]').val(hideFilterDateType);
            buildQuery();
        });

         /**
         * This is a pre submit handler, called by the asset editing framework.
         */
        function updateQuery() {
            buildQuery();
            return true;
        }

       /**
         * The date fields are read only and there is no way to clear the existing date. The following function
         * allows the Del key press to clear the date values.
         */
        function addDelToDateControls() {
            $('#display_end_date').keydown(function (evt) {
                var rawCode = evt.charCode ? evt.charCode : evt.which;
                if (rawCode == 46 || rawCode == 8) {
                    $('#display_end_date').val("");
                    $('[name="end_date"]').val("");
                    buildQuery();
                } else if (rawCode == 9) {
                    return true;
                } else {
                    return false;
                }
            });
            $('#display_start_date').keydown(function (evt) {
                var rawCode = evt.charCode ? evt.charCode : evt.which;
                if (rawCode == 46 || rawCode == 8) {
                    $('#display_start_date').val("");
                    $('[name="start_date"]').val("");
                    buildQuery();
                } else if (rawCode == 9) {
                    return true;
                } else {
                    return false;
                }
            });
        }
        
        /*
        Initialize the control
        */
        function initializeForm() {

            //Set display date range
            setDisplayDate($('[name="start_date"]').val(), "display_start_date");
            setDisplayDate($('[name="end_date"]').val(), "display_end_date");
            // check the hide_past_results input element for a true or false string value, and using the abbreviated syntax below
            // which will check the string value below and return a boolean response
            var hidePastResultsFlag = ($('[name="hide_past_results"]').val() === "true");
            $('[name="hidePastResults"]').prop("checked", hidePastResultsFlag );
            var hideFilterDateType = $('[name="hide_past_results_filter"]').val();
            $('[name="perc-pageautolist-hide-past-results-date-type"][value="' + hideFilterDateType + '"]').prop('checked', true);
            $("#display_title_contains").val($('[name="title_contains"]').val());
            $("#display_title_contains").blur(function () {
                $('[name="title_contains"]').val($("#display_title_contains").val());
                buildQuery();
            });
            $("#display_title_contains").change(function () {
                $('[name="title_contains"]').val($("#display_title_contains").val());
                buildQuery();
            });
            showPageTemplates();
            showCategories();
            addDelToDateControls();
            showSites();
            $.topFrameJQuery.PercContentPreSubmitHandlers.addHandler(updateQuery);

        }

        initializeForm();
        $("#ui-datepicker-div").addClass('ui-helper-hidden-accessible');
        return $(this);

    };
    $(document).ready(function () {
        $("#perc-feed-name").attr("maxlength", 500);
        $("#perc-feed-title").attr("maxlength", 2000);
        $("#perc-feed-description").attr("maxlength", 4000);
        $.perc_textAutoFill($('#perc-feed-title'), $('#perc-feed-name'), $.perc_autoFillTextFilters.URL);
        $.perc_filterField($('#perc-feed-name'), $.perc_autoFillTextFilters.URL);

        var metaFields = $("#perc-feed-name, #perc-feed-title, #perc-feed-description");

        //If there is error make sure the chekcbox is checked on re-redering the form.
        if ($('#perc-content-edit-errors').length > 0) {
            $('#perc-enable-feed').attr("checked", true);
        }
        if (!($('#perc-enable-feed').is(':checked'))) {
            metaFields.addClass('perc-rss-disabled');
            $("#perc-feed-title").after("<div class = 'perc-disabled datadisplay'>" + $("#perc-feed-title").val() + "</div>");
            $("#perc-feed-name").after("<div class = 'perc-disabled datadisplay'>" + $("#perc-feed-name").val() + "</div>");
            $("#perc-feed-description").after("<div class = 'perc-disabled datadisplay'>" + $("#perc-feed-description").val() + "</div>");
            metaFields.hide();

        }

        // Bind the click event to the 'Enable RSS feed' checkbox.
        // If its check the meta-data field will be eidtable and if unchecked
        // meta-data field will become disabled.
    });
    $(document).on('click', "#perc-enable-feed", function () {
        var metaFields = $("#perc-feed-name, #perc-feed-title, #perc-feed-description");
        metaFields.toggleClass('perc-rss-disabled');
        if ($("#perc-feed-name").hasClass('perc-rss-disabled')) {
            $("#perc-feed-title").after("<div class = 'perc-disabled datadisplay'>" + $("#perc-feed-title").val() + "</div>");
            $("#perc-feed-name").after("<div class = 'perc-disabled datadisplay'>" + $("#perc-feed-name").val() + "</div>");
            $("#perc-feed-description").after("<div class = 'perc-disabled datadisplay' style = 'width:320px; height:80px'>" + $("#perc-feed-description").val() + "</div>");
            metaFields.hide();
        } else {
            $(".perc-disabled").hide();
            metaFields.show();
        }
    });


})(jQuery);