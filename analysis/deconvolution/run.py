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
import bpfilter
import peakdetect
from numpy import array


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
    ap.add_argument('-d', '--duration', type=float, help="duration")
    ap.add_argument('-r', '--ratio', type=float, help="amplitude ratio")

    args = ap.parse_args()
    fname = args.fname
    start_freq = args.startfreq
    end_freq = args.endfreq
    duration = args.duration
    amp_ratio = args.ratio
    sample_rate = 44100
    max_amp = 0x7FFF # 32767

    #read
    amps = read_data(args.fname)

#    #filter: http://wiki.scipy.org/Cookbook/FIRFilter 
#    # The Nyquist rate of the signal.
#    #nyq_rate = sample_rate / 2.0
#    # The desired width of the transition from pass to stop,
#    # relative to the Nyquist rate.  We'll design the filter
#    # with a 5 Hz transition width.
#    width = 5.0/nyq_rate
#    # The desired attenuation in the stop band, in dB.
#    ripple_db = 60.0
#    # Compute the order and Kaiser parameter for the FIR filter.
#    N, beta = signal.kaiserord(ripple_db, width)
#    # The cutoff frequency of the filter.
#    cutoff_hz = end_freq + 1000
#    # Use firwin with a Kaiser window to create a lowpass FIR filter.
#    taps = signal.firwin(N, cutoff_hz/nyq_rate, window=('kaiser', beta))
#    # Use lfilter to filter x with the FIR filter.
#    filtered_amps = signal.lfilter(taps, 1.0, amps)

    #origin signal
    start_phase = 0
    signal_func = lambda t: origin_signal.get_sample(t, duration, start_freq, end_freq, start_phase, amp_ratio*max_amp, None, origin_signal.log_chirp)
    chirp_amps = numpy.array([signal_func(t) for t in numpy.arange(0, float(duration), step=1.0/sample_rate)])
    reversed_chirp_amps = chirp_amps[::-1]
    
    # deconvolution
    ir_amps = numpy.convolve(amps, reversed_chirp_amps, 'full')

    # envelop
    hilbert_amps = numpy.abs(signal.hilbert(ir_amps))

    # find peaks
    maxtab, _ = peakdetect.peakdet(hilbert_amps, delta=5e8)
    maxidx = numpy.argmax(maxtab[:,1])
    maxtab = maxtab[maxidx:,:]

    hilbert_amps = hilbert_amps[maxtab[0,0]:]
    maxtab[:,0] = maxtab[:,0] - maxtab[0,0]
    distances = maxtab[1:,0] / 44100 * 340 /2
    print distances
    print maxtab[1:,1] / 1e9

    # theoretical signal shape
    theo_ir_amps = numpy.convolve(chirp_amps, reversed_chirp_amps, 'full')
    theo_hilbert_amps = numpy.abs(signal.hilbert(theo_ir_amps))
    theo_maxtab, _ = peakdetect.peakdet(theo_hilbert_amps, delta=1e9)
    theo_maxidx = numpy.argmax(theo_maxtab[:,1])
    theo_maxtab = theo_maxtab[theo_maxidx:,:]
    theo_hilbert_amps = theo_hilbert_amps[theo_maxtab[0,0]:]
    theo_hilbert_amps = hilbert_amps[0]/theo_hilbert_amps[0]*theo_hilbert_amps


    plt.scatter(array(maxtab)[:,0], array(maxtab)[:,1], color='red')
    plt.plot(hilbert_amps)
    plt.plot(theo_hilbert_amps, 'r', alpha=0.3)
    plt.show()
