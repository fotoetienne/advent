use crate::puzzle::{Puzzle, PuzzleFn::I32};
use std::str::FromStr;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 4,
    part1: I32(part1),
    part2: I32(part2),
};

fn part1(input: &str) -> i32 {
    input
        .lines()
        .map(|line| parse_elves(line))
        .filter(|(a, b)| contains(a, b) || contains(b, a))
        .count() as i32
}

fn parse_elves(line: &str) -> ((i32, i32), (i32, i32)) {
    let mut elves = line.split(',').map(|s| split_i32('-', s));
    (elves.next().unwrap(), elves.next().unwrap())
}

fn split_i32(delimiter: char, s: &str) -> (i32, i32) {
    let mut digits = s.split(delimiter).map(|x| i32::from_str(x).unwrap());
    (digits.next().unwrap(), digits.next().unwrap())
}

// Returns true if range b is completely contains by range a
fn contains(a: &(i32, i32), b: &(i32, i32)) -> bool {
    a.0 <= b.0 && a.1 >= b.1
}

fn part2(input: &str) -> i32 {
    input
        .lines()
        .map(|line| parse_elves(line))
        .filter(|(a, b)| overlaps(a, b))
        .count() as i32
}

// Returns true if ranges overlap at all
fn overlaps(a: &(i32, i32), b: &(i32, i32)) -> bool {
    a.1 >= b.0 && a.0 <= b.1
}

#[cfg(test)]
mod test {
    use crate::day04::{part1, part2};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 2)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 4)
    }

    const SAMPLE_INPUT: &str = "2-4,6-8
2-3,4-5
5-7,7-9
2-8,3-7
6-6,4-6
2-6,4-8";
}
