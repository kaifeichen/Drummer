import math


# Blackman-Harris window
# http://en.wikipedia.org/wiki/Window_function#Blackman.E2.80.93Harris_window
def bh_window(n, N):
    a0 = 0.35875
    a1 = 0.48829
    a2 = 0.14128
    a3 = 0.01168
    val = a0 - \
        a1 * math.cos(2 * math.pi * n / (N - 1)) + \
        a2 * math.cos(4 * math.pi * n / (N - 1)) - \
        a3 * math.cos(6 * math.pi * n / (N - 1))
    return val


# Hamming window
# http://en.wikipedia.org/wiki/Window_function#Hamming_window
def hamming_window(n, N):
    a0 = 0.54
    a1 = 0.46
    val = a0 - a1 * math.cos(2 * math.pi * n / (N - 1))
    return val


# Cosine window
# http://en.wikipedia.org/wiki/Window_function#Cosine_window
def cosine_window(n, N):
    val = math.sin(math.pi * n / (N - 1))
    return val