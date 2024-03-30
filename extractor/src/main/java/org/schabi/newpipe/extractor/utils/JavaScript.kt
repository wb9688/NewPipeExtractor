package org.schabi.newpipe.extractor.utils

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function

object JavaScript {
    fun compileOrThrow(function: String?) {
        try {
            val context = Context.enter()
            context.setOptimizationLevel(-1)

            // If it doesn't compile it throws an exception here
            context.compileString(function, null, 1, null)
        } finally {
            Context.exit()
        }
    }

    fun run(function: String?,
            functionName: String?,
            vararg parameters: String?): String {
        return try {
            val context = Context.enter()
            context.setOptimizationLevel(-1)
            val scope = context.initSafeStandardObjects()
            context.evaluateString(scope, function, functionName, 1, null)
            val jsFunction = scope[functionName, scope] as Function
            val result = jsFunction.call(context, scope, scope, parameters)
            result.toString()
        } finally {
            Context.exit()
        }
    }
}
