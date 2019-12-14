#!/usr/bin/env kscript
@file:Include("fetchInput.kt")

/**
 * Advent of Code 2019 - Day 14
 * https://adventofcode.com/2019/day/14
 */

typealias Chemical = String

typealias Quantity = Long

data class Ingredient(val chemical: Chemical, val quantity: Quantity)
data class Reaction(val input: List<Ingredient>, val output: Ingredient)
typealias ReactionMap = Map<Chemical, Reaction>

fun String.toReactionMap(): ReactionMap = trim().lines().map(::parseReaction)
    .fold(mutableMapOf()) { acc, reaction ->
        acc.apply { set(reaction.output.chemical, reaction) }
    }

fun Map<Chemical, Quantity>.nonOre() = filterKeys { it != "ORE" }.filterValues { it > 0 }

fun MutableMap<Chemical, Quantity>.add(chemical: Chemical, quantity: Quantity) =
    compute(chemical) { _, q -> quantity + (q ?: 0) }

// Divide rounding up
fun divCeil(numerator: Long, divisor: Long) = (numerator + divisor - 1) / divisor

fun ReactionMap.oreRequirements(nFuel: Long): Long {
    val bag = mutableMapOf("FUEL" to nFuel)

    while (bag.nonOre().isNotEmpty()) {
        val (chemical, quantity) = bag.nonOre().entries.first()
        val reaction = getValue(chemical)
        val multiplier = divCeil(quantity, reaction.output.quantity)
        for (ingredient in reaction.input) {
            bag.add(ingredient.chemical, ingredient.quantity * multiplier)
        }
        bag.add(chemical, -reaction.output.quantity * multiplier)
    }

    return bag.getValue("ORE")
}

fun parseReaction(s: String): Reaction {
    val (input, output) = s.split(" => ")
    return Reaction(
        input.split(", ").map(::parseIngredient),
        parseIngredient(output)
    )
}

fun parseIngredient(s: String): Ingredient {
    val (quantity, chemical) = s.split(" ")
    return Ingredient(chemical, quantity.toLong())
}

val testReactions = """10 ORE => 10 A
1 ORE => 1 B
7 A, 1 B => 1 C
7 A, 1 C => 1 D
7 A, 1 D => 1 E
7 A, 1 E => 1 FUEL""".toReactionMap()

check(testReactions.oreRequirements(1) == 31L)

val testReactions2 = """9 ORE => 2 A
8 ORE => 3 B
7 ORE => 5 C
3 A, 4 B => 1 AB
5 B, 7 C => 1 BC
4 C, 1 A => 1 CA
2 AB, 3 BC, 4 CA => 1 FUEL""".toReactionMap()

check(testReactions2.oreRequirements(1) == 165L)

val testReactions3 = """157 ORE => 5 NZVS
165 ORE => 6 DCFZ
44 XJWVT, 5 KHKGT, 1 QDVJ, 29 NZVS, 9 GPVTF, 48 HKGWZ => 1 FUEL
12 HKGWZ, 1 GPVTF, 8 PSHF => 9 QDVJ
179 ORE => 7 PSHF
177 ORE => 5 HKGWZ
7 DCFZ, 7 PSHF => 2 XJWVT
165 ORE => 2 GPVTF
3 DCFZ, 7 NZVS, 5 HKGWZ, 10 PSHF => 8 KHKGT
""".toReactionMap()
check(testReactions3.oreRequirements(1) == 13312L)

val reactions = getInput(14).read().toReactionMap()
println("part 1: ${reactions.oreRequirements(1)}")

/* Part 2 */

val maxOre = 1000000000000L

fun maxFuel(reactions: ReactionMap): Long {
    var ore = 0L
    var fuel = maxOre / reactions.oreRequirements(1)
    var increment = 1000
    while (true) {
//        println(fuel)
        ore = reactions.oreRequirements(fuel)
        if (ore > maxOre) {
            if (increment == 1) return fuel - 1
            fuel -= increment
            increment -= increment / 2
        } else {
            fuel += increment
        }
    }
}

check(maxFuel(testReactions3) == 82892753L)
println("part 2: ${maxFuel(reactions)}")
