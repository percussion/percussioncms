function getTestFunctionNames()
{
    var testFunctionNames = new Array();
    var i;

    if (typeof(document.scripts) != 'undefined' &&
		document.scripts.length > 0) { // IE5 and up
		var scripts = document.scripts;

		for (i = 0; i < scripts.length; i++) {
			var someNames = _extractTestFunctionNamesFromScript(scripts[i]);
			if (someNames)
				testFunctionNames = testFunctionNames.concat(someNames);
		}
	}
	else {
		for (i in self) {
			if (i.substring(0, 4) == 'test' && typeof(self[i]) == 'function')
				push(testFunctionNames, i);
		}
    }

    return testFunctionNames;
}

function _extractTestFunctionNamesFromScript(aScript)
{
    var result;
    var remainingScriptToInspect = aScript.text;
    var currentIndex = _indexOfTestFunctionIn(remainingScriptToInspect);
    while (currentIndex != -1) {
        if (!result)
            result = new Array();

        var fragment = remainingScriptToInspect.substring(currentIndex, remainingScriptToInspect.length);
        result = result.concat(fragment.substring('function '.length, fragment.indexOf('(')));
        remainingScriptToInspect = remainingScriptToInspect.substring(currentIndex + 12, remainingScriptToInspect.length);
        currentIndex = this._indexOfTestFunctionIn(remainingScriptToInspect);
    }
    return result;
}

function _indexOfTestFunctionIn(string)
{
    return string.indexOf('function test');
}

var ___detailArray = new Array();

function ___psRunTest()
{
___detailArray = new Array();
var failure = false;
var tests = getTestFunctionNames();
var starttime = new Date().getMilliseconds();
for(i = 0; i < tests.length; i++)
{
  document.forms['___testRunner'].___testDetail.options[i] =
     new Option(tests[i], tests[i]);
  try
  {
     var func = tests[i] + "()";
     eval(func);
     ___detailArray.push("success");
  }
  catch(e)
  {
     var msg = "";
		 if(e.comment != null)
		    msg += e.comment + "\n\n";
		 msg += e.jsUnitMessage + "\n\n";
		 msg += "StackTrace:\n";
     msg += e.stackTrace + "\n";
     ___detailArray.push(msg);
    failure = true;
  }
}
var endtime = new Date().getMilliseconds();
if(failure)
{
  document.forms['___testRunner'].___status.value = "Failure";
}
else
{
  document.forms['___testRunner'].___status.value = "Success";
}
document.forms['___testRunner'].___time.value = endtime - starttime;

}

function ___showDetail()
{
var idx = document.forms['___testRunner'].___testDetail.selectedIndex;
if(idx == -1)
  return;
document.forms['___testRunner'].___detail.value = ___detailArray[idx];
}

function psAddSimpleTestRunner()
{
document.open();
document.writeln('<form name="___testRunner">');
document.writeln('<h3>Simple Test Runner</h3>');
document.writeln('<input type="button" onclick="___psRunTest()" value="Run Tests"/>');
document.writeln('<br><br>');
document.writeln('<b>Status:<b>&nbsp;<input type="text" onfocus="blur()" id="___status" name="___status">');
document.writeln('<br>');
document.writeln('<b>Execution Time (milliseconds):<b>&nbsp;<input type="text" onfocus="blur()" id="___time" name="___time">');
document.writeln('<br><br>');
document.writeln('<select size="20" style="width:400px" id="___testDetail" name="___testDetail" onchange="___showDetail()">');
document.writeln('</select>');
document.writeln('<textarea name="___detail" id="___detail" cols="40" rows="19">');
document.writeln('</textarea>');
document.writeln('</form>');
document.close();

}

var MAX_DUMP_DEPTH = 10;



function dumpObj(obj, name, indent, depth) 
{

  if (depth > MAX_DUMP_DEPTH) {

		 return indent + name + ": <Maximum Depth Reached>\n";

  }

  if (typeof obj == "object") {

		 var child = null;

		 var output = indent + name + "\n";

		 indent += "\t";

		 for (var item in obj)

		 {

			   try {

					  child = obj[item];

			   } catch (e) {

					  child = "<Unable to Evaluate>";

			   }

			   if (typeof child == "object") {

					  output += dumpObj(child, item, indent, depth + 1);

			   } else {

					  output += indent + item + ": " + child + "\n";

			   }

		 }

		 return output;

  } else {

		 return obj;

  }

}
