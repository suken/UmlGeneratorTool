/**
 *
 */
package com.uml.generator;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author SShah
 */
public class UmlGeneratorUtility {

    public static List<String> getClasses(File directory, boolean includeTests) {
        List<String> classes = new ArrayList<String>();
        File[] directories = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        File[] files = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(".class");
            }
        });

        for (File file : files) {
            if (includeTests || !file.getName().endsWith("Test.class")) {
                String className = file.getName().replace('/', '.').replace(".class", "");
                classes.add(className);
            }
        }

        for (File dir : directories) {
            classes.addAll(getClasses(dir, includeTests));
        }
        return classes;
    }

    public static boolean isIncluded(String className, boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
        if (!hasAnyPatterns) {
            return true;
        }

        // check exclude patterns
        for (String patternText : excludePatterns) {
            if (!patternText.isEmpty()) {
                Pattern pattern = Pattern.compile(patternText);
                Matcher matcher = pattern.matcher(className);
                if (matcher.matches()) {
                    return false;
                }
            }
        }

        // check for include pattern
        for (String patternText : includePatterns) {
            if (!patternText.isEmpty()) {
                Pattern pattern = Pattern.compile(patternText);
                Matcher matcher = pattern.matcher(className);
                if (matcher.matches()) {
                    return true;
                }
            }
        }
        // by default the artifact is not included
        return false;
    }
}
