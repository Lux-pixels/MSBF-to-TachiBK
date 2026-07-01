#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

echo
echo "MSBF-to-TachiBK"
echo "================="
echo "Starting local web converter..."
echo
echo "After the converter starts, open:"
echo "http://localhost:8080"
echo
echo "Leave this terminal open while converting."
echo "Press Ctrl+C to stop the converter."
echo

if [ -x "bin/MSBF-to-TachiBK" ]; then
  exec "bin/MSBF-to-TachiBK" serve
else
  echo "Could not find bin/MSBF-to-TachiBK"
  echo "Make sure you unzipped the full release package before running this file."
  exit 1
fi