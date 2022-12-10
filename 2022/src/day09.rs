use crate::day09::Direction::{DOWN, LEFT, RIGHT, UP};
use crate::puzzle::{Puzzle, PuzzleFn::USIZE};
use std::cmp::Ordering;
use std::collections::HashSet;
use std::fmt::{Display, Formatter};

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 9,
    part1: USIZE(part1),
    part2: USIZE(part2),
};

#[derive(Debug, Clone, Copy)]
enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

impl Display for Direction {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            UP => write!(f, "UP"),
            DOWN => write!(f, "DOWN"),
            LEFT => write!(f, "LEFT"),
            RIGHT => write!(f, "RIGHT"),
        }
    }
}

struct Instruction {
    direction: Direction,
    n: i32,
}

fn parse_line(line: &str) -> Instruction {
    let mut parts = line.split(' ');
    let direction = match parts.next() {
        Some("U") => UP,
        Some("D") => DOWN,
        Some("R") => RIGHT,
        Some("L") => LEFT,
        _ => panic!("Can't parse {}", line),
    };
    let n: i32 = parts.next().unwrap().parse().unwrap();
    Instruction { direction, n }
}

fn parse_input(input: &str) -> Vec<Instruction> {
    input.lines().map(parse_line).collect()
}

type Point = (i32, i32);

fn part1(input: &str) -> usize {
    let instructions = parse_input(input);

    let mut head: Point = (0, 0);
    let mut tail: Point = (0, 0);
    let mut tail_positions: HashSet<Point> = HashSet::new();
    tail_positions.insert(tail);

    for instruction in instructions {
        for _ in 0..instruction.n {
            head = move_point(head, &instruction.direction);
            tail = follow(head, tail);
            tail_positions.insert(tail);
        }
        // println!(
        //     "{} {} ({},{}) ({},{})",
        //     instruction.direction, instruction.n, head.0, head.1, tail.0, tail.1
        // );
    }

    tail_positions.len()
}

fn move_point(p: Point, direction: &Direction) -> Point {
    match direction {
        UP => (p.0 + 1, p.1),
        DOWN => (p.0 - 1, p.1),
        LEFT => (p.0, p.1 - 1),
        RIGHT => (p.0, p.1 + 1),
    }
}

fn follow(head: Point, tail: Point) -> Point {
    if (tail.0 - head.0).abs() > 1 || (tail.1 - head.1).abs() > 1 {
        (tail.0 + cmp(head.0, tail.0), tail.1 + cmp(head.1, tail.1))
    } else {
        tail
    }
}

fn cmp(a: i32, b: i32) -> i32 {
    match a.cmp(&b) {
        Ordering::Less => -1,
        Ordering::Equal => 0,
        Ordering::Greater => 1,
    }
}

fn part2(input: &str) -> usize {
    let instructions = parse_input(input);

    let mut knots: Vec<Point> = vec![(0, 0); 10];
    let mut tail_positions: HashSet<Point> = HashSet::new();
    tail_positions.insert((0, 0));

    for instruction in instructions {
        for _ in 0..instruction.n {
            knots[0] = move_point(knots[0], &instruction.direction);
            for i in 1..10 {
                knots[i] = follow(knots[i - 1], knots[i]);
            }
            tail_positions.insert(knots[9]);
        }
        // println!(
        //     "{} {} ({},{}) ({},{})",
        //     instruction.direction, instruction.n, head.0, head.1, tail.0, tail.1
        // );
    }

    tail_positions.len()
}

#[cfg(test)]
mod test {
    use crate::day09::{part1, part2, PUZZLE};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 13)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 1);
        let answer = part2(SAMPLE_INPUT_2);
        assert_eq!(answer, 36);
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "R 4
U 4
L 3
D 1
R 4
D 1
L 5
R 2";
    const SAMPLE_INPUT_2: &str = "R 5
U 8
L 8
D 3
R 17
D 10
L 25
U 20";
}
