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

(function ($) {

    $.categoryListWidget = {
        psxControl: psxControl,
        readOnlyControl: readOnlyControl
    };

    function psxControl() {
        $('#display_start_date').datepicker({
            onSelect:
                function (value, date) {
                    // *********************************************************************
                    //This will be set from the widget config $date_format (set by velocity)
                    // *********************************************************************
                    $(this).focus();
                    var p_start_date = new Date(value);
                    var p_end_date = new Date($('[name="end_date"]').val());
                    if (p_start_date > p_end_date) {
                        $.perc_utils.alert_dialog({
                            title: "Error",
                            content: "First published on or after date must be less than First published before date.",
                            okCallBack: function () {
                                setDisplayDate($('[name="start_date"]').val(), "display_start_date");
                                return false;
                            }
                        });
                    }
                    else {
                        $('[name="start_date"]').val(value);

                        setDisplayDate(value, "display_start_date");

                        buildQuery();
                    }
                    // if the top most jquery is defined
                    if ($.topFrameJQuery !== undefined)
                    // mark the asset as dirty
                        $.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
                },
            showOn: 'button',
            buttonImage: '../rx_resources/widgets/categoryList/images/calendar.gif',
            buttonImageOnly: true,
            altFormat: 'yy-mm-dd',
            buttonText: ''
        });

        $('#display_end_date').datepicker({
            onSelect:
                function (value, date) {
                    $(this).focus();
                    var p_start_date = new Date($('[name="start_date"]').val());
                    var p_end_date = new Date(value);
                    if (p_start_date >= p_end_date) {
                        $.perc_utils.alert_dialog({
                            title: "Error",
                            content: "First published before date must be greater than First published on or after date.",
                            okCallBack: function () {
                                setDisplayDate($('[name="end_date"]').val(), "display_end_date");
                                return false;
                            }
                        });
                    }
                    else {
                        $('[name="end_date"]').val(value);
                        setDisplayDate(value, "display_end_date");
                        buildQuery();
                    }
                    // if the top most jquery is defined
                    if ($.topFrameJQuery !== undefined)
                    // mark the asset as dirty
                        $.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
                },
            showOn: 'button',
            buttonImage: '../rx_resources/widgets/categoryList/images/calendar.gif',
            buttonImageOnly: true,
            altFormat: 'yy-mm-dd',
            buttonText: ''
        });

        $('#perc-content-form').categoryListControl({});

        $("#categorylist-title").on("click",function () {
            $("#criteria_for_list").toggle();
            $("#categorylist-title").toggleClass("categorylist-expand-image categorylist-close-image");
        });
    }

    function readOnlyControl() {
        $("#categorylist-title").on("click",function () {
            $("#criteria_for_list").toggle();
            $("#categorylist-title").toggleClass("categorylist-expand-image categorylist-close-image");
        });

        // Put site value in website location field
        var sitepath = $("#perc_site_path").val().substring(8);
        var splitPath = sitepath.split("/");

        $("#perc_display_site_path").text(sitepath);

        // Fill templates field

        if (splitPath[0] !== undefined && splitPath[0] !== "") {
            $.PercServiceUtils.makeJsonRequest(
                $.perc_paths.TEMPLATES_BY_SITE + "/" + splitPath[0],
                $.PercServiceUtils.TYPE_GET,
                false,
                function (status, result) {
                    if (status === $.PercServiceUtils.STATUS_SUCCESS) {
                        var summaries = result.data.TemplateSummary;
                        var temps = {};
                        var tempsArray = [];
                        var tempIds = ($("#perc_template_list").val() !== "") ? $("#perc_template_list").val().split(',') : "";
                        for (let i = 0; i < summaries.length; i++) {
                            temps[summaries[i].id] = summaries[i].name;
                        }
                        for (let i = 0; i < tempIds.length; i++) {
                            tempsArray[i] = temps[tempIds[i]];
                        }
                        tempsArray.sort();
                        var buff = "";
                        for (i = 0; i < tempsArray.length; i++) {
                            if (i > 0)
                                buff += "<br/>";
                            buff += tempsArray[i];
                        }
                        $("#perc_display_template_list").append(buff);
                    }
                    else {
                        var defaultMsg =
                            $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        $.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
                    }
                }
            );
        }
    }
})(jQuery);
