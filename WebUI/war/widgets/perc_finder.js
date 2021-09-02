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
 * Creates the Content Finder, a top-level element used to explore
 * and interact with the directory structure.
 *
 * The finder is a singleton that can be retrieved by calling
 * $.perc_finder()
 */

var assetPagination = null;

(function($){

    var defaults = {
        // set the starting height of the finder
        height: 200,
        // set the minimum width/height of the finder on resize
        minWidth: 952,
        minHeight: 100,
        get_max_height: function get_max_finder_height () {
            // 100 >= header and finder header areas
            return $(window).innerHeight() - 150;
        }
    };

    if(assetPagination == null) {
        var url =  $.perc_paths.GET_ASSET_PAGINATION_CONFIG ;
        $.ajax( {
            type: 'GET',

            url: url,
            dataType: 'json',
            cache: false,
            success:  function(data){
                assetPagination = data;
            }
        });
    }
    $.perc_finder = function () {

        if($.perc_finderInstance == null)
            $.perc_finderInstance = finder();
        return $.perc_finderInstance;
    };

    function finder() {

        // WARN: expect this method to be called after body onload
        // determine the width of the native scrollbar
        // http://visualpulse.net/forums/index.php?topic=120.0
        $("body").append('<div id="perc-wide_scroll_div_one" style="width:50px;height:50px;overflow-y:hidden;position:absolute;top:-200px;left:-200px;"><div id="wide_scroll_div_two" style="height:100px;width:100%"></div></div>');
        var SCROLLBAR_WIDTH = $("#perc-wide_scroll_div_one").width() - $("#wide_scroll_div_two").innerWidth();
        $("#perc-wide_scroll_div_one").remove();

        // constant for finder listing id prefix
        var FINDER_LISTING_ID_PREFIX = "perc-finder-listing-";
        // maximum result for page, the user could configured this in the future.
        var MAX_RESULTS = 100;
        var flagChangeView = true,
            dragging = false,
            finderExpandStateCookie = "perc-finder-expand-state",
            first_dir = fn_first_dir(),
            top = make_top_level(first_dir),
            ut = $.perc_utils,
            current_path = [],
            currentItem = null;
        actionListeners = [],
            openListeners = [],
            _finderPathIdArray = {},
            path_changed = function(p){ current_path = p; },
            finderOpenInProgress = false,
            lastClickPath = null,
            dragDelay = ($.PercNavigationManager.isAutoTest() ? 0 : 250),
            actions = {
                DELETE: 'delete',
                FINDER_OPEN_START : 'open_start',
                FINDER_OPEN_END: 'open_end'},
            isLibMode = ((typeof gInitialScreen !== 'undefined') && (gInitialScreen === "library"));
        //Preload images
        $.perc_utils.preLoadImages(
            "/cm/images/images/loading.gif"
        );

        function finder_do_goto_or_search (event) {
            event.preventDefault();
            var val, isGoto, $control, $input, isNotAllowed;
            $control = $(this).parents('.perc-finder-goto-or-search');
            $input = $control.find('input.perc-finder-goto-or-search:first');
            val = $input.val();
            isGoto = /^\//.test(val);
            isNotAllowed=/\[/.test(val) || /\]/.test(val);//check if bracket is there in url[]
            if(isNotAllowed) {
                return false;
            }
            if (val) {
                if(isGoto) {
                    //$('#mcol-path-summary').val(val);
                    $('#perc-finder-go-action').trigger('click');
                } else {
                    $('#perc-finder-item-search').val(val);
                    $('#perc-finder-search-submit').trigger('click');
                }
            }
            return false;
        }

        // attach event handlers to the dom
        $('body').on('click', '.perc-action-goto-or-search', finder_do_goto_or_search);
        $.perc_filterField($("#mcol-path-summary"), $.perc_textFilters.PATH);
        $("#mcol-path-summary").on("keyup",function(evt){
            if (evt.keyCode === 13){
                $("#mcol-path-summary").trigger("blur");
                finder_do_goto_or_search.apply(this, [evt]);
                $("#mcol-path-summary").trigger("focus");
                evt.preventDefault();
                evt.stopPropagation();
            }
            if (evt.keyCode === 27 || evt.keyCode === 9){
                $("#mcol-path-summary").val(getCurrentPath().join("/")).trigger("blur");
                $("#perc-finder-item-search").trigger("blur");
                evt.preventDefault();
                evt.stopPropagation();
            }
            // hide the message if it's visible
            showFinderErrorMessage(false);
        });
        $(document).on('mousedown',function(evt){
            if (evt.target.id !== "perc-finder-go-action" && evt.target.id !== "mcol-path-summary"){
                $("#mcol-path-summary").trigger("blur");
                if (evt.target.id !== "perc-finder-search-submit" &&     // Need this condition to clean path when performing search
                    evt.target.id !== "perc-finder-listing-Search" &&    // else any click in the screen will override the path in other view
                    $(evt.target).parents(".perc-datatable-row").length > 0)
                {
                    $("#mcol-path-summary").val(getCurrentPath().join("/"));
                }

            }
            if (evt.target.id !== "perc-finder-item-search"){
                $("#perc-finder-item-search").trigger("blur");
            }
            // hide the message if it's visible
            showFinderErrorMessage(false);
        });
        $("#perc-finder-go-action").on("click",function(){
            var viaGoButton = true;
            goToNewPath(viaGoButton);
        });

        // dim the ui when the user is not in the finder
        $('.perc-finder-outer').on("mouseenter",function highligh_actions () {
            $(this).removeClass('ui-disabled');
        })
            .on("mouseleave",function dim_actions () {
                $(this).addClass('ui-disabled');
            });

        function absPath(strPath){
            var path = strPath.split("/");
            for(let i=0; i < path.length; i++){
                if (path[i] === ".."){
                    if (i-1 >= 0){
                        path.splice(i-1, 2);
                        i = i - 2;
                    }
                    else{
                        path.splice(i, 1);
                        i--;
                    }
                }
                if(path[1] === "."){
                    path.splice(i, 1);
                    i--;
                }
            }
            return path.join("/");
        }

        function goToNewPath(viaGoButton){
            $("#perc-finder-choose-listview").removeClass('ui-state-disabled');
            $("#perc-finder-choose-columnview").removeClass('ui-state-disabled');
            $("#perc-finder-item-name").removeClass('mcol-opened');
            var newPath = $("#mcol-path-summary").val().trim();
            var currPath = getCurrentPath().join("/");
            //eliminate duplicate "/"
            newPath = newPath.replace( /\/(\/)+/g, '/');
            //Convert a relative path to an absolute and correct path
            newPath = absPath(newPath);
            newPath = (newPath.charAt(0) !== "/") ? "/" + newPath : newPath;
            newPath = (newPath === "/" || newPath === "")? "/" + getCurrentPath()[1] : newPath;
            if (newPath === currPath && viaGoButton === true &&
                ($.Percussion.getCurrentFinderView() !== $.Percussion.PERC_FINDER_SEARCH_RESULTS  && // This condition avoids the check when
                    $.Percussion.getCurrentFinderView() !== $.Percussion.PERC_FINDER_RESULT)){    // in search view to force the path change
                return;
            }else {

                $.PercPathService.validatePath(newPath, function(status, result){
                    if (status === $.PercServiceUtils.STATUS_SUCCESS){
                        //validatePath return the exact caseSensitive path.
                        $("#mcol-path-summary").val(result);
                        if ($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR) {
                            var viewWrapper = $.PercComponentWrapper("perc-action-finder-go-clicked", ["perc-ui-component-finder"]);
                            var isWrapperSet = $.PercViewReadyManager.setWrapper(viewWrapper);
                            if (!isWrapperSet) {
                                if (!isWrapperSet) {
                                    $.PercViewReadyManager.showRenderingProgressWarning();
                                    return;
                                }
                            }
                        }
                        open(result.split("/"), function(){});
                    }
                    else {
                        showFinderErrorMessage(true,result);
                    }
                });
            }
        }

        function validatePath(evt, newPath, callback){
            // encode the newPath if it is under design
            var encodedPath = newPath;
            if($.perc_utils.isPathUnderDesign(newPath))
            {
                encodedPath = $.perc_utils.encodePathArray(newPath);
            }

            $.PercPathService.validatePath(encodedPath.join("/"), function(status, result){
                if (status === $.PercServiceUtils.STATUS_SUCCESS){
                    callback(evt);
                }
                else {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.finder@Path not found") + newPath.join("/")});
                    refresh();
                }
            });
        }

        function showFinderErrorMessage(show, message){
            var $message = $('.perc-finder-message');
            if (show) {
                $message.empty().append('<i class="icon-bell fas fa-bell" aria-hidden="true"></i>');
                $('<label class="perc-finder-error"></label>').text(message).appendTo($message);
                $message.fadeIn();
            } else {
                $message.fadeOut(function () {$(this).empty();});
            }
            fixIframeHeight();
            fixHeight();
        }

        // SCOPE: finder state in cookie

        /** determine if finder should be collapsed or not
         Note: cookie contains 'expanded|collapsed:###' where ### is height of finder */
        function get_finder_is_collapsed_from_cookie () {
            return /collapsed/.test($.cookie(finderExpandStateCookie));
        }

        /** get the saved height, returns ### or '' */
        function get_finder_height_from_cookie () {
            return ('' + $.cookie(finderExpandStateCookie)).replace(/[^0-9.]/g, '');
        }

        /** set the finder height in the cookie */
        function set_finder_height_in_cookie (height) {
            if (!isLibMode && get_finder_height_from_cookie() !== height) {
                var state = get_finder_is_collapsed_from_cookie() ? 'collapsed' : 'expanded';

                var options = {"sameSite": "Strict"};
                if (window.isSecureContext) {
                    options.secure = true;
                }
                $.cookie(finderExpandStateCookie, state + height, options);
            }
        }

        /** set the finder expanded state in the cookie */
        function set_finder_expand_state_in_cookie (isExpanded) {
            if (!isLibMode) {
                var height, state;
                height = get_finder_height_from_cookie();
                state = isExpanded ? 'expanded' : 'collapsed';
                var options = {"sameSite": "Strict"};
                if (window.isSecureContext) {
                    options.secure = true;
                }
                $.cookie(finderExpandStateCookie, state + height, options);
            }
        }

        function notify_resize () {
            fixBottomHeight();
            fixIframeHeight();
            fixHeight();
            // refresh Architecture view
            if( $("#perc_site_map").length > 0 )   {
                try {
                     $("#perc_site_map").perc_site_map('layoutAll');
                }catch(error){
                    //Gettign Initialization error in case site not selected... needs to be ignored
                }
            }
        }

        /**
         * Helper function to expand or collapse the finder.
         * @param expand {boolean} flag indicating an expand action request.
         */
        function expandCollapseFinder (expand) {
            var $button, $header, $finder = $(".perc-finder-body");
            if($finder.is(":visible") === expand) {
                return; // Nothing to do
            }
            $button = $('#perc-finder-expander');
            $header = $button.parents('.perc-finder-outer');
            set_finder_expand_state_in_cookie(expand);
            if (expand) {
                $header.removeAttr('collapsed');
                $finder.slideDown(notify_resize);
                $button.removeClass('icon-plus-sign')
                    .removeClass(' fas fa-plus').
                addClass('icon-minus-sign').
                addClass('fas fa-minus');
            } else {
                $header.attr('collapsed', true);
                $finder.slideUp(notify_resize);
                $button.removeClass('icon-minus-sign').
                removeClass('fas fa-minus').
                addClass('icon-plus-sign').
                addClass('fas fa-plus');
            }
            var frame  = $('#frame');
            var header = $('.perc-main');
            var bottom = $('#bottom');
        }

        /**
         * Toggle the finders expanded/collapsed state
         */
        function percFinderMaximizer (evt) {
            expandCollapseFinder(!$(".perc-finder-body").is(":visible"));
        }

        $("#perc-finder-expander").on("click",
            function(evt){
                percFinderMaximizer(evt);
            });

        /** if not easy to coerce val into a number return def */
        function integer (val, def) {
            val =  parseInt(val, 10);
            return isNaN(val) ? def : val;
        }

        var update_finder_height = (function () {

            // SCOPE: process resizing the finder

            /** current list of ui-resizable elements */
            var $finder_columns, toolbar, height;

            /** collect the current columns to update */
            function on_start_resize () {
                $('.perc-finder').addClass('ui-resizable-resizing');
                $finder_columns = $('.perc-finder .ui-resizable');
                toolbar = integer($('.perc-pagingbar-finder').outerHeight(), 0);
            }

            /** update the finder column heights */
            function on_resize () {
                set_finder_column_heights();
            }

            /** clean up jquery event setting the width, save state */
            function on_stop_resize (event) {
                notify_resize();
                $(this).css('width', 'auto');
                if(event)
                    set_finder_height_in_cookie(height);
                $('.perc-finder').removeClass('ui-resizable-resizing');
            }

            /** return scrollbar width if scrollbar is showing */
            function get_scrollbar_width (n) {
                return n && n.scrollWidth > n.clientWidth ? SCROLLBAR_WIDTH : 0 ;
            }

            /** set the height of the columns adjusting for scrollbar */
            function set_finder_column_heights () {
                var n = $('.perc-finder')[0];
                height = $('.perc-finder-body').height();
                $finder_columns.css('height', height - get_scrollbar_width(n) - toolbar);
            }

            /** set the finder height and update the column heights */
            function set_finder_body_height (new_height) {
                // WARN: this method sets height programmatically, don't call from resize event
                //If it is in library mode always keep the finder to maximum height
                if(isLibMode)
                    new_height = defaults.get_max_height()-100;

                new_height = integer(new_height, $('.perc-finder-body').height());
                if (new_height !== height) {
                    height = Math.max(new_height, defaults.minHeight);
                    height = Math.min(new_height, defaults.get_max_height());
                    $('.perc-finder-body').css('height', height);
                    set_finder_height_in_cookie(height);
                    // notify listeners that we resized the finder
                    $('.perc-finder').trigger('resize');
                }
                // new columns may have been added, so make sure their heights are set
                on_start_resize();
                set_finder_column_heights();
                on_stop_resize();
            }

            $(".perc-finder-body").resizable({
                handles: 's',
                minHeight: defaults.minHeight,
                maxHeight: defaults.get_max_height(),
                start: on_start_resize,
                resize: on_resize,
                stop: on_stop_resize
            });

            $(window).on("resize",function on_window_resize () {
                $('.perc-finder-body').resizable('option', 'maxHeight', defaults.get_max_height());
            });

            // initialize height from the cookie or use default
            set_finder_body_height(integer(get_finder_height_from_cookie(), defaults.height));

            // expose method to update the finder height
            return set_finder_body_height;

        });

        // WARN: don't initialize the finder expand until its height is initialized
        // initialize the finder in the ui to the correct expand state
        var expandFinder = $('[view=PERC_SITE]').length || !get_finder_is_collapsed_from_cookie();
        if(isLibMode)
            expandFinder = true;
        expandCollapseFinder(expandFinder);

        return {

            /** event pub/sub handler for finder resize events */
            on: function () {
                $('.perc-finder-body').on.apply($('.perc-finder-body'), arguments);
            },

            // Action constants
            ACTIONS: actions,
            //The top-level element (add it to a page to use the Finder).
            elem: top,

            //open a given path in the finder.
            open: open,

            //display a list of search results in the finder.
            search: search,

            addPathChangedListener : addPathChangedListener,
            executePathChangedListeners : executePathChangedListeners,
            finderOpenInProgress: finderOpenInProgress,

            /* notify finder to update finder columns and/or set a new height */
            update_finder_height: update_finder_height,

            goToNewPath: goToNewPath,

            refresh: refresh,

            idFromItem: idFromItem,

            addActionListener: addActionListener,

            removeActionListener: removeActionListener,

            fireActionEvent: fireActionEvent, // Only exposed so the button classes can fire.

            addOpenListener: addOpenListener,

            removeOpenListener: removeOpenListener,

            getCurrentPath: getCurrentPath,

            getPathItemByPath: getPathItemByPath,

            getPathItemById:getPathItemById,

            getParentPathItemByPath: getParentPathItemByPath,

            launchPagePreview: launchPagePreview,

            launchPagePreviewByPath: launchPagePreviewByPath,

            launchAssetPreview: launchAssetPreview,

            insertAfter: insert_after,

            maxResults: MAX_RESULTS,

            flagChangeView: flagChangeView,

            onDragStart: onDragStart,

            onDragStop: onDragStop,

            scrollIntoView: scroll_into_view,

            setStateButtonsDesignNode : setStateButtonsDesignNode,

            getCurrentItem : getCurrentItem,

            setCurrentItem : setCurrentItem

        };

        function getCurrentItem(){
            return currentItem;
        }

        function setCurrentItem(item){
            currentItem = item;
            return currentItem;
        }

        function _addToPathIdArray(path, id)
        {
            _finderPathIdArray[path] = id;
        }

        /**
         * Helper function to create an id for a listing.
         * @param item {object} item summary object, cannot be <code>null</code>.
         * @return the id string
         * @type string
         */
        function idFromItem(item) {
            var postfix = typeof(item.id) === 'undefined' ?
                item.path.split("/")[1] :
                item.id;
            return FINDER_LISTING_ID_PREFIX + postfix;
        }

        function refresh(k){
            $('.mcol-opened').each(function(){
                $(this).removeClass('mcol-opened');
            });
            var fwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-finder');
            if($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR && (fwrapper == null || fwrapper.wrapperName !== "perc-action-finder-refresh")){
                var compArray = [];
                compArray.push("perc-ui-component-finder");

                var viewWrapper = $.PercComponentWrapper("perc-action-finder-refresh",compArray);
                var isWrapperSet = $.PercViewReadyManager.setWrapper(viewWrapper);
                if(!isWrapperSet){
                    $.PercViewReadyManager.showRenderingProgressWarning();
                    return;
                }
            }
            open( current_path,k);
        }

        /**
         * Return a copy of the finder's current path array object
         * @return the path array.
         * @type array
         */
        function getCurrentPath(){
            return current_path.slice(0);
        }

        function search( item_list ) {
            //Close whatever is currently open.
            close_after( first_dir );
            dir_children( first_dir ).filter( '.mcol-opened' ).removeClass( '.mcol-opened' );

            //Add a new directory to hold the search results.
            var dir = insert_after( first_dir );

            //Add the search results to the new directory.
            $.each( item_list, function() {
                add_item( dir, make_item( this, open_from_dir( dir ) ) );
            });

        }

        function open_from_dir( dir ) {
            //Open a given element in a given directory - used by the
            //click callback on each element.
            return function( next, path ) {
                var path_sans_name = ut.acop( path );
                path_sans_name.pop();
                open_next( next, dir, [ next.data( 'name' ) ], path_sans_name,
                    function(){} );
            };
        }

        function err( str ) {

            finderOpenInProgress = false;
            fireActionEvent(actions.FINDER_OPEN_END, null);

            current_path =  ["","Sites"];
            refresh(function(){});

            /*
                    window.parent.jQuery.perc_utils.alert_dialog({
                        title: 'Finder Error' ,
                        content: str,
                        okCallBack: function (){
                            return true ;
                        }
                    });
            */
        }

        function open( path, k ) {
            if(finderOpenInProgress)
                return; // Only one open action can be in progress at any one time
            finderOpenInProgress = true;
            fireActionEvent(actions.FINDER_OPEN_START, null);
            //We load the first directory using the root [""] path.
            var initial_loader = load_folder_path( [""] );
            //Enter the recursive _open function
            _open( first_dir, initial_loader, path.slice(1), [""], k );
        }


        function _open( dir, loader, path, new_path, k ) {

            // If the last element in the path array is empty, remove it.
            // If it's not removed, then an open operation will be always in progress
            // (take a look at the finderOpenInProgress variable).
            if ( path[ path.length - 1 ] === '' )
                path.pop();

            //store the next child item to find the correct page that contains the item
            if (path.length > 0)
                dir.data("child", path[0]);

            if (new_path.length > 1 && new_path[1] === $.perc_paths.DESIGN_ROOT_NO_SLASH)
            {
                setStateButtonsDesignNode(true);
            }
            else
            {
                setStateButtonsDesignNode(false);
            }

            //Load the directory contents from the server
            $('.perc-finder-panel-loading').remove();
            dir_container(dir).append('<div class="perc-finder-panel-loading"><span class="icon-spinner icon-spin icon-2x"></span>&nbsp;Loading...</div>');
            loader( onLoad, dir );
            function onLoad( children, content) {
                if( content ) {
                    //If the contents are given directly, add them.
                    dir.find('.mcol-direc-wrapper').empty().addClass('mcol-direc-wrapper-last').append(content);
                } else {
                    //If we have a list of children, update the directory
                    //to reflect the current set of children.
                    update_dir( dir, children);
                }
                if( path.length === 0 ) {
                    //We have finished opening to our destination.

                    //Set the path summary to the correct path.
                    $("#mcol-path-summary").val( new_path.join('/') );
                    // here we are injecting the siteimprove plugin
                    var searchPath = $("#mcol-path-summary").val();
                    if((searchPath.indexOf('/Sites') >= 0) && searchPath !== '/Sites') {
                        var siteName = getSiteNameByPath(searchPath);
                        injectSiteImprove(siteName, getCurrentPath());
                    }

                    finderOpenInProgress = false;
                    fireActionEvent(actions.FINDER_OPEN_END, null);

                    path_changed( new_path);

                    //Close anything after this.
                    close_after( dir );
                    dir_children( dir ).filter( '.mcol-opened' ).removeClass( 'mcol-opened' );

                    var fwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-finder');
                    if(fwrapper != null)
                        fwrapper.handleComponentProgress('perc-ui-component-finder', "complete");
                    //Call the continuation.
                    if( k ){ k(); }
                } else {
                    //Find the element corresponding to the next path element.
                    var next = dir_children( dir ).filter( function() {
                        if (typeof($(this).data('name')) != "undefined" && typeof(path[0]) != "undefined"){
                            return $(this).data('name') === path[0];
                        }
                        return false;
                    });

                    $.PercQueuePostAJAX(function(){
                        setTimeout(function(){
                            open_next( next, dir, path, new_path, k );
                        }, 150);
                    });
                }
                update_finder_height();
                var fwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-finder');
                if(fwrapper != null)
                    fwrapper.handleComponentProgress('perc-ui-component-finder', "processing");

            }
        }

        function pagePrevious(evt){
            var dir = $(this).parents("td");
            var newStartIndex = dir.data('startIndex') - MAX_RESULTS;
            dir.data('startIndex', newStartIndex);
            load_folder(dir, $(this).closest('.perc-paging-finder').is('.perc-paging-finder-bottom'));
        }

        function pageNext(evt){
            var dir = $(this).parents("td");
            var newStartIndex = dir.data('startIndex') + MAX_RESULTS;
            dir.data('startIndex', newStartIndex);
            load_folder(dir, $(this).closest('.perc-paging-finder').is('.perc-paging-finder-bottom'));
        }

        //Retrieve another page of paging result
        function load_folder(dir, scrollBottom) {
            //Generate the url with startIndex and maxResult.
            var path = dir.data('path');
            var startIndex = dir.data('startIndex');
            var str_path = $.perc_utils.encodeURL(path.join("/")) + "/?startIndex=" + startIndex + "&maxResults=" + MAX_RESULTS;

            $.perc_pathmanager.open_path( str_path, true, getChildren, err, true );
            function getChildren( folder_spec ) {
                var children = {};
                $.each( $.perc_utils.convertCXFArray(folder_spec.PagedItemList.childrenInPage), function() {
                    //Use the postfix "_item" to avoid reserved name collision (e.g. toString, watch, toSource, etc).
                    children[ ut.extract_path_end( this.path ) + "_item"] = this;
                });
                dir.data('totalResult', folder_spec.PagedItemList.childrenCount);
                dir.data('startIndex', folder_spec.PagedItemList.startIndex);
                update_dir(dir, children);
                if (scrollBottom)
                    dir.find('.mcol-direc-wrapper').attr({ scrollTop: dir.find('.mcol-direc-wrapper').attr("scrollHeight") });
            }
        }

        //positionClass is to know is the header if top or bottom position.
        function pagingHeader(dir, position){
            var startIndex = dir.data('startIndex');
            var totalResult = dir.data('totalResult');

            //Calculate the header Text
            var headerText = "";
            if (MAX_RESULTS >= totalResult){
                var nItems = totalResult - (startIndex-1);
                headerText = nItems + " item" +(nItems!==1? "s": "");
            }else{
                var endIndex = ((startIndex-1 + MAX_RESULTS > totalResult)? totalResult : (startIndex-1 + MAX_RESULTS));
                headerText = startIndex + " - " + endIndex + " of " + totalResult;
            }

            //Generate the HTML header
            var header = $('<div class="perc-paging-finder"/>')
                .data('name', position)
                .append($('<span class="perc-paging-text" />').text(headerText))
                .append(
                    $('<div class="perc-paging-finder-navigator" />')
                        .append($('<a class="perc-paging-finder-previous" />').text('<<').on("click",
                            function(evt){
                                pagePrevious(evt);
                            }))
                        .append($('<a class="perc-paging-finder-next"/>').text('>>').on("click",
                            function(evt){
                                pageNext(evt);
                            }))
                ).addClass(position);
            header = $("<div/>").append(header).append("<div style='clear:both'/>");
            //Enable/disable navigation buttons
            //Check if have next items
            if ((totalResult - (startIndex-1 + MAX_RESULTS)) > 0)
                header.find('.perc-paging-finder-next').removeClass('perc-hide-navigator');
            else
                header.find('.perc-paging-finder-next').addClass('perc-hide-navigator');

            //Check if have previous items
            if (startIndex > MAX_RESULTS)
                header.find('.perc-paging-finder-previous').removeClass('perc-hide-navigator');
            else
                header.find('.perc-paging-finder-previous').addClass('perc-hide-navigator');

            return header;
        }

        function pagingHeaderCountOnly(dir, position){
            var totalResult = dir.data('totalResult');
            var nItems = totalResult;
            var headerText = nItems + " item" +(nItems!==1? "s": "");
            var header = $('<div class="perc-paging-finder perc-paging-finder-top "/>')
                .data('name', position)
                .append($('<span class="perc-paging-text" />').text(headerText))
                .append('');
            return header;
        }

        function update_dir( dir, children) {
            //Given a current list of children, we go through the
            //directory's elements to determine whether any have
            //been added or removed.

            // since we are reusing the DOM elements clean up the class name
            dir.find('.mcol-direc-wrapper').removeClass('mcol-direc-wrapper-last');

            //Get the current elements, indexed by name.
            var curr_children = {};
            $.each( dir_children( dir ), function(){
                //Use the postfix "_item" to avoid reserved name collision (e.g. toString, watch, toSource, etc).
                curr_children[ $(this).data('name') + "_item" ] = $(this);
            });
            var dChildren = ut.odiff( children, curr_children );

            //odiff gets the set difference of two objects.
            $.each( ut.odiff( curr_children, children ), function() {
                //Listings which have been deleted - close
                //them if open, then remove them.
                if( this.is( '.mcol-opened' ) )
                    close_after( dir );

                //Make sure the resizable handlers are not removed on any refresh event
                if(!dragging && !this.hasClass('ui-resizable-e') && !this.parent().hasClass('perc-view-column-fixed') )
                    this.remove();
            });

            //Add top paging Header
            //Don't add headers in the first column.
            if(dir.data('path').join('/') !== "" && dir.find(".perc-paging-finder-top").length === 0) {
                if(assetPagination && ( (dir.data('path').indexOf("Assets") >-1) || (dir.data('path').indexOf("Sites") >-1)) ) {
                    dir_container(dir).prepend(pagingHeaderCountOnly(dir, "perc-paging-finder-top"));
                }else{
                    dir_container(dir).prepend(pagingHeader(dir, "perc-paging-finder-top"));
                }
            }
            $.each( dChildren, function() {
                if (!isLibMode || this.path !== "/Search/") {
                    add_item(dir, make_item(this, open_from_dir(dir)));
                }
            });
        }

        function open_next( next, dir, path, new_path, k ) {
            if( next.length === 0 ) {
                path_changed( new_path );
                finderOpenInProgress = false;
                //Set the path summary to the correct path.
                $("#mcol-path-summary").val( new_path.join('/') );
                //err( "Child \"" + path[0] + "\" does not exist" );
                var fwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-finder');
                if(fwrapper != null)
                    fwrapper.handleComponentProgress('perc-ui-component-finder', "complete");
            } else if ( next.is( '.mcol-opened' ) ) {
                //Element has already been opened - just continue
                //our open operation at the next directory.
                new_path.push( path.shift() );
                scroll_into_view(next);

                //Expose the data item for multiple purposes.
                if (typeof(next.data('spec')) != "undefined"){
                    currentItem = next.data('spec');
                    $('.perc_last_selected').removeClass("perc_last_selected");
                    next.addClass("perc_last_selected");
                }

                _open( dir.next(), next.data( 'loader' ), path, new_path, k);
            } else {
                //Element is not currently opened - close what is
                //opened, then create a new directory and load it
                //using the current element's loader.
                close_after( dir );
                dir_children( dir ).filter('.mcol-opened').removeClass('mcol-opened');
                next.addClass('mcol-opened');

                scroll_into_view( next );
                //Expose the data item for multiple purposes.
                if (typeof(next.data('spec')) != "undefined"){
                    currentItem = next.data('spec');
                    $('.perc_last_selected').removeClass("perc_last_selected");
                    next.addClass("perc_last_selected");
                }

                var next_dir = insert_after( dir );
                new_path.push( path.shift() );
                _open( next_dir, next.data('loader'), path, new_path, k );
            }
            update_finder_height();
        }

        //Make the element which represents a single listing
        //in a directory.
        function make_item( spec, open_rel ) {

            var tabindex = 10;
            var pref = (spec.type === 'Folder') ? 'a' : 'z';
            var item_path = ut.extract_path( spec.path );
            var isSystemCategory = false;
            var icon;
            if(spec && spec.category && spec.category ==='SYSTEM' && spec.type && spec.type ==='FSFile' &&
                spec.name && spec.name.indexOf('.') !==-1){
                // customizing for case of category:system && it is a file type or image type.
                var ImageFileTypes = ['tif','jpg','jpeg','gif','png','tiff','jfif','jpe','bmp','dib'];
                var myFileType = spec.name.substr(spec.name.indexOf(".") + 1);
                if(ImageFileTypes.indexOf(myFileType) > -1){
                    icon = ut.choose_icon( 'FSIMAGEFile', spec.icon, item_path );
                }else{
                    icon = ut.choose_icon( 'FSFile', spec.icon, item_path );
                }
            }else{ //default workflow
                icon = ut.choose_icon( spec.type, spec.icon, item_path );
            }
            var listing = $("<a />").addClass('mcol-listing')
                .attr("alt",  spec.name )
                .attr('id', idFromItem(spec))
                //.attr('tabindex', tabindex)
                .append($("<img src='"+ icon.src +"' style='float:left' alt='"+ icon.alt + "' title='" + icon.title + "' aria-hidden='" + icon.decorative + "' />" ))
                .append($("<div class='perc-finder-item-name' style='cursor: default; text-overflow : ellipsis;overflow : hidden'>" + spec.name + "</div>" )).attr('title', spec.name)
                .data( 'tag', pref + (spec.name + "").toLowerCase() )
                .data( 'name', item_path[ item_path.length - 1 ] )
                .data( 'spec', spec );

            _addToPathIdArray(spec.path, spec.id);
            if(spec.type)
                listing.addClass("perc-listing-type-" + spec.type);
            if(spec.category)
                listing.addClass("perc-listing-category-" + spec.category);

            if( spec.leaf ) {
                listing.data( 'loader',
                    function(onLoad){
                        make_leaf_summary(spec, function(itemPropsContent){
                            onLoad( null, itemPropsContent );
                        });
                    });
            } else {
                listing.data( 'loader',
                    load_folder_path( ut.acop( item_path ) ) );
            }

            listing.on("click", function(evt){onClick(evt);} );
            listing.on("dblclick", function(evt){
                validatePath(evt, item_path, function(){
                    if(spec.type==="Folder" || item_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH)
                    {
                        onClick(evt);
                    }
                    else
                    {
                        fireOpenEvent(spec);
                    }
                });
            });

            if(isDraggableItem(spec))
            {
                listing.draggable( {
                    helper: function() {
                        return $(this).clone()
                            .css(('overflow', 'visible'),('width', this.offsetWidth)[0]);

                    },
                    appendTo: 'body',
                    refreshPositions: true,
                    zIndex: 9990,
                    revert: true,
                    revertDuration: 0,
                    start: onDragStart,
                    stop: onDragStop,
                    scope: 'perc_iframe_scope',
                    delay: dragDelay
                });

            }

            if(isDroppableItem(spec))
            {
                listing.droppable( {
                    tolerance: 'pointer',
                    accept: dragAcceptor,
                    over: hoverStart,
                    out: hoverCancel,
                    scope: 'perc_iframe_scope',
                    drop: onDrop } );
            }

            var hoverCount = 1, hover_time = 500;

            function hoverStart(event, ui){
                var startCount = hoverCount;
                var itemPath = ui.draggable.data('spec').path;
                var targetPath = spec.path;
                var targetType = spec.type;
                if(!canDrop(itemPath, targetPath, ui.draggable.data('spec').type, targetType))
                    return;
                if(spec.accessLevel !== $.PercFolderHelper().PERMISSION_READ &&
                    ui.draggable.data('spec').accessLevel !== $.PercFolderHelper().PERMISSION_READ)
                    $(this).addClass("perc-finder-item-over");
                if($(this).hasClass("perc-listing-type-site"))
                    return; // do not expand a site node

                setTimeout( function(event){
                        if( hoverCount === startCount ) {
                            onClick(event);
                        }
                    },
                    hover_time );
            }

            function hoverCancel(event, ui){
                $(this).removeClass("perc-finder-item-over");
                hoverCount++;
            }

            /**
             * onDrop action called when dropping an item on a eligable drop zone.
             * @param event {object} the event object passed by the fired event handler.
             * @param ui {object} the special ui object passed by the jQuery drop event.
             */
            function onDrop(event, ui){
                var itemPath = ui.draggable.data('spec').path;
                var itemType = ui.draggable.data('spec').type;
                var targetPath = spec.path;
                var targetType = spec.type;
                $(this).removeClass("perc-finder-item-over");
                hoverCancel();
                if(spec.accessLevel === $.PercFolderHelper().PERMISSION_READ ||
                    ui.draggable.data('spec').accessLevel === $.PercFolderHelper().PERMISSION_READ)
                    return false;
                if(!canDrop(itemPath, targetPath, itemType, targetType))
                    return false;
                $.PercPathService.moveItem(
                    itemPath,
                    targetPath,
                    function(status, data){
                        if(status === $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            var type = null;
                            if (itemType === "percPage")
                                type = "page";
                            else if (itemType === "Folder" && targetPath.indexOf("/Sites")===0)
                                type = "folder";
                            if(type){
                                $.PercRedirectHandler.createRedirect(itemPath, targetPath + ui.draggable.data('spec').name, type)
                                    .fail(function(errMsg){
                                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.contributor.ui.adaptor@Redirect creation error"), content: errMsg, okCallBack: function(){
                                                refresh();
                                            }});
                                    })
                                    .done(function(){
                                        refresh();
                                    });
                            }
                            else{
                                refresh();
                            }
                        }
                        else
                        {
                            var content = data;
                            if (data.indexOf("item with the same name already exists in the folder") !== -1)
                            {
                                var itemLabel = "asset";
                                if (itemType === "percPage")
                                {
                                    itemLabel = "page";
                                }
                                else if (itemType === "Folder")
                                {
                                    itemLabel = "folder";
                                }
                                content = I18N.message( "perc.ui.finder.move.error@Duplicate", [itemLabel, ui.draggable.data('spec').name, targetPath] );
                            }
                            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: content});
                        }
                    }
                );


            }

            /**
             * Helper function to determine if the drop is allowed.
             * This is similar to drag acceptor stuff, but these checks
             * should pass drag acceptor so expansion still occurs.
             * @param itemPath {string} the source item path.
             * @param targetPath {string} the target path.
             * @param itemtype {string} the source item type.
             * @param targetType {string} the target type.
             */
            function canDrop(itemPath, targetPath, itemtype, targetType)
            {
                if(targetType && (targetType !== 'Folder' && targetType !== 'site'))
                {
                    return false;
                }
                var tPath = (targetPath.match("\/$") === "/")
                    ? targetPath.substr(0, targetPath.length - 1)
                    : targetPath;
                if(tPath === (itemPath.substring(0, itemPath.lastIndexOf("/"))))
                    return false;
                if(itemtype === 'Folder')
                {
                    var sPath = itemPath.substr(1).split("/");
                    if(sPath[sPath.length - 1] === "")
                        sPath.pop();
                    var tPath = targetPath.substr(1).split("/");
                    if(tPath[tPath.length - 1] === "")
                        tPath.pop();
                    var isSame = true;
                    for(var i = 0; i < sPath.length; i++)
                    {
                        if(sPath[i] !== tPath[i])
                        {
                            isSame = false;
                            break;
                        }
                    }
                    if(isSame)
                        return false;
                }
                return true;
            }

            /**
             * Determine if the passed in item should be made draggable.
             */
            function isDraggableItem(item){
                var type = item.type;
                var cat = item.category;

                if(!type)
                    return false;
                if(type === 'site')
                    return false;
                if(type === 'percPage' && cat === 'LANDING_PAGE')
                    return false;
                if(type === 'Folder' && cat ==='SECTION_FOLDER')
                    return false;
                if(type === 'Folder' && item.accessLevel !== $.PercFolderHelper().PERMISSION_ADMIN)
                    return false;
                if(type === 'FSFile')
                    return false;
                if(type === 'FSFolder')
                    return false;
                return true;

            }

            /**
             * Determine if the passed in item should be made droppable.
             */
            function isDroppableItem(item){
                var type = item.type;
                var cat = item.category;
                if(!type && item.path === $.perc_paths.ASSETS_ROOT + "/")
                    return true;
                if(!type)
                    return false;
                if(type === 'percPage')
                    return false;
                return true;
            }

            /**
             * Decides if a dragged item will be accepted by the target drop element.
             * @param item {object} the item being dragged.
             */
            function dragAcceptor(item) {
                var their_path = item && item.data('spec') && item.data('spec').path;
                if( !their_path )
                    return false;

                var our_path = spec.path;
                // Do not allow a site item to be moved into a different
                // site
                if(spec.type && spec.type == 'site')
                {
                    var site1 = their_path.substr(1).split("/")[1];
                    var site2 = our_path.substr(1).split("/")[1];
                    if(site1 !== site2)
                        return false;
                }
                // Do not allow dropping asset into a non folder
                if(item.data('spec').category && item.data('spec').category === 'ASSET')
                {
                    if(!spec.type || spec.type !== 'Folder')
                        return false;
                }

                if( our_path.length >= their_path.length &&
                    $.grep( their_path, function(c, ii) { return c === our_path[ii]; } ).length === 0 ) {
                    //their path is a subset of our path - don't allow
                    //item to be dragged into itself or its children
                    return false;
                }

                var their_base = ut.extract_path( their_path )[1];
                var our_base = ut.extract_path( our_path )[1];
                if( their_base !== our_base ) {
                    return false;
                }

                return true;
            }

            return listing;
            function onClick(evt){
                if(evt) {
                    if (evt.currentTarget && evt.currentTarget.id === "perc-finder-listing-Search") {
                        $.Percussion.setView("search");

                        //Set the Search icon for when highlighted
                        $("#perc-finder-listing-Search").find("img").attr("src", "/cm/images/images/searchIcon_on.png");

                        return;
                    }
                    // evt.stopPropagation();
                }

                //Set the Search icon for when highlighted
                $("#perc-finder-listing-Search").find("img").attr("src", "/cm/images/images/searchIcon.png");

                $("#perc-finder-choose-listview").removeClass('ui-state-disabled');
                $("#perc-finder-choose-columnview").removeClass('ui-state-disabled');
                var $evtTarget = $(this);
                var $itemNameEl = $evtTarget.children(".perc-finder-item-name");
                var $inputField = $itemNameEl.find("#perc_finder_inline_field_edit"); //local to event target
                var len = $inputField.length;
                if(len === 0)
                {
                    var $editField = $("#perc_finder_inline_field_edit");
                    if($editField.length > 0)
                    {
                        lastClickPath = item_path;
                        $editField.trigger("blur");
                    }
                    $evtTarget.trigger("focus");

                    // Add a class to the last selected item and remove it from other items.
                    //$('.perc_last_selected').removeClass("perc_last_selected");
                    //$(this).addClass("perc_last_selected");
                    if ($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR) {
                        var viewWrapper = $.PercComponentWrapper("perc-action-finder-item-clicked", ["perc-ui-component-finder"]);
                        var isWrapperSet = $.PercViewReadyManager.setWrapper(viewWrapper);
                        if (!isWrapperSet) {
                            if (!isWrapperSet) {
                                $.PercViewReadyManager.showRenderingProgressWarning();
                                return;
                            }
                        }
                    }
                    open_rel( listing, ut.acop( item_path ));
                }
            }
        }

        /**
         * Executed when dragging starts for one or more finder items.
         */
        function onDragStart()
        {
            var $finderTable = $(".perc-finder-table");
            var props = {};
            props.left= $finderTable.offset().left;
            props.top = $finderTable.offset().top;
            props.width = $finderTable.width();
            props.height = $finderTable.height();
            dragging = true;
            // Add a mouse move listener to body only when dragging a finder item.
            $("body")
                .css("overflow", "visible")
                .on("mousemove.finderDrag", props, function(evt){
                    /* Determine if we are dragging within the finder table.
                       If we are not then disable all droppables within the finder else
                       enable them.
                    */
                    var right = evt.data.left + evt.data.width;
                    var bottom = evt.data.top + evt.data.height;
                    var inX = (evt.pageX >= evt.data.left) && (evt.pageX <= right);
                    var inY  = (evt.pageY >= evt.data.top) && (evt.pageY <= bottom);
                    enableDisableFinderDroppables(inY && inX);

                });


        }

        /**
         * Executed when dragging stops on the currently dragged finder items.
         */
        function onDragStop()
        {
            // Remove the mouse move listener and re-enable finder droppables
            dragging = false;
            $("body")
                .css("overflow", "hidden")
                .off("mousemove.finderDrag");
            enableDisableFinderDroppables(true);
        }
        /**
         * Creates the summary of the item properties and calls the call the callback function with the html content.
         * @param path, item path assumed not null or empty.
         * @param callback, the callback function assumed not null.
         */
        function make_leaf_summary( spec, callback ) {
            var summary = " ";
            $.perc_pathmanager.getItemProperties(spec.path, function(status, itemProps){
                if(status)
                {
                    var nameType = null;
                    var type = null;
                    var linkTag = null;
                    var isAssetResource = spec.category==="ASSET" || spec.category === "RESOURCE";
                    if(isAssetResource)
                    {
                        //Asset/Type
                        nameType = "Asset";
                        type = "Type";
                        if(!spec.path.includes("Recycling")){
                            linkTag = "<a href='#' class='perc-finder-preview-link' id='perc-asset-preview-link' title='Click for preview'>";
                        }else{
                            linkTag = "";
                        }

                    }
                    else
                    {
                        //Page/Template
                        nameType = "Page Link";
                        type = "Template";
                        if(!spec.path.includes("Recycling")){
                            linkTag = "<a href='#' class='perc-finder-preview-link' id='perc-page-preview-link' title='Click for preview'>";
                        }else{
                            linkTag = "";
                        }

                    }
                    var lpdate = itemProps.lastPublishedDate;
                    if (typeof lpdate === "undefined" || lpdate === null || lpdate.trim() === '')
                    {
                        lpdate = '';
                    }
                    else
                    {
                        var lastPublishedDateParts = $.perc_utils.splitDateTime(lpdate);
                        lpdate = '<div style="padding:9px 0 0 10px;">Last Published: <span></span></div>' +
                            '<div style="padding:3px 0 0 10px;"><span "perc_finder_details_lpdate">' + lastPublishedDateParts.date + " " + lastPublishedDateParts.time  + '</span></div>';
                    }

                    var lastModifiedDateParts = $.perc_utils.splitDateTime(itemProps.lastModifiedDate);

                    if (spec.path.split("/")[1] === $.perc_paths.DESIGN_ROOT_NO_SLASH)
                    {
                        var fileSize = $.perc_utils.formatFileSize(itemProps.size);
                        summary = '<div style="padding:10px 0 0 10px;">' + "Properties" + ': <span id="perc_finder_details_name">' + itemProps.name + '</span></div>' +
                            '<div style="padding:10px 0 0 10px;">' + "Size" + ': <span id="perc_finder_details_size">' + fileSize + '</span></div>' +
                            '<div style="padding:10px 0 0 10px;">Last Modified: <span></span></div>' +
                            '<div style="padding:3px 0 0 10px;"><span id="perc_finder_details_lmdate">' + lastModifiedDateParts.date + " " + lastModifiedDateParts.time  + '</span></div>';
                    }
                    else
                    {
                        summary = '<div style="padding:9px 0 0 10px;">' + nameType + ': <span id="perc_finder_details_name">' + linkTag + itemProps.name + '</a></span></div>' +
                            '<div style="padding:9px 0 0 10px;">' + type + ': <span id="perc_finder_details_type">' + itemProps.type + '</span></div>' +
                            '<div style="padding:9px 0 0 10px; width:170px;">Status: <div id="perc_finder_details_status" status="' + itemProps.status + '" workflow="' + itemProps.workflow + '" class="perc-ellipsis" title="' + itemProps.status + " (" + itemProps.workflow + ')">' + itemProps.status + " (" + itemProps.workflow + ")" + '</div></div>' +
                            '<div style="padding:9px 0 0 10px;">Last Modified: <span id="perc_finder_details_lmuser">' + itemProps.lastModifier + '</span></div>' +
                            '<div style="padding:3px 0 0 10px;"><span id="perc_finder_details_lmdate">' + lastModifiedDateParts.date + " " + lastModifiedDateParts.time  + '</span></div>' +
                            lpdate;
                    }
                    var $sum = $(summary);
                    if(isAssetResource)
                    {
                        $sum.find("#perc-asset-preview-link").each(function(){
                            $(this).off().on('click', function(){
                                launchAssetPreview(spec.id);
                            });
                        });
                    }
                    else
                    {
                        $sum.find("#perc-page-preview-link").each(function(){
                            $(this).off().on('click', function(){
                                launchPagePreview(spec.id);
                            });
                        });
                    }
                    callback($sum);
                }
                else
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: itemProps});
                    refresh();
                }
                var fwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-finder');
                if(fwrapper != null)
                    fwrapper.handleComponentProgress('perc-ui-component-finder', "complete");

            });
        }

        /**
         * Launch the asset preview for the specified asset.
         * @param id {string} the asset id, cannot be <code>null</code> or
         * empty.
         */
        function launchAssetPreview(id, revId){
            if(revId)
            {
                var ida = id.split("-");
                ida[0] = revId;
                id = ida.join("-");
            }
            $.PercAssetService.getAssetViewForAssetId(id, function(status, result){
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    var nRef = window.open(result, "percAssetPreviewWindow" + id.replace(/\-/g, ""));
                    $(nRef.document).ready(function(){
                        if(revId)
                        {
                            window.setTimeout(function(){
                                nRef.document.title = nRef.document.title + I18N.message("perc.ui.finder@Revision") + revId + ")";
                            }, 1000); // There needs to be a delay for title to be ready
                        }
                        nRef.focus();
                    });
                }
                else
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                }
            });
        }

        /**
         * Launch the page preview for the specified page.
         * @param id {string} the page id, cannot be <code>null</code> or
         * empty.
         */
        function launchPagePreview(id, revId){
            // Retrieve the path for the given page id to build the friendly URL and open hte preview
            $.PercPathService.getPathItemById(id, function(status, result, errorCode) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    var href = result.PathItem.folderPaths + "/" + result.PathItem.name;
                    var mobilePreview = result.PathItem.mobilePreviewEnabled;
                    if(typeof mobilePreview === "undefined" || mobilePreview === null){
                        mobilePreview = false;
                    }
                    href = href.substring(1);

                    if(revId)
                    {
                        href += "?sys_revision=" + revId + "&percmobilepreview="+mobilePreview;
                    }
                    else{
                        href += "?percmobilepreview="+mobilePreview;
                    }

                    // IE doesn't accept dashes '-' as part of the window name.
                    // The 2nd param needs to be "" and not null because IE will not show
                    // any bars when null. Both IE and FF show the same header in the new
                    // window as the original by passing "" and follow the user's preference
                    // as whether to open in a tab or window.
                    var nRef = window.open(href, "percPagePreviewWindow" + id.replace(/\-/g, ""));
                    $(nRef.document).ready(function() {
                        if(revId) {
                            window.setTimeout(function() {
                                nRef.document.title = nRef.document.title + I18N.message("perc.ui.finder@Revision") + revId + ")";
                            }, 1000); // There needs to be a delay for title to be ready
                        }
                        nRef.focus();
                    });
                }
                else {
                    // We failed retrieving the friendly URL. Show the error dialog
                    $.unblockUI();

                    var msg = "";
                    if (errorCode == "cannot.find.item")
                    {
                        msg = I18N.message( 'perc.ui.common.error@Preview Content Deleted' );
                    }
                    else
                    {
                        msg = result;
                    }

                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: msg});
                }
            });
        }

        /**
         * Launch the page preview for the specified page.
         * @param path{string} the page path, cannot be <code>null</code> or
         * empty.
         * @param id {string} the page id, cannot be <code>null</code> or
         * empty.
         */
        function launchPagePreviewByPath(path,id,revId){
            // Retrieve the path for the given page id to build the friendly URL and open hte preview
            $.PercPathService.getPathItemForPath(path, function(status, result) {
                if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                    //var href = result.PathItem.folderPaths.slice(1) + "/" + result.PathItem.name;

                    var href = result.PathItem.folderPaths + "/" + result.PathItem.name;
                    href = href.substring(1);
                    var mobilePreview = result.PathItem.mobilePreviewEnabled;
                    if(typeof mobilePreview === "undefined" || mobilePreview === null){
                        mobilePreview = false;
                    }

                    if(revId)
                    {
                        href += "?sys_revision=" + revId + "&percmobilepreview="+mobilePreview;
                    }
                    else{
                        href += "?percmobilepreview="+mobilePreview;
                    }

                    // IE doesn't accept dashes '-' as part of the window name.
                    // The 2nd param needs to be "" and not null because IE will not show
                    // any bars when null. Both IE and FF show the same header in the new
                    // window as the original by passing "" and follow the user's preference
                    // as whether to open in a tab or window.
                    var nRef = window.open(href, "percPagePreviewWindow" + id.replace(/\-/g, ""));
                    $(nRef.document).ready(function() {
                        if(revId) {
                            window.setTimeout(function() {
                                nRef.document.title = nRef.document.title + I18N.message("perc.ui.finder@Revision") + revId + ")";
                            }, 1000); // There needs to be a delay for title to be ready
                        }
                        nRef.focus();
                    });
                }
                else {
                    // We failed retrieving the friendly URL. Show the error dialog
                    $.unblockUI();
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                }
            });
        }

        //Load a list of children from a given folder path
        function load_folder_path( path ) {
            return function(k, dir){
                // startIndex always will be 1 because this functions is called when the user do a click on a folder
                //Generate the url with startIndex and maxResult.
                var str_path = $.perc_utils.encodeURL(path.join("/")) + "/?startIndex=1" + "&maxResults=" + MAX_RESULTS;

                if(assetPagination && ((path.indexOf("Assets") >-1 )|| (path.indexOf("Sites") >-1))){
                    str_path = $.perc_utils.encodeURL(path.join("/"));
                }
                //check if we need to find a specific page for the next item and add the child in the url
                //if the child element doesn't exist the server returned the first page of the current folder
                if (!assetPagination && typeof(dir.data('child')) !== "undefined" && dir.data('child') !== ""){

                    if(str_path.indexOf("?") >-1){
                        str_path += "&child=" + $.perc_utils.encodeURL(dir.data('child'));
                    }else{
                        str_path +="/?child=" + $.perc_utils.encodeURL(dir.data('child'));
                    }
                    dir.data('child', ""); //clean the next child element.
                }

                $.perc_pathmanager.open_path( str_path, true, getChildren, err, true );
                function getChildren( folder_spec ) {
                    var children = {};
                    $.each( $.perc_utils.convertCXFArray(folder_spec.PagedItemList.childrenInPage),
                        function() {
                            //Use the postfix "_item" to avoid reserved name collision (e.g. toString, watch, toSource, etc).
                            children[ ut.extract_path_end( this.path ) + "_item"] = this;
                        });
                    dir.data('path', path);
                    //set the startIndex of the child element, if was not provided the child param the service return the original startindex
                    dir.data('startIndex', folder_spec.PagedItemList.startIndex);
                    dir.data('totalResult', folder_spec.PagedItemList.childrenCount);
                    k( children);
                }
            };
        }

        function add_item( dir, item ) {
            //Insertion sort by tag - start with item at the end,
            //then insert it before the first element with a greater
            //tag (if this element has the max. tag, it will remain
            //at the end).
            dir_container( dir ).append( item );
            /*
            dir_children( dir ).each( function() {
                    if( $(this).data( 'tag' ) > item.data( 'tag' ) ) {
                        $(this).before( item );
                        return false;
                    }
                });*/
        }


        function scroll_into_view( listing ) {
            //Scroll the listing into view, if it is not already.
            var par = listing.closest( '.mcol-direc-wrapper' );
            var yoff = listing.position().top + listing.outerHeight();
            var height = par.closest('td').height();
            if( yoff < 0 || yoff > height ) {
                par.animate( { scrollTop : yoff - height}, 200 );
            }
        }

        /**
         * Helper function to enable or disable list view button and finder action buttons when navigating design node.
         * @param flag(boolean) if <code>true</code> the buttons are disabled, otherwise the buttons are enabled.
         */
        function setStateButtonsDesignNode(disable)
        {
            if (disable)
            {
                $.percFinderButtons().disableAllButtonsButSite();
            }
        }

        /**
         * trivial utility functions.
         */
        function insert_after( dir ) {
            var newdir = new_dir();

            $("#perc-finder-table-top").append(newdir);
            return newdir;
        }

        function close_after( dir ) {
            if( dragging ) {
                //If we are dragging an element, it needs to remain in the DOM.
                dir.nextAll().hide();
            } else {
                dir.nextAll().remove();
            }
        }

        function new_dir (resizable) {
            // default to true unless explicitly told not to resize
            resizable = resizable !== false;
            var td = $('<td tabindex="-1" class="mcol-direc" />');
            var resize = $('<div class="perc-resize ui-resizable" />');
            var content = $('<div class="perc-resize-width mcol-direc-wrapper" />');
            function onresize (event, ui) {
                content.css('width', resize.width() - content.siblings().outerWidth());
            }
            function onstart (event, ui) {
                $('.perc-finder').addClass('ui-resizable-resizing');
            }
            function onstop (event, ui) {
                $('.perc-finder').removeClass('ui-resizable-resizing');
            }
            if (resizable) {
                resize.resizable({handles: 'e', resize: onresize, start: onstart, stop: onstop});
            }
            return td.append(resize.append(content));
        }

        function fn_first_dir () {
            var resizable = false, fdir;
            fdir = new_dir(resizable);
            fdir.find('.mcol-direc-wrapper').addClass('perc-view-column-fixed');
            fdir.find('.ui-resizable').append('<div class="ui-resizable-handle ui-resizable-e perc-resize-disabled" />');
            return fdir;
        }

        function make_top_level( dir ) {
            var dv = $("<div class='perc-finder-table'>"+
                "<table><tr id='perc-finder-table-top'></tr></table></div>");
            dv.find('tr').append( dir );
            return dv;
        }

        function dir_children( dir ) {
            return dir_container(dir).children();
        }

        function dir_container( dir ) {
            return dir.find('.mcol-direc-wrapper');
        }

        /**
         * Enables or disables the finder droppables.
         * @param enable {boolean} flag indicating an enable operation
         * if <code>true</code>
         */
        function enableDisableFinderDroppables(enable){

            $(".mcol-listing").each(function(){
                $(this).droppable();
                $(this).droppable("option", "disabled", !enable);

            });
            $("#perc-finder-listview table").droppable("option", "disabled", !enable);
        }

        /**
         * Retreives the site name from the current path in search bar
         * i.e. /Sites/mysite.com/page1.html will return 'mysite.com'
         * @param path {string} the full path currently displayed in the finder search bar in CM1
         * @return siteName the name of the site
         */
        function getSiteNameByPath(path) {
            if (!path || typeof (path) != 'string' || path.length < 1)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.finder@Nonvalid String")});
                return;
            }
            var index = path.indexOf('/', 1);
            var index2 = path.indexOf('/', index + 2);

            if(index2 !== -1) {
                var siteName = path.substring((index + 1), index2);
                return siteName;
            }
            return  path.substring(index + 1);
        }

        /**
         * Gets the PathItem corresponding to the given path, may be <code>null</code> if the object corresponding to the
         * path has never been expanded in the finder.
         *
         * @param path(String)
         *            must not be empty.
         * @return PathItem (@see PSPathItem for the structure.)
         *
         */
        function getPathItemByPath(path)
        {
            if (!path || typeof (path) != 'string' || path.length < 1)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.finder@Nonvalid String")});
                return;
            }
            var pItem = null;
            var objectId = null;
            $.each(_finderPathIdArray, function(key, value){
                if(key == path)
                {
                    objectId = value;
                }
            });
            if (!objectId)
            {
                var altPath = "";
                if (path[path.length - 1] != "/")
                    altPath = path + "/";
                else
                    altPath = path.substring(0,path.length - 1);
                $.each(_finderPathIdArray, function(key, value){
                    if(key == altPath)
                    {
                        objectId = value;
                    }
                });
            }
            if(objectId)
            {
                pItem = getPathItemById(objectId.toString());
            }
            return pItem;
        }

        /**
         * Gets the PathItem corresponding to the given id, may be <code>null</code> if the object corresponding to the
         * id has never been expanded in the finder.
         *
         * @param objectId(String)
         *            must not be empty.
         * @return PathItem (@see PSPathItem for the structure.)
         *
         */
        function getPathItemById(objectId)
        {
            if (!objectId || typeof (objectId) != 'string' || objectId.length < 1)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.finder@Object ID Nonvalid String")});
                return;
            }
            var pItem = $("#" + FINDER_LISTING_ID_PREFIX + objectId).data('spec');

            return pItem;
        }

        /**
         * Gets the parent PathItem corresponding to the given path, may be <code>null</code> if the object corresponding to the
         * path has never been expanded in the finder.
         *
         * @param path(String)
         *            must not be empty.
         * @return PathItem (@see PSPathItem for the structure.)
         *
         */
        function getParentPathItemByPath(path)
        {
            if (!path || typeof (path) != 'string' || path.length < 1)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.finder@Path Nonvalid String")});
                return;
            }
            if(path.charAt(path.length-1)=="/")
                path = path.substring(0,path.length-1);
            path = path.substring(0,path.lastIndexOf("/"));
            return getPathItemByPath(path);
        }

        function addPathChangedListener( new_listener ) {
            (function( old_listener ) {
                path_changed = function(path){ old_listener(path); new_listener(path); };
            })(path_changed);
        }

        function executePathChangedListeners(path) {
            path_changed(path);
        }

        /**
         * Adds an open listener to the finder to be notified when an object
         * was requested to be opened (i.e. double clicked).
         * @param listener {Function} the open listener callback function that
         * will be called when an open event occurs. Cannot be <code>null</code>.
         */
        function addOpenListener(listener)
        {
            if($.inArray(listener, openListeners) == -1)
            {
                openListeners.push(listener);
            }
        }

        /**
         * Removes the specified open listener if it exists.
         * @param listener {Function} the open listener callback function to be
         * removed. Cannot be <code>null</code>.
         */
        function removeOpenListener(listener)
        {
            if($.inArray(listener, openListeners) > -1)
            {
                var len = openListeners.length;
                for(var i = 0; i < len; i++)
                {
                    if(openListeners[i] === listener)
                    {
                        openListeners.splice(i, 1);
                        return;
                    }
                }
            }
        }

        /**
         * Fires open event informing all registered open listeners.
         * @param info
         */
        function fireOpenEvent(info)
        {
            var len = openListeners.length;
            for(var i = 0; i < len; i++)
            {
                openListeners[i](info);
            }
        }


        /**
         * Adds an action listener to the finder to be notified when a finder
         * action occurs.
         * @param listener {Function} the action listener callback function that
         * will be called when an action event occurs. Cannot be <code>null</code>.
         */
        function addActionListener(listener)
        {
            if($.inArray(listener, actionListeners) == -1)
            {
                actionListeners.push(listener);
            }
        }

        /**
         * Removes the specified action listener if it exists.
         * @param listener {Function} the action listener callback function to be
         * removed. Cannot be <code>null</code>.
         */
        function removeActionListener(listener)
        {
            if($.inArray(listener, actionListeners) > -1)
            {
                var len = actionListeners.length;
                for(var i = 0; i < len; i++)
                {
                    if(actionListeners[i] === listener)
                    {
                        actionListeners.splice(i, 1);
                        return;
                    }
                }
            }
        }

        /**
         * Fires action event informing all registered action listeners.
         * @param action {string} the action type.
         * @param data {object} any extra data needed about the fired action. May
         * be <code>null</code>.
         */
        function fireActionEvent(action, data)
        {
            var len = actionListeners.length;
            for(var i = 0; i < len; i++)
            {
                actionListeners[i](action,
                    typeof(data) == 'object' ? data : null);
            }
        }

    }

    $.perc_finderInstance = null;

})(jQuery);
