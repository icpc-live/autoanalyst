import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import org.icpclive.cds.ContestUpdate
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class ContestEmulation(val flowCollector: FlowCollector<ContestUpdate>, numTeams: Int, numProblems: Int, contestLength: Duration = 5.hours, freezeTime: Duration = 4.hours) {
    private var contestInfo = ContestInfo(
        name = "Test Contest",
        status = ContestStatus.BEFORE(scheduledStartAt = Clock.System.now()),
        resultType = ContestResultType.ICPC,
        contestLength = contestLength,
        freezeTime = freezeTime,
        groupList = emptyList(),
        teamList = (1 .. numTeams).map { TeamInfo(id = it.toTeamId(), fullName = "Team $it full name", displayName = "Team $it", groups = emptyList(), hashTag = null, medias = emptyMap(), isHidden = false, isOutOfContest = false, organizationId = null) },
        problemList = (1..numProblems).map{ ProblemInfo(id= ('A' + it - 1).toString().toProblemId(), displayName = "Problem $it", fullName = "Problem $it full name", ordinal = it) },
        organizationList = emptyList(),
        penaltyRoundingMode = PenaltyRoundingMode.EACH_SUBMISSION_DOWN_TO_MINUTE,
        languagesList = listOf(LanguageInfo("cpp".toLanguageId(), "C++", emptyList()))
    )

    private val runs = HashMap<RunId, RunInfo>()

    suspend fun sendInitialUpdate() {
        flowCollector.emit(InfoUpdate(contestInfo))
    }

    suspend fun start() {
        require(contestInfo.status is ContestStatus.BEFORE)
        contestInfo = contestInfo.copy(status = ContestStatus.RUNNING(startedAt = Clock.System.now()))
        flowCollector.emit(InfoUpdate(contestInfo))
    }

    suspend fun freeze() {
        require(contestInfo.status is ContestStatus.RUNNING)
        val newStatus = (contestInfo.status as ContestStatus.RUNNING).copy(frozenAt = Clock.System.now())
        contestInfo = contestInfo.copy(status = newStatus)
        flowCollector.emit(InfoUpdate(contestInfo))
    }

    suspend fun finish() {
        require(contestInfo.status is ContestStatus.RUNNING)
        val oldStatus = contestInfo.status as ContestStatus.RUNNING
        val newStatus = ContestStatus.OVER(
            startedAt=oldStatus.startedAt, frozenAt = oldStatus.frozenAt, finishedAt = Clock.System.now()
        )
        contestInfo = contestInfo.copy(status = newStatus)
        flowCollector.emit(InfoUpdate(contestInfo))
    }

    fun team(id: Int): TeamId {
        return id.toTeamId()
    }

    fun problem(id: Int): ProblemId {
        return ('A' + id - 1).toString().toProblemId()
    }

    suspend fun submit(teamId: TeamId, problemId: ProblemId, contestTime: Duration, languageId: LanguageId = "cpp".toLanguageId()): RunId {
        val runId = (runs.size + 1).toRunId()
        runs[runId] = RunInfo(id=runId, result=RunResult.InProgress(0.0), problemId = problemId, teamId = teamId, time=contestTime, languageId = languageId)
        flowCollector.emit(RunUpdate(runs[runId]!!))
        return runId
    }

    suspend fun judge(runId: RunId, verdict: Verdict, contestTime: Duration) {
        runs[runId] = runs[runId]!!.copy(result=verdict.toICPCRunResult(), testedTime = contestTime)
        flowCollector.emit(RunUpdate(runs[runId]!!))
    }
}

fun emulateContest(numTeams: Int, numProblems: Int, block: suspend ContestEmulation.() -> Unit): Flow<ContestUpdate> = flow {
    val simulation = ContestEmulation(this, numTeams, numProblems)
    simulation.sendInitialUpdate()
    block(simulation)
}