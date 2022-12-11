use crate::puzzle::{Puzzle, PuzzleFn::I32};
use std::cmp::max;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 8,
    part1: I32(part1),
    part2: I32(part2),
};

type Matrix<T> = Vec<Vec<T>>;

fn parse_matrix(input: &str) -> (Matrix<u32>, usize, usize) {
    let matrix: Matrix<u32> = input
        .lines()
        .map(|line| line.chars().map(|c| c.to_digit(10).unwrap()).collect())
        .collect();
    let h = matrix.len();
    let w = matrix[0].len();
    (matrix, w, h)
}

fn part1(input: &str) -> i32 {
    let (trees, w, h) = parse_matrix(input);

    // define mutable matrix for storing visibility
    let mut visible: Matrix<i32> = vec![vec![0; h]; w];
    // mark all edges as visible
    for x in 0..w {
        visible[x][0] = 1;
        visible[x][h - 1] = 1;
    }
    for y in 0..h {
        visible[0][y] = 1;
        visible[w - 1][y] = 1;
    }

    let mut find_visible = |x_range: Vec<usize>, y_range: Vec<usize>, rotate: bool| {
        for i in x_range {
            let mut max_height = 0;
            for j in y_range.clone() {
                let (x, y) = if !rotate { (i, j) } else { (j, i) };
                let tree = trees[x][y];
                // print!("({},{}):{}", x, y, tree);
                if tree > max_height {
                    // print!("Visible!");
                    visible[x][y] = 1;
                }
                max_height = max(max_height, tree);
                if max_height == 9 {
                    break;
                }
                // println!();
            }
        }
    };

    //println!("left");
    find_visible((1..h - 1).collect(), (0..w - 1).collect(), false);
    //println!("top");
    find_visible((1..w - 1).collect(), (0..h - 1).collect(), true);
    //println!("right");
    find_visible((1..h - 1).collect(), (1..w).rev().collect(), false);
    //println!("bottom");
    find_visible((1..w - 1).collect(), (1..h).rev().collect(), true);

    // sum up the visible trees
    visible.iter().map(|row| row.iter().sum::<i32>()).sum()
}

fn part2(input: &str) -> i32 {
    let (trees, w, h) = parse_matrix(input);

    let points = (0..h).flat_map(|x| (0..w).map(move |y| (x, y)));

    points
        .map(|tree| scenic_score(tree, &trees, w, h))
        .max()
        .unwrap()
}

fn scenic_score(tree: (usize, usize), trees: &Matrix<u32>, w: usize, h: usize) -> i32 {
    let (x, y) = tree;

    let height = trees[x][y];

    let mut up = 0;
    for xx in (0..x).rev() {
        up += 1;
        if trees[xx][y] >= height {
            break;
        }
    }

    let mut left = 0;
    for yy in (0..y).rev() {
        left += 1;
        if trees[x][yy] >= height {
            break;
        }
    }

    let mut right = 0;
    for yy in y + 1..w {
        right += 1;
        let i = trees[x][yy];
        if i >= height {
            break;
        }
    }

    let mut down = 0;
    for xx in x + 1..h {
        down += 1;
        if trees[xx][y] >= height {
            break;
        }
    }

    // println!("{} * {} * {} * {}", up, left, right, down);
    up * left * right * down
}

#[cfg(test)]
mod test {
    use crate::day08::{parse_matrix, part1, part2, scenic_score};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 21)
    }

    #[test]
    fn scenic_score_test() {
        let (trees, w, h) = parse_matrix(SAMPLE_INPUT);
        let score = scenic_score((1, 2), &trees, w, h);
        assert_eq!(score, 4)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 8)
    }

    const SAMPLE_INPUT: &str = "30373
25512
65332
33549
35390";
}
