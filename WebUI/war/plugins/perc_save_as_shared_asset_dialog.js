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
 * perc_save_as_shared_asset_dialog
 *
 * Show a dialog to save a widget's local content as an asset.
 *
 * Depends on: PercFinderTree plugin
 */
(function($) {
    // Public API
    $.perc_save_as_shared_asset_dialog = {
        'createDialog' : createDialog
    };
    
    // Private variables
    var dialog;
    
    /**
     * Creates the dialog
     * @param widgetId the widget that will receive the new asset as content.
     * @param onSave callback function invoked before removing the dialog after a successful save.
     * @return dialog element
     */
    function createDialog( dialogOptions )
    {
        var buttons = {
            'Save' : {
                'id' : 'perc-saveas-shared-asset-dialog-save',
                'click' : function() {
                    assignNewAssetToWidget( dialogOptions );
                }
            },
            'Cancel' : {
                'id' : 'perc-saveas-shared-asset-dialog-cancel',
                'click' : function() {
                    $( this ).remove();
                }
            }
        };

        var dialog_content ='<div id="perc-saveas-shared-asset">';
        dialog_content +=       '<div class="perc-saveas-shared-asset-label-denotes-required"><label>' +I18N.message("perc.ui.general@Denotes Required Field") + '</label></div>';
        dialog_content +=       '<form action="">';
        dialog_content +=           '<div>';
        dialog_content +=               '<label for="perc-saveas-shared-asset-dialog-name" class="perc-required-field">' + I18N.message( 'perc.ui.saveassharedassetdialog.label@Name:' ) + '</label>';
        dialog_content +=               '<input type="text" name="perc-saveas-shared-asset-dialog-name" id="perc-saveas-shared-asset-dialog-name" maxlength="255" /><br/>';
        dialog_content +=               '<label id="perc-saveas-shared-asset-dialog-name-error" class="perc-saveas-shared-asset-dialog-error perc_field_error"></label>';
        dialog_content +=           '</div>';
        dialog_content +=           '<div>';
        dialog_content +=               '<label id="perc-saveas-shared-asset-dialog-path-label" for="perc-saveas-shared-asset-dialog-path" class="perc-required-field">' + I18N.message( 'perc.ui.saveassharedassetdialog.label@Where:' ) + '</label><br/>';
        dialog_content +=               '<input type="hidden" name="perc-saveas-shared-asset-dialog-path" id="perc-saveas-shared-asset-dialog-path" />';
        dialog_content +=               '<div id="perc-saveas-shared-asset-dialog-path-tree"></div><br/>';
        dialog_content +=               '<label id="perc-saveas-shared-asset-dialog-where-error" class="perc-saveas-shared-asset-dialog-error perc_field_error"></label>';
        dialog_content +=           '</div>';
        dialog_content +=       '</form>';
        dialog_content +=   '</div>';
        
        dialog = $(dialog_content).perc_dialog({
            title: I18N.message( 'perc.ui.saveassharedassetdialog.title@Save As Shared Asset' ),
            id: 'perc-saveas-shared-asset-dialog',
            width: 686,
            resizable: false,
            modal: true,
            percButtons: buttons
        });
        
        // Apply the PercFinderTree for the vertial tree
        dialog.find( '#perc-saveas-shared-asset-dialog-path-tree' ).PercFinderTree({
            rootPath: $.PercFinderTreeConstants.ROOT_PATH_ASSETS,
            width: '628px',
            classNames: {
                container : 'perc-path-selector-container',
                selected : 'perc-path-selected-item'
            },
            onClick : function( path ) {
                // Whenever a tree element is clicked, change where the asset will be created
                setSection( path );
            }
        });
        
        // Prevent submit (default) behavior of the form element when hitting enter key
        dialog.find( 'form' ).on("submit", function () {
            return false;
        });

        // Filter invaid characters in Name field
        $.perc_filterField(
            dialog.find( '#perc-saveas-shared-asset-dialog-name' ),
            $.perc_textFilters.URL
        );
    }

    /**
     * Whenever a tree element is clicked, change where the asset will be created
     * @param path
     */
    function setSection( path ) {
        var pathVal = '';
        
        if ( path.category === 'ASSET' ) {
            pathVal = path.folderPaths;
        }
        else {
            pathVal = path.folderPath;
        }
        
        // Correct the path value and set the corresponding input and label
        pathVal = $.PercFinderTreeConstants.convertFolderPathToPath( pathVal );
        $( '#perc-saveas-shared-asset-dialog-path' ).val( pathVal );
        $( '#perc-saveas-shared-asset-dialog-path-label' ).html( I18N.message( 'perc.ui.saveassharedassetdialog.label@Where:' ) + ' ' + pathVal );
    }

    /**
     * Display an error message for a given field.
     * @param String labelSelector the corresponding selector for the label
     * @param String message
     */
    function displayError( labelSelector, message ) {
        var errorLabel = dialog.find( labelSelector );
        // Error labels are hidden by default
        errorLabel.html( message ).show();
        $.unblockUI();
    }
    
    /**
     * Hides all the error messages.
     */
    function hideError() {
        var errorLabel = dialog.find( '.perc-saveas-shared-asset-dialog-error' );
        errorLabel.hide();
    }

    /**
     * Performs the corresponding validations and replaces the local content of the widget with
     * the new asset.
     * @param dialogOptions Object
     */
    function assignNewAssetToWidget( dialogOptions )
    {
        var assetName = dialog.find( '#perc-saveas-shared-asset-dialog-name' ).val().trim( );
        var selectedPath =  dialog.find( '#perc-saveas-shared-asset-dialog-path' ).val().trim( );
        var errorLabelsSelectors = {
            'name': '#perc-saveas-shared-asset-dialog-name-error',
            'where': '#perc-saveas-shared-asset-dialog-where-error'
        };
        
        /**
         * Validates that the required fields were complete.
         * @param okCallback function if validation passed
         */
        function validateRequiredFieldsCompleted(okCallback) {
            var assetNameNull = assetName === null || assetName === '';
            var selectedPathNull = selectedPath === null || selectedPath === '';
            
            if ( assetNameNull ) {
                displayError(
                    errorLabelsSelectors.name,
                    I18N.message( 'perc.ui.saveassharedassetdialog.errormessage@Asset Name is required.' )
                );
            }
            if ( selectedPathNull ) {
                displayError(
                    errorLabelsSelectors.where,
                    I18N.message( 'perc.ui.saveassharedassetdialog.errormessage@Selected Path is required.' )
                );
            }
            
            if ( !assetNameNull && !selectedPathNull ) {
                okCallback();
            }
        }
        
        /**
         * Validates that the current user can write or administer the selecter folder.
         * @param okCallback function if validation passed
         */
        function validatePathAccessLevel( okCallback ) {
            // User should not create assets in 'Assets' root node
            if ( selectedPath === $.perc_paths.ASSETS_ROOT ) {
                displayError(
                    errorLabelsSelectors.where,
                    I18N.message( 'perc.ui.saveassharedassetdialog.errormessage@You do not have permission to create an asset here.' )
                );
                return;
            }

            $.PercFolderHelper().getAccessLevelByPath(
                selectedPath,
                true,
                function( status, result ) {
                    var error = status === $.PercFolderHelper().PERMISSION_ERROR,
                        onlyWrite = result === $.PercFolderHelper().PERMISSION_READ;
                    if ( error || onlyWrite ) {
                        displayError(
                            errorLabelsSelectors.where,
                            I18N.message( 'perc.ui.saveassharedassetdialog.errormessage@You do not have permission to create an asset here.' )
                        );
                    }
                    else {
                        okCallback();
                    }
                }
            );
        }
        
        /**
         * Validates that the current user can create assets in the target folder, according to the
         * target folder workflow.
         * @param okCallback function if validation passed
         */
        function validatePathWorkflow( okCallback ) {
            $.PercUserService.getAccessLevel(
                null,
                -1,
                function( status, result) {
                    var error = status === $.PercServiceUtils.STATUS_ERROR,
                        accessRead = result === $.PercUserService.ACCESS_READ,
                        accessNone = result === $.PercUserService.ACCESS_NONE;
                    if (  error || accessRead || accessNone ) {
                        displayError(
                            errorLabelsSelectors.where,
                            I18N.message( 'perc.ui.saveassharedassetdialog.errormessage@You are not authorized to create a new asset.' )
                        );
                    }
                    else {
                        okCallback();
                    }
                },
                selectedPath
            );
        }
        
        /**
         * Validates that the current asset doesn't exist in the target folder.
         * @param okCallback function if validation passed
         */
        function validatePathNotExists( okCallback ) {
            $.PercPathService.getLastExistingPath(selectedPath + '/' + assetName,
                function( status, result ) {
                    if ( result === selectedPath.replace($.perc_paths.ASSETS_ROOT + '/', '') + '/' + assetName ) {
                        displayError(
                            errorLabelsSelectors.name,
                            I18N.message( 'perc.ui.saveassharedassetdialog.errormessage@An asset with the same name already exists.' )
                        );
                    }
                    else {
                        okCallback();
                    }
            });
        }
        
        /**
         * Invokes the corresponding service to create the new asset.
         * @param okCallback function if validation passed
         */
        function createNewAsset( okCallback ) {
            // When creating content for the first time, then assetId is not set until the content
            // editor is refreshed. This may take some time, and that is why we get the value here
            if ( dialogOptions.assetId === null || dialogOptions.assetId === '' ) {
                dialogOptions.assetId = $( '#frame' ).contents()
                    .find( 'div.perc-widget[widgetid=' + dialogOptions.widgetData.widgetid + ']' )
                    .attr('assetid');
            }
            
            $.PercAssetService.asset_from_local_content(
                dialogOptions.assetId,
                dialogOptions.widgetData,
                dialogOptions.parentId,
                assetName,
                selectedPath,
                function( status, result ) {
                    if ( status !== $.PercServiceUtils.STATUS_SUCCESS ) {
                        displayError( result );
                    }
                    else {
                        okCallback(result);
                    }
                }
            );
        }
        
        // Hide all error messages. May be needed if resubmitting the form. Block the UI and
        // invoke the corresponding validations before the asset creation order matters.
        hideError();
        $.PercBlockUI();
        validateRequiredFieldsCompleted( function() {
            validatePathNotExists( function() {
                validatePathAccessLevel( function() {
                    validatePathWorkflow( function () {
                        createNewAsset( function(result) {
                            // Excecute the onSave callback passed to the dialog, unblockUI and close
                            dialogOptions.onSave(result);
                            $.unblockUI();
                            dialog.remove();
                        });
                    });
                });
            });
        });
    }

})(jQuery);
