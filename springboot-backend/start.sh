#!/bin/bash
cd "$(dirname "$0")"
echo "========================================"
echo "  NetConfig SpringBoot Backend"
echo "  Port: 8080"
echo "  Database: date.db"
echo "========================================"
echo ""
echo "Prerequisites:"
echo "  - JDK 17+"
echo "  - Maven 3.6+"
echo ""
if [ ! -f "pom.xml" ]; then
    echo "[Error] pom.xml not found"
    exit 1
fi
mvn spring-boot:run
