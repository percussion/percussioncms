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

/**
 * PercCategoryController.js
 *
 *
 */
var globalVar;

(function($) {

    // interface
    $.PercCategoryController = {
        init                   	: init,
        lockUser				: "x",
        getCategories			: getCategories,
        editCategories			: editCategories,
        getTabLockData			: getTabLockData,
        lockCategoryTab			: lockCategoryTab,
        removeCatTabLock		: removeCatTabLock,
        confirmDialog			: confirmDialog,
        publishToDTS			: publishToDTS
    };

    var dirtyController = $.PercDirtyController;
    var categoryService     = $.PercCategoryService;

    // local variables
    var view;
    var sitename;
    // Build an absolute URL as a workaround for IE issue (popup security warning)
    // See details at http://support.microsoft.com/kb/925014/en-us?fr=1
    var baseUrl = window.location.protocol + "//" + window.location.host + "/cm";

    /**
     * When the view is first instantiated, it gets a refrence to this controller
     * and passes a reference of itself to this controller.
     * The controller can then issue commands to update the view.
     */
    function init(vew) {

        // the view this controller is controlling
        view = vew;

        // put the view in an initial state
        view.init();
    }

    function getCategories(sitename, callback) {

        // Use the service to get the categories
        categoryService.getCategories(sitename, function(status, categoryJson) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.errorDialog(I18N.message( "perc.ui.category.controller@Error loading categories" ) , categoryJson);
                return;
            }

            view.getCategories(categoryJson);

            if(callback)
                callback();
        })
    }

    function editCategories(catArray, sitename, success, error) {

    	// check if the lock is overridden by any other user or not?
    	getTabLockData(function(lockinfo){
    		var user = lockinfo.userName;

    		if(typeof user !== 'undefined' &&  user !== null  && user !== "" && typeof $.PercNavigationManager.getUserName() != 'undefined' && user !== $.PercNavigationManager.getUserName()) {
    			confirmDialog(I18N.message( "perc.ui.category.controller@Category tab is locked" ), user + " " + I18N.message( "perc.ui.category.controller@User working on categories" ), function(action){

    				if(action == "cancel") {
    					window.location.reload();
    				} else {
    					saveCategories(catArray, sitename, success, error);
    				}
    			});
    		} else {
    			saveCategories(catArray, sitename, success, error);
    		}
    	});
    }

    function saveCategories(catArray, sitename, success, error) {

    	var catJson = getCategoryJson(catArray);

        categoryService.editCategories(catJson, sitename, function(status, categoryJson) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message( "perc.ui.category.controller@Error" ), categoryJson);
                typeof error === 'function' && error();
                return;
            }

            dirtyController.setDirty(false);

            lockCategoryTab();
            // get the new category xml data and give it to the view to render
            getCategories(sitename);
            typeof success === 'function' && success();
        });
    }

    function getCategoryJson(catArray) {

    	if(catArray === null) {
            view.alertDialog(I18N.message( "perc.ui.category.controller@Error" ), I18N.message( "perc.ui.category.controller@Categories cannot be null" ));
            return;
        }

        //catJson = {"CategoryTree":{"title": I18N.message( "perc.ui.category.controller@Categories" ), "topLevelNodes": catArray}};
        catJson = {"title": I18N.message( "perc.ui.category.controller@Categories" ), "topLevelNodes": catArray};
		return catJson;

    }

    function getTabLockData(callback) {

    	categoryService.getTabLockData(function(status, lockinfo) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message( "perc.ui.category.controller@Error" ), lockinfo);
                return;
            } else {
            	callback(lockinfo);
            }

            dirtyController.setDirty(false);
    	});
    }

    function lockCategoryTab() {
    	// create the file containing the lock info.

    	categoryService.lockCategoryTab(function(status, lockinfo) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message( "perc.ui.category.controller@Error" ), lockinfo);
                return;
            }

            dirtyController.setDirty(false);
        });

    	getCategories(sitename);
    }

    function removeCatTabLock(callback) {

    	categoryService.removeCatTabLock(function(status) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message( "perc.ui.category.controller@Error" ), I18N.message( "perc.ui.category.controller@Problem Removing Category Tab Lock" ));
                return;
            }

            dirtyController.setDirty(false);
        });
    }

    function confirmDialog(title, message, callback) {
        var w = 400;
        $.perc_utils.confirm_dialog({
            title: title,
            question: message,
            width: w,
            success: function()
            {
                lockCategoryTab();
                view.init();
                getCategories(sitename);
                callback("success");
            },
            cancel: function()
            {
                callback("cancel");
            }
        });
    }

    function publishToDTS(catArray, deliveryServer, sitename) {
        categoryService.publishToDTS(deliveryServer, sitename, function(status, message) {
            if(status != $.PercServiceUtils.STATUS_SUCCESS) {
                view.alertDialog(I18N.message( "perc.ui.category.controller@Publish Error" ) , I18N.message( "perc.ui.category.controller@Error Publishing to DTS" ) + " " + message);
                return;
            }

            dirtyController.setDirty(false);
        });
    }
})(jQuery);
