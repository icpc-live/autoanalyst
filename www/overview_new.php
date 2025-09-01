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
html, body {
  margin: 0;
}

/* Общий контейнер */
.split-container {
  display: flex;
  height: 100vh; /* на весь экран */
  overflow: hidden;
}

/* Левая и правая панели */
.pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 10%; /* чтобы совсем не схлопывались */
  max-width: 90%;
}

/* Разделитель */
.divider {
  width: 5px;
  background: #ccc;
  cursor: col-resize;
}

/* Контейнер фида */
.feed_container {
  border: 1px solid #ddd;
  flex: 1;
  background: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.feed_name,
.feed_controls,
.feed_pinned_rows_container {
  flex: 0 0 auto;
  padding: 4px 8px;
  border-bottom: 1px solid #ddd;
}

.feed_rows_container {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 4px;
  max-height: none !important;
  height: none !important;
}

.chat_input {
  position: unset !important;
  bottom: 0px !important;
  width: 100% !important;
  left: 5% !important;
  background: unset !important;
  border: 0 !important;
}
.chat_input form {
  width: 100%;
  display: flex;
  gap: 4px;
  /* padding: 6px; */
}
.chat_input input {
  flex: 1;
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.chat_input button {
  padding: 6px 12px;
  border: none;
  background: #337ab7;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}
.chat_input button:hover { background: #286090; }


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

.feed_container {
  border: 1px solid #ccc;
  font-family: sans-serif;
  background: #fff;
}

/* Заголовок */
.feed_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f6f6f6;
  padding: 4px 8px;
  border-bottom: 1px solid #ddd;
}
.feed_name {
  font-weight: bold;
  font-size: 1.1em;
  text-transform: uppercase;
}
.feed_controls {
  display: flex;
  gap: 6px;
  font-size: 0.85em;
  align-items: center;
}
.icon-btn {
  background: #eee;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  padding: 2px 6px;
}
.icon-btn:hover { background: #ddd; }

/* Лента */
.feed_rows_container {
  max-height: 20em;
  overflow-y: auto;
}
.feed_row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px !important;
  margin: 4px;
  border-radius: 6px !important;
  background: #fafafa !important;
  font-size: 1em !important;
}
.feed_row:hover { background: #f0f0ff !important; }
.feed_text {
  flex: 1;
}
.feed_meta {
  color: #666;
  font-size: 75% !important;
}

/* Приоритеты */
.feed_container .feed_row span.feed_text::before {
  content: " ";
  opacity: 0;
  margin-right: 2px;
  display: inline-block;
}
.feed_container .feed_row span.feed_text.priority_0 { font-size: 100% !important; font-weight: bold; }
.feed_container .feed_row span.feed_text.priority_0::before {
  opacity: 1;
  content: "⚠️";
}
.feed_container .feed_row span.priority_1 { font-size: 100% !important; }
.feed_container .feed_row span.priority_2 { font-size: 85% !important; }
.feed_container .feed_row span.priority_lowest { font-size: 75% !important; }

/* Особые */
.feed_container .feed_row:has(.studio_message) { background:rgb(253, 212, 212) !important; border-left-color: #339; }
.feed_container .feed_row.interesting_team { background: #f0fff0 !important; }

.feed_container .feed_row span.feed_meta { white-space: nowrap; }

/* Pinned */
.feed_pinned_rows_container {
  border-bottom: 2px solid #d33;
  padding: 4px;
}
.feed_pinned_rows_container:empty { display: none; }

.feed_container .feed_row .feed_row_controls div {
  border-radius: 4px;
}

.feed_container .feed_pinned_rows_container {
  border-top: 0;
}

    </style>

    <script type="text/javascript" src="katalyze/web/jquery-1.6.1.js"></script>
    <script type="text/javascript" src="feed.js"></script>
    <script type="text/javascript" src="misc.js"></script>
    <script type="text/javascript">
$(document).ready(function() {
    const dragbar = document.getElementById("dragbar");
    const container = document.querySelector(".split-container");
    let isDragging = false;

    dragbar.addEventListener("mousedown", (s) => {
      isDragging = true;
      document.body.style.cursor = "col-resize";
    });

    document.addEventListener("mousemove", (e) => {
      e.stopPropagation();
      if (!isDragging) return;
      let offsetRight = container.clientWidth - (e.clientX);
      let leftPane = document.querySelector(".left-pane");
      let rightPane = document.querySelector(".right-pane");

      leftPane.style.flex = "none";
      leftPane.style.width = e.clientX + "px";

      rightPane.style.flex = "1";
    });

    document.addEventListener("mouseup", e => {
      isDragging = false;
      document.body.style.cursor = "default";
    });
});

var all_entries_feed;

const entries_formatter = (row, data, feed) => {
    let text = escapeHtml(row.text);
    text = text.replace(/#p([A-Za-z])/g, "<a href='problem.php?problem_id=$1'>$1</a>");
    text = text.replace(/#t([0-9]+)/g,
        function (match, contents, offset, s) {
        return generate_team_link(contents, feed.teams);
    });

    let row_contest_time = row.contest_time;
    if (row.submission_id) {
        row_contest_time = "<a href='" + submission_url(row.submission_id, data['config'], data['contest']) + "'>" + row.contest_time + '</a>';
    }

    let priority = row.priority;
    if (priority > 2) { // really low priority -- saturate below this
        priority = 'lowest';
    } else if (priority < 0) {
        priority = 0;
    }

    let message_class = `priority_${priority}`;
    if (row.user === "live-studio" || row.user === "lperovskaya") {
        message_class += " studio_message"
    }

    return `
<span class="feed_text ${message_class}">${text}</span>
<span class="feed_meta">by ${row.user} · ${row_contest_time}</span>`;
};

$(document).ready(function() {
    new feed("#high_priority_feed", {
        name: "Important events",
        table: 'entries',
        conditions: 'priority <= 0 OR submission_id is not NULL AND priority <= 2 AND user = "katalyzer"',
        formatter: entries_formatter,
    });

    new feed("#analyst_entries_feed", {
        name: "All human analysts",
        table: 'entries',
        conditions: 'user != "katalyzer"',
        formatter: entries_formatter,
    });
});
    </script>
</head>

<body>
    <div class="split-container">
      <div class="pane left-pane">
        <div class="feed_container" id="analyst_entries_feed">
        </div>
        <div id="add_entry_container" class="chat_input">
          <form class="add_entry_form">
              <?php
                $entry_username = "";
                if (isset($_GET['entry_username'])) {
                    $entry_username = $_GET['entry_username'];
                } else if (isset($_SESSION['entry_username'])) {
                    $entry_username = $_SESSION['entry_username'];
                }
              ?>
              <input type="text" placeholder="Type your message as <?php echo $entry_username ?>..." name="text"/>
              <input type="hidden" name="user" value="<?php echo $entry_username ?>"/>
              <input type="hidden" name="priority" value="2"/>
              <button>Send</button>
          </form>
        </div>
      </div>

      <div class="divider" id="dragbar"></div>

      <div class="pane right-pane">
        <div class="feed_container" id="high_priority_feed">
        </div>
      </div>
    </div>
</body>

</html>