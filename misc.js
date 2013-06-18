// some javascript functions that need to live somewhere
$(document).ready(function() {
    $("#searchbox").change(function(evt) {
        var query = $(this).val().replace(/^ *| *$/, '');
        if (/^[0-9]+$/.test(query)) {
            var href = 'team_feed.php?team_id=' + query;
            window.location.assign(href);
        } else if (/^[A-Z]$/i.test(query)) {
            var href = 'problem.php?problem_id=' + query;
            window.location.assign(href);
        } else {
            $.ajax({url: "search.php?query=" + query}).done(function(response) {
                data = JSON.parse(response);
                if (data.length == 0) {
                    $("#searchbox_chooser").text("I couldn't find anything about that query. Please enter a team #, problem letter, school name, or country 3-letter abbreviation.");
                } else if (data.length == 1) {
                    window.location.assign(data[0].url);
                } else {
                    var links = "I found several teams that match:<br>";
                    for (var idx in data) {
                        links += "<a href='" + data[idx].url + "'>" + data[idx].school_name + "</a><br>";
                    }
                    $("#searchbox_chooser").html(links);
                }
            });
        }
    });

    $("#add_entry_container .add_entry_form").submit(function(evt) {
        evt.preventDefault();
        var query_str = "date=" + new Date().toString();
        $(this).find("input[type=text]").each(function(idx, item) {
            if (query_str) { query_str += "&"; }
            query_str += escape($(item).attr("name")) + "=" + escape($(item).val());
        });
        console.log('query_str = ' + query_str);
        $.ajax({url: "insert_entry.php?" + query_str }).done(function(response) {
            alert("Got this response on insert: " + response);
        });

        return false;
    });
});
