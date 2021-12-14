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

$(function()
{
    $.fn.PercFixedHeaderTable = function()
    {
        var table = $(this);
        var header = $(table.find("thead tr td"));
        console.log(header);
        console.log(header.length);
        
 //       for(header.
        
        var fixedHeaderDiv = $("<div>")
            .css("background","red")
            .css("position","absolute")
            .append("hello");
        $("body").append(fixedHeaderDiv);
        
        function update()
        {
            var tablePosition = table.offset();
            var tableWidth    = table.width();
            var tableHeight   = table.height();
            console.log(tablePosition);
            console.log(tableWidth);
            fixedHeaderDiv
                .css("background", "blue")
                .width(tableWidth)
                .css("left", tablePosition.left+"px")
                .css("top", tablePosition.top+"px");
        }
        
        update();
        return update;
    }
});
