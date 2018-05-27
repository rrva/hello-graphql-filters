package se.rrva.javascript

import jdk.nashorn.api.scripting.ClassFilter
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.ScriptContext

class SafeJavascriptEngine {

    private val safeClassFilter = NoClassesAllowed()
    private val javascriptValidator = JavascriptValidator()

    class NoClassesAllowed : ClassFilter {
        override fun exposeToScripts(s: String?) = false
    }

    private val jsEngine = NashornScriptEngineFactory().getScriptEngine(NoClassesAllowed())

    init {
        jsEngine.eval(JsFunctionArraySome())
    }

    fun eval(s: String): Any {
        javascriptValidator.containsSafeJavascript(s)
        return jsEngine.eval(s)
    }

    fun defineVariable(name: String, value: Any?) {
        jsEngine.getBindings(ScriptContext.ENGINE_SCOPE)[name] = value
    }

}