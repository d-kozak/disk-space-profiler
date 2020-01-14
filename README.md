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

## Technical description
* Written in Kotlin 
* Build using Gradle
* [TornadoFX](https://tornadofx.io/) used for GUI
* Consists of two modules
    * desktop-client - GUI of the program
    * scanner -  disk crawling functionality, file system (fs) tree model

## Extensions
This sections contains features that were not implemented yet, but the author has thought about them 
and would like to add them in the future.
- [ ] FsTree updates on the fly - update the fs tree incrementally during analysis -> better user experience
- [ ] Concurrent scanning - try to paralelize the scanning process
- [ ] Locate biggest files - remember N biggest files found during scan and show them in the GUI in separate view(maybe as tabpane with DirectoryView?)
- [ ] Native file open - add support to open files natively
- [ ] Date modified - show date modified in the GUI  
- [X] DirectoryWatchService using coroutines
- [ ] FSCrawler using FileVisitor -> it will be probably faster for deeper trees
- [ ] When a lazy node is visible in the FileTreeView, automatically start incremental scan of its subtree

## Run
You can run the app directly using gradle.
```
gradle run
```

## Deploy
To deploy the app, you can use shadowJar.
```
gradle shadowJar
```
It will create a jar archive desktop-client/build/libs/desktop-client.jar, which can then be executed.
```
java -jar desktop-client/build/libs/desktop-client.jar
```

Note -  TornadoFX is built on top of JavaFX. Please make sure you have a jdk version with [javafx included](https://askubuntu.com/questions/1091157/javafx-missing-ubuntu-18-04).


