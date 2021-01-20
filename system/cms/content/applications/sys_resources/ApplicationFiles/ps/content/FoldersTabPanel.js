dojo.provide("ps.content.FoldersTabPanel");

dojo.require("ps.content.History");
dojo.require("ps.content.FolderSitesBaseTabPanel");
dojo.require("ps.content.SelectTemplates");
dojo.require("ps.widget.ContentPaneProgress");
dojo.require("ps.util");
dojo.require("dojo.collections.Dictionary");
dojo.require("dojo.lang.declare");
dojo.require("dojo.json");
dojo.require("dojo.string");

dojo.declare("ps.content.FoldersTabPanel", ps.content.FolderSitesBaseTabPanel,
	function(_parent){
	   dojo.lang.assert(_parent, "Parent must be specified");

      this.prefix = ps.util.BROWSETAB_FOLDERS_PANEL_PREF;
      this.parent = _parent;      
	},
{
   init: function()
   {
      this.tabId = this.prefix + ".tab";
      this.tab = dojo.widget.byId(this.tabId);
      dojo.lang.assert(this.tab, "Tab for " + this.prefix + " should exist");
      this.url = this.parent.rxroot + "/ui/content/sitesfolderpanel.jsp?mode=folders";
      ps.content.FoldersTabPanel.superclass.init.apply(this);
   },
	
	/**
    * Is called when templates selection panel is loaded.
    */
   _onTemplatesSiteFolderParamLoaded: function ()
   {
      dojo.lang.assert(false,
            "Templates site folder params pane should not be loaded "
            + "on the folders tab");
   }

});