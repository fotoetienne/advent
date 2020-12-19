object Day19 : AocPuzzle<Day19.SatelliteCommunication>(19) {
    data class SatelliteCommunication(val msgRules: MsgRules, val messages: List<String>)

    class MsgRules(ruleDefinitions: List<String>) {
        val rules: MutableMap<Int, MsgRule> = mutableMapOf()

        init {
            rules.putAll(
                ruleDefinitions.map { line ->
                    val (ruleNum, ruleVal) = line.split(": ")
                    ruleNum.toInt() to parseRule(ruleVal)
                }.toMap()
            )
        }

        fun parseRule(ruleVal: String): MsgRule {
            return when {
                ruleVal.startsWith("\"") -> MsgRule.CharMatch(ruleVal[1])
                ruleVal.contains('|') -> MsgRule.Or(ruleVal.split(" | ").map { parseRule(it) })
                else -> MsgRule.RuleSequence(ruleVal.split(' ').map { MsgRule.Ref(it.toInt(), rules) })
            }
        }

        operator fun invoke(message: String): Boolean {
            val matches = (rules[0]!!)(message)
            return matches.filter { it.isBlank() }.iterator().hasNext()
        }

        sealed class MsgRule {
            abstract operator fun invoke(message: String): Sequence<String>

            class CharMatch(val c: Char) : MsgRule() {
                override operator fun invoke(message: String): Sequence<String> {
                    if (message.isEmpty()) return emptySequence()
                    return if (message.first() == c) {
                        sequenceOf(message.rest())
                    } else emptySequence()
                }
            }

            class RuleSequence(val rules: List<MsgRule>) : MsgRule() {
                private fun _invoke(message: String, ruleIndex: Int): Sequence<String> {
                    if (ruleIndex > rules.lastIndex) return sequenceOf(message)
                    val matches = rules[ruleIndex](message)
                    return matches.flatMap { _invoke(it, ruleIndex + 1) }.asSequence()
                }

                override fun invoke(message: String): Sequence<String> {
                    return _invoke(message, 0)
                }
            }

            class Or(val rules: List<MsgRule>) : MsgRule() {
                override fun invoke(message: String): Sequence<String> {
                    val (rule1, rule2) = rules
                    return sequence {
                        yieldAll(rule1(message))
                        yieldAll(rule2(message))
                    }
                }
            }

            class Ref(val ruleNum: Int, val ruleSet: Map<Int, MsgRule>) : MsgRule() {
                override fun invoke(message: String): Sequence<String> {
                    return (ruleSet[ruleNum]!!)(message)
                }
            }
        }
    }


    override fun parseInput(input: String): SatelliteCommunication {
        val (ruleSet, messages) = input.split("\n\n")
        val rules = MsgRules(ruleSet.lines())
        return SatelliteCommunication(msgRules = rules, messages = messages.lines())
    }

    override fun part1(input: SatelliteCommunication): Int {
        return input.messages.filter { input.msgRules(it) }.size
    }

    override fun part2(input: SatelliteCommunication): Any {
        input.msgRules.rules[8] = input.msgRules.parseRule("42 | 42 8")
        input.msgRules.rules[11] = input.msgRules.parseRule("42 31 | 42 11 31")
        val matches = input.messages.filter { input.msgRules(it) }
        log(matches)
        return matches.size
    }

    val exampleData = """
        0: 4 1 5
        1: 2 3 | 3 2
        2: 4 4 | 5 5
        3: 4 5 | 5 4
        4: "a"
        5: "b"

        ababbb
        bababa
        abbbab
        aaabbb
        aaaabbb
    """.trimIndent()

    val bigExample = """42: 9 14 | 10 1
9: 14 27 | 1 26
10: 23 14 | 28 1
1: "a"
11: 42 31
5: 1 14 | 15 1
19: 14 1 | 14 14
12: 24 14 | 19 1
16: 15 1 | 14 14
31: 14 17 | 1 13
6: 14 14 | 1 14
2: 1 24 | 14 4
0: 8 11
13: 14 3 | 1 12
15: 1 | 14
17: 14 2 | 1 7
23: 25 1 | 22 14
28: 16 1
4: 1 1
20: 14 14 | 1 15
3: 5 14 | 16 1
27: 1 6 | 14 18
14: "b"
21: 14 1 | 1 14
25: 1 1 | 1 14
22: 14 14
8: 42
26: 14 22 | 1 20
18: 15 15
7: 14 5 | 1 21
24: 14 1

abbbbbabbbaaaababbaabbbbabababbbabbbbbbabaaaa
bbabbbbaabaabba
babbbbaabbbbbabbbbbbaabaaabaaa
aaabbbbbbaaaabaababaabababbabaaabbababababaaa
bbbbbbbaaaabbbbaaabbabaaa
bbbababbbbaaaaaaaabbababaaababaabab
ababaaaaaabaaab
ababaaaaabbbaba
baabbaaaabbaaaababbaababb
abbbbabbbbaaaababbbbbbaaaababb
aaaaabbaabaaaaababaa
aaaabbaaaabbaaa
aaaabbaabbaaaaaaabbbabbbaaabbaabaaa
babaaabbbaaabaababbaabababaaab
aabbbbbaabbbaaaaaabbbbbababaaaaabbaaabba"""

    override val part1Tests = listOf(
        TestSet(exampleData, 2),
        TestSet(bigExample, 3),
    )
    override val part2Tests = listOf(TestSet(bigExample, 12))
}

fun main() = Day19.testAndRun()