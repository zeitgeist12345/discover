#!/usr/bin/env python3
import os
import sys

def add_header():
    if len(sys.argv) != 3:
        print("Usage: python script.py <extension> <header_file>")
        sys.exit(1)
    
    ext, header_file = sys.argv[1], sys.argv[2]
    if not ext.startswith('.'):
        ext = '.' + ext
    
    # Read header from file
    with open(header_file, 'r', encoding='utf-8') as f:
        header = f.read()
    
    for root, dirs, files in os.walk('.'):
        for f in files:
            if f.endswith(ext):
                path = os.path.join(root, f)
                try:
                    with open(path, 'r+', encoding='utf-8') as file:
                        content = file.read()
                        # Check if header already exists
                        if content.startswith(header.strip()):
                            print(f"✓ Already has header: {path}")
                            continue
                        file.seek(0)
                        file.write(header + content)
                    print(f"✓ Added to: {path}")
                except Exception as e:
                    print(f"✗ {path}: {e}")

if __name__ == "__main__":
    add_header()