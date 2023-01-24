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

(function($)
{
    var resizable;
    var hide;
    var show;
    var fixedTable = null;
    var table;
    var configo;
    $.fn.PercFixedTableHeader = function(config)
    {
        $(".perc-fixedtableheader").remove();

        configo = config;
        table = $(this);
        var tableIndex = table.css("z-index");
        if(tableIndex == "auto")
            tableIndex = "1000";

        // clone the table for which we are making the header
        // blow away the body and just keep the header with all its styles and event bindings
        
        // Detect the browser and set the correct top:value for the fixed header Table. 
        if($.browser.msie ||$.browser.chrome ||$.browser.safari) {
            var topValue = "-3px";
        }
        else {
            var topValue = "1px";
        }
        
        fixedTable = $(this).clone(true);
        fixedTable.find("tbody").remove();
        fixedTable
            .css("position","relative")
            .css("margin-bottom","-30px")
            .css("top",topValue)
            .css("left","1px")
            .css("z-index", tableIndex + 1)
            .css("background","white")
            .attr("cellpadding", "0")
            .addClass("perc-fixedtableheader");

        configo.container.prepend(fixedTable);

        // blow away this widget if you click on a given element
        remove = config.remove;
        if(remove)
        {
            remove.on("click", function()
            {
                fixedTable.remove();
            });
        }

        // update the width of this widget if the window or container resizes resizes
        resizable = config.resizable;
        if(resizable)
        {
            resizable.on("resize", function()
            {
                update();
            });
        }

        // update this widget if the window is resized
        $(window).on("resize",function()
        {
            update();
        });

        function update()
        {
            // It is preferable to set the width of tables using CSS, than usding $(table).width()
            // since it is more cross browser compatible, specially when dealing with hidden
            // elements.
            fixedTable.css('width', table.css('width'));
        }

        update();
    };
})(jQuery);
