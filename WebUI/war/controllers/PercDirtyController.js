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

(function($)
{
    /** 
     * Singleton to keep track of dirty state of page, template and asset
     * 
     * Use: create reference to this singleton like so:
     *      var dirtyController = $.PercDirtyController;
     * 
     * Update state like so:
     *      dirtyController.setDirty(true, "page");
     * 
     * Query like:
     *      if(dirtyController.isDirty()) { ... }
     * 
     * Pop up a confirmation box:
     *      dirtyController.confirmIfDirty(function(), function(), options)
     * 
     * @author Jose Annunziato
     */
    $.PercDirtyController = {
        
        dirtyState       : false,
        dirtyObjectType  : "object",     // name of the resource that is dirty
        utils            : $.perc_utils, // to display the dialog
        saveCallback     : function(){}, // callback function to save user changes before calling success callback.
        
        navigationEvent : function() {
            if(this.dirtyState){
                this.setDirty(false);
                return "This "+this.dirtyObjectType+" contains unsaved changes.\nIf you continue, all changes will be lost.\nClick OK to proceed and discard changes,\nCancel to return to the "+this.dirtyObjectType+" you were editing.";

            }else{
                return null;
            }

        },
        
        /**
         *  Updates the dirty state and the name of the resource that is dirty
         *  param: saveCallback. A callback function is passed as a parameter. This function will be called when 
         *  using option "Save" from warning dialog. After this function gets called, 
         *  original success callback function will be executed.
         */        
        setDirty : function(isDirty, objectType, saveFunction) {
        	
            this.dirtyState      = isDirty;
            this.dirtyObjectType = objectType;
            if(saveFunction === undefined){
            	this.saveCallback =function(){};
            }else{
            	this.saveCallback = saveFunction;
            }
        },
        
        /**
         *  To query dirty state
         */
        isDirty : function() {
            return this.dirtyState;
        },

        /**
         *  Displays a confirmation dialog with the name of the dirty resource
         *  If it's not dirty, it calls callback, otherwise displays confirmation dialog
         */        
        confirmIfDirty : function ( callback, errorCallback, options )
        {
            if( this.dirtyState )
            {
                var settings = this.setConfirmDialogOptions(callback, errorCallback, options);
                this.utils.confirm_dialog(settings);
            }
            else
            {
                //Object is not dirty, proceed
                callback();
            }
        },
        
        /**
         *  Sets the options for the dialog, depending if save option is available or not.
         *  The options include, among others, title, type (defines which buttons it'll have) and callbacks for buttons.
         */
        setConfirmDialogOptions : function ( callback, errorCallback, options )
        {
            var objectType = this.dirtyObjectType;
            errorCallback = errorCallback || function(){};
            var self = this;
            //Basic settings common to every type of dirty warning dialog.
            var settings = {
                id: 'perc-editor-page-dirty',
                title: I18N.message("perc.ui.navigatedialog.confirm@Unsaved Edits"),
                success: function() { self.setDirty(false); callback(); },
                cancel: errorCallback,
                yes: "Continue anyway"
            };
            
            //Settings for dialog with save option.
            if(self.isSaveOptionAllowed()) {
                settings.dontSaveCallback = options && options.dontSaveCallback ? function() { self.setDirty(false); options.dontSaveCallback(); } : settings.success;
                //If a page editor or a template editor are dirty, then user can Save before leaving the tab.
                settings.type = "SAVE_BEFORE_CONTINUE";
                settings.question = "You will lose your edits if you do not save. Save your edits before leaving this tab?";
                settings.width = "356px";
                //The function to call when Save button is pressed.
                //This function is set by the element that marked itself as dirty using setDirty function.
                settings.save = self.saveCallback || function(){};
            }
            else {
                //Settings for each other case (e.g. Workflow, Publish, etc), save option is not available. User can only cancel or continue.    
                settings.type = "CANCEL_CONTINUE";
                settings.question = I18N.message("perc.ui.navigatedialog.confirm@This page contains unsaved edits");
                settings.width = "326px";
            }
            
            //Initialize the options parameter if it is undefined, and then extend basic settings with the ones in options parameter.
            options = options || {};
            $.extend( settings, options );
            return settings;
        },
        
        /**
         *  Determines if the user will be able to save his changes and continue from the warning dialog.
         *  The save option is available when Page or Template Editor are dirty, for Layout and CSS tabs.
         */
        isSaveOptionAllowed : function () {
            var view = $.PercNavigationManager.getView();
            return (view == $.PercNavigationManager.VIEW_EDIT_TEMPLATE ||
                    view == $.PercNavigationManager.VIEW_EDITOR || 
                    view == $.PercNavigationManager.VIEW_WIDGET_BUILDER);  
        }
    };
})(jQuery);
