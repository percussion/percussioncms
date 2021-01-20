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
 * define the pagemanager functions, to interface with services on the server side.
 */


(function($){

    /**
     * Use the supplied metadata parameters to create a new page.
     */
    function create_new_page( path, params, k, err ) {

        if (!$.perc_fakes.page_service.create_page) {
            $.perc_pathmanager.get_folder_path(path, function(fp){
                createPage(fp, params, k, err)
            }, err);
        }
        else {
            if (false)
                err(I18N.message("perc.ui.page.manager@Create New Page Error"));
            else
                k('16777215-101-733');
        }
    }
    function createPage(folderPath, params, k, err){
        var myObj = {};
        $.each(params, function(){
            myObj[this.name] = this.value;
        });
        var createPath = null;
        var passIn = null;
        var addToRecent = myObj['addToRecent']?true:false;
        if (myObj['landingpage']) {
            createPath = $.perc_paths.SECTION_CREATE;
            pass_in = {
                'CreateSiteSection': {
                    'pageName': myObj['page_name'],
                    'pageTitle': myObj['page_title'],
                    'templateId': myObj['template'],
                    'pageUrlIdentifier': myObj['page_name'],
                    'pageLinkTitle': myObj['page_linktext'],
                    'folderPath': folderPath,
                    'addToRecent':true
                }
            };
        }
        else {
            createPath = $.perc_paths.PAGE_CREATE;
            pass_in = {
                'Page': {
                    'name': myObj['page_name'],
                    'title': myObj['page_title'],
                    'templateId': myObj['template'],
                    'linkTitle': myObj['page_linktext'],
                    'folderPath': folderPath,
                    'addToRecent':true
                }
            };
        }
        $.ajax({
            dataType: 'json',
            data: JSON.stringify(pass_in),
            contentType: 'application/json',
            type: 'POST',
            url: createPath,
            success: k,
            error: err
        });
    }
    function render_page( id, k, err ) {
        $.ajax( {
            url: $.perc_paths.PAGE_EDIT + "/" + id,
            type: 'GET',
            success: k,
            error: err,
            dataType: 'text' });
    }

    function delete_page( id, callback, errorCallback )
    {
        $.ajax(
            {
                url: $.perc_paths.PAGE_DELETE + "/" + id,
                type: 'DELETE',
                success: function() {
                    callback();
                },
                error: errorCallback
            });
    }
    /*
    @param id {String} - name of the site to be deleted
    @param callback {function()	{}} - defines action to be performed on success (when return status = 200)
    @param errorCallback {function( data, textStatus, errorThrown)	{}} - defines action to be performed on failure (when return status != 200)
    */
    function delete_site( id, callback, errorCallback )
    {
        $.ajax(
            {
                url: $.perc_paths.SITE_DELETE + "/" + id,
                type: 'DELETE',
                success: callback,
                error: errorCallback
            });
    }


    function new_asset(path, folder_spec, callback, errorCallback)
    {
        if( !$.perc_fakes.page_service.new_asset )
        {
            errorCallback( I18N.message("perc.ui.page.manager@New Asset Not Implemented") );
        }
        else
        {
            //Call callback with asset id
            callback('54321');
        }
    }

    function get_widget_ctypes( page_id, is_page, callback, errorCallback )
    {
        // if we are not using mocked data,
        // get the asset widget drop criteria from the REST service
        if( !$.perc_fakes.page_service.get_widget_ctypes )
        {
            function parseAssetDropCriteria( json )

            {
                var assetDropCriteria = {};
                $.each( json, function()
                {
                    assetDropCriteria[ this.widgetId ] = this.supportedCtypes;
                });
                callback( assetDropCriteria );
            }

            $.ajax(
                {
                    url: $.perc_paths.ASSET_WIDGET_DROP_CRITERIA + page_id + "/" + is_page,
                    type: 'GET',
                    dataType: 'json',
                    success: parseAssetDropCriteria,
                    error: errorCallback
                });
        }

        // otherwise use mocked data
        else
        {
            var testContentTypes = { '12345' : ['percPage', 'article'], '4567': [] };
            callback( testContentTypes );
        }
    }

    function save_page(pageId, pageObject, callback){
        $.ajax( {
            url: $.perc_paths.PAGE_CREATE + "/",
            type: 'POST',
            contentType: 'application/xml',
            data: pageObject,
            dataType: "xml",
            processData: false,
            success: callback,
            error: function(data, textstatus, error){
                $.unblockUI();
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: $.PercServiceUtils.extractDefaultErrorMessage(data)});
            }
        });
    }

    function load_page(pageId, callback) {
        // show an hour glass cursor when page is still loading
        $.ajax({
            url:      $.perc_paths.PAGE_CREATE + "/" + pageId,
            type:     'GET',
            dataType: 'text',
            accepts: {
                text: "application/xml"
            },
            success:  callback,
            error:    function(data, textstatus, error) {
                // remove the hour glass if there was an error loading the page
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: $.PercServiceUtils.extractDefaultErrorMessage(data)});
            }
        });
    }

    /**
     * Renders an individual region of a page. Called by PercPageModel.render()
     * @argument regionId is the id of the outermost region to be rendered. Result HTML includes the HTML
     * of all enclosing regions within regionId
     * @argument pageObject is an instance of PercPageModel which contains the current state of the page
     * including all new widgets, regions, etc. The state is maintained in the client so that we can cancel
     * all changes before persisting.
     */
    function render_region(regionId, pageObject, callback ) {

        $.ajax({
            url          : $.perc_paths.PAGE_PREVIEW + regionId,
            type         : "POST",
            contentType  : "application/xml",
            dataType     : "xml",
            data         : pageObject,
            processData  : false,
            success      : callback,
            error        : function(data, textstatus, error) {

                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: $.PercServiceUtils.extractDefaultErrorMessage(data)});
            }
        });
    }

    $.perc_pagemanager = {
        save_page : save_page,
        render_region : render_region,
        load_page : load_page,
        create_new_page : create_new_page,
        render_page: render_page,
        new_asset : new_asset,
        delete_page : delete_page,
        delete_site : delete_site,
        get_widget_ctypes : get_widget_ctypes,
        createPage : createPage
    };

})(jQuery);
