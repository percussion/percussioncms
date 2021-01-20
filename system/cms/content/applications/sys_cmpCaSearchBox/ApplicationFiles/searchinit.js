	function onFormLoad() 
	{
		 document.contentsearch.communityid.options.length = communities.length+1;
		 document.contentsearch.communityid.options[0].value="";
		 document.contentsearch.communityid.options[0].text="";
		 for(i=1; i<=communities.length; i++)
		 {
			  document.contentsearch.communityid.options[i].value=communities[i-1].id;
			  document.contentsearch.communityid.options[i].text=communities[i-1].name;
			  if(document.contentsearch.searchdefcomm.value != "no")
			  {
				  if(document.contentsearch.logincommunity.value == communities[i-1].id)
					document.contentsearch.communityid.options[i].selected=true;
			  }
		 }
		 community_onchange();
	}

	function community_onchange() 
	{
		 if(communities.length < 1)
			 return;

		 index = document.contentsearch.communityid.selectedIndex;
		 if (index==0)
		 {
			 document.contentsearch.ctype.options.length = allcontenttypes.length+1;
			 document.contentsearch.ctype.options[0].value="";
			 document.contentsearch.ctype.options[0].text="";
			 for(j=1; j<=allcontenttypes.length; j++)
			 {
				  document.contentsearch.ctype.options[j].value=allcontenttypes[j-1];
				  document.contentsearch.ctype.options[j].text=allcontenttypesdn[j-1];
			 }
		 }
		 else
		 {
			 contenttypes = communities[index-1].contenttypes;

			 document.contentsearch.ctype.options.length = contenttypes.length+1;
			 document.contentsearch.ctype.options[0].value="";
			 document.contentsearch.ctype.options[0].text="";
			 for(i=1; i<=contenttypes.length; i++)
			 {
				  document.contentsearch.ctype.options[i].value=contenttypes[i-1].name;
				  document.contentsearch.ctype.options[i].text=contenttypes[i-1].displayname;
			 }
		 }
		document.contentsearch.ctype.selectedIndex=0;
	}
