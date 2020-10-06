import sys
from collections import Counter

with open(sys.argv[1]) as file:
  lines = file.readlines()
  lines = [l.split() for l in lines]
  lines = [l[2] if l[0] == 'd' else l[1] for l in lines]

  c = Counter(lines)
  print(c)
  print(all(c.values == 1))