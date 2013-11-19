#!/bin/sh -e

CONFIG="../config.yaml"

# Simplistic method to get subkeys of 'teambackup' from config file;
# assumes that every 'key: value' pair is contained on a single line.
yaml_get_key()
{
	grep -A4 '^teambackup:' $CONFIG | grep "$1:" | sed "s/.*$key: *\(.*\)/\1/"
}

BACKUP=` yaml_get_key backupdir`
BASEDIR=`yaml_get_key gitdir`

echo "'$BACKUP' '$BASEDIR'"

LISTINGFULL="listing_full.txt"
LISTINGSHORT="listing.txt"

# git doesn't store file permissions and mod. times, so store these
# separately in a dir. listing (remove first to prevent the file
# itself from showing up). Also store a simplified version that
# doesn't list dotfiles.
gen_listing()
{
	local TMPFULL=`mktemp`
	local TMPSHORT=`mktemp`
	local FMT='%M %10s  %TY-%Tm-%Td %TT  %p\n'

	rm -f $LISTINGFULL $LISTINGSHORT

	find . -mindepth 1 -name .git -prune -o -type f -fprintf $TMPFULL "$FMT"

	find . -mindepth 1 -name '.*' -prune -o -type f -fprintf $TMPSHORT "$FMT"

	sed -r 's/\.[0-9]{10}  /  /' $TMPFULL  > $LISTINGFULL
	sed -r 's/\.[0-9]{10}  /  /' $TMPSHORT > $LISTINGSHORT

	rm -f $TMPFULL $TMPSHORT
}

case "$1" in
	init)
		[ -d $BASEDIR ] || mkdir -p $BASEDIR

		cd $BASEDIR
		chmod a+rx .

		git init -q .

		gen_listing

		git add    -f --all --ignore-errors .
		git commit -q --all --allow-empty \
			-m "Initialization of ICPC homedirs at '$BASEDIR'."
		git gc --aggressive
		;;

	commit)
		cd $BASEDIR

		rsync -a --delete --exclude=.git $BACKUP .

		TAG="Tag_`date '+%H_%M'`"

		gen_listing

		# First do explicit 'git add' to include new files too
		git add    -f --all --ignore-errors .
		git commit -q --all --allow-empty -m "Autocommit"
		git tag -f -m '' "$TAG" HEAD > /dev/null 2>&1
		git gc --auto --quiet

		echo "Checking in $TAG at `date`"
		~/autoanalyst/code_analyzer/analyzer.py $TAG
		;;

	*)
		echo "Error: missing or unknown command '$1'."
		exit 1
		;;
esac
