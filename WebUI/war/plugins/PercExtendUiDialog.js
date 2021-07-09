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
 * [PercExtendUiDialog]
 *
 * Extends the jQuery UI Dialog widget to add special Percussion styled
 * buttons.
 */
(function($)
{
    // Get references to "Super Class" methods so we can call them later
    var _createButtons = $.ui.dialog.prototype._createButtons;
    var _setData = $.ui.dialog.prototype._setData;

    /**
     * Override the "Super Class's" _createButtons method
     * @param buttons
     */
    $.ui.dialog.prototype._createButtons = function(buttons)
    {
        var self = this;
        _createButtons.apply(this, [buttons]);
        if (typeof (this.options.percButtons) != 'undefined' && this.options.percButtons != null)
            self._createPercButtons(this.options.percButtons);
    };

    /**
     * Override the "Super Class's" _setData method
     * @param key
     * @param value
     */
    $.ui.dialog.prototype._setData = function(key, value)
    {
        var self = this;
        if (key === 'perc-buttons')
            self._createPercButtons(value);
        _setData.apply(this, [key, value]);
    };

    /**
     * Custom method to create special Percussion styled buttons.
     * @param percButtons
     */
    $.ui.dialog.prototype._createPercButtons = function(percButtons)
    {
        var self = this;
        var hasButtons = false;
        // Build an absolute URL as a workaround for IE issue (popup security warning)
        // See details at http://support.microsoft.com/kb/925014/en-us?fr=1
        var baseUrl = window.location.protocol + "//" + window.location.host + "/cm";
        uiDialogButtonPane = $('<div></div>').addClass(
            'ui-dialog-buttonpane ' + 'ui-widget-content ' + 'ui-helper-clearfix');
        // Define buttons below in the mapping object
        // Make the URL absolute for each background image
        var mapping = {
            "Ok" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonOk.png)',
                // bgimageOver : 'url(' + baseUrl + '/images/images/buttonOkOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-ok',
                txt : I18N.message("perc.ui.extend.ui.dialog@Ok Button")
                // display : 'block'
            },
            "Override" : {
                // bgimage : 'url(' + baseUrl + '/images/images/buttonOverride.png)',
                // bgimageOver : 'url(' + baseUrl + '/images/images/buttonOverrideOver.png)',
                // width : '126px',
                // height : '29px',
                cls : 'btn btn-primary perc-ok',
                txt : I18N.message("perc.ui.extend.ui.dialog@Override Button")
                // display : 'block'
            },
            "Move" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonMove.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonMoveOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.extend.ui.dialog@Move Button")
                //display : 'block'
            },
            "Select" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonSelect.png)',
                // bgimageOver : 'url(' + baseUrl + '/images/images/buttonSelectOver.png)',
                //width : '100px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.extend.ui.dialog@Select Button")
                //display : 'block'
            },
            "Yes" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonYes.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonYesOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary',
                txt : I18N.message("perc.ui.content.viewer@Yes Button")
                //display : 'block'
            },
            "Yes Preferred" : {
                //bgimage : 'url(' + baseUrl + '/images/images/ButtonYesBlue.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/ButtonYesBlueOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.content.viewer@Yes Button")
                //display : 'block'
            },
            "Yes Silver" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonYesNoFocus.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonYesOverNoFocus.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.content.viewer@Yes Button")
                //display : 'block'
            },
            "Continue" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonContinue.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonContinueOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.extend.ui.dialog@Continue Button")
                //display : 'block'
            },
            "Continue Preferred" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonContinueBlue.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonContinueBlueOver.png)',
                //width : '126px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.extend.ui.dialog@Continue Button")
                //display : 'block'
            },
            "New Folder" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonSave.gif)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonSaveOver.gif)',
                //width : 'auto',
                //height : '22px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.common.label@Save")
                //display : 'block'
            },
            "Save" : {
                // bgimage : 'url(' + baseUrl + '/images/images/buttonSave.png)',
                // bgimageOver : 'url(' + baseUrl + '/images/images/buttonSaveOver.png)',
                // width : '78px',
                // height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.common.label@Save"),
                //display : 'block'
            },
            "Don't Save" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonDontSave.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonDontSaveOver.png)',
                //width : '88px',
                //height : '29px',
                cls : 'btn btn-primary perc-dont-save',
                txt : I18N.message("perc.ui.extend.ui.dialog@Dont Save Button")
                // display : 'block'
            },
            "Cancel" : {
                //  bgimage : 'url(' + baseUrl + '/images/images/buttonCancel.gif)',
                //  bgimageOver : 'url(' + baseUrl + '/images/images/buttonCancelOver.gif)',
                //  width : '78px',
                //  height : '29px',
                cls : 'btn btn-primary perc-cancel',
                txt : I18N.message("perc.ui.assign.workflow@Cancel"),

                // display : 'block'
            },
            "Cancel Blue" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonCancelBlue.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonCancelBlueOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-cancel',
                txt : I18N.message("perc.ui.assign.workflow@Cancel")
                //display : 'block'
            },
            "No" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonNo.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonNoOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary',
                txt : I18N.message("perc.ui.extend.ui.dialog@No Button")
                //display : 'block'
            },
            "No Silver" : {
                //bgimage : 'url(' + baseUrl + '/images/images/ButtonNoSilver.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/ButtonNoSilverOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-cancel',
                txt : I18N.message("perc.ui.extend.ui.dialog@No Button")
                //display : 'block'
            },
            "Next" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonNext.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonNextOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.common.label@Next")
                //display : 'block'
            },
            "Back" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonBack.gif)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonBack.gif)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.common.label@Back")
                //display : 'none'
            },
            "Finish" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonFinish.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonFinishOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-save',
                txt : I18N.message("perc.ui.common.label@Finish")
                //display : 'none'
            },
            "Close" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonClose.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonCloseOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-close',
                txt : I18N.message("perc.ui.change.pw@Close")
                //display : 'block'
            },
            "Close Normal" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonCloseNormal.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonCloseNormalOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-close-normal',
                txt : I18N.message("perc.ui.change.pw@Close")
                //display : 'block'
            },
            "Done" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonDone.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonDoneOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-done',
                txt : I18N.message("perc.ui.extend.ui.dialog@Done Button")
                //display : 'block'
            },
            "Done Disabled": {
                //bgimage: 'url(' + baseUrl + '/images/images/buttonDoneDisabled.png)',
                //width: '78px',
                //height: '29px',
                cls: 'btn btn-primary perc-done-disabled',
                txt : I18N.message("perc.ui.extend.ui.dialog@Done Button")
                //display: 'block'
            },
            "Import" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonImport.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonImportOver.png)',
                //width : '78px',
                // height : '29px',
                cls : 'btn btn-primary perc-import',
                txt : I18N.message("perc.ui.ImportProgressDialog.title@Import")
                //display : 'block'
            },
            "Activate" : {
                //bgimage : 'url(' + baseUrl + '/images/images/perc-lmg-activate-sprites.png)',
                // bgimageOver : 'url(' + baseUrl + '/images/images/perc-lmg-activate-sprites.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-activate',
                txt : I18N.message("perc.ui.extend.ui.dialog@Activate Button")
                //display : 'block'
            },
            "Start" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonStart.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonStartOver.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-start',
                txt : I18N.message("perc.ui.extend.ui.dialog@Start Button")
                //display : 'block'
            },
            "Search" : {
                //bgimage : 'url(' + baseUrl + '/images/images/blueButtonSearch.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/blueButtonSearchOver.png)',
                //width : '100px',
                //height : '29px',
                cls : 'btn btn-primary perc-search',
                txt : I18N.message("perc.ui.dashboard@Search")
                //display : 'block'
            },
            "Submit" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonWfsubmit.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonWfsubmitOver.png)',
                //width : '100px',
                //height : '29px',
                cls : 'btn btn-primary perc-search',
                txt : I18N.message("perc.ui.edit.workflow.step.dialog@Submit")
                //display : 'block'
            },
            "Approve" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonWfapprove.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonWfapproveOver.png)',
                //width : '88px',
                //height : '29px',
                cls : 'btn btn-primary perc-search',
                txt : I18N.message("perc.ui.edit.workflow.step.dialog@Approve")
                //display : 'block'
            },
            "Reject" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonWfreject.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonWfrejectOver.png)',
                //width : '100px',
                //height : '29px',
                cls : 'btn btn-primary perc-search',
                txt : I18N.message("perc.ui.edit.workflow.step.dialog@Reject")
                //display : 'block'
            },
            "Resubmit" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonWfresubmit.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonWfresubmitOver.png)',
                //width : '126px',
                //height : '29px',
                cls : 'btn btn-primary perc-search',
                txt : I18N.message("perc.ui.edit.workflow.step.dialog@Resubmit")
                //display : 'block'
            },
            "Archive" : {
                //bgimage : 'url(' + baseUrl + '/images/images/buttonWfarchive.png)',
                //bgimageOver : 'url(' + baseUrl + '/images/images/buttonWfarchiveOver.png)',
                //width : '88px',
                //height : '29px',
                cls : 'btn btn-primary perc-search',
                txt : I18N.message("perc.ui.edit.workflow.step.dialog@Archive")
                //display : 'block'
            }
        };

        //Added a new mapping object for names in spanish.
        // Currently added text for "Activate" & "Cancel" buttons on license activation dialog.
        // Can add others if found more issues where mapping[name] gives "undefined" as the name text comes as per locale but "mapping" object has English text keys only .
        var mappingSpanish ={
            "Activar" : {
                //bgimage : 'url(' + baseUrl + '/images/images/perc-lmg-activate-sprites.png)',
                // bgimageOver : 'url(' + baseUrl + '/images/images/perc-lmg-activate-sprites.png)',
                //width : '78px',
                //height : '29px',
                cls : 'btn btn-primary perc-activate',
                txt : I18N.message("perc.ui.extend.ui.dialog@Activate Button")
                //display : 'block'
            },
            "Cancelar" : {
                //  bgimage : 'url(' + baseUrl + '/images/images/buttonCancel.gif)',
                //  bgimageOver : 'url(' + baseUrl + '/images/images/buttonCancelOver.gif)',
                //  width : '78px',
                //  height : '29px',
                cls : 'btn btn-primary perc-cancel',
                txt : I18N.message("perc.ui.assign.workflow@Cancel"),
            }
        };

        // if we already have a button pane, remove it
        this.uiDialog.find('.ui-dialog-buttonpane').remove();

        (typeof percButtons == 'object' && percButtons !== null && $.each(percButtons, function()
        {
            return !(hasButtons = true);
        }));
        if (hasButtons)
        {
            $.each(percButtons, function(name, fn)
            {
                var mappingName=mapping[name];
                if(typeof mappingName === 'undefined'){
                    mappingName=mappingSpanish[name];
                }

                $('<button></button>').attr("id", fn.id)
                //.css("background-image", mapping[name].bgimage)
                //.css("display", mapping[name].display)
                // .css("height", mapping[name].height)
                // .css("width", mapping[name].width)
                    .css("type", "button")
                    .css("float", "right")
                    .css("margin-right", "10px")
                    .css("margin-top", "13px")
                    .css("font-weight", "normal")
                    .css("font", "13.333px Arial")
                    .css("padding-top", "6px")
                    .css("padding-bottom", "6px")
                    .css("padding-left", "12px")
                    .css("padding-right", "12px")
                    .css("white-space", "nowrap")
                    .css("background-color", "#00a8df")
                    .css("border-color", "#00a3d9")
                    .css("color", "#ffffff")
                    .css("border-radius", "4px")
                    .css("cursor", "pointer")
                    .css("border-style", "outset")
                    .css("border-width", "2px")
                    .html(mappingName.txt)
                    .addClass(mappingName.cls )
                    .on("click",function()
                    {
                        fn.click.apply(self.element[0], arguments);
                    })
                    .on("focus",function()
                    {
                        $(this).addClass('ui-state-focus');
                    })
                    .on("blur",function()
                    {
                        $(this).removeClass('ui-state-focus');
                    })
                    .appendTo(uiDialogButtonPane);
            });
            uiDialogButtonPane.appendTo(this.uiDialog);
        }
    };

})(jQuery);
