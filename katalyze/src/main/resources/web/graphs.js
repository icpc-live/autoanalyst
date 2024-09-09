var viewModel = {};

// Helper function used while debugging in order to show all methods that exist on an object
var allMethods = function(obj) {
	var result="";

	for (var property in obj) {
		result = result + ":" + property;
	}
	return result;
}


$(function() {

	var showRealtime = true;

	// Simple helper function to zero pad an integer
	function zeroFill( number, width ) {
		width -= number.toString().length;
		if ( width > 0 ) {
			return new Array( width + (/\./.test( number ) ? 2 : 1) ).join( '0' ) + number;
		}
		return number;
	}
	
	var uriParameter = function(parameterName) {
		var name = parameterName.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  		var regexS = "[\\?&]"+name+"=([^&#]*)";
		var regex = new RegExp( regexS );
		var results = regex.exec( window.location.href );
		if( results === null ) {
			return "";
		} else {
    		return results[1];
		}	
	}

	var updateFromUri = function(model) {
		var selectedGraphId = uriParameter("SelectedGraph");
		if (selectedGraphId !== "") {
			model.selectedGraphId(selectedGraphId);
		}
	}

	var updateModel = function(model) {

		viewModel.graphs(model.graphs);
		viewModel.contestTime(model.contestTime);			
		$( "#slider" ).slider( "option", "max", viewModel.contestTime()+1);

		if (showRealtime) {
			$( "#slider" ).slider( "option", "value", viewModel.contestTime()+1);
		}
	}


	var refreshModel = function() {
		jQuery.getJSON( 'output/viewmodel.json', {}, updateModel);  
	}


	// Here's my data model
	viewModel = {
		graphs : ko.observableArray([]),
		selectedGraphId : ko.observable(""),
		useSmallGraphs : ko.observable(false),
		contestTime : ko.observable(0),
		displayTime : ko.observable(0),
		
		graphById : function(searchId) {
			var graphArray = this.graphs();
			for (var i in graphArray) {
				var candidate = graphArray[i];
				if (candidate.description === searchId) {
					return candidate;
				}
			}
			return null;
		},
		

		selected : function() {
			return this.graphById(this.selectedGraphId());
		},		

		select : function(graph) {
			this.selectedGraphId(graph.description);
		},

		resolvePath : function(path) {
			var sizeStr = (this.useSmallGraphs()) ? "small" : "large";
			
						
			var currentContestTime = this.contestTime();
			var time = (showRealtime) ? currentContestTime : this.displayTime();

			if (time > currentContestTime) {
				time = currentContestTime;
			}
			
			var timeStr = zeroFill(time, 3);

			var target = path
				.replace("$size$", sizeStr)
				.replace("$time$", timeStr);

			return target;
		},

		selectedGraphPath : function(graphId) {
			var graph = this.graphById(graphId);
			if (graph !== null) {
				return this.resolvePath(graph.path);
			} else {
				return "";
			}
		}
	};
	


	// Slider
	$('#slider').slider({
		range: false,
		min: 0,
		max: viewModel.contestTime()+1,
		value: viewModel.contestTime()+1,
		slide: function(event, ui) {					
			showRealtime = (ui.value >= viewModel.contestTime());
			viewModel.contestTime(viewModel.contestTime());
			viewModel.displayTime(ui.value);

		}	
	});
	
		
	updateFromUri(viewModel);


	$('button.alert').click(function() {
		refreshModel();
	});

	window.setInterval(refreshModel, 10000);
	refreshModel();
	ko.applyBindings(viewModel); // This makes Knockout get to work

});