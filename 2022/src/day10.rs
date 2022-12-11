use crate::day10::Instruction::{ADDX, NOOP};
use crate::puzzle::PuzzleFn::STR;
use crate::puzzle::{Puzzle, PuzzleFn::I32};
use nom::branch::alt;
use nom::bytes::complete::tag;
use nom::sequence::preceded;
use nom::IResult;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 10,
    part1: I32(part1),
    part2: STR(part2),
};

enum Instruction {
    NOOP,
    ADDX(i32),
}

fn parse_instruction(line: &str) -> Instruction {
    let (_, instr) = instruction(line).unwrap();
    instr
}

fn instruction(i: &str) -> IResult<&str, Instruction> {
    alt((noop, addx))(i)
}

fn noop(i: &str) -> IResult<&str, Instruction> {
    let (input, _) = tag("noop")(i)?;
    Ok((input, NOOP))
}

fn addx(i: &str) -> IResult<&str, Instruction> {
    let (input, v) = preceded(tag("addx "), nom::character::complete::i32)(i)?;
    Ok((input, ADDX(v)))
}

fn execute(instructions: Vec<Instruction>) -> Vec<i32> {
    let mut x = 1;
    let mut x_hist: Vec<i32> = vec![1];

    for instr in instructions {
        match instr {
            NOOP => x_hist.push(x),
            ADDX(v) => {
                x_hist.push(x);
                x += v;
                x_hist.push(x);
            }
        }
    }

    x_hist
}

fn part1(input: &str) -> i32 {
    let instructions = input.lines().map(parse_instruction);
    let x_hist = execute(instructions.collect());
    let signal_strength = |cycle: i32| cycle * x_hist[cycle as usize - 1];
    (0..6).map(|i| signal_strength(i * 40 + 20)).sum()
}

fn part2(input: &str) -> String {
    let instructions = input.lines().map(parse_instruction);
    let x_hist = execute(instructions.collect());
    let mut output: Vec<char> = vec!['\n'];
    for row in 0..6 {
        for p in 0..40 {
            let x = x_hist[(row * 40 + p) as usize];
            let c = if (x - 1..=x + 1).contains(&p) {
                '#'
            } else {
                '.'
            };
            output.push(c);
        }
        output.push('\n');
    }
    output.into_iter().collect()
}

#[cfg(test)]
mod test {
    use crate::day10::{execute, parse_instruction, part1, part2, PUZZLE};

    #[test]
    fn small_example() {
        let program = "noop
addx 3
addx -5";
        let x_hist = execute(program.lines().map(parse_instruction).collect());
        assert_eq!(x_hist, vec![1, 1, 1, 4, 4, -1])
    }

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 13140)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        let expected = "
##..##..##..##..##..##..##..##..##..##..
###...###...###...###...###...###...###.
####....####....####....####....####....
#####.....#####.....#####.....#####.....
######......######......######......####
#######.......#######.......#######.....
";
        assert_eq!(answer, expected);
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "addx 15
addx -11
addx 6
addx -3
addx 5
addx -1
addx -8
addx 13
addx 4
noop
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx -35
addx 1
addx 24
addx -19
addx 1
addx 16
addx -11
noop
noop
addx 21
addx -15
noop
noop
addx -3
addx 9
addx 1
addx -3
addx 8
addx 1
addx 5
noop
noop
noop
noop
noop
addx -36
noop
addx 1
addx 7
noop
noop
noop
addx 2
addx 6
noop
noop
noop
noop
noop
addx 1
noop
noop
addx 7
addx 1
noop
addx -13
addx 13
addx 7
noop
addx 1
addx -33
noop
noop
noop
addx 2
noop
noop
noop
addx 8
noop
addx -1
addx 2
addx 1
noop
addx 17
addx -9
addx 1
addx 1
addx -3
addx 11
noop
noop
addx 1
noop
addx 1
noop
noop
addx -13
addx -19
addx 1
addx 3
addx 26
addx -30
addx 12
addx -1
addx 3
addx 1
noop
noop
noop
addx -9
addx 18
addx 1
addx 2
noop
noop
addx 9
noop
noop
noop
addx -1
addx 2
addx -37
addx 1
addx 3
noop
addx 15
addx -21
addx 22
addx -6
addx 1
noop
addx 2
addx 1
noop
addx -10
noop
noop
addx 20
addx 1
addx 2
addx 2
addx -6
addx -11
noop
noop
noop";
}
