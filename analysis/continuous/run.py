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
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm


chirp_amp_thld = 20000
chirp_echo_duration = 0.030 # in second
max_amp_change = 10000

def read_data(fname):
    f = open(fname, 'rb')
    data = f.read()
    amp = numpy.array(struct.unpack("<" + "h" * (len(data) / 2), data))
    return amp


def plot_probe(enve_amps, duration, fs, create_video = False, plot_changes = False):
    maxtab, mintab = peakdet(enve_amps, 100)

    if create_video == True:
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
                start_idx = int(sample_idx - (duration / 2) * fs)
                stop_idx = start_idx + int((duration + chirp_echo_duration) * fs)
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

    # plot plot_changes 
    if plot_changes == True:
        echo_len = int((duration + chirp_echo_duration) * fs)

        chirp_times = [numpy.zeros(echo_len+1),]
        echo_times = [range(echo_len+1)]
        amps = []
        changes = []
        last_amps = []

        for sample_idx, amp in maxtab:
            if amp > chirp_amp_thld:
                start_idx = int(sample_idx - (duration / 2) * fs)
                stop_idx = start_idx + echo_len
   
                chirp_times.append([start_idx,]*(echo_len+1))
                echo_times.append(range(echo_len+1))
                current_amps = enve_amps[start_idx:stop_idx]
                amps.append(current_amps)
                if last_amps != []:
                    changes.append((current_amps-last_amps))
                else:
                    changes.append(numpy.zeros(echo_len))
                last_amps = current_amps
        chirp_times.append([len(enve_amps),]*(echo_len+1))
        echo_times.append(range(echo_len+1))
        changes.append(numpy.zeros(echo_len))

        
        chirp_times = numpy.array(chirp_times)
        echo_times = numpy.array(echo_times)
        changes = numpy.array(changes)

        fig = plt.figure()
        ax = fig.gca()
        surf = ax.pcolor(chirp_times, echo_times, numpy.abs(changes), cmap=cm.coolwarm, linewidth=0, vmin=0, vmax=max_amp_change)
        plt.axis([chirp_times.min(), chirp_times.max(), echo_times.min(), echo_times.max()])

        xtickitvl = 2 #second
        xticks = range(int(chirp_times.min()), int(chirp_times.max()), int(fs*xtickitvl))
        xlabels = numpy.array(xticks)/fs
        ax.set_xticks(xticks)
        ax.set_xticklabels(xlabels)
        ytickitvl = 2 #millisecond
        yticks = range(int(echo_times.min()), int(echo_times.max()), int(fs*ytickitvl/1000))
        ylabels = numpy.array(yticks)/int(fs/1000)
        ax.set_yticks(yticks)
        ax.set_yticklabels(ylabels)

        plt.xlabel('Chirp Time (second)')
        plt.ylabel('Echo Time (millisecond)')
        plt.colorbar(surf)
        plt.show()


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument('-f', '--fname', help="input file")
    ap.add_argument('-s', '--startfreq', type=int, help="start frequency (Hz)")
    ap.add_argument('-e', '--endfreq', type=int, help="end frequency (Hz)")
    ap.add_argument('-d', '--duration', type=float, help="duration (second)")
    ap.add_argument('-v', '--video', action='store_true', help="create video")
    ap.add_argument('-c', '--changeplot', action='store_true', help="plot changes")

    args = ap.parse_args()
    lowfreq = args.startfreq
    highfreq = args.endfreq
    duration = args.duration
    video = args.video
    changeplot = args.changeplot

    #read
    amps = read_data(args.fname)

    #filter
    lowcut = lowfreq - 500
    highcut = highfreq + 500
    fs = 44100
    filtered_amps = bpfilter.butter_bandpass_filter(
        amps, lowcut, highcut, fs, order=9)

    enve_amps = numpy.abs(signal.hilbert(filtered_amps))
    plot_probe(enve_amps, duration, fs, create_video=video, plot_changes=changeplot)
    # plt.plot(enve_amps)
    # plt.show()
    
