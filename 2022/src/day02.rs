use crate::day02::RPC::{PAPER, ROCK, SCISSORS};
use crate::day02::WLD::{DRAW, LOSE, WIN};

use crate::puzzle::{Puzzle, PuzzleFn::I32};

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 2,
    part1: I32(part1),
    part2: I32(part2),
};

fn part1(input: &str) -> i32 {
    input
        .lines()
        .map(|line| {
            let mut chars = line.chars();
            let opponent = parse_rpc(chars.next().unwrap());
            let me = parse_rpc(chars.nth(1).unwrap());
            score(opponent, me)
        })
        .sum()
}

fn part2(input: &str) -> i32 {
    input
        .lines()
        .map(|line| {
            let mut chars = line.chars();
            let opponent = parse_rpc(chars.next().unwrap());
            let me_wld = parse_wld(chars.nth(1).unwrap());
            let me = match me_wld {
                WIN => match opponent {
                    ROCK => PAPER,
                    PAPER => SCISSORS,
                    SCISSORS => ROCK,
                },
                LOSE => match opponent {
                    ROCK => SCISSORS,
                    PAPER => ROCK,
                    SCISSORS => PAPER,
                },
                DRAW => opponent,
            };

            score(opponent, me)
        })
        .sum()
}

fn parse_rpc(c: char) -> RPC {
    match c {
        'A' | 'X' => ROCK,
        'B' | 'Y' => PAPER,
        'C' | 'Z' => SCISSORS,
        _ => {
            panic!("Can't parse: {}", c)
        }
    }
}

fn parse_wld(c: char) -> WLD {
    match c {
        'X' => LOSE,
        'Y' => DRAW,
        'Z' => WIN,
        _ => {
            panic!("Can't parse: {}", c)
        }
    }
}

#[derive(Clone, Copy)]
enum RPC {
    ROCK,
    PAPER,
    SCISSORS,
}

enum WLD {
    WIN,
    LOSE,
    DRAW,
}

fn score(opponent: RPC, me: RPC) -> i32 {
    let (lose, draw, win) = (0, 3, 6);
    match me {
        ROCK => {
            1 + match opponent {
                ROCK => draw,
                PAPER => lose,
                SCISSORS => win,
            }
        }
        PAPER => {
            2 + match opponent {
                ROCK => win,
                PAPER => draw,
                SCISSORS => lose,
            }
        }
        SCISSORS => {
            3 + match opponent {
                ROCK => lose,
                PAPER => win,
                SCISSORS => draw,
            }
        }
    }
}

#[cfg(test)]
mod test {
    use crate::day02::{part1, part2};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 15)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 12)
    }

    const SAMPLE_INPUT: &str = "A Y
B X
C Z";
}
