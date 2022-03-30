package online.ruin_of_future.informative_mc_core.database

interface SqlTableRow {
    fun toSqlString(): String
}