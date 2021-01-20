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

(function($){

var region_schema, code_schema, template_schema;


code_schema = {templateCode: '$'};

region_schema  = {
   'regionId': '$',
   'attributes':  [{'name':'$','value':'$'}],
   'startTag': '$',
   'children': [function(tag){
                if(tag == 'region')
                   return region_schema;
                if(tag == 'code')
                   return code_schema;
                alert("Unhandled case");
                return '$';}],
   'endTag': '$',
   'cssClass':'$'};


template_schema = {'Template': {
                  'id':'$',
                  'description':'$',
                  'imageThumbPath':'$',
                  'label':'$',
                  'name':'$',
                  'readOnly':'$',
                  'sourceTemplateName': '$',
                  'bodyMarkup': '$',
                  'cssOverride': '$',
                  'cssRegion': '$',
                  'type': '$',
		  'theme' : '$',
                  'htmlHeader': '$',
                  'regionTree': {
                     'rootRegion': region_schema,
                     'regionWidgetAssociations': [{
                           'regionId' : '$',
                           'widgetItems' : [{
                                 'id': '$',
                                 'definitionId': '$',
                                 'name': '$',
                                 'description': '$',
                                 'properties': [{'name':'$','value':'$'}],
                                 'cssProperties': [{'name':'$','value':'$'}]}]}]}}};


var test_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
   + "<Template>"
   + "<id>16777215-101-710</id>"
   + "<description></description>"
   + "<imageThumbPath></imageThumbPath>"
   + "<label>Plain</label>"
   + "<name>Test3 Templ</name>"
   + "<readOnly>false</readOnly>"
   + "<sourceTemplateName>perc.base.plain</sourceTemplateName>"
   + "<bodyMarkup>#perc_templateHeader()"
   + "&lt;div class=&quot;perc-region&quot; id=&quot;container&quot; title=&quot;container&quot;&gt;&lt;div class=&quot;perc-horizontal&quot;&gt;&lt;div class=&quot;perc-region&quot; id=&quot;temp-region-20&quot; title=&quot;temp-region-20&quot;&gt;&lt;div class=&quot;perc-horizontal&quot;&gt;&lt;div class=&quot;perc-region perc-region-leaf&quot; id=&quot;temp-region-22&quot; title=&quot;temp-region-22&quot;&gt;&lt;div class=&quot;perc-vertical&quot;&gt;&lt;/div&gt;&lt;/div&gt;&lt;div class=&quot;perc-region perc-region-leaf&quot; id=&quot;temp-region-23&quot; title=&quot;temp-region-23&quot;&gt;&lt;div class=&quot;perc-vertical&quot;&gt;&lt;/div&gt;&lt;/div&gt;&lt;div class=&quot;clear-float&quot;&gt;&lt;/div&gt;&lt;/div&gt;&lt;/div&gt;&lt;div class=&quot;perc-region perc-region-leaf&quot; id=&quot;temp-region-21&quot; title=&quot;temp-region-21&quot;&gt;&lt;div class=&quot;perc-vertical&quot;&gt;&lt;/div&gt;&lt;/div&gt;&lt;div class=&quot;clear-float&quot;&gt;&lt;/div&gt;&lt;/div&gt;&lt;/div&gt;"
   + "#perc_templateFooter()</bodyMarkup>"
   + "<cssOverride></cssOverride>"
   + "<cssRegion></cssRegion>"
   + "<htmlHeader></htmlHeader>"
   + "<regionTree>"
   + "<regionWidgetAssociations/>"
   + "<rootRegion>"
   + "<regionId>percRoot</regionId>"
   + "<children>"
   + "<code>"
   + "<templateCode>#perc_templateHeader()"
   + "</templateCode>"
   + "</code>"
   + "<region>"
   + "<regionId>container</regionId>"
   + "<startTag>&lt;div class=&quot;perc-region&quot; id=&quot;container&quot; title=&quot;container&quot;&gt;</startTag>"
   + "<children>"
   + "<code>"
   + "<templateCode>&lt;div class=&quot;perc-horizontal&quot;&gt;</templateCode>"
   + "</code>"
   + "<region>"
   + "<regionId>temp-region-20</regionId>"
   + "<startTag>&lt;div class=&quot;perc-region&quot; id=&quot;temp-region-20&quot; title=&quot;temp-region-20&quot;&gt;</startTag>"
   + "<children>"
   + "<code>"
   + "<templateCode>&lt;div class=&quot;perc-horizontal&quot;&gt;</templateCode>"
   + "</code>"
   + "<region>"
   + "<regionId>temp-region-22</regionId>"
   + "<startTag>&lt;div class=&quot;perc-region perc-region-leaf&quot; id=&quot;temp-region-22&quot; title=&quot;temp-region-22&quot;&gt;</startTag>"
   + "<children>"
   + "<code>"
   + "<templateCode>&lt;div class=&quot;perc-vertical&quot;&gt;&lt;/div&gt;</templateCode>"
   + "</code>"
   + "</children>"
   + "<endTag>&lt;/div&gt;</endTag>"
   + "</region>"
   + "<region>"
   + "<regionId>temp-region-23</regionId>"
   + "<attributes><attribute name='data-att-test-name' value='data-att-test-value'/></attribute></attributes>"
   + "<startTag>&lt;div class=&quot;perc-region perc-region-leaf&quot; id=&quot;temp-region-23&quot; title=&quot;temp-region-23&quot;&gt;</startTag>"
   + "<children>"
   + "<code>"
   + "<templateCode>&lt;div class=&quot;perc-vertical&quot;&gt;&lt;/div&gt;</templateCode>"
   + "</code>"
   + "</children>"
   + "<endTag>&lt;/div&gt;</endTag>"
   + "</region>"
   + "<code>"
   + "<templateCode>&lt;div class=&quot;clear-float&quot;&gt;&lt;/div&gt;&lt;/div&gt;</templateCode>"
   + "</code>"
   + "</children>"
   + "<endTag>&lt;/div&gt;</endTag>"
   + "</region>"
   + "<region>"
   + "<regionId>temp-region-21</regionId>"
   + "<startTag>&lt;div class=&quot;perc-region perc-region-leaf&quot; id=&quot;temp-region-21&quot; title=&quot;temp-region-21&quot;&gt;</startTag>"
   + "<children>"
   + "<code>"
   + "<templateCode>&lt;div class=&quot;perc-vertical&quot;&gt;&lt;/div&gt;</templateCode>"
   + "</code>"
   + "</children>"
   + "<endTag>&lt;/div&gt;</endTag>"
   + "</region>"
   + "<code>"
   + "<templateCode>&lt;div class=&quot;clear-float&quot;&gt;&lt;/div&gt;&lt;/div&gt;</templateCode>"
   + "</code>"
   + "</children>"
   + "<endTag>&lt;/div&gt;</endTag>"
   + "</region>"
   + "<code>"
   + "<templateCode>"
   + "#perc_templateFooter()</templateCode>"
   + "</code>"
   + "</children>"
   + "</rootRegion>"
   + "</regionTree>"
   + "<theme></theme>"
   + "</Template>";

$.perc_schemata = $.perc_schemata || {};

$.extend( $.perc_schemata, {template : template_schema, template_test_data: test_xml } );

})(jQuery);
