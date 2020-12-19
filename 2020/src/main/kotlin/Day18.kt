import Day18.Element.*
import Day18.Operator.*
import java.math.BigInteger

object Day18: AocPuzzle<List<Expr>>(18) {
    override fun parseInput(input: String) = input.lines().map(::parseExpr)

    sealed class Element {
        data class Num(val value: BigInteger): Element() {
            override fun toString() = value.toString()
        }
        data class Op(val operator: Operator): Element() {
            override fun toString() = operator.str
        }
        data class Expr(val symbols: List<Element> = emptyList()): Element() {
            override fun toString() = "(${symbols.joinToString(" ")})"
            infix operator fun plus(elem: Element) = Expr(symbols + elem)
        }
    }

    fun parseExpr(exprString: String): Expr {
        val expr = mutableListOf(Expr())
        var s = exprString
        while (s.isNotBlank()) {
            val c = s.first()
            when (c) {
                '+' -> expr[expr.lastIndex] = expr.last() + Op(PLUS)
                '*' -> expr[expr.lastIndex] = expr.last() + Op(TIMES)
                '(' -> expr.add(Expr())
                ')' -> {
                    val penUltimate = expr.size - 2
                    expr[penUltimate] = expr[penUltimate] + expr.last()
                    expr.removeLast()
                }
                ' ' -> {}
                else -> expr[expr.lastIndex] = expr.last() + Num(c.toString().toBigInteger())
            }
            s = s.rest()
        }
        return expr.first()
    }

    enum class Operator(val str: String) { PLUS("+"), TIMES("*") }

    tailrec fun evalExpr(expr: List<Element>, x: BigInteger = 0.toBigInteger()): BigInteger {
        if (expr.isEmpty()) return x
        val op = expr.first()
        if (op !is Op) {
            return evalExpr(expr.drop(1), evalExpr(op))
        }
        val v = expr[1]
        val newX = when (op.operator) {
            PLUS -> x + evalExpr(v)
            TIMES -> x * evalExpr(v)
        }
        return evalExpr(expr.drop(2), newX)
    }

    fun evalExpr(elem: Element) = when (elem) {
        is Expr -> evalExpr(elem.symbols)
        is Num -> elem.value
        else -> error("Invalid element $elem")
    }

    override fun part1(input: List<Expr>): BigInteger {
        log(input)
        return input.map(::evalExpr).sum()
    }

    fun evalExprPlus(expr: List<Element>, head: List<Element>): List<Element> {
        if (expr.isEmpty()) return head
        val op = expr.first()
        if (op !is Op) {
            return evalExprPlus(expr.drop(1), listOf(evalExprPlus(op)))
        }
        val v = expr[1]
        val newHead: List<Element> = when (op.operator) {
            PLUS -> head.dropLast(1) + Expr(listOf(head.last(), op, evalExprPlus(v)))
            TIMES -> head + op + evalExprPlus(v)
        }
        return evalExprPlus(expr.drop(2), newHead)
    }

    fun evalExprPlus(elem: Element): Element = when (elem) {
        is Expr -> Expr(evalExprPlus(elem.symbols, emptyList()))
        is Num -> elem
        else -> error("Invalid element $elem")
    }

    override fun part2(input: List<Expr>): Any {
        val step1 = input.map(::evalExprPlus)
        log("$input => $step1")
        return step1.map(::evalExpr).sum()
    }

    val exampleData = "1 + 2 * 3 + 4 * 5 + 6"

    override val part1Tests = listOf(
        TestSet("2 + (3) * 4", 20),
        TestSet(exampleData, 71),
        TestSet("1 + (2 * 3) + (4 * (5 + 6))", 51),
        TestSet("2 * 3 + (4 * 5)", 26),
        TestSet("5 + (8 * 3 + 9 + 3 * 4 * 3)", 437),
        TestSet("5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))", 12240),
        TestSet("((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2", 13632),
    )
    override val part2Tests = listOf(
        TestSet(exampleData, 231),
        TestSet("1 + (2 * 3) + (4 * (5 + 6))", 51),
        TestSet("2 * 3 + (4 * 5)", 46),
        TestSet("5 + (8 * 3 + 9 + 3 * 4 * 3)", 1445),
        TestSet("5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))", 669060),
        TestSet("((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2", 23340),
    )
}

fun main() = Day18.testAndRun()