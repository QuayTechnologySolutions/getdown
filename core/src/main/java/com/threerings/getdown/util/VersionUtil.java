//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/threerings/getdown/blob/master/LICENSE

package com.threerings.getdown.util;

import java.io.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.threerings.getdown.data.SysProps;
import static com.threerings.getdown.Log.log;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Version related utilities.
 */
public final class VersionUtil
{
    /**
     * Reads a version number from a file.
     */
    public static long readVersion (File vfile)
    {
        long fileVersion = -1;
        try (BufferedReader bin =
             new BufferedReader(new InputStreamReader(new FileInputStream(vfile), UTF_8))) {
            String vstr = bin.readLine();
            if (!StringUtil.isBlank(vstr)) {
                fileVersion = Long.parseLong(vstr);
            }
        } catch (Exception e) {
            log.info("Unable to read version file: " + e.getMessage());
        }

        return fileVersion;
    }

    /**
     * Writes a version number to a file.
     */
    public static void writeVersion (File vfile, long version) throws IOException
    {
        try (PrintStream out = new PrintStream(new FileOutputStream(vfile))) {
            out.println(version);
        } catch (Exception e) {
            log.warning("Unable to write version file: " + e.getMessage());
        }
    }

    /**
     * Parses {@code versStr} using {@code versRegex} into a (long) integer version number.
     * @see SysProps#parseJavaVersion
     */
    public static long parseJavaVersion (String versRegex, String versStr)
    {
        Matcher m = Pattern.compile(versRegex).matcher(versStr);
        if (!m.matches()) return 0L;

        long vers = 0L;
        for (int ii = 1; ii <= m.groupCount(); ii++) {
            String valstr = m.group(ii);
            int value = (valstr == null) ? 0 : parseInt(valstr);
            vers *= 100;
            vers += value;
        }
        return vers;
    }

    /**
     * Reads and parses the version from the {@code release} file bundled with a JVM.
     */
    public static long readReleaseVersion (File relfile, String versRegex)
    {
        try (BufferedReader in =
             new BufferedReader(new InputStreamReader(new FileInputStream(relfile), UTF_8))) {
            String line = null, relvers = null;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("JAVA_VERSION=")) {
                    relvers = line.substring("JAVA_VERSION=".length()).replace('"', ' ').trim();
                }
            }

            if (relvers == null) {
                log.warning("No JAVA_VERSION line in 'release' file", "file", relfile);
                return 0L;
            }
            return parseJavaVersion(versRegex, relvers);

        } catch (Exception e) {
            log.warning("Failed to read version from 'release' file", "file", relfile, e);
            return 0L;
        }
    }

    /**
     * Reads the version from the custom Getdown version file added to a JVM.
     */
    public static String readCustomJvmVersion (File versionFile)
    {
        Properties javaVersionProperties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(versionFile)) {
            javaVersionProperties.load(fileInputStream);
            return javaVersionProperties.getProperty("JAVA_VERSION");
        } catch (Exception e) {
            log.warning("Failed to read version from custom version file", "file", versionFile, e);
            return "";
        }
    }

    private static final String GETDOWN_VERSION_FILE_NAME = "getdownVersion.properties";
    /**
     * @return current running Getdown version. It is retrieved from "getdownVersion.properties",
     * which is built by Maven during packaging.
     */
    public static String getGetdownVersion() {
        final Properties properties = new java.util.Properties();

        final String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            final File getdownVersionFile = new File(javaHome + File.separator +
                "conf" + File.separator +  GETDOWN_VERSION_FILE_NAME);
            if (getdownVersionFile.exists()) {
                try (InputStream propertyStream = new FileInputStream(getdownVersionFile)) {
                    properties.load(propertyStream);
                    final String getdownVersion = properties.getProperty("getdownVersion");
                    if (getdownVersion != null && !getdownVersion.isEmpty()) {
                        return getdownVersion;
                    }
                } catch (IOException e) {
                    log.warning("Failed to read Getdown version from 'getdownVersion.properties' " +
                        "resource file in 'conf' folder ", e);
                }
            }
        }
        try {
            final ClassLoader classLoader = VersionUtil.class.getClassLoader();
            properties.load(classLoader.getResourceAsStream(GETDOWN_VERSION_FILE_NAME));
            return properties.getProperty("getdownVersion");
        } catch (IOException e) {
            log.warning("Failed to read Getdown version from 'getdownVersion.properties' resource file", e);
            return "";
        }
    }

    private static int parseInt (String str) {
        int value = 0;
        for (int ii = 0, ll = str.length(); ii < ll; ii++) {
            char c = str.charAt(ii);
            if (c >= '0' && c <= '9') {
                value *= 10;
                value += (c - '0');
            }
        }
        return value;
    }
}
