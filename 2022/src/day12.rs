use crate::puzzle::{Puzzle, PuzzleFn::I32};
use crate::util::color_gradient;
use std::collections::VecDeque;
use yansi::Paint;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 12,
    part1: I32(part1),
    part2: I32(part2),
};

struct TrailMap {
    topo: Matrix<char>,
    start: Point,
    end: Point,
}

struct Matrix<T> {
    inner: Vec<Vec<T>>,
    m: i32,
    n: i32,
}

type Point = (i32, i32);

impl<T> Matrix<T> {
    fn get(&self, p: &Point) -> Option<&T> {
        if p.0 < 0 || p.1 < 0 || p.0 >= self.m || p.1 >= self.n {
            None
        } else {
            Some(&self.inner[p.0 as usize][p.1 as usize])
        }
    }

    fn set(&mut self, p: &Point, v: T) {
        self.inner[p.0 as usize][p.1 as usize] = v;
    }

    fn neighbors(&self, (i, j): &Point) -> Vec<Point> {
        vec![(i + 1, *j), (i - 1, *j), (*i, j + 1), (*i, j - 1)]
            .iter()
            .filter(|p| self.get(p).is_some())
            .copied()
            .collect()
    }
}

fn print_map(map: &Matrix<char>, dist: &Matrix<i32>, shortest_dist: i32) {
    for i in 0..map.m {
        for j in 0..map.n {
            let c = map.get(&(i, j)).unwrap();
            let d: i32 = *dist.get(&(i, j)).unwrap();
            let (r, g, b) = if d == i32::MAX {
                (100, 100, 100)
            } else {
                color_gradient(d * (360 / shortest_dist))
            };
            print!("{} ", Paint::rgb(r, g, b, c));
        }
        println!();
    }
    println!();
}

fn parse_input(input: &str) -> TrailMap {
    let inner: Vec<Vec<char>> = input.lines().map(|line| line.chars().collect()).collect();
    let m = inner.len() as i32;
    let n = inner[0].len() as i32;
    let mut start: Point = (0, 0);
    let mut end: Point = (0, 0);
    for i in 0..m {
        for j in 0..n {
            match inner[i as usize][j as usize] {
                'S' => start = (i, j),
                'E' => end = (i, j),
                _ => {}
            }
        }
    }
    let topo = Matrix { inner, m, n };
    TrailMap { topo, start, end }
}

fn val(c: &char) -> i32 {
    match c {
        'S' => 0,
        'E' => 25,
        _ => c.to_digit(36).unwrap() as i32 - 10,
    }
}
fn shortest_path(
    topo: &Matrix<char>,
    start: Point,
    path_ok: fn(i32, i32) -> bool,
    is_goal: fn(char) -> bool,
) -> i32 {
    let mut dist: Matrix<i32> = Matrix {
        inner: vec![vec![i32::MAX; topo.n as usize]; topo.m as usize],
        m: topo.m,
        n: topo.n,
    };

    dist.set(&start, 0);
    let mut curr_dist = 0;
    let mut q: VecDeque<Point> = VecDeque::from([start]);
    'outer: loop {
        if q.is_empty() {
            break;
        }
        let p = q.pop_front().unwrap();
        let p_dist = dist.get(&p).unwrap();
        curr_dist = p_dist + 1;
        let p_elevation = val(topo.get(&p).unwrap());

        for neighbor in topo.neighbors(&p) {
            if dist.get(&neighbor).unwrap() != &i32::MAX {
                continue;
            }
            if let Some(c) = topo.get(&neighbor) {
                let elevation = val(c);
                if path_ok(p_elevation, elevation) {
                    dist.set(&neighbor, curr_dist);
                    if is_goal(*c) {
                        break 'outer;
                    }
                    q.push_back(neighbor);
                }
            }
        }
    }
    print_map(&topo, &dist, curr_dist);
    curr_dist
}

fn part1(input: &str) -> i32 {
    let map = parse_input(input);
    let TrailMap { topo, start, end } = map;
    let path_ok = |here, there| there - here <= 1;
    shortest_path(&topo, start, path_ok, |c| c == 'E')
}

fn part2(input: &str) -> i32 {
    let map = parse_input(input);
    let TrailMap { topo, start, end } = map;
    let path_ok = |here, there| here - there <= 1;
    shortest_path(&topo, end, path_ok, |c| c == 'a' || c == 'S')
}

#[cfg(test)]
mod test {
    use crate::day12::{part1, part2, val, PUZZLE};

    #[test]
    fn val_test() {
        assert_eq!(val(&'a'), 0);
        assert_eq!(val(&'z'), 25);
        assert_eq!(val(&'S'), 0);
        assert_eq!(val(&'E'), 25);
    }

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 31)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 29)
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "Sabqponm
abcryxxl
accszExk
acctuvwj
abdefghi";
}
