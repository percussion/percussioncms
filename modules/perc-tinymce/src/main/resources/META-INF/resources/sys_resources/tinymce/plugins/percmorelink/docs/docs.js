
/**
initialize help page
**/

(function () {
	var onLoad = window.onload;
	window.onload = function () {
		if (typeof onLoad == "function")
			onLoad();

		if (window.parent && window.parent.HelpDialog) {
			window.parent.HelpDialog.helpPage(window);
		}
	};
})();
