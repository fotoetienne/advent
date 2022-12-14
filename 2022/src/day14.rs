use std::cmp::{max, min};
use std::collections::HashMap;

use nom::bytes::complete::tag;
use nom::character::complete::i32 as nom_i32;
use nom::multi::separated_list1;
use nom::sequence::separated_pair;
use nom::IResult;

use crate::day14::Item::{FallingSand, Rock, Sand};
use crate::puzzle::{Puzzle, PuzzleFn::I32};

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 14,
    part1: I32(part1),
    part2: I32(part2),
};

type Point = (i32, i32);

fn part1(input: &str) -> i32 {
    let rocks = parse_input(input);
    let mut cave = build_cave(rocks);

    let mut sands = 0;
    while drop_sand(&mut cave, false) {
        sands += 1;
    }

    drop_sand(&mut cave, true);

    print_cave(&cave);
    sands
}

#[derive(Debug, Copy, Clone)]
enum Item {
    Sand,
    Rock,
    FallingSand,
}

fn parse_input(input: &str) -> Vec<Vec<Point>> {
    input.lines().map(|l| rock(l).unwrap().1).collect()
}

fn rock(i: &str) -> IResult<&str, Vec<Point>> {
    separated_list1(tag(" -> "), separated_pair(nom_i32, tag(","), nom_i32))(i)
}

fn build_cave(rocks: Vec<Vec<Point>>) -> HashMap<Point, Item> {
    let mut map = HashMap::new();
    for rock_path in rocks {
        let mut points = rock_path.iter();
        let mut a = points.next().unwrap();
        for b in points {
            // println!("({a:?},{b:?})");
            let rocks: Vec<Point> = if a.0 == b.0 {
                (min(a.1, b.1)..=max(a.1, b.1)).map(|y| (a.0, y)).collect()
            } else if a.1 == b.1 {
                (min(a.0, b.0)..=max(a.0, b.0)).map(|x| (x, a.1)).collect()
            } else {
                panic!("Unhandled: diagonal rock line {a:?} -> {b:?}")
            };
            for rock in rocks {
                map.insert(rock, Rock);
            }
            a = b;
        }
    }
    map
}

fn print_cave(cave: &HashMap<Point, Item>) {
    // dbg!(cave);
    let (mut xmin, mut xmax, mut ymin, mut ymax) = (i32::MAX, 0, i32::MAX, 0);
    for (x, y) in cave.keys() {
        (xmin, xmax) = (min(xmin, *x), max(xmax, *x));
        (ymin, ymax) = (min(ymin, *y), max(ymax, *y));
    }

    for y in 0..=ymax {
        for x in xmin..=xmax {
            let c = match cave.get(&(x, y)) {
                Some(Rock) => '#',
                Some(Sand) => 'o',
                Some(FallingSand) => '~',
                None => '.',
            };
            print!("{c}")
        }
        println!()
    }
    println!()
}

fn drop_sand(cave: &mut HashMap<Point, Item>, trace: bool) -> bool {
    let ymax = cave.iter().map(|((_, y), _)| y).max().unwrap();

    let (mut x, mut y) = (500, 0);
    let mut path = vec![];
    loop {
        let ps = vec![(x, y + 1), (x - 1, y + 1), (x + 1, y + 1)];
        if let Some(next) = ps.iter().find(|p| cave.get(*p).is_none()) {
            (x, y) = *next;
            if trace {
                path.push(*next);
            }
            if y > ymax + 3 {
                // free fall
                for p in path {
                    cave.insert(p, FallingSand);
                }
                return false;
            }
        } else {
            // landed
            break;
        }
    }
    cave.insert((x, y), Sand);
    true
}

fn part2(input: &str) -> i32 {
    let rocks = parse_input(input);
    let mut cave = build_cave(rocks);
    let (mut xmin, mut xmax, mut ymax) = (i32::MAX, 0, 0);
    for (x, y) in cave.keys() {
        (xmin, xmax, ymax) = (min(xmin, *x), max(xmax, *x), max(ymax, *y));
    }
    for x in (xmin - ymax)..=(xmax + ymax) {
        cave.insert((x, ymax + 2), Rock);
    }

    let mut sands = 0;
    while drop_sand(&mut cave, false) {
        sands += 1;
        if cave.get(&(500, 0)).is_some() {
            break;
        }
    }

    print_cave(&cave);
    sands
}

#[cfg(test)]
mod test {
    use crate::day14::{part1, part2, PUZZLE};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 24)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 93)
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "498,4 -> 498,6 -> 496,6
503,4 -> 502,4 -> 502,9 -> 494,9";
}
