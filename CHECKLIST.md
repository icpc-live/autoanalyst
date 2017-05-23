# Procedure checklists

## Setting up the analyst environment
- Set up as many computers as necessary and connect them to the green network.
- Collect MAC addresses for access to domjudge-green. This can be done using
  `nmap -T5 -sP 192.168.2.38/22` (where `192.168.2.38/22` is your IP address
  and network mask on the green network) or another ping scan and then
  `arp -a`. Make sure to remove computers that shouldn't have access.
- Create key pairs for the computers that need to communicate.
- Make sure domjudge-green has Internet access. Possibly modify DNS server
  settings.
- Make sure all computers are configured with the correct time zone.
- Set up the `analystplus` account for retrieving the Git home directories
  from the CDS and `analyst` for everything else in order to avoid retrieving
  the unfiltered feed.
- Set up the Twitter connection.
    - Set `katalyzer.twitter.enable` to `true`.
    - Update the credentials.

## Before start of contest
- If `code_analyzer/githomes.py` and `katalyze/run_katalyze.sh` are running,
  shut them down.
- Run the database reset scripts, and verify that there are no messages shown in iCat.
  Since there's currently not a single script that will clear all the tables,
  carefully check that the database is empty.
- Delete all files in the `githomes` directory. Including the hidden `.git` directory.
  The exact location is determined by the `teambackup` configuration, but it's
  typically `~/githomes`. The entire directory can be removed. This shouldn't
  be cleared during a contest as the history won't be recreated.
- Populate the database with information about contest, problem set, and teams
  through JSON from the CDS.
  This is done with `code_analyzer/importConfig.py`, but you need to make sure
  that we get the correct data.
- Set the MyICPC and ICPC Live passwords to the same as their CDS passwords.
  The feed shouldn't be used for testing whether the password is correctly set,
  as it doesn't require a password.
- Fill out `code_analyzer/populate.sql` with likely directory names for the
  different problems. Import it into the database.
- Make sure that `katalyzer.notifications.suppressUntil` isn't set, or the
  Katalyzer won't tweet until it reaches that minute.
- Initialize the githomes directory by running the `code_analyzer/prephomes.py` script
- Start the code analyzer.
  In the directory `code_analyzer`, run `./githomes.py`. It doesn't return
  your shell, so make sure you create a screen with `screen -S code-analyzer`. Be
  aware that if the CDS connection is interrupted, the Code Analyzer might
  crash. Hence, start it just before the contest starts.
  Use `screen -r code-analyzer` to reconnect to this screen.
- Start Katalyzer, and verify that the scoreboard is working.
  In the directory `katalyze`, run `./run_katalyze.sh` after creating a screen using
  `screen -S katalyzer`. Be aware that if the CDS connection is interrupted, the
  Katalyzer might silently stop working. Hence, start it just before the
  contest starts.
- Test if the team camera video feeds work. Accessible from the scoreboard in iCat.
- Test if the team desktop video feeds work. Accessible from the scoreboard in iCat.
- Check that DomJudge is reset

## During contest
- Check that the Code Analyzer doesn't crashes and that the Katalyzer doesn't
  stop updating. Possibly set `katalyzer.notifications.suppressUntil` if the
  Katalyzer has to be restarted, to avoid spamming the Twitter API.
- Run `code_analyzer/unclassified.py` to identify files with an unknown
  problem affiliation, and handle them using `code_analyzer/forceFile.py`.

## Scoreboard freeze
- Ensure that Katalyzer events no longer appear in the extended event feed.

## After end of contest
- Back up the database.
