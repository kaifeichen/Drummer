from __future__ import division

import numpy
from numpy.linalg import inv
import bisect
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import argparse

NS2S = 1e9

def q2r(q):
    """
    Quaternion to Rotation Matrix
    ref: http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion
    q: quaternion (w, x, y, z)
    return: rotation matrix
    """
    w = q[0]
    x = q[1]
    y = q[2]
    z = q[3]
    n = w**2 + x**2 + y**2 + z**2
    s = 0.0 if n == 0 else 2.0/n
    wx = s * w * x
    wy = s * w * y
    wz = s * w * z
    xx = s * x * x
    xy = s * x * y
    xz = s * x * z
    yy = s * y * y
    yz = s * y * z
    zz = s * z * z
    r = [[1 - (yy+zz), xy - wz, xz + wy], \
        [xy + wz, 1 - (xx+zz), yz - wx], \
        [xz - wy, yz + wx, 1 - (xx + yy)]]
    r = numpy.matrix(r)
    return r


def loc(acclist, quatlist):
    """
    Calculate locations given quaternions and accelerations.
    acclist: list of (time in nanosecond, (x, y, z))
    quatlist: list of (time in nanosecond, (x, y, z)), where x, y, z are in unit quaternion (w, x, y, z)
    """

    # change accelerations to global frame
    acclist.sort(key = lambda x: x[0])
    quatlist.sort(key = lambda x: x[0])

    quat_idx = 0
    gacclist = []
    for time, acc in acclist:
        while True:
            if quat_idx < len(quatlist) and time < quatlist[quat_idx][0]:
                quat_idx -= 1
                print "FUUUU!"
            elif quat_idx+1 < len(quatlist) and time >= quatlist[quat_idx+1][0]:
                quat_idx += 1
            else:
                break
            
        quat = quatlist[quat_idx][1]
        w = (1 - quat[0]**2 - quat[1]**2 - quat[2]**2) ** 0.5
        quat = (w,) + quat
        r = q2r(quat) # r is the rotation matrix from phone frame to earth frame
        gacc = numpy.array(r.dot(numpy.matrix(acc).T).T)[0] # make gacc a list of (time in nanosecond, (x, y, z))
        gacclist.append((time, gacc))

    # calculate distance at 3 axises over time
    # velocities
    vlist = list(numpy.zeros(len(gacclist)))
    vlist[0] = (gacclist[0][0], numpy.zeros(3)) # init velocity is 0
    for idx in range(len(gacclist[:-1])): # ignore the last accelerometer
        time = gacclist[idx][0]
        next_time = gacclist[idx+1][0]
        gacc = gacclist[idx][1]
        v = vlist[idx][1]
        elapsed_time = (next_time - time) / NS2S # in second
        next_v = v + gacc * elapsed_time
        vlist[idx+1] = (next_time, next_v) # always calculate next velocity

    # locations
    loclist = list(numpy.zeros(len(vlist)))
    loclist[0] = (vlist[0][0], numpy.zeros(3)) # init velocity is 0
    for idx in range(len(vlist[:-1])): # ignore the last accelerometer
        time = vlist[idx][0]
        next_time = vlist[idx+1][0]
        elapsed_time = (next_time - time) / NS2S # in second
        gacc = gacclist[idx][1]
        v = vlist[idx][1]
        loc = loclist[idx][1]
        next_loc = loc + v * elapsed_time + 0.5 * gacc * (elapsed_time ** 2)
        loclist[idx+1] = (next_time, next_loc) # always calculate next velocity

    return loclist


def readfiles(acc_file_name, quat_file_name):
    accfile = open(acc_file_name, 'r')
    quatfile = open(quat_file_name, 'r')
    acctmpstr = [l.split() for l in accfile.readlines()]
    quattmpstr = [l.split() for l in quatfile.readlines()]
    acclist = [(float(l[1]), (float(l[2]), float(l[3]), float(l[4]))) for l in acctmpstr]
    quatlist = [(float(l[1]), (float(l[2]), float(l[3]), float(l[4]))) for l in quattmpstr]

    return acclist, quatlist


def plot(loclist):
    fig = plt.figure()
    ax = fig.gca(projection='3d')
    coordlist = [coord for time, coord in loclist]
    xlist, ylist, zlist = zip(*coordlist)
    ax.plot(xlist, ylist, zlist)
    ax.set_xlabel('x (meter)')
    ax.set_ylabel('y (meter)')
    ax.set_zlabel('z (meter)')
    plt.show()


if __name__ == "__main__":

    ap = argparse.ArgumentParser()
    ap.add_argument('-a', '--accfile', help="acceleration input file")
    ap.add_argument('-r', '--rotationfile', help="rotation input file")
    args = ap.parse_args()

    accfile = args.accfile
    rotationfile = args.rotationfile

    acclist, quatlist = readfiles(accfile, rotationfile)
    loclist = loc(acclist, quatlist)
    plot(loclist)




