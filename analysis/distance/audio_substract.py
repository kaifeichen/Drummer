import csv
import math
import numpy
import scipy as sp
import struct
import creat_signal
import matplotlib.pyplot as plt
from scipy import optimize
import scipy
TIME = 1.0 / 44100


def substract(data, sample_rate, signal_func):
    start_idx, stop_idx = find_head(data, sample_rate)
    head = data[start_idx:stop_idx]

    origin_start_time = continuous_match(head, sample_rate, signal_func)
    # origin_data = numpy.array([signal_func(t) for t in numpy.arange(origin_start_time, origin_start_time+float(len(head))/sample_rate, step=1.0/sample_rate)])
    

    chirp_sample_num = signal_func.duration*sample_rate
    received_chirp = data[start_idx:start_idx+chirp_sample_num]
    origin_chirp = numpy.array([signal_func(t) for t in numpy.arange(origin_start_time, origin_start_time+float(chirp_sample_num)/sample_rate, step=1.0/sample_rate)])
    amp_ratio = float(max(received_chirp*received_chirp))/max(received_chirp*origin_chirp)
    origin_chirp = amp_ratio*origin_chirp
    
    # plt.plot(received_chirp, 'r', alpha = 0.2)
    # plt.plot(origin_chirp, 'b', alpha = 0.2)
    plt.plot(received_chirp-origin_chirp, 'r')
    plt.show()


    # results_amp = []
    # counter = 0
    
    # amp = max(results_amp)
    # index_rec = list(data).index(amp)
    # points = creat_signal.signal(0.0, amp)
    # index_ori = points.index(max(points))
    # index_start = index_rec - index_ori
    # print index_start
    # print index_rec

    # results = []
    # for k in range(int(length / TIME)):
    #     results.append(data[index_start + k])

    # value = float("inf")
    # starttime = 0
    # final_points = []
    # for j in range(640):
    #     start = j * TIME / 80.0
    #     origin = []
    #     error = 0
    #     points = creat_signal.signal(start, amp)
    #     for i in range(20):
    #         error += abs(results[i] - points[i])
    #     if (error < value):
    #         starttime = start
    #         value = error
    #         final_points = points

    # num = 0
    # plot1 = []
    # now_array = []
    # past_array = []
    # while (num < len(data)):
    #     if (num >= index_start and num < index_start + len(results)):
    #         now = data[num]
    #         past = final_points[num-index_start]
    #         plot1.append(now - past)
    #         past_array.append(past)
    #         now_array.append(now)
    #     else:
    #         plot1.append(data[num])
    #     num += 1
    # return np.array(now_array) - np.array(past_array)

def continuous_match(data, sample_rate, signal_func):
    """
    match singal in several iterations to pick most similar one.
    it is just more fine grained, not rally continuous.
    return: start time in signal_func to substract.
    """
    length = len(data)
    min_diff = float('Inf')
    min_time = 0

    # TODO give more names/comments to these confusing variables
    scan_period_num = 1.0
    scan_start_time_span = scan_period_num/signal_func.start_freq
    scan_start_time_slice_num = 100
    scan_start_time_itvl = 1.0/signal_func.start_freq/scan_start_time_slice_num
    scan_start_times = numpy.arange(scan_start_time_span, step=scan_start_time_itvl)
    scan_samples_period_num = 3.0
    for start_time in scan_start_times:
        scan_times = numpy.arange(start_time, start_time+scan_samples_period_num*float(length)/sample_rate, step=1.0/sample_rate)
        origin_data = numpy.array([signal_func(t) for t in scan_times])
        current_min_diff, start_idx = descrete_match(data, origin_data)
        if min_diff > current_min_diff:
            min_diff = current_min_diff
            min_time = start_time + float(start_idx)/sample_rate

    return min_time


def descrete_match(data, origin_data):
    """
    match data with origin_data.
    length of origin_data should be larger than data.
    return: max_val, start index of origin data.
    """
    min_diff = float('Inf')
    for origin_idx in range(len(origin_data) - len(data)):
        diff = numpy.sum(numpy.abs(data-origin_data[origin_idx:origin_idx+len(data)]))
        if min_diff > diff:
            min_diff = diff
            start_idx = origin_idx
    return (min_diff, start_idx)


def find_head(data, sample_rate):
    """
    find head of signal to be subtracted.

    duration: in second.

    return: start index, stop index.
    """
    hilbert = numpy.abs(scipy.signal.hilbert(data))

 
    trial_num = 50 # number of trials before decision
    sig_small_amp = 100 # significant small amplitude to believe it is quiet
    sig_large_amp = 2000 # significant large amplitude to believe it is sound
    min_num = 10 # min number of points to return
    max_num = 40 # max number of points to return

    # search forward for uphill
    stop_idx = None
    trail_cnt = 0
    last_val = 0
    for idx in range(len(hilbert)):
        current_val = hilbert[idx]
        if current_val > sig_large_amp:
            # avoid small dip
            if current_val >= last_val:
                trail_cnt += 1
            else:
                trail_cnt -= 1
        else:
            trail_cnt = 0
        if trail_cnt == trial_num:
            break
        last_val = current_val
    stop_idx = idx - trail_cnt

    # search backward for quiet valley
    start_idx = None
    quiet = False
    last_val = float('Inf')
    for idx in reversed(range(stop_idx)):
        current_val = hilbert[idx]
        if current_val < sig_small_amp:
            quiet = True
        if quiet == True and current_val > last_val:
            break
        last_val = current_val
    start_idx = idx

    stop_idx = (start_idx + min_num) if (stop_idx < start_idx + min_num) else stop_idx
    stop_idx = (start_idx + max_num) if (stop_idx > start_idx + max_num) else stop_idx

    # plt.plot(hilbert[start_idx:stop_idx])
    # plt.plot(data[start_idx:stop_idx])
    # plt.show()

    return (start_idx, stop_idx)


