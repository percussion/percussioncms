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

var _byteLenKey = "org.apache.myfaces.trinidad.validator.ByteLengthValidator.MAXIMUM";

function TrByteLengthValidator(
  length,
  messages
  )
{
  this._length   = length;
  this._messages = messages;
  this._class    = "TrByteLengthValidator";
}

TrByteLengthValidator.prototype = new TrValidator();

function CjkFormat(
  length,
  messages
  )
{
  this._base = TrByteLengthValidator;
  this._base(length, messages);
  this._class = "CjkFormat";
  
}

CjkFormat.prototype = new TrByteLengthValidator();
CjkFormat.prototype.getHints = function(
  converter
  )
{
  var messages = null;
  
  if(this._messages["hint"])
  {
  	messages = new Array();
    messages.push(TrMessageFactory.createCustomMessage(
      this._messages["hint"],
	    this._length)
	  );
  }
	return messages;
}
CjkFormat.prototype.validate  = function(
  parseString,
  label,
  converter
  )
{
  var i = 0;
  var length = this._length;

  while (i < parseString.length)
  { 
    var ch = parseString.charCodeAt(i);
    if ((ch < 0x80) || ((0xFF60 < ch) && (ch < 0xFFA0))) length--; 
    else length -= 2;
   
    if (length < 0)
    {
      var facesMessage;
      if(!this._messages["detail"])
      {
        facesMessage = _createFacesMessage(_byteLenKey,
                                           label,
                                           parseString,
                                           this._length);
      }
      else
      {
        facesMessage = _createCustomFacesMessage(
                                           TrMessageFactory.getSummaryString(_byteLenKey),
                                           this._messages["detail"],
                                           label,
                                           parseString,
                                           this._length);
      }
      throw new TrValidatorException(facesMessage);     
    }

    i++;
  }

  return parseString;
}




function Utf8Format(
  length,
  messages
  )
{
  this._base = TrByteLengthValidator;
  this._base(length, messages);
  this._class = "Utf8Format";
}


Utf8Format.prototype = new TrByteLengthValidator();
Utf8Format.prototype.getHints = function(
  converter
  )
{
  var messages = null;
  
  if(this._messages["hint"])
  {
  	messages = new Array();
    messages.push(TrMessageFactory.createCustomMessage(
      this._messages["hint"],
	    this._length)
	  );
  }
	return messages;
}
Utf8Format.prototype.validate  = function(
  parseString,
  label,
  converter
  )
{
  var i = 0;
  var length = this._length;

  while (i < parseString.length)
  { 
    var ch = parseString.charCodeAt(i);
    if (ch < 0x80) length--;
    else if (ch < 0x800) length -= 2;
    else
    {
      // Surrogates;  see bug 3849516
      if ((ch & 0xF800) == 0xD800)
        length -= 2;
      else
        length -= 3;
    }

    if (length < 0)
    {
      var facesMessage;
      if(!this._messages["detail"])
      {
        facesMessage = _createFacesMessage(_byteLenKey,
                                           label,
                                           parseString,
                                           this._length);
      }
      else
      {
        facesMessage = _createCustomFacesMessage(
                                            TrMessageFactory.getSummaryString(_byteLenKey),
                                            this._messages["detail"],
                                            label,
                                            parseString,
                                            this._length);
      }
      throw new TrValidatorException(facesMessage);              
    }

    i++;
  }

  return parseString;
}

function SBFormat(
  length,
  messages
  )
{
  this._base = TrByteLengthValidator;
  this._base(length, messages);
  this._class = "SBFormat";
  
}


SBFormat.prototype = new TrByteLengthValidator();
SBFormat.prototype.getHints = function(
  converter
  )
{
  var messages = null;
  
  if(this._messages["hint"])
  {
  	messages = new Array();
    messages.push(TrMessageFactory.createCustomMessage(
      this._messages["hint"],
	    this._length)
	  );
  }
	return messages;
}
SBFormat.prototype.validate  = function(
  parseString,
  label,
  converter
  )
{
  if (this._length < parseString.length)
  {
      var facesMessage;
      if(!this._messages["detail"])
      {
        facesMessage = _createFacesMessage(_byteLenKey,
                                           label,
                                           parseString,
                                           this._length);
      }
      else
      {
        facesMessage = _createCustomFacesMessage(
                                            TrMessageFactory.getSummaryString(_byteLenKey),
                                            this._messages["detail"],
                                            label,
                                            parseString,
                                            this._length);
      }
    throw new TrValidatorException(facesMessage);      
  }

  return parseString;
}