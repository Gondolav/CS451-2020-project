import sys

def all_unique(x):
  seen = set()
  return not any(i in seen or seen.add(i) for i in x)

with open(sys.argv[1]) as file:
  lines = file.readlines()
  lines = [l.split() for l in lines]
  lines_deliver = [(l[1], l[2]) for l in lines if l[0] == 'd']
  lines_broadcast = [l[1] for l in lines if l[0] == 'b']

  print(f"No duplicates in broadcast : {all_unique(lines_broadcast)}")
  print(f"No duplicates in delivered : {all_unique(lines_deliver)}")

  print(lines_deliver)