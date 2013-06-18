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

    this.div = $(div);

    // update the object according to the user-specified properties
    for (var attrname in properties) {
        if (! (attrname in this)) { console.warn("Alert: property '" + attrname + "' is unknown to the feed object"); }
        this[attrname] = properties[attrname];
    }

    // set up the methods for this object
    this.togglePause = _feed_togglePause;
    this.update      = _feed_update;
    this.updateWith  = _feed_updateWith;
    this.updateTimer = _feed_updateTimer;
    this.initUI      = _feed_initUI;
    this.start       = _feed_start;
    this.pinRow      = _feed_pinRow;
    this.sort        = _feed_sort;

    // Set up the user interface, and start the feed
    this.initUI();
    this.start();
}

// "Pin" a row for the control that has been clicked. Copies the row and places
// it in the pinned container.
function _feed_pinRow(clickedObject) {
    var pinned_row = $(clickedObject).parent().clone();
    var control = pinned_row.find("div.feed_row_pin_control");
    var self = this;
    control.text("unpin");
    control.click(function() { $(this).parent().remove(); });

    // TODO -- should we detect pinned duplicates and avoid them being pinned?
    this.div.find("div.feed_pinned_rows_container").append(pinned_row);
}

// Toggle the paused status of the given feed. Clears the update timeout.
function _feed_togglePause() {
    this.paused = ! this.paused;
    var pause_control = this.div.find('span.feed_pause_control');
    if (this.paused) {
        pause_control.addClass('feed_paused');
        pause_control.text("Paused");
        clearTimeout(this.timeout);
    } else {
        pause_control.removeClass('feed_paused');
        pause_control.text("Running");
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
        var response_obj = JSON.parse(response);
        if (response_obj && response_obj.result == 'success') {
            self.updateWith(response_obj.data);
        } else {
            console.warn("Error in querying '" + url + "': response = '" + response_obj.data + "'");
        }
    });
}

// Update the feed with results returned by the ajax query.
function _feed_updateWith(rows) {
    var self = this;

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
            if (this.formatter) {
                description = this.formatter(row);
            } else if (this.table == 'entries') {
                // TODO -- change team numbers into team names?
                var text = row.text.replace(/#p([A-Za-z])/g, "<a href='problem.php?problem_id=$1'>problem $1</a>");
                text = text.replace(/#t([0-9]+)/g, 
                        function(match, contents, offset, s) {
                            var link = "<a href='team_feed.php?team_id=" + contents + "'>" + id_to_school_name[contents] + "</a>";
                            return link;
                        });
                description = "<span class='priority_" + row.priority + "'>" + row.contest_time + ': ' + text + "</span>";
            } else if (this.table == 'edit_activity') {
                var gitweb_url = '/gitweb/?p=homedirs/.git;a=blob;hb=' + row.git_tag + ';f=team' + row.team_id + "/" + row.path;
                description = "<a href='problem.php?problem_id=" + row.problem_id + "'>Problem " + row.problem_id.toUpperCase() + "</a> &mdash; " +
                              "<a href='team_feed.php?team_id=" + row.team_id + "'>" + id_to_school_name[row.team_id] + "</a> &mdash; " +
                              "<a href='" + gitweb_url + "'>" + row.path + "</a> &mdash; " + 
                              // "<a href='view_source.php?id=" + row.id + "'>" + row.path + "</a> &mdash; " +
                              row.modify_time;
            } else if (this.table == 'submissions') {
                var kattis_result_translator = {
                   'AC'  : "Accepted",
                   '(CE)': "Compile Error",
                   '(IF)': "Illegal Function",
                   'MLE' : "Memory Limit Exceeded",
                   'OLE' : "Output Limit Exceeded",
                   'PE'  : "Presentation Error",
                   'RTE' : "Run Time Error",
                   'TLE' : "Time Limit Exceeded",
                   'WA'  : "Wrong Answer",
                };
                var is_accepted = (row.result == 'AC') ? 'kattis_result_accepted' : 'kattis_result_not_accepted';
                var result = "<span class='" + is_accepted + "'>" + kattis_result_translator[row.result] + "</span>";

                description = row.contest_time + ': ' + 
                             "<a href='problem.php?problem_id=" + row.problem_id + "'>Problem " + row.problem_id.toUpperCase() + "</a> &mdash; " +
                             "<a href='team_feed.php?team_id=" + row.team_id + "'>" + id_to_school_name[row.team_id] + "</a> &mdash; " +
                             row.lang_id + " &mdash; " +
                             result;
            } else {
                // default formatter -- just display everything in the row
                description = '';
                for (var key in row) {
                    description = description + key + ': ' + row[key] + '; ';
                }
            }

            var htmlDescription = $("<div class='feed_row feed_row_recent'>" +
                    "<div class='feed_row_description'>" + description + "</div>" +
                    "<div class='feed_row_pin_control'>pin</div>" +
                    "</div>");
            // add data which allows sorting
            for (var key in row) {
                var val = row[key];
                // treat integers as such
                if (parseInt(val) == val) { val = parseInt(val); }
                htmlDescription.data(key, val);
            }
            
            // enable the row to be pinned
            htmlDescription.find('div.feed_row_pin_control').click(function() { self.pinRow(this); });
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


// create the relevant DOM objects that will contain the feed and its controls
function _feed_initUI() {
    this.div.addClass('feed_container');
    var checkbox_id = "feed_ascending_control_" + this.unique_feed_id;
    var feed_content =
        $("<div class='feed_name'>" + this.name + "</div>\n" +
          "<div class='feed_controls'>\n" +
          "    <span class='feed_pause_control'>Running</span>\n" +
          "    <span class='feed_update'>Last update: <span class='feed_last_update_seconds'>0</span> secs.</span>\n" +
          "    <span class='feed_sort'>Sort: " +
          "         <select class='feed_sort_key'><option value='id'>id</option></select>\n" +
          "         <input type='checkbox' id='" + checkbox_id + "' class='feed_sort_ascending'>" +
          "         <label for='" + checkbox_id + "'>Ascending</label>" +
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

