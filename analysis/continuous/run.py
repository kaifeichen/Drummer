import struct
import bpfilter
import argparse
import numpy
import os
import time
import matplotlib.pyplot as plt
from scipy import signal
from peakdetect import peakdet
from subprocess import call


chirp_amp_thld = 30000
chirp_echo_duration = 0.030 # in second

def read_data(fname):
    f = open(fname, 'rb')
    data = f.read()
    amp = numpy.array(struct.unpack("<" + "h" * (len(data) / 2), data))
    return amp


def plot_probe(enve_amps, duration, fs):
    maxtab, mintab = peakdet(enve_amps, 100)
    print maxtab
    print mintab

    ts = int(time.time())
    path = './png/'+str(ts)+'/'
    if not os.path.exists(path):
        os.makedirs(path)

    image_names = []
    image_times = []

    plt.clf()
    image_name = 'image00000.png'
    plt.savefig(path+image_name)
    image_names.append(image_name)
    image_times.append(0)

    cnt = 1
    for sample_idx, amp in maxtab:
        if amp > chirp_amp_thld:
            start_idx = sample_idx - (duration / 2) * fs 
            stop_idx = start_idx + (duration + chirp_echo_duration) * fs
            plt.clf()
            plt.ylim((0, 45000))
            plt.plot(enve_amps[start_idx:stop_idx])
            image_name = 'image'+str(cnt).zfill(5)+'.png'
            plt.savefig(path+image_name)
            image_names.append(image_name)
            image_times.append(float(start_idx)/fs)
            cnt += 1
    image_times.append(float(len(enve_amps))/fs)

    #create video
    listfile = open(path+'videos.txt', 'w')
    for idx in range(len(image_names)):
        image_name = image_names[idx]
        image_duration = image_times[idx+1] - image_times[idx]
        video_name = image_name[:-3] + "mp4"
        call(['ffmpeg', '-loop', '1', '-f', 'image2', '-i', path+image_name, "-c:v", "libx264", '-pix_fmt', 'yuv420p', '-t', str(image_duration), path+video_name])
        listfile.write("file "+"'"+video_name+"'\n")
    listfile.close()
    call(['ffmpeg', '-f', 'concat', '-i', path+'videos.txt', "-c", "copy", path+"out.mp4"])


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument('-f', '--fname', help="input file")
    ap.add_argument('-s', '--startfreq', type=int, help="start frequency (Hz)")
    ap.add_argument('-e', '--endfreq', type=int, help="end frequency (Hz)")
    ap.add_argument('-d', '--duration', type=float, help="duration (second)")

    args = ap.parse_args()
    lowfreq = args.startfreq
    highfreq = args.endfreq
    duration = args.duration


    #read
    amps = read_data(args.fname)

    #filter
    lowcut = lowfreq - 500
    highcut = highfreq + 500
    fs = 44100
    filtered_amps = bpfilter.butter_bandpass_filter(
        amps, lowcut, highcut, fs, order=9)

    enve_amps = numpy.abs(signal.hilbert(filtered_amps))
    plot_probe(enve_amps, duration, fs)
    
