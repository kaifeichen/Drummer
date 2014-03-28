import struct
import sys
import bpfilter
import math
import argparse
import numpy as np
import matplotlib.pyplot as plt
from scipy import signal



if __name__ == "__main__":

    ap = argparse.ArgumentParser()
    ap.add_argument('-f', '--fname', help="input file")
    ap.add_argument('-p', '--png', help="output")
    ap.add_argument('-s', '--startfreq', type=int, help="low frequency")
    ap.add_argument('-e', '--endfreq', type=int, help="high frequency")
    ap.add_argument('-d', '--duration', type=float, help="duration")

    args = ap.parse_args()

    lowfreq = args.startfreq
    highfreq = args.endfreq
    duration = args.duration

    f = open(args.fname, 'rb')
    data = f.read()
    amp = np.array(struct.unpack("<" + "h" * (len(data) / 2), data))

    lowcut = lowfreq - 1000
    highcut = highfreq + 1000
    fs = 44100

    filtered_amp = bpfilter.butter_bandpass_filter(
        amp, lowcut, highcut, fs, order=9)


    plt.plot(filtered_amp, 'b', alpha=0.3)
    envelop_amp = abs(signal.hilbert(filtered_amp))
    plt.plot(envelop_amp, 'r', alpha=0.3)
    plt.show()