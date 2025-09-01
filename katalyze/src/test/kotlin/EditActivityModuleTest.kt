package rules_kt

import DbTestBase
import emulateContest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import model.dsl.v1.*
import org.icpclive.cds.adapters.addFirstToSolves
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.Verdict
import org.icpclive.cds.scoreboard.calculateScoreboard
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

import katalyzeapp.*
import java.io.File

class EditActivityModuleTest : DbTestBase() {
    @Test
    fun testSimple() = testApplication {
        application {
            editActivityModule(db)
        }
        val sample_db_path = this.javaClass.getResource("/2014_dress_rehearsal_icat_db_backup.sql")!!.path
        val test_queries = File(sample_db_path).readLines()[212].replace("icpc2014_edit_activity", "edit_activity")
        transaction(db){
            //load data from icpc2014 dress rehearsal
            exec(test_queries)
        }
        
        val response = client.get("/editacticity/104")
        assertEquals(HttpStatusCode.OK, response.status)
        val decoded: Map<String, Int> = Json.decodeFromString(response.bodyAsText())
        assertEquals(decoded["Desktop/E.cpp"],69)
        println(decoded)
        
        val response2 = client.get("/editacticity/not_an_id")
        assertEquals(HttpStatusCode.BadRequest, response2.status)
        assertEquals("Invalid or missing id parameter", response2.bodyAsText())
    }
}