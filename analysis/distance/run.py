import struct
import sys
import bpfilter
import math
import argparse
import numpy
import matplotlib.pyplot as plt
import audio_substract
import origin_signal
import scipy
import window


# def find_echo(fname, duration):
#     """
#     fname: str
#     duration: float in ms
#     """
#     f = open(fname, 'rb')
#     data = f.read()

#     amp = struct.unpack("<" + "h" * (len(data) / 2), data)
#     hilbert = np.abs(scipy.signal.hilbert(amp))
#     result = [1 if x > 6700 else 0 for x in hilbert]

#     return (amp, hilbert, result)

# def plot(fname):
#     (amp, hilbert, result) = find_echo(fname, 0.2)

#     plt.plot(amp, 'b', alpha=0.2)
#     plt.plot(hilbert, 'r', alpha=0.3)
#     plt.show()

# def find_distance(reduced_amp, ratio=0.1):
#     length = len(reduced_amp)
#     threshold = max(reduced_amp) * ratio
#     counter=0
#     for i in range(length):
#         if (abs(reduced_amp[i]) > threshold):
#             counter += 1
#         if counter > 6:
#             return i - 4

def read_data(fname):
    f = open(fname, 'rb')
    data = f.read()
    amps = numpy.array(struct.unpack("<" + "h" * (len(data) / 2), data))
    return amps

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument('-f', '--fname', help="input file")
    ap.add_argument('-s', '--startfreq', type=int, help="low frequency")
    ap.add_argument('-e', '--endfreq', type=int, help="high frequency")
    ap.add_argument('-p', '--startphase', type=int, help="start phase")
    ap.add_argument('-d', '--duration', type=float, help="duration")
    ap.add_argument('-r', '--ratio', type=float, help="amplitude ratio")

    args = ap.parse_args()
    fname = args.fname
    start_freq = args.startfreq
    end_freq = args.endfreq
    start_phase = args.startphase
    duration = args.duration
    amp_ratio = args.ratio
    sample_rate = 44100
    max_amp = 0x7FFF
    filter_width = 500

    amps = read_data(fname)

    low_cut = start_freq - filter_width
    high_cut = end_freq + filter_width
    filtered_amps = bpfilter.butter_bandpass_filter(amps, low_cut, high_cut, sample_rate, order=9)

    # plt.plot(filtered_amps)
    # plt.show()

    signal_func = lambda t: origin_signal.get_sample(t, duration, start_freq, end_freq, start_phase, sample_rate, amp_ratio*max_amp, window.cosine_window)

    tmp = []
    for i in range(int(0.05*44100)):
        tmp.append(signal_func(float(i)/44100))
    plt.plot(tmp)
    plt.show()

    substracted_amps = audio_substract.substract(filterd_amps, signal_func)

    time = float(find_distance(reduced_amps)) / 44100 * 340 / 2 
    print time

    plt.plot(reduced_amps, 'r', alpha=0.3)
    plt.show()