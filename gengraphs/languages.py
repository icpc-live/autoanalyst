#!/usr/bin/python

import os

color = [ [.333,.666,.444] , [1,1,0.666] , [1,.5,.333] , [0.333,0.166,.777] ]

languages = os.popen ('echo "select langid from submission where cid=5" | mysql --skip-column-names domjudge-tmp')

count = dict()

for lang in languages:
  lang = lang.rstrip()
  if lang not in count:
    count[lang] = 0
  count[lang] += 1

i = 0
D = 0.0
v = []
label = []

for key in count:
  label.append(key)
  v.append(count[key])
  i+=1
  D+=count[key]

print 'reset'
print 'b=0.4; a=0.4; B=-0.4; r=1.0; s=0.1'
print 'set term postscript eps enhanced color'
print 'set output "languages.eps"'
print 'set view 30, 20'
print 'set parametric'
print 'unset border'
print 'unset tics'
print 'unset key'
print 'unset colorbox'
print 'set ticslevel 0'
print 'set urange [0:1]'
print 'set vrange [0:1]'
print 'set xrange [-2:2]'
print 'set yrange [-2:2]'
print 'set zrange [0:3]'
print 'set cbrange [0:1]' # to prevent warnings (?)
print 'set multiplot'
print 'set pm3d'

#square
print 'set palette model RGB functions 0.8, 0.8, 0.95'
print 'set label "powered by DOMjudge" at 1.9,0,0 textcolor rgb "#A9A9B9" rotate by 58 centre'
print 'splot -2+4*u, -2+4*(1-v)*2, 0 with pm3d'

#shade
print 'set palette model RGB functions 0.8, 0.8, 0.85'
print 'splot cos(u*2*pi)*v, sin(u*2*pi)*v, 0 with pm3d'

# pie pieces
d=0.0;
for j in range(i):
  print 'set palette model RGB function ',color[j][0],',',color[j][1],',',color[j][2]
  print 'set urange [', d, ':', d+v[j]/D, ']'
  d+=v[j]/(2*D)
  print 'set label "',label[j],'" at (1+B)*cos(',d,'*2*pi), (1+B)*sin(',d,'*2*pi),s+a centre'
  d+=v[j]/(2*D)
  print 'splot cos(u*2*pi)*r, sin(u*2*pi)*r, s+v*a with pm3d'
  print 'splot cos(u*2*pi)*r*v, sin(u*2*pi)*r*v, s+a with pm3d'
  print 'set palette model RGB function ',0.9*color[j][0],',',0.9*color[j][1],',',0.9*color[j][2]
  print 'splot cos(u*2*pi)*r, sin(u*2*pi)*r, s+a with pm3d'

print 'unset multiplot'
