<?php
$problem_id = 'A';
if (isset($_GET["problem_id"])) {
    $problem_id = strtoupper($_GET["problem_id"]);
}
require_once 'icat.php';
$db = init_db();

?>
<!doctype html>
<html>
<head>
<title>iCAT -- problems</title>

<link rel="stylesheet" type="text/css" href="feed.css" />
<link rel="stylesheet" type="text/css" href="style.css" />
<meta charset="utf-8">
<style type="text/css">
h1 { text-align: center; margin: 0px; }

div#feed_container { display: table; table-layout: fixed; width: 100%; }
div#entries_feed_container, div#edit_activity_feed_container,
div#submission_feed_container { display: table-cell; }

div#statistics_activity_container { white-space: nowrap;  }

div#problem_statistics {
    display: inline-block;
    width: 25em;
}

div#judgement_proportions {
    width: 17em;
    /* I would like to make the judgement_proportions resizeable (i.e. % width) 
        but pie charts break on resize and I'm too tired to create a fix right now */
    height:  200px;
    display: inline-block;
}

div#activity_container {
    background: #ddd;
    width: 40em;
    height: 200px;
    display: inline-block;
}

</style>

<script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
<script type="text/javascript" src="feed.js"></script>
<script type="text/javascript" src="misc.js"></script>
<script type="text/javascript" src="flot/jquery.flot.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.resize.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.navigate.min.js"></script>
<script type="text/javascript" src="flot/jquery.flot.pie.min.js"></script>
<script type="text/javascript" src="activity.js"></script>
<script type="text/javascript">

// Set up the feeds.
$(document).ready(function() {
    var problem_id = '<?php echo $problem_id; ?>';

    new feed("#edit_activity_feed_container", {
        name: "Edit activity",
        table: 'edit_activity_problem',
        conditions: 'problem_id = "' + problem_id + '"',
    });

    new feed("#entries_feed_container", {
        name: 'Katalyze events',
        table: 'entries',
        conditions: 'text regexp "#p' + problem_id + '[[:>:]]"',
    });

    new feed("#submission_feed_container", {
        name: "Submissions",
        table: 'submissions',
        conditions: 'problem_id = "' + problem_id + '"',
    });

    function PieChart(target, problem_id) {
        var self = this;

        self.target = target;
        self.problem_id = problem_id;

        self.plot = function(response) {
            var options = { series: { pie: { show: true } } };
            self.flot = $.plot(self.target, response, options);
        }

        self.updatePlot = function() {
            var url = 'result_per_problem.php';
            $.ajax({
                url: url,
                data: { problem_id: self.problem_id },
                success: self.plot,
                error: function(jqXHR, err) { console.log("updatePlot failed for " + url + ": " + jqXHR + ", " + err); },
                dataType: "json"
            });

            setTimeout(self.updatePlot, 10 * 1000);
        }
        $(function() {
            self.updatePlot();
        });
    }

    new PieChart($("#judgement_proportions"), problem_id);

    new ActivityPlot($("#activity_container"), '', problem_id, true, false);
});

</script>
</head>
<body>

<?php navigation_container(); ?>

<h1>Problem <?php echo $problem_id; ?> &mdash; <?php echo $COMMON_DATA['PROBLEM_ID_TO_NAME'][$problem_id]; ?> </h1>

<div id="statistics_activity_container">
<div id='problem_statistics'>
Statistics about the problem:
<ul>
        <?php
        #########################################
        $result = mysqli_query($db, "select count(distinct team_id) as num_solutions, "
            . " avg(distinct contest_time) as avg_time_to_soln, min(contest_time) as first_solution_time "
            . " from submissions where problem_id = '$problem_id' and result = 'AC'");
        $row = mysqli_fetch_assoc($result);
        $num_solutions = $row['num_solutions'];
        $avg_time_to_soln = intval($row['avg_time_to_soln']);
        $first_solution_time = $row['first_solution_time'];

        #########################################
        $result = mysqli_query($db,
            "select avg(c) as avg_num_incorrect_submissions from "
            . " (select count(*) - 1 as c from submissions where team_id in "
            . " (select distinct(team_id) from submissions where problem_id = '$problem_id' and result = 'AC') "
            . " and problem_id = '$problem_id' group by team_id) as arbitrary_table_name"); 
        $row = mysqli_fetch_assoc($result);
        $avg_num_incorrect_submissions = sprintf("%0.2f", $row['avg_num_incorrect_submissions']);

        #########################################
        $result = mysqli_query($db,
            "select min(contest_time) as first_submission_time, count(*) as count "
            . " from submissions where problem_id = '$problem_id'");
        $row = mysqli_fetch_assoc($result);
        $first_submission_time = $row["first_submission_time"];
        $num_submissions = $row["count"];

        
        #########################################
        $result = mysqli_query($db, "select distinct team_id as team_id from submissions "
            . " where problem_id = '$problem_id' and contest_time = $first_submission_time order by team_id");
        $first_teams_to_submit = array();
        while ($row = mysqli_fetch_assoc($result)) {
            $first_teams_to_submit[] = sprintf("<a href='team.php?team_id=%d'>%d</a>", $row['team_id'], $row['team_id']);
        }
        $first_teams_to_submit = $first_teams_to_submit ? sprintf("(Team %s)", implode(", ", $first_teams_to_submit)) : "";

        #########################################
        $result = mysqli_query($db, "select distinct team_id as team_id from submissions "
            . " where problem_id = '$problem_id' and result = 'AC' and contest_time = $first_solution_time order by team_id");
        $first_teams_to_solve = array();
        while ($row = mysqli_fetch_assoc($result)) {
            $first_teams_to_solve[] = sprintf("<a href='team.php?team_id=%d'>%d</a>", $row['team_id'], $row['team_id']);
        }
        $first_teams_to_solve = $first_teams_to_solve ? sprintf("(Team %s)", implode(", ", $first_teams_to_solve)) : "";

        #########################################
        $submissions_by_language = array();
        $solutions_by_language = array();
        $result = mysqli_query($db, "select count(*) as cnt, lang_id from submissions where problem_id = '$problem_id' group by lang_id");
        while ($row = mysqli_fetch_assoc($result)) {
            $submissions_by_language[] = sprintf("%s: %d", $row["lang_id"], $row["cnt"]);
        }
        $result = mysqli_query($db, "select count(*) as cnt, lang_id from submissions where problem_id = '$problem_id' and result = 'AC' group by lang_id");
        while ($row = mysqli_fetch_assoc($result)) {
            $solutions_by_language[] = sprintf("%s: %d", $row["lang_id"], $row["cnt"]);
        }
        $submissions_by_language = implode(", ", $submissions_by_language);
        $solutions_by_language = implode(", ", $solutions_by_language);

        #########################################
       #$result = mysqli_query($db, "select count(*) as num_started_problem from "
       #    . " (select *, count(*) as c from edit_activity_problem "
       #    . " where problem_id = '$problem_id' group by team_id having c > 1) as arbitrary_table_name");
       #$row = mysqli_fetch_assoc($result);
       #$num_started_problem = $row["num_started_problem"];

        #########################################
        $result = mysqli_query($db, "select count(*) as count_one_edit from "
            . " (select *, count(*) as c from edit_activity_problem "
            . " where problem_id = '$problem_id' group by team_id having c = 1) as arbitrary_table_name");
        if ($result) {
            $row = mysqli_fetch_assoc($result);
            $count_one_edit = $row["count_one_edit"];
        } else {
            $count_one_edit = 0;
        }

        #########################################
        $result = mysqli_query($db, "select count(*) as count_two_plus_edits from "
            . " (select *, count(*) as c from edit_activity_problem "
            . " where problem_id = '$problem_id' group by team_id having c > 1) as arbitrary_table_name");
        if ($result) {
            $row = mysqli_fetch_assoc($result);
            $count_two_plus_edits = $row["count_two_plus_edits"];
        } else {
            $count_two_plus_edits = 0;
        }

        #########################################
        $result = mysqli_query($db, "select count(distinct team_id) as num_submitted_problem "
            . " from submissions where problem_id = '$problem_id'");
        $row = mysqli_fetch_assoc($result);
        $num_submitted_problem = $row["num_submitted_problem"];

        ?>
    <li>Teams solved: <?php echo $num_solutions; ?>
    <li>Teams submitted but not solved: <?php echo $num_submitted_problem - $num_solutions; ?>
    <li>Total submissions: <?php echo $num_submissions; ?>
    <li># solutions by <a href="language.php?problem_id=<?php echo $problem_id; ?>">language</a>: <?php echo $solutions_by_language; ?>
    <li># submissions by language: <?php echo $submissions_by_language; ?>
    <li>Avg. time to solution: <?php echo $avg_time_to_soln; ?> min.
    <li>Avg. # incorrect submissions before accepted: <?php echo $avg_num_incorrect_submissions; ?>
    <li>First submission: <?php printf("%d min. %s", $first_submission_time, $first_teams_to_submit); ?>
    <li>First solution: <?php printf("%d min. %s", $first_solution_time, $first_teams_to_solve); ?>
    <li># teams with 1 edit: <?php echo $count_one_edit; ?>
    <li># teams with 2+ edits: <?php echo $count_two_plus_edits; ?>
    
        <?php
            /*
    <li>Teams that solved this problem (in order of solution):
            do {
                printf("<a href='team.php?team_id=%d'>%d</a>, ", $row['team_id'], $row['team_id']);
            } while ($row = mysqli_fetch_assoc($result));
            */
        ?>
</ul>

</div>
<div id='activity_container'></div>
<div id="judgement_proportions"></div>

</div>

<div id='feed_container'>
<div id='entries_feed_container'></div>
<div id='edit_activity_feed_container'></div>
<div id='submission_feed_container'></div>
</div>

<?php add_entry_container($db); ?>

</body>
</html>

