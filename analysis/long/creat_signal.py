import numpy
import math

def signal(t, amp):
	sampleRate = 44100.0
	startFreq = 5000.0
	stopFreq = 5100.0
	buffsize = 6144
	duration = 0.005

	count = duration * sampleRate;
	sampleNum = 1;
	remain = 0;
	if count < buffsize:
		remain = int(count)
	else:
		sampleNum = int(count/buffsize)
		remain = int(count - sampleNum * buffsize)

	twopi = math.pi*2
	phase = t
	samples = []
	samples_zero = []
	k = math.pow(stopFreq / startFreq, 1 / duration)

	for j in range(sampleNum):
		for i in range(buffsize):
			if (j == sampleNum - 1) and (i > remain - 1):
				samples_zero.append(0)
			else:
				samples.append(math.sin(twopi * startFreq * (math.pow(k, phase) - 1) / math.log(k)))
				phase += 1.0 / sampleRate
			

	shape = numpy.kaiser(len(samples), 8.6)
	# print shape
	samples = (amp*numpy.array(shape) * numpy.array(samples)).astype(int)
	# print samples
	return list(samples)