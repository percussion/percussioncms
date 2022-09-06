function p13nTrackUpdate() {

	var segmentString = getmetacontents("segments");
	if (typeof segmentString != 'undefined') {
		var params1 = {
			actionName :"update"
		};
		addSegments(params1, segmentString);
		p13nTrack(params1);
	}
}

function p13nTrackUpdateWeights(segmentWeights) {
	if (typeof segmentString != 'undefined') {
		var params1 = {
			actionName :"update"
		};
		addSegments(params1, segmentWeights);
		p13nTrack(params1);
	}
}

function p13nTrackSet() {
	var segmentString = getmetacontents("segments");
	if (typeof segmentString != 'undefined') {
		var params1 = {
			actionName :"set"
		};
		addSegments(params1, segmentString);
		p13nTrack(params1);
	}
}

function p13nTrackSetWeights(segmentWeights) {
	if (typeof segmentString != 'undefined') {
		var params1 = {
			actionName :"set"
		};
		addSegments(params1, segmentWeights);
		p13nTrack(params1);
	}
}

function p13nTrackClear() {
	var segmentString = getmetacontents("segments");
	if (typeof segmentString != 'undefined') {
		var params1 = {
			actionName :"clear"
		};
		addSegments(params1, segmentString);
		p13nTrack(params1);
	}
}

function p13nTrackClearWeights(segmentWeights) {
	if (typeof segmentString != 'undefined') {
		var params1 = {
			actionName :"clear"
		};
		addSegments(params, segmentWeights);
		p13nTrack(params1);
	}
}

function p13nTrack(params) {
	params.referrerUrl = document.referrer;
	var callback = function(data) {
		if (data.status == "ERROR") {
			return "I failed because of: " + data.errorMessage;
		}

		var header = '<div class="header">';
		var body = 'ID=';
		body = body + data.visitorProfileId + "<br>";
		body = body + "STATUS=" + data.status + "<br>";
		body = body + "meta=" + getmetacontents("segments");

		var footer = '<' + '/div>';
		return header + body + footer;
	};
	params._ie = new Date().getTime();

	$.getJSON(p13N_TrackingService, params, function(data) {
		jQuery('.p13nTrackResult').replaceWith(callback(data));
		/*
		 * Tell the P13N UI that the profile editor is now dirty
		 * since tracking has update the profile.
		 */
		if (typeof parent.p13nProfileEditor != "undefined") {
			parent.p13nProfileEditor.previewPageIsDirty = false;
			parent.p13nProfileEditor.refreshProfileEditorWithTracking(data,
					params);
		}
		/*
		 * Fire an event to signify tracking is completed.
		 */
		jQuery('html *').trigger("p13nAfterTracking", [ data, params ]);
	});

}

function addTrackingSegmentWeight(params, name, value) {
	params["segmentWeights[" + name + "]"] = value;
}
function addTrackingSegment(params, name) {
	params["segmentWeights[" + name + "]"] = 1;
}

function getmetacontents(mn) {
	var m = document.getElementsByTagName('meta');
	for ( var i in m) {
		if (m[i].name == mn) {
			return m[i].content;
		}
	}
}

function addSegments(params, segmentString) {
	var segmentArray = segmentString.split(',');

	for (i = 0; i < segmentArray.length; i++) {
		var segmentWeights = segmentArray[i].split(':');
		if (segmentWeights.length == 2) {
			addTrackingSegmentWeight(params, segmentWeights[0],
					segmentWeights[1]);
		} else {
			addTrackingSegment(params, segmentArray[i]);
		}
	}
}
