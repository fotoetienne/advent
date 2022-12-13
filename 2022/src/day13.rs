use crate::day13::Value::List;
use crate::puzzle::{Puzzle, PuzzleFn::I32};
use itertools::Itertools;
use nom::branch::alt;
use nom::bytes::complete::tag;
use nom::character::complete::i32 as nom_i32;
use nom::combinator::map as nom_map;
use nom::multi::separated_list0;
use nom::sequence::delimited;
use nom::IResult;
use std::cmp::Ordering;
use Value::Int;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 13,
    part1: I32(part1),
    part2: I32(part2),
};

#[derive(Clone, Debug)]
enum Value {
    Int(i32),
    List(Vec<Value>),
}

impl Eq for Value {}

impl PartialEq<Self> for Value {
    fn eq(&self, other: &Self) -> bool {
        match (self, other) {
            (Int(left), Int(right)) => left == right,
            (Int(_), List(_)) => List(vec![self.clone()]) == *other,
            (List(left), List(right)) => left == right,
            (List(_), Int(_)) => self == &List(vec![other.clone()]),
        }
    }
}

impl PartialOrd<Self> for Value {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for Value {
    fn cmp(&self, other: &Self) -> Ordering {
        match (self, other) {
            (Int(left), Int(right)) => left.cmp(right),
            (Int(_), List(_)) => List(vec![self.clone()]).cmp(other),
            (List(left), List(right)) => left.cmp(right),
            (List(_), Int(_)) => self.cmp(&List(vec![other.clone()])),
        }
    }
}

fn parse_input(input: &str) -> Vec<(Value, Value)> {
    input.split("\n\n").map(parse_packets).collect()
}

fn parse_packets(i: &str) -> (Value, Value) {
    let mut lines = i.lines();
    let (_, left) = list(lines.next().unwrap()).unwrap();
    let (_, right) = list(lines.next().unwrap()).unwrap();
    (left, right)
}

fn int(i: &str) -> IResult<&str, Value> {
    nom_map(nom_i32, Int)(i)
}

fn list(i: &str) -> IResult<&str, Value> {
    nom_map(
        delimited(
            tag("["),
            separated_list0(tag(","), alt((int, list))),
            tag("]"),
        ),
        Value::List,
    )(i)
}

fn ordered_correctly((left, right): &(Value, Value)) -> bool {
    left < right
}

fn part1(input: &str) -> i32 {
    let result: Vec<bool> = parse_input(input).iter().map(ordered_correctly).collect();
    result
        .iter()
        .enumerate()
        .filter_map(|(i, v)| if *v { Some(i as i32 + 1) } else { None })
        .sum()
}

const DIVIDER_PACKETS: &str = "[[2]]\n[[6]]\n";

fn part2(input: &str) -> i32 {
    (DIVIDER_PACKETS.to_string() + input)
        .lines()
        .filter_map(|l| match list(l) {
            Ok((_, v)) => Some(v),
            Err(_) => None,
        })
        .sorted()
        .enumerate()
        .filter_map(|(i, v)| {
            let (d2, d6) = parse_packets(DIVIDER_PACKETS);
            if v == d2 || v == d6 {
                Some(i as i32 + 1)
            } else {
                None
            }
        })
        .product()
}

#[cfg(test)]
mod test {
    use crate::day13::Value::{Int, List};
    use crate::day13::{list, ordered_correctly, parse_packets, part1, part2, PUZZLE};

    #[test]
    fn parser_test() {
        let (_, result) = list("[1,1,3,1,[[]]]").unwrap();
        let expected = List(vec![
            Int(1),
            Int(1),
            Int(3),
            Int(1),
            List(vec![List(vec![])]),
        ]);
        assert_eq!(result, expected)
    }

    #[test]
    fn order_test() {
        assert!(ordered_correctly(&parse_packets(
            "[1,1,3,1,1]\n[1,1,5,1,1]"
        )));
        assert!(ordered_correctly(&parse_packets("[[1],[2,3,4]]\n[[1],4]")));
    }

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 13)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 140)
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "[1,1,3,1,1]
[1,1,5,1,1]

[[1],[2,3,4]]
[[1],4]

[9]
[[8,7,6]]

[[4,4],4,4]
[[4,4],4,4,4]

[7,7,7,7]
[7,7,7]

[]
[3]

[[[]]]
[[]]

[1,[2,[3,[4,[5,6,7]]]],8,9]
[1,[2,[3,[4,[5,6,0]]]],8,9]";
}
