package se.rrva.javascript

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.rrva.App
import se.rrva.App.createObjectMapper


class JavascriptValidator {

    private val jsEngine = NashornScriptEngineFactory().scriptEngine
    private val log: Logger = LoggerFactory.getLogger(App.javaClass)


    private val objectMapper = createObjectMapper()

    private fun escapeQuotes(str: String): String {
        return str.replace("\"", "\\\"")
    }

    fun containsSafeJavascript(js: String) {
        if (js.length > 150) {
            throw IllegalArgumentException("Given javascript is too long! ${js.substring(0, 20)}...")
        }
        val jsAbstractSyntaxTree = parseJavascript(js)
        val whitelistedNodeTypes = setOf(
            "Program",
            "ExpressionStatement",
            "LogicalExpression",
            "BinaryExpression",
            "Identifier",
            "Literal",
            "CallExpression",
            "MemberExpression",
            "UnaryExpression"
        )
        validateAllowedStatements("root", jsAbstractSyntaxTree, whitelistedNodeTypes)
    }

    private fun parseJavascript(filter: String): JsonNode {
        return objectMapper.readTree(
            jsEngine.eval("load('nashorn:parser.js'); JSON.stringify(parse(\"${escapeQuotes(filter)}\"))")
                .toString()
        )
    }

    private fun validateAllowedStatements(nodeName: String, jsonNode: JsonNode, whitelistedNodeTypes: Set<String>) {
        when {
            jsonNode.isObject -> {
                val objectNode = jsonNode as ObjectNode
                objectNode.fields().forEach {
                    validateAllowedStatements(it.key, it.value, whitelistedNodeTypes)
                }
            }
            jsonNode.isArray -> {
                val arrayNode = jsonNode as ArrayNode
                arrayNode.forEach {
                    validateAllowedStatements(nodeName, it, whitelistedNodeTypes)
                }
            }
            jsonNode.isValueNode -> {
                val valueNode = jsonNode as ValueNode
                if (nodeName == "type") {
                    val type = valueNode.asText()
                    if (!whitelistedNodeTypes.contains(type)) {
                        throw IllegalArgumentException(
                            "javascript statement $type is not allowed. " +
                                    "Allowed types are $whitelistedNodeTypes"
                        )
                    }
                }
            }
        }
    }

}


fun main(args: Array<String>) {
    JavascriptValidator().containsSafeJavascript("name == \"kalle\"")
}