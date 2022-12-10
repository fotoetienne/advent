use anyhow::{Context, Result};
use itertools::Itertools;
use std::collections::HashSet;

use crate::puzzle::{Puzzle, PuzzleFn::I32};

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 3,
    part1: I32(part1),
    part2: I32(part2),
};

fn part1(input: &str) -> i32 {
    input
        .lines()
        .map(|line| {
            let mistake = find_mistake(line)?;
            priority(mistake)
        })
        .map(Result::unwrap)
        .sum()
}

fn find_mistake(line: &str) -> Result<char> {
    let midpoint = line.len() / 2;
    let left: HashSet<char> = into_set(&line[..midpoint]);
    let right = line[midpoint..].chars();
    for c in right {
        if left.contains(&c) {
            return Ok(c);
        }
    }
    Err(anyhow::anyhow!("No mistakes found"))
}

fn into_set(s: &str) -> HashSet<char> {
    HashSet::from_iter(s.chars())
}

fn part2(input: &str) -> i32 {
    input
        .lines()
        .chunks(3)
        .into_iter()
        .map(|mut chunk| {
            let first = into_set(chunk.next().unwrap());
            let second = into_set(chunk.next().unwrap());
            let mut third = chunk.next().unwrap().chars();
            let badge = third
                .find(|c| first.contains(c) && second.contains(c))
                .context("No badge found")?;
            priority(badge)
        })
        .map(Result::unwrap)
        .sum()
}

fn priority(c: char) -> Result<i32> {
    let priorities = ('a'..='z').chain('A'..='Z').collect::<Vec<_>>();
    let priority = priorities.iter().position(|&p| p == c);
    Ok(priority.context(format!("Unable to parse {}", c))? as i32 + 1)
}

#[cfg(test)]
mod test {
    use crate::day03::{part1, part2, priority};

    #[test]
    fn priority_test() {
        assert_eq!(priority('a').unwrap(), 1);
        assert_eq!(priority('z').unwrap(), 26);
        assert_eq!(priority('A').unwrap(), 27);
        assert_eq!(priority('Z').unwrap(), 52);
    }

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 157)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 70)
    }

    const SAMPLE_INPUT: &str = "vJrwpWtwJgWrhcsFMMfFFhFp
jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
PmmdzqPrVvPwwTWBwg
wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
ttgJtRGJQctTZtZT
CrZsJsPPZsGzwwsLwLmpwMDw";
}
