import csv
import math
import numpy as np
import scipy as sp
import struct
import creat_signal
import matplotlib.pyplot as plt
from scipy import optimize
TIME = 1.0 / 44100


def substract(data, signal_func):
    results_amp = []
    counter = 0
    for elem in data:
        if elem > 10000:
            for i in range(int(length / TIME)):
                results_amp.append(data[counter + i])
            break
        counter += 1
    amp = max(results_amp)
    index_rec = list(data).index(amp)
    points = creat_signal.signal(0.0, amp)
    index_ori = points.index(max(points))
    index_start = index_rec - index_ori
    print index_start
    print index_rec

    results = []
    for k in range(int(length / TIME)):
        results.append(data[index_start + k])

    value = float("inf")
    starttime = 0
    final_points = []
    for j in range(640):
        start = j * TIME / 80.0
        origin = []
        error = 0
        points = creat_signal.signal(start, amp)
        for i in range(20):
            error += abs(results[i] - points[i])
        if (error < value):
            starttime = start
            value = error
            final_points = points

    num = 0
    plot1 = []
    now_array = []
    past_array = []
    while (num < len(data)):
        if (num >= index_start and num < index_start + len(results)):
            now = data[num]
            past = final_points[num-index_start]
            plot1.append(now - past)
            past_array.append(past)
            now_array.append(now)
        else:
            plot1.append(data[num])
        num += 1
    return np.array(now_array) - np.array(past_array)
