/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * Gloabl Variables Plugin.
 */
(function() {
	tinymce.create('tinymce.plugins.GlobalVariables', {
		init : function(ed, url) {
            var cls = 'perc-global-variable-marker';

            // Register commands
			ed.addCommand('mceInsertGlobalVariables', function() {
				//Insert code here
                var mainEditor = ed.contentWindow.parent;
                if(!mainEditor.jQuery || !mainEditor.jQuery.topFrameJQuery)
                    return;
                var topFrJQ = mainEditor.jQuery.topFrameJQuery;
                var dialogHtml = '<div><div id="perc-global-variables-wrapper"><table id="perc-global-variables-table"><tbody><tr class="perc-row-0"><th align="left" class="perc-column-0 perc-global-variables-column-header">Variable name</th><th class="perc-column-1 perc-global-variables-column-header">Variable value</th></tr>';
                topFrJQ.each(PercGlobalVariablesData, function(varName, varValue){
                    dialogHtml += '<tr class="perc-wfconfig-row perc-gv-row"><td align:"left"="" class="perc-column-0 perc-global-variables-column"><input type="radio" class="perc-global-variables-radio" style="width:auto;"><span class="perc-roleName-wrapper perc-ellipsis perc-gv-name">' + varName + '</span></td><td align:"left"="" class="perc-column-1 perc-global-variables-column"><span class="perc-roleName-wrapper perc-ellipsis perc-gv-value">' + varValue + '</span></td></tr>'
                });
                dialogHtml += '</table></div></div>';
                // if we are in the new blog post dialog, the width is 
                var dialogWidth = 700;
                var dialogHeight = 300;
                var dialog = topFrJQ(dialogHtml).perc_dialog({
                    title: "Insert Global Variable",
                    buttons: {},
                    percButtons:{
                        "Save":{
                            click: function(){
                                var gvRow = dialog.find(".perc-global-variable-selected");
                                if(gvRow.length < 1)
                                {
                                    alert("Please select a global variable to insert.");
                                    return;
                                }
                                var varName = gvRow.find(".perc-gv-name").html();
                                var varValue = gvRow.find(".perc-gv-value").html();
                                var urlToServlet = window.location.protocol+"//"+window.location.host + "/Rhythmyx/textToImage?imageText=&lt; " + varValue + " &gt;";
                                var gvhtml = "<img class='perc-global-variable-marker' title='"+varName+"' src='" + urlToServlet + "'>";
                                
                                ed.execCommand('mceInsertContent', 0, gvhtml);
                                dialog.remove();
                            },
                            id: "perc-edit-global-variables-insert"
                        },
                        "Cancel":{
                            click: function(){
                                dialog.remove();
                            },
                            id: "perc-edit-global-variables-cancel"
                        }
                    },
                    id: "perc-insert-global-variables-dialog",
                    width: dialogWidth,
                    modal: true
                });
                dialog.find(".perc-gv-row").on("click",function(){
                    dialog.find(".perc-global-variable-selected").removeClass("perc-global-variable-selected");
                    dialog.find("input:radio").attr('checked',false);
                    $(this).addClass("perc-global-variable-selected").find('td').addClass("perc-global-variable-selected");
                    $(this).addClass("perc-global-variable-selected").find('td').find("input:radio").attr('checked',true);
                });
			});
            
            // Register button
			ed.addButton('globalvariables', {title : "Global variables", image: url + "/img/GlobalVariablesIcon.png", cmd : 'mceInsertGlobalVariables'});
            
            ed.onInit.add(function() {
				if (ed.settings.content_css !== false)
					ed.dom.loadCSS(url + "/css/globalVariables.css");

				if (ed.theme.onResolveName) {
					ed.theme.onResolveName.add(function(th, o) {
						if (o.node.nodeName == 'IMG' && ed.dom.hasClass(o.node, cls))
							o.name = 'globalvariables';
					});
				}
			});
            
            ed.onClick.add(function(ed, e) {
				e = e.target;
				if (e.nodeName === 'IMG' && ed.dom.hasClass(e, cls))
					ed.selection.select(e);
			});

			ed.onNodeChange.add(function(ed, cm, n) {
				cm.setActive('globalvariables', n.nodeName === 'IMG' && ed.dom.hasClass(n, cls));
			});

			ed.onBeforeSetContent.add(function(ed, o) {
                o.content = o.content.replace(/<span[^>]*?(.*?)<\/span>/g, function(im) {
                    if (im.indexOf('class="perc-global-variable-marker') !== -1){
                        var varName = $(im).attr("title");
                        var varValue = PercGlobalVariablesData[varName];
                        if(varValue)
                        {
                            var urlToServlet = window.location.protocol+"//"+window.location.host + "/Rhythmyx/textToImage?imageText=&lt; " + varValue + " &gt;";
                            var gvhtml = "<img class='perc-global-variable-marker' title='"+varName+"' src='" + urlToServlet + "'>";
                                    
                            im = gvhtml;
                        }
                    }
                    return im;
                });
			});

			ed.onPostProcess.add(function(ed, o) {
                if (o.get)
                    o.content = o.content.replace(/<img[^>]+>/g, function(im) {
                        if (im.indexOf('class="perc-global-variable-marker') !== -1){
                            var varName = $(im).attr("title");
                            var varValue = PercGlobalVariablesData[varName];
                            var gvhtml = '<span class="perc-global-variable-marker" title="' + varName + '">' + varValue + '</span>';
                            im = gvhtml;
                        }
                        return im;
                    });
            });
            
		},
        //Setup required info
		getInfo : function() {
			return {
				longname : 'Insert global variables',
				author : 'Percussion Software Inc.',
				authorurl : 'https://www.percussion.com',
				infourl : 'https://help.percussion.com/cm1',
				version : tinymce.majorVersion + "." + tinymce.minorVersion
			};
		},

	});

	// Register plugin
	tinymce.PluginManager.add('globalvariables', tinymce.plugins.GlobalVariables);
})();
