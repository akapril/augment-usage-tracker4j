#!/bin/bash

# Build and test script for Augment Usage Tracker IDEA Plugin

set -e

echo "🚀 Building Augment Usage Tracker IDEA Plugin..."

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required (found Java $JAVA_VERSION)"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"

# Make gradlew executable
chmod +x gradlew

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Compile the project
echo "🔨 Compiling project..."
./gradlew compileKotlin

# Run tests
echo "🧪 Running tests..."
./gradlew test

# Build the plugin
echo "📦 Building plugin..."
./gradlew buildPlugin

# Verify the plugin
echo "🔍 Verifying plugin..."
./gradlew verifyPlugin

# Check if plugin JAR was created
PLUGIN_JAR=$(find build/distributions -name "*.zip" | head -n 1)
if [ -n "$PLUGIN_JAR" ]; then
    echo "✅ Plugin built successfully: $PLUGIN_JAR"
    echo "📊 Plugin size: $(du -h "$PLUGIN_JAR" | cut -f1)"
else
    echo "❌ Plugin JAR not found"
    exit 1
fi

echo ""
echo "🎉 Build completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Install the plugin: File → Settings → Plugins → Install Plugin from Disk"
echo "2. Select the file: $PLUGIN_JAR"
echo "3. Restart IntelliJ IDEA"
echo "4. Configure in: File → Settings → Tools → Augment Usage Tracker"
echo ""
echo "🔧 Development commands:"
echo "• Run in development: ./gradlew runIde"
echo "• Run tests only: ./gradlew test"
echo "• Clean build: ./gradlew clean build"
