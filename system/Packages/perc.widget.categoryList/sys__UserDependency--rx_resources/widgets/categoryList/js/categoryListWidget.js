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
