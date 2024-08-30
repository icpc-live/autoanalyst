import model.dsl.v1.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedConnection
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class DbTestBase {
    protected lateinit var db: Database
        private set
    private lateinit var connection: ExposedConnection<*>
    private val tables = arrayOf(Contests, Entries, Problems, Submissions, TeamRegions, Teams)

    @BeforeTest
    fun setUpDb() {
        db = Database.connect("jdbc:h2:mem:test;MODE=MYSQL", driver = "org.h2.Driver")
        connection = db.connector()
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(*tables)
        }
    }

    @AfterTest
    fun tearDownDb() {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(*tables)
        }
        connection.close()
    }
}