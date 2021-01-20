function getobj(name)
{
	var obj = null;

	if (document.getElementById)
		obj = document.getElementById(name);
	
	else if (document.all)
		obj = document.all[name];
	
	else if (is_nav4) 
		obj = PSnetscape_getObj(document, name);
	
	else
		obj = getImg(name);

	return obj;		
}

function show(obj)
{
	obj.style.display = "block";
}

function hide(obj)
{
	obj.style.display = "none";
}

function ishidden(obj)
{
	return obj.style.display == 'none'
}

function toggle(el, name)
{
	var obj = getobj(name)
	if (ishidden(obj))
	{
		show(obj)
		el.firstChild.data = 'hide'
	}
	else
	{
		hide(obj)
		el.firstChild.data = 'show'
	}
}