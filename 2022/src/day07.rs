use crate::puzzle::{Puzzle, PuzzleFn::I32};
use nom::branch::alt;
use nom::bytes::complete::tag;
use nom::character::complete::{alpha1, anychar};
use nom::sequence::pair;
use nom::sequence::preceded;
use nom::IResult;

pub(crate) const PUZZLE: Puzzle = Puzzle {
    day: 7,
    part1: I32(part1),
    part2: I32(part2),
};

fn cd(i: &str) -> IResult<&str, &str> {
    preceded(tag("$ cd "), alt((tag("/"), tag(".."), alpha1)))(i)
}

fn ls(i: &str) -> IResult<&str, &str> {
    tag("$ ls")(i)
}

fn dir(i: &str) -> IResult<&str, &str> {
    let (input, name) = preceded(tag("dir "), alpha1)(i)?;
    Ok((input, name))
}

fn file(i: &str) -> IResult<&str, File> {
    let (input, (size, name)) = pair(nom::character::complete::i32, anychar)(i)?;
    Ok((
        input,
        File {
            _name: name.to_string(),
            size,
        },
    ))
}

struct Directory<'a> {
    path: Vec<&'a str>,
    name: &'a str,
    // originally I tried including recursive references to directories,
    // but the borrowing got too complicated
    // Instead, we'll just record the list of directory names
    dir_names: Vec<&'a str>,
    files: Vec<File>,
}

impl<'a> Directory<'a> {
    fn add_directory(&mut self, dir: &'a str) {
        self.dir_names.push(dir);
    }

    fn add_file(&mut self, file: File) {
        self.files.push(file);
    }

    fn file_size(&self) -> i32 {
        self.files.iter().map(|f| f.size).sum()
    }

    fn size(&self, directories: &Vec<Directory>) -> i32 {
        let subdir_size: i32 = directories
            .iter()
            .filter(|d| {
                if d.path.is_empty() {
                    false
                } else {
                    let (dir_name, dir_path) = d.path.split_last().unwrap();
                    dir_name == &self.name && dir_path == &self.path
                }
            })
            .map(|d| d.size(directories))
            .sum();
        self.file_size() + subdir_size
    }
}

struct File {
    _name: String,
    size: i32,
}

fn build_directories(input: &str) -> Vec<Directory> {
    let mut path: Vec<&str> = vec!["/"];
    let mut directories: Vec<Directory> = vec![];

    let mut lines = input.lines();
    let mut line = lines.next().unwrap();

    'outer: loop {
        if let Ok((_, dir)) = cd(line) {
            if dir == ".." {
                path.pop();
            } else if dir != "/" {
                path.push(dir);
            }
            line = lines.next().unwrap();
        } else if let Ok((_, _)) = ls(line) {
            let (dir_name, dir_path) = path.split_last().unwrap();
            let mut cwd = Directory {
                path: dir_path.to_vec(),
                name: dir_name,
                dir_names: vec![],
                files: vec![],
            };
            loop {
                match lines.next() {
                    Some(ls_line) => {
                        if let Ok((_, file)) = file(ls_line) {
                            cwd.add_file(file);
                        } else if let Ok((_, dir)) = dir(ls_line) {
                            cwd.add_directory(dir);
                        } else if let Ok((_, _)) = cd(ls_line) {
                            // if not a file or directory, save the current directory and restart the main loop
                            directories.push(cwd);
                            line = ls_line;
                            break; // go back to outer loop
                        } else {
                            panic!("Can't parse {}", ls_line.to_string())
                        }
                    }
                    None => {
                        directories.push(cwd);
                        break 'outer; // No more lines so end outer loop
                    }
                }
            }
        } else {
            panic!("Can't parse {}", line.to_string())
        }
    }
    directories
}

fn part1(input: &str) -> i32 {
    let directories = build_directories(input);
    directories
        .iter()
        .map(|d| d.size(&directories))
        .filter(|d| d <= &100000)
        .sum()
}

const TOTAL_DISK_SPACE: i32 = 70000000;
const REQUIRED_DISK_SPACE: i32 = 30000000;

fn part2(input: &str) -> i32 {
    let directories = build_directories(input);
    let root = directories.iter().find(|d| d.name == "/").unwrap();
    let free_space = TOTAL_DISK_SPACE - root.size(&directories);
    let needed_space = REQUIRED_DISK_SPACE - free_space;
    // println!("needed space: {}", needed_space);
    directories
        .iter()
        .map(|d| d.size(&directories))
        .filter(|d| d >= &needed_space)
        .min()
        .unwrap()
}

#[cfg(test)]
mod test {
    use crate::day07::{part1, part2};

    #[test]
    fn part1_test() {
        let answer = part1(SAMPLE_INPUT);
        assert_eq!(answer, 95437)
    }

    #[test]
    fn part2_test() {
        let answer = part2(SAMPLE_INPUT);
        assert_eq!(answer, 24933642)
    }

    const SAMPLE_INPUT: &str = "$ cd /
$ ls
dir a
14848514 b.txt
8504156 c.dat
dir d
$ cd a
$ ls
dir e
29116 f
2557 g
62596 h.lst
$ cd e
$ ls
584 i
$ cd ..
$ cd ..
$ cd d
$ ls
4060174 j
8033020 d.log
5626152 d.ext
7214296 k";
}
