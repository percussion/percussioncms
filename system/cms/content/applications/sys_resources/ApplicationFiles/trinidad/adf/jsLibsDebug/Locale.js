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

var _digits;
var _decimalSep;
var _groupingSep;

/**
 * Returns true if the character is a digit.
 */
function isDigit(
  digitChar
  )
{  
  return (_getDigits()[digitChar] != null);
}


/**
 * Returns an Object containing the digit value for all numeric characters
 * and undefined for non-numeric characters
 */
function _getDigits()
{
  if (_digits == null)
  {
    // starts of 10 digit unicode ranges
    var digitStarts = [
                        0x0030, // ISO-LATIN-1 digits ('0' through '9')
                        0x0660, // Arabic-Indic digits
                        0x06F0, // Extended Arabic-Indic digits
                        0x0966, // Devanagari digits
                        0x09E6, // Bengali digits
                        0x0A66, // Gurmukhi digits
                        0x0AE6, // Gujarati digits
                        0x0B66, // Oriya digits
                        0x0BE7, // Tamil digits
                        0x0C66, // Telugu digits
                        0x0CE6, // Kannada digits
                        0x0D66, // Malayalam digits
                        0x0E50, // Thai digits
                        0x0ED0, // Lao digits
                        0x0F20, // Tibetan digits
                        0xFF10  // Fullwidth digits
                      ];
    
    _digits = new Object();
    
    for (var i = 0; i < digitStarts.length; i++)
    {
      for (var offset = 0; offset < 10; offset++)
      {
        // get the string value of the current unicode character
        var currKey = String.fromCharCode(digitStarts[i] + offset);
        
        // store the digit value of this character
        _digits[currKey] = offset;
      }
    }
  }
  
  return _digits;
}


/**
 * Returns thenumeric value of a digit character or Nan if the
 * character isn't a digit.
 */
function parseDigit(
  digitChar
  )
{  
  var value = _getDigits()[digitChar];
  
  if (value == null)
  {
    return NaN;
  }
  else
  {
    return value;
  }
}


/**
 * Returns true if a character isn't a lowercase character or
 * might not be a lowercase character.
 */
function isNotLowerCase()
{
  var charCode = alphaChar.charCodeAt(0);

  if (charCode > 0xFF)
  {
    // be lenient for non-ISO-Latin1
    return true;
  }
  else
  {    
    return !_isLowerCaseStrict(alphaChar);
  }
}


/**
 * Returns true if a character is a lowercase character or
 * might be a lowercase character.
 */
function isLowerCase(
  alphaChar
  )
{
  var charCode = alphaChar.charCodeAt(0);

  if (charCode > 0xFF)
  {
    // be lenient for non-ISO-Latin1
    return !isDigit(alphaChar);
  }
  else
  {    
    return _isLowerCaseStrict(alphaChar);
  }
}


function _isLowerCaseStrict(
  alphaChar
  )
{
  var charCode = alphaChar.charCodeAt(0);

  return (((charCode >= 0x61) && (charCode <= 0x7A)) || // "a-z"
          ((charCode >= 0xDF) && (charCode <= 0xFF)));  // iso-latin1 lowercase
}


/**
 * Returns true if a character is an uppercase character or might be an
 * uppercase character.
 */
function isUpperCase(
  alphaChar
  )
{
  var charCode = alphaChar.charCodeAt(0);
  
  if (charCode > 0xFF)
  {
    // be lenient for non-IS-Latin1
    return !isDigit(alphaChar);
  }
  else
  {
    return _isUpperCaseStrict(alphaChar);
  }
}


/**
 * Returns true if a character isn't an uppercase character or might not be an
 * uppercase character.
 */
function isNotUpperCase(
  alphaChar
  )
{
  var charCode = alphaChar.charCodeAt(0);
  
  if (charCode > 0xFF)
  {
    // be lenient for non-IS-Latin1
    return true;
  }
  else
  {
    return !_isUpperCaseStrict(alphaChar);
  }
}


/**
 * Returns true if a character is an uppercase character.
 */
function _isUpperCaseStrict(
  alphaChar
  )
{
  var charCode = alphaChar.charCodeAt(0);
  
  return (((charCode >= 0x41) && (charCode <= 0x5A)) || // "A-Z"
          ((charCode >= 0xC0) && (charCode <= 0xDe)));  // iso-latin1 lowercase
}


/**
 * Returns true if a character is a latter.
 */
function isLetter(
  alphaChar
  )
{
  // =-= bts not technically correct but hopefully OK for ISO-Latin1
  return isLowerCase(alphaChar) | isUpperCase(alphaChar);
}


function getUserLanguage()
{
  var language = _locale;

  if (language == null)
  {
    // try this the IE way
    language =  window.navigator.userLanguage;
    
    if (language == null)
    {
      // try this the Netscape way
      language = window.navigator.language;
    }
  }
  
  // return language;
  return language;
}


function getJavaLanguage(
  javascriptLang
  )
{
  // default to the user language if no language is passed in
  if (javascriptLang == null)
  {
    javascriptLang = getUserLanguage();
  }
      
  // look for first dash, the territory appears after the dash
  var territoryIndex = javascriptLang.indexOf("-", 0);
  
  // no dash found, so the name is just a language;
  if (territoryIndex == -1)
    return javascriptLang;
  
  var inLength = javascriptLang.length;
  var javaLang = javascriptLang.substring(0, territoryIndex);
  
  javaLang += "_";
  
  territoryIndex++;
  
  var variantIndex = javascriptLang.indexOf("-", territoryIndex);
  
  if (variantIndex == -1)
  {
    // we have no variant
    variantIndex = inLength;
  }
  
  var territoryString = javascriptLang.substring(territoryIndex,
                                                 variantIndex);
                                                 
  javaLang += territoryString.toUpperCase();
  
  // we have a variant, so add it
  if (variantIndex != inLength)
  {
    javaLang += "_";
    javaLang += javascriptLang.substring(variantIndex + 1,
                                         inLength);
  }
    
  return javaLang;
}
 

function getLocaleSymbols(
  jsLocaleString
  )
{  
  var suffix = getJavaLanguage(jsLocaleString);
    
  //
  // look for our localeSymbols, from most specific to least
  // specific.  Unfortunately, this will only work if the
  // less specific library has already been loaded.
  //
  while(true)
  {    
    var localeSymbols = window["LocaleSymbols_" + suffix];
    
    if (localeSymbols != null)
    {
      return localeSymbols;
    }
    else
    {
      var previousIndex = suffix.lastIndexOf("_");
      
      if (previousIndex != -1)
      {
        suffix = suffix.substring(0, previousIndex);
      }
      else
      {
        break;
      }
    }
  }
}


function _getEras()
{
  return this.getLocaleElements()["Eras"];
}

function _getMonths()
{
  return this.getLocaleElements()["MonthNames"];
}

function _getShortMonths()
{
  return this.getLocaleElements()["MonthAbbreviations"];
}

function _getWeekdays()
{
  return this.getLocaleElements()["DayNames"];
}

function _getShortWeekdays()
{
  return this.getLocaleElements()["DayAbbreviations"];
}

function _getAmPmStrings()
{
  return this.getLocaleElements()["AmPmMarkers"];
}

function _getZoneStrings()
{
  return this.getLocaleElements()["zoneStrings"];
}

function _getLocalPatternChars()
{
  return this.getLocaleElements()["localPatternChars"];
}


function _getDecimalSeparator()
{
  if (_decimalSep != null)
    return _decimalSep;

  return this.getLocaleElements()["NumberElements"][0];
}

function _getGroupingSeparator()
{
  if (_groupingSep != null)
    return _groupingSep;

  return this.getLocaleElements()["NumberElements"][1];
}

function _getPatternSeparator()
{
  return this.getLocaleElements()["NumberElements"][2];
}

function _getPercent()
{
  return this.getLocaleElements()["NumberElements"][3];
}

function _getZeroDigit()
{
  return this.getLocaleElements()["NumberElements"][4];
}

function _getDigit()
{
  return this.getLocaleElements()["NumberElements"][5];
}

function _getMinusSign()
{
  return this.getLocaleElements()["NumberElements"][6];
}

function _getExponential()
{
  return this.getLocaleElements()["NumberElements"][7];
}

function _getPerMill()
{
  return this.getLocaleElements()["NumberElements"][8];
}

function _getInfinity()
{
  return this.getLocaleElements()["NumberElements"][9];
}

function _getNaN()
{
  return this.getLocaleElements()["NumberElements"][10];
}

function _getCurrencySymbol()
{
  return this.getLocaleElements()["CurrencyElements"][0];
}
function _getCurrencyCode()
{
  return this.getLocaleElements()["CurrencyElements"][1];
}
function _getPositivePrefix()
{
  return this.getLocaleElements()["CurrencyElements"][2];
}
function _getPositiveSuffix()
{
  return this.getLocaleElements()["CurrencyElements"][3];
}
function _getNegativePrefix()
{
  return this.getLocaleElements()["CurrencyElements"][4];
}
function _getNegativeSuffix()
{
  return this.getLocaleElements()["CurrencyElements"][5];
}

function _getLocaleElements()
{
  return this["LocaleElements"];
}

function _getFullTimePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][0];
}

function _getLongTimePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][1];
}

function _getMediumTimePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][2];
}

function _getShortTimePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][3];
}

function _getFullDatePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][4];
}

function _getLongDatePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][5];
}

function _getMediumDatePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][6];
}

function _getShortDatePatternString()
{
  return this.getLocaleElements()["DateTimePatterns"][7];
}

function _getDateTimeFormatString()
{
  return this.getLocaleElements()["DateTimePatterns"][8];
}


function LocaleSymbols(
  localeElements
  )
{
  this["LocaleElements"] = localeElements;
}

LocaleSymbols.prototype.getFullTimePatternString = _getFullTimePatternString;
LocaleSymbols.prototype.getLongTimePatternString = _getLongTimePatternString;
LocaleSymbols.prototype.getMediumTimePatternString = _getMediumTimePatternString;
LocaleSymbols.prototype.getShortTimePatternString = _getShortTimePatternString;
LocaleSymbols.prototype.getFullDatePatternString = _getFullDatePatternString;
LocaleSymbols.prototype.getLongDatePatternString = _getLongDatePatternString;
LocaleSymbols.prototype.getMediumDatePatternString = _getMediumDatePatternString;
LocaleSymbols.prototype.getShortDatePatternString = _getShortDatePatternString;
LocaleSymbols.prototype.getDateTimeFormatString = _getDateTimeFormatString;

LocaleSymbols.prototype.getEras = _getEras;
LocaleSymbols.prototype.getMonths = _getMonths;
LocaleSymbols.prototype.getShortMonths = _getShortMonths;
LocaleSymbols.prototype.getWeekdays = _getWeekdays;
LocaleSymbols.prototype.getShortWeekdays = _getShortWeekdays;
LocaleSymbols.prototype.getAmPmStrings = _getAmPmStrings;
LocaleSymbols.prototype.getZoneStrings = _getZoneStrings;
LocaleSymbols.prototype.getLocalPatternChars = _getLocalPatternChars;

LocaleSymbols.prototype.getDecimalSeparator = _getDecimalSeparator;
LocaleSymbols.prototype.getGroupingSeparator = _getGroupingSeparator;
LocaleSymbols.prototype.getPatternSeparator = _getPatternSeparator;
LocaleSymbols.prototype.getPercent = _getPercent;
LocaleSymbols.prototype.getZeroDigit = _getZeroDigit;
LocaleSymbols.prototype.getDigit = _getDigit;
LocaleSymbols.prototype.getMinusSign = _getMinusSign;
LocaleSymbols.prototype.getExponential = _getExponential;
LocaleSymbols.prototype.getPerMill = _getPerMill;
LocaleSymbols.prototype.getInfinity = _getInfinity;
LocaleSymbols.prototype.getNaN = _getNaN;
LocaleSymbols.prototype.getCurrencySymbol = _getCurrencySymbol;
LocaleSymbols.prototype.getCurrencyCode   = _getCurrencyCode;
LocaleSymbols.prototype.getPositivePrefix = _getPositivePrefix;
LocaleSymbols.prototype.getPositiveSuffix = _getPositiveSuffix;
LocaleSymbols.prototype.getNegativePrefix = _getNegativePrefix;
LocaleSymbols.prototype.getNegativeSuffix = _getNegativeSuffix;
LocaleSymbols.prototype.getLocaleElements = _getLocaleElements;

/**
 * ConverterHint "interface" for a client side TrConverter instance.
 * The ConverterHint "interface" is for guiding a user on the desired format to ensure
 * that no converter exceptions are thrown.
 *
 */
function TrConverterHint()
{
  // for debugging
  this._class = "TrConverterHint";
}

/**
 * Returns a hint for the used converter, which format is
 * expected by the ui component.
 */
TrConverterHint.prototype.getFormatHint = function(){}

/**
 * ValidatorHint "interface" for a client side TrValidator instance.
 * The ValidatorHint "interface" is to guide a user when entering a
 * value to an input component, to ensure that no validator exceptions is thrown.
 */
function TrValidatorHint()
{
  // for debugging
  this._class = "TrValidatorHint";
}

/**
 * Since an implementation of this "interface" can have multiple
 * hints available, we return all available hint messages in an JavaScript Array.
 * 
 * @param converter converter is passed to this method, because sometimes a default converter is used
 * and the validator, implementing this interface, shouldn't need to figure out
 * anything about that
 */
TrConverterHint.prototype.getHints = function(converter){}


/**
 * Converter "interface" similar to javax.faces.convert.Converter,
 * except that all relevant information must be passed to the constructor
 * as the context and component are not passed to the getAsString or getAsObject method 
 *
 */
function TrConverter()
{
  // for debugging
  this._class = "TrConverter";
}

/**
 * Convert the specified model object value, into a String for display
 *
 * @param value Model object value to be converted 
 * @param label label to identify the editableValueHolder to the user 
 * 
 * @return the value as a string or undefined in case of no converter mechanism is
 * available (see TrNumberConverter).
 */
TrConverter.prototype.getAsString = function(value, label){}

/**
 * Convert the specified string value into a model data object 
 * which can be passed to validators
 *
 * @param value String value to be converted 
 * @param label label to identify the editableValueHolder to the user 
 * 
 * @return the converted value or undefined in case of no converter mechanism is
 * available (see TrNumberConverter).
 */
TrConverter.prototype.getAsObject = function(value, label){}

/**
 * Validator "interface" similar to javax.faces.validator.Validator,
 * except that all relevant information must be passed to the constructor
 * as the context and component are not passed to the validate method 
 *
 */
function TrValidator()
{
  // for debugging
  this._class = "TrValidator";
}

/**
 * Perform the correctness checks implemented by this Validator. 
 * If any violations are found, a TrValidatorException will be thrown 
 * containing the TrFacesMessage describing the failure. 
 * @param value value to be validated 
 * @param label label to identify the editableValueHolder to the user
 * @param converter converter to format error string properly
 */
TrValidator.prototype.validate = function(value, label, converter){}


/** 
 * TrConverterException is an exception thrown by the getAsObject() or getAsString() 
 * method of a TrConverter, to indicate that the requested conversion cannot be performed.
 *
 * @param facesMessage the TrFacesMessage associated with this exception
 * @param summary Localized summary message text, used only if facesMessage is null
 * @param detail Localized detail message text, used only if facesMessage is null
 */
function TrConverterException(
  facesMessage, 
  summary,
  detail
  )
{
  
  if (facesMessage == null)
  {
      this._facesMessage = new TrFacesMessage(summary, 
                                            detail,
                                            TrFacesMessage.SEVERITY_ERROR);
  }
  else
  {
    this._facesMessage = facesMessage;
  }      
    
  
}

/**
 * Returns the TrFacesMessage associated with the exception.
 */
TrConverterException.prototype.getFacesMessage = 
    function()
    {
      return this._facesMessage;
    }



/**
 * A TrValidatorException is an exception thrown by the validate() method of 
 * a Validator to indicate that validation failed.
 *
 * @param facesMessage the TrFacesMessage associated with this exception
 * @param summary Localized summary message text, used only if facesMessage is null
 * @param detail Localized detail message text, used only if facesMessage is null
 */
function TrValidatorException(
  facesMessage,
  summary, 
  detail
  )
{
  
  if (facesMessage == null)
  {
      this._facesMessage = new TrFacesMessage(summary, 
                                            detail,
                                            TrFacesMessage.SEVERITY_ERROR);
  }
  else
  {
    this._facesMessage = facesMessage;
  }      
}


/**
 * Returns the TrFacesMessage associated with the exception.
 */
TrValidatorException.prototype.getFacesMessage = 
  function()
  {
    return this._facesMessage;
  }

/**
 * Message similar to javax.faces.application.FacesMessage
 *
 * @param summary - Localized summary message text
 * @param detail - Localized detail message text 
 * @param severity - An optional severity for this message.  Use constants
 *                   SEVERITY_INFO, SEVERITY_WARN, SEVERITY_ERROR, and
 *                   SEVERITY_FATAL from the FacesMessage class.  Default is
 *                   SEVERITY_INFO
 */
function TrFacesMessage(
  summary,
  detail,
  severity
  )
{
  this._summary = summary;
  this._detail = detail;
  
  if(severity == null)
  {
    this._severity = TrFacesMessage.SEVERITY_INFO;
  }
  else
  {
    this._severity = severity;
  }
}

TrFacesMessage.SEVERITY_INFO    = 0;
TrFacesMessage.SEVERITY_WARN    = 1;
TrFacesMessage.SEVERITY_ERROR   = 2;
TrFacesMessage.SEVERITY_FATAL   = 3;

TrFacesMessage._SEVERITY_DEFAULT = TrFacesMessage.SEVERITY_INFO;
 
TrFacesMessage.prototype.getDetail = 
  function()
  {
    return this._detail;
  }
TrFacesMessage.prototype.getSummary = 
  function()
  {
    return this._summary;
  }
TrFacesMessage.prototype.setDetail = 
  function(
    detail
    )
  {
    this._detail = detail;
  }
TrFacesMessage.prototype.setSummary = 
  function(
    summary
    )
  {
    this._summary = summary;
  }

TrFacesMessage.prototype.getSeverity =
  function()
  {
    return this._severity;
  }
    
TrFacesMessage.prototype.setSeverity =
  function(
    severity
  )
  {
    this._severity = severity;
  }


/**
 * TrFastMessageFormatUtils is a greatly reduced version
 * of the java.text.MessageFormat class, but delivered as a utility. 
 * <p>
 * The only syntax supported by this class is simple index-based
 * replacement, namely:
 * <pre>
 *     some{1}text{0}here{2}andthere
 * </pre>
 * as well as escaping using single quotes.  Like MessageFormat,
 * a single quote must be represented using two consecutive single
 * quotes, but the contents of any text between single quotes
 * will not be interpreted.  So, the following pattern could
 * be used to include a left bracket:
 * <pre>
 *     some'{'text{0}
 * </pre>
 */
var TrFastMessageFormatUtils = new Object();

 /**
  * Formats the given array of strings based on the initial
  * pattern.  
  * @param {String} String to format
  * @param {any...:undefined} Varargs objects to substitute for positional parameters.
  * Each parameter will be converted to a String and substituted into the format.
  */
TrFastMessageFormatUtils.format = function(
  formatString, // error format string with embedded indexes to be replaced
  parameters    // {any...:undefined} Varargs objects to substitute for positional parameters.
  )
{
  // I need to create an array here because I have to strip the first arg
  var tempArray = new Array();
  
  for ( var i = 1; i < arguments.length; i++)
  {
    tempArray[i -1] = arguments[i];
  }
  
  // TODO - move the code of the function below into here after 
  //        simplifying it
  return _formatErrorString(formatString, tempArray);
}

var TrMessageFactory = new Object();

TrMessageFactory.createFacesMessage = function(
  key,
  customDetail,
  parameters,
  messageSeverity
  )
{  
  // the strings to create a facesMessage to use have been sent down
  var summary = TrMessageFactory.getSummaryString(key);       
  var detail = customDetail;
  var severity = messageSeverity;
  
  if ( severity == null)
  {
    severity = TrFacesMessage.SEVERITY_ERROR
  }
  
  if (detail == null)
  {
    detail =  TrMessageFactory.getDetailString(key); 
  }
  
  if ( detail != null )
  {
    if ( parameters != null )
    {
      detail = TrFastMessageFormatUtils.format(detail,parameters);
    }
  }
    
  return new TrFacesMessage( summary, detail, severity);
}

TrMessageFactory.getSummaryString = function(
  key
  )
{
  if (key == null)
    return null;
  return TrMessageFactory._TRANSLATIONS[key];
}

TrMessageFactory.getDetailString = function(
  key
  )
{
  if (key == null)
    return null;
    
  // TODO should I be doing string concat here, or have a map of key -> detailKey?
  return TrMessageFactory._TRANSLATIONS[key+"_detail"];
}

TrMessageFactory.getString = function(
  key
  )
{
  return TrMessageFactory.getSummaryString(key);
}

TrMessageFactory.createMessage = function(
  key,
  param1,
  param2
  )
{  
  // the strings to create a facesMessage to use have been sent down
  var message = TrMessageFactory.getSummaryString(key);       
  if ( message != null )
  {
    message = TrFastMessageFormatUtils.format(message, param1, param2);
  }
    
  return message;
}
TrMessageFactory.createCustomMessage = function(
  customMessage,
  param1,
  param2
  )
{
  // the strings to create a facesMessage to use have been sent down
  var message;
  if ( customMessage != null )
  {
    message = TrFastMessageFormatUtils.format(customMessage, param1, param2);
  }
  return message;
}