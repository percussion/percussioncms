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

//FIXME: grab from a translation JS
var _shuttle_no_name = "You must supply the shuttle's name to create a proxy";
var _shuttle_no_form_name_provided = "A form name must be provided";
var _shuttle_no_form_available = "This shuttle is not in a form";


//========================================================================
//
// (Public) TrShuttleProxy Object
//

// TrShuttleProxy instances can be used to determine information about
// a shuttle at runtime and gain access to the data it contains.
// A TrShuttleProxy can be constructed from just the name of the shuttle,
// or the shuttle name and its form name. If the form name is not
// supplied, it will be determined at runtime.


// Constructor
function TrShuttleProxy(
  shuttleName,
  formName
  )
{
  if (shuttleName == (void 0))
  {
    alert(_shuttle_no_name);
    this.shuttleName = "";
    this.formName = "";
    return;
  }

  this.shuttleName = shuttleName;

  this.formName = "";
  // get the form name
  if (formName == (void 0))
  {
    // find the first form with this shuttle
    var formsLength = document.forms.length;
    var listName = shuttleName + ":leading";

    for (var i = 0; i < formsLength; i++)
    {
      if (document.forms[i][listName] != (void 0))
      {
        this.formName = document.forms[i].name;
        break;
      }
    }
    if(this.formName == "")
    {
      alert(shuttle_no_form_available);
      return;
    }
  }
  else
  {
    this.formName = formName;
  }
}

//
// Public prototype method hookups
//

//
// Calling getItems(boolean leadingList) on a TrShuttleProxy will return an
// array of the items in the given list of the shuttle
//
TrShuttleProxy.prototype.getItems = function(
  leadingList
  )
{
  //default params not given
  if(leadingList == (void 0))
  {
    leadingList = true;
  }

  //get the list name
  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);


  var list = document.forms[this.formName].elements[listName];

  var items = new Array();
  //length - 1 because of bars
  for(var i=0; i<list.length-1; i++)
  {
    items[i] = list.options[i];
  }

  return items;
};

//
// Calling getSelectedItems(boolean leadingList) on a TrShuttleProxy will
// return an array of the selected items in the given list of the shuttle
//
TrShuttleProxy.prototype.getSelectedItems = function(
  leadingList
  )
{
  //default params not given
  if(leadingList == (void 0))
  {
    leadingList = true;
  }

  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);


  var list = document.forms[this.formName].elements[listName];

  var items = new Array();
  var j = 0;
  //length - 1 because of bars
  for(var i=0; i<list.length-1; i++)
  {
    if(list.options[i].selected)
    {
      items[j] = list.options[i];
      j++;
    }
  }

  return items;
};

//
// Calling getItemCount(boolean leadingList) on a TrShuttleProxy will
// return the number of items in the given list of the shuttle
//
TrShuttleProxy.prototype.getItemCount = function(
  leadingList
  )
{
  //default params not given
  if(leadingList == (void 0))
  {
    leadingList = true;
  }

  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);


  //minus 1 for bars
  return document.forms[this.formName].elements[listName].length - 1;
};


//
// Calling getSelectedItemCount(boolean leadingList) on a TrShuttleProxy will
// return the number of selected items in the given list of the shuttle
//
TrShuttleProxy.prototype.getSelectedItemCount = function(
  leadingList
)
{
  //default params not given
  if(leadingList == (void 0))
  {
    leadingList = true;
  }

  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);


  var list = document.forms[this.formName].elements[listName];

  var j = 0;
  //length - 1 because of bars
  for(var i=0; i<list.length-1; i++)
  {
    if(list.options[i].selected)
    {
       j++;
    }
  }

  return j;
};

//
// Calling addItem(boolean leadingList, int index, string text, string value, string description)
// on a TrShuttleProxy will insert a new option into the given list at the
// specified index with the specified text, value, and description.
//
TrShuttleProxy.prototype.addItem = function(
  leadingList,
  index,
  text,
  value,
  description
  )
{
  //default params not given
  if(value == (void 0))
  {
    value = "";
  }
  if(text == (void 0))
  {
    text = "";
  }
  if(description == (void 0))
    {
      description = "";
  }
  if(leadingList == (void 0))
  {
    leadingList = true;
  }


  //get the list
  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);



  //get an appropriate index
  if(index == (void 0))
  { //minus 1 for bars
    index = document.forms[this.formName].elements[listName].length - 1;
  }

  if(index < 0)
  {
    index = 0;
  }

  //minus 1 for bars
  if(index > document.forms[this.formName].elements[listName].length - 1)
  {
    index = document.forms[this.formName].elements[listName].length - 1;
  }

  //first move all items at that index and below down to make room for this
  //new item.
  var theList = document.forms[this.formName].elements[listName];

  //make a new option for the bars
  theList.options[theList.length] =
       new Option(theList.options[theList.length-1].text,
                  theList.options[theList.length-1].value,
                  false,
                  false);

  //move items down
  for(var i = theList.length - 1; i > index; i--)
  {
    theList.options[i].text = theList.options[i-1].text;
    theList.options[i].value = theList.options[i-1].value;
    theList.options[i].selected = theList.options[i-1].selected;
  }

  //insert the new item
  theList.options[index].text = text;
  theList.options[index].value = value;
  theList.options[index].selected = false;

  // add description
  var descArray = TrShuttleProxy._getDescArray(listName);
  TrShuttleProxy._addDescAtIndex( descArray, description, index);

  TrShuttleProxy._makeList(this.formName, listName);
};

//
// Calling deleteItemByValue(boolean leadingList, string value)
// on a TrShuttleProxy will delete the option with the given value
// from the given list.
//
TrShuttleProxy.prototype.deleteItemByValue = function(
  leadingList,
  value
  )
{
  if(value == (void 0))
  {
    return;
  }


  //get the list
  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);
  var theList = document.forms[this.formName].elements[listName];

  for(var i=0; i<theList.length-1; i++)
  {
    var val = theList.options[i].value;
    if(val == value)
    {
      var descArray = TrShuttleProxy._getDescArray( listName );
      TrShuttleProxy._deleteDescAtIndex( descArray, i);
      TrShuttleProxy._clearDescAreas(this.formName, listName);

      theList.options[i] = null;
      TrShuttleProxy._makeList(this.formName, listName);

      return;
    }
  }

};


//
// Calling deleteSelectedItems(boolean leadingList)
// on a TrShuttleProxy will delete all selected items from the given list
//
TrShuttleProxy.prototype.deleteSelectedItems = function(
  leadingList
  )
{
  if(leadingList == (void 0))
  {
    leadingList = true;
  }


  //get the list
  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);
  var theList = document.forms[this.formName].elements[listName];

  var selIndexes = TrShuttleProxy._getSelectedIndexes(this.formName,listName);

  for(var i = selIndexes.length; i >=0; i--)
  {
    theList.options[selIndexes[i]] = null;
  }

  var descArray = TrShuttleProxy._getDescArray(listName);

  TrShuttleProxy._deleteDescAtIndexes( descArray, selIndexes);

  TrShuttleProxy._clearDescAreas(this.formName, listName);

  TrShuttleProxy._makeList(this.formName, listName);
};

//
// Calling move(boolean fromLeadingList, boolean allItems) on a TrShuttleProxy
// will move the selected items, or all items depending on allItems parameter,
// from the given list to the other list.
//
TrShuttleProxy.prototype.move = function(
  fromLeadingList,
  allItems
  )
{
  //default parameters not given
  if(allItems == (void 0))
  {
    allItems = false;
  }
  if(fromLeadingList == (void 0))
  {
    fromLeadingList = true;
  }

  //get the list names
  var fromListName = TrShuttleProxy._getListName(this.shuttleName, fromLeadingList);
  var toListName = TrShuttleProxy._getListName(this.shuttleName, !fromLeadingList);

  //move the items
  if(allItems)
  {
    TrShuttleProxy._moveAllItems(fromListName, toListName, this.formName);
  }
  else
  {
    TrShuttleProxy._moveItems(fromListName, toListName, this.formName);
  }
};

//
// Calling reorderList(boolean down, boolean allTheWay, boolean leadingList) on
// a TrShuttleProxy will move the selected items in the second list of the proxy
//in the direction specified.  If allTheWay is true it will move the items all
// the way to the top or bottom.  Otherwise the items move one slot.
//
TrShuttleProxy.prototype.reorderList = function(
  down,
  allTheWay,
  leadingList
)
{
  //default params not given
  if(leadingList == (void 0))
  {
    leadingList = true;
  }
  if(allTheWay == (void 0))
  {
    allTheWay = false;
  }
  if(down == (void 0))
  {
    down = false;
  }

  //get the listName
  var listName = TrShuttleProxy._getListName(this.shuttleName, leadingList);



  //reorder the list
  if(!allTheWay)
  {
    TrShuttleProxy._orderList(down, listName, this.formName);
  }
  else
  {
    TrShuttleProxy._orderTopBottomList(down, listName, this.formName);
  }
};


//
// Calling reset will reset the shuttle to its initial state.
//
TrShuttleProxy.prototype.reset = function()
{
  TrShuttleProxy._resetItems( this.shuttleName, this.formName);
};



/*===========================================================================*/
/*-----------------------------------------------------------------------
 *PRIVATE SHUTTLE METHODS
 *-----------------------------------------------------------------------*/


/*
 * _remove
 *
 * This function removes the number of elements specified
 * by deleteCount from an array starting at the index given
 * by start
 */

TrShuttleProxy._remove = function( array, start, deleteCount )
{

  var len = array.length;

  if (deleteCount > len)
    return;

  for ( var i = start; i < len ; i++)
  {

    if ( i < len - deleteCount )
      array[i] = array[ i + deleteCount];
    else
      array[ i ] = void 0;
  }

  array.length = len - deleteCount;

}


/*
 * _displayDesc
 *
 * Displays the description in the description area below the list.
 *
 */
TrShuttleProxy._displayDesc = function(
  listName,
  formName
  )
{


  if(formName == (void 0))
  {
    alert(_shuttle_no_form_name_provided);
    return;
  }


  if(formName.length == 0)
  {
    alert(shuttle_no_form_available);
    return;
  }

  // the textInput where descriptions are displayed
  var descArea = document.forms[formName].elements[ listName + ':desc'];

  if( descArea == void(0))
  {
    return;
  }


  // the array of descriptions
  var descArray  =  TrShuttleProxy._getDescArray( listName );

  if( descArray == (void 0) || descArray.length == 0)
  {
    return;
  }

  //get the indexes of the selected items
  var selItems = TrShuttleProxy._getSelectedIndexes(formName, listName);

  //if no items are selected, return
  if(selItems.length == 0)
  {
    descArea.value = "";
    TrShuttleProxy._setSelected( listName, selItems );
    return;
  }

  // get the last description selected
  var selOptDesc = TrShuttleProxy._getSelectedDesc( listName, descArray, selItems );

  // set the value of the description area to be the last selected item
  descArea.value = selOptDesc;

  // set which items are currently selected
  TrShuttleProxy._setSelected( listName, selItems );


}


/*
 * _getDescArray
 *
 * This function gets the description array
 */

TrShuttleProxy._getDescArray = function
(
  listName
)
{

  var descArray = window[listName.replace(':','_') + '_desc'];
  return descArray;

}

/*
 * _getSelectedDesc
 *
 * This function gets the last selected description. If a
 * user selects items using the control key, and they select
 * the item at index 4, then 10, and then 6, the descriptions
 * for 4, then 10, then 6 should be displayed.
 *
 * There is a local variable where what was previously selected
 * is kept and this is compared to what is currently selected.
 * These are compared to determine the last item selected.
 */

TrShuttleProxy._getSelectedDesc = function
(
  listName,
  descArray,
  selItems
)
{

  // get the array of the indexes of previously selected items
  var prevSelArray = TrShuttleProxy._getSelectedArray( listName );

  // if only one item is currently selected return its description
  if ( selItems.length == 1 )
    return descArray[selItems[0]];


  // if the difference between the number of items
  // previously selected and currently selected is
  // not equal to one return no description, otherwise
  // it is unclear which description to display
  if ( selItems.length - prevSelArray.length != 1 )
    return "";


  // find the index now selected that was not
  // previously selected and return the description
  // for the item at that index.
  for ( var i = 0; i < selItems.length; i++ )
  {
    if ( i >= prevSelArray.length || prevSelArray[i] != selItems[i] )
      return descArray[selItems[i]];
  }

  // return an empty string if all else fails
  return "";
}

/*
 * _getSelectedArray
 *
 * This function gets the array of indexes of
 * previously selected items
 *
 */


TrShuttleProxy._getSelectedArray = function
(
  listName
)
{
  var selected = window[listName.replace(':','_') + '_sel'];
  return selected;
}

/*
 * _setSelected
 *
 * This function sets the array of indexes of
 * previously selected items
 *
 */

TrShuttleProxy._setSelected = function
(
  listName,
  selected
)
{

  var selectedArray = TrShuttleProxy._getSelectedArray( listName );

  if ( selectedArray != (void 0) )
  {
    var len = selectedArray.length;
    TrShuttleProxy._remove( selectedArray, 0, len);

    for ( var i = 0; i < selected.length; i++ )
    {
      selectedArray[i] = selected[i];
    }

  }

}


/*
 * _addDescAtIndex
 *
 * This function adds a description at a given index.
 */

TrShuttleProxy._addDescAtIndex = function
(
  descArray,
  addedDesc,
  index
)
{


  if ( descArray != (void 0 ) )
  {
    var len = descArray.length;

    for ( var i = len - 1 ; i >= index; i-- )
    {
      descArray[i + 1] = descArray[i];
    }

    descArray[index] = addedDesc;
    descArray.length = len + 1;
  }
}

/*
 * _deleteDescAtIndex
 *
 * This function removes a description at a given index.
 */

TrShuttleProxy._deleteDescAtIndex = function
(
  descArray,
  index
)
{
  if ( descArray != (void 0 ))
    TrShuttleProxy._remove(descArray, index, 1);
}

/*
 * _deleteDescAtIndexes
 *
 * This function removes descriptions at given indexes.
 */

TrShuttleProxy._deleteDescAtIndexes = function
(
  descArray,
  indexes
)
{
  if ( descArray != (void 0 ))
  {
    for ( var i = indexes.length - 1; i >= 0; i--)
    {
      TrShuttleProxy._remove(descArray, indexes[i], 1);
    }
  }
}


/*
 * _deleteDescAtIndexes
 *
 * Sets the textInput areas displaying descriptions to
 * the empty string.
 */

TrShuttleProxy._clearDescAreas = function(
  formName,
  list1,
  list2
)
{
  // move descriptions and clear description area
  var descArea1 = document.forms[formName].elements[ list1 + ':desc'];
  var descArea2 = document.forms[formName].elements[ list2 + ':desc'];

  if( descArea1 != void(0))
  {
    descArea1.value = "";
  }

  if( descArea2 != void(0))
  {
    descArea2.value = "";
  }
}



/*
 * _moveItems
 *
 * This function moves the selected items in the 'from' list to the
 * 'to' list.  If no formName is supplied, the form is found when
 * this is called.  The items are inserted in the 'to' list
 * at the bottom. The 'from' and 'to' parameters should be the
 * list names(i.e.  "<shuttleName>:leading" or "<shuttleName>:trailing")
 */
TrShuttleProxy._moveItems = function(
  from,
  to,
  formName
  )
{
  //get the formName if needed
  if(formName == (void 0))
  {
    formName = TrShuttleProxy._findFormNameContaining(from);
  }

  if(formName.length == 0)
  {
    alert(shuttle_no_form_available);
    return;
  }

  //store the from and to lists
  var fromList = document.forms[formName].elements[from];
  var toList = document.forms[formName].elements[to];

  if ( fromList == (void 0 ) || toList == (void 0 ))
    return;


  //get all the indexes of the selected items
  var selItems = TrShuttleProxy._getSelectedIndexes(formName, from);

  //if no items are selected, return with alert.
  if(selItems.length == 0)
  {
    if (_shuttle_no_items_selected.length > 0)
      alert(_shuttle_no_items_selected);

    return;
  }


  var fromDescArray = TrShuttleProxy._getDescArray(from);
  var toDescArray = TrShuttleProxy._getDescArray(to);

  //set no selection on toList so it will only have new items selected.
  toList.selectedIndex = -1;

  //get the index in the toList to start inserting at.  Length-1 because of
  //bars.
  var insertAt = toList.length-1;

  //save bar text so you know how long it should be
  var barText = toList.options[insertAt].text;

  //insert the items at the end of the toList
  for(var i=0; i<selItems.length; i++)
  {
    var oText = fromList.options[selItems[i]].text;
    var oValue = fromList.options[selItems[i]].value;

    if(i == 0)
    { //replace the bars
      toList.options[insertAt].text = oText;
      toList.options[insertAt].value = oValue;
    }
    else
    {  //have to make new item
      toList.options[insertAt] = new Option(oText, oValue, false, false);
    }

    if ( toDescArray != (void 0) && fromDescArray != (void 0) )
      toDescArray[insertAt] = fromDescArray[selItems[i]];

    toList.options[insertAt].selected = true;
    insertAt++;
  }

  //insert a new bar at bottom of toList
  toList.options[insertAt] = new Option(barText, "", false, false);
  toList.options[insertAt].selected = false;

  //remove items from fromList.  do this backward to maintain indices
  for( var i=selItems.length-1; i >= 0; i--)
  {
    if ( fromDescArray != (void 0) )
      TrShuttleProxy._remove( fromDescArray, selItems[i], 1 );
    fromList.options[selItems[i]] = null;
  }

  //make no selected on fromList
  fromList.selectedIndex = -1;

  TrShuttleProxy._clearDescAreas( formName, from);
  TrShuttleProxy._displayDesc( to, formName );

  //make the new lists for submitting.
  TrShuttleProxy._makeList(formName, from);
  TrShuttleProxy._makeList(formName, to);
}

/*
 * _moveAllItems
 *
 * This function moves all the items in the 'from' list to the
 * 'to' list.  If no formName is supplied, the form is found when
 * this is called.  The items are inserted in the 'to' list
 * at the bottom. The 'from' and 'to' parameters should be the
 * list names(i.e.  "<shuttleName>:leading" or "<shuttleName>:trailing")
 */
TrShuttleProxy._moveAllItems = function(
  from,
  to,
  formName
  )
{
  //get the formName is needed
  if(formName == (void 0))
  {
    formName = TrShuttleProxy._findFormNameContaining(from);
  }

  //get the lists
  var fromList = document.forms[formName].elements[from];
  var toList = document.forms[formName].elements[to];

  //save the bar text for later use.
  var barText =
    toList.options[document.forms[formName].elements[to].length-1].text

  //get the index to start inserting at in the toList.  length-1 because of
  //bars
  var insertAt = toList.length-1;
  var fromDescArray = TrShuttleProxy._getDescArray(from);
  var toDescArray = TrShuttleProxy._getDescArray(to);

  //move the items
  if (fromList.length > 1)
  {
    //move all but the last (bars).
    var initialLength = fromList.length
    for(var i=0; i<initialLength-1; i++)
    {
      var oText = fromList.options[0].text;
      var oValue = fromList.options[0].value;
      fromList.options[0] = null;
      if(i == 0)
      { //replace the bars
        toList.options[insertAt].text = oText;
        toList.options[insertAt].value = oValue;
      }
      else
      { //make new option
        toList.options[insertAt] = new Option (oText,oValue,false,false);

      }

      if ( toDescArray != (void 0) && fromDescArray != (void 0) )
        toDescArray[insertAt] = fromDescArray[i];

      insertAt++;
    }

    //insert a new bar:
    toList.options[insertAt] = new Option(barText, "", false, false);
    toList.options[insertAt].selected = false;

    if ( fromDescArray != (void 0) )
    {
      var len = fromDescArray.length;
      TrShuttleProxy._remove(fromDescArray, 0, len);
    }

    //set no selection on both lists
    fromList.selectedIndex = -1;
    toList.selectedIndex = -1;

    TrShuttleProxy._clearDescAreas( formName, from, to );


    //make the lists for submission
    TrShuttleProxy._makeList(formName, from);
    TrShuttleProxy._makeList(formName, to);
  }
  else if (_shuttle_no_items.length > 0)
  {
    alert(_shuttle_no_items);
  }
}

/*
 * _orderList
 *
 * This function reorders the given list by shifting the selections in
 * the given direction.  If no formName is supplied, the form is found when
 * this is called. The 'list' parameter should be the
 * list name(i.e.  "<shuttleName>:leading" or "<shuttleName>:trailing")
 */
TrShuttleProxy._orderList = function(
  down,
  list,
  formName
  )
{
  //get the formName if needed
  if(formName == (void 0))
  {
    formName = TrShuttleProxy._findFormNameContaining(list);
  }

  //get the actual list
  var colList = document.forms[formName].elements[list];

  //get all the selected item indexes
  var selItems = TrShuttleProxy._getSelectedIndexes(formName, list);

  //if no items are selected, return with alert.
  if(selItems.length == 0)
  {
    if (_shuttle_no_items_selected.length > 0)
      alert(_shuttle_no_items_selected);

    return;
  }

  var descArray = TrShuttleProxy._getDescArray(list);

  // Start with the last selected index and move up, working by blocks
  var processed = selItems.length - 1;
  while (processed >= 0)
  {
    var lastInBlock = selItems[processed];
    var firstInBlock = lastInBlock;

    var tempIndex = processed;


    // find the first index in that block
    while ((tempIndex > 0) && ((selItems[tempIndex] -
                                selItems[tempIndex - 1]) == 1))
    {
      tempIndex--;
      firstInBlock--;
    }

    if (down == 0)
    {
      // move this block up
      // if we are at the top, do nothing
      if(firstInBlock != 0)
      {
        //get the text and value of the one space above the block
        var oText = colList.options[firstInBlock-1].text;
        var oValue = colList.options[firstInBlock-1].value;

        if ( descArray != (void 0) )
          var dValue = descArray[firstInBlock - 1];

        //move the block up one at a time
        for (var i = firstInBlock; i <= lastInBlock; i++)
        {
          colList.options[i-1].text = colList.options[i].text;
          colList.options[i-1].value = colList.options[i].value;
          colList.options[i-1].selected = true;

          if ( descArray != (void 0) )
            descArray[i-1] = descArray[i];
        }

         //put the info of the slot above the selection below it
        colList.options[lastInBlock].text = oText;
        colList.options[lastInBlock].value = oValue;
        colList.options[lastInBlock].selected = false;

        if ( descArray != (void 0) )
          descArray[lastInBlock] = dValue;
      }
    }
    else
    {
      // move this block down
      // if we are at the bottom, do nothing
      if(lastInBlock != colList.length-2)
      {
        //get the text and value of the one space below the block
        var oText = colList.options[lastInBlock+1].text;
        var oValue = colList.options[lastInBlock+1].value;

        if ( descArray != (void 0) )
          var dValue = descArray[lastInBlock+1];

         //move the block down one at a time
        for (var i = lastInBlock; i >= firstInBlock; i--)
        {
          colList.options[i+1].text = colList.options[i].text;
          colList.options[i+1].value = colList.options[i].value;
          colList.options[i+1].selected = true;

          if ( descArray != (void 0) )
            descArray[i+1] = descArray[i];
        }

         //put the info of the slot below the selection above it
        colList.options[firstInBlock].text = oText;
        colList.options[firstInBlock].value = oValue;
        colList.options[firstInBlock].selected = false;

        if ( descArray != (void 0) )
          descArray[firstInBlock] = dValue;
      }
    }

    processed = tempIndex - 1;
  }

  TrShuttleProxy._displayDesc( list, formName );

  //make the list for submission
  TrShuttleProxy._makeList(formName, list);
}

/*
 * _orderTopBottomList
 *
 * This function reorders the given list by shifting the selections all the way
 * in the given direction.  If no formName is supplied, the form is found when
 * this is called. The 'list' parameter should be the
 * list name(i.e.  "<shuttleName>:leading" or "<shuttleName>:trailing")
 */
TrShuttleProxy._orderTopBottomList = function(
  down,
  list,
  formName
  )
{
  //get the formname if needed
  if(formName == (void 0))
  {
    formName = TrShuttleProxy._findFormNameContaining(list);
  }

  //get the actual list
  var colList = document.forms[formName].elements[list];

  //get all the indexes of the items selected in the list
  var selItems = TrShuttleProxy._getSelectedIndexes(formName, list);

  //if no items are selected, return with alert.
  if(selItems.length == 0)
  {
    if (_shuttle_no_items_selected.length > 0)
      alert(_shuttle_no_items_selected);

    return;
  }

  var descArray = TrShuttleProxy._getDescArray(list);
  var moveDescArray = new Array();
  var selDescArray = new Array();

  var moveItemsText = new Array();
  var moveItemsValue = new Array();
  var moveItemsIndex = 0;
  if(down == 0)
  {
    //get an array of all the items we will have to displace in order
    var selItemsIndex = 0;
    var moveItemsIndex = 0;
    for(var colListIndex=0;
        colListIndex < selItems[selItems.length - 1];
        colListIndex++)
    {
      if(colListIndex != selItems[selItemsIndex])
      {
        moveItemsText[moveItemsIndex] = colList.options[colListIndex].text;
        moveItemsValue[moveItemsIndex] = colList.options[colListIndex].value;

        if (  descArray != (void 0) )
          moveDescArray[moveItemsIndex] = descArray[colListIndex];

        moveItemsIndex++
      }
      else
      {

        if ( descArray != (void 0) )
          selDescArray[selItemsIndex] = descArray[colListIndex];

        selItemsIndex++;

      }
    }

    if ( descArray != (void 0) )
      selDescArray[selItemsIndex] = descArray[colListIndex];


    //place items to move toward top of col
    for(var i = 0; i < selItems.length; i++)
    {
      colList.options[i].text = colList.options[selItems[i]].text;
      colList.options[i].value = colList.options[selItems[i]].value;
      colList.options[i].selected = true;

      if ( descArray != (void 0) )
        descArray[i] = selDescArray[i];
    }

    //place displaced items below
    for(var j = 0; j < moveItemsText.length; j++)
    {
      colList.options[i].text = moveItemsText[j];
      colList.options[i].value = moveItemsValue[j];
      colList.options[i].selected = false;

      if ( descArray != (void 0) )
        descArray[i] = moveDescArray[j];
      i++
    }



  }
  else
  {
    //get an array of all the items we will have to displace in order
    var selItemsIndex = 1;
    var moveItemsIndex = 0;

    if ( descArray != (void 0) )
      selDescArray[0] = descArray[selItems[0]];

    for(var colItemsIndex=selItems[0]+1;
        colItemsIndex <= colList.length-2;
        colItemsIndex++)
    {
      if((selItemsIndex == selItems.length) ||
         (colItemsIndex != selItems[selItemsIndex]))
      {
        moveItemsText[moveItemsIndex] = colList.options[colItemsIndex].text;
        moveItemsValue[moveItemsIndex] = colList.options[colItemsIndex].value;

        if ( descArray != (void 0) )
          moveDescArray[moveItemsIndex] = descArray[colItemsIndex];

        moveItemsIndex++;
      }
      else
      {
        if ( descArray != (void 0) )
          selDescArray[selItemsIndex] = descArray[colItemsIndex];

        selItemsIndex++;
      }
    }


    //place items to move toward bottom of col
    var j = colList.length - 2;
    for(var i = selItems.length-1; i >= 0; i--)
    {
      colList.options[j].text = colList.options[selItems[i]].text;
      colList.options[j].value = colList.options[selItems[i]].value;
      colList.options[j].selected = true;

      if ( descArray != (void 0) )
        descArray[j] = selDescArray[i];
      j--;
    }


    //place displaced items above
    for(var i = moveItemsText.length-1; i >= 0; i--)
    {
      colList.options[j].text = moveItemsText[i];
      colList.options[j].value = moveItemsValue[i];
      colList.options[j].selected = false;

      if ( descArray != (void 0) )
        descArray[j] = moveDescArray[i];
      j--
    }
  }

  TrShuttleProxy._displayDesc( list, formName );

  //make the list for submission
  TrShuttleProxy._makeList(formName, list);
}

// helper functions
TrShuttleProxy._getSelectedIndexes = function(
  formName,
  listName
  )
{
  var colList = document.forms[formName].elements[listName];
  var selItems = new Array();
  var selItemsIndex=0;
  //minus 1 for bars
  for(var colListIndex=0; colListIndex<colList.length-1; colListIndex++)
  {
    if(colList.options[colListIndex].selected)
    {
      selItems[selItemsIndex] = colListIndex;
      selItemsIndex++;
    }
  }

  return selItems;
}

TrShuttleProxy._findFormNameContaining = function(
  element
  )
{
  var formsLength = document.forms.length;

  for (var i = 0; i < formsLength; i++)
  {
    if (document.forms[i][element] != (void 0))
    {
      return document.forms[i].name;
    }
  }
  return "";
}

TrShuttleProxy._makeList = function(
  formName,
  listName
  )
{
  var list = document.forms[formName].elements[listName];

  if ( list == null )
    return;


  var val = "";
  for(var i=0; i< list.length - 1; i++)
  {
    if(list.options[i].value.length > 0)
    {
      val = val +
            TrShuttleProxy._trimString(list.options[i].value)
            + ';';
    }
    else
    {
      val = val +
            TrShuttleProxy._trimString(list.options[i].text)
            + ';';
    }
  }
  document.forms[formName].elements[listName+':items'].value = val;
}

TrShuttleProxy._trimString = function (
  str
  )
{
  var j = str.length - 1;
  if(str.charAt(j) != ' ')
  {
    return str;
  }
  while ((str.charAt(j) == ' ') && (j > 0))
  {
    j = j - 1;
  }
  str = str.substring(0, j+1);
  return str;
}

TrShuttleProxy._getListName = function(
  shuttleName,
  leadingList
  )
{
  var theListName = (leadingList) ? shuttleName + ":leading" :
                                  shuttleName + ":trailing";
  return theListName;
}


/**
* Reset items to their original values
*
*/
TrShuttleProxy._resetItems = function(
  shuttleName,
  formName)
{

  //get the formName if needed
  if(formName == (void 0))
  {
    formName = TrShuttleProxy._findFormNameContaining(from);
  }

  if(formName.length == 0)
  {
    alert(shuttle_no_form_available);
    return;
  }

  // get list names
  leadingListName = TrShuttleProxy._getListName( shuttleName, true);
  trailingListName = TrShuttleProxy._getListName( shuttleName, false);

  // get current lists
  var leadingList  = document.forms[formName].elements[leadingListName];
  var trailingList = document.forms[formName].elements[trailingListName];

  // get original lists
  var origLists = TrShuttleProxy._getOriginalLists(shuttleName, formName);
  var origLeadingList  = origLists.leading;
  var origTrailingList = origLists.trailing;

  // get original description arrays
  var origLeadingDescArray =  TrShuttleProxy._getDescArray(leadingListName);
  var origTrailingDescArray = TrShuttleProxy._getDescArray(trailingListName);

  // reset values of lists
  TrShuttleProxy._resetToOriginalList( origLeadingList, origLeadingDescArray, leadingList );
  TrShuttleProxy._resetToOriginalList( origTrailingList, origTrailingDescArray, trailingList );

  //make the new lists for submitting.
  TrShuttleProxy._makeList(formName, leadingListName);
  TrShuttleProxy._makeList(formName, trailingListName);

  // return that no reload necessary
  return false;
}



/*
 * _getOriginalLists
 *
 * This function gets a copy of the original lists list
 */
TrShuttleProxy._getOriginalLists = function
(
  shuttleName,
  formName
)
{

  var originalLists = window['_' + formName + '_' + shuttleName + '_orig'];
  return originalLists;

}

/**
 * Given the original list info,
 * reset the list and description info
 *
 */
TrShuttleProxy._resetToOriginalList = function
(
  origList,
  descArray,
  list
)
{

  // If the original list or new list are null, return
  if ( origList == (void 0) || list == (void 0) )
    return;

  // reset selectedIndex
  list.selectedIndex = origList.selectedIndex;

  var i = 0;

  for( ;i < origList.options.length;i++)
  {
    var oText            = origList.options[i].text;
    var oValue           = origList.options[i].value;
    var oDefaultSelected = origList.options[i].defaultSelected;
    var oSelected        = origList.options[i].selected;

    // =-= gc Replacing values for options that already exist
    // doesn't work properly on Mozilla 1.3. If you
    // uncomment the following it breaks on mozilla, but if you then
    // add "alert('hi');" at the end of this method, it does work!
    /*
    if(list.options[i] != (void 0 ))
    {
      list.options[i].text = oText;
      list.options[i].value = oValue;
      list.options[i].defaultSelected = oDefaultSelected;
      list.options[i].selected = oSelected;
    }
    else
    */
    {
      // make new option
      list.options[i] = new Option(oText, oValue,
                                   oDefaultSelected, oSelected);

      // Netscape 4 bug, selection using constructor not working,
      // Remove next 2 lines when we desupport Netscape 4.
      list.options[i].defaultSelected = oDefaultSelected;
      list.options[i].selected = oSelected;
    }

    // reset description
    if ( descArray != (void 0 ))
      descArray[i] = origList.descriptions[i];

  }



  var max = list.options.length - 1;

  // nulling out extra options from high end of array
  // because doing otherwise not working.
  while ( max >= i )
  {
    if ( descArray != (void 0 ))
      descArray[max] = null;

    list.options[max] = null;
    max--;
  }


 }


 /**
  * _copyLists
  *
  * Copy the lists and descriptions info to an object
  *
  *
  */
 TrShuttleProxy._copyLists = function( shuttleName, formName )
 {

   //get the formName if needed
   if(formName == (void 0))
   {
     formName = TrShuttleProxy._findFormNameContaining(from);
   }

   if(formName.length == 0)
   {
     alert(shuttle_no_form_available);
     return;
   }

   var origLists = new Object();
   origLists.leading = TrShuttleProxy._copyList( TrShuttleProxy._getListName( shuttleName, true), formName);
   origLists.trailing = TrShuttleProxy._copyList( TrShuttleProxy._getListName( shuttleName, false), formName);


   return origLists;
 }


 /**
  * _copyList
  *
  * copy the values in a single list
  */
 TrShuttleProxy._copyList = function( listName, formName )
 {
   if ( formName == (void 0 ) || listName == (void 0))
     return;

   var origList = document.forms[formName].elements[listName];

   if ( origList == null)
     return;

   var origDescs = TrShuttleProxy._getDescArray(listName);

   var copyList = new Object();


   copyList.selectedIndex = origList.selectedIndex;
   copyList.options = new Array();
   copyList.descriptions = new Array();

   for ( var i = 0; i < origList.options.length; i++ )
   {
     copyList.options[i] = new Option( origList.options[i].text,
                                       origList.options[i].value,
                                       origList.options[i].defaultSelected,
                                       origList.options[i].selected);

     // Netscape 4 bug, selection using constructor not working,
     // Remove next 2 lines when we desupport Netscape 4.
     copyList.options[i].defaultSelected = origList.options[i].defaultSelected;
     copyList.options[i].selected = origList.options[i].selected;

     if (origDescs != null )
       copyList.descriptions[i] = origDescs[i];
   }


   return copyList;
 }
