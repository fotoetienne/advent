use crate::puzzle::Puzzle;
use crate::puzzle::PuzzleFn::{U64, USIZE};
use nom::bytes::complete::tag;
use nom::character::complete::i32 as parse_i32;
use nom::sequence::{pair, preceded};
use nom::IResult;
use std::collections::HashSet;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 15,
    part1: USIZE(|i| part1(i, 2000000)),
    part2: U64(|i| part2(i, 4000000)),
};

fn part1(input: &str, y: i32) -> usize {
    let sensor_data = parse_input(input);
    let mut row: HashSet<i32> = HashSet::new();

    for (sensor, beacon) in sensor_data {
        let beacon_dist = manhattan_dist(&sensor, &beacon);
        let mut x = sensor.0;
        while manhattan_dist(&sensor, &(x, y)) <= beacon_dist && (x, y) != beacon {
            row.insert(x);
            x += 1;
        }
        let mut x = sensor.0;
        while manhattan_dist(&sensor, &(x, y)) <= beacon_dist && (x, y) != beacon {
            row.insert(x);
            x -= 1;
        }
    }

    row.len()
}

fn part2(input: &str, bound: i32) -> u64 {
    let sensor_data = parse_input(input);
    let sensor_ranges: Vec<(&Point, i32)> = sensor_data
        .iter()
        .map(|(sensor, beacon)| (sensor, manhattan_dist(sensor, beacon)))
        .collect();

    for y in 0..=bound {
        let mut x = 0;
        'row: while x <= bound {
            for (sensor, range) in &sensor_ranges {
                if manhattan_dist(sensor, &(x as i32, y as i32)) <= *range {
                    // In range of sensor. Skip to the next point on the same row that is our of range
                    let y_dist = (y - sensor.1).abs();
                    x = sensor.0 + (range - y_dist) + 1;
                    continue 'row;
                }
            }
            // No sensors in range. This is it!
            // println!("Distress beacon: {x},{y}");
            return x as u64 * 4000000 + y as u64;
        }
    }
    panic!("No solution found!");
}

fn manhattan_dist(a: &Point, b: &Point) -> i32 {
    (a.0 - b.0).abs() + (a.1 - b.1).abs()
}

type Point = (i32, i32);

fn parse_input(input: &str) -> Vec<(Point, Point)> {
    input.lines().map(|l| parse_sensor(l).unwrap().1).collect()
}

fn parse_sensor(i: &str) -> IResult<&str, (Point, Point)> {
    // x=2, y=18
    let point = |i| {
        pair(
            preceded(tag("x="), parse_i32),
            preceded(tag(", y="), parse_i32),
        )(i)
    };
    // Sensor at x=9, y=16: closest beacon is at x=10, y=16
    pair(
        preceded(tag("Sensor at "), point),
        preceded(tag(": closest beacon is at "), point),
    )(i)
}

#[cfg(test)]
mod test {
    use crate::day15::{part1, part2, PUZZLE};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT, 10);
        assert_eq!(answer, 26)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT, 20);
        assert_eq!(answer, 56000011)
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "Sensor at x=2, y=18: closest beacon is at x=-2, y=15
Sensor at x=9, y=16: closest beacon is at x=10, y=16
Sensor at x=13, y=2: closest beacon is at x=15, y=3
Sensor at x=12, y=14: closest beacon is at x=10, y=16
Sensor at x=10, y=20: closest beacon is at x=10, y=16
Sensor at x=14, y=17: closest beacon is at x=10, y=16
Sensor at x=8, y=7: closest beacon is at x=2, y=10
Sensor at x=2, y=0: closest beacon is at x=2, y=10
Sensor at x=0, y=11: closest beacon is at x=2, y=10
Sensor at x=20, y=14: closest beacon is at x=25, y=17
Sensor at x=17, y=20: closest beacon is at x=21, y=22
Sensor at x=16, y=7: closest beacon is at x=15, y=3
Sensor at x=14, y=3: closest beacon is at x=15, y=3
Sensor at x=20, y=1: closest beacon is at x=15, y=3";
}
