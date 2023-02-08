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
