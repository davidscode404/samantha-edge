import os
import argparse
import sys

def print_directory_structure(startpath, output_file, indent_level):
    try:
        items = os.listdir(startpath)
    except PermissionError:
        output_file.write('    ' * indent_level + '[Permission Denied]\n')
        return

    directories = []
    files = []
    for item in items:
        item_path = os.path.join(startpath, item)
        if os.path.isdir(item_path) and not os.path.islink(item_path):
            directories.append(item)
        else:
            files.append(item)

    directories.sort()
    files.sort()

    for dir_name in directories:
        output_file.write('    ' * indent_level + f'[{dir_name}]\n')
        dir_path = os.path.join(startpath, dir_name)
        print_directory_structure(dir_path, output_file, indent_level + 1)

    for file_name in files:
        output_file.write('    ' * indent_level + f'{file_name}\n')

def main():
    parser = argparse.ArgumentParser(description='Generate folder structure as text file.')
    parser.add_argument('input_folder', help='Path to the input folder')
    parser.add_argument('-o', '--output', default='folder_structure.txt',
                        help='Output file path (default: folder_structure.txt)')
    args = parser.parse_args()

    if not os.path.isdir(args.input_folder):
        print(f"Error: {args.input_folder} is not a valid directory.")
        sys.exit(1)

    root_name = os.path.basename(os.path.normpath(args.input_folder))

    with open(args.output, 'w') as f:
        f.write(f'[{root_name}]\n')
        print_directory_structure(args.input_folder, f, indent_level=1)

if __name__ == "__main__":
    main()