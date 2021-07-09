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

(function($) {
    $.PercViewReadyManager = {
        wrappers : [],
        isInitialized:false,
        init:function(){
            this.isInitialized=true;
        },
        setWrapper : function(wrappertoset){
            if(!this.isInitialized)
                return false;
            var alreadyInUse = false;
            var self = this;
            //If any of the component in the wrappertoset is not finished rendering then return false
            $(wrappertoset.components).each(function(index, value){
                if(self.getWrapper(value)!==null){
                    alreadyInUse = true;

                }
            });
            if(alreadyInUse) {
                return false;
            }  
            $("#perc-ui-view-indicator").removeClass("perc-ui-view-ready").addClass("perc-ui-view-processing");
            this.wrappers.push(wrappertoset);
            wrappertoset.init();
            this.logMessage("Added wrapper '" + wrappertoset.wrapperName + "' to manager");
            return true;
        },
        setProcessingFlag: function(){
            $("#perc-ui-view-indicator").removeClass("perc-ui-view-ready").addClass("perc-ui-view-processing");
        },
        getWrapper : function(componentname){
            var wrapper = null;
            $(this.wrappers).each(function(){
                if(this.isComponentInWrapper(componentname, true)){
                    wrapper = this;

                }
            });
            return wrapper;
        },
        logMessage : function(msg){
            if(window.console && gDebug){
                console.log(msg);
            }
        },
        handleWrapperComplete:function(wrapperName){
            this.wrappers.splice($.inArray(wrapperName,this.wrappers),1);
            this.logMessage("Removed wrapper '" + wrapperName + "' from manager");

            if(this.wrappers.length === 0){
                $("#perc-ui-view-indicator").removeClass("perc-ui-view-processing").addClass("perc-ui-view-ready");
                this.logMessage("The view is ready.");
            }
        },
        showRenderingProgressWarning: function(){
          $.perc_utils.alert_dialog(
          {
              content: I18N.message("perc.ui.view.ready.manager@Page Components Rendering"),
              title: I18N.message("perc.ui.page.general@Warning")
          });
        }
    };
    $.PercComponentWrapper = function(name, componentArray){
        return {
            wrapperName : name,
            wrapperStatus : "initialized",
            processedComponents : [],
            components : componentArray,
            isWrapperComplete : function(){
                return this.wrapperStatus === "processed";
            },
            isComponentInWrapper : function(componentName, processedFlag){
                return processedFlag?$.inArray(componentName, this.components)!==-1 && $.inArray(componentName, this.processedComponents) === -1 : $.inArray(componentName, this.components) !== -1;
            },
            handleComponentProgress:function(compName, progress){
                if($.inArray(compName, this.components) === -1){
                    $.PercViewReadyManager.logMessage("The component '" + compName + "' doesn't exist in the wrapper." + this.wrapperName);
                    return;
                }
                let compWrapper;
                let component;
                if(progress === "complete"){
                    this.processedComponents.push(compName);
                    compWrapper = $("#" + compName);
                    compWrapper.remove();
                    component = $("[perc-ui-component='" + compName + "']");
                    component.addClass("perc-ui-component-ready").removeClass("perc-ui-component-processing");
                    if(this.components.length === this.processedComponents.length){
                        this.wrapperStatus = "processed";
                        $.PercViewReadyManager.handleWrapperComplete(this.wrapperName);
                    }
                }
                else{
                    $(".perc-ui-component-overlay").each(function(){
                         compWrapper = $(this);
                         component = $("[perc-ui-component='" + compWrapper.attr("id") + "']");
                        var compPos = component.position();
                        if(compPos == null)
                            return;
                        compWrapper.css("top", compPos.top).css("left",compPos.left).css("height",component.height()).css("width",component.width());
                        //Toggle the image
                    });
                }
            },            
            init:function(){
                $(this.components).each(function(){
                    var compName = this;
                    var component = $("[perc-ui-component='" + compName + "']");
                    component.addClass("perc-ui-component-processing").removeClass("perc-ui-component-ready");
                    var compPos = component.position();
                    var compWrapper = $("<div class='perc-ui-component-overlay ui-widget-overlay' style='position:absolute; background-color:silver; opacity:0.3; border-radius: 6px; border: 1px solid silver; z-index:9000'>" +
                                        "<img src='/cm/images/images/Busy.gif' alt='perc.ui.assign.workflow@LoadingGifAlt' style='margin: auto;  position: absolute;  top: 0; left: 0; bottom: 0; right: 0;'>" +
                                      "</div>");
                    compWrapper.attr("id",compName).css("top", compPos.top).css("left",compPos.left).css("height",component.height()).css("width",component.width());
                    component.after(compWrapper);
                });

            }
        };
    };
})(jQuery);
