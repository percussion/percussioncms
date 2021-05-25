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
 *    jquery.tagList.js
 *
 *    Java Script code for the control queryListWidgetControl used by the QueryListWidget
 *    This control used to build the JCR query for the any List widget, based on
 *    a date range, title contains field, page template multi select list and Site location selection dialogue
 *
 *     
 */
 
 
    //Sets the display date in SimpleDate format, based on the Widget config $date_format
    function setDisplayDate (p_dateValue, p_displayDate){
        /**        
         * Since the Rest service for the read the Widgets config $date_format does not exist at the time of building the widget
         * the date format is hard coded for now and the widget $date_format has been commented out in the widget file percTagList.xml
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
        var p_query_published_after = new Date($('[name="query_published_after"]').val());
        var p_query_published_before = new Date($('[name="query_published_before"]').val());

        if ( p_query_published_before < p_query_published_after)
        {
            alert("Please enter a valid date range");    
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * Get array of names of all the page templates that are checked. Returns null if nothing is checked.
     */
    function getPageTemplates()
    {
        var pageTpls = [];
        $.each($(".perc-pagetemplates-chkbox"),function(){
            if(this.checked)
                pageTpls.push(this.value); 
        });
        return pageTpls.length>0?pageTpls:null;
    }

    /*
     * Build the JCR Query
     */
    function buildQuery()
    {

		var p_pageresults = $('[name="archive_page_result"]').val();
        var p_titlecontains = $('[name = "query_title_contains"]').val();
        var p_query_published_after = $('[name="query_published_after"]').val();

        var jxl_query_published_after=$.datepicker.formatDate('yy/mm/dd', new Date(p_query_published_after));

        var p_query_published_before = $('[name="query_published_before"]').val();

        var jxl_query_published_before=$.datepicker.formatDate('yy/mm/dd', new Date(p_query_published_before));

        var p_site_path = $('[name="query_site_path"]').val();

        var p_pagetemplates = getPageTemplates();

        //Sample JCR query 
        //select rx:sys_contentid, rx:sys_folderid from rx:percPage where rx:sys_contentpostdate >= '2010/01/01' and rx:sys_contentpostdate <= '2010/01/05' jcr:path like '//Sites/CorporateInvestments%'";

        var p_query = "select rx:sys_contentid, rx:sys_folderid from rx:percPage ";

        var queryOp=" where ";

        if (p_site_path!=null && p_site_path!=""){

            //Save site_path
            $('[name="query_site_path"]').val(p_site_path);

            // CM-43 add the trailing slash
            if(p_site_path.substr(p_site_path.length - 1) != '/')
            {
                p_query += queryOp + "jcr:path like '" + p_site_path + "/%'";
            }
            else
            {
                p_query += queryOp + "jcr:path like '" + p_site_path + "%'";
            }
            queryOp = " and ";
        }
        if (p_titlecontains!=null && p_titlecontains!=""){
            p_query+=queryOp+"rx:page_title like '%"+p_titlecontains+"%'";
            queryOp=" and ";
        }
        if (p_query_published_after!=null && p_query_published_after!=""){
            p_query+=queryOp+"rx:sys_contentpostdate >='"+jxl_query_published_after+"'";
            queryOp=" and ";
        }    

        if (p_query_published_before!=null && p_query_published_before!=""){
            p_query+=queryOp+"rx:sys_contentpostdate <='"+jxl_query_published_before+"'";
            queryOp=" and ";
        }        

        if (p_pagetemplates!=null)
        {

            //Save the seleceted  page templates to query_template_list
            $('[name="query_template_list"]').val(p_pagetemplates.toString());

            p_query+=queryOp+" ( ";
            for(i = 0; i < p_pagetemplates.length; i++)
            {
                p_query+="rx:templateid='"+p_pagetemplates[i]+"'";

                if (i < (p_pagetemplates.length-1))
                {
                    p_query+=" or ";
                }
            }
            p_query+=") ";    
        }

        //Set Query field
        $('[name="query_string"]').val(p_query);

        return false;    
    } 

    /*
     * jQuery code for the control
     */    
    (function($) {

        $.fn.queryListControl = function(settings) {
            var config = {};
    
    
            if (settings) $.extend(config, settings);
    
            /*
            Multi Select list for the Page Templates
            */
            function showPageTemplates()
            {
                ;   
                //make an array from the comma delimited options string
                var pageTemplatesOptionsArray = [];
        
                //Check if page_templates has been defined
                if (typeof $('[name="query_template_list"]').val() != "undefined")
                {   
                    var pageTemplatesOptionsArray = $('[name="query_template_list"]').val().split(",");
                }

                $(document).ready( function(){
                    populateTemplateTypes(getSiteFromFolderPath($('[name="query_site_path"]').val()),pageTemplatesOptionsArray);
                });
        
            }
             function populateTemplateTypes(siteName,pageTemplatesOptionsArray){
                 // has to inject the HTML query result here because $('.ui-perc-querylist-pagetypes').empty();
                 var tplPath = $.perc_paths.TEMPLATES_USER;
                 if(siteName)
                 {
                     tplPath = $.perc_paths.TEMPLATES_BY_SITE + "/" + siteName;
                 }
                 $('.ui-perc-querylist-pagetypes').empty().append($('<div id="perc-pagetemplates-container"/>'));
                 $.getJSON(tplPath, function(data) {
                     var tpls = data.TemplateSummary;
                     for(i=0; i<tpls.length; i++)
                     {
                         var tpl = tpls[i];
                         var checked = $.inArray(tpl.id,pageTemplatesOptionsArray) == -1?"":" checked ='true' ";
                         $("#perc-pagetemplates-container").append($("<div class='perc-pagetemplates-entry'><input type='checkbox' class='perc-pagetemplates-chkbox'" + checked + " value='" + tpl.id + "'></input><span title='"+ tpl.name +"'>" + tpl.name + "</span></div>"));
                     }
                     $(".perc-pagetemplates-chkbox").on("change", function(){
                         var pts = getPageTemplates();
                         pts = pts?pts:"";
                         $('[name="query_template_list"]').val(pts);
                         buildQuery();
                         $.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
                     });
                 });
             }
            /*
            Site and Folder path select diagloue, which populates the content editor field site_path
            */
            function showSites()
            {
                $(document).ready(function(){
                    var path = $.PercFinderTreeConstants.convertFolderPathToPath($('[name="query_site_path"]').val());
                    if(!path)
                       path="/site";
                    $("#perc-folder-selector").PercFinderTree(
                    {rootPath:$.PercFinderTreeConstants.ROOT_PATH_SITES,
                    showFoldersOnly:true,
                    classNames:{container:"perc-folder-selector-container", selected:"perc-folder-selected-item"},
                    height:"250px",
                    width:"300px", 
                    initialPath:path,
                    onRenderComplete: renderComplete,
                    onClick:setPath});
                    function setPath(pathItem)
                    {
                        //Reset the templates if site changes.
                        var oldPath = $('[name="query_site_path"]').val();
                        if(oldPath != pathItem.folderPath && getSiteFromFolderPath(oldPath) != getSiteFromFolderPath(pathItem.folderPath))
                        {
                           populateTemplateTypes(getSiteFromFolderPath(pathItem.folderPath),[]);
                        }
                        //Save site_path
                        $('[name="query_site_path"]').val(pathItem.folderPath);    
                        buildQuery();                
                        $.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
                    }
                    function renderComplete()
                    {
                        window.scrollTo(0,0);
                    }
                    
                });
                
            }
            function getSiteFromFolderPath(folderPath)
            {
                 var siteName = null;
                 if(folderPath && folderPath.indexOf("//Sites/")>-1)
                 {
                     siteName = folderPath.substring(("//Sites/").length);
                     if(siteName.indexOf("/")>-1)
                        siteName = siteName.substring(0,siteName.indexOf("/"));
                 }
                 return siteName;
            }
            //Callbacks Event Code
    
            $('#perc-content-edit-title_contains').on("change", function(){
                buildQuery();
            });    
    
            //Build query if mouse leaves the form, i.e when the user goes to click on the save button which is not part of the iframe
            $('#perc-content-form').on("mouseleave", function(){
                buildQuery();
            });
    
            /*
            Initialize the controls
            */
            function initializeForm () {
    
                //Set display date range
                setDisplayDate($('[name="query_published_after"]').val(),"display_query_published_after");
                setDisplayDate($('[name="query_published_before"]').val(),"display_query_published_before");        
                $("#display_title_contains").val($('[name = "query_title_contains"]').val());
				$("#display_query_page_result").val($('[name="archive_page_result"]').val());
				
				$("#display_query_page_result").on("blur",function(){
                    $('[name="archive_page_result"]').val($("#display_query_page_result").val());
                });
                $("#display_query_page_result").on("change", function(){
                    $('[name="archive_page_result"]').val($("#display_query_page_result").val());
                });
				
                $("#display_title_contains").on("blur", function(){
                    $('[name = "query_title_contains"]').val($("#display_title_contains").val());
                    buildQuery();
                });
                $("#display_title_contains").on("change", function(){
                    $('[name = "query_title_contains"]').val($("#display_title_contains").val());
                    buildQuery();
                });
                showPageTemplates();
                addDelToDateControls();
                showSites();
                
                //Handle the results page browse button click
                $("#perc_querylist_resultspage_browse").on("click", function(evt){
                     var dlgTitle = "Select Results Page";
                     var inputElemId = "display_query_page_result";
                     handleBrowseButtonClick(dlgTitle, inputElemId );               
                });
                $.topFrameJQuery.PercContentPreSubmitHandlers.addHandler(updateQuery);

            }
            
            /**
             * This is a pre submit handler, called by the asset editing framework.
             */
            function updateQuery()
            {
                buildQuery();
                return true;
            }

            
            /**
             * The date fields are read only and there is no way to clear the existing date. The following function
             * allows the Del key press to clear the date values.
             */
            function addDelToDateControls()
            {
                $('#display_query_published_before').keydown(function(evt){
                    var rawCode = evt.charCode ? evt.charCode : evt.which;
                    if(rawCode==46 || rawCode==8)
                    {
                        $('#display_query_published_before').val("");
                        $('[name="query_published_before"]').val("");
                        buildQuery();
                    } else if(rawCode==9) {
			return true;
		    } else {
			return false;
		    }
                });
                $('#display_query_published_after').keydown(function(evt){
                    var rawCode = evt.charCode ? evt.charCode : evt.which;
                    if(rawCode==46 || rawCode==8)
                    {
                        $('#display_query_published_after').val("");
                        $('[name="query_published_after"]').val("");
                        buildQuery();
                    } else if(rawCode==9) {
			return true;
                    } else {
			return false;
		    }
                });
            }
            
            initializeForm();
            $("#ui-datepicker-div").addClass('ui-helper-hidden-accessible');
            return $(this);

        };

        //Function to handle click on browse button.
        function handleBrowseButtonClick(dlgTitle, inputElemId)
        {
            $.perc_browser
                    ({ 
                        on_save: function(spec, closer, show_error)
                        {
                            var pagePath = spec.path;
                            pagePath = pagePath.replace("/Sites/", "");
                            var chopStartPosition = pagePath.indexOf("/");
                            pagePath = pagePath.substring(chopStartPosition);
                            $("#" + inputElemId).val(pagePath);
                            //Update the hidden field for page result
							$('[name="archive_page_result"]').val(pagePath);
                            closer();
                        }, 
                        new_asset_option: false, 
                        selectable_object: "leaf", 
                        new_folder_opt: false, 
                        displayed_containers: "Sites", 
                        asset_name: I18N.message( "perc.ui.saveasdialog.label@Selected Page:" ),
                        title: dlgTitle,
                        save_class: 'perc-save'
                    });       
        }
        
    })(jQuery);     