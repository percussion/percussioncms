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
 * Actions split button
 */
(function ($)
{
    /**
     * Constructs the actions button.
     *
     * @param finder
     * @param contentViewer
     */
    $.perc_build_actions_button = function (finder, contentViewer)
    {
        // Create the action elements
        var cp = $.perc_build_copy_page_button( finder, contentViewer );
        var fp = $.perc_build_folderproperties_button( finder, contentViewer );
        var db = $.perc_build_download_button( finder, contentViewer );
        var ub = $.perc_build_upload_button( finder, contentViewer );
        var ri = $.perc_build_restore_button( finder, contentViewer );

        var menuEntries = [cp, fp, db, ub, ri];
        // Create the menu and the button
        var menu = createMenuHTML(menuEntries)
            .on("mouseenter",function(e){
                preventHide(e);
            })
            .on("mouseleave",function(e){
                hideOnMouseOut(e);
            });

        var btnHtml ='<div id="perc-finder-actions" >' +
            '<a id="perc-finder-actions-button" class="perc-font-icon" title="' +I18N.message("perc.ui.actions.button@Select An Action") +
            '" href="#"><span class="icon-cog fas fa-cog"></span><span class="icon-caret-down fas fa-caret-down"></span></a>' +
            '</div>';
        var btn = $(btnHtml)
            //.perc_button()
            .append(menu)
            .on("mouseenter",function(e){
                preventHide(e);
            })
            .on("mouseleave",function(e){
                hideOnMouseOut(e);
            });

        // This flag, hideOnMouseOut and preventHide prevent an unnatural hiding of the menu
        var flag_show = false;

        /**
         * Binds the hiding behavior to the menu once the cursor left it.
         */
        function hideOnMouseOut(e)
        {
            flag_show = false;
            setTimeout(function() {
                if (!flag_show) {
                    showMenu(false);
                }
            },500);
        }

        /**
         * Prevents the menu hiding if the cursor returns to the hover the menu or the button.
         */
        function preventHide(event)
        {
            var target = $(event.target);

            if (target.attr('id') === btn.attr('id'))
            {
                flag_show = true;
                return;
            }

            if (target.is("#perc-finder-actions *"))
            {
                flag_show = true;
            }
        }

        /**
         * Handler that get called when the button is clicked
         */
        function clickHandler(evt)
        {
            if ($('#perc-finder-actions-button').hasClass('ui-disabled'))
            {
                return false;
            }
            else
            {
                if (menu.css('display') === 'none')
                {
                    showMenu(true,event.pageX,event.pageY);
                }
                else
                {
                    showMenu(false);
                }
                return false;
            }
        }

        /**
         * Makes the menu visible/invisible.
         * @param boolean flag If true, makes the menu visible
         */
        function showMenu(flag,X,Y)
        {
            if (flag)
            {
                var menuX = X  - menu.outerWidth(true);
                var menuY = Y + 10;
                menu
                    .css("top", menuY)
                    .css("left", menuX)
                    .css("display", "block");
            }
            else
            {
                menu.hide();
            }
        }

        /**
         * Helper function to enable or disable the button in the finder.
         * @param flag(boolean) if <code>true</code> the button is enabled, otherwise the button is disabled.
         */
        function enableButton(flag)
        {
            var anchor = $('#perc-finder-actions-button');
            if (flag)
            {
                // We perform an "unbind" first, in case clickHandler has been bound several times by error
                // (same thing in the else)
                anchor.removeClass('ui-disabled').addClass('ui-enabled').off('click').on("click",
                    function(evt){
                        clickHandler(evt);
                    });
            }
            else
            {
                anchor.addClass('ui-disabled').removeClass('ui-enabled').off('click');
            }
        }

        /**
         * Creates the base HTML and adds the menu entries.
         * @param array of menuentries (former button elements)
         */
        function createMenuHTML(menuentries)
        {
            var dropdown = $("<ul class=\"perc-actions-menu box_shadow_with_padding\">");
            var option = $("<li class=\"perc-actions-menu-item\">");

            for(let l of menuentries){
                option.clone().append(l).appendTo(dropdown);
            }

            return dropdown;
        }

        var entriesListenedLeft = menuEntries.length;
        var entriesDisabled = 0;

        /**
         * Callback function that is called whenever an 'actions-change-enabled-state' event
         * is triggered. It uses closure to take advantage of storing state between asynchronous
         * calls and maintain state to finally enable/disable the actions button.
         * NOTE: To debug this function I recommend using console.log()
         */
        function entryChangeEnabledStateListener(evt)
        {

            // In this case, "this" represents the menu entry
            var state_enabled = evt.target.classList.contains("ui-enabled");
            if (entriesListenedLeft === 1 && entriesDisabled < menuEntries.length )
            {
                enableButton(true);
                entriesListenedLeft = menuEntries.length;
                entriesDisabled = 0;
            }
            else
            {
                // The entry is not the last, if is disabled count it
                if (!state_enabled)
                {
                    entriesDisabled++;
                }
                entriesListenedLeft--;
            }
        }

        // Bind the declared function to the buttons in the array menuEntries
        for (let m of menuEntries){
            m.on('actions-change-enabled-state', function(evt){
                entryChangeEnabledStateListener(evt);
            });
        }



        function update_action_btn(path){
            //Placeholder for capturing path changes.
        }

        finder.addPathChangedListener( update_action_btn );

        return btn;
    };

})(jQuery);
