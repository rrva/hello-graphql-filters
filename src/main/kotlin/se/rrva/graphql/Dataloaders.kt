package se.rrva.graphql

import org.dataloader.BatchLoader
import org.dataloader.DataLoader
import se.rrva.ContentRepository
import java.util.concurrent.CompletableFuture

internal fun recommendedDataLoader(contentRepository: ContentRepository): DataLoader<Genre, List<PromotedItem>> {
    val recommendedBatchLoader: BatchLoader<Genre, List<PromotedItem>> =
        BatchLoader { genres -> CompletableFuture.supplyAsync { contentRepository.promotedByGenres(genres) } }
    return DataLoader(recommendedBatchLoader)
}
