#!/usr/bin/env python

import sys

def reverse_captcha(digits):
    last = None
    total = 0
    for d in digits + digits[0]:
        if d == last:
            total += int(d)
        last = d
    return total

def reverse_captcha2(digits):
    midpoint = len(digits) // 2
    total = 0
    for i in range(midpoint):
        if digits[i] == digits[midpoint + i]:
            total += 2 * int(digits[i])
    return total

if __name__ == '__main__':
    print(reverse_captcha(sys.argv[1]))
    print(reverse_captcha2(sys.argv[1]))
