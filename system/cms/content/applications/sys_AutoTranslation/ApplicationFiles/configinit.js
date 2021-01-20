	function onFormLoad(init) 
	{
		 document.editconfiguration.sys_community.options.length = communities.length;
		 for(i=0; i<communities.length; i++)
		 {
			 document.editconfiguration.sys_community.options[i].value=communities[i].id;
			 document.editconfiguration.sys_community.options[i].text=communities[i].name;
			 if(init == "yes" && communities[i].id == document.editconfiguration.communityid.value)
			 {
				document.editconfiguration.sys_community.options[i].selected=true;
			 }
		 }
		 
		 community_onchange(init);
	}

	function community_onchange(init) 
	{

		 if(communities.length < 1)
			 return;
		
		index = document.editconfiguration.sys_community.selectedIndex;

		contenttypes = communities[index].contenttypes;
		if(document.editconfiguration.sys_contenttype.type == "select-one")
		{
		 document.editconfiguration.sys_contenttype.options.length = contenttypes.length;
		 for(i=0; i<contenttypes.length; i++)
		 {
			  document.editconfiguration.sys_contenttype.options[i].value=contenttypes[i].id;
			  document.editconfiguration.sys_contenttype.options[i].text=contenttypes[i].name;
			  if(init == "yes" && contenttypes[i].id == document.editconfiguration.contenttypeid.value)
			  {
				 document.editconfiguration.sys_contenttype.options[i].selected=true;
			  }
		 }
		 if(init != "yes")
			document.editconfiguration.sys_contenttype.selectedIndex=0;
		}
		contenttype_onchange(init);
	}

	function contenttype_onchange(init) 
	{
		 if(contenttypes.length < 1)
			 return;

		if(document.editconfiguration.sys_contenttype.type == "select-one")
		{
		 index = document.editconfiguration.sys_contenttype.selectedIndex;
		}
		else
		{
			for(i=0; i<contenttypes.length; i++)
			{
				if(contenttypes[i].id == document.editconfiguration.sys_contenttype.value)
				{
					index = i;
					break;
				}
			}
		}
		 workflows = contenttypes[index].workflows;

		 document.editconfiguration.sys_workflow.options.length = workflows.length;
		 for(i=0; i<workflows.length; i++)
		 {
			  document.editconfiguration.sys_workflow.options[i].value=workflows[i].id;
			  document.editconfiguration.sys_workflow.options[i].text=workflows[i].name;
			  if(init == "yes" && workflows[i].id == document.editconfiguration.workflowid.value)
			  {
				 document.editconfiguration.sys_workflow.options[i].selected=true;
			  }
		 }
		 if(init != "yes")
			document.editconfiguration.sys_workflow.selectedIndex=0;
		if(document.editconfiguration.sys_locale.type == "select-one")
		{
		 locales = contenttypes[index].locales;
		 document.editconfiguration.sys_locale.options.length = locales.length;
		 for(i=0; i<locales.length; i++)
		 {
			  document.editconfiguration.sys_locale.options[i].value=locales[i].id;
			  document.editconfiguration.sys_locale.options[i].text=locales[i].name;
			  if(init == "yes" && locales[i].id == document.editconfiguration.locale.value)
			  {
				 document.editconfiguration.sys_locale.options[i].selected=true;
			  }
		 }
		 if(init != "yes")
			document.editconfiguration.sys_locale.selectedIndex=0;
		}
	}
