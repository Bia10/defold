# Defold Editor

## Requirements
* [Java 11](https://jdk.java.net/11/) ([installation instructions](#installing-jdk11) - Note that we do not yet officially support development with a newer version of Java than 11.
* Leiningen ([install instructions](#install-leiningen))

## Installing JDK 11
### macOS

* Download the [macOS](https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_osx-x64_bin.tar.gz) version and extract it
* Run `sudo cp -R <path-to-extracted-folder>/jdk-11.0.2.jdk /Library/Java/JavaVirtualMachines/`
* Verify that the jdk version is available by running `/usr/libexec/java_home -V`
* Switch to the new version by running `/usr/libexec/java_home -v 11.0.2`

### Linux

* Download [Linux](https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz) version and extract it somewhere
* Run `sudo update-alternatives --install "/usr/bin/java" "java" "/path/to/jdk/bin/java" 1102`
* Run `sudo update-alternatives --install "/usr/bin/javac" "javac" "/path/to/jdk/bin/javac" 1102`
* If require, switch to new version using `sudo update-alternatives --config java` and `sudo update-alternatives --config javac`

### Notes

If you are using IntelliJ for lein tasks, you will need to first add the new SDK (file->project structure/SDKs)
and then set the project SDK setting (file->project structure/Project) to the new version.

## Installing Leiningen
### Windows

First of all, follow the Windows instructions in [Defold Readme](../README.md)

* Start `msys.bat` as described
* Download the [lein.sh script](https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein) from [Leiningen.org](http://leiningen.org) Put it somewhere in your (msys) path - if you're lazy, put it in `C:\MinGW\msys\1.0\bin`. You might need to `chmod a+x lein.sh`.
* Run `lein` in the `editor` subdirectory
  This will attempt to download leiningen and dependencies to your home directory.

  - If this fails with message

          Could not find or load main class clojure.main

    Try pointing your `HOME` environment variable to your “windows home”. For instance change it from `/home/Erik.Angelin` (msys) to `/c/Users/erik.angelin`:

        export HOME="/c/Users/erik.angelin"

    The problem seems to be that the (windows) java class path points to an invalid home directory.

  - If this fails because the github certificate cannot be verified:

          export HTTP_CLIENT='wget --no-check-certificate -O'

### macOS/Linux

* Install Leiningen `brew install leiningen`

## Setup
* Open a terminal/console and change directory to `defold`.

    `cd defold`

* Setup the shell environment (consider putting it in an alias in your bash profile):

    `./scripts/build.py shell --platform=...`
    
* Run install_ext for the correct platform:
    
    `./scripts/build.py install_ext --platform=...`

* Build the engine:

    `./scripts/build.py build_engine --platform=... --skip-tests -- --skip-build-tests`
  
* Build builtins:

    `scripts/build.py build_builtins`
    
* Build Bob:

    `scripts/build.py build_bob --skip-tests`

* Change directory to the editor directory

    `cd editor`
    
* Run:

    `lein init`

## Cursive IDE Setup
Some of the remaining instructions are about EMACS. If instead you want to use the Cursive IDE, read the [Cursive setup guide](README_CURSIVE.md).

## Running Tests
`lein test` will run all the tests including the integration tests.

If you are using a repl, you can also run the tests by calling `(suite/suite)`.

## Setup NREPL for debugging

If you want to work on the editor chances are you want to `connect` or `jack-in` to a REPL as described below.

For this to work you will need a `~/.lein/profiles.clj` file and put the nREPL, Cider (etc) dependencies there;

```
{:user {:plugins [[cider/cider-nrepl "0.10.2"]
                  [refactor-nrepl "1.1.0" :exclusions [org.clojure/clojure]]]
        :dependencies [[org.clojure/tools.nrepl "0.2.12"]]}}
```

Please note that Lein will introduce a nREPL dependency automagically, but its a good idea to override to your preferred version here anyway.

## Running the Editor
`lein run` will launch the editor as well as providing a nrepl port
for you to jack into

## Building the Editor

Use `scripts/bundle.py` to produce a bundled version of the editor.

There are a few different scenarios in which you might want to build the editor locally:

- Local editor sources, archived engine artifacts based on HEAD:
  - `./scripts/bundle.py --platform=x86_64-win32 --version=1.2.3.4 --engine-artifacts=archived`
    - This will fetch engine and launcher artifacts using the `HEAD` revision.
- Local editor sources, archived engine artifacts based on a different revision:
  - `./scripts/bundle.py --platform=x86_64-win32 --version=1.2.3.4 --engine-artifacts=archived-stable`
    - This will fetch engine and launcher artifacts using the latest stable revision and is handy if you are on a branch where no engine artifacts have been archived.
- Local editor sources, local engine artifacts, local launcher:
  - `./scripts/bundle.py --platform=x86_64-win32 --version=1.2.3.4 --engine-artifacts=dynamo-home`
    - This will use local engine artifacts from `$DYNAMO_HOME`, with the exception of the launcher.
- Local editor sources, archived engine artifacts, local launcher:
  - `./scripts/bundle.py --platform=x86_64-win32 --version=1.2.3.4  --engine-artifacts=archived --launcher ../tmp/dynamo_home/bin/x86_64-darwin/launcher`
    - This will fetch engine artifacts using the `HEAD` revision.


## Jacking into a REPL

You can also use `M-x cider-jack-in` or launch the editor inside Cursive for debugging with breakpoints etc.

First set the environment variable `DYNAMO_HOME`. Example of a value `/Users/martin/work/defold/tmp/dynamo_home`.

After you jacked in do the following to load and start the app;

```
user=> (dev)
dev=> (go)
```

## Running Benchmarks
`lein benchmark` will run the benchmarks and put the results to the
`test/benchmark/bench-result.txt` file. Make sure to have everything
on your system closed down

## Generating the docs
Running `lein doc` will generate the codox to the target/docs directory

## Styling
A single stylesheet is set on the root node (by convention) in the scene. The stylesheet `editor.css` is loaded as a regular java resource, from the uberjar or from the file-system in dev-mode. If an `editor.css` is found in the current working directory that file will take precedence over the aforementioned java resource.

The stylesheet can be reloaded with the function key `F5`.

The `editor.css` stylesheet is generated from the the sass/scss files in `styling/stylesheets`. To generate the file you can use either leiningen or gulp:

**leiningen**

- `lein sass once` to generate once
- `lein sass watch` to watch and re-generate css on changes

**nodejs**

In the `styling` directory:
- `npm install`

- `gulp` to generate once
- `gulp watch` to watch and re-generate css on changes

See `styling/README.md` for details.


### JavaFX Styling

The best way to understand how JavaFX styling works is by studying the default stylesheet `modena.css` included in `jfxrt.jar`

## Bundling games and running in browser

As a temporary solution, we use Bob (from Editor1) as the content pipeline for bundling and running in the browser. In order to setup Bob locally, you need to:

- Build the engine for the specific platform, e.g. `python scripts/build.py build_engine --platform=js-web --skip-tests -- --skip-build-tests`
  - For android, you also need to `build_go` through `build.py` to obtain `apkc`
- Build Bob with local artifacts, `python scripts/build.py build_bob`
- `lein init`, which will install `bob.jar` as a local maven package


# Defold Editor Systems Overview

## Startup

The Defold editor runs on the Java Virtual Machine. We bundle our own JVM with the editor and use a simple launcher executable to boot the JVM with a particular set of command-line arguments. The entry point is in `editor/src/java/com/defold/editor/Main.java`, which starts up a JavaFx `Application` subclass. A splash screen is displayed while a custom `ClassLoader` loads all the classes required to show the Welcome dialog. While it is shown, the custom `ClassLoader` keeps loading the classes required by the editor on all available background threads while the user ponders which project to open. Once that happens we await loading of all the remaining classes, then proceed to loading the project from disk.

We load the entire set of editable project data into memory, but non-editable resources such as images can be loaded on demand. From this, we create the project graph, which represents the complete state of all editable resources in the project.

## Graphs, nodes and connections

The Defold editor uses a graph-based data model. A graph consists of nodes and connections between these nodes. Nodes can have inputs, outputs and properties. Inputs can be connected to outputs or properties on other nodes. Properties are used to store state in a node, whereas outputs are pure functions that may depend on other outputs, inputs and properties from the same node. Node types are declared using the `g/defnode` macro, and can inherit from one or more node types like in an object-oriented programming language.

When a node output is evaluated, the connections are followed upstream until all the dependencies are satisfied in a recursive manner. For example, the `:scene` output of a `ShapeNode` might depend on a `:texture-image` input in the same node that is connected to an `:image` output on a `PngImageNode`. In this case, evaluating `:scene` on the `ShapeNode` means that we'd first have to evaluate `:image` on the `PngImageNode`.

Doing all this evaluation can take time, so the editor makes extensive use of caching. The value produced from any output marked as `:cached` will be stored in a cache for fast retrieval the next time it is evaluated. Entries will be evicted from the cache if any of their upstream dependencies change.

The inputs and outputs are generally constrained to a particular data type, but any output can also produce a special type of value called an `ErrorValue`. Error values will be propagated and in a sense "infect" anything downstream of them in the graph. As the error propagates, its path in the graph is recorded until it eventually reaches an input that is able to handle it by specifying a `:substitute` function. Typically the error value reaches a some sort of view where the error can be presented in a meaningful way and allow the user to navigate to the source of the error. Beware that evaluating an output programmatically may return an unexpected `ErrorValue` as a result of a upstream connection in the graph.

## Transactions

The graph is modified by executing transactions using the `g/transact` function. It takes a (possibly nested) sequence of transaction steps and ensures all of them or none of them are applied. Each transaction step only performs a small operation like "Create Node", "Set Property" or "Add Connection". Larger operations like adding a component to a game object are composed of many small steps. It is possible to evaluate the graph on a background thread by obtaining an `evaluation-context`, which contains a snapshot of the graph state, and supplying it with all graph queries. However, running `g/transact` from a background thread is not allowed, so a background thread must post `g/transact` calls to the main thread using something like `ui/run-later`.

## Multiple graphs

There might be several graphs in play at any one time. Typically all the project data (i.e. "model" data) resides in one graph, whereas any number of views (as in user-interface elements) can have their own graphs whose node inputs are connected to the project graph. Closing a view discards the view graph but leaves the project graph intact.

The project graph has history enabled, which means that it keeps an ever-growing list of graph states that we append to every time an undoable action is performed. Undoing and redoing becomes a simple matter of pointing to one of the previous graph states. However, care must be taken when introducing state as any programmatic change to the project graph creates an undo step.

## The workspace

Alongside the project graph, there is also the workspace graph. The workspace is associated with the project directory on disk, and keeps track of files in the project. It does not host any of the editable state derived from those files - that is all in the project graph - rather, it keeps track of changes to the file system and is responsible for notifying the project graph of external modifications to the project files, etc. It also acts as a registry of the various resource types (effectively based on file extensions) and the recipes for loading each type of resource into the project graph.

## Resource sync

Whenever an operation may have modified the files in the project folder, we trigger what is known as a `resource-sync`. During a `resource-sync`, the workspace figures out what has happened and updates the project graph to reflect the updated state. Files may have been added or deleted, moved to new locations, or changed their contents entirely. In response to this we might re-create parts of the project graph related to the modified resources from scratch. Every file in the project has a corresponding `ResourceNode`, in the project graph. In addition, the `:load-fn` registered with the resource type in the workspace might create additional nodes for objects that reside in the file. If such a file needs to be reloaded from disk, we delete the old `ResourceNode` and any nodes connected to a `:cascade-delete`-marked input, recursively. We then create a new node in place of the old `ResourceNode` and re-run the `:load-fn`. This may create a new set of additional nodes based on the updated contents of the file. We then reconnect the outputs from the `ResourceNode` to whatever other node inputs the old `ResourceNode` was connected to.

At present, the node ids of the recreated substructure will not match up to the node ids from the old, deleted nodes. Objects might have been removed or added in a different order to the modified file, so it is difficult to retain the structure. Because of this, we must clear the undo history whenever this happens.

Deleted files are handled in a different fashion. Here the old `ResourceNode` remains in the graph along with all its connections to other nodes, but the resource node is marked as defective. In this state, its outputs are jammed with an `ErrorValue` denoting the file as missing. Anything that depends on the deleted resource will produce an `ErrorValue` referring to the deleted resource. If the deleted file reappears on disk, the defective resource node will be replaced with a non-defective one during `resource-sync`.

## View types

Along with resource types, view types are also registered with the workspace. Current examples of view types include a code editor used for textual editing and a scene view used to arrange visual elements. The same scene view component is used to edit collections, tile maps and texture atlases. It is also used to preview assets such as meshes and cube map textures.

Every resource type can support editing through one or more view types, specified when registering the resource type. The actual contract required to edit a resource using a particular view type is loosely defined by an expected set of properties and outputs on the edited `ResourceNode`. The `:make-view-fn` provided when registering a view type is responsible for connecting the view node to the edited resource. For example, a resource can be opened in the scene view if it includes the `:scene` type among its supported `:view-types` when the resource type is registered. When the view is created, the `:make-view-fn` connects the `:scene` output of the `ResourceNode` to the `:scene` input on the `SceneView` node. The scene is described as data, and the `ResourceNode` simply needs to provide the data required to render its scene.

## Scene view scenes

A scene is a map that typically contains a local transform, a local-space axis-aligned bounding box, a renderable, and optionally a vector of child scenes in the same vein. Each renderable specifies a render function and some user data that will be supplied to the render function along with render-args that include the world-space matrices and so on. Rendering is split into passes so that transparent elements can be blended correctly onto opaque elements, among other things. The renderables specify which passes it should be rendered to.

When rendered, the scene hierarchy is flattened so that each renderable obtains a world-space transform. The axis-aligned bounding boxes of parent scenes are expanded to include the transformed bounding boxes of child scenes. Similar renderables are batched together, and the render function is called with all the similar renderables in one go. Renderables can specify a `:batch-key` to control which renderables are batched together.

## The scene cache

Frequently there is a need to create an object that is managed by the OpenGL context. This can be done directly in the render function call if you've previously registered an object type with the scene cache system. You provide functions for creating, updating and destroying the OpenGL-managed objects, and can then request an object from the scene cache from directly within a render function. The scene cache will create an instance of the requested object if it had not been requested before, return the existing one if the parameters match, or update the existing one if they differ. If the render function does not request the object during a render call, it is pruned from the cache automatically. This way, users do not have to worry about explicitly creating or destroying OpenGL resources in response to scene changes.

## Save data

Any node that connects to the `:save-data` input of the project node will be saved when the user invokes the Save command. Each save data entry is a map with a `:resource` and a `:content` field. The content is an array of bytes that should be written to disk and the resource is a file in the project the data should be written to. Unless explicitly prevented from doing so, all resource nodes that correspond to editable project files will be automatically connected to the `:save-data` input of the project node when loaded. Typically resource nodes do not implement the `:save-data` output themselves, but depend on the implementation provided by the base `ResourceNode`. The subclassing `ResourceNode` instead provides a `:save-value` output and gets automatic dirty-state tracking by comparing against the save value obtained from the `:read-fn` registered with the resource type.

## Build targets

In order to run the game, the project resources must be compiled into a binary format for the engine runtime. In the graph, the "recipe" for such a binary output file is called a build target.

Build targets are maps containing all the necessary information to produce a binary from a particular resource that can be loaded by the engine runtime. Build targets specify an output build `:resource` (i.e. an output file below the build output folder for the resulting binary), a `:build-fn` that will be called to produce the resulting binary, and `:user-data` that will be given to the `:build-fn` as an argument. The build function is expected to return a map with the above build `:resource` and a `:content` field. The content is an array of bytes that should be written to disk at the location denoted by the build resource. The contents of the `:user-data` is hashed along with the build function and later used as a key in a disk-based build cache to speed up subsequent builds.

A build target can optionally specify a list of `:deps` - build targets that it depends on. The build functions of all dependent build targets will be run before the depending build target is built. The build system will also attempt to fuse equivalent build targets into a single build output file if possible in order to save memory in the running game. Each build function must update any internal references to the fused build resources from its dependent build targets by looking up the original resource in the `dep-resources` mapping provided to the build function as an argument.
