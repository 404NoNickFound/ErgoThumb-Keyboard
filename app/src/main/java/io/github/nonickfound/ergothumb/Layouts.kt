package com.aleix.ergothumb

data class KeyDef(
    val tap: String,
    val swipes: Map<SwipeDir, String> = emptyMap()
)

data class LayoutData(
    val left: List<KeyDef>,
    val right: List<KeyDef>
)

object Layouts {

    val CODES = listOf("ca", "es", "en")

    val NAMES = mapOf(
        "ca" to "Català",
        "es" to "Castellà",
        "en" to "Anglès"
    )
    val NATIVE = mapOf(
        "ca" to "Català",
        "es" to "Castellano",
        "en" to "English"
    )

    private val CA = LayoutData(
        left = listOf(
            KeyDef("l", mapOf(SwipeDir.S to "'", SwipeDir.SE to "l·l")),
            KeyDef("t"),
            KeyDef("d"),
            KeyDef("m"),
            KeyDef("s", mapOf(SwipeDir.N to "x", SwipeDir.E to "q", SwipeDir.W to "z")),
            KeyDef("c", mapOf(SwipeDir.S to "ç")),
            KeyDef("f"),
            KeyDef("n", mapOf(SwipeDir.N to "y")),
            KeyDef("r")
        ),
        right = listOf(
            KeyDef("i", mapOf(SwipeDir.SE to "í", SwipeDir.S to "ï")),
            KeyDef("o", mapOf(SwipeDir.SW to "ò", SwipeDir.SE to "ó")),
            KeyDef("p"),
            KeyDef("a", mapOf(SwipeDir.S to "à")),
            KeyDef("e", mapOf(
                SwipeDir.SW to "è", SwipeDir.SE to "é",
                SwipeDir.W to ".", SwipeDir.E to ",",
                SwipeDir.N to "?", SwipeDir.NW to "!",
                SwipeDir.NE to "-", SwipeDir.NE to "-"
            )),
            KeyDef("v", mapOf(SwipeDir.N to "w")),
            KeyDef("g", mapOf(SwipeDir.N to "k", SwipeDir.E to "h", SwipeDir.NE to "j")),
            KeyDef("u", mapOf(SwipeDir.NE to "ú", SwipeDir.E to "ü")),
            KeyDef("b")
        )
    )

    private val ES = LayoutData(
        left = listOf(
            KeyDef("l", mapOf(SwipeDir.S to "'", SwipeDir.SE to "l·l")),
            KeyDef("t"),
            KeyDef("d"),
            KeyDef("m"),
            KeyDef("s", mapOf(SwipeDir.N to "x", SwipeDir.E to "q", SwipeDir.W to "z")),
            KeyDef("c"),
            KeyDef("f"),
            KeyDef("n", mapOf(SwipeDir.N to "ñ")),
            KeyDef("r")
        ),
        right = listOf(
            KeyDef("i", mapOf(SwipeDir.SE to "í", SwipeDir.S to "y")),
            KeyDef("o", mapOf(SwipeDir.SE to "ó", SwipeDir.N to "?", SwipeDir.NW to "!")),
            KeyDef("p"),
            KeyDef("a", mapOf(SwipeDir.SE to "á")),
            KeyDef("e", mapOf(
                SwipeDir.SE to "é", SwipeDir.NE to "-",
                SwipeDir.W to ".", SwipeDir.E to ",",
                SwipeDir.N to "¿", SwipeDir.NW to "¡"
            )),
            KeyDef("v", mapOf(SwipeDir.N to "w")),
            KeyDef("g", mapOf(SwipeDir.N to "k", SwipeDir.E to "h", SwipeDir.NE to "j")),
            KeyDef("u", mapOf(SwipeDir.NE to "ú", SwipeDir.E to "ü")),
            KeyDef("b")
        )
    )

    private val EN = LayoutData(
        left = listOf(
            KeyDef("l", mapOf(SwipeDir.S to "'")),
            KeyDef("t"),
            KeyDef("d"),
            KeyDef("m"),
            KeyDef("s", mapOf(SwipeDir.N to "x", SwipeDir.E to "q", SwipeDir.W to "z")),
            KeyDef("c"),
            KeyDef("f"),
            KeyDef("n"),
            KeyDef("r")
        ),
        right = listOf(
            KeyDef("i", mapOf(SwipeDir.S to "y")),
            KeyDef("o"),
            KeyDef("p"),
            KeyDef("a"),
            KeyDef("e", mapOf(
                SwipeDir.W to ".", SwipeDir.E to ",",
                SwipeDir.N to "?", SwipeDir.NW to "!",
                SwipeDir.NE to "-"
            )),
            KeyDef("v", mapOf(SwipeDir.N to "w")),
            KeyDef("g", mapOf(SwipeDir.N to "k", SwipeDir.E to "h", SwipeDir.NE to "j")),
            KeyDef("u"),
            KeyDef("b")
        )
    )

    private val TABLES = mapOf("ca" to CA, "es" to ES, "en" to EN)

    fun forLang(code: String): LayoutData = TABLES[code] ?: CA
}