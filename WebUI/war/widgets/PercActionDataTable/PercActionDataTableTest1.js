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