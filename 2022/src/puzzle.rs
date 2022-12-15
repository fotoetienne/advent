use crate::util::get_input;
use anyhow::Result;
use std::time::Instant;

pub(crate) struct Puzzle {
    pub(crate) day: i32,
    pub(crate) part1: PuzzleFn,
    pub(crate) part2: PuzzleFn,
}

impl Puzzle {
    pub(crate) fn run(&self) -> Result<()> {
        let input = get_input(self.day)?;
        let start = Instant::now();
        let answer = self.part1.invoke(&input);
        let duration = start.elapsed();
        println!(
            "Day {} part 1 ({} ms): {}",
            self.day,
            duration.as_micros() as f64 / 1000.0,
            answer,
        );
        let start2 = Instant::now();
        let answer2 = self.part2.invoke(&input);
        let duration2 = start2.elapsed();
        println!(
            "Day {} part 2 ({} ms): {}",
            self.day,
            duration2.as_micros() as f64 / 1000.0,
            answer2,
        );
        Ok(())
    }
}

pub(crate) enum PuzzleFn {
    I32(fn(&str) -> i32),
    U64(fn(&str) -> u64),
    USIZE(fn(&str) -> usize),
    STR(fn(&str) -> String),
}

impl PuzzleFn {
    fn invoke(&self, input: &str) -> String {
        match self {
            PuzzleFn::I32(f) => f(input).to_string(),
            PuzzleFn::U64(f) => f(input).to_string(),
            PuzzleFn::USIZE(f) => f(input).to_string(),
            PuzzleFn::STR(f) => f(input),
        }
    }
}
