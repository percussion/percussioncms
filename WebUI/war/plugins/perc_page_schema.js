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

(function($){

$.perc_schemata = $.perc_schemata || {};

/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Page>
    <id>16777215-101-1218</id>
    <category>PAGE</category>
    <folderPaths>//Sites/T10</folderPaths>
    <name>T10</name>
    <type>percPage</type>
    <folderPath>//Sites/T10</folderPath>
    <author>authorName</author>
    <linkTitle>T10</linkTitle>
    <templateId>16777215-101-1215</templateId>
    <title>T10</title>
    <additionalHeadContent>head</additionalHeadContent>
    <afterBodyStartContent>start</afterBodyStartContent>
    <beforeBodyCloseContent>end</beforeBodyCloseContent>
    <regionBranches>
        <regionWidgetAssociations/>
        <regions/>
    </regionBranches>
</Page>
*/

var test_xml = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
       '<Page>' +
                '<name>Page Name</name>' +
                '<templateId>2000</templateId>' +
                '<folderPath>//folderpath</folderPath>' +
                '<author>authorName</author>' +
                '<id>1000</id>' +
                '<category>PAGE</category>' +
                '<regionBranches>' +
                      '<regionWidgetAssociations>' +
                            '<regionWidget>' +
                                  '<regionId>rid</regionId>' +
                                  '<widgetItems>' +
                                        '<widgetItem>' +
                                              '<definitionId>STUFF</definitionId>' +
                                              '<properties>' +
                                                 '<property>' +
                                                     '<name>AAA</name>' +
                                                     '<value>BBB</value>' +
                                                 '</property>' +
                                              '</properties>' +
                                        '</widgetItem>' +
                                  '</widgetItems>' +
                            '</regionWidget>' +
                      '</regionWidgetAssociations>' +
                      '<regions>' +
                            '<region>' +
                                  '<regionId>templateRegion</regionId>' +
                                  '<children>' +
                                     '<region>' +
                                        '<regionId>rid</regionId>' +
                                        '<children>' +
                                           '<code>' +
                                              "<templateCode>#region('' '' '' '' '')</templateCode>" +
                                           '</code>' +
                                        '</children>' +
                                     '</region>' +
                                  '</children>' +
                            '</region>' +
                      '</regions>' +
                '</regionBranches>' +
       '</Page>';


var region_schema, code_schema, page_schema;


code_schema = {templateCode: '$'};

region_schema  = {
   'regionId': '$',
   'startTag': '$',
   'children': [function(tag){ 
                if(tag === 'region')
                   return region_schema;
                if(tag === 'code')
                   return code_schema;
                alert("Unhandled case");
                return '$';}],
   'endTag': '$'};
page_schema = {'Page': {
                  'id':'$',
                  'category':'$',
                  'folderPaths': '$',
                  'name':'$',
                  'type':'$',
                  'folderPath': '$',
                  'author': '$',
                  'linkTitle': '$',
                  'templateId':'$',
                  'title':'$',
                  'footer':'$',
                  'header':'$',
                  'keywords': ['$'],
                  'description': '$',
                  'tags': ['$'],
                  'additionalHeadContent': '$',
                  'afterBodyStartContent': '$',
                  'beforeBodyCloseContent': '$',
                  'regionBranches': {
                     'regions': [region_schema],
                     'regionWidgetAssociations': [{
                           'regionId' : '$',
                           'widgetItems' : [{
                                 'id': '$',
                                 'definitionId': '$',
                                 'properties': [{'name':'$','value':'$'}],
                                 'cssProperties': [{'name':'$','value':'$'}] }]}]}}};
                       
$.extend( $.perc_schemata, {page : page_schema, page_test_data: test_xml } );

})(jQuery);
