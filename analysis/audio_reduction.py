import csv
import math
import numpy as np
import scipy as sp
import struct
import matplotlib.pyplot as plt
from scipy import optimize
FILE = '1wal_180d_20ft_10ms.2.dat'
RESULT = 'RESULT.txt'
TIME = 1.0/44100

def reduction(filename, startf=5000.0, endf=5100.0, length=10.0):
    results = []
    counter = 0
    data = read(filename)
    for elem in data:
        if elem > 15000:
            for i in range(int(length/1000/TIME)):
                results.append(data[counter+i])
            break;
        counter+=1
    amp = max(results)
    print amp
    value = 0
    starttime = 0
    for j in range(160):
        start = j*TIME/80.0
        origin = [function(start, amp, startf, endf, length) for i in range(len(results))]
        local_max = max(np.correlate(origin, results, "full"))
        if (local_max>value):
            starttime = start
            value = local_max
        print "got here"

    f = open(RESULT, 'w')
    num = 0
    plot1 = []
    # print len(data)
    # print len(results)
    while (num<len(data)):
        if (num>=counter and num < counter+len(results)):
            now = data[num]
            past = function(starttime + TIME*(num-counter), amp, startf, endf, length)
            f.write(str(now-past)+' ')
            plot1.append(now-past)
            print now
            print past
        
        else:
            f.write(str(data[num])+' ')
            plot1.append(data[num])
        num+=1  
    # plt.plot(plot1)
    # plt.show()

        

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

def function(t, a, start_fr, end_fr, length):
    k = math.pow(end_fr/start_fr, 1.0/length);
    den = math.log(k)
    num = math.pow(k,t)-1
    rest = math.pi*2*start_fr
    result = math.sin(1.0*num*rest/den)
    return a*result


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