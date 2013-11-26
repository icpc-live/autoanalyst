#!/bin/sh -e
#
# This script is run one time only at the first boot of this
# icpclive image. It (re)generates host specific things such as
# SSH host keys, root passwords.

dpkg-reconfigure openssh-server

exit 0
