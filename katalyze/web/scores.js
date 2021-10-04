(function() {
	

 	var entityMap = {
	    "&": "&amp;",
	    "<": "&lt;",
 	   ">": "&gt;",
 	   '"': '&quot;',
 	   "'": '&#39;',
 	   "/": '&#x2F;'
  };

  function escapeHtml(string) {
    return String(string).replace(/[&<>"'\/]/g, function (s) {
      return entityMap[s];
    });
  }


	function ScoreUpdater(target) {
		var that = this;
		
		var dataSourceUrl = "";
		var defaultWidth = 80;
		
		var boards = [];
		var columnHeaders = [];
		
		var problems = [];
		var teams = {};
		
		dataSourceUrl = target.first().attr("data-Source");
		var highlightWindow = 10;

	    if (!dataSourceUrl || dataSourceUrl == "") {
			dataSourceUrl = window.location.protocol+"//"+window.location.host;
	    }
		
		
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


		var addTableHeader = function(boardTable, problemLabels) {
			
			var headerColumns = ["Rank", "Team", "Solved", "Time", "Video", "Language" ];
			$.each(problemLabels, function(i, label) {
				headerColumns.push(label);
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

		var getProblemLabels = function(scoreRow) {
			var result = [];

			$.each(scoreRow.problems, function(i, problem) {
				result.push(problem.label)
			});
			return result;
		}


		var createScoreBoardDiv = function(targetDiv, standings, contestInfo) {

			var scoreBoard = $('<div />');
			var boardTable = $('<table class="scoreTable" />');
			scoreBoard.append(boardTable);


			var firstTeamScore = standings[0];

			addTableHeader(boardTable,getProblemLabels(firstTeamScore));
					
			var filterPredicate = makeFilterPredicate(targetDiv.attr("data-filter"));


			// If there is a contestinfo entry, we can highlight the last changes
			var highlighting;

			var contestTime = standings[0].contestTime;
			if (contestTime) {
				
				// Set up the highlighting function.
				highlighting = function (cell) {
					var lastUpdate = cell.lastUpd;
					if (lastUpdate) {
						return lastUpdate + highlightWindow > contestTime;
					} else {
						return false;
					}
				}
			} else {
				highlighting = function () { return false;}
			}


			
			$.each(standings, function(i, scoreData) {
				if (filterPredicate(scoreData)) { 
					that.addRow(boardTable, scoreData, highlighting);
				}
			});
			
			return scoreBoard;
		}
		
					
		that.update = function(contestStatus) {
			
			$.each(boards, function(i, toRemove) {
				toRemove.remove();
			});
			
			boards = [];
			var contestInfo = null;
			
			$.each(target, function(i, div) {
				var divTarget = $(div);
				var scoreBoard = (contestStatus) ? 
						createScoreBoardDiv(divTarget, contestStatus, contestInfo) : notAvailable();
				
				divTarget.append(scoreBoard);
				boards.push(scoreBoard);
			});
		}
		
		var notAvailable = function() {
			var scoreBoard = $('<div />');
			scoreBoard.text("Scoreboard is currently not available");
			
			return scoreBoard;
		}


		that.addRow = function(scoreBoard, data, highlighting) {
			var rowToAdd = $('<tr class="scoreRow" />');
			var cellNumber = 0;

			var scoreCell = function(problemStatus) {
				var result = $('<td />');
				var time = $('<div />');
				if (problemStatus.solved == true) {
					result.addClass("problemSolved");
					time.text(problemStatus.num_judged+" ("+problemStatus.time+")");
				} else {
					if (problemStatus.num_judged>0) {
						result.addClass("problemAttempted");
						time.text(problemStatus.num_judged);
					} else {
						time.text("\u00A0");
					}
				}
				if (highlighting(problemStatus)) {
				    result.addClass("recentlyChanged");
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

			var teamInfo = teams[data.team_id];
			var teamName = "Team "+data.team_id;

			if (teamInfo) {
			    teamName = (teamInfo.organization) ? teamInfo.organization : teamInfo.name
			}

		    var name = "<a href='team.php?team_id=" + data.team_id + "'>" + escapeHtml(teamName) + "</a>";
		    var videoLinks = "";

		    var desktopLinksArray = teamInfo.desktops.join();
		    videoLinks = videoLinks + "<a href='"+dataSourceUrl+"/web/showvideo.html?streams=" + encodeURIComponent(desktopLinksArray)+"'> &#128187;</a>";

		    var webcamLinksArray = teamInfo.webcams.join();
		    videoLinks = videoLinks + "<a href='"+dataSourceUrl+"/web/showvideo.html?streams=" + encodeURIComponent(webcamLinksArray)+"'> &#128247;</a>";

		    addCells([data.rank, name, data.score.num_solved, data.score.total_time, videoLinks, data.main_lang]);
		    
			 $.each(data.problems, function(i, problemData) {
				 rowToAdd.append(scoreCell(problemData));
			 });
			 		
			scoreBoard.append(rowToAdd);
			
		}


		that.updateTeams = function(data) {
			teams = {}
			$.each(data, function(i, team) {
				teams[team.id] = team;
			});
		}


	}

	$(function() {
	
		
		var updater = new ScoreUpdater($(".teamscore"));

		var refreshData = function() {
			$.ajax({
				url: updater.dataSource()+"/teams",
				success: updater.updateTeams,
				dataType: "json"
			});

			$.ajax({
				url: updater.dataSource()+"/scoreboard",
				success: updater.update,
				data: {},
				dataType: "json",
				error: updater.notAvailable
					
			});
		}
		
		refreshData();
		window.setInterval(refreshData, 10000);
		
	
	});
	
})();
