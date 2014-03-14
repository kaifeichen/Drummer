import struct
import matplotlib.pyplot as plt
import numpy as np
from scipy import signal

fname = "465F_45d_2m_1m_2ms.1.dat"
f = open(fname, 'rb')
data = f.read()

amp = struct.unpack("<"+"h"*(len(data)/2), data)
hilbert = np.abs(signal.hilbert(amp))
result = [10000 if x > 6700 else 0 for x in hilbert]
plt.plot(amp, 'b', alpha=0.2)
plt.plot(hilbert, 'r', alpha=0.3)

plt.show()
