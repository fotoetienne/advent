use crate::util::get_input;
use anyhow::Result;

pub(crate) struct Puzzle {
    pub(crate) day: i32,
    pub(crate) part1: PuzzleFn,
    pub(crate) part2: PuzzleFn,
}

impl Puzzle {
    pub(crate) fn run(&self) -> Result<()> {
        let input = get_input(self.day)?;
        let answer = self.part1.invoke(&input);
        println!("Day {} part 1: {}", self.day, answer);
        let answer2 = self.part2.invoke(&input);
        println!("Day {} part 2: {}", self.day, answer2);
        Ok(())
    }
}

pub(crate) enum PuzzleFn {
    I32(fn(&str) -> i32),
    USIZE(fn(&str) -> usize),
    STR(fn(&str) -> String),
}

impl PuzzleFn {
    fn invoke(&self, input: &str) -> String {
        match self {
            PuzzleFn::I32(f) => f(input).to_string(),
            PuzzleFn::USIZE(f) => f(input).to_string(),
            PuzzleFn::STR(f) => f(input),
        }
    }
}
