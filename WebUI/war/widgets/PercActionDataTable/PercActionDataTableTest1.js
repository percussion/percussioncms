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

/**
 *  PercActionDataTable.js
 *  @author Jose Annunziato
 */

$(document).ready(function(){
        var data = [
            {rowContent : [["Comment 1","/Site/Site2"],"12/22/33"], rowData : {pageId : 1123, pagePath : 4123, commentId : 7123}},
            {rowContent : [["Comment 2","/Site/Site3"],"13/22/33"], rowData : {pageId : 2123, pagePath : 5123, commentId : 8123}},
            {rowContent : [["Comment 3","/Site/Site4"],"14/22/33"], rowData : {pageId : 3123, pagePath : 6123, commentId : 9123}}
        ];
            
        console.log($.PercPageActions);
            
        var headers = ["Page 1","Heading 22"];
        config = {percData : data, percHeaders : headers, percMenus : $.PercPageActions};
        $("#myTable").PercActionDataTable(config);
});
