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
