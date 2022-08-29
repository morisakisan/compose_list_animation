package com.example.testpaging.data

data class Sample(
    val id: Int,
    val name: String
) {
    companion object {
        val lists: List<Sample> = listOf(
            Sample(1, "sample_1"),
            Sample(2, "sample_2"),
            Sample(3, "sample_3"),
            Sample(4, "sample_4"),
            Sample(5, "sample_5"),
            Sample(6, "sample_6"),
            Sample(7, "sample_7"),
            Sample(8, "sample_8"),
            Sample(9, "sample_9"),
            Sample(10, "sample_10")
        )
    }
}
