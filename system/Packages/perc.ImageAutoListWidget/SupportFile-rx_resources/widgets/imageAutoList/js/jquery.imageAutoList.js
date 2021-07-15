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

/**
 *    jquery.imageAutoList.js
 *
 *    Java Script code for the control imageAutoListWidgetControl used by the ImageAutoListWidget
 *     This control used to build the JCR query for the Image Auto List widget, based on
 *    a date range, title contains field, and Asset library location selection dialogue
 *
 *     
 */
 
 
    //Sets the display date in SimpleDate format, based on the Widget config $date_format
    function setDisplayDate (p_dateValue, p_displayDate){
        /**        
         * Since the Rest service for the read the Widgets config $date_format does not exist at the time of building the widget
         * the date format is hard coded for now and the widget $date_format has been commented out in the widget file ImageAutoList.xml
         */

        if(p_dateValue!=null && p_dateValue!="")
        {        
            var dateFormat = "M/dd/yyyy" ;
            var dateObject = new Date(p_dateValue);    
            $('[name="'+p_displayDate+'"]').val(dateObject.format(dateFormat));    
        }
        else
        {
            $('[name="'+p_displayDate+'"]').val("");
        }
    }    
    
    /*
    Validate controls fields
    */
    function validateFields()
    {
        var p_start_date = new Date($('[name="start_date"]').val());
        var p_end_date = new Date($('[name="end_date"]').val());

        if ( p_end_date < p_start_date)
        {
            alert("Please enter a valid date range");    
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /*
     * Build the JCR Query
     */
    function buildQuery()
    {

        var p_titlecontains = $('[name="title_contains"]').val();
        var p_start_date = $('[name="start_date"]').val();

        var jxl_start_date=$.datepicker.formatDate('yy/mm/dd', new Date(p_start_date));

        var p_end_date = $('[name="end_date"]').val();

        var jxl_end_date=$.datepicker.formatDate('yy/mm/dd', new Date(p_end_date));

        var p_asset_library_path = $('[name="asset_library_path"]').val();

        //Sample JCR query 
        //select rx:sys_contentid, rx:sys_folderid from rx:percImageAsset where rx:sys_contentcreateddate >= '2010/01/01' and rx:sys_contentcreateddate <= '2010/01/05' jcr:path like '//Folders/$System$/Assets/Images%'";

        var p_query = "select rx:sys_contentid, rx:sys_folderid from rx:percImageAsset ";

        var queryOp=" where ";

        if (p_asset_library_path!=null && p_asset_library_path!=""){

            //Save asset_library_path
            $('[name="asset_library_path"]').val(p_asset_library_path);

            p_query+=queryOp+"jcr:path like '"+p_asset_library_path+"/%'";
            queryOp=" and ";
        }        
        if (p_titlecontains!=null && p_titlecontains!=""){
            p_query+=queryOp+"rx:displaytitle like '%"+p_titlecontains+"%'";
            queryOp=" and ";
        }
        if (p_start_date!=null && p_start_date!=""){
            p_query+=queryOp+"rx:sys_contentcreateddate >='"+jxl_start_date+"'";
            queryOp=" and ";
        }    

        if (p_end_date!=null && p_end_date!=""){
            p_query+=queryOp+"rx:sys_contentcreateddate <='"+jxl_end_date+"'";
            queryOp=" and ";
        }        

        //Set Query field
        $('[name="query"]').val(p_query);

        return false;    
    } 

    /*
     * jQuery code for the control
     */    
    (function($) {

        $.fn.imageAutoListControl = function(settings) {
            var config = {};
    
    
            if (settings) $.extend(config, settings);
    
            /*
            Folder path select dialogue, which populates the content editor field asset_library_path
            */
            function showAssets()
            {
                $(document).ready(function(){
                    var path = $.PercFinderTreeConstants.convertFolderPathToPath($('[name="asset_library_path"]').val());
                    if(!path)
                       path = $.perc_paths.ASSETS_ROOT;
                    $("#perc-folder-selector").PercFinderTree(
                    {rootPath:$.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                    showFoldersOnly:true,
                    classNames:{container:"perc-folder-selector-container", selected:"perc-folder-selected-item"},
                    height:"250px",
                    width:"300px", 
                    initialPath:path,
                    onRenderComplete: renderComplete,
                    onClick:setPath});
                    function setPath(pathItem)
                    {
                        //Save asset_library_path
                        $('[name="asset_library_path"]').val(pathItem.folderPath);    
                        buildQuery();                
			$.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
                    }
                    function renderComplete()
                    {
                        window.scrollTo(0,0);
                    }
                    
                });
                
            }
            //Callbacks Event Code
    
            $('#perc-content-edit-title_contains').on("change",function(){
                buildQuery();
            });    
    
            //Build query if mouse leaves the form, i.e when the user goes to click on the save button which is not part of the iframe
            $('#perc-content-form').mouseleave(function(){
                buildQuery();
            });
    
            /*
            Initialize the control
            */
            function initializeForm () {
    
                //Set display date range
                setDisplayDate($('[name="start_date"]').val(),"display_start_date");
                setDisplayDate($('[name="end_date"]').val(),"display_end_date");        
                $("#display_title_contains").val($('[name="title_contains"]').val())
                    .on("blur",function(){
                    $('[name="title_contains"]').val($("#display_title_contains").val());
                    buildQuery();
                })
                    .on("change",function(){
                    $('[name="title_contains"]').val($("#display_title_contains").val());
                    buildQuery();
                });
                addDelToDateControls();
                showAssets();
            }     
            
            /**
             * The date fields are read only and there is no way to clear the existing date. The following function
             * allows the Del key press to clear the date values.
             */
            function addDelToDateControls()
            {
                $('#display_end_date').on("keydown",function(evt){
                    var rawCode = evt.charCode ? evt.charCode : evt.which;
                    if(rawCode===46 || rawCode===8)
                    {
                        $('#display_end_date').val("");
                        $('[name="end_date"]').val("");
                        buildQuery();
                    } else if(rawCode===9) {
			return true;
		    } else {
			return false;
		    }
                });
                $('#display_start_date').on("keydown",function(evt){
                    var rawCode = evt.charCode ? evt.charCode : evt.which;
                    if(rawCode===46 || rawCode===8)
                    {
                        $('#display_start_date').val("");
                        $('[name="start_date"]').val("");
                        buildQuery();
                    } else if(rawCode===9) {
			return true;
                    } else {
			return false;
		    }
                });
            }
            
            initializeForm();
            return $(this);

        };

    })(jQuery);     
