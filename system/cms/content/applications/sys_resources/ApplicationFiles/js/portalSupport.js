function showActionPage(url)
{
  var w = window.open(url, 'actionpage',
		      'toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=550,height=550');
  if (w != null) w.focus();
}

// Set the sort column and submit the sorter form
//
// element the submitting anchor tag
// sortColumn is a value from 1 to the number of available fields
function sortOn(element, sortColumn)
{
	var sorter = findSorter(element); // Get the sorter form
	if (sorter == null)
	{
		alert("Couldn't find sorter form");
		return;
	}
	var type = sorter.name.substring(6);
	var sortColumnField = sorter["sortColumn" + type];
	if (sortColumnField == null)
	{
		alert("Couldn't find sortColumn hidden field");
	}
	sortColumnField.value = sortColumn;
	sorter.submit();
}

// Find the sort element by searching the hierarchy upward from the current
// element. Terminate when we run out or find the sorter.
//
// The current element to examine
function findSorter(element)
{
	if (element == null) return null;
	if (element.name != null && element.name.indexOf('sorter') >= 0) return element;
	return findSorter(element.parentNode);
}