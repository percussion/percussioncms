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
 * Handles the region css changes performed while editing a template. It basically performs the CRUD operations
 * to update those properties.
 */
(function($, P){

    var oTable;
    
    var _layoutFunctions = null;
    var _iframe = null;
    var _region = null;
    var _themeName = "";
    var _templateName = "";
    var _templateId = "";
    var tempDeletedRegions = [];
       
    $.PercRegionCSSHandler = {
        init: init,
        getRegionCSS: getRegionCSS,
        saveRegionCSS: saveRegionCSS,
        mergeRegionCSS: mergeRegionCSS,
        prepareForEditRegionCSS: prepareForEditRegionCSS
    };
    
    /**
     * Initializes the handler and the variables involved.
     * @param layoutFunctions (object) - the functions exposed in PercLayoutView.js to work with the layout view
     * @param model (object) - the template model
     * @param iframe (string) - the iframe containing the template's editor html.
     * @param region (object) - the region object (retrieved from the model) to perform operations on
     */
    function init(layoutFunctions, model, iframe, region){
        _layoutFunctions = layoutFunctions;
        _iframe = iframe;
        _region = region;
        _themeName = model.getTemplateObj().Template.theme; 
        _templateName = model.getTemplateObj().Template.name;
        _templateId = model.getTemplateObj().Template.id;
        
        getRegionCSS(_region.attr("id"),
            function(status, data) 
            {
                getRegionCallback(status, data, _region.attr("id"));
            });
        
        
        function getRegionCallback(status, data, id)
        {
            if(status == $.PercServiceUtils.STATUS_SUCCESS)
            {
                var percData = [];
                var row;
                if (data.RegionCSS.properties != null)
                {
                    var regionCSSProperties = $.perc_utils.convertCXFArray(data.RegionCSS.properties);
                    for (var i = 0; i < regionCSSProperties.length; i++)
                    {
                        row = {rowContent : [regionCSSProperties[i].name,regionCSSProperties[i].value]};
                        percData.push(row);
                    }
                }
                
                var config = {
                        percColumnWidths : ["300","300"],
                        percHeaders : ["CSS Property","Value"],
                        percEditableCols : [true, true],
                        percData : percData,
                        percDeleteRow : true,
                        percAddRowElementId : "perc-new-css-property",
                        percNewRowDefaultValues : ['', ''],
                        percPlaceHolderValues : ['Enter property name', 'Enter property value'],
                        aoColumns: [{sType: "string"}, {sType: "string"}],
                        bDestroy : true
                    };
                
                oTable = $.PercInlineEditDataTable.init($('#perc-region-css-table'), config);
                
                $('#perc-region-edit').dialog('open');
                
            }
            else
            {
                return;
            }
        }
    }
    
    /**
     * Gets the outer most region asociated to the current template. This is needed to identify the rule
     * correctly as the format is the following (in perc-region.css):
     *   #<outerMostRegionName>.perc-region #<regionName>.perc-region
     * Eg:#container.perc-region #header1.perc-region
     */
    function getOuterMostRegion ()
    {
        return _iframe.contents().find('.perc-region:first').attr('id');
    }

    /**
     * Calls the service responsible for retrieving the region's css properties from the temp file.
     * @param id - the id of the region to retieve the rule
     * @param callback (function) callback function to be invoked when ajax call returns
     */    
    function getRegionCSS(id, callback)
    {
        $.PercTemplateService().getRegionCSS(
        _themeName, 
        _templateName,
        getOuterMostRegion(),
        id,
            function(status, data) 
            {
                callback(status, data, id);
            });
    }
    
    /**
     * Calls the service responsible for saving the region's css properties in the temp file.
     * It retrieves the data from the Inline edit datatable asociated to the dialog.
     * @param id - the id of the region to save the properties
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function saveRegionCSS(id)
    {
        var properties = [];
        var data = oTable.fnGetData();
        if (data.length == 1 && $.trim(data[0][0]) == "")
            data.pop();
        if (data.length > 0)
        {
            for (var i = 0; i < data.length; i++)
            {
                properties.push({"name": $.trim($(data[i][0]).text()), "value": $.trim($(data[i][1]).text())});
            }
            
            var regionCSSObject = {
                    "RegionCSS":{
                        "properties": properties
                        ,"regionName": id
                        ,"outerRegionName": getOuterMostRegion()
                        }}; 
            $.PercTemplateService().saveRegionCSS(
                    _themeName, 
                    _templateName, 
                    regionCSSObject,
                    function(status, data) {
                        if(status == $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            tempDeletedRegions = jQuery.grep(tempDeletedRegions, function(value) {
                                return value != id;
                            });
                            reloadRegionCSSFile();
                        }
                    });
        }
        else
        {
            $.PercTemplateService().deleteRegionCSS(
                _themeName, 
                _templateName, 
                getOuterMostRegion(),
                id,
                function(status, data) {
                    if(status == $.PercServiceUtils.STATUS_SUCCESS)
                    {
                        tempDeletedRegions.push(id);
                        reloadRegionCSSFile();
                    }
                });
        }        
    }
    
    /**
     * Updates the css <LINK> dom attribute to display the css changes on the fly (without reloading the iframe content).
     * It basically replaces the following path link element: <theme_name>/perc/perc_region.css, with a new one including
     * a time query param to avoid browser caching.
     */        
    function reloadRegionCSSFile()
    {
        var elem = _iframe.contents().find("link[href*='" + _themeName + "/perc/perc_region.css']");
        if (elem.length > 0)
        {
            var date = new Date();
            var timestamp = date.getTime();
            var href = elem.attr("href");
            href = href.split("?")[0];
            href = href + "?time=<value>";
            href = href.replace("<value>", timestamp);
            var linkElem = $("<link>")
                                .attr("href", href)
                                .attr("media", "all")
                                .attr("type", "text/css")
                                .attr("rel", "stylesheet");
            elem.replaceWith(linkElem);
        }
    }
    
    /**
     * Calls the service responsible for merging the region's css properties from the temp file
     * to the master one.
     */
    function mergeRegionCSS()
    {
        if (_themeName == null || _themeName == "" || _templateName == null || _templateName == "")
        {
        	return false;
        }
    	
    	var deletedRegionsJSON = {"RegionCssList":null};
        var deletedRegionsObjects = [];
        var deletedRegions = tempDeletedRegions;
        if (deletedRegions != null && deletedRegions.length > 0)
        {
            for (var i = 0; i < deletedRegions.length; i++)
            {
                var regionCSSObject = {
                    "properties": null
                    ,"regionName": deletedRegions[i]
                    ,"outerRegionName": getOuterMostRegion()
                }; 
                deletedRegionsObjects.push(regionCSSObject);
            }
            deletedRegionsJSON = {"RegionCssList": {"regions":deletedRegionsObjects}};
        }
        
        $.PercTemplateService().regionCSSMerge(
                _themeName, 
                _templateId,
                deletedRegionsJSON,
                function(status, data) {});
    }

    /**
     * Calls the service responsible for copying the master file content to the temp file (when
     * the template is opened for editing)
     */    
    function prepareForEditRegionCSS()
    {
        $.PercTemplateService().regionCSSPrepareForEdit(
                _themeName, 
                _templateName, 
                function(status, data) {});
    }
    
})(jQuery, jQuery.Percussion);
