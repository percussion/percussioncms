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

var MODULE_ID = '';
var iFrame = $(percJQuery.find("#remote_iframe_" + MODULE_ID));

$(function() {
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
            $('#perc-bulk-target-collapser').trigger("blur");
        },
        function() {
            gadgets.window.adjustHeight(50 + $("#perc-bulk-form").height() + $("#perc-buttonbar").height() + $("#perc-buttonbar2").height());
        }
    );

    $('#perc-bulk-details-collapser').trigger("click")
        .collapser(
        {
            target: '#perc-bulk-details-section',
            expandClass: 'perc-bulk-expand',
            collapseClass: 'perc-bulk-collapse',
            changeText: false
        },
        function() {
            $('#perc-bulk-details-collapser').trigger("blur");
        },
        function() {
            var tableHeight = $('#perc-added-files').height();
            iFrame.height(450 + tableHeight);
        }
    );
});
