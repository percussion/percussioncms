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

/*
 * Wizard dialog widget
 *
 * This widget can create a multi page wizard dialog based on a set of
 * step defined in a div structure.
 *
 * Example Usage:
 *
 * $(document).ready(function() {
 *
 *      var $wizard = $("<div></div>").perc_wizard({
 *         templateUrl: "../html/dialogs/perc_newSiteDialog.html",
 *         title: "New Site",
 *         height: 400
 *         }
 *
 *      });
 *
 *      $('#someButton').click(function() {
 *           $wizard.perc_wizard('open');
 *      });
 * });
 *
 *  The template structure:
 *
 * <div id="perc_newSiteDialog" class="perc_dialog">
 *    <div id="perc_wizard_step1" class="perc_wizard_step">
 *      <div class="perc_dialog_summary">
 *          Some summary text
 *      </div>
 *        ...
 *        ... step 1 body here
 *     </div>
 *     <div id="perc_wizard_step2" class="perc_wizard_step">
 *      <div class="perc_dialog_summary">
 *          Some summary text
 *      </div>
 *        ...
 *        ... step 2 body here
 *     </div>
 *   </div>
 *
 */
(function($)
{
    $.widget("ui.perc_wizard", {
        // Globals
        steps: null,
        currentStep: 0,
        isCancelled: false,

        _init: function()
        {
            var self = this,
                options = this.options;
            this.element.load(options.templateUrl, function()
            {
                self.element.perc_dialog(
                {
                    autoOpen: false,
                    title: options.title,
                    modal: options.modal,
                    width: options.width,
                    height: options.height,
                    show: options.show,
                    hide: options.hide,
                    open: options.open,
                    resizable: options.resizable
                });
                self.steps = $('.perc_wizard_step');
                self._addButtons();

            });
        },

        open: function()
        {
            this._showStep(0);
            this.element.dialog('open');
        },

        _addButtons: function()
        {
            var self = this;

            var buttons = "<div class='ui-dialog-buttonpane ui-widget-content ui-helper-clearfix'>" +
            	 "<button id='perc_wizard_finish' class='btn btn-primary' name='perc_wizard_finish' style='float:right;'>" +I18N.message("perc.ui.common.label@Finish") + "</button>" +
                "<button id='perc_wizard_next' class='btn btn-primary'  name='perc_wizard_next' style='float:right;'>" + I18N.message("perc.ui.common.label@Next") + "</button>" +
                 "<button id='perc_wizard_cancel' class='btn btn-primary' name='perc_wizard_cancel' style='float:right;'>" +I18N.message("perc.ui.assign.workflow@Cancel") + "</button>" +
                 "<button id='perc_wizard_back' class='btn btn-primary'  name='perc_wizard_back' style='float:right;'>" +I18N.message("perc.ui.common.label@Back") +  "</button>" + "</div>";
            //Appending buttons to the buttonpane
            $(self.element).closest('.ui-dialog').append(buttons);
            $('#perc_wizard_back').on("click", function()
            {
                self._onBack();
            });
            $('#perc_wizard_cancel').on("click", function()
            {
                self._onCancel();
            }).val(this.options.cancelButtonLabel);
            $('#perc_wizard_next').on("click",function()
            {
                self._onNext();
            });
            $('#perc_wizard_finish').on("click",function()
            {
                self._onOk();
            });

        },

        _showStep: function(step)
        {
            if (this.steps == null || step > this.steps.length)
            {
                alert(I18N.message("perc.ui.wizard@Step Does Not Exist"));
                return;
            }
            this.currentStep = step;
            for (i = 0; i < this.steps.length; i++)
            {
                if (i == step) $(this.steps[i]).show();
                else $(this.steps[i]).hide();
            }

            if (step == 0 && this.steps.length > 1)
            {
                //first step of 2 or more
                $('#perc_wizard_back').hide();
                $('#perc_wizard_cancel').show().val(this.options.cancelButtonLabel);
                $('#perc_wizard_next').show().val(this.options.nextButtonLabel);
                $('#perc_wizard_finish').hide();

            }
            else if (step < this.steps.length - 1)
            {
                //mid step
                $('#perc_wizard_back').show().val(this.options.backButtonLabel);
                $('#perc_wizard_cancel').show().val(this.options.cancelButtonLabel);
                $('#perc_wizard_next').show().val(this.options.nextButtonLabel);
                $('#perc_wizard_finish').hide();
            }
            else
            {
                //final step
                $('#perc_wizard_back').show().val(this.options.backButtonLabel);
                $('#perc_wizard_cancel').show().val(this.options.cancelButtonLabel);
                $('#perc_wizard_next').hide();
                $('#perc_wizard_finish').show().val(this.options.finishButtonLabel);
            }


            var elem = this.element;
        },

        _onOk: function(e)
        {
            var self = this;
            if (this.options.onValidate()) this.options.onOk();
        },

        _onNext: function(e)
        {
            if (this.options.onValidate())
            {
                // If a custom onNext function was defined, excecute it before going to the next
                // step (and retrieve its return value)
                if (this.options.onNext !== undefined && this.options.onNext() === false)
                {
                    return;
                }
                this._showStep(this.currentStep + 1);
            }
        },

        _onBack: function()
        {
            if (this.currentStep > 0) this._showStep(this.currentStep - 1);
        },

        _onCancel: function()
        {
            this.isCancelled = true;
            this.element.dialog('close');
        }
    });

    $.extend($.ui.perc_wizard, {
        version: "1.0.0",
        defaults: {
            backButtonLabel: typeof I18N == "undefined" ? "Back" : I18N.message("perc.ui.common.label@Back"),
            cancelButtonLabel: typeof I18N == "undefined" ? "Cancel" : I18N.message("perc.ui.common.label@Cancel"),
            nextButtonLabel: typeof I18N == "undefined" ? "Next" : I18N.message("perc.ui.common.label@Next"),
            finishButtonLabel: typeof I18N == "undefined" ? "Finish" : I18N.message("perc.ui.common.label@Finish"),
            width: "800px",
            height: "700px",
            modal: true,
            title: typeof I18N == "undefined" ? "Wizard" : I18N.message("perc.ui.common.label@Wizard"),
            onOk: function()
            {
                console.log("No onOk callback was set.");
            },
            onValidate: function()
            {
                return true;
            }
        }
    });

})(jQuery);
