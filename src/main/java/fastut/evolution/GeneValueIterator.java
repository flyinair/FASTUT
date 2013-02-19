package fastut.evolution;

import org.jgap.IChromosome;

public class GeneValueIterator {

    private final IChromosome a_subject;
    private int               pos = 0;

    public GeneValueIterator(IChromosome a_subject){
        this.a_subject = a_subject;
    }

    public Object next() {
        Object next = a_subject.getGene(pos).getAllele();
        pos++;
        return next;
    }
}
