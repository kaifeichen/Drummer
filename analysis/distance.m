setenv GNUTERM x11

addpath('./peakfit')
pkg load signal

function result = iff(condition, v1, v2)
    if condition
        result = v1;
    else
        result = v2;
    end
end

% x=[0:.1:10];
% y=exp(-(x-5).^2);
% [FitResults,MeanFitError]=peakfit([x' y'],5,10,1,1)

% read data from file
filename = '1wall_0d_10ft_10ms.2.dat';
fid = fopen(filename, 'rb');
[val, _] = fread(fid, Inf, 'int16', 0, 'ieee-le');
fclose(fid);

% calcualte peakfit
itvl = 1/44100; % in second
count = rows(val);
time = [0:itvl:(count-1)*itvl]';
envelop = abs(hilbert(val));

time = time(53700:54600);
envelop = envelop(53700:54600);
%envelop = arrayfun(@(x) iff(x < 10000, 0, x), envelop);
fixedwidth = 0.010; % in second
peakfit([time envelop], center=0, window=0, NumPeaks=3, peakshape=11, extra=0, NumTrails=1, start=0, autozero=0, fixedwidth=fixedwidth, plot=1, bipolar=0)

% plot peakfits
