import sys

def all_unique(x):
  seen = set()
  return not any(i in seen or seen.add(i) for i in x)

def no_duplication(lines):
  lines_deliver = [(l[1], l[2]) for l in lines if l[0] == 'd']
  lines_broadcast = [l[1] for l in lines if l[0] == 'b']

  print(f"No duplicates in broadcast : {all_unique(lines_broadcast)}")
  print(f"No duplicates in delivered : {all_unique(lines_deliver)}")

  seen = {}
  dupes = []

  for x in lines_deliver:
      if x not in seen:
          seen[x] = 1
      else:
          if seen[x] == 1:
              dupes.append(x)
          seen[x] += 1  

  print([(k, v) for (k, v) in seen.items() if v > 1])
  print(len(dupes))

def validity(lines1, lines2):
  lines_deliver = [l[2] for l in lines2 if l[0] == 'd' and l[1] == '1']
  lines_broadcast = [l[1] for l in lines1 if l[0] == 'b']

  print(len(set(lines_broadcast) - set(lines_deliver)))

with open(sys.argv[2]) as file1:
  with open(sys.argv[3]) as file2:
    lines1 = file1.readlines()
    lines1 = [l.split() for l in lines1]

    lines2 = file2.readlines()
    lines2 = [l.split() for l in lines2]

    print('P1')
    no_duplication(lines1)

    print('P2')
    no_duplication(lines2)

    print('P1 -> P2')
    validity(lines1, lines2)

    print('P2 -> P1')
    validity(lines2, lines1)