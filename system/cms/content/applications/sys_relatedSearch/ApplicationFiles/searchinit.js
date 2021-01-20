	function onFormLoad() 
	{
		 document.updatesearch.sys_communityid.options.length = communities.length+1;
		 document.updatesearch.sys_communityid.options[0].value="";
		 document.updatesearch.sys_communityid.options[0].text="";

		 for(i=1; i<=communities.length; i++)
		 {
			  document.updatesearch.sys_communityid.options[i].value=communities[i-1].id;
			  document.updatesearch.sys_communityid.options[i].text=communities[i-1].name;
			  if(document.updatesearch.searchdefcomm.value != "no")
			  {
				  if(document.updatesearch.logincommunity.value == communities[i-1].id)
					document.updatesearch.sys_communityid.options[i].selected=true;
			  }
		 }
		 community_onchange();
	}

	function community_onchange() 
	{
		 if(communities.length < 1)
			 return;

		 index = document.updatesearch.sys_communityid.selectedIndex;
		 if (index==0)
		 {
			 document.updatesearch.sys_contenttype.options.length = allcontenttypes.length+1;
			 document.updatesearch.sys_contenttype.options[0].value="";
			 document.updatesearch.sys_contenttype.options[0].text="";
			 for(j=1; j<=allcontenttypes.length; j++)
			 {
				  document.updatesearch.sys_contenttype.options[j].value=allcontentids[j-1];
				  document.updatesearch.sys_contenttype.options[j].text=allcontenttypes[j-1];
			 }
		 }
		 else
		 {
			 contenttypes = communities[index-1].contenttypes;

			 document.updatesearch.sys_contenttype.options.length = contenttypes.length+1;
			 document.updatesearch.sys_contenttype.options[0].value="";
			 document.updatesearch.sys_contenttype.options[0].text="";
			 for(i=1; i<=contenttypes.length; i++)
			 {
				  document.updatesearch.sys_contenttype.options[i].value=contenttypes[i-1].id;
				  document.updatesearch.sys_contenttype.options[i].text=contenttypes[i-1].name;
			 }
		 }
		document.updatesearch.sys_contenttype.selectedIndex=0;
	}
