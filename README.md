# RankTracker

RankTracker is a tool for tracking your Rocket League rankings over time. It scans the Rocket League log files and saves the match results to CSV files. Rocket League creates a new log file everytime the game is started, and keeps a few as backups.

Running `RankTracker.bat` will parse the log files, but it's more convenient to do this automatically when Rocket League closes. That's why there is an [AutoHotkey](http://www.autohotkey.com/) script included that waits for the Rocket League process to finish, and runs the tool when Rocket League is shut down.

### Installation
1. Download the [distribution zip](https://github.com/trew/RankTracker/releases/tag/1.0.0) and unpack it.
2. The bin folder contains a file called `RankTracker.bat`. This is the file that will parse the log files.
3. Modify the Autohotkey script (`scripts/ScanRLLogs.ahk`) with the path to RankTracker.bat.
4. (optional) Add a link to the autohotkey script in your startup folder.
5. Run the AutoHotkey script to start listening for the Rocket League process or run RankTracker.bat to import the match results available in your current logs.