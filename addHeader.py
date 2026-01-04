# Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details.

#!/usr/bin/env python3
import os

def add_header():
    # Source file extensions with their comment formats
    comment_formats = {
        '.html': '<!-- {} -->\n\n',
        '.js': '/* {} */\n\n',
        '.css': '/* {} */\n\n',
        '.py': '# {}\n\n',
        '.java': '/* {} */\n\n',
        '.cpp': '/* {} */\n\n',
        '.c': '/* {} */\n\n',
        '.h': '/* {} */\n\n',
        '.hpp': '/* {} */\n\n',
        '.kt': '/* {} */\n\n',
        '.kts': '/* {} */\n\n',
        '.properties': '# {}\n\n',
        '.sql': '-- {}\n\n',
        '.toml': '# {}\n\n',
        '.yml': '# {}\n\n',
        '.yaml': '# {}\n\n',
        '.pro': '# {}\n\n',
        '.sh': '# {}\n\n',
        '.bash': '# {}\n\n',
        '.zsh': '# {}\n\n',
        '.rs': '// {}\n\n',
        '.go': '/* {} */\n\n',
        '.rb': '# {}\n\n',
        '.php': '/* {} */\n\n',
        '.swift': '// {}\n\n',
        '.scala': '/* {} */\n\n',
    }
    
    # Skip these non-source file extensions
    skip_extensions = {'.ico', '.png', '.webp', '.jar', '.jpg', '.jpeg', '.gif', 
                      '.svg', '.pdf', '.zip', '.tar', '.gz', '.exe', '.dll', 
                      '.so', '.dylib', '.class', '.o', '.a', '.idx', '.pack', 
                      '.rev', '.sample', '.name', '.md', '.xml'}
    
    # License text
    license_text = """Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details."""
    
    for root, dirs, files in os.walk('.'):
        for f in files:
            # Skip if extension should be skipped
            skip = False
            for skip_ext in skip_extensions:
                if f.endswith(skip_ext):
                    skip = True
                    break
            if skip:
                continue
                
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
                    except UnicodeDecodeError:
                        print(f"✗ Skipped binary file: {path}")
                    except Exception as e:
                        print(f"✗ {path}: {e}")
                    break

if __name__ == "__main__":
    add_header()