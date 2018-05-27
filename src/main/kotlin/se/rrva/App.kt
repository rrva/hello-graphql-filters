package se.rrva

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation
import graphql.execution.preparsed.PreparsedDocumentEntry
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import org.dataloader.DataLoaderRegistry
import org.jooby.AsyncMapper
import org.jooby.Status
import org.jooby.handlers.CorsHandler
import org.jooby.json.Jackson
import org.jooby.run
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.rrva.graphql.buildRuntimeWiring
import se.rrva.graphql.loadSchema
import se.rrva.graphql.recommendedDataLoader

object App {

    private val log: Logger = LoggerFactory.getLogger(App.javaClass)

    private val om = createObjectMapper()

    internal fun createObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerKotlinModule()
            registerModule(Jdk8Module())
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        run(*args) {
            use(Jackson(om))
            use("*", CorsHandler())
            map(AsyncMapper())

            var repository: ContentRepository?
            var graphQl: GraphQL? = null
            val preParsedQueryCache: Cache<String, PreparsedDocumentEntry> =
                Caffeine.newBuilder().maximumSize(10_000).build()

            onStart { _ ->
                repository = require(ContentRepository::class.java)
                graphQl = createGraphQl(repository!!, preParsedQueryCache)
            }

            assets("/**")

            get("/") {req, rsp->
                rsp.status(Status.MOVED_PERMANENTLY)
                rsp.header("Location", "/graphiql.html")
            }

            options("/graphql") { _, rsp, _ ->
                rsp.header("Access-Control-Allow-Origin", "*")
                rsp.header("Access-Control-Allow-Credentials", "true")
                rsp.header("Access-Control-Allow-Headers", "content-type, authorization")
                rsp.header("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, PATCH, POST, PUT")
                rsp.status(204)
                rsp.send("OK")

            }

            post("/graphql") { req ->
                val body = req.body(GraphRequest::class.java)
                val (query, variables) = body
                val executionInput = ExecutionInput.Builder()
                    .query(query)
                    .variables(variables)
                graphQl!!.executeAsync(executionInput).thenApply { it.toSpecification() }
            }
        }
    }

}

data class GraphRequest(
    val query: String,
    val variables: Map<String, Any>?
)

fun createGraphQl(
    repository: ContentRepository,
    preParsedQueryCache: Cache<String, PreparsedDocumentEntry>
): GraphQL {

    val schemaParser = SchemaParser()
    val schemaGenerator = SchemaGenerator()
    val schemaString = loadSchema("schema.graphqls")
    val typeRegistry = schemaParser.parse(schemaString)

    val recommendedBatchLoader = recommendedDataLoader(repository)
    val wiring = buildRuntimeWiring(repository, recommendedBatchLoader)

    val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring)

    val registry = DataLoaderRegistry()
    registry.register("recommended", recommendedBatchLoader)
    val dispatcherInstrumentation = DataLoaderDispatcherInstrumentation(registry)

    return GraphQL.newGraphQL(graphQLSchema).instrumentation(dispatcherInstrumentation)
        .preparsedDocumentProvider(preParsedQueryCache::get)
        .build()
}

