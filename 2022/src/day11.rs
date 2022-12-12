use crate::puzzle::{Puzzle, PuzzleFn::U64};
use nom::branch::alt;
use nom::bytes::complete::tag;
use nom::character::complete::u32 as nom_u32;
use nom::character::complete::u64 as nom_u64;
use nom::combinator::map as nom_map;
use nom::multi::separated_list1;
use nom::sequence::{delimited, preceded, tuple};
use nom::IResult;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 11,
    part1: U64(part1),
    part2: U64(part2),
};

struct Monkey {
    n: u32,
    items: Vec<u64>,
    operation: Operation,
    divisible_by: u64,
    if_true: u32,
    if_false: u32,
    inspections: u64,
}

impl Monkey {
    fn inspect(&self, item: u64) -> u64 {
        let op = &self.operation;
        let eval = |operand: &Operand| match operand {
            Operand::VAR(_) => item,
            Operand::INT(v) => *v,
        };
        let (a, b) = (eval(&op.a), eval(&op.b));
        match op.op {
            Operator::MULT => a * b,
            Operator::ADD => a + b,
        }
    }

    fn throw(&self, item: u64) -> usize {
        if item % self.divisible_by == 0 {
            self.if_true as usize
        } else {
            self.if_false as usize
        }
    }

    fn take_turn(
        &self,
        items: &Vec<u64>,
        divisor: Option<u64>,
        modulus: Option<u64>,
    ) -> Vec<(usize, u64)> {
        let mut thrown = vec![];
        for item in items {
            let worry = self.inspect(*item) / divisor.unwrap_or(1);
            let worry = if modulus.is_some() {
                worry % modulus.unwrap()
            } else {
                worry
            };
            let throw_to = self.throw(worry);
            thrown.push((throw_to, worry))
        }
        thrown
    }
}

struct Operation {
    a: Operand,
    op: Operator,
    b: Operand,
}

enum Operator {
    MULT,
    ADD,
}

enum Operand {
    VAR(String),
    INT(u64),
}

fn parse_input(input: &str) -> Vec<Monkey> {
    input
        .split("\n\n")
        .map(|line| monkey(line).unwrap().1)
        .collect()
}

// Monkey Parser
fn monkey(s: &str) -> IResult<&str, Monkey> {
    // Monkey 1:
    let (s, n) = delimited(tag("Monkey "), nom_u32, tag(":"))(s)?;
    //  Starting items: 79, 98
    let (s, items) = preceded(
        tag("\n  Starting items: "),
        separated_list1(tag(", "), nom_u64),
    )(s)?;
    //  Operation: new = old * 19
    fn operator(i: &str) -> IResult<&str, Operator> {
        let add = nom_map(tag(" + "), |_| Operator::ADD);
        let mult = nom_map(tag(" * "), |_| Operator::MULT);
        alt((add, mult))(i)
    }
    fn operand(i: &str) -> IResult<&str, Operand> {
        let var = nom_map(tag("old"), |v: &str| Operand::VAR(v.to_string()));
        let int = nom_map(nom_u64, Operand::INT);
        alt((var, int))(i)
    }
    let (s, (a, op, b)) = preceded(
        tag("\n  Operation: new = "),
        tuple((operand, operator, operand)),
    )(s)?;
    let operation = Operation { a, op, b };
    //  Test: divisible by 23
    let (s, divisible_by) = preceded(tag("\n  Test: divisible by "), nom_u64)(s)?;
    //  If true: throw to monkey 2
    let (s, if_true) = preceded(tag("\n    If true: throw to monkey "), nom_u32)(s)?;
    //  If false: throw to monkey 3
    let (s, if_false) = preceded(tag("\n    If false: throw to monkey "), nom_u32)(s)?;

    Ok((
        s,
        Monkey {
            n,
            items,
            operation,
            divisible_by,
            if_true,
            if_false,
            inspections: 0,
        },
    ))
}

fn monkey_business(
    mut monkeys: Vec<Monkey>,
    rounds: u32,
    divisor: Option<u64>,
    modulus: Option<u64>,
) -> u64 {
    for _ in 0..rounds {
        for i in 0..monkeys.len() {
            let monkey = monkeys.get_mut(i).unwrap();
            monkey.inspections += monkey.items.len() as u64;
            let thrown = monkey.take_turn(&monkey.items, divisor, modulus);
            monkey.items.clear();
            for (m, i) in thrown {
                monkeys.get_mut(m).unwrap().items.push(i);
            }
        }
    }

    let mut inspection_count: Vec<u64> = monkeys.iter().map(|m| m.inspections).collect();
    inspection_count.sort();
    inspection_count.iter().rev().take(2).product()
}

fn part1(input: &str) -> u64 {
    let monkeys = parse_input(input);
    monkey_business(monkeys, 20, Some(3), None)
}

fn part2(input: &str) -> u64 {
    let monkeys = parse_input(input);
    let modulus = monkeys.iter().map(|m| m.divisible_by).product();
    monkey_business(monkeys, 10000, None, Some(modulus))
}

#[cfg(test)]
mod test {
    use crate::day11::{parse_input, part1, part2, PUZZLE};

    #[test]
    fn parser_test() {
        let monkeys = parse_input(SAMPLE_INPUT);
        assert_eq!(monkeys.len(), 4);
        assert_eq!(monkeys[0].n, 0);
        assert_eq!(monkeys[0].items, vec![79, 98]);
    }

    #[test]
    fn monkey_inspection_test() {
        let monkey = &parse_input(SAMPLE_INPUT)[0];
        let worry = monkey.inspect(monkey.items[0]);
        assert_eq!(worry, 1501);
    }

    #[test]
    fn monkey_throw_test() {
        let monkey = &parse_input(SAMPLE_INPUT)[0];
        let throw_to = monkey.throw(500);
        assert_eq!(throw_to, 3);
    }

    #[test]
    fn monkey_take_turn_test() {
        let monkey = &parse_input(SAMPLE_INPUT)[0];
        let thrown = monkey.take_turn(&monkey.items, Some(3), None);
        assert_eq!(thrown, vec![(3, 500), (3, 620)]);
    }

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 10605)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 2713310158)
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "Monkey 0:
  Starting items: 79, 98
  Operation: new = old * 19
  Test: divisible by 23
    If true: throw to monkey 2
    If false: throw to monkey 3

Monkey 1:
  Starting items: 54, 65, 75, 74
  Operation: new = old + 6
  Test: divisible by 19
    If true: throw to monkey 2
    If false: throw to monkey 0

Monkey 2:
  Starting items: 79, 60, 97
  Operation: new = old * old
  Test: divisible by 13
    If true: throw to monkey 1
    If false: throw to monkey 3

Monkey 3:
  Starting items: 74
  Operation: new = old + 3
  Test: divisible by 17
    If true: throw to monkey 0
    If false: throw to monkey 1
";
}
