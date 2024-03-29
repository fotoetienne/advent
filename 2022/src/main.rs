extern crate core;

mod day01;
mod day02;
mod day03;
mod day04;
mod day05;
mod day06;
mod day07;
mod day08;
mod day09;
mod day10;
mod day11;
mod day12;
mod day13;
mod day14;
mod day15;
mod day16;
mod puzzle;
mod util;

use crate::puzzle::Puzzle;
use anyhow::Result;

fn main() -> Result<()> {
    let puzzles: Vec<Puzzle> = vec![
        day01::PUZZLE,
        day02::PUZZLE,
        day03::PUZZLE,
        day04::PUZZLE,
        day05::PUZZLE,
        day06::PUZZLE,
        day07::PUZZLE,
        day08::PUZZLE,
        day09::PUZZLE,
        day10::PUZZLE,
        day11::PUZZLE,
        day12::PUZZLE,
        day13::PUZZLE,
        day14::PUZZLE,
        day15::PUZZLE,
        day16::PUZZLE,
    ];

    for puzzle in puzzles {
        puzzle.run()?;
    }

    Ok(())
}
