use crate::puzzle::{Puzzle, PuzzleFn::I32};

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 0,
    part1: I32(part1),
    part2: I32(part2),
};

fn part1(input: &str) -> i32 {
    todo!()
}

fn part2(input: &str) -> i32 {
    todo!()
}

#[cfg(test)]
mod test {
    use crate::template::{part1, part2, PUZZLE};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 0)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 0)
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "";
}
