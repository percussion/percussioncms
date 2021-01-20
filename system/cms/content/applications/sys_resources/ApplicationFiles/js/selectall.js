// select all functions for controls.

function PSOselectAll(field)
{
for (i = 0; i < field.options.length; i++)
   field.options[i].selected = true ;
}

function PSOunselectAll(field)
{
for (i = 0; i < field.options.length; i++)
   field.options[i].selected = false ;
}




function PSOcheckAll(field)
{
for (i = 0; i < field.length; i++)
   field[i].checked = true ;
}

function PSOuncheckAll(field)
{
for (i = 0; i < field.length; i++)
   field[i].checked = false ;
}
