
(function($) {

	$.fn.dropDownControl = function(opts) {

		var categoryXmlUrl = opts.url;
		var parent = opts.parent;
		var paramName = opts.paramName;
		var autoGenerate = opts.autoGenerate;
		var optsVal = opts.val;
		var readonly = opts.readonly;
		var sortorder = opts.sortorder;
		var sitename = opts.sitename; // THIS NEEDS TO BE HANDLED WHEN WE WORK ON CATEGORIES PER SITE.
		var categoryTree;
		var containerSelect = $("#parent-categories-"+paramName);
		var parentContainer = "#parent-categories-"+paramName;
		var selectedContainer = "first";
		var setVal = [];
		var values = null;
		var selectionMade = "false";

		function getCategoryData() {
			if(optsVal !== '') {
				//parent = "root";

				// get the list of ids from the json string in the val.
				values = JSON.parse(optsVal);
			}
			// The js needs to access the results from the xml applications and present the results to the xsl displaying the control.
			getCategoryXml(categoryXmlUrl, function(xml) {
				//alert("reaching here with xml : " + xml);
				categoryTree = xml;
				showCategories(containerSelect, categoryTree.documentElement);
			});
		}

		function getCategoryXml(url, callback) {
			$.ajax({
				type: "GET",
				url: url+"?parentCategory="+parent+"&sitename="+sitename,
				dataType: "xml",
				success: callback
			});
		}

		function showCategories(container, xml) {
			var x, i, totalChild = 0;
			x = xml.childNodes;
			if(x.length >= 3)
				container.append('<option value="">Select Category</option></br>');
			for (i = 0; i < x.length; i++) {
				if (x[i].nodeType === 1) {
					for (var j = 0; j < x[i].children.length; j++) {
						var y = x[i].children[j].getAttribute("title");
						if(x[i].children[j].getAttribute("selectable") === "true") {
							container.append('<option value='+x[i].children[j].getAttribute("id")+'>'+y+'</option>');
							totalChild = totalChild + 1;
						}
					}
				}
			}

			if(sortorder != null)
				sortOptions(container, sortorder);

			if(readonly === 'yes')
				containerSelect[0].disabled = 'disabled';

			markSelectedOption(container);
		}

		containerSelect.change(function() {

			removeSubSelects(0);
			selectedContainer = "first";
			selectionMade = "true";

			$("#perc-categories-json-"+paramName).val("");
			if(($(parentContainer +" :selected").val()) !== null && ($(parentContainer +" :selected").val()) !== "") {
				if(autoGenerate === "yes") {
					var value = {id : $(parentContainer +" :selected").val(),
						title : $(parentContainer +" :selected").text()};
					setVal.splice(0, setVal.length);
					setVal.push(value);

					$("#perc-categories-json-"+paramName).val(JSON.stringify(setVal));
				} else {
					setVal.splice(0, setVal.length);
					$(parentContainer +' :selected').each(function(i, selected){
						if($(selected).val() != null) {
							value = {id : $(selected).val(),
								title : $(selected).text()};
							setVal.push(value);
						}
					});

					$("#perc-categories-json-"+paramName).val(JSON.stringify(setVal));
				}

			} else {
				$("#perc-categories-json-"+paramName).val("");
			}
		});

		$(document).on('change', 'select[id^="subCategory-'+paramName+'"]', function () {

			var thisId = $(this).attr("id");
			var subSelectIdVal = parseInt(thisId.slice(-1));

			removeSubSelects(subSelectIdVal);
			selectedContainer = "sub";
			selectionMade = "true";
			var selectedValue = $(this).val();
			findNode(selectedValue, subSelectIdVal+1);

			if(($("#" + thisId + " :selected").val()) != null) {
				setVal.splice(0, setVal.length);
				var value = {id : $(parentContainer +" :selected").val(),
					title : $(parentContainer +" :selected").text()};
				setVal.push(value);
				setSubSelectsValues(subSelectIdVal, setVal);
			}
		});

		function markSelectedOption(container) {
			if(values != null) {
				if(selectedContainer === "first") {
					if(autoGenerate === "no" && selectionMade === "false") {
						var multiValues = " ";
						for(var j = 0; j < values.length; j++) {
							//alert(values[j].id + ",");
							multiValues = multiValues + values[j].id + ",";
						}
						multiValues = multiValues.substring(0,multiValues.length-1);
						markMultipleOptions(container, multiValues.trim());
					} else {
						if(selectionMade === "false") {
							markOption(container, values[0].id);
							selectedContainer = "sub";
							findNode(values[0].id, 1);
						}
					}
				} else {
					if(selectionMade === "false"){
						var x = parseInt((container.selector).slice(-1));
						if(values.length > x) {
							markOption(container, values[x].id);
							selectedContainer = "sub";
							findNode(values[x].id, x+1);
						}
					}
				}
			}
		}

		function markOption(container, markValue) {
			$(container.selector).find('option').each(function(i,e){
				if($(e).val() != null) {
					if($(e).val() === markValue){
						$(container.selector).prop('selectedIndex',i);
						if(containerSelect[0].disabled === 'disabled' || containerSelect[0].disabled)
							$(container.selector).prop('disabled','disabled');
					}
				}
			});
		}

		function markMultipleOptions(container, vals) {
			$.each(vals.split(","), function(i,e){
				$(container.selector+" option[value='" + e + "']").prop("selected", true);
				if(containerSelect[0].disabled === 'disabled' || containerSelect[0].disabled)
					$(container.selector+" option[value='" + e + "']").prop('disabled','disabled');
			});
		}

		function setSubSelectsValues(subSelectId, setValue){
			var subSelect = $('[id^="subCategory-'+paramName+'"]');
			for(var i = 0; i < subSelectId; i++) {
				var value = {id : $("#"+subSelect[i].id+" :selected").val(),
					title : $("#"+subSelect[i].id+" :selected").text()};
				setVal.push(value);
				value = null;
			}

			$("#perc-categories-json-"+paramName).val(JSON.stringify(setValue));
		}

		function removeSubSelects(subSelectCount) {

			if($('[id^="subCategory-'+paramName+'"]').length > subSelectCount) {
				var subSelect = $('[id^="subCategory-'+paramName+'"]');
				for(var i = subSelectCount; i < subSelect.length; i++) {
					subSelect[i].remove();
				}
			}
		}

		function findNode(selectedValue, subSelectIdVal) {
			var x = [];
			var parentId = "";
			for(var j = 0; j < subSelectIdVal; j++) {
				if(j === 0) {
					x = getChildNodes(categoryTree.documentElement);
					parentId = $(parentContainer +" :selected").val();
				} else {
					x = getChildNodes(x, parentId);
					parentId = $("#subCategory-"+paramName+"-"+j).val();
				}
			}

			for (var i = 0; i < x.length; i++) {
				if (x[i].nodeType === 1) {
					if(x[i].attributes.getNamedItem("id") === selectedValue) {
						if(autoGenerate === "yes") {
							if(x[i].childNodes.length > 0) {
								// create new select element and add the child nodes as options in there.
								if(!$("#subCategory-"+paramName+"-"+subSelectIdVal).length || $("#subCategory-"+paramName+"-"+subSelectIdVal).length < 1) {
									createSubCategorySelect(paramName+"-Categories", "subCategory-"+paramName+"-"+subSelectIdVal);
								} else {
									$("#subCategory-"+paramName+"-"+subSelectIdVal).empty();
								}

								var c = $("#subCategory-"+paramName+"-"+subSelectIdVal);
								showCategories(c, x[i]);
								break;
							}
						}
					}
				}
			}
			return subSelectIdVal;
		}

		function getChildNodes(node, parentId) {
			var childNodeList = [];

			if(parentId == null)
				childNodeList = node.childNodes;
			else {
				for(var i = 0; i < node.length; i++) {
					if(node[i].nodeType === 1) {
						if(node[i].attributes.getNamedItem("id").value === parentId) {

							childNodeList = node[i].childNodes;
							break;
						}
					}
				}
			}

			return childNodeList;
		}

		function createSubCategorySelect(name, id) {
			var subCategories = $("<select id=\""+ id +"\" name=\""+name+"\" />");
			if(readonly === 'yes')
				$("#datadisplay-"+paramName).append(subCategories);
			else
				$("#maindiv-"+paramName).append(subCategories);
		}

		function sortOptions(container, order){

			order = order.toLowerCase();

			var options = $(container.selector + " option");
			options = options.slice(1, options.length);
			var arr = options.map(function(_, opt) {
				return { t: $(opt).text(), v: opt.value };
			}).get();

			if(order === "ascending" || order === "asc") {
				arr.sort(function(opt1, opt2) {
					return opt1.t > opt2.t ? 1 : opt1.t < opt2.t ? -1 : 0;
				});
			} else {
				arr.sort(function(opt1, opt2) {
					return opt1.t < opt2.t ? 1 : opt1.t > opt2.t ? -1 : 0;
				});
			}

			options.each(function(i, opt) {
				if(opt.value != null) {
					opt.value = arr[i].v;
					$(opt).text(arr[i].t);
				}
			});

		}

		getCategoryData();
	};
})(jQuery);