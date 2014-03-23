import struct
import sys
import bpfilter
import math
import argparse
import numpy as np
import matplotlib.pyplot as plt
import audio_reduction

from scipy import signal


def find_echo(fname, duration):
    """
    fname: str
    duration: float in ms
    """
    f = open(fname, 'rb')
    data = f.read()

    amp = struct.unpack("<" + "h" * (len(data) / 2), data)
    hilbert = np.abs(signal.hilbert(amp))
    result = [1 if x > 6700 else 0 for x in hilbert]

    return (amp, hilbert, result)


def plot(fname):
    (amp, hilbert, result) = find_echo(fname, 0.2)

    plt.plot(amp, 'b', alpha=0.2)
    plt.plot(hilbert, 'r', alpha=0.3)
    plt.show()

def find_distance(reduced_amp, ratio=0.1):
    length = len(reduced_amp)
    threshold = max(reduced_amp) * ratio
    counter=0
    for i in range(length):
        if (abs(reduced_amp[i]) > threshold):
            counter += 1
        if counter > 6:
            return i - 4

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
        amp, lowcut, highcut, fs, order=6)


    plt.plot(filtered_amp, 'r', alpha=0.3)
    

    reduced_amp = audio_reduction.reduction(
        filtered_amp, lowfreq, highfreq, duration)

    plt.plot(reduced_amp, 'r', alpha=0.3)
    plt.show()

    time = float(find_distance(reduced_amp)) / 44100 * 340 / 2 
    print time

    # plt.plot(reduced_amp, 'r', alpha=0.3)
    # plt.savefig(args.png)