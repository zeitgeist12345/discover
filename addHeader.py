# Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details.

#!/usr/bin/env python3
import os

def add_header():
    # File extensions with their comment formats
    comment_formats = {
        '.html': '<!-- {} -->\n\n',
        '.js': '/* {} */\n\n',
        '.css': '/* {} */\n\n',
        '.py': '# {}\n\n',
        '.java': '/* {} */\n\n',
        '.cpp': '/* {} */\n\n',
        '.c': '/* {} */\n\n',
        '.h': '/* {} */\n\n',
        '.hpp': '/* {} */\n\n'
    }
    
    # License text
    license_text = """Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details."""
    
    for root, dirs, files in os.walk('.'):
        for f in files:
            for ext, format in comment_formats.items():
                if f.endswith(ext):
                    path = os.path.join(root, f)
                    header = format.format(license_text)
                    
                    try:
                        with open(path, 'r+', encoding='utf-8') as file:
                            content = file.read()
                            if content.startswith(header.strip()):
                                print(f"✓ Already has header: {path}")
                                continue
                            file.seek(0)
                            file.write(header + content)
                        print(f"✓ Added to: {path}")
                    except Exception as e:
                        print(f"✗ {path}: {e}")
                    break

if __name__ == "__main__":
    add_header()