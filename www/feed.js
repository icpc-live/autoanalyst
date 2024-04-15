/* feed.js
 *
 * A library for creating feeds on a web page. To use it, the html document
 * should contain a div that is uniquely referenced by some id or other
 * selector. For example:
 *  <div id='feed_container'></div>
 *
 * When the document has been loaded, the relevant javascript to create the feed
 * is:
 *  new feed("#feed_container", {
 *      name: 'Displayed name',
 *      conditions: '...', // sql conditions for the where/order by/etc. clauses
 *               // (can be a function which takes the feed object as a parameter)
 *      formatter: function(row) { ... } // code for formatting the content of a returned row
 *      // other properties
 *  });
 *
 * See feed.css for styling properties.
 */

// A global counter to create unique ids for the checkboxes and their labels. Yuck.
var GLOBAL_FEED_ID_COUNTER = 0;

// Return the current system time in milliseconds (just used to show the user
// how long it's been since the last feed update).
function currentTimeSeconds() { return Math.floor(new Date().getTime() / 1000); }

// Construct a new feed object and its relevant DOM objects.
function feed(div, properties) {
    // default properties for this object
    this.name = 'unnamed feed';              // the name is displayed to the user
    this.poll_interval_seconds = 10;         // how often to update the feed
    this.paused = false;                     // whether or not this feed is paused
    this.data_source = 'feed_query.php';     // where to find the data
    this.table = 'entries';                  // which table to look in
    this.conditions = '';                    // conditions on the "where" portion of the sql query; may be string
                                             // or a function that takes the feed object as a parameter; can also
                                             // include things like "limit" and "order by"; should NOT contain the
                                             // keyword "where" -- and must match an allowed query in feed_query.php
    this.limit = null;                       // an integer used to limit the number of returned rows
    this.ids_seen = {};                      // keep track of those rows the feed has seen to avoid displaying duplicates
    this.formatter = null;                   // a method that, given an row, returns a formatted string to display
    this.sort_key = 'id';                    // used to sort the rows
    this.possible_sort_keys = {};            // list of keys that can be used to sort by
    this.sort_ascending = false;             // sort keys ascending or not
    this.unique_feed_id = GLOBAL_FEED_ID_COUNTER;
    GLOBAL_FEED_ID_COUNTER++;

    var data = get_json_synchronous("common_data.php");
    this.teams = data['teams'];
    this.judgements = data['judgements'];

    this.div = $(div);

    // update the object according to the user-specified properties
    for (var attrname in properties) {
        if (! (attrname in this)) { console.warn("Alert: property '" + attrname + "' is unknown to the feed object"); }
        this[attrname] = properties[attrname];
    }

    // figure out if we have any interesting teams, provided as sets of ranges
    this.interesting_teams = null;
    var teams_regexp = /\?.*\bteams=((?:[0-9]+(?:-[0-9]+)?,)*(?:[0-9]+(?:-[0-9]+)?))/;
    var m = window.location.search.match(teams_regexp);
    if (m) {
        this.interesting_teams = [];
        var ranges = m[1].split(',');
        for (i = 0; i < ranges.length; ++i) {
            var rm = ranges[i].match('([0-9]+)-([0-9]+)');
            if (rm) {
                var start = parseInt(rm[1]);
                var end = parseInt(rm[2]);
                for (j = start; j <= end; ++j) {
                    this.interesting_teams.push(j.toString());
                }
            } else {
                this.interesting_teams.push(ranges[i]);
            }
        }
    }

    // set up the methods for this object
    this.togglePause = _feed_togglePause;
    this.update      = _feed_update;
    this.updateWith  = _feed_updateWith;
    this.updateTimer = _feed_updateTimer;
    this.initUI      = _feed_initUI;
    this.start       = _feed_start;
    this.pinRow      = _feed_pinRow;
    this.tweet       = _feed_tweet;
    this.sort        = _feed_sort;
    this.updateTimestamps = _feed_updateTimestamps;

    // Set up the user interface, and start the feed
    this.initUI();
    this.start();
}

// "Pin" a row for the control that has been clicked. Copies the row and places
// it in the pinned container.
function _feed_pinRow(clickedObject) {
    var p = $(clickedObject).parents(".feed_row");
    var pinned_row = p.clone();
    var control = pinned_row.find("div.feed_row_pin_control");
    var self = this;
    control.html("<i class='icon-remove'></i>");
    control.click(function() { $(this).parents(".feed_row").remove(); });

    // TODO -- should we detect pinned duplicates and avoid them being pinned?
    this.div.find("div.feed_pinned_rows_container").append(pinned_row);
}

// Send the row to twitter.
function _feed_tweet(clickedObject) {
    var tweeted_row = $(clickedObject).parents('.feed_row');
    console.log('tweeting');
    console.log(tweeted_row[0].innerHTML);
}

// Toggle the paused status of the given feed. Clears the update timeout.
function _feed_togglePause() {
    this.paused = ! this.paused;
    var pause_control = this.div.find('span.feed_pause_control');
    if (this.paused) {
        pause_control.addClass('feed_paused');
        pause_control.attr('title', 'Resume this feed');
        pause_control.html("<i class='icon-play'></i>");
        clearTimeout(this.timeout);
    } else {
        pause_control.removeClass('feed_paused');
        pause_control.attr('title', 'Pause this feed');
        pause_control.html("<i class='icon-pause'></i>");
        this.update();
    }
}

// Determine if x is a function.
// See http://stackoverflow.com/questions/5999998/how-can-i-check-if-a-javascript-variable-is-function-type
function is_function(x) {
    var getType = {};
    return (x && getType.toString.call(x) == '[object Function]');
}

// Set up the ajax call for updating the feed.
function _feed_update() {
    // if conditions is a function, call it to get the actual conditions
    conditions = is_function(this.conditions) ? this.conditions(this) : this.conditions;
    conditions = escape(conditions);

    var last_id_seen = 0;
    for (var id in this.ids_seen) {
        last_id_seen = Math.max(id, parseInt(last_id_seen));
    }
    var url = this.data_source + '?table=' + this.table + '&id=' + last_id_seen + '&conditions=' + conditions;
    if (this.limit) {
        url += '&limit=' + this.limit;
    }
    var self = this;
    //console.log("about to query " + url);
    $.ajax({ url: url }).done(function(response) {
        //console.log('response: ', response);
        if (response && response.result == 'success') {
            self.updateWith(response.data);
        } else {
            console.warn("Error in querying '" + url + "': response = '" + response.data + "'");
        }
    }).error(function(response) {
        console.log("ERROR:");
        console.log(response);
    });
}

// Zero-pad a number to size.
function pad(num, size) {
    var s = num+"";
    while (s.length < size) s = "0" + s;
    return s;
}

var escapeHtml = (function () {
    var entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
        "/": "&#x2F;",
    };

    return function (string) {
        return String(string).replace(/[&<>"'\/]/g, function (s) {
            return entityMap[s];
        }
        );
    };
} ());
        

// Update the feed with results returned by the ajax query.
function _feed_updateWith(rows) {
    var self = this;
    var data = get_json_synchronous("common_data.php");

    // update the select box the first time we see a row with the column titles
    // of the row
    if (rows.length > 0 && Object.keys(this.possible_sort_keys).length <= 1) {
        // update the sorting fields
        for (var key in rows[0]) {
            this.possible_sort_keys[key] = 1;
        }
        var select = this.div.find("div.feed_controls select.feed_sort_key");
        select.children().remove();
        for (var key in this.possible_sort_keys) {
            select.append($("<option value='" + key + "'>" + key + "</option>"));
        }
    }

    this.div.find('div.feed_row_recent').removeClass('feed_row_recent');

    for (var key in rows) {
        var row = rows[key];
        if (! (row.id in this.ids_seen)) {
            var description = '';
            var row_contest_time = row.contest_time;
            if (row.submission_id) {
                row_contest_time = "<a href='" + submission_url(row.submission_id,data['config'],data['contest']) + "'>" + row.contest_time + '</a>';
            }
            if (this.formatter) {
                description = this.formatter(row);
            } else if (this.table == 'entries') {
                var text = escapeHtml(row.text);
                text = text.replace(/#p([A-Za-z])/g, "<a href='problem.php?problem_id=$1'>problem $1</a>");
                text = text.replace(/#t([0-9]+)/g, 
                        function(match, contents, offset, s) {
                            var link = contents;
                            try { // FIXME: QUICK FIX FOR DATABASE WITH NOT ENOUGH TEAMS IN IT
                                link = "<a href='team.php?team_id=" + contents + "'>" + self.teams[contents]['school_short'] + "</a> (#t" + contents + ")";
                            } catch (e) { }
                            return link;
                        });
                var priority = row.priority;
                if (priority > 2) { // really low priority -- saturate below this
                    priority = 'lowest';
                }
                description = "<span class='priority_" + priority + "'>" + row_contest_time + ': ' + text + "</span>" + 
                              " (<span class='entry_user'>" + row.user + "</span>" +
                              '<span class="feed_timestamp" timestamp="' + row.date + '"></span>)';
            } else if (this.table == 'edit_activity_problem') {
		var d = new Date(0);
		d.setUTCSeconds(row.modify_timestamp);

                var gitweb_url = data['config']['teambackup']['gitweburl'] + ';a=blob;hb=' + row.git_tag + ';f=team' + row.team_id + "/" + row.path;
                description = row.modify_time + ": <a href='problem.php?problem_id=" + row.problem_id + "'>Problem " + row.problem_id.toUpperCase() + "</a> &mdash; " +
                              "<a href='team.php?team_id=" + row.team_id + "'>" + self.teams[row.team_id]['school_short'] + "</a> (#t" + row.team_id + ") &mdash; " +
                              "<a href='" + gitweb_url + "'>" + row.path + "</a> &mdash; " + 
                              "<span class='feed_timestamp' timestamp='" + d.toLocaleString() + "'></span>";
            } else if (this.table == 'submissions') {
                var is_accepted = (row.result == 'AC') ? 'kattis_result_accepted' : 'kattis_result_not_accepted';
                var result = "<span class='" + is_accepted + "'>" + self.judgements[row.result].label_long + "</span>";

                var school_name = "Team " + row.team_id;
                try {
                    // FIXME -- this protection is only here if the school_name
                    // is not in the databases
                    school_name = self.teams[row.team_id]['school_short'];
                } catch (e) {}
                description = row_contest_time + ': ' + 
                             "<a href='problem.php?problem_id=" + row.problem_id + "'>Problem " + row.problem_id.toUpperCase() + "</a> &mdash; " +
                             "<a href='team.php?team_id=" + row.team_id + "'>" + school_name + "</a> (#t" + row.team_id + ") &mdash; " +
                             row.lang_id + " &mdash; " +
                             result +
                             "<span class='feed_timestamp' timestamp='" + row.date + "'></span>";
            } else {
                // default formatter -- just display everything in the row
                description = '';
                for (var key in row) {
                    description = description + key + ': ' + row[key] + '; ';
                }
            }

            var interesting_team_class = '';
            if (this.interesting_teams) {
                var team_matches = row.text.match(/#t[0-9]+/g);
                interesting_team_class = ' uninteresting_team ';
                for (i = 0; team_matches && i < team_matches.length; ++i) {
                    if (this.interesting_teams.indexOf(team_matches[i].substr(2)) >= 0) {
                        interesting_team_class = ' interesting_team ';
                        break;
                    }
                }
            }

            var htmlDescription = $("<div class='feed_row feed_row_recent " + interesting_team_class + "'>" +
                    "<div class='feed_row_description'>" + description + "</div>" +
                    "<div class='feed_row_controls'>" +
                    "<div class='feed_row_pin_control' title='Pin this event'><i class='icon-pushpin'></i></div>" +
                    //"<div class='feed_row_tweet_control' title='Tweet this event'><i class='icon-twitter'></i></div>" +
                    "</div></div>");
            // add data which allows sorting
            for (var key in row) {
                var val = row[key];
                // treat integers as such
                if (parseInt(val) == val) { val = parseInt(val); }
                htmlDescription.data(key, val);
            }
            
            // enable the row to be pinned
            htmlDescription.find('div.feed_row_pin_control').click(function() { self.pinRow(this); });
            htmlDescription.find('div.feed_row_tweet_control').click(function() { self.tweet(this); });
            this.div.find('div.feed_rows_container').prepend(htmlDescription);
            this.ids_seen[row.id] = true;
        }
    }

    // put the rows into the desired order
    this.sort();

    this.lastUpdate = currentTimeSeconds();

    /* Avoid a potential race condition where someone paused while waiting for an ajax response. */
    if (! this.paused) {
        var self = this;
        this.timeout = setTimeout(function() { self.update(); }, this.poll_interval_seconds * 1000);
    }

    self.updateTimestamps();
}

// sort all the rows in the live feed according to the user's selection
function _feed_sort() {
    // find out how the user wants to sort
    this.sort_key = this.div.find("select.feed_sort_key").val();
    this.sort_ascending = this.div.find("input.feed_sort_ascending").is(':checked');

    // detach (but don't destroy) the feed rows from the dom
    var feed_events = this.div.find("div.feed_rows_container div.feed_row").detach();

    // create a proxy of things to sort with indexes into the original list
    var ordering = [];
    var self = this;
    feed_events.each(function(idx, item) {
        ordering.push({ key: $(item).data(self.sort_key), index: idx });
    });
    var multiplier = this.sort_ascending ? 1 : -1;
    ordering.sort(function(a, b) {
        if (a.key == b.key) {
            return multiplier * (feed_events.eq(a.index).data('id') - feed_events.eq(b.index).data('id'));
        }
        return multiplier * (a.key < b.key ? -1 : 1)
    });

    // put the detached rows back in, in the sorted order
    var container = this.div.find("div.feed_rows_container");
    $.each(ordering, function(idx, item) { container.append(feed_events.get(item.index)); });
}

// Update the amount of time since the feed has last been updated, and set
// another timout to update again.
function _feed_updateTimer() {
    this.div.find('span.feed_last_update_seconds').text(currentTimeSeconds() - this.lastUpdate);
    var self = this;
    setTimeout(function() { self.updateTimer(); }, 1000);
}

function _feed_updateTimestamps() {
    var timestamps = this.div.find('.feed_timestamp');
    var now = new Date();
    timestamps.each(function(ndx, element) {
        var e = $(element);
        var ts = e.attr('timestamp');
        if (ts) {
            ts = Date.parse(ts.replace(/^([0-9-]*) ([0-9:]*)$/, '$1T$2Z'));
            var diff_minutes = Math.floor((now - ts) / (60 * 1000));
            var msg = diff_minutes + ' mins. ago';
            e.text(msg);
        }
    });
}

// create the relevant DOM objects that will contain the feed and its controls
function _feed_initUI() {
    this.div.addClass('feed_container');
    var checkbox_id = "feed_ascending_control_" + this.unique_feed_id;
    var feed_content =
        $("<div class='feed_name'>" + this.name + "</div>\n" +
          "<div class='feed_controls'>\n" +
          "    <span class='feed_pause_control' title='Pause this feed'><i class='icon-pause'></i></span>\n" +
          "    <span class='feed_update'>Updated <span class='feed_last_update_seconds'>0</span> secs. ago</span>\n" +
          "    <span class='feed_sort'>Sort: " +
          "         <select class='feed_sort_key' title='Sort feed by database field'><option value='id'>id</option></select>\n" +
          "         <div class='feed_sort_ascending_container'>" +
          "             <input type='checkbox' id='" + checkbox_id + "' class='feed_sort_ascending'>" +
          "             <label for='" + checkbox_id + "'>Ascending</label>" +
          "         </div>" +
          "    </span>\n" +
          "</div>\n" +
          "<div class='feed_pinned_rows_container'></div>\n" +
          "<div class='feed_rows_container'></div>\n");

    // add the feed_content to the div we've been provided
    this.div.append(feed_content);

    // associate the javascript object and DOM object with each other
    this.div.data('feed', this);

    // enable a pause button that is clickable
    var self = this;
    this.div.find('div.feed_controls span.feed_pause_control').click(function() { self.togglePause(); });

    // whenever the sorting key changes, update the list
    this.div.find('div.feed_controls select.feed_sort_key').change(function() { self.sort(); });
    this.div.find('div.feed_controls input.feed_sort_ascending').change(function() { self.sort(); });
}

// start the feed running
function _feed_start() {
    // register the current time as the last update time of the feed
    this.lastUpdate = currentTimeSeconds();
    // start repeatedly updating the feed data
    if (! this.paused) { this.update(); }
    this.updateTimer();
}

