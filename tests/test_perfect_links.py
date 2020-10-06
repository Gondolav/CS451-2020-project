import sys, os, math, signal
from time import sleep, time

# Were there errors?
were_errors = False

def soft_assert(condition, message):
    """ Print message if there was an error without exiting """
    global were_errors
    if not condition:
        print("ASSERT failed " + message)
        were_errors = True
        

# Reading hosts file
hosts = list(map(lambda x : x.split(), filter(lambda x: len(x) > 0, open('../template_java/hosts', 'r').read().split('\n'))))

print(hosts)

# Counting processes
n = len(list(hosts))
print('There are %d processes' % n)

# Reading logs
logs = {i: list(filter(lambda x : len(x) > 0, open('../template_java/output%d' % i, 'r').read().split('\n'))) for i in range(1, n + 1)}

# Printing how many log messages are in the dict
for key, value in logs.items():
    print("Process %d: %d messages" % (key, len(value)))
    logs[key] = [l[1:] for l in value]
    logs[key] = [l.split() for l in logs[key]]

print(logs)

# ### No duplication - No message is delivered (to a process) more than once
for key, value in logs.items():
    logs_pld = list(filter(lambda l: l[0] == 'd' in l, value))
    s = set([x for x in logs_pld if logs_pld.count(x) > 1])
    soft_assert(0 == len(s), "Some messages have been delived more than once : {}".format(s))

# ### No creation - No message is delivered unless it was sent
for key, value in logs.items():
    logs_pld = list(filter(lambda l: l[0] == 'd' in l, value))
    for d in logs_pld:
        soft_assert(any(l[0] == 'b' and l[2:] == d[1:] for l in logs[int(d[2])]), "Message {} is delivered while not sent ".format(d))
        # soft_assert(['pls', d[1], d[2], d[3]] in logs[int(d[2])], "Message {} is delivered while not sent ".format(d))
        
# ### Validity - If pi and pj are correct, then every message sent by pi to pj is eventually delivered by pj
for key, value in logs.items():
    logs_pls = list(filter(lambda l: l[0] == 'b' in l, value))
    for m in logs_pls:
        soft_assert(['d', d[1], d[2], d[3]] in logs[int(d[1])], "Message {} is never delivered".format(m))

# printing the last line with status
print("INCORRECT" if were_errors else "CORRECT")