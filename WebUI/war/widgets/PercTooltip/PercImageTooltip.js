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
 *
 */
(function($) {
    /**
     *  PercImageTooltip()
     *  Plugin implementation
     *
     */
    /** number of pixels to offset the placement of the tooltip */
    var TOP_OFFSET = 100;

    /** template html of the tooltip to render in page */
    var TOOLTIP = '<div style="width:304px;">' +
        '<div style="margin-left:7px; margin-right:7px; height:20px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{name}}</div>' +
        '<div style="text-align:center; width:auto; margin-left:7px; margin-right:7px; vertical-align: middle;">' +
        '<div style="width:290px; height:207px; border:1px solid #BBBBBB; background: url({{url}}) no-repeat center;"></div>' +
        '</div><div style="word-wrap:break-word; white-space:normal; margin-left:7px; margin-right:7px;">{{path}}</div></div>';

    /** replace template variables with respective data values in given text string */
    function template (textString, keysArray, dataHash) {
        for (i = 0; i < keysArray.length; i++) {
            key = keysArray[i];
            re = new RegExp('{{' + key + '}}', 'gi');
            textString = textString.replace(re, dataHash[key]);
        }
        return textString;
    }

    $.fn.PercImageTooltip = function(config) {
        var _clickCallback;
        if (config != null && typeof config.clickCallback === "function")
            _clickCallback = config.clickCallback;
        else
            _clickCallback = function(){};
        var element = $(this).
        on("mouseenter",function(event){$.PercImageTooltip.showTooltip(event, _clickCallback);}).
        on("mouseleave",function(){});
    };

    /**
     *  Singleton containing state variables, DOM, and methods implementing the tooltip behavior
     */
    $.PercImageTooltip = {
        tooltipDom : $("<div/>").
        addClass("perc-tooltip").
        addClass("perc-tooltip-arrow-down").
        append($("<div/>").addClass("perc-tooltip-content").append("Title Goes Here")).
        css("position","absolute").
        css("top","-10000px").
        css("left","-10000px").
        css("z-index","10000").
        on("mouseenter", function(event){$.PercImageTooltip.enterTooltip(event);}).
        on("mouseleave", function(event){$.PercImageTooltip.exitTooltip(event);}).
        on("click",function(event){event.stopPropagation();}),
        /*
         * this is the hider DIV that will be as big as the tooltip and element
         * to emulate blur on both the tooltip and element to then hide the tooltip
         */
        hiderDom : $("<div class='perc-tooltip-hider'/>").
        css("position","absolute").
        css("top","-10000px").
        css("left","-10000px").
        css("z-index","9000").
        on("mouseenter",function(event){$.PercImageTooltip.enterHider(event);}).
        on("mouseleave", function(event){$.PercImageTooltip.exitHider(event);}),

        /*
         *  State variables to keep track hover on tooltip and hider
         *  0 == not hovering on tooltip/hider
         *  1 == hovering on tooltip/hider
         *  TODO: make these named constants
         */
        hiderHoverState   : 0,
        tooltipHoverState : 0,

        showTooltip : function(event, clickCallback) {
            var keys = ['name', 'path', 'url'];
            var _clickCallback = clickCallback;
            var _self = this;
            checkImageAvailable = function(url, pageData){
                var path = pageData.path.replace("/Sites/", "").replace(/^\//, "");
                var html = template(TOOLTIP, keys, {
                    url: url,
                    path: path,
                    name: pageData.name
                });
                var args = {
                    async: true,
                    type: "HEAD",
                    url: url,
                    success: function(data, textstatus){
                        $.PercImageTooltip.tooltipDom.find(".perc-tooltip-content").html(html);
                    },
                    error: function(request, textstatus, error){
                        if ($(".perc-tooltip").position().top !== -10000) {
                            setTimeout(function(){
                                url="/Rhythmyx/rx_resources/images/TemplateImages/AnySite/perc.base.plain_Thumb.png?nocache=0.16234142855889466";
                                checkImageAvailable(url, pageData);
                            }, 500);
                        }
                    }
                };
                $.ajax(args);
            };

            var element = $(event.currentTarget);

            var ePos = element.offset();
            var eLeft = ePos.left;
            var eTop = ePos.top;
            var eWidth = element.width();
            var eHeight = element.height();

            var data = JSON.parse(element.attr("data"));

            // dont bother with empty data
            if (data === undefined || data === "" || data === null)
                return;

            var imgSrc = "/Rhythmyx/rx_resources/images/TemplateImages/" + $.PercNavigationManager.getSiteName() + "/" + data.id + "-page.jpg"  + "?nocache=" + Math.random();

            var path = data.path.replace("/Sites/", "").replace(/^\//, "");
            var html = template(TOOLTIP, keys, {
                url: '/Rhythmyx/sys_resources/images/running.gif',
                path: path,
                name: data.name
            });

            $.PercImageTooltip.tooltipDom.find(".perc-tooltip-content").html(html).css("display", "block");

            var tWidth = $.PercImageTooltip.tooltipDom.width();
            var tHeight = $.PercImageTooltip.tooltipDom.height();
            var mLeft = event.pageX;
            //var tLeft = mLeft - tWidth / 2;
            var tLeft = mLeft + 20;
            if (tLeft < 0)
                tLeft = 0;
            var tTop = (eTop - tHeight + TOP_OFFSET);
            $.PercImageTooltip.tooltipDom.css("top", tTop).css("left", tLeft).css("display", "block");

            $.PercImageTooltip.hiderDom.css("width", tWidth + 5).css("height", eHeight). // TODO: make 7 into a named constant
                css("top", eTop).css("left", eLeft).css({
                "background": "blue",
                "opacity": 0.0
            }).css("cursor", element.css("cursor")).off("click").on("click",function(event){
                if (typeof _clickCallback === "function")
                    _clickCallback(element, data.id);
                if (element.data("events") && element.data("events")["click"] && element.data("events")["click"][0] && element.data("events")["click"][0].data && element.data("events")["click"][0].handler) {
                    var e = {
                        data: element.data("events")["click"][0].data
                    };
                    element.data("events")["click"][0].handler(e);
                }
            });

            // check if the image exists and update the url to display it in the tooltip
            checkImageAvailable(imgSrc, data);

        },
        exitHider : function(event) {
            this.hiderHoverState = 0;   // TODO: use a named constant instead
            var self = this;
            setTimeout(function(){self.hideTooltip(event);},500);
        },
        enterHider : function(event) {
            this.hiderHoverState = 1;   // TODO: use a named constant instead
            var self = this;
            setTimeout(function(){self.hideTooltip(event);},500);
        },
        exitTooltip : function(event) {
            this.tooltipHoverState = 0;   // TODO: use a named constant instead
            var self = this;
            setTimeout(function(){self.hideTooltip(event);},500);
        },
        enterTooltip : function(event) {
            this.tooltipHoverState = 1;   // TODO: use a named constant instead
            var self = this;
            setTimeout(function(){self.hideTooltip(event);},500);
        },
        hideTooltip : function(event) {
            // TODO: use named constants instead
            if(this.tooltipHoverState === 0 && this.hiderHoverState === 0) {
                this.tooltipDom.
                trigger("blur").
                css("top",-10000).
                css("left",-10000).
                find(".perc-tooltip-content").
                html("");
                this.hiderDom.
                css("top",-10000).
                css("left",-10000);
            }
        }
    };

    $(document).ready(function() {
        $("body").append($.PercImageTooltip.tooltipDom);
        $("body").append($.PercImageTooltip.hiderDom);
    });


})(jQuery);
