# Installation and setup

These are instructions to setup the ICPC AutoAnalyst tools. These
tools are meant run in conjuction with an ICPC-style programming
contest and provide both automatic analysis and support for manual
analysis of the contest. As such they require tight integration with
the contest environment.

Note that these instructions are work in progress and also, you may
need to make additional changes depending on your contest environment.

## Instructions

On a Debian/Ubuntu-like system the following set of packages has to be
installed:
```
sudo apt-get install \
       git gitk gitweb apache2 phpmyadmin mariadb-server mariadb-client \
       php5-cli ntp rsync make curl python-yaml php-pear php5-dev \
       libyaml-dev openjdk-7-jdk openjdk-7-jre python-httplib2 python-mysqldb
```

Then install the PECL YAML extension:
```
sudo pecl install yaml
```
and enable it by adding `extension=yaml.so` to the PHP config. This is
best done by adding it to a new file
```
/etc/php5/mods-available/yaml.ini
```
and then symlinking it as follows:
```
sudo ln -s ../mods-available/yaml.ini /etc/php5/conf.d/99-yaml.ini
```

Then enable some Apache modules:
```
sudo a2enmod proxy proxy_http cgi
```
and add the following lines to the relevant apache site configuration
```
ProxyPass        /icat/api/Scoreboard http://localhost:8099/Scoreboard
ProxyPassReverse /icat/api/Scoreboard http://localhost:8099/Scoreboard
```
or as a configuration snippet in
```
/etc/apache2/conf-available/icat.conf
```
and enable this with `a2enconf`.

On Ubuntu (not Debian) you should also run
```
sudo ln -s /etc/apache2/{conf.d/gitweb,conf-enabled/gitweb.conf}
```
Finally, restart Apache to make the changes take effect:
```
sudo service apache2 restart
```

Create a user and database `icat` in MySQL/MariaDB.

Get the AutoAnalyst repository:
```
git clone https://github.com/icpc-live/autoanalyst.git
cd autoanalyst
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

