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
function TrStatusIndicator()
{
}

TrStatusIndicator._register = function(id)
{
  if (!TrStatusIndicator._registered)
  {
    TrStatusIndicator._registered = new Object();
    TrPage.getInstance().getRequestQueue().addStateChangeListener(
       TrStatusIndicator._handleStateChange);
  }

  TrStatusIndicator._registered[id] = true;
}


TrStatusIndicator._handleStateChange = function(state)
{
  var busy = state == TrRequestQueue.STATE_BUSY;

  for (id in TrStatusIndicator._registered)
  {
    var busyIcon = document.getElementById(id + "::busy");
    // If we can't find the icon, bail on the assumption that we've
    // been removed (easier than aggressive removal)
    // TODO: in theory, we should be able to remove the property here -
    // but would that break the "for" loop?
    if (!busyIcon)
      continue;

    busyIcon.style.display  = busy ? "inline" : "none";
    var readyIcon = document.getElementById(id + "::ready");
    readyIcon.style.display  = busy ? "none" : "inline";
  }
}

