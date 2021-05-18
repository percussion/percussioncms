
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

    //Pairs of
    //{Element to test if visible: Element on which to invoke click event}
    //
    $.globalEventIds = {'#perc_head #perc-links':'#perc_head #perc_topnav'};
//Onclick event at the document level to toggle any elements
//put in globalEventIds
    document.onclick = function() {
        $.each($.globalEventIds, function(k, v) {
                if($(k).hasClass('perc-visible')) {
                    $(v).click();
                }
            }
        )};

    $.ajaxSetup( { cache: false } );
    $.Percussion = $.Percussion || {};

    $.perc_fakes = {
        path_service: false,
        page_service: {
            create_page: false,
            render_page: false,
            new_asset: false,
            get_widget_ctypes: false
        },
        template_service: {
            get_template_css: false,
            set_template_css: false
        }
    };

//gives feedback on focus - not complete, but useful for debugging.
    /*
    setInterval( function() {
                    $("a,input,button,textarea")
                          .unbind('.focus')
                          .bind( 'focus.focus', function(){
                                     $(this).css({ border: 'thin dashed grey' } );
                                        } )
                       .bind( 'blur.focus', function(){
                              $(this).css({border: 'none'});
                              });
    }, 500 );
    */

    $.perc_utils = {
        id : id,
        acop : acop,
        acat : acat,
        odiff : odiff,
        a_o : a_o,
        o_a : o_a,
        elem: elem,
        show_error : show_error,
        alert_dialog: alert_dialog,
        prompt_dialog: prompt_dialog,
        confirm_dialog: confirm_dialog,
        choose_icon : choose_icon,
        path_id : path_id,
        input : input,
        handleLinks : handleLinks,
        handleObjects : handleObjects,
        select : select,
        extract_path: extract_path,
        extract_path_end: extract_path_end,
        click_and_double_click: click_and_double_click,
        unxml: unxml,
        rexml: rexml,
        arem: arem,
        deep_get : deep_get,
        debug : debug,
        info : info,
        error : error,
        isBlankString:isBlankString,
        convertCXFArray : convertCXFArray,
        encodeURL : encodeURL,
        preLoadImages: preLoadImages,
        replaceURLWithHTMLLinks : replaceURLWithHTMLLinks,
        sortCaseInsensitive : sortCaseInsensitive,
        formatTimeFromDate : formatTimeFromDate,
        copyRegionObject : copyRegionObject,
        max : max,
        min : min,
        addArrays : addArrays,
        newArray : newArray,
        splitDateTime : splitDateTime,
        addAutoScroll : addAutoScroll,
        removeAutoScroll : removeAutoScroll,
        formatFileSize : formatFileSize,
        parseUTCintoDate : parseUTCintoDate,
        getDisplayFormat : getDisplayFormat,
        makeFolderEditable : makeFolderEditable,
        contains: contains,
        encodePathArray : encodePathArray,
        isPathUnderDesign : isPathUnderDesign,
        percParseInt : percParseInt,
        logToServer : logToServer,
        checkValidUserSession : checkValidUserSession,
        checkMandatoryFieldsEmpty : checkMandatoryFieldsEmpty,
        getContentId : getContentId
    };

    function getContentId(id)
    {
        if (id !== null && id !== undefined){
            var idArray = id.split("-");
            return idArray[idArray.length - 1];
        } else {
            console.warn("Cannot get contentId from given Asset.");
            return false;
        }
    }

    function percParseInt(value)
    {
        var result = null;
        if(value){
            try{
                result = parseInt(value,10);
            }
            catch(err){
                //ignore
            }

        }
        return result;
    }
    function getDisplayFormat(path)
    {
        if(typeof path !== "string")
            return "";
        var pathConstant = path.split("/");
        var value = "";
        while (pathConstant.length > 0)
        {
            value = $.perc_displayformats[pathConstant.join("/")];
            if (value !== undefined)
            {
                return value;
            }
            pathConstant.pop();
        }
        return "";
    }

    /**
     * Logs supplied mesage to client and then server.
     * @param {Object} type if debug, error otherwise set to info.
     * @param {Object} category the category name to recognize the error on server
     * @param {Object} message the message that needs to be logged must be a valid string if not no logging happens.
     */
    function logToServer(type, category, message){
        if(!($.type( type ) === "string")){
            return;
        }
        //Log client side
        if('debug' === type){
            debug(message);
        }
        else if('error' === type){
            error(message);
        }
        else{
            info(message);
        }
        $.PercUtilService.logToServer(type, category, message);
    }

    /**
     * Checks if valid user session present.
     * calls call back function on session validity
     * failCaseCallback if user session is invalid.
     * passCaseCallback if user session is valid.
     */
    function checkValidUserSession(failCaseCallback , passCaseCallback) {
        $.ajax({
            url: '/Rhythmyx/sessioncheck',
            type: 'get',
            cache: false,
            data: {},
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success: function (json,textStatus) {
                if (!json || (json && 0 >= json.expiry)) {
                    failCaseCallback(json , textStatus);
                }else{
                    passCaseCallback(json, textStatus);
                }},
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                console.log("error :" + XMLHttpRequest.responseText);
            }
        });
    }

    function checkMandatoryFieldsEmpty(frame) {

        if(frame.contents().find("#perc-content-form").find("#perc-enable-feed").is(':checked')) {
            frame.contents().find("#perc-content-form").find("#perc-content-edit-metadata-panel").find('label').each(function(){
                if(!$(this).hasClass("perc-required-field")){
                    $(this).addClass("perc-required-field");
                }
            });
        }else{
            frame.contents().find("#perc-content-form").find("#perc-content-edit-metadata-panel").find('label').each(function(){
                if($(this).hasClass("perc-required-field")){
                    $(this).removeClass("perc-required-field");
                }
            });
        }

        var showMandatoryFieldAlertPopUp=false;
        frame.contents().find("#perc-content-form").find('label').each(function(){
            if($(this).hasClass("perc-required-field")){
                if($(this).siblings('input').val()===''){
                    //dosubmit = false;
                    showMandatoryFieldAlertPopUp=true;
                }
            }
        });


        if(showMandatoryFieldAlertPopUp){
            var dialogMarkup = $('<div/>');

            dialogMarkup.append(
                $('<div style="height: 50px; overflow:hidden; margin-left: 30px; margin-right: 30px">')
                    .append(
                        $('<p></p>')
                    )
            );

            dialogMarkup.find('p').html(I18N.message("perc.ui.utils@Please fill all required fields"));
            var dialogButtons = {
                "Close Normal": {
                    id: "perc_fill_mandatory_fields_close",
                    click: function()
                    {
                        dialog.remove();
                    }
                }
            };

            var dialogOptions = {
                id: "perc_fill_mandatory_fields_dialog",
                title: I18N.message("perc.ui.utils@Mandatory Form Fields Alert"),
                modal: true,
                resizable: false,
                closeOnEscape: false,
                width: 'auto',
                height: 'auto',
                percButtons: dialogButtons
            };

            dialog = $(dialogMarkup).perc_dialog(dialogOptions);

        }
        return showMandatoryFieldAlertPopUp;
    }

    /**
     *  Tranforms a UTC String to be correctly read by new Date() function in IE and Safari browsers
     *  Transforms from the following format: "yyyy-MM-ddTHH:mm:ss.SSS-HH:mm" to "yyyy/MM/ddTHH:mm:ss-HHmm"
     *  Note: this is required due to some problem that javascript constructor new Date() presents in IE an Safari
     *
     *  @param dateTime (string) the string with the original UTC format
     *  @return the created date from the transformed string.
     */
    function parseUTCintoDate (dateTime)
    {
        var dateTimeString = dateTime.replace(/(\d\d)-(\d\d)-(\d\d)/, "$1/$2/$3").replace(/([+-]\d\d):(\d\d)/, "$1$2").replace(/\.\d\d\d/, "").replace("T"," ");
        var dateObject = new Date(dateTimeString);
        return dateObject;
    }

    /**
     *  Splits a Date object into its date and time components.
     *  @param dateTimeString (string) the date time string to parse.
     *  The format of the string should be ISO-8601. Eg: 2012-01-04T15:16:11.000-02:00
     *  @param dateFormat (string) optional. Default date format is "M d, yy". Time format is "h:mm AM"
     */
    function splitDateTime(dateTimeString, dateFormat) {
        var dateTime = new Date(dateTimeString);
        if(typeof(dateTime) === 'undefined' || isNaN(dateTime)) // in IE it returns NaN
        {
            dateTime = parseUTCintoDate(dateTimeString);
        }

        if(!dateFormat)
            dateFormat = "M d, yy";
        var date  = $.datepicker.formatDate(dateFormat, dateTime);
        var time  = formatTimeFromDate(dateTime);
        return {date : date, time : time};
    }

    /**
     * A utility function to convert cxf arrays to JavaScript arrays.
     * @param cxfarray array to convert.
     * @return JavaScript array
     */

    function convertCXFArray(cxfarray) {

        if(typeof(cxfarray) === 'undefined')
            return [];

        if(!$.isArray(cxfarray))
            return [cxfarray];

        return cxfarray;
    }



    /**
     * A utility function to check whether supplied string is blank or not.
     * @param str String to be checked for.
     * @return true if the str is undefined or null or not string type or the length of it is < 1. Otherwise false.
     */
    function isBlankString(str){
        return !str || str === null || 'string' !== typeof str || $.trim(str).length < 1 || "undefined" === typeof str;
    }

    function elem( needle, haystack ){
        var matches = $.grep( haystack, function(h) { return h === needle; } );
        return matches.length > 0;
    }

    function select( label, name, id ) {
        return "<label for='"+id+"'>" + label + "</label><br/>" +
             "<select name='"+name+"' id='"+id+"' ></select><br/>";
    }

    function input( label, name, id, tabindex, type ) {
        type = type || "text";
        return "<label for='"+id+"'>" + label + "</label><br/>" +
             "<input type='"+type+"' name='"+name+"' id='"+id+"' ></input><br/>";
    }

    function extract_path( path ) {
        var p = path.split('/');
        if( p.length > 1 && p[ p.length - 1 ] === "" )
            p.pop();
        return p;
    }

    function path_id(path){
        return $.map( path, function(x)
        {
            return x;
            //return x.replace(/[^a-zA-Z0-9\/]/g, '_');
        }).join('-');
    }

//Generic perc dialog that allows to apply specific styling
    $.fn.perc_dialog = function(options)    {
        //Passing perc classes to the dialog
        options.dialogClass='perc-dialog perc-dialog-corner-all';
        options.zIndex=9500;

        //Call jquery dialog with all the options
        var dlgContent = $(this).dialog(options);
        var uiDialog = dlgContent.closest('.ui-dialog');
        //Set id of the dialog if available
        if(options.id)    {
            uiDialog.attr('id', options.id);
        }
        // Fix for CML-3917.  Forces window to resize twice which apparently fixes the problem.
        $(window).trigger("resize").trigger("resize");

        return dlgContent;
    };

    function initializeShowAgainCheck(event, ui){
        var showCheck =  $("<div/>")
            .addClass("perc-show-again")
            .append(
                $("<input/>")
                    .attr("type","checkBox")
                    .attr("id","perc_show_again_check")
            )
            .append(
                $("<label/>")
                    .attr("for", "perc_show_again_check")
                    .text("Do not show again")
            );
        $(this).parent().find(".ui-dialog-buttonpane").append(showCheck);
    }

//Check the "dont show again" user setting of the currentDialogId.
    function dontShowAgain(confirmDialogId){
        var currentUser = $.PercNavigationManager.getUserName();
        var cookieKey = "dontShowAgain-" + currentUser + "-" + confirmDialogId;
        var userSetting = $.cookie(cookieKey);
        return userSetting === "true";
    }

//Save the user choice to avoid seeing the dialog each time.
    function saveConfirmUserSetting(event, ui){
        var dontShowAgainOption = $(this).parent().find("#perc_show_again_check").is(':checked');
        if (dontShowAgainOption) {
            var currentUser = $.PercNavigationManager.getUserName();
            var confirmDialogId = $(this).parent().attr('id');
            var cookieKey = "dontShowAgain-" + currentUser + "-" + confirmDialogId;
            $.cookie(cookieKey, "true");
        }
    }

//A mildly specialized version of the jQuery dialog.
    /**
     * A confirmation diloag that takes the following options.
     * title: The dialog title.
     * question: The confirmation question to be diaplayed.
     * type: Flag to indicate the type of buttons to show. <code>YES_NO</code> requests
     *      2 buttons, Yes and No. <code>OK</code> requests a single OK button.
     *      <code>OK_CANCEL</code> requests 2 buttons, OK and cancel. Any other value is
     *      treated as OK_CANCEL.
     * cancel: Cancel call back that will be called when cancel or no button is clicked.
     * success: Success callback that will be called when ok or yes button is clicked.
     * 09/17/2012 Luis A. Mendez
     * showAgainCheck: default "false", add the capability to avoid seeing this dialog each time, the user can select to not see again.
     * the user choice is stored using cookie with the cookieKey: "dontShowAgain-" + currentUser + "-" + confirmDialogId;
     * dontShowAgainAction: the default action if the dont show again option was checked is the success function, using this property we
     * can define an alternative action.
     */
    function confirm_dialog( options ) {

        //Default settings
        var settings = {
            title: I18N.message("perc.ui.utils@Confirm"),
            question: I18N.message("perc.ui.utils@Do You Want To"),
            type: "OK_CANCEL",
            cancel: function(){},
            success: function(){},
            showAgainCheck: false,
            dontShowAgainAction: null
        };

        function genericSuccess(){
            settings.success();  dialog.dialog( "close" ); dialog.remove();
        }
        function genericCancel(){
            settings.cancel();  dialog.dialog( "close" ); dialog.remove();
        }

        $.extend( settings, options );
        var buttons = {};
        if(settings.type === "YES_NO")
        {
            buttons = {
                "Yes":  {
                    click: genericSuccess,
                    id: "perc-confirm-generic-yes"
                },
                "No": {
                    click: genericCancel,
                    id: "perc-confirm-generic-no"
                }
            };
        }
        else if(settings.type === "YES_PREFERRED_NO")
        {
            buttons = {
                "Yes Preferred": {
                    click: genericSuccess,
                    id: "perc-confirm-generic-yes"
                },
                "No Silver": {
                    click: genericCancel,
                    id: "perc-confirm-generic-no"
                }
            };
        }
        else if(settings.type === "YES_NO_PREFERRED")
        {
            buttons = {
                "Yes Silver": {
                    click: function() {settings.success();  dialog.remove();  },
                    id: "perc-confirm-generic-yes"
                },
                "No": {
                    click: function() {settings.cancel(); dialog.remove(); },
                    id: "perc-confirm-generic-no"
                }
            };
        }
        else if (settings.type === "OK")
        {
            buttons = {
                "Ok":  {
                    click: genericSuccess,
                    id: "perc-confirm-generic-ok"
                }
            };
        }
        else if (settings.type === "OVERRIDE_OK")
        {
            buttons = {
                "Ok": {
                    click: genericCancel,
                    id: "perc-confirm-generic-ok"
                },
                "Override":  {
                    click: genericSuccess,
                    id: "perc-confirm-generic-override"
                }
            };
        }
        else if (settings.type === "SAVE_BEFORE_CONTINUE")
        {
            buttons = {
                "Save":  {
                    click:  function() {
                        settings.save(genericSuccess);
                    },
                    id: "perc-confirm-generic-save"
                },
                "Cancel":  {
                    click: genericCancel,
                    id: "perc-confirm-generic-cancel"
                },
                "Don't Save": {
                    click: function() {settings.dontSaveCallback(); dialog.dialog( "close" ); dialog.remove();},
                    id: "perc-confirm-generic-continue"
                }
            };
        }
        else if (settings.type === "CANCEL_CONTINUE")
        {
            buttons = {
                "Continue":  {
                    click: genericSuccess,
                    id: "perc-confirm-generic-continue"
                },
                "Cancel Blue":  {
                    click: genericCancel,
                    id: "perc-confirm-generic-cancel"
                }
            };
        }
        else if (settings.type === "CANCEL_START")
        {
            buttons = {
                "Start":  {
                    click: genericSuccess,
                    id: "perc-confirm-generic-start"
                },
                "Cancel":  {
                    click: genericCancel,
                    id: "perc-confirm-generic-cancel"
                }
            };
        }
        else
        {
            buttons = {
                "Ok":  {
                    click: genericSuccess,
                    id: "perc-confirm-generic-ok"
                },
                "Cancel": {
                    click: genericCancel,
                    id: "perc-confirm-generic-cancel"
                }
            };
        }
        var dialog;
        var dlgOptions = {
            "dialogClass": "perc-confirm-dialog",
            "title":settings.title,
            "modal":true,
            "resizable": false,
            "percButtons": buttons,
            "id": settings.id
        };
        if(options.height)
            dlgOptions.height = options.height;
        if(options.width)
            dlgOptions.width = options.width;

        //Add don't show again functionality
        if(settings.showAgainCheck){
            //Check user setting on show again warning.
            if (dontShowAgain(dlgOptions.id)){
                if (typeof(options.dontShowAgainAction) === "function")
                    options.dontShowAgainAction();
                else
                    options.success();
                return "";
            }
            dlgOptions.open = initializeShowAgainCheck;
            dlgOptions.beforeclose = saveConfirmUserSetting;
        }

        dialog = $("<div/>").append( settings.question ).perc_dialog(dlgOptions);
    }

    function alert_dialog( options ) {
        var settings = {
            title       : I18N.message("perc.ui.utils@Confirm"),
            content     : I18N.message("perc.ui.utils@Something Wrong"),
            okCallBack  : function(){}
        };

        $.extend( settings, options );

        //Using the title size with fixed number to come with approximate width
        // minimum width is 400px
        var w = (typeof settings.width === 'undefined' || settings.width === null) ?
            Math.max(settings.title.length * 35, 400) : settings.width;

        var dialog;
        dialog = $("<div/>").append( settings.content ).perc_dialog({
            dialogClass   : 'perc-alert-dialog',
            title         : settings.title,
            modal         : true,
            width         : w,
            resizable     : false,
            id            : settings.id,
            "percButtons" : {
                "Ok" : {
                    click : function() { dialog.remove(); settings.okCallBack(); },
                    id    : "perc-alert-generic-ok"
                }
            }
        });
    }

    function prompt_dialog( options )
    {
        var settings = {
            title:I18N.message("perc.ui.utils@Beg Pardon"),
            question: I18N.message("perc.ui.utils@What To Do"),
            cancel: function(){},
            success: function(){}
        };

        $.extend( settings, options );

        var inputField = $("<input type='text' id='perc-prompt-dialog-question' />");
        var dialog = $("<div/>")
            .append( $("<label for='perc-prompt-dialog-question'/>").append(settings.question) )
            .append( $("<br/>") )
            .append( inputField )
            .dialog(
                {
                    dialogClass: 'perc-prompt-dialog',
                    title: settings.title,
                    modal: true,
                    resizable: false,
                    "percButtons" :
                        {
                            "Yes":
                                {
                                    click: function()
                                    {
                                        var res = inputField.val(); dialog.remove(); settings.success(res);
                                    },
                                    id: "perc-prompt-generic-yes"
                                },
                            "No":
                                {
                                    click: function()
                                    {
                                        dialog.remove(); settings.cancel();
                                    },
                                    id: "perc-prompt-generic-no"
                                }
                        },
                    id: settings.id
                });
    }

    function id(x) { return x; }

    function acop( arr ) {
        return $.map( arr, id );
    }

    function acat( a1, a2 ) {
        return acop(a1).concat( acop(a2) );
    }

    function odiff( eye, beam ) {
        //Remove beam from eye
        var diff = {};

        $.each(eye, function(k,v) {
            if ( ! beam[k] )
                diff[k] = v;
        });

        return diff;
    }

    function arem( eye, beam ) {
        $.each( eye, function(idx) {
            if( eye[idx] === beam ) {
                delete eye[idx];
            }
        } );

    }

    function a_o( obj ) {
        var a = [];
        $.each( obj, function(k) {
            a.push( [k,this] );
        } );
        return a;
    }

    function o_a( arr ) {
        var o = {};
        $.each( arr, function() {
            o[ this.k ] = this.v;
        });
        return o;
    }


    function extract_path_end( path ){
        var path_end = path.split('/');
        if( path_end[ path_end.length - 1 ] === '' )
            path_end.pop();
        return path_end[ path_end.length - 1 ];
    }

    function show_error( x ) {
        if( x ) {
            alert( x );
        }
    }


    $.fn.perc_button = function( ) {
        this.addClass("ui-state-default ui-corner-all")
            .hover(function(){
                    if( ! $(this).hasClass("ui-state-disabled") )
                        $(this).addClass("ui-state-hover");
                },
                function(){
                    $(this).removeClass("ui-state-hover");
                } );
        return this;
    };

    /**
     *  Formats the file size to B / KB / MB according to the size itself
     *  Eg: 320 B, 4 KB, 2 MB
     *  @author federicoromanelli
     *  @param size (number) the size of the file in bytes
     *  @return a string with the formatted size of the file or an empty string if the size is not a number.
     */
    function formatFileSize (size)
    {
        var oneKB = 1024;
        var oneMB = (1024 * 1024);

        var fileSize = parseFloat(size);
        if(isNaN(fileSize))
            return "";

        if (fileSize < oneKB)
            return fileSize + " B";
        if (fileSize > oneMB)
            return (fileSize / oneMB).toFixed(0) + " MB";

        return (fileSize / oneKB).toFixed(0) + " KB";
    }

    /**
     * Utility function for returning the icon to be used by the ui.
     *
     * @param {string} type - The icon type.
     * @param {string} icon - The icon to return
     * @param {id} - The id of the icon to return
     */
    function choose_icon( type, icon , id) {
        var returnIcon = new Object();
        var type_icons = {
            'site': '/cm/images/images/iconWebsite.gif',
            'Folder' : '/cm/images/images/iconFolder.gif',
            'percPage' :'/cm/images/images/iconPage.gif',
            'FSIMAGEFile' : '/cm/images/images/genericImage.png',
            'FSFile' : '/cm/images/images/genericFile.png',
            'FSFolder' :'/cm/images/images/iconFolder.gif'
        };

        if(id === ',' + $.perc_paths.ASSETS_ROOT_NO_SLASH){
            returnIcon.src='/cm/images/images/iconLibrary.gif';
            returnIcon.alt=I18N.message("perc.ui.images@AssetLibraryIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@AssetLibraryIconTitle");
            returnIcon.decorative=false;
            return returnIcon;
        }

        if(id === ',' + $.perc_paths.SITES_ROOT_NO_SLASH){
            returnIcon.src='/cm/images/images/iconWebsite.gif';
            returnIcon.alt=I18N.message("perc.ui.images@SiteIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@SiteIconTitle");
            returnIcon.decorative=false;
            return returnIcon;
        }

        if(id === ',' + $.perc_paths.DESIGN_ROOT_NO_SLASH){
            returnIcon.src='../images/images/iconDesign.png';
            returnIcon.alt=I18N.message("perc.ui.images@DesignIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@DesignIconTitle");
            returnIcon.decorative=false;
            return returnIcon;
        }

        if(id === ',' + 'Search'){
            returnIcon.src='../images/images/searchIcon.png';
            returnIcon.alt=I18N.message("perc.ui.images@SearchIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@SearchIconTitle");
            returnIcon.decorative=false;
            return returnIcon;
        }

        if (id === ',' + $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
            returnIcon.src='/cm/images/images/iconRecycle.gif';
            returnIcon.alt=I18N.message("perc.ui.images@RecyclingIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@RecyclingIconTitle");
            returnIcon.decorative=false;
            return returnIcon;
        }

        //If statement for Landing Page Icon
        if (icon && icon.includes('finderLandingPage') && type && type_icons[type]) {
            returnIcon.src = "/Rhythmyx/sys_resources/images/finderLandingPage.png";
            returnIcon.alt = I18N.message("perc.ui.newpagedialog.label@Navigation Landing Page");
            returnIcon.title =I18N.message("perc.ui.newpagedialog.label@Navigation Landing Page");
            returnIcon.decorative = false;
            return returnIcon;
        }else if( icon ){
            //TODO: Need to map calls to this to make sure they are setting alt/title/role
            debug("Accessibility Check: Verify that " + icon + " has accessible attributes set");
            debug("i18n Check: Verify that " + icon + " has i18n strings set");

            /*returnIcon.src=icon;
            returnIcon.alt=I18N.message("perc.ui.images@FolderIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@FolderIconTitle");
            returnIcon.decorative=true;
            return returnIcon;*/
        }

        if( type && type_icons[ type ] ){

            if(type==="site"){
                returnIcon.src='/cm/images/images/iconWebsite.gif';
                returnIcon.alt=I18N.message("perc.ui.images@SiteIconAlt");
                returnIcon.title=I18N.message("perc.ui.images@SiteIconTitle");
                returnIcon.decorative=false;
                return returnIcon;
            }

            if( type==="percPage" ){
                returnIcon.src =  '/cm/images/images/iconPage.gif';
                returnIcon.alt=I18N.message("perc.ui.images@PageIconAlt");
                returnIcon.title=I18N.message("perc.ui.images@PageIconTitle");
                returnIcon.decorative=false;
                return returnIcon;
            }

            if(type === "Folder" || type === "FSFolder"){
                if(typeof icon ==="undefined" || icon.indexOf("finderFolder.png")>0){
                    returnIcon.src =  '/cm/images/images/iconFolder.gif';
                }else{
                    returnIcon.src =  icon;
                }
                //returnIcon.src =  '/cm/images/images/iconFolder.gif';
                returnIcon.alt=I18N.message("perc.ui.images@FolderIconAlt");
                returnIcon.title=I18N.message("perc.ui.images@FolderIconTitle");
                returnIcon.decorative=false;
                return returnIcon;
            }
            if(type === 'FSIMAGEFile'){
                returnIcon.src = '/cm/images/images/genericImage.png';
                returnIcon.alt=I18N.message("perc.ui.images@ImageAssetIconAlt");
                returnIcon.title=I18N.message("perc.ui.images@ImageAssetIconTitle");
                returnIcon.decorative=true;
                return returnIcon;
            }
            if('FSFile' === type){
                returnIcon.src =  '/cm/images/images/genericFile.png';
                returnIcon.alt=I18N.message("perc.ui.images@FileAssetIconAlt");
                returnIcon.title=I18N.message("perc.ui.images@FileAssetIconTitle");
                returnIcon.decorative=true;
                return returnIcon;
            }
            //TODO: Need to map calls to this to make sure they are setting alt/title/role
            debug("Accessibility Check: Verify that " + icon + " has accessible attributes set");
            debug("i18n Check: Verify that " + icon + " has i18n strings set");


            returnIcon.src = type_icons[ type ];
            returnIcon.alt='';
            returnIcon.title='';
            returnIcon.decorative=false;
            return returnIcon;
        }

        if('percImageAsset' === type){
            // Last resort if image icon url not provided
            returnIcon.src = '../images/images/genericImage.png';
            returnIcon.alt=I18N.message("perc.ui.images@ImageAssetIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@ImageAssetIconTitle");
            returnIcon.decorative=true;
            return returnIcon;

        }

        if('percFileAsset' === type || 'percFlashAsset' === type){
            // Last resort if file icon url not provided
            returnIcon.src =  '../images/images/genericFile.png';
            returnIcon.alt=I18N.message("perc.ui.images@FileAssetIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@FileAssetIconTitle");
            returnIcon.decorative=true;
            return returnIcon;

        }

        if(type && id[1] === $.perc_paths.ASSETS_ROOT_NO_SLASH){
            // Last resort if icon url not provided for any other asset type
            returnIcon.src =  '../images/images/genericAsset.png';
            returnIcon.alt=I18N.message("perc.ui.images@GenericAssetIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@GenericAssetIconTitle");
            returnIcon.decorative=true;
            return returnIcon;
        }

        if( type ){
            returnIcon.src =  '/cm/images/images/iconPage.gif';
            returnIcon.alt=I18N.message("perc.ui.images@PageIconAlt");
            returnIcon.title=I18N.message("perc.ui.images@PageIconTitle");
            returnIcon.decorative=true;
            return returnIcon;
        }


        returnIcon.src =  '/cm/images/images/iconFolder.gif';
        returnIcon.alt=I18N.message("perc.ui.images@FolderIconAlt");
        returnIcon.title=I18N.message("perc.ui.images@FolderIconTitle");
        returnIcon.decorative=true;
        return returnIcon;

    }

    /**
     * This handles the show/hide of the Add Widget button
     */
    $.fn.percWidLibMaximizer = function (P)    {
        var baseEle = "#perc-layout-menu";
        if($j("#tabs-3").length)    {
            baseEle = "#tabs-3 #perc-layout-menu";
        }

        if($j(baseEle).parent().find(".perc-template-container").hasClass("perc-visible")) {
            $j(baseEle).parent().find(".perc-template-container").removeClass("perc-visible").addClass("perc-hidden");
            $j(baseEle).parent().find("#perc-wid-lib-expander").removeClass("perc-whitebg");
            $j(baseEle).parent().find("#perc-wid-lib-minimizer").replaceWith('<a id="perc-wid-lib-maximizer" style="float: left;" href="#"></a>');
        } else {
            var regionLibContainer   = $j(baseEle).parent().find(".perc-region-library-container");
            // if region tray is visible, toggle it (close it) so that only the widget tray is shown
            if(regionLibContainer.hasClass("perc-visible")) {
                $.fn.percRegionLibraryMaximizer(P);
            }

            $j(baseEle).parent().find(".perc-template-container").removeClass("perc-hidden").addClass("perc-visible");
            $j(baseEle).parent().find("#perc-wid-lib-expander").addClass("perc-whitebg");
            $j(baseEle).parent().find("#perc-wid-lib-maximizer").replaceWith('<a id="perc-wid-lib-minimizer" style="float: left;" href="#"></a>');
        }

        // fix the height of the iframe based on the height of the top part
        var frame  = $('#frame');
        var header = $('.perc-main');
        var bottom = $('#bottom');
        fixIframeHeight(header, bottom, frame);
    };

    /**
     * This handles the show/hide of the Explore Regions button
     */
    $.fn.percRegionLibraryMaximizer = function (P)    {
        var baseEle = "#perc-layout-menu";
        if($j("#tabs-3").length)    {
            baseEle = "#tabs-3 #perc-layout-menu";
        }

        var parent = $j(baseEle).parent();
        var regionLibraryContainer = $j(parent.find(".perc-region-library-container"));
        var templateContainer      = $j(baseEle).parent().find(".perc-template-container");
        var regionLibraryExpander  = $j(parent.find("#perc-region-library-expander" ));
        var regionLibraryMaximizer = $j(parent.find("#perc-region-library-maximizer"));
        var regionLibraryMinimizer = $j(parent.find("#perc-region-library-minimizer"));

        if( regionLibraryContainer.hasClass("perc-visible")) {
            regionLibraryContainer.removeClass("perc-visible").addClass("perc-hidden");
            regionLibraryExpander.removeClass("perc-whitebg");
            regionLibraryMinimizer.replaceWith('<a id="perc-region-library-maximizer" style="float: left;" href="#"></a>');
        } else {
            // if widget tray is visible, toggle it (close it) so that only the region tray is shown
            if(templateContainer.hasClass("perc-visible")) {
                $.fn.percWidLibMaximizer(P);
            }

            regionLibraryContainer.removeClass("perc-hidden").addClass("perc-visible");
            regionLibraryExpander.addClass("perc-whitebg");
            regionLibraryMaximizer.replaceWith('<a id="perc-region-library-minimizer" style="float: left;" href="#"></a>');
        }

        // fix the height of the iframe based on the height of the top part
        var frame  = $('#frame');
        var header = $('.perc-main');
        var bottom = $('#bottom');
        fixIframeHeight(header, bottom, frame);
    };

    /**
     * This handles the show/hide of the Orphan Assetss button
     */
    $.fn.percOrphanAssetsMaximizer = function (P) {
        var baseEle = "#perc-layout-menu";
        if($j("#tabs-2").length)    {
            baseEle = "#tabs-2 #perc-content-menu";
        }

        var parent = $j(baseEle).parent();
        var orphanAssetsContainer = parent.find("#perc_asset_library");
        var orphanAssetsExpander  = parent.find("#perc_orphan_assets_expander" );

        if( orphanAssetsContainer.hasClass("perc-visible")) {
            var orphanAssetsMinimizer = parent.find("#perc_orphan_assets_minimizer");
            orphanAssetsContainer.removeClass("perc-visible").addClass("perc-hidden");
            orphanAssetsExpander.removeClass("perc-whitebg");
            orphanAssetsMinimizer.replaceWith('<a id="perc_orphan_assets_maximizer" style="float: left;" href="#"></a>');
        } else {
            var orphanAssetsMaximizer = parent.find("#perc_orphan_assets_maximizer");
            orphanAssetsContainer.removeClass("perc-hidden").addClass("perc-visible");
            orphanAssetsExpander.addClass("perc-whitebg");
            orphanAssetsMaximizer.replaceWith('<a id="perc_orphan_assets_minimizer" style="float: left;" href="#"></a>');
        }

        // fix the height of the iframe based on the height of the top part
        var frame  = $('#frame');
        var header = $('.perc-main');
        var bottom = $('#bottom');
        fixIframeHeight(header, bottom, frame);
    };

    function click_and_double_click( elem, single, dbl ){
        var clicked = false;
        var interval = 500; //milliseconds
        elem.click( function() {
            if( clicked ) {
                //Double click - ignore the click event.
            } else {
                //Single click - set up to ignore the next click, if it falls with the chosen interval
                clicked = true;
                setTimeout( function(){ clicked = false; }, interval );
                single();
            }
        });
        elem.dblclick( dbl );
    }
    $.fn.perc_toggle = function( d )    {
        if($(d).length && $(d).hasClass('perc-hidden'))    {
            $(d).removeClass('perc-hidden');
            $(d).addClass('perc-visible');
        }
        else    {
            $(d).removeClass('perc-visible');
            $(d).addClass('perc-hidden');
        }

        return this;
    };
    $.fn.perc_toggle_padding = function(  )    {
        var args = $.fn.perc_toggle_padding.arguments;
        for(var i = 0 ; i < args.length ; i ++ )    {
            if($(args[i]).length && $(args[i]).hasClass('perc-nopadding'))    {
                $(args[i]).removeClass('perc-nopadding');
            }
            else    {
                $(args[i]).addClass('perc-nopadding');
            }
        }
        return this;
    };



    var tot = 0;

    /*
    var recursive_test_schema;
    recursive_test_schema = {'a': '$', 'rts': [function(){ return recursive_test_schema; }]};
    rts_schema = { 'Top': recursive_test_schema };

    rts_json = {'a': 'x', 'rts': [{'a': 'y', 'rts': [{'a': 'z', 'rts': []}]}]};

    // rexml( rts_schema, rts_json ) = <Top><a>x</a><rts><rt><a>y</a>....</Top>

    option_schema = { 'Top': [function(tag) { if(tag == 'a'){ return {'b':'$'} } else { return {'d':'$'} } }] };

    option_xml = "<Top><a><b>foo</b></a><c><d>bar</d></c><a><b>foo2</b></a></Top>";
    */

    function addAutoScroll(){
        $("#frame").percAutoScroll({
            offsetY:10,
            width : 10,
            speed : 10,
            directions : "n, s"
        });
        $("#frame").percAutoScroll.postScrollView = function(movedX, movedY){
            $("div.ui-droppable").each(function(){
                if (!$(this).hasClass("ui-layout-ignore"))
                    $(this).css("top", $(this).position().top - movedY);
            });
        };
    }

    function removeAutoScroll(){
        $("#frame").percAutoScroll.remove();
    }


    function unxml ( schema, data ) {
        if( $.isFunction( schema ) ) {
            schema = schema(data.get(0).tagName);
        }
        if( schema.valueOf() === '$' ) {
            return data.text();
        }
        if( $.isArray( schema ) ) {
            var ret = [];
            var child_schema = schema[0];
            var children = data.children();

            if(children.length === 0 && child_schema === '$')
            {
                var len = data.length;
                for(i = 0; i < len; i++)
                {
                    ret.unshift($(data[i]).text());
                }
            }
            else
            {
                children.each( function(){
                    var next = unxml( child_schema, $(this) );
                    next._tagName = $(this)[0].tagName;
                    ret.push( next );
                });
            }

            return ret;
        }
        var ret = {};
        $.each( schema, function( name ) {
            var tagNames = [];
            var matches =data.children().filter( function() {
                tagNames.push( this.tagName );
                return this.tagName.toLowerCase() === name.toLowerCase(); } );
            if( matches.length ) {
                ret[ name ] = unxml( this, matches );
            } else {
                debug( I18N.message("perc.ui.utils@Expected To Find") + name + I18N.message("perc.ui.utils@Tags Found") + tagNames );
            }
        });
        return ret;
    }

    function rexml( schema, pageObj ) {
        return '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' + _rexml( schema, pageObj );
    }

    function _rexml( schema, pageObj, elemName ) {
        if( $.isFunction( schema ) ) {
            schema = schema(pageObj._tagName);
        }
        if( schema.valueOf() === '$' ) {
            if( !pageObj ) {
                debug( "Unexpected null in rexml" );
                return "";
            } else {
                if (typeof(pageObj) === 'string')
                    return pageObj.replace(/&/g, '&amp;' ).replace( />/g, '&gt;' ).replace( /</g, '&lt;' );
                else
                    return pageObj;
            }
        } else if( $.isArray( schema ) ) {
            var ret = "";
            if(schema.length === 1 && schema[0] === "$")
            {
                var len = pageObj.length;
                for(i = 0; i < len; i++)
                {
                    ret += "<" + elemName + ">" + pageObj[i] + "</" + elemName + ">" ;
                }
            } else if( $.isArray( pageObj ) ) {
                $.each( pageObj, function() {
                    ret += "<" + this._tagName + ">" + _rexml( schema[0], this, this.tagName ) + "</" + this._tagName + ">" ;
                } );
            } else {
                debug( "Expected array, got %o instead", pageObj );
            }
            return ret;
        } else {
            //Object
            var ret = "";
            $.each( schema, function(name) {
                if( name in pageObj ) {
                    var isStringArray = $.isArray(schema[name]) && schema[name].length === 1 && (schema[name])[0] === "$";
                    if(!isStringArray) ret += "<" + name + ">";
                    ret += _rexml( schema[ name ], pageObj[ name ], name );
                    if(!isStringArray) ret += "</" + name + ">";
                } else {
                    debug("Can't find " + name + " in %o", pageObj);
                }
            } );
            return ret;
        }
    }


//deep_get -- safely get an object that is nested in a JSON object.
    function deep_get( json, keys ) {
        var keyArr = keys.split(" ");
        var key;
        while( (key = shift( keyArr )) ) {
            if( key in json ) {
                json = json[ key ];
            }
            else {
                return null;
            }
        }
        return json;
    }

    /**
     * URL encode the specified path. This is needed for none-ascii characters.
     * Firefox seems to be able to automatically URL encode the path before
     * communicate to server, but not IE.
     *
     * @param path the path need to be encoded before use it to communicate to server.<b>
     */
    function encodeURL(path)
    {
        return encodeURIComponent(path).replace(/%2F/g,'/');
    }

    var debugFlag = false;

    /**
     * return <code>true</code> if "debug=true" as part of the request parameters.
     * for example, when use the URL to login CM1: http://localhost:9992/cm/app/?view=dash&debug=true
     */
    function isDebug()
    {
        return debugFlag;
    }

    function debug(message)
    {
        if(!isDebug())
            return;

        var msg;

        if (arguments.length > 1)
            msg = "DEBUG [" + arguments[0] + "] " + arguments[1];
        else
            msg = "[DEBUG] " + arguments[0];

        log(msg);
    }

    function info(message)
    {
        var msg;
        if (arguments.length > 1)
            msg = "INFO [" + arguments[0] + "] " + arguments[1];
        else
            msg = "[INFO] " + arguments[0];

        log(msg);
    }

    function error(message)
    {
        var msg;
        if (arguments.length > 1)
            msg = "ERROR [" + arguments[0] + "] " + arguments[1];
        else
            msg = "[ERROR] " + arguments[0];

        log(msg);
    }

    function log(arg)
    {
        if( window.console && console && console.log)
        {
            if( $.browser.msie )
            {
                console.log( arg );
            }
            else
            {
                console.log.apply( console, arguments );
            }
        }
    }

    var __cache = [];
    /**
     * Preload images into browser cache.<b>
     * @param args a list of comma delimited strings containing
     * image urls.<b>
     */
    function preLoadImages() {
        var args_len = arguments.length;
        for (var i = args_len; i--;) {
            var cacheImage = document.createElement('img');
            cacheImage.src = arguments[i];
            __cache.push(cacheImage);
        }
    }

    /**
     * Replace occurences of URL for a hyperlink
     *
     * @param text {string} The text that might contain hyperlinks
     * @return Same text but with URLs replaced with hyperlinks
     */
    function replaceURLWithHTMLLinks(text) {
        var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
        return text.replace(exp,"<a href='$1'>$1</a>");
    }

    /**
     * Finds all object tags and replaces them with a place holder
     *
     * @param root frame
     */
    function handleObjects(frame){
        var replacedNum = 0;
        frame.contents().find('.perc-flash').find("object").each(function(){
            replacedNum++;
            var obWidth = $(this).attr("width");
            var obHeight = $(this).attr("height");
            var divWidth = obWidth;
            var divHeight = obHeight;
            //If object doesn't have the size check the embed tag else set default size.
            if(obWidth === "")
            {
                var emWidth = $(this).children("embed").attr("width");
                if(emWidth === "")
                {
                    divWidth = '100%';
                }
                else
                {
                    divWidth = emWidth;
                }
            }
            if(obHeight === "")
            {
                var emHeight = $(this).children("embed").attr("height");
                if(emHeight === "")
                {
                    divHeight = '100%';
                }
                else
                {
                    divHeight = emHeight;
                }
            }

            //Check to see if its & or px defaults to px if not specified
            if(divWidth.lastIndexOf("%") < 0 && divWidth.lastIndexOf("px") < 0)
            {
                divWidth = divWidth + 'px';
            }
            if(divHeight.lastIndexOf("%") < 0 && divHeight.lastIndexOf("px") < 0)
            {
                divHeight = divHeight + 'px';
            }

            $(this).replaceWith($('<div id="perc-object-placeholder-'+ replacedNum +'" class="perc-object-placeholder" style="width: ' + divWidth + '; height: ' + divHeight + ';"></div>'));
        });
    }

    /**
     * Finds all links in the rendered content and deactivates them.
     *
     * @param root frame
     */
    function handleLinks(frame){

        frame.contents().find("a").each(function(){
            // Get the href value so we don't lose it
            var url = $(this).attr("href");
            $(this).attr('tempURL', url);
            // Remove target
            $(this).removeAttr("target");
            // Deactivate the link by replacing its href value
            $(this).attr("href", "javascript:void(0)");

            /*
            // For future use to handle the click ourselves, maybe
            // Maybe checking for ctrl-click and then folowing
            // the link. Notice we add the url into the event
            // binding as extra data so that we retain the original href value
            // and are able to use it in the events callback function.
            // It is accessed as evt.data.url.
            $(this).bind("click", {url: url}, function(evt){

            });
            */

        });
        //Fix for YouTube iFrame Overlay and Z-Index Issues for IE
        //CM-8286 -- Videos in Rich Text are placed over popup windows in Editor on IE
        if($.browser.msie){
            frame.contents().find("iframe").each(function(){
                var ifr_source = $(this).attr('src');
                if(ifr_source.length > 0){
                    var wmode = "wmode=transparent";
                    if(ifr_source.indexOf('?') !== -1){
                        $(this).attr('src',ifr_source+'&'+wmode);
                    }
                    else{
                        $(this).attr('src',ifr_source+'?'+wmode);
                    }
                }
            });
        }
    }


    /**
     * Sorts the specified array case insensitive.
     *
     * @param list {array} The list of items to sort, may be modified.
     */
    function sortCaseInsensitive(list) {
        if ($.isArray(list))
        {
            list.sort(function(x,y){
                var a = String(x).toUpperCase();
                var b = String(y).toUpperCase();
                if (a > b)
                    return 1;
                if (a < b)
                    return -1;
                return 0;
            });
        }
    }

    /**
     * Formats a date object into a time string h:mm AM
     */
    function formatTimeFromDate(date, showsecs) {

        var formattedTime;
        var hours      = date.getHours();
        var minutes    = date.getMinutes();
        var seconds    = date.getSeconds();
        var ampm        = (hours>=12 ? " PM" : " AM");

        minutes        = (minutes < 10 ? "0" : "") + minutes;
        hours        = hours%12;

        if(hours === 0) hours = 12;

        formattedTime    = hours + ":" + minutes;
        if(showsecs)
            formattedTime += ":" + (seconds<10?"0" + seconds : seconds);
        formattedTime += ampm;

        return formattedTime;
    }
    function max(array) {
        var mx = -1;
        for(let a=0; a<array.length; a++) {
            if(mx < array[a])
                mx = array[a];
        }
        return mx;
    }

    function min(array) {
        var mn = 10000000;
        for(a=0; a<array.length; a++) {
            if(array[a] < mn )
                mn = array[a];
        }
        return mn;
    }

    function addArrays(array1, array2) {
        var resultArray = [];
        for(i=0; i<array1.length; i++) {
            resultArray[i] = array1[i] + array2[i];
        }
        return resultArray;
    }

    function newArray(length) {
        var array = new Array(length);
        for(i=0; i<length; i++)
            array[i] = 0;
        return array;
    }

    $.percHideBodyScrollbars = function() {
        $("body")
            .css("overflow","hidden");
    };

    $.percShowBodyScrollbars = function() {
        $("body")
            .css("position","")
            .css("overflow","");
    };

    /**
     * Makes the folder editable, by finding the new folder node and simulating
     * dblclick on it.
     */
    function makeFolderEditable(pathItem){
        // Add the JEditable plugin to folders for rename functionality
        var listing = $('#perc-finder-listing-' + pathItem.id);
        var $itemName = listing.children('.perc-finder-item-name');
        var pageItemType = pathItem.type;

        $itemName.editable(
            function(value, settings){
                value = $.trim(value);
                value = $.perc_textFilters.WINDOWS_FILE_NAME(value);

                // only replace spaces with dashes if we are not renaming fsfolders
                if(pageItemType !== 'FSFolder')
                {
                    value = value.replace(/ /g, '-');
                }

                var $nameEl = $(this);
                var oldName = $nameEl.parent().attr('title');
                if(value.length === 0)
                    return oldName;
                if(value !== oldName)
                {
                    $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                    $.PercPathService.renameFolder(
                        pathItem['path'],
                        value,
                        function(status, result, code){
                            if(status === $.PercServiceUtils.STATUS_SUCCESS)
                            {
                                var pth = $.perc_finder().lastClickPath;
                                if($.perc_finder().lastClickPath === null ||  typeof $.perc_finder().lastClickPath === "undefined")
                                {
                                    pth = result.PathItem.path.split("/");
                                    pth.push(value);
                                }
                                $.perc_finder().lastClickPath = null;
                                $.perc_finder().open(pth);
                                $.unblockUI();
                            }
                            else
                            {
                                $nameEl.text(oldName); // Reset back to old name
                                $.unblockUI();
                                var errorMsg = "";
                                if (code === "renameFolderItem.reservedName" || code === "renameFolderItem.longName" ||
                                     code === "renameFolderItem.invalidCharInName")
                                {
                                    errorMsg = result.replace("<old_name>", oldName).replace("<new_name>", value);
                                }
                                else
                                {
                                    //TODO: I18N below with correct formatting
                                    errorMsg = 'Cannot rename folder \'' +
                                        oldName + '\' to \'' + value +
                                        '\' because an object with the same name already exists.';
                                }
                                $.perc_utils.alert_dialog({id: 'perc-finder-rename-folder-error',
                                    title: I18N.message("perc.ui.publish.title@Error"), content: errorMsg});
                            }
                        }
                    );
                }
                else
                {
                    $.perc_finder().refresh();
                }
                return value;
            },
            {
                type: "filteredText",
                event: "dblclick",
                // issue CM-89: removing 'inherit' we avoid the field to have ellipsis
                // style: "inherit",
                height: "14px",
                width: "125px",
                select: true,
                onblur: "submit",
                fieldid: 'perc_finder_inline_field_edit',
                onedit: function(settings){
                    $("#perc_finder_inline_field_edit").blur();
                },
                data: function(value, settings){
                    return $(this).parent().attr('title');
                }
            });
        $('#perc-finder-listing-' + pathItem.id)
            .children(".perc-finder-item-name").trigger('dblclick');
    }

    /**
     * Checks if the specified enumeration value object contains the
     * specified value.
     *
     * @param enumVals JSON object with the following structure:
     * {"EnumVals":{"entries":[{"value":"Archive"},{"value":"Draft"}]}}
     * @param val {string} The value to check for.
     * @return true if the object contains the value, false otherwise.
     */
    function contains(enumVals, val) {
        var vals = enumVals.entries;
        if (!$.isArray(vals))
        {
            var tempArray = [];
            tempArray.push(vals);
            vals = tempArray;
        }

        for (i = 0; i < vals.length; i++)
        {
            if (vals[i].value === val)
            {
                return true;
            }
        }

        return false;
    }

    /**
     *  Encodes the array given as a parameter. The array is not modified, a new one is returned.
     */
    function encodePathArray(path)
    {
        if(typeof(path) === 'undefined')
        {
            return;
        }

        var paths = [];
        $(path).each(function(index, pathElement)
        {
            var encodedPath = encodeURL(pathElement);
            paths.push(encodedPath);
        });
        return paths;
    }

    /**
     * Returns true if the given path is under Design. The path array should be like this:
     * path[] = ""
     * path[] = "Design"
     * path[] = "..."
     * So basically it compares the position 1 in the array with the word "Design"
     */
    function isPathUnderDesign(path)
    {
        if(path.length === 0)
        {
            return false;
        }

        if(path[1] === 'Design')
        {
            return true;
        }
        else
        {
            return false;
        }
    }


})(jQuery);

/**
 * Makes a shallow copy of region object
 */
function copyRegionObject(copyFrom, copyTo) {
    copyTo.vertical = copyFrom.vertical;
    copyTo.children = copyFrom.children;
    copyTo.widgets = copyFrom.widgets;
    copyTo.width = copyFrom.width;
    copyTo.height = copyFrom.height;
    copyTo.owner = copyFrom.owner;
}

function htmlEntities(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/, '&#39;');
}

