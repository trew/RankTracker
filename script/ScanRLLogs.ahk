; Listen for Rocket League

ScanProgramFolder = <Absolute Path to folder containing RankTracker.bat>
ScanProgramFile = RankTracker.bat
RocketLeagueExe = RocketLeague.exe

Loop
{
	; Wait for Rocket League to start
	Process, Wait, %RocketLeagueExe%
	{
		; Oh, Rocket League started! Let's wait for it to finish.
		Process, WaitClose, %RocketLeagueExe%
		{
			; Rocket League exited, let's scan now.
			Run, %ScanProgramFolder%\%ScanProgramFile%, %ScanProgramFolder%
		}
	}
}
