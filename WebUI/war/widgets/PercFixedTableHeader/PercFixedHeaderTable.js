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
