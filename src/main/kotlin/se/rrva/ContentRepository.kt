package se.rrva

import se.rrva.graphql.*
import se.rrva.javascript.SafeJavascriptEngine
import javax.inject.Singleton
import kotlin.reflect.full.declaredMemberProperties

@Singleton
class ContentRepository {

    val jsEngine = SafeJavascriptEngine()

    fun promotedByGenres(genres: List<Genre>): List<List<PromotedItem>>? {
        return listOf(listOf(PromotedItem("123", "foo-123", "Foo", "Lorem ipsum", "Lorem ipsum dolor sit amet")))
    }

    private fun jsFilter(it: Any, filter: String): Boolean {
        it::class::declaredMemberProperties.get().forEach { p ->
            jsEngine.defineVariable(p.name, p.getter.call(it))
        }
        return jsEngine.eval(filter) as Boolean
    }

    fun myContent(n: Int?, filter: String?): MyContent {


        val content = dummyContent(n)

        if(filter != null) {
            return MyContent(all = content.all.filter { jsFilter(it, filter) })
        }
        return content
    }

    private fun dummyContent(n: Int?): MyContent {
        return MyContent(all = (1..(n ?: 10)).map { i ->
            TvSeries(i.toString(), "Series $i", listOf(
                Genre(
                    "drama",
                    "Drama"
                )
            ), (1..2).map { seasonNo ->
                Season("s$seasonNo", "Season $seasonNo", seasonNo, (1..10).map { episodeNo ->
                    Episode(
                        "S${seasonNo}E$episodeNo", "Episode $episodeNo", listOf(
                            Genre(
                                "drama",
                                "Drama"
                            )
                        ), false, episodeNo % 2 == 0
                    )
                })
            })
        })
    }

}