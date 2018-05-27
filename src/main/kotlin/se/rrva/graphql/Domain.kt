package se.rrva.graphql

data class TvSeries(
    override val id: String,
    override val name: String,
    val genres: List<Genre>,
    var seasons: List<Season>
) : Thing

data class Movie(
    override val id: String,
    override val name: String,
    val genres: List<Genre>
) : Thing

data class Season(
    val id: String,
    val name: String,
    val number: Int,
    val episodes: List<Episode>
)

data class Episode(
    override val id: String,
    override val name: String,
    val genres: List<Genre>,
    val scary : Boolean,
    val boring : Boolean
) : Thing

data class PromotedItem(
    val id: String,
    val slug: String?,
    val name: String?,
    val byline: String?,
    val description: String?
)

data class MyContent(
    val all: List<Any>
)

interface Thing {
    val id: String
    val name: String
}

data class Genre(
    val id: String,
    val name: String
)