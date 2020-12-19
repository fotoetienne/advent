typealias LuggageRuleSet = Map<String, Map<String, Int>>

object Day07 : AocPuzzle<LuggageRuleSet>(7) {
    override fun parseInput(input: String) = input.lines().let(::parseRules)

    fun parseRule(rule: String): Pair<String, Map<String, Int>> {
        val (bagColor, contains) = rule.trim('.').split(" bags contain ")
        val contents: Map<String, Int> = if (contains == "no other bags") {
            emptyMap()
        } else {
            val subBags = contains.split(',')
            subBags.map {
                val matchGroups = Regex("""(\d+) ([a-z ]+) bags?""").matchEntire(it.trim())?.groupValues
                val quantity = matchGroups?.get(1)?.toInt()
                val color = matchGroups?.get(2)
                if (quantity == null || color == null) {
                    error("No match for $it")
                }
                color to quantity
            }.toMap()
        }
        return bagColor to contents
    }

    fun parseRules(rules: List<String>) = rules.map(this::parseRule).toMap()

    fun LuggageRuleSet.canContainColor(
        fromColor: String,
        toColor: String,
        cache: MutableMap<String, Boolean>,
    ): Boolean {
        return fromColor == toColor || cache.getOrPut(fromColor) {
            this[fromColor]?.keys?.any { canContainColor(it, toColor, cache) } ?: error("Color not found")
        }
    }

    fun LuggageRuleSet.totalBagsContained(color: String): Int = get(color).let { children ->
        if (children.isNullOrEmpty()) 0
        else children.entries.sumBy { (child, n) -> n + n * totalBagsContained(child) }
    }

    const val shinyGold = "shiny gold"

    override fun part1(input: LuggageRuleSet): Int {
        val colors = input.keys - shinyGold
        log(input)
        val cache = mutableMapOf<String, Boolean>()
        return colors.filter { input.canContainColor(it, shinyGold, cache) }.size
    }

    override fun part2(input: LuggageRuleSet): Int {
        return input.totalBagsContained(shinyGold)
    }

    val exampleData = """
        light red bags contain 1 bright white bag, 2 muted yellow bags.
        dark orange bags contain 3 bright white bags, 4 muted yellow bags.
        bright white bags contain 1 shiny gold bag.
        muted yellow bags contain 2 shiny gold bags, 9 faded blue bags.
        shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
        dark olive bags contain 3 faded blue bags, 4 dotted black bags.
        vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
        faded blue bags contain no other bags.
        dotted black bags contain no other bags.
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 4))

    val anotherExample = """shiny gold bags contain 2 dark red bags.
dark red bags contain 2 dark orange bags.
dark orange bags contain 2 dark yellow bags.
dark yellow bags contain 2 dark green bags.
dark green bags contain 2 dark blue bags.
dark blue bags contain 2 dark violet bags.
dark violet bags contain no other bags."""

    val reallySimpleExample = """
       shiny gold bags contain 2 dark red bags.
       dark red bags contain no other bags.
""".trimIndent()

    val anotherSimpleExample = """
        shiny gold bags contain 5 faded blue bags, 6 dotted black bags.
        faded blue bags contain no other bags.
        dotted black bags contain no other bags.
    """.trimIndent()

    val lessSimpleExample = """
        shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
        vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
        faded blue bags contain no other bags.
        dotted black bags contain no other bags.
    """.trimIndent()

    override val part2Tests = listOf(
        TestSet(exampleData, 32),
        TestSet(anotherExample, 126),
        TestSet(reallySimpleExample, 2),
        TestSet(anotherSimpleExample, 11),
    )
}


fun main() = Day07.testAndRun()