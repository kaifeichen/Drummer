import struct
import matplotlib.pyplot as plt
import numpy as np
from scipy import signal


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


if __name__ ==  "__main__":
    fname = "1wall_0d_10ft_10ms.2.dat"
    plot(fname)
