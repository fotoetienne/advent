DAY=$1

curl "https://adventofcode.com/2019/day/$DAY/input" -H "Cookie: $ADVENT_COOKIE" > input0$DAY.txt
