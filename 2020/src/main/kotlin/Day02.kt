object Day02 : AocPuzzle<List<String>>(2) {
    override fun parseInput(input: String): List<String> {
        return input.lines()
    }
    data class PasswordPolicy(val char: Char, val freqRange: IntRange)

    fun isValid(password: String, policy: PasswordPolicy): Boolean {
        val freq = password.filter { it == policy.char }.length
        return policy.freqRange.contains(freq)
    }

    fun validatePassword(line: String): Boolean {
        val pieces = line.split(" ")
        val password = pieces[2]
        val char = pieces[1][0]
        val rangeStr = pieces[0].split("-").map { it.toInt() }
        val range = (rangeStr[0]..rangeStr[1])
        return isValid(password, PasswordPolicy(char, range))
    }

    override fun part1(input: List<String>): Any {
        return input.sumBy { if (validatePassword(it)) 1 else 0 }
    }

    data class PasswordPolicyV2(val char: Char, val pos1: Int, val pos2: Int)

    fun validatePasswordV2(line: String): Boolean {
        val pieces = line.split(" ")
        val password = pieces[2]
        val char = pieces[1][0]
        val rangeStr = pieces[0].split("-").map { it.toInt() }
        val policy = PasswordPolicyV2(char, rangeStr[0], rangeStr[1])
        return isValidV2(password, policy)
    }

    fun isValidV2(password: String, policy: PasswordPolicyV2): Boolean {
        return (password.getOrNull(policy.pos1 - 1) == policy.char) xor (password.getOrNull(policy.pos2 - 1) == policy.char)
    }

    override fun part2(input: List<String>): Any {
        return input.sumBy { if (validatePasswordV2(it)) 1 else 0 }
    }

    val exampleInput = """1-3 a: abcde
1-3 b: cdefg
2-9 c: ccccccccc"""

    override val part1Tests = listOf(TestSet(exampleInput, 2))
    override val part2Tests = listOf(TestSet(exampleInput, 1))
}

fun main() = Day02.testAndRun()
