import csv
import math
import numpy as np
import scipy as sp
import struct
import matplotlib.pyplot as plt
from scipy import optimize

TIME = 1.0/44100

def reduce(data, origin_func):
    results = []
    # data = [0,1,2,3,4,5]

    # get index of first value in data that is larger than 10000
    # TODO this value (15000) needs to be changed when freq is not around 5K Hz
    offset = (i for i,v in enumerate(data) if v > 15000).next()
    results = data[offset:offset+int(origin_func.duration/TIME)]
    # print amp
    value = 1000000000000000000
    starttime = 0
    # TODO this value (160) needs to be changed when freq is not around 5K Hz
    for start in np.arange(0, 2*TIME, TIME/80):
        # origin = [function(start, amp, startf, endf, length) for i in range(len(results))]
        # origin = [function(start) for  in range(len(results))]
        origin = []
        error = 0
        for i in range(10):
            # origin.append(function(start+i*TIME))
            error+=abs(results[i]-origin_func(start+i*TIME))
        # local_max = max(np.correlate(origin, results, "full"))
        # error = [(results[i]-origin[i]) for i in range(len(results))]
        #print error
        if (error<value):
            starttime = start
            value = error
        #print "got here", j

    #f = open(RESULT, 'w')
    num = 0
    plot1 = []
    kaifei_now=[]
    kaifei_past=[]
    # print len(data)
    # print len(results)
    while (num<len(data)):
        if (num>=offset and num < offset+len(results)):
            now = data[num]
            past = origin_func(starttime + TIME*(num-offset))
            #f.write(str(now-past)+' ')
            plot1.append(now-past)
            kaifei_past.append(past)
            kaifei_now.append(now)
        
        else:
            #f.write(str(data[num])+' ')
            plot1.append(data[num])
        num+=1
    # plt.plot(plot1)
    # plt.show()

    print "a =", kaifei_now
    print "b =", kaifei_past
    return np.array(kaifei_now) - np.array(kaifei_past)

    # with open(filename, 'r') as inputfile:
    #     linenumber = 0
    #     for row in csv.reader(inputfile):
    #         linenumber+=1
    #         if linenumber>5: 
    #             if (row[0]!="[-inf]" and row[0]!="[inf]"):   
    #                 results.append(row[0])

    # for elem in results:

    #     sample = float(elem)
    #     ratio = 10**(sample/10.0)
    #     if (ratio>0.9):
    #         point2 = 10**(float(results[counter])/10.0)
    #         point3 = 10**(float(results[counter+1])/10.0)

    #         amp = solve(func, point2, point3)
    #         determinetime = 0
    #         errortime = 1
    #         for i in range(20):
    #             error = abs(function((time+i*(time/20.0)), amp, startf, endf, length)-ratio)
    #             if (error<errortime):
    #                 errortime = error
    #                 determinetime = time+i*(time/20.0)
    #         break
    # counter+=1

    # for j in range((int)(length/1000.0*time)):
    #     origin = 10**(float(results[counter-1+j])/10.0)
    #     reduction = abs(function(determinetime+(j-1)*time))
    #     results[counter-1+j] = 10.0*math.log10(abs(origin-reduction))

    # with open(RESULT, 'w') as resultfile:
    #     for elem in results:
    #         resultfile.write(elem)


# def solve(func, point1, point2):
#     print (point1, point2)
#     a= abs(sp.optimize.newton(func, -800, None, (point1, point2)))
#     return a



# def function(t):
#     return t


def func(a, point1, point2):
    #TODO
    # function()
    # point1=asin(t)
    # point2=asin(t+time)
    # t=arcsin(point1/a)
    # t+time=arcsin(point2/a)
    return np.arcsin(point1/a)-np.arcsin(point2/a)-time
    

def read(filename):
    f = open(filename, 'rb').read()
    data_tuple = struct.unpack('<'+'h'*(len(f)/2), bytearray(f))
    return data_tuple

def main():
    reduction(FILE)

if __name__ == "__main__":
    main()