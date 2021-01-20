// convert the location href string parameters into a hash table
function PSHref2Hash(loc)
{

	if (loc == null)
		loc = window.location.href;
	
	var hash = new Array();
	var temp = loc.split("?");
	if(temp.length > 1){
	var params = temp[1].split("&");
		for (i=0; i<params.length; i++)
	{
		var tmp = params[i].split("=");
		hash[tmp[0]] = tmp[1];
	}
	}
	return hash;
}
					

// convert the passed in hash table into a new href based on the location href string
function PSHash2Href(hash, loc)
{
	if (loc == null)
		loc = window.location.href;

	var x = loc.split("?")[0] + "?";
	var first = true;
	for (i in hash)
	{
		//is it not weird???
		if(i.indexOf("toJSON")!=-1)
		{
			continue;
		}
		if (hash[i] != null)
		{
			if (!first)
				x += "&"
					
			x += i + "=" + hash[i];
			first = false;
		}
	}
	return x;
}

function PSGetParam(url, param)
{
	var i = url.indexOf(param + "=");
	if (i >= 0)
	{
		var s = i + param.length + 1;
		var e = url.indexOf("&", s);
		if (e == -1) 
		{
		    e = url.length;
		}
		return url.substring(s, e);
	}
	else
	{
		return null;
	}
}