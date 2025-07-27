# ICPC AutoAnalyst Setup Instructions

This setup script will automatically install and configure the complete ICPC AutoAnalyst system on your machine.

## Quick Start

### Prerequisites
- A Unix-like system (Ubuntu/Debian, macOS, RHEL/CentOS, Arch Linux)
- sudo privileges for installing system packages
- Internet connection for downloading dependencies

### Installation

1. **Run the setup script**:
   ```bash
   ./setup.sh
   ```

2. **Wait for completion** - The script will:
   - Detect your operating system
   - Install all required dependencies (Apache, PHP, MySQL, Java, Python, etc.)
   - Configure Apache web server
   - Set up MySQL database with user and permissions
   - Create configuration files
   - Build the Katalyzer application
   - Set up directory structure

3. **Access the system**:
   - **Web Interface**: http://localhost/icat/
   - **Katalyzer API**: http://localhost:8099/ (when running)
   - **Database**: `mysql -u autoanalyst -pautoanalyst icat`

## What Gets Installed

### System Packages
- **Apache 2** - Web server
- **PHP 8+** - With MySQL, YAML, cURL, mbstring, XML extensions
- **MySQL/MariaDB** - Database server
- **Python 3** - With MySQLdb and YAML support
- **Java 21** - For Katalyzer application
- **Gradle** - For building Kotlin/Java components
- **Build tools** - gcc, make, etc.

### Configuration
- **Database**: `icat` database with `autoanalyst` user (password: `autoanalyst`)
- **Web Server**: Apache configured with virtual host for AutoAnalyst
- **Config File**: `config.yaml` created from template with default settings

### Directory Structure
```
autoanalyst/
├── www/           # Web interface files
├── katalyze/      # Katalyzer application
├── code_analyzer/ # Python analysis tools
├── backup/        # Team backup storage
├── githomes/      # Git repositories
├── logs/          # Log files
└── config.yaml    # Configuration file
```

## Starting Services

### 1. Katalyzer (Real-time Analysis)
```bash
cd katalyze
java -jar build/libs/katalyze-1.0-SNAPSHOT.jar
```

### 2. Code Analyzer (Python Tools)
```bash
cd code_analyzer
python3 analyzer.py
```

### 3. Web Interface
The web interface should already be running at http://localhost/icat/

## Supported Operating Systems

| OS | Package Manager | Status |
|----|-----------------|---------|
| Ubuntu/Debian | apt-get | ✅ Fully Supported |
| macOS | Homebrew | ✅ Supported (manual Apache config) |
| RHEL/CentOS | yum | ✅ Supported (manual Apache config) |
| Arch Linux | pacman | ✅ Supported (manual Apache config) |

## Troubleshooting

### Common Issues

1. **Permission denied errors**:
   ```bash
   # Make sure you're not running as root
   whoami  # Should NOT be 'root'
   
   # Make sure you have sudo privileges
   sudo echo "Test"
   ```

2. **MySQL connection issues**:
   ```bash
   # Check MySQL status
   sudo systemctl status mysql
   
   # Test database connection
   mysql -u autoanalyst -pautoanalyst -e "SHOW DATABASES;"
   ```

3. **Apache not serving files**:
   ```bash
   # Check Apache status
   sudo systemctl status apache2
   
   # Check Apache logs
   sudo tail -f /var/log/apache2/error.log
   
   # Restart Apache
   sudo systemctl restart apache2
   ```

4. **Java/Gradle build issues**:
   ```bash
   # Check Java version
   java -version  # Should be 21+
   
   # Check Gradle
   gradle --version
   
   # Manual build
   cd katalyze && gradle clean build
   ```

5. **PHP extensions missing**:
   ```bash
   # Check installed PHP modules
   php -m | grep -E "(mysql|yaml|curl)"
   
   # Install missing extensions (Ubuntu)
   sudo apt-get install php-mysql php-yaml php-curl
   ```

### Service Management

```bash
# Apache
sudo systemctl start apache2
sudo systemctl stop apache2
sudo systemctl restart apache2
sudo systemctl status apache2

# MySQL
sudo systemctl start mysql
sudo systemctl stop mysql
sudo systemctl restart mysql
sudo systemctl status mysql
```

### Log Files

```bash
# Apache logs
sudo tail -f /var/log/apache2/error.log
sudo tail -f /var/log/apache2/access.log

# MySQL logs
sudo tail -f /var/log/mysql/error.log

# Katalyzer logs (when running)
# Check console output where you started Katalyzer
```

## Manual Configuration

If you need to customize the setup:

1. **Edit configuration**:
   ```bash
   vim config.yaml
   ```

2. **Apache virtual host** (Ubuntu):
   ```bash
   sudo vim /etc/apache2/sites-available/autoanalyst.conf
   sudo systemctl reload apache2
   ```

3. **Database settings**:
   ```bash
   mysql -u autoanalyst -pautoanalyst icat
   ```

## Next Steps

After successful setup:

1. **Configure contest data** in `config.yaml`
2. **Import contest information** using the web interface
3. **Start real-time analysis** with Katalyzer
4. **Access analysis results** via the web interface
5. **Use code analysis tools** for team submissions

## Getting Help

- Check the main project README for application-specific documentation
- Review log files for error messages
- Ensure all services are running: Apache, MySQL
- Verify network connectivity for downloading dependencies

## Cleanup

To remove the installation:

```bash
# Stop services
sudo systemctl stop apache2 mysql

# Remove packages (Ubuntu)
sudo apt-get remove apache2 php mysql-server openjdk-21-jdk gradle

# Remove configuration
sudo rm /etc/apache2/sites-available/autoanalyst.conf
sudo rm -rf /var/www/html  # if symlinked to this project

# Remove database
mysql -u root -p -e "DROP DATABASE icat; DROP USER 'autoanalyst'@'localhost';"
``` 
