package com.kuss.krude.interfaces

data class BingResponse(
    val AS: BingASData
)

data class BingASData(
    val Query: String,
    val FullResults: Int,
    val Results: List<BingResult>
)

data class BingResult(
    val Type: String,
    val Suggests: List<BingSuggestion>
)

data class BingSuggestion(
    val Txt: String,
    val Type: String,
    val Sk: String,
    val HCS: Int
)

data class GptApp(
    val name: String,
    val `package`: String,
    val scheme: String
)

data class GptData(
    val search: String,
    val apps: List<GptApp>
)
