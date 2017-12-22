# MyBatter - Formats your MyBatis mapper XML files

Are you using [MyBatis](http://www.mybatis.org/mybatis-3/) with its [mapper XML files](http://www.mybatis.org/mybatis-3/sqlmap-xml.html)?

Are you using [Eclipse](https://www.eclipse.org/) or any other IDE?

As a user of MyBatis and Eclipse you are probably also using [MyBatipse](https://github.com/mybatis/mybatipse) - a great Eclipse plugin to ease the use of MyBatis.

However, chances are good you have been looking for a way how to automatically format those mapper XML files.

So was I. But I haven't found any way to do it, even with the toolset mentioned above.

So I tried to do it on my own ... and here we go.

## Installation

Add MyBatter to your [Maven](https://maven.apache.org/) dependencies:

    <dependency>
      <groupId>de.dknapps.mybatter</groupId>
      <artifactId>mybatter</artifactId>
      <version>1.0-SNAPSHOT</version>
      <scope>test</scope>
    </dependency>

If you want to use a [snapshot version of MyBatter](https://oss.sonatype.org/#nexus-search;quick~mybatter) please allow snapshots from [Sonatype](https://oss.sonatype.org/):

	<profile>
		<id>allow-snapshots</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
		<repositories>
			<repository>
				<id>snapshots-repo</id>
				<url>https://oss.sonatype.org/content/repositories/snapshots</url>
				<releases>
					<enabled>false</enabled>
				</releases>
				<snapshots>
					<enabled>true</enabled>
				</snapshots>
			</repository>
		</repositories>
	</profile>


Run `mvn install` on your project at least once after adding MyBatter to your `pom.xml`. This downloads MyBatter to your local repository. Afterwards you can run MyBatter from there.

## Format mapper XML files automatically (in Eclipse)

Open you project properties. Use `Builder > New` to add a new builder. Choose `Program` and press `OK`.

In `Main` set `Location` to

    <your java home>/bin/java

In `Main` set `Arguments` to

    <your local maven repository>/de/dknapps/mybatter/mybatter/1.0-SNAPSHOT/mybatter-1.0-SNAPSHOT-jar-with-dependencies.jar ${project_loc}/src/main/java/<path to your mapper XML files>

In `Refresh` enable `Refresh resources upon completion` and choose `Specify resources` with your mapper XML files as specified resources.

In `Build Options` enable `After a 'Clean'`, `During manual builds` and `During auto builds` and choose `Specify working set of relevant resources` with your mapper XML files as specified resources.

## Available options

    usage: java -jar mybatter-<version>-jar-with-dependencies.jar [options] [
           file|directory [ file|directory ] ... ] [-e <encoding>] [-g
           <pattern>] [-h] [-s] [-v]
     -e,--encoding <encoding>   encoding to be used when reading and writing
                                the files (default is UTF-8)
     -g,--glob <pattern>        in directories format only files with names
                                matching the pattern (default is *.xml), be
                                sure to escape the pattern according to your
                                environment
     -h,--help                  print this message
     -s,--silent                suppress all messages, ignores verbose option
     -v,--verbose               print names and number of formatted files

## License

MyBatter is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
