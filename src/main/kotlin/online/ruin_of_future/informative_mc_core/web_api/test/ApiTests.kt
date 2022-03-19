package online.ruin_of_future.informative_mc_core.web_api.test

import org.apache.logging.log4j.LogManager

class ApiTestBatch(
    private val tests: List<ApiTest>,
    private val isOrdered: Boolean
) {
    private val LOGGER = LogManager.getLogger("IMC API Test")

    var passedTestNum: Int = 0
    var failedTestNum: Int = 0

    fun runAll() {
        // TODO: Possible parallel run.
        tests.forEach {
            LOGGER.info("Testing ${it.apiId.toURIString()}")
            try {
                if (it.run()) {
                    LOGGER.info("Passed ${it.apiId.toURIString()}")
                    passedTestNum++
                } else {
                    LOGGER.error("Failed at ${it.apiId.toURIString()}")
                    failedTestNum++
                }
            } catch (e: Exception) {
                LOGGER.error(e)
                LOGGER.error("Failed at ${it.apiId.toURIString()}")
                failedTestNum++
            }
        }
    }
}

object ApiTests {
    private val LOGGER = LogManager.getLogger("IMC API Test")
    private val tests = listOf(
        ApiTestBatch(listOf(HeartbeatTest()), true)
    )

    var passedTestNum: Int = 0
    var failedTestNum: Int = 0
    val testNum: Int
        get() = passedTestNum + failedTestNum

    fun runAll() {
        LOGGER.warn("Starting all tests...")
        tests.forEach {
            it.runAll()
            passedTestNum += it.passedTestNum
            failedTestNum += it.failedTestNum
        }
        val ratio = passedTestNum.toDouble() * 100.0 / testNum
        LOGGER.warn("Finished $testNum test(s). $passedTestNum passed, $failedTestNum failed. (${"%.2f".format(ratio)}%)")
    }
}