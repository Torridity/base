/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.util.update;

import edu.kit.dama.util.update.parameters.UpdateParameters;
import edu.kit.dama.util.update.types.JavaLibrary;
import edu.kit.dama.util.update.types.LibraryDiffInformation;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.jcommander.generic.status.Status;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.LoggerFactory;

/**
 * Helper tool to bundle update packages for a previous release of a software
 * product without having to ship all libraries again and without having two
 * versions of the same library at the end. There are two outputs:
 *
 * <ul>
 * <li>A directory containing all libraries that are new/updated in the new
 * release.</li>
 * <li>A script that removed any old version of a new library.</li>
 * </ul>
 *
 * In order to get these two outputs the workflow is as follows:
 *
 * <ul>
 * <li>Obtain all jar files from directory A (all libraries of old release) and
 * directory B (all libraries from new release)</li>
 * <li>Obtain library names and versions by the following pattern:
 * &lt;LIBRARY_NAME&gt;-&lt;VERSION&gt;.jar</li>
 * <li>Compare both lists and...</li>
 * <li>
 * <ul>
 * <li>...skip identical libraries with same version.</li>
 * <li>...copy new libraries (not in list A) to directory C (final output
 * folder).</li>
 * <li>...copy updated libraries (in list A and B) to directory C (final output
 * folder) and plan removal of previous version(s).</li>
 * </ul>
 * </li>
 * <li>Write the cleanup script containing all planned removals.</li>
 * </ul>
 *
 * Now, for an update, the removal script has to be executed first to cleanup
 * old libraries. Afterwards, the new and updated libraries could be extracted.
 * Libraries which where added manually are not affected and remain the same
 * (must be updated separately)
 *
 * @author mf6319
 */
public class JarDiff {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JarDiff.class);
  private final static List<File> invalidFiles = new ArrayList<>();
  private final static FileFilter jarFilter = new FileFilter() {

    @Override
    public boolean accept(File pFile) {
      return pFile.getName().endsWith(".jar");
    }
  };

  /**
   * Helper method to obtain all java libraries from pLibFolder.
   * <b>Attention:</b> Only files directly located in this folder are checked.
   * There is no recursive traversing through subfolders.
   *
   * Obtained files will be parsed by the {@link JavaLibrary} implementation and
   * checked whether version information could be obtained or not. If not, a
   * warning will be logged and the file is ignored. There is also a check for
   * duplicated libraries with different versions. If a duplicate was found a
   * warning is logged and the first version is used.
   *
   * @param pLibFolder The folder that will be searched for library files.
   *
   * @return A map containing all library names and the according
   * {@link JavaLibrary} object.
   */
  private static Map<String, JavaLibrary> obtainLibraries(File pLibFolder) {
    LOGGER.debug("Obtaining file listing for folder {}", pLibFolder);
    Map<String, JavaLibrary> libraryVersionMap = new TreeMap<>();
    File[] libraries = pLibFolder.listFiles(jarFilter);
    for (File libFile : libraries) {
      LOGGER.debug("Parsing library information from file {}", libFile);
      JavaLibrary javaLib = new JavaLibrary(libFile);
      if (!javaLib.isValid()) {
        LOGGER.warn("Unable to obtain version information from file {}. Ignoring library.", libFile);
        invalidFiles.add(libFile);
        continue;
      }
      LOGGER.debug("Library information successfully parsed. Checking for duplicates.");
      JavaLibrary existingLib = libraryVersionMap.get(javaLib.getName());
      if (existingLib != null) {
        LOGGER.warn("Library {}  does already exist in another version ({}). Ignoring new version.", javaLib, existingLib);
      } else {
        //add jar
        LOGGER.debug("Adding new library to result map.");
        libraryVersionMap.put(javaLib.getName(), javaLib);
      }
    }
    return libraryVersionMap;
  }

  /**
   * Compare two maps generated by {@link #obtainLibraries(java.io.File)} and
   * store differences in the provided {@link LibraryDiffInformation} object.
   *
   * @param pCurrentVersionLibraries The map containing all libraries of the
   * current version of the software to update.
   * @param pNewVersionLibraries The map containing all libraries of the new
   * version of the software to update.
   */
  private static void compareLibraryMaps(Map<String, JavaLibrary> pCurrentVersionLibraries, Map<String, JavaLibrary> pNewVersionLibraries, LibraryDiffInformation pDiffInfo) {
    Set<Entry<String, JavaLibrary>> nextVersionEntries = pNewVersionLibraries.entrySet();

    for (Entry<String, JavaLibrary> entry : nextVersionEntries) {
      JavaLibrary libEntry = entry.getValue();
      LOGGER.debug("Checking new version entry {}", libEntry);
      JavaLibrary previousVersionEntry = pCurrentVersionLibraries.get(entry.getKey());
      if (previousVersionEntry == null) {
        //new
        pDiffInfo.addAddedEntry(libEntry);
      } else {
        if (previousVersionEntry.getVersion().equals(libEntry.getVersion()) && Boolean.valueOf(previousVersionEntry.isSnapshot()).equals(libEntry.isSnapshot())) {
          //same
          pDiffInfo.addUnchangedEntry(libEntry);
        } else {
          //new release version
          pDiffInfo.addChangedEntry(libEntry);
          pDiffInfo.addDeprecatedEntry(previousVersionEntry);
        }
        //check for snapshot versions in new version
        if (libEntry.isSnapshot()) {
          //snapshot
          LOGGER.warn("Updated library {} seems to be a SNAPSHOT version.", libEntry);
          pDiffInfo.addSnapshot();
        }
      }
    }
  }

  /**
   * Update command implementation.
   *
   * @param params The commandline params.
   *
   * @return The CommandStatus.
   */
  public static CommandStatus update(UpdateParameters params) {
    LOGGER.debug("Starting JarDiff");

    File libsPreviousVersion = params.currentDirectory;
    File libsNextVersion = params.newDirectory;
    File scriptFolder = params.scriptDestination;
    LOGGER.debug("Obtaining library information.");
    LibraryDiffInformation diffInfo = new LibraryDiffInformation();
    Map<String, JavaLibrary> currentVersionLibMap = obtainLibraries(libsPreviousVersion);
    Map<String, JavaLibrary> newVersionLibMap = obtainLibraries(libsNextVersion);

    LOGGER.debug("Comparing library maps.");
    compareLibraryMaps(currentVersionLibMap, newVersionLibMap, diffInfo);
    LOGGER.debug("Checking for manually added libraries.");
    Set<Entry<String, JavaLibrary>> currentVersionEntries = currentVersionLibMap.entrySet();
    for (Entry<String, JavaLibrary> entry : currentVersionEntries) {
      JavaLibrary libEntry = entry.getValue();
      LOGGER.debug("Checking entry {} from current version.", libEntry);
      JavaLibrary nextVersionEntry = newVersionLibMap.get(libEntry.getName());
      if (nextVersionEntry == null) {
        //unused or custom
        LOGGER.debug("Found manually added library {}", libEntry);
        diffInfo.addUnknownEntry(libEntry);
      }
    }
    LOGGER.debug("Adding unparsable libraries to final result.");
    diffInfo.setInvalidEntries(invalidFiles);

    if (scriptFolder == null) {
      LOGGER.debug("No script output folder provided. Just printing changes to stdout.");
      System.out.println(diffInfo);
      return new CommandStatus(Status.SUCCESSFUL);
    }else{
        LOGGER.debug("Writing update script 'update.sh' to output folder '{}'.", scriptFolder);
    }

    try {
      UpdateScriptGenerator.generateUpdateScript(scriptFolder, libsPreviousVersion, diffInfo);
    } catch (IOException ex) {
      LOGGER.error("Failed to write update script.", ex);
      return new CommandStatus(Status.FAILED, ex, null);
    }

    return new CommandStatus(Status.SUCCESSFUL);
  }

}
