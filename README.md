# Disk space profiler

## Task description

Develop a disk space profiler – a cross-platform tool with a GUI that
helps the user identify which files and folders take up the most space
on a specific folder or disk.

The typical use scenario is to find large files or folders that are not
needed anymore and be able to delete them to free up space.

The interface should be user-friendly and remain responsive during
the initial scan and any further re-scans.

The tool should be able to handle file system updates (e.g., file added
or deleted) under scanned (opened) folder and update displayed data.
You may assume that there are no symbolic links in the folders being
profiled.

The tool may be a desktop app or a web application (or even an
electron-based tool).

You can use any technologies, libraries, or programming languages
that you prefer.

No authorization or access management functionality is required.

The following aspects will be evaluated:
1. The correctness of the source code and the program operation.
2. The tool’s performance and responsiveness.
3. Simplicity and clarity of the source code.
4. User experience.

## Used technology
* Written in Kotlin 
* TornadoFX used for GUI - https://tornadofx.io/
    * Based on the examples from https://github.com/edvin/tornadofx-samples
        * TreeView -  https://github.com/edvin/tornadofx-samples/tree/master/treeviews
    
# Build
Note if you are on Ubuntu, please make sure you have a jdk version with [javafx included](https://askubuntu.com/questions/1091157/javafx-missing-ubuntu-18-04).

