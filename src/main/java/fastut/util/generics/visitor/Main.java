package fastut.util.generics.visitor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fastut.util.generics.parser.SignatureParser;
import fastut.util.generics.tree.TypeSignature;
import fastut.util.generics.type.SignaturedType;

public class Main {

    public static void main(String[] args) {

        Reifier rf = Reifier.make();
        // Ljava/util/List<Ljava/util/List<Ljava/util/List<Ljava/util/List<Ljava/util/List<Ljava/lang/Integer;>;>;>;>;>;
        // Ljava/util/Map<Ljava/lang/String;Ljava/util/List<[Ljava/lang/Integer;>;>;
        // Ljava/util/List<TT;>;
        TypeSignature tree = SignatureParser.make().parseTypeSig("Ljava/util/Map;");
        tree.accept(rf);
        SignaturedType t = rf.getResult();
        System.err.println(t);
        Object ret = t.make();
        if (ret instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<Object, Object> mret = (HashMap<Object, Object>) ret;
            for (Map.Entry<Object, Object> m : mret.entrySet()) {
                System.err.println("KEY: " + m.getKey());
                Object v = m.getValue();
                if (v instanceof ArrayList) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> al = (ArrayList<Object>) v;
                    for (Object alo : al) {
                        if (alo.getClass().isArray()) {
                            int length = Array.getLength(alo);
                            for(int i = 0; i < length; ++i) {
                                System.err.println("V" + i + ":\t" + Array.get(alo, i));
                            }
                        } else {
                            System.err.println("V:\t" + alo);
                        }
                    }
                } else {
                    System.err.println("VALUE:\t" + m.getValue());
                }
            }
        } else {
            System.err.println(ret);
        }
    }
}
