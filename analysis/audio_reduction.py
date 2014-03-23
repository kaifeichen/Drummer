import csv
import math
import numpy as np
import scipy as sp
import struct
import matplotlib.pyplot as plt
from scipy import optimize
TIME = 1.0 / 44100


def reduction(data, startf=5000.0, endf=5100.0, length=3.0):
    results = []
    counter = 0

    for elem in data:
        if elem > 10000:
            for i in range(int(length / TIME)):
                results.append(data[counter + i])
            break
        counter += 1
    amp = max(results[:30])
    value = float("inf")
    starttime = 0
    for j in range(640):
        start = j * TIME / 80.0
        origin = []
        error = 0
        for i in range(30):
            error += abs(results[i] -
                         chirp_func(start + i * TIME, amp, startf, endf, length))
        if (error < value):
            starttime = start
            value = error

    num = 0
    plot1 = []
    now_array = []
    past_array = []
    while (num < len(data)):
        if (num >= counter and num < counter + len(results)):
            now = data[num]
            past = chirp_func(starttime + TIME * (num - counter),
                            amp, startf, endf, length)
            plot1.append(now - past)
            past_array.append(past)
            now_array.append(now)
        else:
            plot1.append(data[num])
        num += 1
    return np.array(now_array) - np.array(past_array)


def chirp_func(t, a, start_fr, end_fr, length):
    k = math.pow(float(end_fr) / start_fr, 1.0 / length)
    den = math.log(k)
    num = math.pow(k, t) - 1
    rest = math.pi * 2 * start_fr
    result = math.sin(1.0 * num * rest / den)
    return a * result