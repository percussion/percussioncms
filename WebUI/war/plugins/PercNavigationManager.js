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
 * [PercNavigationManager.js]
 *
 *  The navigation manager is the central controller of page navigation
 *  in the ui.
 */
(function($){
    $.PercNavigationManager = {};

    /* PRIVATE METHODS AND VARIABLES */
    var site = "";
    var siteProperties = {};
    var view = "";
    var mode = "";
    var path = "";
    var pathType = "";
    var memento = {};
    var name = "";
    var id = "";
    var isAdmin = false;
    var isDesigner = false;
    var isAccessibilityUser = false;
    var userName = "";
    var debug = false;
    var autoTest = false;
    var pageChangeListeners  = [];
    var locationChangeRequests = {};
    var allowReopen = false;
    var javascriptOff = false;
    var templateModel;
    var warningMessage = "";
    /**
     * Reference to the registered finder. <code>null</code> until
     * registerFinder is called.
     */
    var finderRef = null;

    /* CONSTANTS */
    var constants = {

        URL_BASE: "/cm/app/",
        MODE_EDIT: "edit",
        MODE_READONLY: "readonly",
        VIEW_EDIT_ASSET: "editAsset",
        VIEW_HOME: "home",
        VIEW_DASHBOARD: "dash",
        VIEW_DESIGN: "design",
        VIEW_EDITOR: "editor",
        VIEW_SITE_ARCH: "arch",
        VIEW_PUBLISH: "publish",
        VIEW_USERS: "users",
        VIEW_WORKFLOW: "workflow",
        VIEW_WIDGET_BUILDER:"widgetbuilder",
        PATH_TYPE_PAGE: "page",
        PATH_TYPE_ASSET: "asset",
        PATH_TYPE_UNKNOWN: "unknown",
        VIEW_EDIT_TEMPLATE: "editTemplate"
    };

    /**
     * Runs right after the navigator is defined.
     */
    function init()
    {
        $.perc_sessionTimeout();
        loadContextFromUrl();
        loadUserInfoFromCookie();
        if(warningMessage)
        {
            alert(warningMessage);
        }
    }

    /**
     * Invoked when an open action is triggered by the finder.
     * @param item {Object}
     */
    function onFinderItemOpen(item){
        var type = item['type'];
        switch (type)
        {
            case "site":
                handleOpenSite(item);
                break;

            case "percPage":
                handleOpenPage(item);
                break;

            case "Folder":
                handleOpenFolder(item);
                break;

            case "FSFolder":
                handleOpenFolder(item);
                break;

            default:
                if(item['category'] === 'ASSET') {
                    handleOpenAsset(item);
                }
        }
    }

    /**
     * Open the specified site.
     * @param item {Object}
     */
    function handleOpenSite(item){
        if(!(isAdmin || isDesigner))
        {
            changeLocation(item["name"],
                constants.VIEW_EDITOR, null, null, item["name"],item["path"], null, null);
            return;
        }
        var toView = constants.VIEW_SITE_ARCH;
        // If already on design view then just change site
        // and reload design view
        if(view === constants.VIEW_WORKFLOW)
            toView = constants.VIEW_WORKFLOW;

        if(view === constants.VIEW_DESIGN)
            toView = constants.VIEW_DESIGN;
        //If already on publish iew then just change site and reload publish view
        if(view === constants.VIEW_PUBLISH)
            toView = constants.VIEW_PUBLISH;
        changeLocation(item["id"],
            toView, mode, null, null, item["path"], null, null);
    }

    /**
     * Open the specified page in edit or readonly mode, defaults to readonly
     * mode unless the isEditMode flag is set to <code>true</code>.
     * @param item {Object}
     * @param isEditMode {boolean}
     */
    function handleOpenPage(item, isEditMode){
        var fwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-frame');
        if(fwrapper != null){
            $.PercViewReadyManager.showRenderingProgressWarning();
            return;
        }
        $.PercFolderHelper().getAccessLevelById(item["id"],false,function(status, result){
            if(status === $.PercFolderHelper().PERMISSION_ERROR)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});

            }
            else
            {
                if(isEditMode && result === $.PercFolderHelper().PERMISSION_READ)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.page.general@Warning"), content: I18N.message("perc.ui.navi.manager@No Permissions For Page") + item["name"] +"'."});
                }
                else
                {
                    var aSite = parseSiteFromPath(item['path']);
                    var aMode = isEditMode ? constants.MODE_EDIT : constants.MODE_READONLY;
                    changeLocation(aSite, constants.VIEW_EDITOR, aMode, item["id"], item["name"], item['path'], constants.PATH_TYPE_PAGE, null);
                }
            }
        });
    }

    /**
     * Open the specified folder
     * @param folder(Object)
     */
    function handleOpenFolder(pathItem){
        // first check that the path item is not FSFolder
        if(pathItem.type === "FSFolder")
        {
            // check that the path is allowed for renaming the folder
            var path = pathItem.path;
            var paths = path.split("/");

            // Disable the editable foldername if we are in the list view
            if($.Percussion.getCurrentFinderView() !== $.Percussion.PERC_FINDER_VIEW_COLUMN)
            {

            }
            else
            {
                // See if the selected folder is allowed to be editable
                // This is the JS array:
                // [0] = ""
                // [1] = "Design"
                // [2] = "Web Resources"
                // [3] = "themes"
                // [4] = ""
                if(paths.length > 5)
                {
                    $.perc_utils.makeFolderEditable(pathItem);
                }
                else
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.page.general@Warning"), content: I18N.message("perc.ui.navi.manager@System Folder")});

                }
            }
        }
//        //Simply return if the category is SYSTEM or SECTION_FOLDER
//        else if(pathItem.category == "SYSTEM")
//        {
//            //$.perc_utils.alert_dialog({title: 'Warning', content: "This is a system folder.  Its properties cannot be modified."});
//            return;
//        }
//        else if(pathItem.accessLevel != $.PercFolderHelper().PERMISSION_ADMIN)
//        {
//            //var type = pathItem.category == "SECTION_FOLDER"?"section":"folder";
//            //$.perc_utils.alert_dialog({title: 'Warning', content: "You do not have permission to modify the properties of this "+ type +"."});
//            return;
//        }
//        else if(pathItem.category == "SECTION_FOLDER")
//        {
//            //$.perc_utils.alert_dialog({title: 'Warning', content: "Use the navigation editor to change section properties."});
//            return;
//        }

        /********************************
         * Don't open the dialog
         if(pathItem.type != "FSFolder")
         {
            $.PercFolderPropertiesDialog().open(pathItem, function(status){
                //If the status is save refresh the finder to reflect the updated data.
                if(!status || status == "save")
                {
                    $.perc_finder().refresh();
                }
            });
        }
         */
    }

    /**
     * Open the specified asset in edit or readonly mode, defaults to readonly
     * mode unless the isEditMode flag is set to <code>true</code>.
     * @param item {Object}
     * @param isEditMode {boolean}
     */
    function handleOpenAsset(item, isEditMode){
        $.PercFolderHelper().getAccessLevelById(item["id"],false,function(status, result){
            if(status === $.PercFolderHelper().PERMISSION_ERROR)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});

            }
            else
            {
                if(isEditMode && result === $.PercFolderHelper().PERMISSION_READ)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.page.general@Warning"), content: I18N.message("perc.ui.navi.manager@No Permissions For Asset") + item["name"] +"'."});
                }
                else
                {
                    var aMode = isEditMode ? constants.MODE_EDIT : constants.MODE_READONLY;
                    changeLocation(null, constants.VIEW_EDIT_ASSET, aMode, item["id"], item["name"], item['path'], constants.PATH_TYPE_ASSET, null);
                }
            }
        });
    }



    /**
     * Helper function to create the url and cause the browser to change
     * its stuff. All location change listeners are notified and any one
     * of them can stop the change from occuring by passing back <code>false</code>.
     * @param aSite {String} site name, may be <code>null</code> or empty.
     * @param aView {String} the view, cannot be <code>null</code> or empty.
     * @param aMode {String} the mode, may be <code>null</code> or empty.
     * @param aId {String} object id, may be <code>null</code> or empty.
     * @param aName {String} name of object , may be <code>null</code> or empty.
     * @param aPath {String} the path, may be <code>null</code> or empty.
     * @param aPathType {String} the pathType, may be <code>null</code> or empty.
     * @param aMemento {Object} the memento object name, may be <code>null</code> or empty.
     */
    function changeLocation(aSite, aView, aMode, aId, aName, aPath, aPathType, aMemento)
    {
        var params = buildParams(aSite, aView, aMode, aId, aName, aPath, aPathType, aMemento);
        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
        $.PercQueuePostAJAX(function(){
            fireLocationChangeEvent(params);
        });

    }

    /**
     * Create a url from the passed in params.
     * @param params {Object} site name, may be <code>null</code> or empty.
     */
    function createUrl(params)
    {
        return $.param.querystring(constants.URL_BASE, params);
    }

    /**
     * Helper function to build param object needed for location change.
     * @param aSite {String} site name, may be <code>null</code> or empty.
     * @param aView {String} the view, cannot be <code>null</code> or empty.
     * @param aMode {String} the mode, may be <code>null</code> or empty.
     * @param aId {String} object id, may be <code>null</code> or empty.
     * @param aName {String} name of object , may be <code>null</code> or empty.
     * @param aPath {String} the path, may be <code>null</code> or empty.
     * @param aPathType {String} the pathType, may be <code>null</code> or empty.
     * @param aMemento {Object} the memento object name, may be <code>null</code> or empty.
     * Finds the type of the path from the supplied aPath param and adds it to the params as pathType.
     */
    function buildParams(aSite, aView, aMode, aId, aName, aPath, aPathType, aMemento)
    {

        var params = {view: aView};
        if(!isBlank(aSite))
            $.extend(params, {site: aSite});
        if(!isBlank(aMode))
            $.extend(params, {mode: aMode});
        if(!isBlank(aId))
            $.extend(params, {id: aId});
        if(!isBlank(aName))
            $.extend(params, {name: aName});
        if(!isBlank(aPath))
            $.extend(params, {path: aPath});
        if(debug)
            $.extend(params, {debug: "true"});
        if(autoTest)
            $.extend(params, {at: "true"});
        if(aMemento != null && typeof(aMemento) == 'object')
            $.extend(params, {memento: JSON.stringify(aMemento)});
        if(!isBlank(aPathType))
            $.extend(params, {pathType:aPathType});
        if(javascriptOff)
            $.extend(params, {disableJS:"true"});
        return params;
    }

    /**
     * Create a bookmark based on the current state of the nav manager.
     * @return a URL string that is a bookmark to the current page and state.
     * @type String
     */
    function createBookmark()
    {
        var loc = window.location;
        var buff = loc.protocol;
        buff += "//";
        buff += loc.host;
        var count = 0;
        for(var key in memento)
            ++count;
        var mem = count === 0 ? null : encodeURIComponent(memento);
        buff += createUrl(buildParams(site, view, mode, id, name, path, pathType, mem));
        return buff;
    }

    /**
     * Finds the type of the path for the supplied string.
     * If the string starts with "/Assets/" returns the type as constants.PATH_TYPE_ASSET, if it starts with "/Sites/"
     * then returns constants.PATH_TYPE_PAGE, for any thing else returns constants.PATH_TYPE_UNKNOWN.
     * @return <code>PATH_TYPE_ASSET</code> or <code>PATH_TYPE_PAGE</code> or <code>PATH_TYPE_UNKNOWN</code>
     * @type string
     */
    function findPathType(pathStr)
    {
        var pathType = constants.PATH_TYPE_UNKNOWN;
        if(pathStr) {
            if(pathStr.indexOf($.perc_paths.ASSETS_ROOT + "/")===0)
                pathType = constants.PATH_TYPE_ASSET;
            if(pathStr.indexOf($.perc_paths.SITES_ROOT + "/")===0)
                pathType = constants.PATH_TYPE_PAGE;
        }
        return pathType;
    }
    /**
     * Parses a valid finder site path, must start with /Sites/ or
     * will return an empty string.
     * @param {String} the site path to be parsed, assumed not <code>null</code>.
     * @return the site or an empty string.
     * @type String
     */
    function parseSiteFromPath(path)
    {
        var prefix = $.perc_paths.SITES_ROOT + "/";
        if(path.indexOf(prefix) === 0)
        {
            var pos = path.indexOf("/", prefix.length);
            if(pos === -1)
                pos = path.length;
            return path.substring(prefix.length, pos);
        }
        return "";
    }

    /**
     * Parses the contextual values from the querystring.
     */
    function loadContextFromUrl()
    {
        var querystring = $.deparam.querystring();
        site = querystring.site;
        view = querystring.view;
        mode = querystring.mode;
        path = querystring.path;
        id = querystring.id;
        name = querystring.name;
        debug = querystring.debug;
        autoTest = querystring.at;
        pathType = querystring.pathType;
        warningMessage = querystring.warningMessage;

        javascriptOff = (querystring.disableJS === "true" ? true : false);
        if(typeof(querystring.memento) != "undefined")
        {
            memento = JSON.parse(decodeURIComponent(querystring.memento));
        }
    }

    /**
     * Loads the user info from the cookie and sets it to a member variable.<b>
     */
    function loadUserInfoFromCookie()
    {
        userName = $.cookie("perc_userName");
        isAdmin = $.cookie("perc_isAdmin") === 'true' ? true : false;
        isDesigner = $.cookie("perc_isDesigner") === 'true' ? true : false;
        isAccessibilityUser = $.cookie("perc_isAccessibilityUser") === 'true' ? true : false;

        $.perc_utils.info("UserInfo", "userName: " + userName + ", isAdmin: " +  isAdmin + ", isDesigner: " +  isDesigner + ", isAccessibilityUser: " + isAccessibilityUser);
    }

    function doReloadIfRequred(url)
    {
        var toUrlParms = $.deparam.querystring(url);
        var current = $.deparam.querystring();

        if (current.view !== toUrlParms.view ||
             current.view === 'arch' ||
             current.path !== toUrlParms.path ||
             current.mode !== toUrlParms.mode ||
             current.id !== toUrlParms.id ||
             current.name !== toUrlParms.name ||
             current.debug !== toUrlParms.debug ||
             current.autoTest !== toUrlParms.autoTest ||
             current.pathType !== toUrlParms.pathType ||
             current.memento !== toUrlParms.memento)
        {
            window.location.href = url;
        }
        else {
            window.history.pushState("object or string", "Title", url);
            loadContextFromUrl();
            if (toUrlParms.view === "arch")
            {
                $("#perc_site_map").perc_site_map({
                    site: toUrlParms.site,
                    onChange: function () {
                        $.perc_finder().refresh();
                    }
                });
            }
            $.unblockUI();
        }

    }

    window.addEventListener('popstate', function(e) {
        // e.state is equal to the data-attribute of the last image we clicked
    });



    /**
     * Fires Location change event informing all registered listeners.
     * @param params {Object} params needed to create th url for location change.
     * change event.
     */
    function fireLocationChangeEvent(params)
    {
        var url = createUrl(params);
        var len = pageChangeListeners.length;
        if(len === 0)
        {
            doReloadIfRequred(url);
            return;
        }
        // Create a location change request
        var id = createUUID();
        locationChangeRequests[id] = {
            url: url,
            listenerTotal: len,
            continueFlag: true,
            listenersComplete: 0
        };
        // notify all listeners
        for(var i = 0; i < len; i++)
        {
            // Each listener gets passed a "hook"  that is called by the listener
            // when its has finished its own processing. The hook is told whether
            // it should continue. Once all listeners have called their hooks,
            // the actual location change will occur if all listeners passed back
            // <code>true</code> for <code>canContinue</code>.
            $.unblockUI();
            pageChangeListeners[i](url, id, locationChangeHook, params);

        }
    }

    /**
     * This function is passed to each location change listener
     * and must be called by that listener when it is complete with its own
     * processing. When all listeners have called there hooks and all
     * have passed back <code>true</code> for the canContinue value, then
     * the location change will occur.
     * @param id {String} the id of the location Change request, cannot
     * be <code>null</code> or empty.
     * @param canContinue {boolean} flag indicating that the location
     * change should be allowed to continue.
     */
    function locationChangeHook(id, canContinue)
    {
        var request = locationChangeRequests[id];
        request.continueFlag &= canContinue;
        ++request.listenersComplete;
        if(request.listenersComplete === request.listenerTotal)
        {
            if(request.continueFlag)
            {
                doReloadIfRequred(request.url);
            }
            else
            {
                // remove the request
                delete locationChangeRequests[id];
                $.unblockUI();
            }
        }
    }

    /**
     * Creates a unique id.
     */
    function createUUID() {
        // http://www.ietf.org/rfc/rfc4122.txt
        var s = [];
        var hexDigits = "0123456789ABCDEF";
        for (var i = 0; i < 32; i++) {
            s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
        }
        s[12] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
        s[16] = hexDigits.substr((s[16] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01

        var uuid = s.join("");
        return uuid;
    }

    /**
     * Utility function to determine if a string is blank (i.e. null or empty).
     * @param str {String} the string to evaluate.
     * @return <code>true</code> if the string is blank.
     * @type boolean
     */
    function isBlank(str){
        return (str == null || str.length === 0);
    }


    /* PUBLIC METHODS AND VARIABLES */
    var obj = {
        /**
         * Retrieve bookmark that contains the current state of the
         * navigator.
         * @return url string
         * @type string
         */
        getBookmark: createBookmark,

        /**
         * Retrieve the site name if currently set on the navigation mgr.
         * @return the mode, may be <code>null</code> or empty.
         * @type String
         */
        getSiteName: function(){
            return site;
        },
        /**
         * Set the site name.
         * @param newSiteName {string} the new name to be used.
         */
        setSiteName: function(newSiteName){
            site = newSiteName;
        },
        /**
         * Get the memento object. This object can have properties set on it
         * by an editor allowing it to save state information that will end up
         * in the url for bookmarking.
         * @return the memento object, never <code>null</code>.
         * @type Object
         */
        getMemento: function(){
            return memento;
        },
        /**
         * Clear the memento
         */
        clearMemento: function(){
            memento = {};
        },
        /**
         * Retrieve the view if currently set on the navigation mgr.
         * @return the view, may be <code>null</code> or empty.
         * @type String
         */
        getView: function(){
            return view;
        },
        /**
         * Retrieve the mode if currently set on the navigation mgr.
         * @return the mode, may be <code>null</code> or empty.
         * @type String
         */
        getMode: function(){
            return mode;
        },
        /**
         * Set the mode of the navigation mgr.
         * @param aMode {String} the mode to be set, may be <code>null</code> or
         * empty.
         */
        setMode: function(aMode)
        {
            mode = aMode;
        },
        /**
         * Set the mode of the navigation mgr.
         * @param aMode {String} the mode to be set, may be <code>null</code> or
         * empty.
         */
        clearId: function()
        {
            id = "";
        },
        /**
         * Retrieve the id if currently set on the navigation mgr.
         * @return the mode, may be <code>null</code> or empty.
         * @type String
         */
        getId: function(){
            return id;
        },
        /**
         * Retrieve the name if currently set on the navigation mgr.
         * @return the mode, may be <code>null</code> or empty.
         * @type String
         */
        getName: function(){
            return name;
        },
        /**
         * Set the name value for the curent item.
         * @param newName {string} the new name to be used.
         */
        setName: function(newName)
        {
            name = newName;
        },
        /**
         * Retrieve the path if currently set on the navigation mgr.
         * @return the mode, may be <code>null</code> or empty.
         * @type String
         */
        getPath: function(){
            return path;
        },

        /**
         * Sets the path value for the current item.
         * @param newPath {string} the new path to be used.
         */
        setPath: function(newPath){
            path = newPath;
        },

        /**
         * Return the name of the current user.
         * @return the current user, may be empty.
         * @type String
         */
        getUserName: function(){
            return userName;
        },
        /**
         * Flag indicating if in autoTest mode.
         * @return <code>true</code> if in debug mode.
         * @type boolean
         */
        isAutoTest: function(){
            return autoTest;
        },
        /**
         * Flag indicating if in debug mode.
         * @return <code>true</code> if in debug mode.
         * @type boolean
         */
        isDebug: function(){
            return debug;
        },
        /**
         * Flag indicating that the current user is admin.
         * @return <code>true</code> if current user is in admin role.
         * @type boolean
         */
        isAdmin: function(){
            return isAdmin;
        },
        /**
         * Flag indicating that the current user is a designer.
         * @return <code>true</code> if current user is in the Designer role.
         * @type boolean
         */
        isDesigner: function(){
            return isDesigner;
        },
        /**
         * Flag indicating that the current user is an accessibility user.
         * @return <code>true</code> if current user is in accessibility user.
         * @type boolean
         */
        isAccessibilityUser: function(){
            return isAccessibilityUser;
        },
        /**
         * Flag indicating that javascript should not be processed by the model
         * when opening the template editor or page editor.
         * @return <code>true</code> if javascript should be disabled
         * @type boolean
         */
        isJavascriptOff: function(){
            return javascriptOff;
        },
        /**
         * Is page/asset reopen allowed flag.
         */
        isReopenAllowed: function(){
            return allowReopen;
        },
        /**
         * Set the flag that indicates if a page/asset can be reopened.
         * Caution this should only be used for very special cases.
         */
        setReopenAllowed: function(allowed){
            allowReopen = allowed;
        },
        /**
         * Returns the pathType, if not defined tries to find from the path.
         * @see #findPathType for the details.
         * @return type of the path.
         * @type string
         */
        getPathType: function() {
            if(pathType)
                return pathType;
            return findPathType(path);
        },
        /**
         * Change location based on specified view. Uses values in current navigator
         * state to populate the URL and can be restricted to only use the view/site
         * values.
         * @param aView {String} the view to be moved to.
         * @param siteViewPathOnly {boolean} flag indicating that the only params to
         * be sent will be view, path and site only.
         */
        goTo: function(aView, siteViewPathOnly)
        {
            if(siteViewPathOnly)
            {
                changeLocation(site, aView, null, null, null, path, null, null);
            }
            else
            {
                changeLocation(site, aView, null, id, name, path, pathType, null);
            }
        },
        /**
         * Convenience method to change location to dashboard.
         */
        goToDashboard: function()
        {
            obj.goTo(constants.VIEW_DASHBOARD, true);
        },
        /**
         * Changes location based on specified parameters, this gives more control
         * the the <code>goTo</code>.
         * @param aView {String} the view, cannot be <code>null</code> or empty.
         * @param aSite {String} site name, may be <code>null</code> or empty.
         * @param aMode {String} the mode, may be <code>null</code> or empty.
         * @param aId {String} object id, may be <code>null</code> or empty.
         * @param aName {String} name of object , may be <code>null</code> or empty.
         * @param aPath {String} the path, may be <code>null</code> or empty.
         * @param aMemento {object} memento object, may be <code>null</code>.
         */
        goToLocation: function(aView, aSite, aMode, aId, aName, aPath, aPathType, aMemento){
            changeLocation(aSite, aView, aMode, aId, aName, aPath, aPathType, aMemento);
        },

        /**
         * Public reference to handleOpenAsset
         */
        handleOpenAsset: handleOpenAsset,

        /**
         * Public reference to handleOpenPage
         */
        handleOpenPage: handleOpenPage,

        /**
         * Public reference to handleOpenPage
         */

        parseSiteFromPath: parseSiteFromPath,

        /**
         * Register the finder with the navigation manager.
         * @param finder {Object} the finder to be registered, cannot be <code>null</code>.
         */
        registerFinder: function(finder){
            finderRef = finder;
            finder.addOpenListener(onFinderItemOpen);
            finder.addPathChangedListener(function(p){
                if(finder.finderOpenInProgress)
                    return;
                var temp = p.slice(1);
                var pt = "/" + temp.join("/");
                path = pt;
            });
            var initialPath = path;
            // if there is no current path then set it to default of /Sites
            path = path ? path : "/Sites";
            if(path)
            {
                finderRef.open(path.split("/"), function(){
                    path = initialPath;
                });
            }
        },

        /**
         * Adds an location change listener to the navigator to be notified when the current
         * page is about to be or has been changed.
         * @param listener {Function} the open listener callback function that
         * will be called when an location change event occurs. Cannot be <code>null</code>.
         * <pre>
         *     The listener callback will be passed the following args:
         *        url {String} the url of the page to be loaded.
         *        id {String} the id for the location change request.
         *        notifyComplete {function} this function MUST be called by the listener
         *           when it is done with its own processing. It takes the location change
         *           request id and a boolean flag indicting that the location change should
         *           continue.
         *
         *            Example location change listener:
         *            ---------------------------------
         *
         *            function locChangeLister(url, id, notifyComplete, params)
         *            {
         *               call_confirm_dialog(
         *                 function(){notifyComplete(id, true)}, // Callback when YES
         *                 funtion(){notifyComplete(id, false)}  // Callback when NO
         *               );
         *            }
         *         params {Object} object with parameter properties used to create the url.
         *    The listener callback should return <code>true</code> if
         *    the event should be allowed to happen. Return
         *    <code>false</code> to prevent the page change.
         * </pre>
         */
        addLocationChangeListener: function(listener)
        {
            if($.inArray(listener, pageChangeListeners) === -1)
            {
                pageChangeListeners.push(listener);
            }
        },

        /**
         * Removes the specified location change listener if it exists.
         * @param listener {Function} the open listener callback function to be
         * removed. Cannot be <code>null</code>.
         */
        removeLocationChangeListener: function(listener)
        {
            if($.inArray(listener, openListeners) > -1)
            {
                var len = pageChangeListeners.length;
                for(var i = 0; i < len; i++)
                {
                    if(pageChangeListeners[i] === listener)
                    {
                        pageChangeListeners.splice(i, 1);
                        return;
                    }
                }
            }
        },

        /**
         * Open the item for a given path. The path can be of folder, page or asset.
         * @param path(String) The finder path for a given page.
         */
        openPathItem: function(path)
        {
            $.PercPathService.getPathItemForPath(path, function(status, data){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    data.PathItem.path = path;
                    onFinderItemOpen(data.PathItem);
                }
                else
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});

                }
            });

        },

        /**
         * Opens the page for the given path. Calls the $.PercPathService.getPathItemForPath to get the PathItem of the
         * given path. If there is an error getting the path, displays the error message otherwise passes the PathItem to
         * this.handleOpenPage to open the page.
         * @param path(String) The finder path for a given page.
         */
        openPage: function(path, isEditMode){
            $.PercPathService.getPathItemForPath(path, function(status, data){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    data.PathItem.path = path;
                    handleOpenPage(data.PathItem, isEditMode);
                }
                else
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});

                }
            });
        },

        openAsset : function(assetObj) {
            $.PercPathService.getPathItemById(assetObj.id, function(status, data){
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    data.PathItem.path = assetObj.path;
                    handleOpenAsset(data.PathItem, false);
                } else {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});

                }
            });
        },

        openFolderDialog: function(path){
            $.PercPathService.getPathItemForPath(path, function(status, data){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    data.PathItem.path = path;
                    handleOpenFolder(data.PathItem);
                }
                else
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});

                }
            });
        },

        loadSiteProperties: function(siteName, callback){
            $.PercSiteService.getSiteProperties(siteName, function(status, result){
                if (status === $.PercServiceUtils.STATUS_SUCCESS) {
                    siteProperties = result.SiteProperties;
                    if (typeof(callback) == "function")
                        callback(result.SiteProperties);
                }
            });
        },

        getSiteProperties: function(){
            return siteProperties;
        },

        setTemplateModel: function (model)
        {
            templateModel = model;
        },

        getTemplateModel: function (model)
        {
            return templateModel;
        }
    };

    // Merge the public methods/fields into the navigator object
    $.extend($.PercNavigationManager, obj);
    // Merge the constants to Publically expose them.
    $.extend($.PercNavigationManager, constants);
    // Invoke the init method
    init();

})(jQuery);
