use std::collections::HashSet;

use crate::puzzle::{Puzzle, PuzzleFn::I32};

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 6,
    part1: I32(part1),
    part2: I32(part2),
};

fn part1(input: &str) -> i32 {
    let mut cursor = 0;
    let mut buffer = [' '; 4];
    for (i, c) in input.chars().enumerate() {
        buffer[cursor] = c;
        cursor = (cursor + 1) % 4;
        let set = HashSet::from(buffer);
        if !set.contains(&' ') && set.len() == 4 {
            return (i + 1) as i32;
        }
    }
    -1
}

fn part2(input: &str) -> i32 {
    let mut cursor = 0;
    let mut buffer = [' '; 14];
    for (i, c) in input.chars().enumerate() {
        buffer[cursor] = c;
        cursor = (cursor + 1) % 14;
        let set = HashSet::from(buffer);
        if !set.contains(&' ') && set.len() == 14 {
            return (i + 1) as i32;
        }
    }
    -1
}

#[cfg(test)]
mod test {
    use crate::day06::{part1, part2};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 7)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 19)
    }

    const SAMPLE_INPUT: &str = "mjqjpqmgbljsphdztnvjfqwrcgsmlb";
}
