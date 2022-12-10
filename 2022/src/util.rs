use anyhow::{Error, Result};
use std::fs;

pub(crate) fn get_input(day: i32) -> Result<String> {
    let rt = tokio::runtime::Runtime::new().unwrap();
    rt.block_on(read_or_fetch_input(day))
}

const YEAR: &str = "2022";

// #[tokio::main]
async fn read_or_fetch_input(day: i32) -> Result<String> {
    let input = read_input_from_file(day);
    if input.is_ok() {
        return input;
    }
    let input = fetch_input(day).await?;
    write_input_to_file(day, &input)?;
    Ok(input)
}

fn read_input_from_file(day: i32) -> Result<String> {
    Ok(fs::read_to_string(input_filename(day))?)
}

fn write_input_to_file(day: i32, input: &str) -> Result<()> {
    Ok(fs::write(input_filename(day), input)?)
}

fn input_filename(day: i32) -> String {
    format!("inputs/input{:02}.txt", day)
}

fn input_uri(day: i32) -> String {
    format!("https://adventofcode.com/{}/day/{}/input", YEAR, day)
}

fn get_cookie() -> Result<String> {
    let cookie = fs::read_to_string("cookie.txt")?;
    Ok(match cookie.strip_suffix("\n") {
        Some(stripped) => stripped.to_string(),
        None => cookie,
    })
}

async fn fetch_input(day: i32) -> Result<String> {
    let client = reqwest::Client::new();
    let cookie = get_cookie()?;
    let result = client
        .get(input_uri(day))
        .header("Cookie", cookie)
        .send()
        .await;
    return if result.is_ok() {
        let body = result?.text().await?;
        Ok(body)
    } else {
        let err = result.err().unwrap();
        println!("{}", err);
        Err(Error::from(err))
    };
}
