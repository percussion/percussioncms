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
