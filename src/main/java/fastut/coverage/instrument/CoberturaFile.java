package fastut.coverage.instrument;

import java.io.File;

import fastut.coverage.util.ArchiveUtil;

/**
 * This represents a regular File, but unlike java.io.File, the baseDir and relative pathname used to create it are
 * saved for later use.
 *
 * @author John Lewis
 */
class CoberturaFile extends File {

    private static final long serialVersionUID = 0L;

    private String            baseDir;
    private String            pathname;

    CoberturaFile(String baseDir, String pathname){
        super(baseDir, pathname);
        this.baseDir = baseDir;
        this.pathname = pathname;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String getPathname() {
        return pathname;
    }

    /**
     * @return True if file has an extension that matches one of the standard java archives, false otherwise.
     */
    boolean isArchive() {
        if (!isFile()) {
            return false;
        }
        return ArchiveUtil.isArchive(pathname);
    }

    /**
     * @return True if file has "class" as its extension, false otherwise.
     */
    boolean isClass() {
        return isFile() && pathname.endsWith(".class");
    }

    public String toString() {
        return "pathname=" + pathname + " and baseDir=" + baseDir;
    }

}
