#!/bin/sh
# This script installs ICPC-live specific stuff to a cleanly installed
# Debian image. It assumes that the Debian system is reachable via
# SSH; it copies the necessary files and then runs the installation
# script there.

# Run this script as:
# $ install.sh <hostname>

set -e

EXTRA=/tmp/extra-files.tgz

DEFPASSWD=icpclive

if [ -n "$1" ]; then
	make -C  `dirname $0` `basename $EXTRA`
	scp "$0" `dirname $0`/`basename $EXTRA` "root@$1:`dirname $EXTRA`"
	ssh "root@$1" /tmp/`basename $0`
	exit 0
else
	if [ ! -f $EXTRA ]; then
		echo "Error: file '$EXTRA' not found; did you specify the target hostname?"
		exit 1
	fi
fi

# Unpack extra files:
cd /
tar xzf $EXTRA
rm -f $EXTRA

export DEBIAN_FRONTEND=noninteractive

# Update system, don't installed recommended dependencies:
echo 'APT::Install-Recommends "false";' >> /etc/apt/apt.conf
apt-get -q update
apt-get -q upgrade

# Make sure that root fs UUID is unique for each new image version:
tune2fs -U random /dev/root

# Fix some GRUB boot loader settings:
sed -i -e 's/^\(GRUB_TIMEOUT\)=.*/\1=15/' \
       -e 's/^#\(GRUB_\(DISABLE.*_RECOVERY\|INIT_TUNE\)\)/\1/' \
       -e '/GRUB_GFXMODE/a GRUB_GFXPAYLOAD_LINUX=1024x786,640x480' \
	/etc/default/grub
update-grub

# Mount /tmp as tmpfs:
sed -i '/^proc/a tmpfs		/tmp		tmpfs	size=512M,mode=1777	0	0' /etc/fstab

# Enable Bash autocompletion and ls colors:
sed -i '/^#if \[ -f \/etc\/bash_completion/,/^#fi/ s/^#//' /etc/bash.bashrc
sed -i '/^# *export LS_OPTIONS/,/^# *alias ls=/ s/^# *//' /root/.bashrc

# Use Google public nameservers:
sed -i '/^iface lo inet loopback/a 	dns-nameservers 8.8.8.8 8.8.4.4' \
	/etc/network/interfaces

# Disable persistent storage and network udev rules:
cd /lib/udev/rules.d
mkdir disabled
mv 75-persistent-net-generator.rules disabled
mv 75-cd-aliases-generator.rules     disabled
cd -

# Install packages:
debconf-set-selections <<EOF
mysql-server-5.1	mysql-server/root_password	password	$DEFPASSWD
mysql-server-5.1	mysql-server/root_password_again		password	$DEFPASSWD
mysql-server-5.5	mysql-server/root_password	password	$DEFPASSWD
mysql-server-5.5	mysql-server/root_password_again		password	$DEFPASSWD

phpmyadmin	phpmyadmin/mysql/admin-user	string	root
phpmyadmin	phpmyadmin/mysql/admin-pass	password	$DEFPASSWD
phpmyadmin	phpmyadmin/reconfigure-webserver	multiselect	apache2
phpmyadmin	phpmyadmin/database-type	select	mysql

EOF

apt-get install -q -y \
	openssh-server mysql-server apache2 phpmyadmin php5-cli \
	openjdk-6-jdk openjdk-6-jre-headless gcc g++ ntp debootstrap \
	sudo git gitweb rsync make curl python-yaml python-mysqldb \
	php-pear php5-dev libyaml-dev resolvconf

# Do not have stuff listening that we don't use:
apt-get remove -q -y --purge portmap nfs-common rpcbind

# Add ICPC-live git repository:
if [ -f /tmp/autoanalyst.tar.gz ]; then
	su -l -c 'tar xzf /tmp/autoanalyst.tar.gz' icpclive
else
	su -l -c 'git clone -q https://github.com/icpc-live/autoanalyst.git' icpclive
fi
su -l -c 'cd autoanalyst && git pull -q' icpclive
ln -s /home/icpclive/autoanalyst/www /var/www/icpc

# Make git repository of team homedirs available:
ln -s /home/icpclive/githomes/.git /var/lib/git/teambackups

# Build PHP PECL yaml extension (pass <enter> to interactive request
# for autodetection of libyaml install path):
printf '\n' | pecl -q install yaml

# Do some cleanup to prepare for creating a releasable image:
echo "Doing final cleanup, this can take a while..."
apt-get -q clean
rm -f /root/.ssh/authorized_keys /root/.bash_history

# Prebuild locate database:
/etc/cron.daily/mlocate

# Remove SSH host keys to regenerate them on next first boot:
rm -f /etc/ssh/ssh_host_*

# Replace /etc/issue with live image specifics:
mv /etc/issue /etc/issue.orig
cat > /etc/issue.icpclive <<EOF
ICPC-live running on `cat /etc/issue.orig`

Image generated on `date`

EOF
cp /etc/issue.icpclive /etc/issue.icpclive-default-passwords
cat /tmp/icpclive-default-passwords >> /etc/issue.icpclive-default-passwords
ln -s /etc/issue.icpclive-default-passwords /etc/issue

# Unmount swap and zero empty space to improve compressibility:
swapoff -a
cat /dev/zero > /dev/sda1 2>/dev/null || true
cat /dev/zero > /zerofile 2>/dev/null || true
sync
rm -f /zerofile

echo "Done installing, halting system..."

halt

# FIXME: other stuff, described here for documentation:
#
# If using DOMjudge as CCS, then a converted event feed 'ext.php' must
# be installed and a script run to convert it into a continuous feed.
# Install DOMjudge repository from
#
# $ git clone https://github.com/eldering/domjudge.git
#
# and build DOMjudgeFeed.class under www/plugin.
# On either domserver or this machine then run:
#
# www/plugin$ java cp .:commons-lang3.jar DOMjudgeFeed \
#		http://example.com/domjudge/plugin/ext.php $USER $PASS | nc -l 4714
#
# and let katalyzer connect to port 4714 on that machine.
