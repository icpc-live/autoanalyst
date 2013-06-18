/* diff.js
 *
 * This module can be used for computing the diff on two divs. To use it, attach
 * a new diff object to an empty div in the html. The code below creates
 * controls and views of the diffs. See the code below and diff.css for more
 * information about the structure created.
 *
 * When the document has been loaded, the relevant javascript to create the diff
 * is:
 *  new diff('some_unique_id', {
 *      a_name: 'name of source A',
 *      b_name: 'name of source A',
 *      a_src: [contents to diff for source A],
 *      b_src: [contents to diff for source B]
 *  });
 *
 * Author: Greg Hamerly (hamerly@cs.baylor.edu)
 */

// A global counter to create unique ids for the checkboxes and their labels. Yuck.
var GLOBAL_DIFF_ID_COUNTER = 0;

// construct a new diff object with the given jquery selector strings
// The requirements are that the named container have the following containers:
// div.diff_controls, div.a_code, div.b_code. The first contains the UI
// controls, the latter two contain the texts to be compared.
function diff(container_selector, options) {
    // create a unique id
    this.id = GLOBAL_DIFF_ID_COUNTER;
    GLOBAL_DIFF_ID_COUNTER++;

    // initialize data
    this.options = {
        a_name: 'source 1',
        b_name: 'source 2',
        a_src: '[no source provided]',
        b_src: '[no source provided]',
        doDiff: true,
        ignoreSpaceChange: true,
        ignoreLeadingTrailingSpace: true,
        ignoreAllSpace: false,
        ignoreCase: true,
      //ignoreNumbers: false,
        showCommonLines: false,
        showLineNumbers: true,
        bindScroll: true
    };
    for (var opt in options) {
        this.options[opt] = options[opt];
    }
    this.container = $(container_selector);

    // initialize methods
    this.lcs = _diff_lcs;
    this.preprocess_text = _diff_preprocess_text;
    this.recompute = _diff_recompute;
    this.render = _diff_render;
    this.setup_ui = _diff_setup_ui;
    this.set_sources = _diff_set_sources;

    // initialize the UI
    this.setup_ui();

    // set the sources and do the diff
    this.set_sources(this.options.a_src, this.options.b_src);
}


// Set the source strings to diff, and recompute the diff.
function _diff_set_sources(a, b, a_name, b_name) {
    if (a) { this.a_src = a.split('\n'); }
    if (b) { this.b_src = b.split('\n'); }
    if (a_name) { this.container.find(".diff_column_a .diff_name").html(a_name); }
    if (b_name) { this.container.find(".diff_column_b .diff_name").html(b_name); }
    if (a || b) { this.recompute(); }
}

// Create the controls for the UI, and bind the scrolling of the two code
// displays.
function _diff_setup_ui() {

    // generate the html for the controls
    var control_settings = [
       { optionName: 'doDiff',                      id: 'checkbox_do_diff_' + this.id,                       requiresRediff: true,  description: 'Diff' },
       { optionName: 'ignoreSpaceChange',           id: 'checkbox_ignore_space_change_' + this.id,           requiresRediff: true,  description: 'Ignore space change' },
       { optionName: 'ignoreLeadingTrailingSpace',  id: 'checkbox_ignore_leading_trailing_space_' + this.id, requiresRediff: true,  description: 'Ignore leading/trailing space' },
       { optionName: 'ignoreAllSpace',              id: 'checkbox_ignore_all_space_' + this.id,              requiresRediff: true,  description: 'Ignore all space' },
       { optionName: 'ignoreCase',                  id: 'checkbox_ignore_case_' + this.id,                   requiresRediff: true,  description: 'Ignore case' },
     //{ optionName: 'ignoreNumbers',               id: 'checkbox_ignore_numbers_' + this.id,                requiresRediff: true,  description: 'Ignore numbers' },
       { optionName: 'showCommonLines',             id: 'checkbox_show_common_lines_' + this.id,             requiresRediff: true,  description: 'Common lines' },
       { optionName: 'showLineNumbers',             id: 'checkbox_show_line_numbers_' + this.id,             requiresRediff: false, description: 'Line numbers' },
       { optionName: 'bindScroll',                  id: 'checkbox_bind_scroll_' + this.id,                   requiresRediff: false, description: 'Bind scrolling' },
    ];

    var controls = "<div class='diff_controls'>\n";
    for (var i = 0; i < control_settings.length; ++i) {
        var c = control_settings[i];
        var checked = this.options[c.optionName] ? " checked" : "";
        controls += "<span class='diff_option'>" +
            "<input type='checkbox' id='" + c.id + "' value='" + c.optionName + "' requiresRediff=" + c.requiresRediff + checked + ">" +
            "<label for='" + c.id + "'>" + c.description + "</label></span>\n";
    }
    controls += "</div>";

    this.container.append($(controls));
    this.controls = this.container.find("div.diff_controls");

    this.container.append($("<div class='diff_column_a'>" + 
                            "<div class='diff_name'>" + this.options.a_name + "</div>" +
                            "<div class='diff_code'></div>" +
                            "</div>" +
                            "<div class='diff_column_b'>" + 
                            "<div class='diff_name'>" + this.options.b_name + "</div>" +
                            "<div class='diff_code'></div>" +
                            "</div>"));

    // remember a and b divs
    this.a = this.container.find('div.diff_column_a div.diff_code');
    this.b = this.container.find('div.diff_column_b div.diff_code');

    // bind actions for when the controls change
    var self = this;
    this.controls.find('input:checkbox[requiresRediff=true]').change(function() { self.recompute(); });

    this.controls.find('input:checkbox[value=showLineNumbers]').change(function(checkbox) {
        if (this.checked) {
            self.container.find('.diff_line_number').removeClass('diff_hidden');
        } else {
            self.container.find('.diff_line_number').addClass('diff_hidden');
        }
    });

    var bindScroll = function() {
        self.a.scroll(function() { self.b.scrollTop(self.a.scrollTop()); self.b.scrollLeft(self.a.scrollLeft()) });
        self.b.scroll(function() { self.a.scrollTop(self.b.scrollTop()); self.a.scrollLeft(self.b.scrollLeft()) });
    }

    this.controls.find('input:checkbox[value=bindScroll]').change(function(checkbox) {
        if (this.checked) {
            bindScroll();
        } else {
            self.a.unbind('scroll');
            self.b.unbind('scroll');
        }
    });

    // connect scrolling
    if (this.options.bindScroll) { bindScroll(); }
}

// Take all the lines in this.a_src and this.b_src, and apply the
// transformations specified in options so that we can compare the transformed
// strings directly.
function _diff_preprocess_text() {
    // these transforms are triggered if they are found in "options"
    var transforms = {
        ignoreAllSpace:             function(x) { return x.replace(/\s+/g, "");  },
        ignoreSpaceChange:          function(x) { return x.replace(/\s+/g, " "); },
        ignoreLeadingTrailingSpace: function(x) { return x.replace(/^\s+|\s+$/g, ""); },
        ignoreCase:                 function(x) { return x.toLowerCase(); },
      //ignoreNumbers:              function(x) { return x.replace(/[0-9]+/g, ""); }
    };

    var self = this;
    var transform = function(x) {
        for (var t in transforms) {
            if (self.options[t]) {
                x = transforms[t](x);
            }
        }
        return x;
    }

    // do the transformation and return the results
    return {a: this.a_src.map(transform), b: this.b_src.map(transform)};
}

// Compute the longest common subsequence of a and b, breaking ties
// arbitrarily. Return the two maximum length subsequences as an array of pairs
// of indexes.
function _diff_lcs(a, b) {
    // Compute the lcs_length and lcs_predecessor tables. We only keep two rows
    // of the LCS length (enough to construct the next row), but we construct
    // the full lcs_predecessor table.
    var lcs_length_prev = [], lcs_length_current = [], lcs_predecessor = [];
    for (var a_ndx = a.length - 1; a_ndx >= 0; --a_ndx) {
        lcs_predecessor[a_ndx] = [];

        for (var b_ndx = b.length - 1; b_ndx >= 0; --b_ndx) {
            if (a[a_ndx] == b[b_ndx]) {
                // if we have a match, we can extend the length of some subsequence
                lcs_predecessor[a_ndx][b_ndx] = 'd'; // move in the "diagonal" direction -- both sequences at once
                var prev_len = (a_ndx + 1 < a.length && b_ndx + 1 < b.length) ? lcs_length_prev[b_ndx + 1] : 0;
                lcs_length_current[b_ndx] = 1 + prev_len;
            } else {
                // otherwise, there's no match, so take the longest subsequence up to this point
                var a_len = a_ndx + 1 < a.length ? lcs_length_prev[b_ndx] : 0;
                var b_len = b_ndx + 1 < b.length ? lcs_length_current[b_ndx + 1] : 0;
                if (b_len < a_len) {
                    lcs_predecessor[a_ndx][b_ndx] = 'a'; // move in the "a" direction
                    lcs_length_current[b_ndx] = a_len;
                } else {
                    lcs_predecessor[a_ndx][b_ndx] = 'b'; // move in the "b" direction
                    lcs_length_current[b_ndx] = b_len;
                }
            }
        }

        // swap the two length rows
        var lcs_temp = lcs_length_prev;
        lcs_length_prev = lcs_length_current;
        lcs_length_current = lcs_temp;
    }

    // find the longest common subsequence
    var lcs = [];
    var a_ndx = 0, b_ndx = 0;
    while (a_ndx < a.length || b_ndx < b.length) {
        if (a_ndx < this.a_src.length && b_ndx < this.b_src.length && lcs_predecessor[a_ndx][b_ndx] == 'd') {
            lcs.push([a_ndx, b_ndx]);
            a_ndx++;
            b_ndx++;
        } else if (a_ndx >= this.a_src.length || lcs_predecessor[a_ndx][b_ndx] == 'b') {
            b_ndx++;
        } else {
            a_ndx++;
        }
    }

    return lcs;
}

// Render the source lines as divs, labeling them appropriately as matching or
// not matching, and inserting empty lines to pad as necessary. Return an
// object containing the two html strings (one for each side of the diff).
function _diff_render(lcs) {
    // push on sentinel values -- so we don't have to do extra work for the
    // last item in the loop below
    lcs.push([this.a_src.length, this.b_src.length]);

    if (! this.options.showCommonLines) {
        // null out long runs of common lines so they won't display in the main
        // loop below
        var show_length = 2; // modify this to show more/less context around hidden common lines
        var s = show_length * 2;
        var lcs_hidden = lcs.slice(); // copy the array
        for (var i = 0; i + 3 * show_length < lcs.length; ++i) {
            // Check that the numbering is continuous between the current line
            // and the one that is 2*show_length away. If so, we can hide lines
            // in between them. Need to have at least two potential hidden
            // lines to make it worthwhile (avoid hiding one line which could
            // just be shown).
            if ((lcs[i][0] + s == lcs[i + s][0] && lcs[i][1] + s == lcs[i + s][1]) &&
                (lcs[i][0] + s + 1 == lcs[i + s + 1][0] && lcs[i][1] + s + 1 == lcs[i + s + 1][1])) {
                lcs_hidden[i + show_length] = lcs_hidden[i + show_length + 1] = null;
            }
        }
        lcs = lcs_hidden; // replace the lcs with the version containing nulls
    }

    var should_show_hidden_count = false; // keep track of whether we should insert a hidden line count
    var last_line_shown = -1; // for counting how many lines have been hidden
    var last_an = -1, last_bn = -1; // indexes for the last matched lines we printed
    var a_lines = [], b_lines = []; // these will contain the rendered html, one entry per line

    var line_number_span = "<span class='diff_line_number" + (this.options.showLineNumbers ? "" : " diff_hidden") + "'>";
    var nomatch_div = "<div class='diff_line diff_nomatch'>";
    var match_div = "<div class='diff_line diff_match'>";
    var empty_line = "<div class='diff_line diff_empty'></div>";

    // go over all pairs of matching lines, and render them (first adding any
    // non-matching and empty lines in between matching regions)
    for (var i = 0; i < lcs.length; ++i) {

        // lcs[i] is null if we should not render the line due to hiding
        if (lcs[i]) {
            var an = lcs[i][0], bn = lcs[i][1];

            if (should_show_hidden_count) {
                var hidden_line = "<div class='diff_line diff_hidden_common_lines'>[" + (i - last_line_shown - 1) + " COMMON LINES HIDDEN]</div>";
                a_lines.push(hidden_line);
                b_lines.push(hidden_line);
            }

            // push on all the non-matching lines we have skipped to get to the
            // current matching line
            for (var j = last_an + 1; j < an; ++j) {
                a_lines.push(nomatch_div + line_number_span + (j+1) + "</span>" + this.a_src[j] + "</div>");
            }
            for (var j = last_bn + 1; j < bn; ++j) {
                b_lines.push(nomatch_div + line_number_span + (j+1) + "</span>" + this.b_src[j] + "</div>");
            }

            // fill out with empty lines
            while (a_lines.length < b_lines.length) { a_lines.push(empty_line); }
            while (b_lines.length < a_lines.length) { b_lines.push(empty_line); }

            // don't try to get the last LCS entry; it is invalid (sentinel value past the end of this.a_src/b_src)
            if (i + 1 == lcs.length) { break; }

            // push on the matching lines
            a_lines.push(match_div + line_number_span + (an + 1) + "</span>" + this.a_src[an] + "</div>");
            b_lines.push(match_div + line_number_span + (bn + 1) + "</span>" + this.b_src[bn] + "</div>");

            // remember the last common line rendered so next time through we
            // can potentially render non-matching lines
            last_an = an;
            last_bn = bn;
            last_line_shown = i;
            should_show_hidden_count = false; // if we had been showing anything hidden, don't do it anymore
        } else {
            // If we didn't render anything this time (because the line is
            // hidden), then we need to do two things:
            // 1. increment last_an/last_bn so that we don't show intermediate
            // matching lines as non-matching, and
            last_an++;
            last_bn++;
            // 2. remember that next time there's anything to show, first show
            // that lines have been hidden.
            should_show_hidden_count = true;
        }
    }

    // join the arrays and return them
    return { a: a_lines.join(''), b: b_lines.join('') };
}

// Gather up the current options (from the control checkboxes) and re-compute
// the diff according to those options.
function _diff_recompute() {
    this.options = {};
    var self = this;
    this.controls.find('input:checkbox').each(function(index, element) {
        if (element.checked) {
            self.options[element.value] = true;
        }
    });

    var a_html = [], b_html = [];
    if (this.options.doDiff) {
        var transformed = this.preprocess_text();
        var lcs = this.lcs(transformed.a, transformed.b);
        var ab_diff = this.render(lcs);
        a_html = ab_diff.a;
        b_html = ab_diff.b;
    } else {
        var div_and_span = "<div class='diff_line diff_nodiff'><span class='diff_line_number" + (this.options.showLineNumbers ? "" : " diff_hidden") + "'>";
        for (var i = 0; i < this.a_src.length; ++i) {
            a_html.push(div_and_span + (i+1) + "</span>" + this.a_src[i] + "</div>");
        }
        for (var i = 0; i < this.b_src.length; ++i) {
            b_html.push(div_and_span + (i+1) + "</span>" + this.b_src[i] + "</div>");
        }
        a_html = a_html.join('');
        b_html = b_html.join('');
    }
    this.a.html(a_html);
    this.b.html(b_html);
}

