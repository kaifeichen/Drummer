import struct
import argparse
import numpy
import os
import time
import matplotlib.pyplot as plt
from scipy import signal
from subprocess import call
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
import origin_signal


chirp_echo_duration = 0.030 # in second


def read_data(fname):
    f = open(fname, 'rb')
    data = f.read()
    amp = numpy.array(struct.unpack("<" + "h" * (len(data) / 2), data))
    return amp


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument('-f', '--fname', help="input file")
    ap.add_argument('-s', '--startfreq', type=int, help="low frequency")
    ap.add_argument('-e', '--endfreq', type=int, help="high frequency")
    ap.add_argument('-p', '--startphase', type=int, help="start phase")
    ap.add_argument('-d', '--duration', type=float, help="duration")
    ap.add_argument('-r', '--ratio', type=float, help="amplitude ratio")
    ap.add_argument('-c', '--ampcomp', type=float, help="amplitude compensation")

    args = ap.parse_args()
    fname = args.fname
    start_freq = args.startfreq
    end_freq = args.endfreq
    start_phase = args.startphase
    duration = args.duration
    amp_ratio = args.ratio
    amp_comp = args.ampcomp
    sample_rate = 44100
    max_amp = 0x7FFF # 32767

    #read
    amps = read_data(args.fname)

    #origin signal
    signal_func = lambda t: origin_signal.get_sample(t, duration, start_freq, end_freq, start_phase, amp_ratio*max_amp, None, None, origin_signal.log_chirp)
    chirp_amps = numpy.array([signal_func(t) for t in numpy.arange(0, float(duration), step=1.0/sample_rate)])
    reversed_chirp_amps = chirp_amps[::-1]
    
    ir_amps = numpy.convolve(amps, reversed_chirp_amps, 'valid')

    plt.plot(ir_amps)
    plt.show()
