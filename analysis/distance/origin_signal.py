import numpy
import math
import window


# get one value at any time from windowed linear chirp
def get_sample(time, duration, start_freq, stop_freq, start_phase, sample_rate, max_amp, win_func):
    k = linear_chirp_rate(stop_freq-start_freq, duration)
    chirp_val = max_amp * linear_chirp(time, k, start_freq, start_phase)
    N = 1e10 # make it large enough to get enough accuracy
    n = time/duration*N
    window_val = win_func(n, N)
    return chirp_val*window_val


# frequency change rate k given start and stop frequency
def linear_chirp_rate(freq_span, time_span):
    return freq_span/time_span


# Linear chirp: x(t) = sin(phase0 + 2*pi*(f0*t + k/2*t^2))
def linear_chirp(time, k, start_freq, start_phase):
    phase = start_phase + 2. * math.pi * (start_freq * time + k / 2 * time * time)
    return math.sin(phase)