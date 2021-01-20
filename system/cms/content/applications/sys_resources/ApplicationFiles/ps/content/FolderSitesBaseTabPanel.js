dojo.provide("ps.content.FolderSitesBaseTabPanel");

dojo.require("ps.content.History");
dojo.require("ps.content.BrowseTabPanel");
dojo.require("ps.content.SelectTemplates");
dojo.require("ps.widget.ContentPaneProgress");
dojo.require("dojo.collections.Dictionary");
dojo.require("dojo.lang.declare");
dojo.require("dojo.json");
dojo.require("dojo.string");


dojo.declare("ps.content.FolderSitesBaseTabPanel", ps.content.BrowseTabPanel,
	function(_parent){
	   dojo.lang.assert(_parent, "Parent must be specified");

      this.parent = _parent;      
	},
{
   /**
    * Parses the address bar panel controls and add appropriate events.
    */
   _parseAddressbarPanelControls: function()
   {
      this.pathText = this._getElemById("pathText");
      this.refreshButton = this._getWidgetById("refreshButton");
      this.backButton = this._getWidgetById("backButton");
      this.upButton = this._getWidgetById("upButton");
      this.clientPanel = this._getWidgetById("clientpanel");
      this.addressbarPanel = this._getWidgetById("addressbarpanel");

      dojo.event.connect(this.pathText, "onchange", this, "_onPathTextChanged");
      dojo.event.connect(this.refreshButton, "onClick", this, "_onRefresh");
      dojo.event.connect(this.backButton, "onClick", this, "_onBack");
      dojo.event.connect(this.upButton, "onClick", this, "_onUp");      
   },
	
	/**
    *  Parses all the expected controls for this tab.
    */
   parseControls: function()
   {
      this._parseCommonControls();
      this._parseFilterPanelControls();
      this.contentTable = this._getWidgetById("FilteringTable");
      // initial conent table columns
      this.contentTableColumns = this._cloneColumns(this.contentTable.columns);
      this._parseAddressbarPanelControls();
      dojo.event.connect(
            this.contentTable, "onSelect", this, "_onContentTableSelect");      
   },
	
	/**
    * Parses the filter panel controls and add appropriate events.
    */
   _parseFilterPanelControls: function()
   {
      this.filterPanel = this._getWidgetById("filterpanel");
      this.filteringTablePanel = this._getWidgetById("tablepanel");
      this.filteringTable = this._getWidgetById("FilteringTable");
      this.filterText = this._getElemById("filterText");

      dojo.event.connect(this.filterText, "onkeyup", this, "_onFilterTyped");

      this._ctypeList = this._getElemById("ctypeList");
      dojo.event.connect(
         this.getContentTypeList(), "onchange", this, "_onCTypeChanged");
      dojo.event.connectBefore(
         this.getContentTypeList(), "onfocus", this, "_onCTypeFocused");
      
   },
	
   /**
    * Callback function called when the path text changes.
    */
   _onPathTextChanged: function()
   {
      this._goTo(this.pathText.value);
   },
   
   /**
    * Callback function called when the "Refresh" button is clicked.
    */
   _onRefresh: function()
   {
      this._refresh();
   },
   
   /**
    * Callback function called when the "Back" button is clicked.
    */
   _onBack: function()
   {
      this.pathHistory.back();
      this._refresh();
   },
   
   /**
    * Callback function called when the "Up" button is clicked.
    */
   _onUp: function()
   {
      this._goTo(this.getParentFolder());
   }
});