import numpy
import math


# get one value at any time from windowed linear chirp
def get_sample(time, duration, start_freq, stop_freq, start_phase, max_amp, win_func, chirp_func):
    chirp_val = max_amp * chirp_func(time, start_freq, start_phase, stop_freq, duration)
    val = chirp_val
    if win_func:
        N = 1e10 # make it large enough to get enough accuracy
        n = time/duration*N
        window_val = win_func(n, N)
        val = chirp_val*window_val
    return val


# Linear chirp: x(t) = sin(phase0 + 2*pi*(f0*t + k/2*t^2))
def linear_chirp(time, start_freq, start_phase, stop_freq, time_span):
    k = (stop_freq - start_freq) /time_span
    phase = start_phase + 2. * math.pi * (start_freq * time + k / 2 * time * time)
    return math.sin(phase)


# logarithmic chirp: x(t) = sin(phase0 + 2*pi*f0*T/ln(f1/f0)*(e^(t/T*ln(f1/f0)) - 1))
def log_chirp(time, start_freq, start_phase, stop_freq, time_span):
    phase = start_phase \
            + 2. * math.pi * start_freq * time_span / math.log(float(stop_freq)/start_freq) \
            * (math.exp(float(time) / time_span * math.log(float(stop_freq)/start_freq)) - 1)
    return math.sin(phase)


