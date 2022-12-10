use crate::puzzle::{Puzzle, PuzzleFn::STR};
use lazy_static::lazy_static;
use regex::Regex;
use std::str::FromStr;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 5,
    part1: STR(part1),
    part2: STR(part2),
};

type Stacks = Vec<Vec<char>>;

fn part1(input: &str) -> String {
    let (mut stacks, instructions) = parse_input(input);
    for instr in instructions {
        for _ in 0..instr.n_crates {
            let crate_to_move = stacks.get_mut(instr.from - 1).unwrap().pop().unwrap();
            stacks.get_mut(instr.to - 1).unwrap().push(crate_to_move);
        }
    }
    top_stacks(stacks)
}

fn parse_input(input: &str) -> (Stacks, Vec<Instruction>) {
    let mut split_input = input.split("\n\n");
    let stacks = parse_stacks(split_input.next().unwrap());
    let instructions = split_input
        .next()
        .unwrap()
        .lines()
        .map(|line| line.parse::<Instruction>().unwrap())
        .collect();
    (stacks, instructions)
}

fn top_stacks(stacks: Vec<Vec<char>>) -> String {
    let mut top_stacks: Vec<char> = vec![];
    for mut stack in stacks {
        top_stacks.push(stack.pop().unwrap())
    }
    top_stacks.into_iter().collect()
}

fn parse_stacks(input: &str) -> Vec<Vec<char>> {
    let mut reversed = input.lines().rev();
    let indices = reversed.next().unwrap();
    let n_stacks = (indices.len() + 1) / 4;
    let mut stacks: Vec<Vec<char>> = vec![];
    stacks.resize_with(n_stacks, Vec::new);
    for line in reversed {
        let chars: Vec<char> = line.chars().collect();
        for stack in 0..n_stacks {
            let c = chars.get(stack * 4 + 1).unwrap();
            if c.is_ascii_uppercase() {
                stacks.get_mut(stack).unwrap().push(*c);
            }
        }
    }
    stacks
}

struct Instruction {
    n_crates: usize,
    from: usize,
    to: usize,
}

impl FromStr for Instruction {
    type Err = ();

    fn from_str(s: &str) -> std::result::Result<Self, Self::Err> {
        lazy_static! {
            static ref RE: Regex = Regex::new(r"^move (\d+) from (\d+) to (\d+)$").unwrap();
        }
        let caps = RE.captures(s).unwrap();
        Ok(Instruction {
            n_crates: caps.get(1).unwrap().as_str().parse().unwrap(),
            from: caps.get(2).unwrap().as_str().parse().unwrap(),
            to: caps.get(3).unwrap().as_str().parse().unwrap(),
        })
    }
}

fn part2(input: &str) -> String {
    let (mut stacks, instructions) = parse_input(input);
    let mut flipper: Vec<char> = vec![];
    for instr in instructions {
        for _ in 0..instr.n_crates {
            let crate_to_move = stacks.get_mut(instr.from - 1).unwrap().pop().unwrap();
            flipper.push(crate_to_move);
        }
        for _ in 0..instr.n_crates {
            let crate_to_move = flipper.pop().unwrap();
            stacks.get_mut(instr.to - 1).unwrap().push(crate_to_move);
        }
    }
    top_stacks(stacks)
}

#[cfg(test)]
mod test {
    use crate::day05::{parse_stacks, part1, part2, Instruction};

    #[test]
    fn parse_stacks_test() {
        let stack_input = "    [D]    
[N] [C]    
[Z] [M] [P]
 1   2   3 ";
        let mut stacks = parse_stacks(stack_input);
        assert_eq!(stacks.len(), 3);
        assert_eq!(stacks.get(1).unwrap().len(), 3);
        assert_eq!(stacks.get_mut(0).unwrap().pop().unwrap(), 'N');
        assert_eq!(stacks.get_mut(1).unwrap().pop().unwrap(), 'D');
        assert_eq!(stacks.get_mut(2).unwrap().pop().unwrap(), 'P');
    }

    #[test]
    fn instruction_parsing_test() {
        let instr = "move 10 from 5 to 9".parse::<Instruction>().unwrap();
        assert_eq!(instr.n_crates, 10);
        assert_eq!(instr.from, 5);
        assert_eq!(instr.to, 9);
    }

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, "CMZ")
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, "MCD")
    }

    const SAMPLE_INPUT: &str = "    [D]    
[N] [C]    
[Z] [M] [P]
 1   2   3 

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2";
}
