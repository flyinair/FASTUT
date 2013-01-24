package fastut.coverage.data;

public interface CoverageData
{

    double getBranchCoverageRate();

    double getLineCoverageRate();

    int getNumberOfCoveredBranches();

    int getNumberOfCoveredLines();

    int getNumberOfValidBranches();

    int getNumberOfValidLines();

    /**
     * Warning: This is generally implemented as a
     * "shallow" merge.  For our current use, this
     * should be fine, but in the future it may make
     * sense to modify the merge methods of the
     * various classes to do a deep copy of the
     * appropriate objects.
     */
    void merge(CoverageData coverageData);

    void reset();
}
