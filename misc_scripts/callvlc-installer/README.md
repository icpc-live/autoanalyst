# Installer for `callvlc`
During the contest, the CDS publishes webcam and desktop video feeds. VLC
identifies this feed as *H264 - MPEG-4 AVC (part 10)*, which Firefox can't
decode. Hence, we have created a solution for opening VLC when clicking
a video stream link. During WF 2019, these streams are served over HTTP
without authentication.

## Installation
1. Install the Ubuntu packages `docker.io` and `docker-compose` on some
master computer.
2. Execute `docker-compose up` in this directory.
3. Note the IP address of the machine. Let's say that it's `192.168.2.66`.
4. On each machine, run `wget -O - 192.168.2.66 | sh`.
