<?php
require_once 'icat.php';

$db = init_db();
?>
<!doctype html>
<html>

<head>
    <title>iCAT overview</title>

    <link rel="stylesheet" type="text/css" href="style.css" />
    <link rel="stylesheet" type="text/css" href="feed.css" />
    <meta charset="utf-8">
    <style type="text/css">
        div#leftColumn,
        div#rightColumn {
            display: inline-block;
            width: 49%;
            vertical-align: text-top;
        }

        div#high_priority_feed .feed_rows_container,
        div#analyst_entries_feed .feed_rows_container {
            height: 30em;
        }

        div#all_entries_feed .feed_rows_container,
        div#first_solution_feed .feed_rows_container,
        div#important_submissions_feed .feed_rows_container {
            height: 20em;
        }
    </style>

    <script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
    <script type="text/javascript" src="feed.js"></script>
    <script type="text/javascript" src="misc.js"></script>
    <script type="text/javascript">
        var all_entries_feed;

        $(document).ready(function() {
            new feed("#high_priority_feed", {
                name: "High priority katalyze events",
                table: 'entries',
                conditions: 'priority <= 0'
            });

            /* new feed("#first_solution_feed", {
                name: "First solutions",
                table: 'entries',
                conditions: 'text regexp "the first team to solve"'
            }); */

            new feed("#important_submissions_feed", {
                name: "Important submissions",
                table: 'entries',
                conditions: 'submission_id is not NULL AND priority <= 2 AND user = "katalyzer"'
            });

            all_entries_feed = new feed("#all_entries_feed", {
                name: "All katalyze events",
                table: 'entries',
                conditions: '1'
            });

            new feed("#analyst_entries_feed", {
                name: "All human analysts",
                table: 'entries',
                conditions: 'user != "katalyzer"'
            });
        });
    </script>
</head>

<body>
    <?php navigation_container() ?>
    <div id="leftColumn">
        <div id="high_priority_feed"></div>
        <div id="all_entries_feed"></div>
    </div>
    <div id="rightColumn">
        <div id="analyst_entries_feed"></div>
        <!-- <div id="first_solution_feed"></div> -->
        <div id="important_submissions_feed"></div>
    </div>

    <?php add_entry_container($db); ?>

    <div id="all_codalyzer_feed"></div>
</body>

</html>