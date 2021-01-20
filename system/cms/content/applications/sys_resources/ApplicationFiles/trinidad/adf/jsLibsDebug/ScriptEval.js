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
 
/**
 * Note that this file may change without notice.
 */

// Evaluate the scripts stored in the _pprScripts
// javascript variable. 
// To guarantee the other libraries are loaded first, we eval
// the scripts in this library, which is loaded after all
// the other libraries.
if (!_pprBackRestoreInlineScripts)
{
  var iFrameElement = document.getElementById("_pprIFrame");
  if (iFrameElement != null)
  {
  
    // get the script to eval from the _pprScripts variable.
    
    // first try to get it from the iFrame's document  
    var code = 
      _getCommentedScript(iFrameElement.contentWindow.document, "_pprScripts");
    // if that didn't yield anything, then try to get it from the
    // document. This is how you get it from a full page response
    // to a PPR request.
    if (code == null)
    {
      code = _getCommentedScript(document, "_pprScripts");
    }
  
    // now that we have the code, evaluate it.
    if (code != null)
    {
      _eval(window, code);
    }
  }
}
else
{
  // For the PPR Back Support, re-evaluate the inline javascript 
  // that is saved in the hidden field.
  var saveScriptElement = _getElementById(document, "_pprSaveScript");
  if (saveScriptElement != null && saveScriptElement.value != "")
  {
    _eval(window, saveScriptElement.value);
  }
  _pprBackRestoreInlineScripts = false;
}
