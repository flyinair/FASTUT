package fastut.evolution;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import fastut.coverage.data.ClassData;
import fastut.coverage.data.TouchCollector;
import fastut.generate.TestDataGenerator;

public class DependencyFitnessFunction extends FitnessFunction {

    private static final long   serialVersionUID = 5891009005406173159L;

    private MethodInvokeContext invokeContext;

    public DependencyFitnessFunction(MethodInvokeContext invokeContext){
        this.invokeContext = invokeContext;
    }

    @Override
    protected double evaluate(IChromosome a_subject) {
        TouchCollector.reset();
        TestDataGenerator.projectData.reset();

        int size = a_subject.size();
        int iSize = invokeContext.getGeneTypeSize();
        int gSize = size / iSize;
        GeneValueIterator geneIter = new GeneValueIterator(a_subject);
        for (int i = 0; i < gSize; ++i) {
            for (int j = 0; j < iSize; ++j) {
                if (invokeContext.isParam(j)) {
                    invokeContext.processParam(j, geneIter);
                    continue;
                }

                invokeContext.processField(j, geneIter);
            }
            invokeContext.tryInvoke();
        }

        TouchCollector.applyTouchesOnProjectData(TestDataGenerator.projectData);
        ClassData classData = TestDataGenerator.projectData.getClassData(invokeContext.getClassName());
        return classData.getBranchCoverageRate(invokeContext.getMethodSignature());
    }

}
