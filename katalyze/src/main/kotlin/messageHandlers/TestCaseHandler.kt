package messageHandlers

import legacyfeed.SimpleMessage
import model.TestCaseExecution
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class TestCaseHandler : SingleMessageHandler("testcase") {
    /*
	<testcase>
	 <i>3</i>
	 <judged>True</judged>
	 <judgement_id>1</judgement_id>
	 <n>35</n>
	 <result>AC</result>
	 <run-id>1</run-id>
	 <solved>True</solved>
	 <time>711.83203</time>
	 <timestamp>1337242312.06</timestamp>
	</testcase>
	*/
    override fun process(message: SimpleMessage) {
        val submissionId = message["run-id"]
        val caseNumber = message.getInt("i")
        val totalCaseCount = message.getInt("n")
        val result = message["result"]
        val time = message.getDouble("time")
        val solved = message.getBool("solved")

        val analyzer = contest!!.analyzer
        val submission = analyzer.submissionById(submissionId)
        if (submission == null) {
            logger.error(
                String.format(
                    "Unable to find judgement %n, which seems to be executed test cases on. Ignoring outcome!",
                    submissionId
                )
            )
        }
        val outcome = TestCaseExecution(submission, caseNumber, totalCaseCount, time, solved, result)

        analyzer.testCaseExecuted(outcome)
    }

    companion object {
        var logger: Logger = LogManager.getLogger(TestCaseHandler::class.java)
    }
}