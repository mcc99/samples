Glad you took up the challenge and read this. :)

To build a .jar from within IntelliJ, due to the manifest copy-over bug, you need to follow these
steps (ver: IntelliJ IDEA Ultimate 2017.2.5):

1. File...Project Structure...Project Settings : Artifacts
2. Click green + sign at top in middle column.  Choose Add -> JAR -> From modules with dependencies
3. Accept the default filled-in values except for one.  Change the Manifest File path so that it refers
   to the file in src/main/resources/META-INF, not the default path, which points to a non-existent file
   in src/main/java, then click Apply, then close the dialog.
4. Choose Build...Build Artifacts... then in the pop-up, choose the name of the artifact and choose Build.

For subsequent builds, choose Re-build to overwrite the current .jar file.

Default output directory for the .jar file is:
<codebase path>\serviceendpointtests\serviceendpointtests-blessed\out\artifacts\windstream_restservice_integration_tests_jar

It builds with all dependent .jars in it.  You can tweak this to your tastes but this is easiest because
you need not deal with specifying a -classpath value in the command line.

To utilize the test application as a self-contained .jar, and assuming you are using default paths and files,
at the .jar location, create or copy from the IntelliJ project the following paths and files to your host/local machine:

baseline/<the various baseline files in the project>
main/resources/baseserviceendpointtest.properties
main/resources/log4j.properties
test/conf/testng.xml

Per the notes in ServiceEndpointTestsRunner.java above main() you can see that the test program
can via params passed into it be customized in terms of what and where of the above files.  Locations
and content are all changeable to suit your needs.  The key thing to remember is that *what* test
classes get run are determined by the content of test/conf/ (ie, the testng .xml files) that are indicated
either by default value or passed-in file location and name value.  The default or user-specified test files
are read and the named test classes are then executed.

By setting up different jobs with different parameters in a scheduler like Jenkins, each job with its
own specified values in the  "java -jar" command to run it, the testing application can be run multiple
times simultaneously via the same scheduler with different values as specified in the .properties
files and .xml TestNG config files, so they can be pointed at different servers with the tests
for each job running in their own VM all at the same time testing different hosts, using their own specified
.properties files and generating their own separate log files.

Usage examples:

1. "/some/path/jre1.8.0_121/bin/java" -jar "/some/path/windstream-restservice-integration-tests.jar"
2. "/some/path/jre1.8.0_121/bin/java" -jar "/some/path/windstream-restservice-integration-tests.jar" null "/some/path/log4j_1.properties" "/some/path/testng_1.xml"
3. "/some/path/jre1.8.0_121/bin/java" -classpath "/some/path/windstream-restservice-integration-tests.jar" ServiceEndpointTestsRunner null "/some/path/log4j_1.properties" "/some/path/testng_1.xml"
4. "/some/path/jre1.8.0_121/bin/java" -jar "/some/path/windstream-restservice-integration-tests.jar" null "/some/path/log4j_1.properties" "/some/path/testng_1.xml,/some/path/testng_2.xml"

ServiceEndpointTestsRunner is the default class that the .jar is run with based on the content of
src/main/resources/META-INF/MANIFEST.MF so in the case of the third example above, using
"ServiceEndpointTestsRunner" in the command line is redundant.  The 2nd and 3rd command lines are
functionally equivalent.  The 4th command example shows how you would specify the processing of multiple
testNG .xml run files by passing in a CSV list of them.