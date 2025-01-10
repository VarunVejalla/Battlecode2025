import os
from difflib import unified_diff

def compare_folders(folder1, folder2):
    for root, _, files in os.walk(folder1):
        for file in files:
            file1 = os.path.join(root, file)
            file2 = os.path.join(folder2, os.path.relpath(file1, folder1))

            if os.path.exists(file2):
                with open(file1, 'r') as f1, open(file2, 'r') as f2:
                    diff = unified_diff(f1.readlines(), f2.readlines(),
                                        fromfile=file1, tofile=file2)
                    for line in diff:
                        print(line, end="")
            else:
                print(f"File {file1} exists only in {folder1}")

    for root, _, files in os.walk(folder2):
        for file in files:
            file2 = os.path.join(root, file)
            file1 = os.path.join(folder1, os.path.relpath(file2, folder2))

            if not os.path.exists(file1):
                print(f"File {file2} exists only in {folder2}")

compare_folders("src/boony", "src/boostedboony")
