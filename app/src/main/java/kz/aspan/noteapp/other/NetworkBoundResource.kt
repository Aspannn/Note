package kz.aspan.noteapp.other

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    emit(Resource.Loading(null))
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading(data))

        try {
            val fetchedResult = fetch()
            saveFetchResult(fetchedResult)
            query().map { Resource.Success(it) }
        } catch (t: Throwable) {
            onFetchFailed(t)
            query().map {
                Resource.Error("Couldn't reach server. It might be down", it)
            }
        }
    } else {
        query().map { Resource.Success(it) }
    }
    emitAll(flow)
}