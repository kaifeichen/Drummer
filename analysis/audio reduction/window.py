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


# Gaussian window
# http://en.wikipedia.org/wiki/Window_function#Gaussian_window
def gaussian_window(n, N, std=0.4):
    val = math.exp(-1.0/2*math.pow((n - (N-1)/2)/(std*(N-1)/2), 2))
    return val


# kaiser window will be hard to calculate, becaue bessel is hard
# def kaiser_window(n, N, alpha=3):
#     # zero-th order modified Bessel function of the first kind
#     # 
#     def bessel():
#         # gamma function
#         # http://en.wikipedia.org/wiki/Gamma_function
#         for m in range():

#         math.gamma(x)
#     val = bessel(math.pi * alpha * math.sqrt(1 - math.pow((2*n/(N-1) - 1), 2))) / bessel(math.pi*alpha)
#     return val