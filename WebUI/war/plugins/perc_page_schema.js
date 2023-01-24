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
