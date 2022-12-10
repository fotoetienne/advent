use crate::puzzle::Puzzle;
use crate::puzzle::PuzzleFn::I32;
use std::cmp::max;
use std::collections::BinaryHeap;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 1,
    part1: I32(count_calories),
    part2: I32(top_3),
};

// Part 1
fn count_calories(input: &str) -> i32 {
    let mut max_sum = 0;
    let mut cum_sum = 0;

    for line in input.lines() {
        if line.is_empty() {
            cum_sum = 0;
        } else {
            let calories: i32 = line
                .parse()
                .unwrap_or_else(|_| panic!("Unable to parse line: {}", line));
            cum_sum += calories;
        }
        max_sum = max(max_sum, cum_sum);
    }

    max_sum
}

// Part 2
fn top_3(input: &str) -> i32 {
    let mut elf_heap = BinaryHeap::new();
    let mut cum_sum = 0;

    for line in input.lines() {
        if line.is_empty() {
            elf_heap.push(cum_sum);
            cum_sum = 0;
        } else {
            let calories: i32 = line
                .parse()
                .unwrap_or_else(|_| panic!("Unable to parse line: {}", line));
            cum_sum += calories;
        }
    }
    elf_heap.push(cum_sum);

    let mut sum = 0;
    for _ in 0..3 {
        sum += elf_heap.pop().unwrap();
    }
    sum
}

#[cfg(test)]
mod tests {
    use crate::day01::{count_calories, top_3};

    #[test]
    fn count_calories_test() {
        let result = count_calories(SAMPLE_INPUT);
        assert_eq!(result, 24000);
    }

    #[test]
    fn top_3_test() {
        let result = top_3(SAMPLE_INPUT);
        assert_eq!(result, 45000);
    }

    const SAMPLE_INPUT: &str = "1000
2000
3000

4000

5000
6000

7000
8000
9000

10000";
}
