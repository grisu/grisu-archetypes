grisu-archetypes
==============

This packages provides a maven archetype which support developer by creating stub maven projects which can be used as a starting point when trying to develop a new grid client (using the [Grisu client library](https://github.com/grisu/grisu/wiki/Grisu-client-library)).

Prerequisites
---------------------

In order to build grisu-archetypes from the git sources, you need: 

- Java (version >= 6)
- [Apache Maven](http://maven.apache.org) (version >=2)


Usage
---------

### Creating the project stub ###

To create a new grid client project, we need to execute the maven archetype:generate goal:

#### Java client stub ####

For the current snapshot release of Grisu:

    mvn archetype:generate -DarchetypeGroupId=grisu.frontend -DarchetypeArtifactId=grisu-client-archetype -DarchetypeVersion=0.5-SNAPSHOT -DgroupId=your.project -DartifactId=projectName -DarchetypeRepository=http://code.ceres.auckland.ac.nz/nexus/content/groups/public-snapshots/

For the stable version of Grisu:

   (later)

#### Groovy client stub ####

For the current snapshot release of Grisu:

    mvn archetype:generate -DarchetypeGroupId=grisu.frontend -DarchetypeArtifactId=grisu-groovy-client-archetype -DarchetypeVersion=0.5-SNAPSHOT -DgroupId=your.project -DartifactId=projectName -DarchetypeRepository=http://code.ceres.auckland.ac.nz/nexus/content/groups/public-snapshots/

For the stable version of Grisu:

   (later)

### Working with the (Java) client stub ###

We'll be working with the java client stub here, for the groovy client have a look here (later).

Use your own values for the groupId and artifactId keys.

This should give you a project directory that looks like:

    ./src
    ./src/pkg
    ./src/pkg/data
    ./src/pkg/data/projectName
    ./src/pkg/control
    ./src/pkg/control/control
    ./src/main
    ./src/main/java
    ./src/main/java/your
    ./src/main/java/your/project
    ./src/main/java/your/project/swing
    ./src/main/java/your/project/swing/ExampleJobCreationPanel.java
    ./src/main/java/your/project/swing/SwingClient.java
    ./src/main/java/your/project/Client.java
    ./src/main/resources
    ./src/main/resources/logback.xml
    ./src/main/resources/projectName.version
    ./client.assembly.xml
    ./pom.xml

    
### Building the project ###

We need to change into the project directory. Then we can tell maven to build and assemble an executable jar file for us (the first run might take quite a while, don't worry, it'll be faster in subsequent runs):

    cd projectName
    mvn clean install
    
should give us those two artifacts (for the commandline version) in the `target` directory:

    projectName-1.0-SNAPSHOT.jar
    projectName-binary.jar
    
### Running the client ###

    java -jar target/projectName-binary.jar <some_input_text_file>
    
This should create and submit a simple "cat" job to the NeSI testbed grid that uses the specified input file, uploads it to the grid and then prints out its content. Have a look in the Client.java class under src/main/java/your/project/ to see how that's done.

This example also builds a very simple example Swing client which submits a "echo hello world" job after the user presses a button. Try it out using:

    java -cp target/projectName-binary.jar your.project.swing.SwingClient

To see what's going on behind the scene (for both of those cases), you can tail the log file that's written to $HOME/.grisu/projectName.debug

### Building rpm & deb packages

The maven project we just created using the Grisu client archetypes also makes it very easy to create Debian and/or RedHat packages. The only thing you have to do is replace the mvn command from above with:

    mvn clean install -P deb,rpm

(for the rpm-creation to work the 'rpm' executable needs to be installed on the system)

The created packages will be

    ./target/projectName-1.0-SNAPSHOT.deb
    ./target/rpm/projectName/RPMS/noarch/projectName-1.0-SNAPSHOT<timestamp>.noarch.rpm

After installation, you should be able to start your client using

    projectName <optional arguments>


### Adding functionality ###

So, the client stub that comes with the maven archetype only submits a very simple echo job. Let's say we want to submit a job that diffs two input text files and we also want the user to be able to select a specific submission location.

If you are not quite sure how to use the client library, first have a look here: [GrisuClientSnippets](https://github.com/grisu/grisu/wiki/GrisuClientSnippets). This contains a growing list of code snippets that show how to use certain features of the grisu client library.

In the following, you have to edit the file src/main/java/your/project/Client.java.

#### Changing the application (to diff) ####

Since we need the user to specify 2 text files, we parse the commandline arguments with which the job was started and assume they are urls to the two input files. Then we change the part of the code that creates the JobObject like this:

    String file1url = args[0];
    String file1Name = FileManager.getFilename(file1url);
    String file2url = args[1];
    String file2Name = FileManager.getFilename(file2url);
    
    System.out.println("Creating job...");
    JobObject job = new JobObject(si);
    job.setApplication("UnixCommands");
    job.setTimestampJobname("MyFirstDiffJob");
    System.out.println("Set jobname to be: "+job.getJobname());
    job.setCommandline("diff "+file1Name+" "+file2Name);
    		
    // now we need to add the input files to the job
    job.addInputFileUrl(file1url);
    job.addInputFileUrl(file2url);

We could check first whether the two urls the user specifed are valid and the files they describe exists, but for this example we omit this step. The user can specify remote urls/paths (like: gsiftp://ng2.vpac.org/home/grid-startup/whatever as well as local ones (file:///home/markus/test/tmp.txt as well as /home/markus/test/tmp.txt.

#### Asking the user where to submit the job to

This uses one of the information objects you can get from the GrisuRegistryManager. Here's how to do it:

    // let's get an object that contains all the information about the application on the grid
    ApplicationInformation appInfo = GrisuRegistryManager.getDefault(si).getApplicationInformation("UnixCommands");
    // we don't care about the version here. it's possible to get that kind of information for a specific version too...
    Set<String> allSubmissionLocations = appInfo.getAvailableSubmissionLocationsForFqan("/ARCS/Startup");
    		
    // now we ask the user (on the commandline) which submission location to use
    // we give him the option of not specifing one, in which case we rely on grisu to figure out the best one
    String subLoc = CliHelpers.getUserChoice(allSubmissionLocations, "Auto-select the best one");
    
    if ( StringUtils.isNotBlank(subLoc) ) {
       job.setSubmissionLocation(subLoc);
    }

The rest of the code stays the same. Build and re-run the client as per above.


