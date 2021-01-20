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

var MODULE_ID = '';
var iFrame = $(percJQuery.find("#remote_iframe_" + MODULE_ID));

$(document).ready(function() {
    MODULE_ID = $('#fileupload').data('module-id');

    // Initialize the two collapsers used
    $('#perc-bulk-target-collapser').collapser(
        {
            target: '#perc-bulk-target-tree',
            expandClass: 'perc-bulk-expand',
            collapseClass: 'perc-bulk-collapse',
            changeText: false
        },
        function() {
            $('#perc-bulk-target-collapser').blur();
        },
        function() {
            gadgets.window.adjustHeight(50 + $("#perc-bulk-form").height() + $("#perc-buttonbar").height() + $("#perc-buttonbar2").height());
        }
    );

    $('#perc-bulk-details-collapser').click();

    $('#perc-bulk-details-collapser').collapser(
        {
            target: '#perc-bulk-details-section',
            expandClass: 'perc-bulk-expand',
            collapseClass: 'perc-bulk-collapse',
            changeText: false
        },
        function() {
            $('#perc-bulk-details-collapser').blur();
        },
        function() {
            var tableHeight = $('#perc-added-files').height();
            iFrame.height(450 + tableHeight);
        }
    );
});