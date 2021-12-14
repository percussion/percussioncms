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

$(function(){
        var data1 = [
            {rowContent : [[{content : "Comment 000 Comment 000 Comment 000 Comment 000 Comment 000 Comment 000 Comment 000 Comment 000", title : "/Site/Site22", callback : function(){}},"/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 21","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 31","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 41","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 51","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 61","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 71","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 81","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 91","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 01","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 12","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 13","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 14","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 15","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 16","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 17","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 18","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 19","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 10","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 113","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 114","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 115","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 11","/Site/Site22"],["12/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 22","/Site/Site33"],["13/22/33","22:44:55 PM"]]},
            {rowContent : [["Comment 33","/Site/Site44"],["14/22/33","22:44:55 PM"]]}
        ];
        var headers = ["Page","Heading 2"];
        var config = {percData : data1, percHeaders : headers, percVisibleColumns : [0,1]};
        $("#myTable").PercPageDataTable(config);
//        $("#myTable").PercDataTable(config);
        
        var data2 = [
            {rowContent : [["Comment 1","/Site/Site2"],"12/22/33"], rowData : {pageId : 1123, pagePath : 4123, commentId : 7123}},
            {rowContent : [["Comment 2","/Site/Site3"],"13/22/33"], rowData : {pageId : 2123, pagePath : 5123, commentId : 8123}},
            {rowContent : [["Comment 3","/Site/Site4"],"14/22/33"], rowData : {pageId : 3123, pagePath : 6123, commentId : 9123}}
        ];
//        config = {percRowDblclickCallback : rowDblclickCallback, percRowClickCallback : rowClickCallback, percData : data2, percHeaders : headers};
        var headers = ["Page 1","Heading 22"];
        config = {percData : data2, percHeaders : headers};
        $("#myTable2").PercPageDataTable(config);
    })
    
    function rowClickCallback(row) {
        console.log("rowClickCallback");
        console.log(row);
    }
    
    function rowDblclickCallback(row) {
        console.log("rowDblclickCallback");
        console.log(row);
    }
