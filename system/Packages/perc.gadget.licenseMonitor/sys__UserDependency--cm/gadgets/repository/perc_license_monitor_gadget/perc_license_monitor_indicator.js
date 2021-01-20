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

(function($) {
    // Public API
    $.perc_license_monitor_indicator = {
        createIndicator: createIndicator
    };

    /**
     * Creates an indicator given a containing DIV wrapped with jQuery
     * @param elem DIV element wrapped with jQuery
     * @param label String label for the indicator (if undefined, the indicator is a placeholder)
     * @return Object with the API of the indicator
     */
    function createIndicator(elem, label)
    {
        var indicator = elem,
            theIndicator = this,
            api = {};

        function update(usage, limit)
        {
            var isLimited = (limit > 0);
                percentage = 100 * usage / limit,
                bar = indicator.find('.bar'),
                usageBar = bar.find('.usage'),
                usageBarExcess = indicator.find('.bar-exceeded');

            // Calculates the CSS class name depending on the % given
            function cssClassNameOfPercentage(percentage)
            {
                if (percentage > 100)
                {
                    return 'level-exceeded';
                }
                if (percentage > 79)
                {
                    return 'level-warning';
                }
                return 'level-ok';
            }

            // Updates the usage numbers and labels
            function updateUsageLabel()
            {
                var limitLabel;
                if (isLimited)
                {
                    limitLabel = limit.toString();
                }
                else
                {
                    limitLabel = 'Unlimited';
                }
                indicator.find('.usage-numbers').html(usage.toString() + ' of ' + limitLabel);
            }

            // Updates and re-render the corresponding usage bar
            function updateUsageBar()
            {
                var excessBar,
                    excessBarWidth;
                // Remove the corresponding HTML elements of the indicator to re-render them
                usageBar.remove();
                usageBarExcess.remove();

                // Manipulate the appearance according to the limit
                if (isLimited)
                {
                    bar.parent().addClass('shadow');
                    indicator.append('<div class="limit-marker">100%</div>');

                    usageBar = $('<div class="usage ' + cssClassNameOfPercentage(percentage) + '"></div>');
                    usageBar.css('width', ((percentage > 100) ? 100 : percentage) + '%');
                    bar.append(usageBar).addClass('limited');

                    if (percentage > 100)
                    {
                        excessBar = $('<div class="bar-exceeded"></div>');
                        indicator.find('.bar-container').append(excessBar).append('<div class="bar-exceeded-end"></div>');
                        // The default width of the excess bar is 20%, but if it less we must
                        // redimension the bar
                        excessBarWidth = (percentage < 120) ? percentage - 100 : 20;
                        excessBar.css('width', excessBarWidth + '%');
                    }
                }
                else
                {
                    // Unlimited bar is made of 2 "halfs" bars with CSS gradients
                    usageBar = $('<div class="usage level-unlimited-start"></div>');
                    bar.append(usageBar)
                        .append('<div class="level-unlimited-end"></div>')
                        .addClass('unlimited').removeClass('');
                }
            }
            //////////////////////////////////////////////////
            // update function excecution starts here
            usage = (usage === undefined) ? 0 : usage;
            updateUsageLabel();
            //updateUsageBar();
        }

        function display(flag)
        {
            if (flag)
            {
                indicator.removeClass('nodisplay');
            }
            else
            {
                indicator.addClass('nodisplay');
            }
        }

        function getElem()
        {
            return indicator;
        }

        //////////////////////////////////////////////////
        // createIndicator function excecution starts here
        indicator.addClass('indicator');

        // Work with the markup, depending if it is a placeholder (nolabel)
        if (label === undefined)
        {
            var indicatorPlaceholderMarkup = '<div class="placeholder-bar-container">';
            indicatorPlaceholderMarkup += '<div class="placeholder-bar"></div>';
            indicatorPlaceholderMarkup += '</div>';
            indicator
                .addClass('empty')
                .html(indicatorPlaceholderMarkup);
        }
        else
        {
            var indicatorNormalMarkup = label + ': <span class="usage-numbers">0</span>';
            indicator.html(indicatorNormalMarkup);
        }

        // Add the corresponding methods to the api object and return it
        if (label !== undefined)
        {
            api['update'] = update;
        }
        api['display'] = display;
        api['getElem'] = getElem;
        return api;
    }
})(jQuery);