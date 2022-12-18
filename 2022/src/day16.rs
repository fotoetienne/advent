use fxhash::FxHashMap;
use itertools::Itertools;
use nom::bytes::complete::tag;
use nom::character::complete::alpha1;
use nom::character::complete::i32 as parse_i32;
use nom::combinator::map as nom_map;
use nom::error::ErrorKind;
use nom::multi::separated_list1;
use nom::sequence::preceded;
use nom::IResult;
use nom_regex::str::re_find;
use regex::Regex;
use std::cell::RefCell;
use std::collections::HashMap;
use std::hash::Hash;

use crate::puzzle::{Puzzle, PuzzleFn::I32};

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 16,
    part1: I32(part1),
    part2: I32(part2),
};

#[derive(Clone, Debug)]
struct Valve {
    id: String,
    flow_rate: i32,
    connections: Vec<String>,
}

#[derive(Clone, Debug, PartialEq, Eq, Hash)]
struct State {
    location: usize,
    open_valves: Vec<usize>,
    eventual_pressure: i32,
    time_remaining: i32,
}

type Matrix<T> = Vec<Vec<T>>;

fn build_cave(input: &str) -> Cave {
    let valves = parse_input(input);
    // Valves with 0 flow are not considered as nodes in the graph, but only contribute to the path
    // distance between functioning valves
    let flowable_valves: Vec<usize> = valves
        .iter()
        .enumerate()
        .filter(|(_, v)| v.flow_rate != 0)
        .map(|(i, _)| i)
        .collect();
    let edges = edges(&valves);
    // Build a graph adjacency matrix with the minimum distance from
    // each valve to each of the other valves.
    // each functioning valve will be an index in the matrix rather than a string
    let adj = min_dists(edges, valves.len());
    Cave {
        valves,
        flowable_valves,
        adj,
    }
}

// Convert
fn edges(valves: &[Valve]) -> Vec<(usize, usize)> {
    let idx: HashMap<String, usize> = valves
        .iter()
        .enumerate()
        .map(|(i, v)| (v.id.clone(), i))
        .collect();
    valves
        .iter()
        .flat_map(|v| v.connections.iter().map(|c| (idx[&v.id], idx[c])))
        .collect()
}

// Use the Floyd-Warshall algorithm to find min-dist between all Nodes
fn min_dists(edges: Vec<(usize, usize)>, n_nodes: usize) -> Matrix<i32> {
    let mut dist: Matrix<i32> = vec![vec![i32::MAX / 2; n_nodes]; n_nodes];

    for (u, v) in edges {
        dist[u][v] = 1; // All of the direct connections have distance 1
    }

    for v in 0..n_nodes {
        dist[v][v] = 0; // Distnace from a node to itself is obviously zero
    }

    for k in 0..n_nodes {
        for i in 0..n_nodes {
            for j in 0..n_nodes {
                if dist[i][j] > dist[i][k] + dist[k][j] {
                    dist[i][j] = dist[i][k] + dist[k][j];
                }
            }
        }
    }

    dist
}

struct Cave {
    valves: Vec<Valve>,
    flowable_valves: Vec<usize>,
    adj: Matrix<i32>,
}

impl Cave {
    fn travel_time(&self, from: usize, to: usize) -> i32 {
        self.adj[from][to]
    }
    fn openable_valves(&self, state: &State) -> Vec<&usize> {
        self.flowable_valves
            .iter()
            .filter(|i| !state.open_valves.contains(i))
            .collect()
    }
}

fn part1(input: &str) -> i32 {
    let cave = build_cave(input);
    let initial_state = State {
        location: 0,
        open_valves: vec![],
        eventual_pressure: 0,
        time_remaining: 30,
    };
    max_pressure(&cave, initial_state)
}

// thread_local! {
//     static PRESSURES: RefCell<FxHashMap<State, i32>> = RefCell::new(FxHashMap::default());
// }

fn max_pressure(cave: &Cave, state: State) -> i32 {
    if state.time_remaining == 0 {
        state.eventual_pressure
    // } else if let Some(cached) = PRESSURES.with(|p| p.borrow().get(&state).cloned()) {
    //     println!("Cache hit! {:?}", cached);
    //     cached
    } else {
        let max_pressure = cave
            .openable_valves(&state)
            .iter()
            .map(|v| {
                let minutes_required = cave.travel_time(state.location, **v) + 1;
                let time_remaining = state.time_remaining - minutes_required;
                let new_state = if time_remaining >= 0 {
                    let mut open_valves = state.open_valves.clone();
                    open_valves.push(**v);
                    let eventual_pressure =
                        state.eventual_pressure + cave.valves[**v].flow_rate * time_remaining;
                    State {
                        location: **v,
                        open_valves,
                        eventual_pressure,
                        time_remaining,
                    }
                } else {
                    State {
                        location: state.location,
                        open_valves: state.open_valves.clone(),
                        eventual_pressure: state.eventual_pressure,
                        time_remaining: 0,
                    }
                };
                max_pressure(cave, new_state)
            })
            .max()
            .unwrap_or(state.eventual_pressure);
        // PRESSURES.with(|p| p.borrow_mut().insert(state, max_pressure));
        max_pressure
    }
}

// 2622 is too high
fn part2(input: &str) -> i32 {
    let cave = build_cave(input);
    let valves_sets: Vec<(Vec<usize>, Vec<usize>)> = cave
        .flowable_valves
        .iter()
        .combinations(cave.flowable_valves.len() / 2)
        .map(|my_valves| {
            let elephant_valves: Vec<usize> = cave
                .flowable_valves
                .iter()
                .filter(|v| !my_valves.contains(v))
                .copied()
                .collect();
            (my_valves.iter().map(|&v| *v).collect(), elephant_valves)
        })
        .collect();
    dbg!(&valves_sets.len());
    valves_sets
        .iter()
        .map(|(my_valves, elephant_valves)| {
            let me = max_pressure(
                &cave,
                State {
                    location: 0,
                    open_valves: elephant_valves.clone(),
                    eventual_pressure: 0,
                    time_remaining: 26,
                },
            );
            let elephant = max_pressure(
                &cave,
                State {
                    location: 0,
                    open_valves: my_valves.clone(),
                    eventual_pressure: 0,
                    time_remaining: 26,
                },
            );
            me + elephant
        })
        .max()
        .unwrap()
}

fn parse_input(input: &str) -> Vec<Valve> {
    input
        .lines()
        .map(|l| parse_valve(l).unwrap().1)
        .sorted_by(|a, b| a.id.cmp(&b.id))
        .collect()
}

fn parse_valve(i: &str) -> IResult<&str, Valve> {
    // Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
    let (i, id) = preceded(tag("Valve "), nom_map(alpha1, str::to_string))(i)?;
    let (i, flow_rate) = preceded(tag(" has flow rate="), parse_i32)(i)?;
    let (i, _) =
        re_find::<(&str, ErrorKind)>(Regex::new(r"; tunnels? leads? to valves? ").unwrap())(i)
            .unwrap();
    let (i, connections) = separated_list1(tag(", "), nom_map(alpha1, str::to_string))(i)?;
    Ok((
        i,
        Valve {
            id,
            flow_rate,
            connections,
        },
    ))
}

#[cfg(test)]
mod test {
    use crate::day16::{edges, min_dists, parse_input, parse_valve, part1, part2, PUZZLE};

    #[test]
    fn parse_valve_test() {
        let (_, v1) =
            parse_valve("Valve AA has flow rate=0; tunnels lead to valves DD, II, BB").unwrap();
        assert_eq!(v1.id, "AA");
        assert_eq!(v1.flow_rate, 0);
        assert_eq!(v1.connections, vec!["DD", "II", "BB"]);
        let (_, v1) = parse_valve("Valve HH has flow rate=22; tunnel leads to valve GG").unwrap();
        assert_eq!(v1.id, "HH");
        assert_eq!(v1.flow_rate, 22);
        assert_eq!(v1.connections, vec!["GG"]);
    }

    #[test]
    fn edges_test() {
        let valves = parse_input(SAMPLE_INPUT);
        let edges = edges(&valves);
        assert_eq!(edges.len(), 20);
        assert!(edges.contains(&(0, 1)));
        assert!(edges.contains(&(0, 3)));
        assert!(edges.contains(&(0, 8)));
    }

    #[test]
    fn min_dists_test() {
        let valves = parse_input(SAMPLE_INPUT);
        let edges = edges(&valves);
        let adj = min_dists(edges, valves.len());
        assert_eq!(adj[0], vec![0, 1, 2, 1, 2, 3, 4, 5, 1, 2]);
    }

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 1651)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 1707)
    }

    #[test]
    fn run() {
        assert!(PUZZLE.run().is_ok())
    }

    const SAMPLE_INPUT: &str = "Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
Valve BB has flow rate=13; tunnels lead to valves CC, AA
Valve CC has flow rate=2; tunnels lead to valves DD, BB
Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
Valve EE has flow rate=3; tunnels lead to valves FF, DD
Valve FF has flow rate=0; tunnels lead to valves EE, GG
Valve GG has flow rate=0; tunnels lead to valves FF, HH
Valve HH has flow rate=22; tunnel leads to valve GG
Valve II has flow rate=0; tunnels lead to valves AA, JJ
Valve JJ has flow rate=21; tunnel leads to valve II";
}
