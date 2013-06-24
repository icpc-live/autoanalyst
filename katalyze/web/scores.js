(function() {
	

	function ScoreUpdater(target, teamIDs) {
		var that = this;
		
		var dataSourceUrl = "";
		var defaultWidth = 80;
		
		var boards = [];
		var columnHeaders = [];
		
		var problems = [];
		
		dataSourceUrl = target.first().attr("data-Source");
		
		that.dataSource = function() {
			return dataSourceUrl;
		}
		
		var makeFilterPredicate = function(filter) {
			if (!filter) {
				return function() {return true;};
			} else {
				return eval("(function(score) {return ("+filter+");})");
			}
		}
		
		var addTableHeader = function(boardTable, contestInfo) {
			
			var headerColumns = ["Rank", "Team", "Solved", "Time", "Video" ];
			$.each(contestInfo.problems, function(i, problem) {
				headerColumns.push(problem.tag);
			});
			
			var header = $('<tr />');
			boardTable.append(header);
			var columns = $('<colgroup />"');
			boardTable.append(columns);
			
			for (var cellNumber=0; cellNumber<headerColumns.length; cellNumber++) {
				var column = $('<col class="scoreCell cell'+cellNumber+'"/>');
				columns.append(column);
				var headerCell = $('<th />');
                var headerText = headerColumns[cellNumber];
                if (/^[A-Z]$/i.test(headerText)) {
                    headerText = "<a href='problem.php?problem_id=" + headerText + "'>" + headerText + "</a>";
                }
				headerCell.html(headerText);
				header.append(headerCell);
			}
		}

		var createScoreBoardDiv = function(targetDiv, contestStatus) {

			var scoreBoard = $('<div />');
			var boardTable = $('<table class="scoreTable" />');
			scoreBoard.append(boardTable);

			addTableHeader(boardTable, contestStatus.contestInfo);
					
			var filterPredicate = makeFilterPredicate(targetDiv.attr("data-filter"));
			
			$.each(contestStatus.scoreBoard, function(i, scoreData) {
				if (filterPredicate(scoreData)) { 
					that.addRow(boardTable, scoreData);
				}
			});
			
			return scoreBoard;
		}
		
					
		that.update = function(contestStatus) {
			
			$.each(boards, function(i, toRemove) {
				toRemove.remove();
			});
			
			boards = [];
			
			$.each(target, function(i, div) {
				var divTarget = $(div);
				var scoreBoard = (contestStatus) ? 
						createScoreBoardDiv(divTarget, contestStatus) : notAvailable();
				
				divTarget.append(scoreBoard);
				boards.push(scoreBoard);
			});
		}
		
		var notAvailable = function() {
			var scoreBoard = $('<div />');
			scoreBoard.text("Scoreboard is currently not available");
			
			return scoreBoard;
		}		
		
	
		that.addRow = function(scoreBoard, data) {
			var rowToAdd = $('<tr class="scoreRow" />');
			var cellNumber = 0;

			var scoreCell = function(problemStatus) {
				var result = $('<td />');
				var time = $('<div />');
				if (problemStatus.solved == true) {
					result.addClass("problemSolved");
					time.text(problemStatus.attempts+" ("+problemStatus.time+")");
				} else {
					if (problemStatus.attempts>0) {
						result.addClass("problemAttempted");
						time.text(problemStatus.attempts);
					} else {
						time.text("\u00A0");
					}
				}
				
				result.append(time);

				var potential = problemStatus.potential;
				if (potential) {
					var withinMinutes = (potential.before) ? " \u231a"+potential.before : " \u221e";
					var potential = $('<div />').text("\u2192"+problemStatus.potential.rank+withinMinutes);
					potential.addClass("whatIf");
					result.append(potential);
				}
				
				return result;
			}
			
			var tableCell = function(innerData) {
				var result = $('<td />');
				result.html(innerData);
				return result;
			
			}
			
			var addCells = function(texts) {
				$.each(texts, function(i, text) {
					rowToAdd.append(tableCell(text));
				});
			}

            var name = "<a href='team.php?team_id=" + data.team.id + "'>" + data.team.name + "</a>";
            var padded_id = "" + data.team.id;
            while (padded_id.length < 3) { padded_id = "0" + padded_id; } // there's got to be a better way to do this
            var videoLinks = "<a href='vlc://192.168.1.141:58" + padded_id + "'>low</a>, " +
                             "<a href='vlc://192.168.1.141:60" + padded_id + "'>high</a>, " +
                             "<a href='vnc://192.168.1.141:59" + padded_id + "'>screen</a>";
			addCells([data.rank, name, data.nSolved, data.totalTime, videoLinks]);
			 $.each(data.problems, function(i, problemData) {
				 rowToAdd.append(scoreCell(problemData));
			 });
			 		
			scoreBoard.append(rowToAdd);
			
		}				
	}

	$(function() {
	
		
		var updater = new ScoreUpdater($(".teamscore"),[104,24]);

		var refreshData = function() {
			$.ajax({
				url: updater.dataSource()+"/Standings",
				success: updater.update,
				data: {},
				dataType: "json",
				error: updater.notAvailable
					
				});
		}
		
		refreshData();
		window.setInterval(refreshData, 5000);
		
	
	});
	
})();
