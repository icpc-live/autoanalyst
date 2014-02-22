#!/usr/bin/python

import os, sys, time, subprocess

from common import BACKUP_TOP, config

# Git doesn't store file permissions and modification times, so store
# these separately in a directory listing (which is removed before
# each listing to prevent the file itself from showing up). Also store
# a simplified version that doesn't list dotfiles.
listings=[{'file':'listing_full.txt','ignore':".git"},
          {'file':'listing.txt'     ,'ignore':"'.*'"}]

def gen_listing():
	"""Generate directory listing(s) of git repository to keep track of file permissions."""
	fmt='%M %10s  %TY-%Tm-%Td %TT  %p\n'

	for l in listings:
		if os.path.exists(l['file']): os.unlink(l['file'])

	for l in listings:
		l['output'] = subprocess.check_output(
			'find . -mindepth 1 -name %s -prune -o -type f -printf "%s" '%(l['ignore'],fmt) +
			'| sed -r \'s/\.[0-9]{10}  /  /\'', shell=True)

	for l in listings:
		f = open(l['file'],'w')
		f.write(l['output'])
		f.close()

def init():
	"""Create and initialize homedirs git repository."""
	if not os.path.isdir(BACKUP_TOP): os.mkdir(BACKUP_TOP, 0755)
	os.chdir(BACKUP_TOP)

	if os.path.isdir(os.path.join(BACKUP_TOP,'.git')):
		print("Error: directory '%s' already contains a git repository."%BACKUP_TOP)
		exit(1)

	os.system('git init -q .')

	gen_listing()

	os.system('git add    -f --all --ignore-errors .')
	os.system('git commit -q --all --allow-empty ' +
	          '  -m "Initialization of ICPC homedirs at \'%s\'."'%BACKUP_TOP)
	os.system('git gc --aggressive')

	print("Team homedirs git repository initialized in '%s'"%BACKUP_TOP)

def commit():
	olddir = os.getcwd()
	os.chdir(BACKUP_TOP)

	# Make sure that source path ends with a '/' to sync its contents,
	# but not the directory itself into the target directory.
	os.system('rsync -a --delete --exclude=.git %s/ .'%config['teambackup']['backupdir'])

	tag = time.strftime('Tag_%H_%M')

	gen_listing()

	# First do explicit 'git add' to include new files too
	os.system('git add    -f --all --ignore-errors .')
	os.system('git commit -q --all --allow-empty -m "Autocommit"')
	os.system('git tag -f -m "" "%s" HEAD > /dev/null 2>&1'%tag)
	os.system('git gc --auto --quiet')

	print('Checking in %s at %s'%(tag,time.strftime('%c')))
	os.system(olddir + '/analyzer.py %s'%tag)

if __name__ == '__main__':
	if len(sys.argv)>1:
		if   sys.argv[1]=='init':
			init()
			exit()
		elif sys.argv[1]=='commit':
			commit()
			exit()

	print("Starting sync/commit loop...")
	while ( True ):
		commit()
		time.sleep(3)
