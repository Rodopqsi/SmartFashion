#!/usr/bin/env bash
# Script to create a cleaned SQL dump file from a possibly contaminated dump
# Usage: ./clean_db_dump.sh /path/to/db_dump_full.utf8.sql /path/to/output_cleaned.sql

set -euo pipefail

INPUT_FILE="${1:-cliente/backend/db_dump_full.utf8.sql}"
OUTPUT_FILE="${2:-cliente/backend/db_dump_full.utf8.cleaned.sql}"

echo "Cleaning SQL dump: $INPUT_FILE -> $OUTPUT_FILE"

# Strategy: print from the first occurrence of a valid SQL dump header ("-- MySQL dump") onward.
# This strips leading lines like "Enter password:" or any other shell prompts.

awk 'BEGIN{p=0} /^-- MySQL dump/ {p=1} p{print}' "$INPUT_FILE" > "$OUTPUT_FILE"

echo "Cleaned file written to: $OUTPUT_FILE"
