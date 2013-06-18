// for 2012 wf teams -- quick hack
var id_to_school_name = {
'95': 'U New Sout',
'87': 'U Canterbu',
'81': 'UF Paraná',
'50': 'PUCP',
'77': 'UNI',
'79': 'UFCG',
'29': 'ITA',
'80': 'UF Pernamb',
'34': 'ITESM-Pueb',
'74': 'Uniandes',
'78': 'UTM',
'30': 'INTEC',
'73': 'UH',
'75': 'UNAL Bogot',
'96': 'IME-USP',
'76': 'UNC-FaMAF',
'71': 'U Buenos A',
'72': 'U de Guana',
'82': 'UF Rio de ',
'35': 'Jagielloni',
'11': 'Charles U ',
'45': 'NTU KhPI',
'86': 'U Buchares',
'63': 'Taurida NU',
'83': 'UP Catalun',
'61': 'SPbSU ITMO',
'42': 'Moscow Sta',
'8': 'Belarusian',
'53': 'Saratov St',
'60': 'St. Peters',
'41': 'MIPT',
'107': 'Ural FU',
'3': 'Altai STU',
'70': 'Ufa SATU',
'7': 'BSUIR',
'48': 'UNN',
'93': 'Latvia',
'69': 'Udmurt SU',
'67': 'TSU',
'36': 'Kazakh-Bri',
'1': 'Aalto U',
'14': 'TU Delft',
'108': 'Volgograd ',
'16': 'TUE',
'51': 'PUT',
'106': 'U Wroclaw',
'103': 'U Warsaw',
'100': 'U Tokyo',
'44': 'National T',
'49': 'Peking U',
'109': 'Wuhan U',
'68': 'Tsinghua U',
'17': 'Fudan U',
'6': 'BUPT',
'56': 'SJTU',
'111': 'ZJUT',
'27': 'IIT Madras',
'23': 'HKUST',
'112': 'Zhongshan ',
'25': 'IIT Delhi',
'38': 'Kyoto U',
'59': 'SEI - ECNU',
'54': 'Seoul Nati',
'31': 'IIIT Hyder',
'21': 'GUCAS',
'5': 'BUET',
'55': 'SUST',
'37': 'Korea U',
'46': 'NUDT',
'28': 'ITB',
'26': 'IIT Kanpur',
'66': 'Tianjin U',
'64': 'CUHK',
'18': 'Fuzhou U',
'47': 'NU Singapo',
'65': 'U.E.C.',
'99': 'UP Diliman',
'32': 'IIUM',
'98': 'U Tehran',
'57': 'Sharif UT',
'110': 'Zhejiang U',
'91': 'UESTC',
'58': 'Sichuan U',
'12': 'CMI',
'43': 'Nanyang TU',
'24': 'IIT-Chicag',
'105': 'U Wisconsi',
'33': 'Iowa State',
'94': 'U Minnesot',
'40': 'Messiah Co',
'92': 'U Illinois',
'90': 'U Chicago',
'102': 'Tulsa',
'84': 'U Alberta',
'15': 'Duke',
'13': 'William & ',
'52': 'Princeton',
'10': 'Carnegie M',
'101': 'U of Toron',
'104': 'U Waterloo',
'97': 'USC',
'62': 'Stanford U',
'89': 'U Central ',
'19': 'Georgia Te',
'22': 'Harvard',
'39': 'MIT',
'85': 'U British ',
'4': 'AUS',
'88': 'U Cape Tow',
'20': 'GUC',
'2': 'Alex Univ',
'9': 'Cairo-FCI',
};

function ActivityPlot(target, team_id, problem_id) {
    var self = this;

    self.target = target;
    self.team_id = team_id;
    self.problem_id = problem_id;
    self.source_url = 'activity_data_source.php';
    self.target.data("activity_plot", self);
    self.update_interval = 5 * 1000;

    self.plot = function(response) {
        var problems_used = response.problems_used;
        var num_problems = problems_used.length;
        var problem_height = response.max_problems_per_bin;
        var plot_height = num_problems * problem_height;
        var minY = - problem_height / 4;
        var maxY = plot_height + problem_height / 4;

        var ticks = [];
        var colors = [];
        // colors for the 2012 finals
        var all_colors = {
            A: '#ffffff',
            B: '#ff1546',
            C: '#7818a4',
            D: '#000000',
            E: '#31e113',
            F: '#f3b3c8',
            G: '#ff8315',
            H: '#c6c6c6',
            I: '#caf727',
            J: '#f3c13b',
            K: '#00a0dc',
            L: '#f7f417'
        };
        for (var i = 0; i < num_problems; ++i) {
            var pid = problems_used[i].toUpperCase();
            ticks.push([i * problem_height, pid]);
            colors.push(all_colors[pid]);
        }

        var options = {
            // FIXME -- need to color the problems consistently with the balloons
            //colors: [ "#000", "#00f", "#0f0", "#0ff", "#f00", "#f0f", "#ff0", "#999", "#99f", "#9f9", "#9ff", "#f99" ],
            //colors: ['#ffffff', '#ff1546', '#7818a4', '#000000', '#31e113', '#f3b3c8', '#ff8315', '#c6c6c6', '#caf727', '#f3c13b', '#00a0dc', '#f7f417' ], // colors for the 2012 finals
            colors: colors,
            grid: { hoverable: true, clickable: true, },
            yaxis: { ticks: ticks,
                zoomRange: [maxY, maxY], // do not allow zooming in on the y-axis
                panRange: [maxY, maxY], // do not allow panning on the y-axis
                min: minY,
                max: maxY,
            },
            xaxis: {
                zoomRange: [20, 321], // extra space to allow for the legend
                panRange: [0, 320], // extra space to allow for the legend
                min: 0,
                max: 320, // extra space to allow for the legend
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
                'background-color': '#efe',
                opacity: 0.80
            }
        ).appendTo(target);
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

        setTimeout(self.updatePlot, self.update_interval);
    }

    self.initUI = function() {
        self.target.bind("plothover", 
            function(evt, pos, item) {
                if (item) {
                    self.target.find("div.hoverinfo").remove();
                    var content = '(unknown)';
                    if (item.series && item.series.submissionInfo) {
                        // for the submissions
                        content = item.series.label + ": " 
                                + id_to_school_name[item.series.submissionInfo[item.dataIndex].team_id] +
                                  " problem " + item.series.submissionInfo[item.dataIndex].problem_id +
                                  " at time " + item.datapoint[0];
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
                    var query = '?team_id=' + item.series.submissionInfo[item.dataIndex].team_id;
                    var current = window.location.href;
                    // hack...
                    if (/activity\.php/.test(current)) {
                        current = current.replace(/\?.*/, '') + query;
                    } else {
                        current = current.replace(/[^\/]+$/, '') + 'activity.php' + query;
                    }
                        
                    //var href = window.location.href.replace(/\?.*/, '') + '?team_id=' + item.series.submissionInfo[item.dataIndex].team_id;
                    window.location.assign(current);
                }
            }
        );
    }

    $(function() {
        self.initUI();
        self.updatePlot();
    });
}
