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
////////////////////////////////////////////////////////////////////////////////
// The custom object for managing poll related services at the client.
//  Service provided currently is...
//  1. Store poll commands (usually the setTimeOut command), and the id
//  resulting out of activating (eval) it, keyed by the poll id (typically the  
//  id's of pollcomponents themselves)
//  2. Add and activate a poll command.
//  3. Reactivate all the poll commands that were set earlier.
////////////////////////////////////////////////////////////////////////////////
function _TrPollManager()
{
  this.pollIdList;
  this.active = true;
}

// Adds a command (replaces one that already exists with the same poll id)
//  and executes it right away.
_TrPollManager.prototype.addAndActivate = function(pollId, commandString, timeout)
{
  if (!this.pollIdList)
    this.pollIdList = new Array();
  this[pollId] = new _TrPollCommand(commandString, timeout, this.active);
  idIndex = -1;
  for (var i=0; i<this.pollIdList.length; i++)
  {
    if (pollId == this.pollIdList[i])
    {
      idIndex = i;
      break;
    }
  }
  if (idIndex != -1)
  {
    this.pollIdList[idIndex] == pollId;
  }
  else
  {
    this.pollIdList.push(pollId);
  }
}

// Deactivate all the registered poll commands.
_TrPollManager.prototype.deactivateAll = function()
{
  for (var i=0; i<this.pollIdList.length; i++)
  {
    clearTimeout(this[this.pollIdList[i]].commandId);
  }
  this.active = false;
}

// Reactivate all the registered poll commands.
_TrPollManager.prototype.reactivateAll = function()
{
  for (var i=0; i<this.pollIdList.length; i++)
  {
    this[this.pollIdList[i]].activate();
  }
  this.active = true;
}

////////////////////////////////////////////////////////////////////////////////
// Custom object representing a poll command.
////////////////////////////////////////////////////////////////////////////////
function _TrPollCommand(commandString, timeout, activate)
{
  this.commandString = commandString;
  this.timeout = timeout;
  if (activate)
    this.activate();
}

_TrPollCommand.prototype.activate = function()
{
  this.commandId = setTimeout(this.commandString, this.timeout);
}

