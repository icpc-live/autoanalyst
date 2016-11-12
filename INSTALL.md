# Installation and setup

These are instructions to setup the ICPC AutoAnalyst tools. These
tools are meant to run in conjuction with an ICPC-style programming
contest and provide both automatic analysis and support for manual
analysis of the contest. As such they require tight integration with
the contest environment.

Note that these instructions are work in progress: they might be
incomplete and depending on your contest environment, you may need to
make additional changes.

## Instructions

On a Debian/Ubuntu-like system the following set of packages has to be
installed:
```
sudo apt-get install \
       git gitk gitweb apache2 apache2-utils phpmyadmin mariadb-server
       mariadb-client php5-cli ntp rsync make curl python-yaml \
       openjdk-7-jdk openjdk-7-jre python-httplib2 python-mysqldb
```

Create a user and database `icat` in MySQL/MariaDB.

Get the AutoAnalyst repository:
```
git clone https://github.com/icpc-live/autoanalyst.git
cd autoanalyst
```

Add the included `apache.conf` configuration to `/etc/apache2/conf-available/icat.conf`;
enable required modules and restart:
```
sudo a2enmod cgi proxy_http rewrite
sudo a2enconf icat gitweb
sudo service apache2 restart
```

Copy the configuration template and edit it:
```
cp config.yaml.template config.yaml
$EDITOR config.yaml
```
and do the same for the Katalyzer configuration:
```
cp katalyzer/katalyzer.properties.template katalyzer/katalyzer.properties
$EDITOR katalyzer/katalyzer.properties
```
TODO: merge Katalyzer properties into the global configuration.

Build the Katalyzer code:
```
make -C katalyzer
```

Create some symlinks:
- Symlink the homedir backup git repo to /var/lib/git/
- Symlink autoanalyst/www to /var/www/html/icat


## TODO

To run the katalyzer from the CDS use the script katalyze/run_katalyze.sh
To run the katalyzer from the stored event feed ef.xml use the script katalyze/run_local.sh < ef.xml


To initialize the GitHomes create the two directories githomes and backup, as configured in config.yaml and then run the script code_analyzer/prephomes.py.

To get the team backups from the CDS and run the code analyzer run the script code_analyzer/githomes.py.
