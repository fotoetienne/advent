object Day16 : AocPuzzle<Day16.Tickets>(16) {
    data class Tickets(
        val rules: List<Rule>,
        val yourTicket: List<Int>,
        val nearbyTickets: List<List<Int>>,
    )

    data class Rule(val fieldName: String, val range1: IntRange, val range2: IntRange)

    fun parseRange(rangeString: String): IntRange {
        val (lower, upper) = rangeString.split("-")
        return (lower.toInt()..upper.toInt())
    }

    fun parseRule(ruleString: String): Rule {
        val (fieldName, ranges) = ruleString.split(": ")
        val (range1, range2) = ranges.split(" or ")
        return Rule(fieldName, parseRange(range1), parseRange(range2))
    }

    override fun parseInput(input: String): Tickets {
        val (rules, yours, nearby) = input.split("\n\n")
        return Tickets(
            rules.lines().map(::parseRule),
            yours.lines()[1].toInts(),
            nearby.lines().drop(1).map(String::toInts)
        )
    }

    fun Rule.validate(n: Int) =
        range1.contains(n) || range2.contains(n)

    fun Collection<Rule>.validate(n: Int) = filter { it.validate(n) }

    override fun part1(input: Tickets): Int {
        log(input)
        val invalidNumbers = input.nearbyTickets.flatMap { ticket ->
            ticket.filter {
                input.rules.validate(it).isEmpty()
            }
        }
        return invalidNumbers.sum()
    }

    override fun part2(input: Tickets): Any {
        val validTickets = input.nearbyTickets.filter { ticket ->
            ticket.all { input.rules.validate(it).isNotEmpty() }
        }

        var possibleNamesPerField = input.yourTicket.indices.map { i ->
            validTickets
                .map { ticket ->
                    input.rules.validate(ticket[i])
                        .map { it.fieldName }
                        .toSet()
                }
                .reduce { a, b -> a.intersect(b) }
        }
        val fieldNames: MutableList<String?> = input.yourTicket.map { null }.toMutableList()
        while (fieldNames.any { it == null }) {
            log(possibleNamesPerField.map { it.size })
            log(fieldNames)
            val index = possibleNamesPerField.indexOfFirst { it.size == 1 }
            if (index == -1) error("No more singleton rulesets :(")
            val fieldName = possibleNamesPerField[index].first()
            fieldNames[index] = fieldName
            possibleNamesPerField = possibleNamesPerField.map { rules ->
                rules.filter { !fieldNames.contains(it) }.toSet()
            }
        }
        val departures = fieldNames.mapIndexedNotNull { index, fieldName ->
            if (fieldName!!.contains("departure")) input.yourTicket[index] else null
        }
        return departures.product()
    }

    val exampleData = """
        class: 1-3 or 5-7
        row: 6-11 or 33-44
        seat: 13-40 or 45-50

        your ticket:
        7,1,14

        nearby tickets:
        7,3,47
        40,4,50
        55,2,20
        38,6,12
    """.trimIndent()

    override val part1Tests = listOf(TestSet(exampleData, 71))
//    override val part2Tests = listOf(TestSet(exampleData, 0))
}

fun main() = Day16.testAndRun()