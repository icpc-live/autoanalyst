// some javascript functions that need to live somewhere
$(document).ready(function () {
    $("#searchbox").change(function (evt) {
        var query = $(this).val().replace(/^ *| *$/, '');
        return search_query(query);
    });

    $("#add_entry_container .add_entry_form").submit(function (evt) {
        evt.preventDefault();
        var query_str = "date=" + new Date().toString();
        //var user = $('#user').val();
        var user = $(this).find('input[name="user"]')
        if (user.val() === "") {
            alert("You need to set username.");
            user.css("border-color", "red")
            return false;
        } else {
            user.css("border-color", "")
        }
        $(this).find("input[type=text]").each(function (idx, item) {
            if (query_str) { query_str += "&"; }
            query_str += escape($(item).attr("name")) + "=" + encodeURIComponent($(item).val());
        });
        console.log('query_str = ' + query_str);
        $.ajax({ url: "insert_entry.php?" + query_str }).done(function (response) {
            $('#txtEntry').val('').trigger('input');
            alert("Got this response on insert: " + response);
        });

        return false;
    });

    $("#add_entry_container .add_entry_form input[type=text]").bind("input", function () {
        text = escapeHtml($(this).val());
        previewHtml = ""
        for (const match of text.matchAll(/#t([0-9]+)/g)) {
            previewHtml += generate_team_link(match[1], all_entries_feed.teams)
        }
        $("#add_entry_container .add_entry_form .preview").html(previewHtml)
    })

    $("#hide_uninteresting_teams_checkbox").change(function () {
        if ($(this).is(":checked")) {
            $(".feed_row.uninteresting_team").hide();
        } else {
            $(".feed_row.uninteresting_team").show();
        }
    });
});

function get_json_synchronous(url) {
    var response;
    $.ajax({
        'async': false,
        'global': false,
        'url': url,
        'dataType': 'json',
        'success': function (data) { response = data; },
        'error': function (err) {
            console.log('error in get_json_synchronous:');
            console.log(err);
        }
    })
    return response;
}

function search_query(query, type) {
    if (type == 'team' || /^[0-9]+$/.test(query)) {
        var href = 'team.php?team_id=' + query;
        window.location.assign(href);
    } else if (type == 'problem' || /^[A-Z]$/i.test(query)) {
        var href = 'problem.php?problem_id=' + query;
        window.location.assign(href);
    } else {
        var type_query = "";
        if (type) {
            type_query = "&type=" + type;
        }
        $.ajax({ url: "search.php?query=" + query + type_query }).done(function (response) {
            data = JSON.parse(response);
            if (data.length == 0) {
                $("#searchbox_chooser").text("I couldn't find anything about that query ('" + query + "'). Please enter a team #, problem letter, school name, or country 3-letter abbreviation.");
            } else if (data.length == 1) {
                window.location.assign(data[0].url);
            } else {
                var links = "<p>I found " + data.length + " teams that match:<br>";
                for (var idx in data) {
                    links += "<div class='search_query_result'><a href='" + data[idx].url + "'>" + data[idx].school_name + "</a></div>";
                }
                $("#searchbox_chooser").html(links);
            }
        });
    }
}

function add_query_field(url, field, value) {
    var parts = url.split('?');
    if (parts.length == 1) {
        return url + '?' + field + '=' + value;
    }
    var already_there = RegExp(field + '=[^&]*' + value, 'i');
    var has_field = RegExp('(' + field + '=[^&]*)', 'i');
    if (already_there.test(url)) {
        return url;
    } else if (has_field.test(url)) {
        return url.replace(has_field, '$1' + ',' + value);
    } else {
        return url + '&' + field + '=' + value;
    }
}

function set_query_field(url, field, value) {
    var parts = url.split('?');
    if (parts.length == 1) {
        return url + '?' + field + '=' + value;
    }
    var has_field = RegExp('(' + field + '=[^&]*)', 'i');
    if (has_field.test(url)) {
        return url.replace(has_field, field + '=' + value);
    } else {
        return url + '&' + field + '=' + value;
    }
}

// Returns a link to the submission's page on an external CCS.
function submission_url(submission_id, config, contest) {
    var url = config['CCS']['baseurl'] + config['CCS']['submissionurl'];

    return url.replace('@ID@', submission_id).replace('@CONTEST@', contest['id']);
}

function generate_team_link(team_id, teams) {
    team_id = team_id.toString()
    var link = "#t" + team_id;
    try { // FIXME: QUICK FIX FOR DATABASE WITH NOT ENOUGH TEAMS IN IT
        link = "<a href='team.php?team_id=" + team_id + "'>" + teams[team_id]['school_short'] + "</a> (#t" + team_id + ")";
    } catch (e) { }
    return link;
}