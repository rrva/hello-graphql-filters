package se.rrva.graphql

import graphql.TypeResolutionEnvironment
import graphql.schema.GraphQLObjectType
import graphql.schema.TypeResolver
import graphql.schema.idl.RuntimeWiring
import org.dataloader.DataLoader
import se.rrva.ContentRepository
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

internal fun loadSchema(filename: String): String {
    return BufferedReader(
        InputStreamReader(
            object : Any() {}.javaClass.classLoader.getResourceAsStream(filename)
                    ?: throw FileNotFoundException("classpath:$filename")
        )
    ).readText()
}

fun buildRuntimeWiring(
    repository: ContentRepository,
    recommendedBatchLoader: DataLoader<Genre, List<PromotedItem>>
): RuntimeWiring {
    return RuntimeWiring.newRuntimeWiring()
        .type("Query", { wiring ->
            wiring.dataFetcher("myContent", MyContentDataFetcher(repository))
        })
        .type("Genre", { wiring ->
            wiring.dataFetcher("recommended", RecommendedDataFetcher(repository, recommendedBatchLoader))

        })
        .type("Content", { wiring ->
            wiring.typeResolver(ContentTypeResolver())
        })
        .type("PromotedContent", { wiring ->
            wiring.typeResolver(PromotedContentTypeResolver())
        })
        .build()
}

class ContentTypeResolver : TypeResolver {
    override fun getType(env: TypeResolutionEnvironment?): GraphQLObjectType {
        val javaObject: Any? = env?.getObject()
        return when (javaObject) {
            is TvSeries -> env.schema.getType("TvSeries") as GraphQLObjectType
            is Movie -> env.schema.getType("Movie") as GraphQLObjectType
            else -> throw IllegalStateException("Unknown type")
        }
    }
}

class PromotedContentTypeResolver : TypeResolver {
    override fun getType(env: TypeResolutionEnvironment?): GraphQLObjectType {
        val javaObject: Any? = env?.getObject()
        return when (javaObject) {
            is Episode -> env.schema.getType("Episode") as GraphQLObjectType
            is Movie -> env.schema.getType("Movie") as GraphQLObjectType

            else -> throw IllegalStateException("Unknown type: $javaObject")

        }
    }
}
