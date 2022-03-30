package online.ruin_of_future.informative_mc_core.database

sealed interface DatabaseTable<T, U> {
    val name: String
    fun insert(entry: T)
    fun select(predicate: U): List<T>
    fun delete(predicate: U)
}