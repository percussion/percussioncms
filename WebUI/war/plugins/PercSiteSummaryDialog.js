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

/**
 * Site Summary Dialog
 */
;(function ($) {

    "use strict";

    var $content, order, minWidth, make3columnAt;

    /** $content is the site summary dialog content container */
    $content = null;

    /** order the missing assets sub-sections as follows */
    order = ['missing-page', 'missing-asset', 'missing-css'];

    /** minWidth of dialog just in case we add resizable */
    minWidth = 700;

    /** make3columnAt number of items to add a column */
    make3columnAt = 16;

    // Public API
    $.PercSiteSummaryDialog = {
        open: createDialogAndOpen
    };

    /** get the site id of the current navigator context */
    function getSiteId () {
        return $.PercNavigationManager.getSiteName();
    }

    /** attempt to localize code values */
    function localize (key) {
        return I18N.message(key, key);
    }

    /** get localized text from templates otherwise use key itself */
    function heading (key) {
        var $el = $('[templates] .perc-site-summary-' + key).clone();
        if ($el.length === 0) {
            $el = $('[templates] .perc-site-summary-missing-unknown').clone();
            $el.html(localize(key));
        }
        return $el;
    }

    /** render the stats and adjust columns */
    function displayStats (stats) {
        var key, $div, $ui = $('<div></div>'), count = 0;
        for (key in stats) {
            $div = $('<table class="perc-stat"></table>').appendTo($ui);
            $div = $('<tr></tr>').appendTo($div);
            $('<td class="perc-stat-label"></td>').html(localize(key)).appendTo($div);
            $('<td class="perc-stat-value"></td>').html(stats[key]).appendTo($div);
            count = count + 1;
        }
        if (count > make3columnAt) {
            $ui.addClass('perc-3column');
        }
        return $ui;
    }

    /** render the warnings and add sub-headings */
    function displayWarnings (warnings) {
        var i, key, warning, $div, $ui = $('<div>'), groups = {};
        // group by type
        if(typeof(warnings) !== 'undefined'){
            for (i = 0; i < warnings.length; i++) {
                warning = warnings[i];
                $div = groups[warning.type] || (groups[warning.type] = $('<div>'));
                $div = $('<dl>').appendTo($div);
                $('<dt>').append($('<small>').html(I18N.message("perc.ui.siteSummary@Referenced by") + ": " + warning['refUri'])).appendTo($div);
                $('<dd>').text(warning.suggestion).appendTo($div);
            }
        }
        // put groups in display order
        for (i = 0; i < order.length; i++) {
            $div = groups[order[i]];
            if ($div && $div.length) {
                heading(order[i]).appendTo($ui);
                $div.appendTo($ui);
                delete groups[order[i]];
            }
        }
        // display any miscellaneous ones
        for (key in groups) {
            $div = groups[key];
            if ($div && $div.length) {
                heading(key).appendTo($ui);
                $div.appendTo($ui);
            }
        }
        return $ui;
    }
    /** render site summary abridged log message */
    function displayAbridgedLogMessage (message) {
        var $div = $('<div>');
        if (message) {
            $div.text(message);
        }
        return $div;
    }
    /** given site summary data render the dialog sections */
    function renderSiteSummaryData (status, siteSummaryData) {
        var $ui;

        // TODO: may want to account for status here, too

        // render site statistics
        $ui = $content.find('.perc-site-summary-statistics .ui-widget-content').empty();
        displayStats(siteSummaryData.statistics).appendTo($ui);

        // render log truncated message at the top if exists other wise clear the perc-log-header class.
        if (siteSummaryData.abridged_log_message) {
            $ui = $content.find('.perc-site-summary-warnings .perc-log-top').empty().addClass("perc-log-header");
            displayAbridgedLogMessage(siteSummaryData.abridged_log_message).appendTo($ui);
        }
        else
        {
            $content.find('.perc-site-summary-warnings .perc-log-top').empty().removeClass("perc-log-header");
        }

        // render site warning messages
        $ui = $content.find('.perc-site-summary-warnings .perc-log').empty();
        displayWarnings(siteSummaryData.issues).appendTo($ui);

        // render log truncated message otherwise leave default end of file message
        if (siteSummaryData.abridged_log_message) {
            $ui = $content.find('.perc-site-summary-warnings .perc-log-footer').empty();
            displayAbridgedLogMessage(siteSummaryData.abridged_log_message).appendTo($ui);
        }

    }

    /** given site summary data, open dialog and display */
    function createDialogAndOpen(status, siteSummaryData) {
        if(!getSiteId())
        {
            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.site.summary.dialog@Select a Site")});
            return;
        }
        // handle error status
        if(status === $.PercServiceUtils.STATUS_ERROR) {
            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(siteSummaryData.request);
            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: defaultMsg});
            return;
        }
        var title = $('[templates] .perc-site-summary-dialog-title').html();
        $content = $('[templates] .perc-site-summary').clone();

        var percDialogObject = {
            id: "perc-site-summary-dialog",
            title: title,
            modal: true,
            width: minWidth,
            height: 578,
            resizable : false,
            closeOnEscape: true,
            percButtons: {},
            close: function (event , ui) {
                $(this).dialog('destroy').remove();
            },
            open: function (event, ui) {
                renderSiteSummaryData(status, siteSummaryData);
            }
        };

        percDialogObject.percButtons["Close"] = {
            click: function () {
                $(this).dialog('close');
            },
            id: "perc-site-summary-dialog-close"
        };

        $content.perc_dialog(percDialogObject);
    }

    /** event listener for opening the site summary dialog */
    function onOpenDialog (event) {
        event.preventDefault();
        $.PercSiteSummaryService.getSiteSummaryData(getSiteId(), createDialogAndOpen);
    }

    /** event listener for refreshing the site summary dialog */
    function onRefreshDialog (event) {
        event.preventDefault();
        $.PercSiteSummaryService.getSiteSummaryData(getSiteId(), renderSiteSummaryData);
    }

    /** event listener for printing the site summary dialog */
    function onPrintDialog (event) {
        event.preventDefault();
        $content.printThis();
    }

    /** event listener for toggling the site summary sections */
    function onToggleSection (event) {
        var $this = $(this), $parent = $this.parents('.perc-section:first'), $target;
        $target = $parent.find('.ui-widget-content');
        $target.toggleClass('perc-hide');
        if ($target.hasClass('perc-hide')) {
            $this.removeClass('perc-section-open').addClass('perc-section-closed');
        } else {
            $this.removeClass('perc-section-closed').addClass('perc-section-open');
        }
    }

    /** initialize the UI event listeners for this dialog */
    $(function () {
        $('body').on('click', '.perc-site-summary h2 a', onToggleSection);
        $('body').on('click', '.perc-action-print', onPrintDialog);
        $('body').on('click', '.perc-action-refresh', onRefreshDialog);
        $('body').on('click', '.perc-site-summary-action.perc-open-dialog', onOpenDialog);
    });

})(jQuery);
