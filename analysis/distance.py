import struct
import matplotlib.pyplot as plt
import numpy as np
from scipy import signal
import sys
import bpfilter
import audio_reduction as reduction
import math



def find_echo(fname, duration):
    """
    fname: str
    duration: float in ms
    """
    f = open(fname, 'rb')
    data = f.read()

    amp = struct.unpack("<"+"h"*(len(data)/2), data)
    hilbert = np.abs(signal.hilbert(amp))
    result = [1 if x > 6700 else 0 for x in hilbert]

    return (amp, hilbert, result)


def plot(fname):
    (amp, hilbert, result) = find_echo(fname, 0.2)

    plt.plot(amp, 'b', alpha=0.2)
    plt.plot(hilbert, 'r', alpha=0.3)
    plt.show()


def chirp_func(t, a, start_fr, end_fr, length):
    k = math.pow(float(end_fr)/start_fr, 1.0/length)
    den = math.log(k)
    num = math.pow(k,t)-1
    rest = math.pi*2*start_fr
    result = math.sin(1.0*num*rest/den)
    return a*result


if __name__ ==  "__main__":
    fname = sys.argv[1]
    lowfreq = int(sys.argv[2])
    highfreq = int(sys.argv[3])
    duration = float(sys.argv[4]) # in second

    f = open(fname, 'rb')
    data = f.read()
    amp = np.array(struct.unpack("<"+"h"*(len(data)/2), data))
    lowcut = lowfreq - 1000
    highcut = highfreq + 1000
    fs = 44100

    filtered_amp = bpfilter.butter_bandpass_filter(amp, lowcut, highcut, fs, order=6)
    origin_chirp_func = lambda t: chirp_func(t, max(abs(filtered_amp)), lowfreq, highfreq, duration)
    origin_chirp_func.duration = duration
    reduced_amp = reduction.reduce(filtered_amp, origin_chirp_func)

   # plt.plot(filtered_amp, 'b', alpha=0.2)
    plt.plot(reduced_amp, 'r', alpha=0.3)
    # print len(filtered_amp)
    # print len(reduced_amp)
    plt.show()
