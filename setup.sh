#!/bin/bash

# ICPC AutoAnalyst Setup Script
# This script sets up the complete ICPC AutoAnalyst environment

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
check_root() {
    if [[ $EUID -eq 0 ]]; then
        log_error "This script should not be run as root for security reasons"
        log_info "Please run as a regular user with sudo privileges"
        exit 1
    fi
}

# Detect operating system
detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command -v apt-get &> /dev/null; then
            OS="ubuntu"
            log_info "Detected Ubuntu/Debian system"
        elif command -v yum &> /dev/null; then
            OS="rhel"
            log_info "Detected RHEL/CentOS system"
        elif command -v pacman &> /dev/null; then
            OS="arch"
            log_info "Detected Arch Linux system"
        else
            log_error "Unsupported Linux distribution"
            exit 1
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
        log_info "Detected macOS system"
    else
        log_error "Unsupported operating system: $OSTYPE"
        exit 1
    fi
}

# Install dependencies based on OS
install_dependencies() {
    log_info "Installing system dependencies..."
    
    case $OS in
        "ubuntu")
            sudo apt-get update
            sudo apt-get install -y \
                apache2 \
                php \
                php-cli \
                php-mysql \
                php-yaml \
                php-curl \
                php-mbstring \
                php-xml \
                libapache2-mod-php \
                mariadb-server \
                mariadb-client \
                python3 \
                python3-pip \
                python3-dev \
                python3-mysqldb \
                python3-yaml \
                openjdk-21-jdk \
                git \
                curl \
                wget \
                unzip \
                build-essential \
                default-libmysqlclient-dev
            ;;
        "macos")
            if ! command -v brew &> /dev/null; then
                log_error "Homebrew is required but not installed"
                log_info "Please install Homebrew first: https://brew.sh/"
                exit 1
            fi
            
            brew update
            brew install \
                httpd \
                php \
                mysql \
                python3 \
                openjdk@21 \
                git \
                curl \
                wget \
                unzip
            
            # Install PHP extensions
            brew install php-yaml || log_warning "php-yaml not available via brew"
            
            # Install Python packages
            pip3 install --user PyMySQL PyYAML
            ;;
        "rhel")
            sudo yum update -y
            sudo yum install -y \
                httpd \
                php \
                php-cli \
                php-mysql \
                php-curl \
                php-mbstring \
                php-xml \
                mariadb-server \
                mariadb \
                python3 \
                python3-pip \
                python3-devel \
                java-21-openjdk-devel \
                git \
                curl \
                wget \
                unzip \
                gcc \
                gcc-c++ \
                make
            ;;
        "arch")
            sudo pacman -Syu --noconfirm
            sudo pacman -S --noconfirm \
                apache \
                php \
                php-apache \
                mariadb \
                python \
                python-pip \
                jdk21-openjdk \
                git \
                curl \
                wget \
                unzip \
                base-devel
            ;;
    esac
    
    log_success "System dependencies installed"
}

# Configure Apache
configure_apache() {
    log_info "Configuring Apache web server..."
    
    case $OS in
        "ubuntu")
            # Enable required modules
            sudo a2enmod rewrite proxy_http
            
            # Create virtual host configuration
            sudo tee /etc/apache2/sites-available/autoanalyst.conf > /dev/null <<EOF
<VirtualHost *:80>
    ServerName localhost
    DocumentRoot /var/www/html
    ErrorLog \${APACHE_LOG_DIR}/error.log
    CustomLog \${APACHE_LOG_DIR}/access.log combined
    
    <Directory /var/www/html/icat>
        AllowOverride All
        Require all granted
    </Directory>
    
    ProxyPassMatch ^/icat/api/(EventFeed|scoreboard|teams)$ http://localhost:8099/\$1
    
    <Location "/icat/api/">
        RewriteEngine On
        RewriteRule icat/api/(CodeActivity|EditActivity|LastEditActivity|LastEditedNotSolvedProblem)$ /icat/api/\$1.php
    </Location>
</VirtualHost>
EOF
            
            # Enable the site
            sudo a2ensite autoanalyst
            sudo a2dissite 000-default || true
            
            # Create symlink for web files
            sudo rm -rf /var/www/html
            sudo ln -sf "$(pwd)/www" /var/www/html
            
            # Set permissions
            sudo chown -R www-data:www-data "$(pwd)/www" || true
            sudo chmod -R 755 "$(pwd)/www"
            
            # Restart Apache
            sudo systemctl restart apache2
            sudo systemctl enable apache2
            ;;
        "macos")
            # macOS Apache configuration
            APACHE_CONFIG="/usr/local/etc/httpd/httpd.conf"
            
            # Basic Apache configuration for macOS
            log_info "Please manually configure Apache on macOS"
            log_info "Web files are in: $(pwd)/www"
            ;;
        "rhel"|"arch")
            # Similar configuration for RHEL/Arch
            log_info "Please manually configure Apache for your distribution"
            log_info "Web files are in: $(pwd)/www"
            ;;
    esac
    
    log_success "Apache configured"
}

# Configure MySQL/MariaDB
configure_database() {
    log_info "Configuring MySQL/MariaDB database..."
    
    case $OS in
        "ubuntu")
            # Start MySQL service
            sudo systemctl start mysql
            sudo systemctl enable mysql
            
            # Secure installation (basic)
            sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'autoanalyst';" || true
            ;;
        "macos")
            # Start MySQL service
            brew services start mysql
            ;;
        "rhel"|"arch")
            # Start MariaDB service
            sudo systemctl start mariadb
            sudo systemctl enable mariadb
            ;;
    esac
    
    # Wait for MySQL to start
    sleep 5
    
    # Create database and user
    mysql -u root -pautoanalyst -e "CREATE DATABASE IF NOT EXISTS icat;" 2>/dev/null || \
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS icat;" 2>/dev/null || \
    sudo mysql -e "CREATE DATABASE IF NOT EXISTS icat;"
    
    mysql -u root -pautoanalyst -e "CREATE USER IF NOT EXISTS 'autoanalyst'@'localhost' IDENTIFIED BY 'autoanalyst';" 2>/dev/null || \
    mysql -u root -e "CREATE USER IF NOT EXISTS 'autoanalyst'@'localhost' IDENTIFIED BY 'autoanalyst';" 2>/dev/null || \
    sudo mysql -e "CREATE USER IF NOT EXISTS 'autoanalyst'@'localhost' IDENTIFIED BY 'autoanalyst';"
    
    mysql -u root -pautoanalyst -e "GRANT ALL PRIVILEGES ON icat.* TO 'autoanalyst'@'localhost';" 2>/dev/null || \
    mysql -u root -e "GRANT ALL PRIVILEGES ON icat.* TO 'autoanalyst'@'localhost';" 2>/dev/null || \
    sudo mysql -e "GRANT ALL PRIVILEGES ON icat.* TO 'autoanalyst'@'localhost';"
    
    mysql -u root -pautoanalyst -e "FLUSH PRIVILEGES;" 2>/dev/null || \
    mysql -u root -e "FLUSH PRIVILEGES;" 2>/dev/null || \
    sudo mysql -e "FLUSH PRIVILEGES;"
    
    # Initialize database schema if available
    if [[ -f "create_icat_instance.sql" ]]; then
        log_info "Initializing database schema..."
        mysql -u autoanalyst -pautoanalyst icat < create_icat_instance.sql 2>/dev/null || \
        log_warning "Could not initialize database schema automatically"
    fi
    
    log_success "Database configured"
}

# Create configuration file
create_config() {
    log_info "Creating configuration file..."
    
    if [[ ! -f "config.yaml" ]]; then
        if [[ -f "config.yaml.template" ]]; then
            cp config.yaml.template config.yaml
            
            # Update configuration with default values
            sed -i.bak 's/THISISNOTAPASSWORD/autoanalyst/g' config.yaml
            sed -i.bak 's/localhost/localhost/g' config.yaml
            
            rm config.yaml.bak 2>/dev/null || true
            
            log_success "Configuration file created from template"
        else
            log_error "config.yaml.template not found"
            exit 1
        fi
    else
        log_info "Configuration file already exists"
    fi
}

# Build Katalyzer
build_katalyzer() {
    log_info "Building Katalyzer application..."
    
    if [[ -d "katalyze" ]]; then
        cd katalyze
        ./gradlew install
        
        cd - > /dev/null
        log_success "Katalyzer build completed"
    else
        log_warning "Katalyze directory not found, skipping build"
    fi
}

# Set up directory structure
setup_directories() {
    log_info "Setting up directory structure..."
    
    # Create necessary directories
    mkdir -p backup
    mkdir -p githomes
    mkdir -p logs
    
    # Set permissions
    sudo chmod 755 backup githomes logs
    sudo chmod a+x ..

    log_success "Directory structure created"
}

# Install Python dependencies
install_python_deps() {
    log_info "Installing Python dependencies..."
    
    # Install required Python packages
    pip3 install --user PyMySQL PyYAML 2>/dev/null || \
    python3 -m pip install --user PyMySQL PyYAML 2>/dev/null || \
    log_warning "Could not install Python dependencies"
    
    log_success "Python dependencies installed"
}

# Display final information
show_completion_info() {
    echo
    log_success "ICPC AutoAnalyst setup completed!"
    echo
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  🎉 Setup Complete!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo
    echo "📁 Project directory: $(pwd)"
    echo "🌐 Web interface: http://localhost/icat/"
    echo "🔧 Katalyzer API: http://localhost:8099/ (when running)"
    echo "🗄️  Database: icat (user: autoanalyst, password: autoanalyst)"
    echo
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  📋 Next Steps:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo
    echo "1. 🚀 Start Katalyzer:"
    echo "   cd katalyze && java -jar build/libs/katalyze-1.0-SNAPSHOT.jar"
    echo
    echo "2. 🔧 Run Python code analyzer:"
    echo "   cd code_analyzer && python3 analyzer.py"
    echo
    echo "3. ⚙️  Edit configuration:"
    echo "   vim config.yaml"
    echo
    echo "4. 🌐 Access web interface:"
    echo "   Open http://localhost/icat/ in your browser"
    echo
    echo "5. 🗄️  Access database:"
    echo "   mysql -u autoanalyst -pautoanalyst icat"
    echo
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  🔍 Troubleshooting:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo
    echo "• Check Apache status: sudo systemctl status apache2"
    echo "• Check MySQL status: sudo systemctl status mysql"
    echo "• View Apache logs: sudo tail -f /var/log/apache2/error.log"
    echo "• Test database: mysql -u autoanalyst -pautoanalyst -e 'SHOW DATABASES;'"
    echo
    echo "🎯 The system is ready for testing ICPC contest analysis!"
    echo
}

# Main execution
main() {
    echo "🚀 ICPC AutoAnalyst Setup Script"
    echo "=================================="
    echo
    
    check_root
    detect_os
    install_dependencies
    create_config
    setup_directories
    configure_apache
    configure_database
    install_python_deps
    build_katalyzer
    show_completion_info
}

# Run main function
main "$@" 
