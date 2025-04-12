#!/bin/bash

#
# Copyright 2025 Todd Ginsberg
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Check if version argument is provided
if [ -z "$1" ]; then
  echo "Please provide a Hugo version."
  exit 1
fi

VERSION=$1
PLATFORM=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCHITECTURE=$(uname -m)

# Set Hugo binary name based on architecture
if [ "$ARCHITECTURE" == "x86_64" ]; then
  ARCH="amd64"
else
  ARCH="$ARCHITECTURE"
fi

# Recreate a subdirectory called 'hugo' if it doesn't exist
rm -rf hugo
mkdir -p hugo

# Construct the download URL
URL="https://github.com/gohugoio/hugo/releases/download/v$VERSION/hugo_extended_${VERSION}_${PLATFORM}-${ARCH}.tar.gz"

echo $URL

# Download the file
echo "Downloading Hugo version $VERSION for $PLATFORM-$ARCH..."
curl -L -O "$URL"

# Check if the download was successful
if [ $? -ne 0 ]; then
  echo "Download failed. Please check the version and try again."
  exit 1
fi

# Extract the downloaded file to the 'hugo' subdirectory
echo "Extracting Hugo..."
tar -xvzf "hugo_extended_${VERSION}_${PLATFORM}-${ARCH}.tar.gz" -C hugo

# Clean up the tarball
rm "hugo_extended_${VERSION}_${PLATFORM}-${ARCH}.tar.gz"

# Display the location of the Hugo binary
echo "Hugo binary is located in the 'hugo' directory: $(pwd)/hugo/hugo"

# Verify the installation
echo "Hugo version installed:"
./hugo/hugo version
