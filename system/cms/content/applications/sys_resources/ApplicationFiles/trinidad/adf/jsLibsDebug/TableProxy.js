/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


function _tableSort(
 formName,
 vld,
 src,
 val,
 order)
{
  _submitPartialChange(formName,vld,
                       {event:'sort',
                        source:src,
                        value:val,
                        state:order});
  return false;
}


// Constructor
function CollectionComponent(
  formName,
  name
  )
{
  this._formName = formName;
  this._name = name;
}

// returns the name of the form
CollectionComponent.prototype.getFormName = function()
{
    return this._formName;
};

// returns the name of this component
CollectionComponent.prototype.getName = function()
{
    return this._name;
};

// gets a form input element with the given name that is inside this
// component.
CollectionComponent.prototype.getFormElement = function(elementName)
{
    var form = document.forms[this.getFormName()];
    var formElementName = this.getName()+":"+elementName;
    var element = form[formElementName];
    return element;
};

// extends the CollectionComponent class to perform submits.
// @param eventParam the event parameter name. eg:"event"
// @param sourceParam the source parameter name. eg:"source"
CollectionComponent.defineSubmit = function(eventParam,sourceParam)
{
    // check to make sure we are called only once:
    if (this._eventParam != (void 0))
      return;
    CollectionComponent.prototype._eventParam = eventParam;
    CollectionComponent.prototype._sourceParam = sourceParam;
    CollectionComponent.prototype._pTargetsParam = "partialTargets";

    // adds the given name,value pair to the submit parameters
    CollectionComponent.prototype.addParam = function(paramName,paramValue) {
      if (this._params == (void 0))
      {
        this._params = new Object();
      }
      this._params[paramName] = paramValue;
    }

    // submit the form with the given event.
    // "link" is the DOM <a> that triggered this submission.
    CollectionComponent.prototype.submit = function(event, link) {
      this.addParam(this._eventParam, event);
      this.addParam(this._sourceParam, this.getName());
      var params = this._params;
      var pTargets = params[this._pTargetsParam];

      if (link != (void 0))
      {
        var focusId = link.id;
        if (focusId != (void 0))
        {
          // this is the most reliable way to ensure that the
          // focus is reset in both moz and IE:
          _setRequestedFocusNode(document, focusId, false, window);
        }//VAC fix for PDA's
        if (pTargets == (void 0))
        {
          pTargets = this.getName();
          params[this._pTargetsParam] = pTargets;
        }
      }

      // immediate defaults to false, meaning there should be client validation
      var validate = this._validate;
      if ( validate == (void 0))
        validate = 1;

      var subFunc = submitForm;
      if (pTargets != (void 0))
      {
        subFunc = _submitPartialChange;
      }
      subFunc(this.getFormName(), validate, params);
      return false;
    };
};

// enhances the CollectionComponent class to perform multiple selection.
// @param selectedKey the name of the checkbox element.
// @param selectedModeKey the name of the 'selectedMode' hidden form field.
// @param auto whether to autosubmit
CollectionComponent.defineMultiSelect = function(selectedKey,selectedModeKey,auto)
{
    // check to make sure we are called only once:
    if (this._selectedKey != (void 0))
      return;
    // these are on the prototype because they only need to be defined
    // once:
    CollectionComponent.prototype._selectedKey = selectedKey;
    CollectionComponent.prototype._selectedModeKey = selectedModeKey;


    // returns the number of rows displayed on screen for this component
    CollectionComponent.prototype.getLength = function() {
        var boxes = this._getBoxes();
        return boxes.length;
    };

    // preforms a select all or select none depending on the parameter:
    CollectionComponent.prototype.multiSelect = function(selectAll) {
        var boxes = this._getBoxes();
        for(var i=0; i<boxes.length; i++)
        {
            var box = boxes[i];
            box.checked = selectAll;
        }
        var selectedMode = this.getFormElement(this._selectedModeKey);
        if (selectAll)
        {
          selectedMode.value = "all";
        }
        else
        {
          selectedMode.value = "none";
        }
        if (auto) // bug 4612379
        {
          _submitPartialChange(this.getFormName(), 1, null);
        }
    };


    // Gets all the select check boxes (or radio buttons):
    CollectionComponent.prototype._getBoxes = function() {
        var boxes = this.getFormElement(this._selectedKey);
        // if there was only one checkBox/radioButton on the page
        // then the value returned will not be an array so convert it
        // into one:
        if (boxes.length == (void 0))
        {
          var boxes_t = new Array(1);
          boxes_t[0] = boxes;
          boxes = boxes_t;
        }
        return boxes;
    };
};

// enhances this class to perform tree functions.
CollectionComponent.defineTree =
    function(eventParam,
             sourceParam,
             pathParam,
             startParam,
             gotoEvent,
             focusEvent,
             validate)
{
    // check to make sure we are called only once:
    if (this._pathParam != (void 0))
      return;

    CollectionComponent.defineSubmit(eventParam, sourceParam);

    CollectionComponent.prototype._pathParam = pathParam;
    CollectionComponent.prototype._startParam = startParam;
    CollectionComponent.prototype._gotoEvent = gotoEvent;
    CollectionComponent.prototype._focusEvent = focusEvent;
    CollectionComponent.prototype._validate = validate;

    CollectionComponent.prototype.action = function(event,path,link)
    {
      this.addParam(this._pathParam,path);
      return this.submit(event, link);
    };

    CollectionComponent.prototype.range = function(path,start)
    {
      this.addParam(this._startParam, start);
      return this.action(this._gotoEvent, path);
    };

    CollectionComponent.prototype.focus = function(path,link)
    {
        return this.action(this._focusEvent, path, link);
    };
};
