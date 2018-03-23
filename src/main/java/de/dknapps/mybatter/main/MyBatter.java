/*
 * MyBatter - Formats your MyBatis mapper XML files
 *
 *     Copyright (C) 2017-2018 Uwe Damken
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dknapps.mybatter.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;

import de.dknapps.mybatter.formatter.Formatter;

/**
 * Contains main method to handle command line input and call internal classes respectively.
 */
public class MyBatter {

	/** Encoding to be used when reading an writing files */
	private static final String DEFAULT_ENCODING = "UTF-8";

	/** Only files with a name matching this pattern get formatted in directories */
	private static final String DEFAULT_GLOB = "[!~]*.xml";

	/** Stream to print messages to that shall go to /dev/null */
	private static final PrintStream NULL_PRINT_STREAM = new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM);

	/** Stream to print standard output messages to */
	private static PrintStream stdout = NULL_PRINT_STREAM;

	/** Stream to print error output messages to */
	private static PrintStream stderr = System.err;

	public static void main(String[] args) {
		Options options = createOptions();
		try {
			CommandLine commandLine = new DefaultParser().parse(options, args);
			List<String> pathList = commandLine.getArgList();
			if (commandLine.hasOption("h") || CollectionUtils.isEmpty(pathList)) {
				printHelp(options);
			} else {
				String encoding = commandLine.getOptionValue("e", DEFAULT_ENCODING);
				String suffix = commandLine.getOptionValue("g", DEFAULT_GLOB);
				boolean keep = commandLine.hasOption("k");
				boolean override = commandLine.hasOption("o");
				if (commandLine.hasOption("s")) {
					stderr = NULL_PRINT_STREAM;
				} else if (commandLine.hasOption("v")) {
					stdout = System.out;
				}
				pathList.stream()
						.forEach(path -> formatFileOrDirectory(path, suffix, encoding, keep, override));
			}
		} catch (ParseException e) {
			stderr.println(e.getLocalizedMessage());
			printHelp(options);
		}
	}

	/**
	 * Creates all options usable on the command line.
	 */
	private static Options createOptions() {
		Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help").desc("print this message").build());
		options.addOption(Option.builder("e").longOpt("encoding").hasArg().argName("encoding")
				.desc("encoding to be used when reading and writing the files (default is " + DEFAULT_ENCODING
						+ ")")
				.build());
		options.addOption(Option.builder("g").longOpt("glob").hasArg().argName("pattern")
				.desc("in directories format only files with names matching the pattern (default is "
						+ DEFAULT_GLOB + "), be sure to escape the pattern according to your environment")
				.build());
		options.addOption(Option.builder("k").longOpt("keep")
				.desc("keep original files, start names of formatted files with a ~").build());
		options.addOption(Option.builder("o").longOpt("override")
				.desc("override backup files silently (names start with a ~)").build());
		options.addOption(Option.builder("s").longOpt("silent")
				.desc("ignore verbose option and suppress all messages, even error messages").build());
		options.addOption(Option.builder("v").longOpt("verbose")
				.desc("print names and number of formatted files").build());
		return options;
	}

	/**
	 * Prints the command line help.
	 */
	private static void printHelp(Options options) {
		new HelpFormatter().printHelp(
				"java -jar mybatter-<version>-jar-with-dependencies.jar [options] [ file|directory [ file|directory ] ... ]",
				options, true);
	}

	/**
	 * Formats the given file or all files in the given directory.
	 * 
	 * @param glob
	 *            The pattern according to {@link Files#newDirectoryStream(Path, String)} which in turn links
	 *            to {@link FileSystem#getPathMatcher(String)}
	 * @param encoding
	 *            encoding to be used when reading and writing the files
	 * @param keep
	 *            True to keep original files, start names of formatted files with a ~
	 * @param override
	 *            True to override backup files silently (names starting with a ~)
	 * @param path
	 *            The file or directory.
	 * 
	 * @throws IOException
	 */
	private static void formatFileOrDirectory(String fileOrDirectory, String glob, String encoding,
			boolean keep, boolean override) {
		try {
			Path path = Paths.get(fileOrDirectory);
			File file = path.toFile();
			if (file.isFile()) {
				formatFile(file, encoding, keep, override);
			} else if (file.isDirectory()) {
				int formattedFilesCount = 0;
				for (Path filePath : Files.newDirectoryStream(path, glob)) {
					formatFile(filePath.toFile(), encoding, keep, override);
					formattedFilesCount++;
				}
				stdout.println(formattedFilesCount + " file(s) formatted");
			} else {
				stderr.println(fileOrDirectory + " is neither a file nor a directory");
			}
		} catch (IOException e) {
			stderr.println("Cannot format '" + fileOrDirectory + "': " + e.getLocalizedMessage());
		}
	}

	/**
	 * Formats the given file.
	 * 
	 * @param file
	 *            The file.
	 * @param encoding
	 *            encoding to be used when reading and writing the files
	 * @param keep
	 *            True to keep original file, start name of formatted file with a ~
	 * @param override
	 *            True to override backup file silently (name starting with a ~)
	 * @throws IOException
	 */
	private static void formatFile(File file, String encoding, boolean keep, boolean override) {
		try {
			stdout.println(file.getAbsolutePath());
			String input = FileUtils.readFileToString(file, encoding);
			String output = new Formatter().format(input);
			File backup = new File(file.getParent(), "~" + file.getName());
			if (backup.exists() && !override) {
				stderr.println(
						"Cannot format '" + file.getAbsolutePath() + "' into '" + backup.getAbsolutePath()
								+ "' because it already exists, use option -o to silently delete it");
			} else {
				backup.delete();
				FileUtils.write(backup, output, encoding);
				if (!keep) {
					file.delete();
					backup.renameTo(file);
				}
			}
		} catch (IOException e) {
			stderr.println("Cannot format '" + file.getAbsolutePath() + "': " + e.getLocalizedMessage());
		}
	}

}
