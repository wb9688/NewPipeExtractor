package org.schabi.newpipe.downloader

import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * @see MockOnly
 */
class MockOnlyCondition : ExecutionCondition {
    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        return if (DownloaderFactory.getDownloaderType() == DownloaderType.REAL) {
            ConditionEvaluationResult.disabled(MOCK_ONLY_REASON)
        } else {
            ConditionEvaluationResult.enabled(MOCK_ONLY_REASON)
        }
    }

    companion object {
        private const val MOCK_ONLY_REASON = "Mock only"
    }
}
