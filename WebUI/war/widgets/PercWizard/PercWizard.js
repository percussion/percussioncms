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
 * @author Jose Annunziato
 */
(function($) {
    $.PercWizard = {
        clearFieldsOnShow : true,
        stepTitleNavigation : true,
        showStepTitles : true,
        dom : $("<div class='perc-wizard'>")
            .append("<div class='perc-wizard-steps'>")
            .append("<div class='perc-wizard-content'>")
            .append("<div class='perc-wizard-required-fields-tip' style='display:none'>* - denotes required field</div>"),
        stepIndexStarts : 1,
        currentStep : 0,
        step : 0,
        steps : [
            {title : "Step 1", content : "Step 1"},
            {title : "Step 2", content : step2},
            {title : "Step 3", content : step3}
        ],
        cache : [],
        next : function(){
            var step = this.currentStep + 1;
            this.gotoStep(step);
        },
        prev : function(){
            var step = this.currentStep - 1;
            this.gotoStep(step);
        },
        beforeTransition : function(currentStep, nextStep, wizardObj){return true;},
        afterTransition  : function(currentStep, prevStep, wizardObj){return true;},
        gotoStep : function(step, me){

            var self = this;

            if(typeof step == "object") {
                self = me;
                var event = step;
                var step = $(event.currentTarget).attr("step");
            }

            // make sure current step is between 0 and this.steps.length-1
            if(step < 0)
                step = 0;
            if(step > this.steps.length-1)
                step = this.steps.length-1;

            // notify beforeTransition listener and, if it's ok with listener, continue
            var ok = true;
            if(typeof this.beforeTransition == "function")
                ok = this.beforeTransition(this.currentStep, step, this);
            if(!ok)
                return;            

            var prevStep = this.currentStep;
            this.currentStep = step;

            if(this.steps[step].showRequiredFieldLabel)
                this.dom.find(".perc-wizard-required-fields-tip").show();
            else
                this.dom.find(".perc-wizard-required-fields-tip").hide();

            if(typeof this.cache[step] == "undefined")
            {
                this.cache[step] = true;
            }

            // hide all the steps and then show only the current step
            $(".perc-wizard-content .perc-wizard-content-step").hide();
            
            var content = $(".perc-wizard-content").find(".perc-wizard-content-"+step);

            if(content.length == 0 || !this.cache[step]) {
                content = this.steps[step].content;
                if(typeof content == "function")
                    content = content(self.dom);
                $(".perc-wizard-content-"+step+".perc-wizard-content-step").empty().remove();
                var contentStep = $("<div class='perc-wizard-content-"+step+" perc-wizard-content-step'>").append(content);
                this.dom.find(".perc-wizard-content").append(contentStep);
            } else {
                content.show();
            }

            // show and hide the buttons depending on what step we are in and the total number of steps
            if(step > 0){
                $(this.buttons.prev).show();
            } else {
                $(this.buttons.prev).hide();
            }
            if(step < this.steps.length-1){
                $(this.buttons.next).show();
                $(this.buttons.finish).hide();
            } else {
                $(this.buttons.next).hide();
                $(this.buttons.finish).show();
            }
            
            if(this.showStepTitles){
                // highlight the current step, unhighlight other steps
                $(".perc-wizard-step-index, .perc-wizard-step-title")
                    .removeClass("perc-wizard-step-active")
                    .addClass("perc-wizard-step-inactive")
                    .css("cursor","default");
                
                // activate current step index and make other steps clickable
                $(".perc-wizard-step-index").each(function(index, stepDom){
                    if(index == step){
                        $(this)
                            .addClass("perc-wizard-step-active")
                            .removeClass("perc-wizard-step-inactive");
                    } else if(self.stepTitleNavigation){
                        $(this)
                            .css("cursor","pointer")
                            .on("click",function(event){self.gotoStep(event, self);});
                    }
                });
                
                // activate current step title and make other steps clickable
                $(".perc-wizard-step-title").each(function(index, stepDom){
                    if(index === step){
                        $(this)
                            .addClass("perc-wizard-step-active")
                            .removeClass("perc-wizard-step-inactive");
                    } else if(self.stepTitleNavigation){
                        $(this)
                            .css("cursor","pointer")
                            .on("click",function(event){
                                self.gotoStep(event, self);
                            });
                    }
                });
            }
            
            if(typeof this.afterTransition == "function")
                ok = this.afterTransition(this.currentStep, prevStep, this);
            
            return true;
        },
        parent : null,
        buttons : {
            prev : '#perc-wizard-back',
            next : '#perc-wizard-next',
            finish : '#perc-wizard-finish'
        },
        dialog : null,
        remove : function(){
            this.dialog.remove();
        },
        show : function(config){

            var self = this;
            $.each(config.steps,function(i,o){
                self.cache[i] = o.cache;
            });

            if(typeof config.stepTitleNavigation != "undefined")
                this.stepTitleNavigation = config.stepTitleNavigation;
            if(typeof config.beforeTransition == "function")
                this.beforeTransition = config.beforeTransition;
            if(typeof config.afterTransition == "function")
                this.afterTransition = config.afterTransition;

            this.id = config.id;
            this.style = config.style;
            this.steps = config.steps;

            var self =this;
            this.dialog = this.dom.perc_dialog({
                autoOpen:true, title:"New Blog", resizable:false, modal:true, height:config.height, width:config.width,
                percButtons:{
                    "Finish":{
                        id:"perc-wizard-finish",
                        click:function(){
                            if($.PercWizard.beforeTransition($.PercWizard.step+1,null,$.PercWizard)){
                               $(".perc-wizard-content").empty();
                               $.PercWizard.remove();
                               self.cache = [];
                               self.stepIndexStarts = 1;
                               self.currentStep = 0;
                               self.step = 0;
                            }
                        }
                    },
                    "Next":{
                        id:"perc-wizard-next",
                        click:function(){
                            $.PercWizard.next();
                        }
                    },
                    "Cancel":{
                        id: "perc-wizard-cancel",
                        click: function(){
                            $.PercWizard.remove();
                        }
                    },
                    "Back":{
                        id:"perc-wizard-back",
                        click:function(){
                            $.PercWizard.prev();
                        }
                    }
                },
                id:self.id
            });

            if(this.showStepTitles){
                var stepsDom = $(".perc-wizard-steps");
                var stepIndex = this.stepIndexStarts;
                if(stepsDom.html() == "") {
                    $.each(this.steps, function(index, step){
                        stepsDom
                            .append("<div step='"+index+"' class='perc-wizard-step-index perc-wizard-step-inactive'>"+stepIndex+"</div>");
                        var stepTitle = step.title;
                        stepsDom.append("<div step='"+index+"' class='perc-wizard-step-title'>"+step.title+"</div>");
                        stepIndex++;
                    });
                    stepsDom.append("<div style='clear : both'>");
                }
            }
            this.gotoStep(0);
            
            if(this.clearFieldsOnShow) {
                this.dom.find("input").val("");
            }
        }
    };
    
    function injectStyle(styleSheet) {
        var head = $(document).find("head");
        /*
        var cssNode = document.createElement('link');
        cssNode.type = 'text/css';
        cssNode.rel = 'stylesheet';
        cssNode.href = styleSheet;
        cssNode.media = 'screen';
        head.appendChild(cssNode);
        */
    }
})(jQuery);

function step2(wizardContentDom){
    return "Step 2";
}

function step3(wizardContentDom){
    return "Step 3";
}
function nextListener(wizardContentDom){
}
