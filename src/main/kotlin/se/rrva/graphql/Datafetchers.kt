package se.rrva.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import se.rrva.ContentRepository
import java.util.concurrent.CompletableFuture

class RecommendedDataFetcher(
    private val repository: ContentRepository,
    private val recommendedBatchLoader: DataLoader<Genre, List<PromotedItem>>
) : DataFetcher<CompletableFuture<List<PromotedItem>>> {

    override fun get(env: DataFetchingEnvironment): CompletableFuture<List<PromotedItem>> {
        return recommendedBatchLoader.load(env.getSource<Genre>())
    }
}

class MyContentDataFetcher(private val repository: ContentRepository) : DataFetcher<MyContent> {
    override fun get(env: DataFetchingEnvironment): MyContent {
        return repository.myContent(env.getArgument("n"), env.getArgument("filter"))
    }
}