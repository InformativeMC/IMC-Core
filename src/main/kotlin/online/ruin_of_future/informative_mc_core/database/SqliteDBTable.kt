package online.ruin_of_future.informative_mc_core.database

import java.nio.file.Path
import java.sql.DriverManager
import java.sql.ResultSet

abstract class SqliteDBTable<T : SqlTableRow>(
    override val name: String,
    private val sqlLiteDBPath: Path,
    private val createIfAbsent: Boolean,
) : DatabaseTable<T, String> {
    // TODO: Check sql input

    abstract val tableSchema: String

    /**
     * Row description without primary key.
     * Used in SQL insert operation.
     * For example, if the table schema is (id INTEGER PRIMARY KEY, name TEXT NOT NULL),
     * then row Schema should be ('name').
     * */
    abstract val rowSchema: String

    abstract fun resultSetParser(rs: ResultSet): List<T>

    private val connection = run {
        val c = DriverManager.getConnection("jdbc:sqlite:${sqlLiteDBPath.toAbsolutePath()}")
        val statement = c.createStatement()
        if (createIfAbsent) {
            val rs = statement
                .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='${this.name}'")
            if (!rs.next()) {
                statement.execute("CREATE TABLE ${this.name} ${this.tableSchema}")
            }
        }
        c
    }

    override fun insert(entry: T) {
        val statement = connection.createStatement()
        statement.executeUpdate("INSERT INTO ${this.name}${this.rowSchema} VALUES ${entry.toSqlString()}")
    }

    override fun select(predicate: String): List<T> {
        val statement = connection.createStatement()
        val rs = statement.executeQuery("SELECT * FROM ${this.name} WHERE $predicate")
        return resultSetParser(rs)
    }

    override fun delete(predicate: String) {
        val statement = connection.createStatement()
        statement.execute("DELETE FROM ${this.name} WHERE $predicate")
    }
}