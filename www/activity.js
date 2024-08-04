function ActivityPlot(target, team_id, problem_id, update, clickable) {
    var self = this;

    self.update = typeof update !== 'undefined' ? update : true;
    self.clickable = typeof clickable !== 'undefined' ? clickable : true;

    self.target = target;
    self.team_id = team_id;
    self.problem_id = problem_id;
    self.source_url = 'activity_data_source.php';
    self.target.data("activity_plot", self);
    self.update_interval = 5 * 1000;
    var data = get_json_synchronous("common_data.php");
    self.problems = data['problems'];
    self.teams = data['teams'];

    self.plot = function(response) {
        var problems_used = response.problems_used;
        var num_problems = problems_used.length;
        var problem_height = response.max_problems_per_bin;
        var plot_height = num_problems * problem_height;
        var minY = - problem_height / 4;
        var maxY = plot_height + problem_height / 4;

        var ticks = [];
        var colors = [];

        for (var i = 0; i < num_problems; ++i) {
            var pid = problems_used[i].toUpperCase();
            var href = set_query_field(window.location.href, 'problem_id', pid);
            ticks.push([i * problem_height, '<a href="' + href + '">' + pid + '</a>']);
            colors.push(self.problems[pid]['color']);
        }

        var options = {
            colors: colors,
            grid: { hoverable: true, clickable: self.clickable, },
            yaxis: { ticks: ticks,
                zoomRange: [maxY, maxY], // do not allow zooming in on the y-axis
                panRange: [maxY, maxY], // do not allow panning on the y-axis
                min: minY,
                max: maxY,
            },
            xaxis: {
                zoomRange: [10, 321], // extra space to allow for the legend
                panRange: [0, 320], // extra space to allow for the legend
                min: 0,
                max: 320, // extra space to allow for the legend
                tickDecimals: 0,
            },
            zoom: { interactive: true,  },
            pan: { interactive: true,  },
        }

        // if we are already zoomed, preserve the zoom
        if (self.flot) {
            var zoom = self.flot.getAxes();
            options.xaxis.min = zoom.xaxis.min;
            options.xaxis.max = zoom.xaxis.max;
            options.yaxis.min = zoom.yaxis.min;
            options.yaxis.max = zoom.yaxis.max;
        }

        self.flot = $.plot(self.target, response.flot_data, options);
    }

    self.showInfo = function(x, y, contents) {
        $("<div class='hoverinfo'>" + contents + "</div>").css(
            {
                position: 'absolute',
                top: y + 10,
                left: x + 10,
                border: '1px solid green',
                padding: '2px',
                'white-space': 'normal',
                'background-color': '#efe',
                'z-index': 100,
                opacity: 0.90,
                'max-width': '20em'
            }
        ).appendTo(target);
    }

    self.clickableURL = function(clicked_item) {
        var msg, url;
        if (self.team_id && /^[0-9]+$/.test(self.team_id)) {
            msg = 'View Submission';
            url = submission_url(clicked_item.submission_id,data['config'], data['contest']);
        } else {
            msg = 'View Team Activity';
            url = set_query_field(window.location.href, 'team_id', clicked_item.team_id);
        }
        return {message: msg, url: url};
    }

    self.updatePlot = function() {
        var query = [];
        if (self.team_id) { query.push("team_id=" + self.team_id); }
        if (self.problem_id) { query.push("problem_id=" + self.problem_id); }
        if (query) { query = "?" + query.join("&"); }
        var url = self.source_url + query;
        $.ajax({
            url: url,
            data: { team_id: self.team_id },
            success: self.plot,
            error: function(jqXHR, err) { console.log("updatePlot failed for " + url + ": " + jqXHR + ", " + err); },
            dataType: "json"
        });

        if (self.update) {
            setTimeout(self.updatePlot, self.update_interval);
        }
    }

    self.initUI = function() {
        self.target.bind("plothover", 
            function(evt, pos, item) {
                if (item) {
                    self.target.find("div.hoverinfo").remove();
                    var content = '(unknown)';
                    if (item.series && item.series.submissionInfo) {
                        // for the submissions
                        var school_name = 'UNKNOWN';
                        try {
                            school_name = self.teams[item.series.submissionInfo[item.dataIndex].team_id]['school_name'];
                        } catch (e) {}
                        content = item.series.label + ": " 
                                + school_name +
                                  " problem " + item.series.submissionInfo[item.dataIndex].problem_id +
                                  " at time " + item.datapoint[0] +
                                  " (" + item.series.submissionInfo[item.dataIndex].lang_id + ").";
                        if (self.clickable) {
                            content += " Click to " + self.clickableURL(item.series.submissionInfo[item.dataIndex]).message + ".";
                        }
                    } else {
                        // for the edits
                        var numEdits = item.datapoint[1] - item.datapoint[2];
                        var edit = numEdits == 1 ? "team edited" : "teams edited";
                        content = numEdits + " " + edit;
                    }
                    // move the tooltip to a position relative to the plot container
                    var offset = self.target.offset();
                    self.showInfo(item.pageX - offset.left, item.pageY - offset.top, content);
                }
            }
        );

        self.target.bind("plotclick",
            function(evt, pos, item) {
               if (item && item.series && item.series.submissionInfo) {
                   var msg_url = self.clickableURL(item.series.submissionInfo[item.dataIndex]);
                   if (msg_url.url) {
                       window.location.assign(msg_url.url);
                   }
               }
            }
        );
    }

    $(function() {
        self.initUI();
        self.updatePlot();
    });
}
