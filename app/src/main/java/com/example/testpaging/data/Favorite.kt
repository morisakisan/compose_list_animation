package com.example.testpaging.data

data class Favorite(
    val id: Int,
    val name: String
) {
    companion object {
        val lists: List<Favorite> = listOf(
            Favorite(1, "test1"),
            Favorite(2, "test2"),
            Favorite(3, "test3"),
            Favorite(4, "test4"),
            Favorite(5, "test5"),
            Favorite(6, "test6"),
            Favorite(7, "test7"),
            Favorite(8, "test8")
        )
    }
}
