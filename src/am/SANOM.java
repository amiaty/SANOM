/*
 * Copyright (c) 2017.
 *    * Unauthorized copying of this file  (SANOM.java), via any medium is strictly prohibited
 *    * Proprietary and confidential
 *    * Written by :
 * 		Amir Ahooye Atashin - FUM <amir.atashin@mail.um.ac.ir>
 * 		Majeed Mohammadi - TBM <M.Mohammadi@tudelft.nl>
 *
 * 																						Last modified: 2017 - 9 - 6
 *
 */
package am;
import static am.StringUtilsAM.*;
import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.ontosim.string.JWNLDistances;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory;

import info.debatty.java.stringsimilarity.*;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;

import java.net.URI;
import java.util.*;

@SuppressWarnings("Duplicates")
public class SANOM extends DistanceAlignment implements AlignmentProcess {

    private HeavyLoadedOntology<Object> heavyOntology1;
    private HeavyLoadedOntology<Object> heavyOntology2;
    public SANOM() {
        heavyOntology1 = heavyOntology2 = null;
        setType("**");
    }

    public void init(Object o1, Object o2) throws AlignmentException {
        super.init(o1, o2);
    }
    public void initSANOM(URI o1, URI o2) {
        //dataFactory
        OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory");
        Ontology ontology = null;
        try {
            ontology = OntologyFactory.getFactory().loadOntology(o1);
            heavyOntology1 = (HeavyLoadedOntology<Object>)ontology;
            ontology = OntologyFactory.getFactory().loadOntology(o2);
            heavyOntology2 = (HeavyLoadedOntology<Object>)ontology;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {
            //JWNLDistances Dist = new JWNLDistances();
            //Dist.Initialize("./dict", "3.1");
            String p1 = param.getProperty("objType", "class");
            int nbIter = Integer.parseInt(param.getProperty("nbIter", "2"));
            int nbEntities1;
            int nbEntities2;
            OWLObject[] entity1o;
            OWLObject[] entity2o;
            switch (p1) {
                case "class":
                    nbEntities1 = heavyOntology1.nbClasses();
                    nbEntities2 = heavyOntology2.nbClasses();
                    entity1o = heavyOntology1.getClasses().toArray(new OWLObject[nbEntities1]);
                    entity2o = heavyOntology2.getClasses().toArray(new OWLObject[nbEntities2]);
                    break;
                case "property":
                    nbEntities1 = heavyOntology1.nbProperties();
                    nbEntities2 = heavyOntology2.nbProperties();
                    entity1o = heavyOntology1.getProperties().toArray(new OWLObject[nbEntities1]);
                    entity2o = heavyOntology2.getProperties().toArray(new OWLObject[nbEntities2]);
                    break;
                case "data_property":
                    nbEntities1 = heavyOntology1.nbDataProperties();
                    nbEntities2 = heavyOntology2.nbDataProperties();
                    entity1o = heavyOntology1.getDataProperties().toArray(new OWLObject[nbEntities1]);
                    entity2o = heavyOntology2.getDataProperties().toArray(new OWLObject[nbEntities2]);
                    break;
                case "object_property":
                    nbEntities1 = heavyOntology1.nbObjectProperties();
                    nbEntities2 = heavyOntology2.nbObjectProperties();
                    entity1o = heavyOntology1.getObjectProperties().toArray(new OWLObject[nbEntities1]);
                    entity2o = heavyOntology2.getObjectProperties().toArray(new OWLObject[nbEntities2]);
                    break;
                case "individual":
                    nbEntities1 = heavyOntology1.nbIndividuals();
                    nbEntities2 = heavyOntology2.nbIndividuals();
                    entity1o = heavyOntology1.getIndividuals().toArray(new OWLObject[nbEntities1]);
                    entity2o = heavyOntology2.getIndividuals().toArray(new OWLObject[nbEntities2]);
                    break;
                default:
                    nbEntities1 = heavyOntology1.nbEntities();
                    nbEntities2 = heavyOntology2.nbEntities();
                    entity1o = heavyOntology1.getEntities().toArray(new OWLObject[nbEntities1]);
                    entity2o = heavyOntology2.getEntities().toArray(new OWLObject[nbEntities2]);
                    break;
            }

            double[][] matrix = new double[nbEntities1][nbEntities2];
            List<Set<String>> entity1ss = new ArrayList<>(nbEntities1);
            List<Set<String>> entity2ss = new ArrayList<>(nbEntities1);
            String str1;


            for (OWLObject ob : entity1o) {
                Set<String> names = new HashSet<>();

                for (OWLAnnotationAssertionAxiom ob1 : ((OWLOntology)heavyOntology1.getOntology()).getAnnotationAssertionAxioms(((OWLClass) ob).getIRI())) {
                    if (ob1.getProperty().isLabel()) {
                        str1 = ((OWLLiteralImpl) ob1.getValue()).getLiteral();
                        names.add(str1.trim().replaceAll("_", " ").toLowerCase());
                    } else if (ob1.getProperty().toStringID().endsWith("hasRelatedSynonym")) {
                        str1 = ((OWLLiteralImpl) (((OWLOntology) heavyOntology1.getOntology()).getAnnotationAssertionAxioms((OWLAnnotationSubject) ob1.getValue()).iterator().next()).getValue()).getLiteral();
                        names.add(str1.trim().replaceAll("_", " ").toLowerCase());
                    }
                }
                if (names.size() <= 0) {
                    str1 = ob.getClassesInSignature().iterator().next().getIRI().getFragment();
                    names.add(str1.replaceAll("_", " ").toLowerCase());
                }
                entity1ss.add(names);
            }

            for (OWLObject ob : entity2o) {
                Set<String> names = new HashSet<>();
                for (OWLAnnotationAssertionAxiom ob1 : ((OWLOntology) heavyOntology2.getOntology()).getAnnotationAssertionAxioms(((OWLClass) ob).getIRI())) {
                    if (ob1.getProperty().isLabel()) {
                        str1 = ((OWLLiteralImpl) ob1.getValue()).getLiteral();
                        names.add(str1.trim().replaceAll("_", " ").toLowerCase());
                    } else if (ob1.getProperty().toStringID().endsWith("hasRelatedSynonym")) {
                        str1 = ((OWLLiteralImpl) (((OWLOntology) heavyOntology2.getOntology()).getAnnotationAssertionAxioms((OWLAnnotationSubject) ob1.getValue()).iterator().next()).getValue()).getLiteral();
                        names.add(str1.trim().replaceAll("_", " ").toLowerCase());
                    }
                }
                if (names.size() <= 0) {
                    str1 = ob.getClassesInSignature().iterator().next().getIRI().getFragment();
                    names.add(str1.replaceAll("_", " ").toLowerCase());
                }
                entity2ss.add(names);
            }

            LinkedList<NormalizedStringDistance> algos = new LinkedList<>();
            algos.add(new NormalizedLevenshtein());
            //algos.add(new JaroWinkler());
            //algos.add(new NGram(3));
            //algos.add(new Jaccard());
            //algos.add(new SorensenDice());
            //algos.add(new Cosine());

            System.out.println("Preparing:");

            int i, j;
            double m, step = 100.0 / nbEntities1, ii = step;

            // make similarity matrix
            for (i = 0; i < nbEntities1; ++i, ii += step) {
                for (j = 0; j < nbEntities2; ++j) {
                    m = 0;
                    for (String s1 : entity1ss.get(i)) {
                        boolean s1HasNum = StringUtilsAM.ContrainNumber(s1);
                        for (String s2 : entity2ss.get(j)) {
                            for (NormalizedStringDistance algo : algos) {
                                m = Math.max(m, 1.0 - algo.distance(s1, s2));
                            }
                            if (s1HasNum && StringUtilsAM.ContrainNumber(s2)) {
                                m = Math.max(m, StringUtilsAM.StringSetSimilarity(s1, s2));
                            }
                        }
                    }
                    matrix[i][j] = m;
                }
                System.out.print(String.format("\r%.0f%% completed!", ii));
            }

            List<Set<String>> supO1 = new ArrayList<>();
            List<Set<String>> supO2 = new ArrayList<>();
            double[][] matSup = null;
            if (Objects.equals(p1, "class")) {
                matSup = new double[nbEntities1][nbEntities2];
                HashMap<String, Integer> iriC1 = new HashMap<>(nbEntities1);
                HashMap<String, Integer> iriC2 = new HashMap<>(nbEntities2);


                for (i = 0; i < nbEntities1; ++i) {
                    iriC1.put(((OWLClass) entity1o[i]).getIRI().getFragment(), i);
                    Set<String> temp = new HashSet<>();
                    for (OWLObject ob : ((OWLClassImpl) entity1o[i]).getSuperClasses((OWLOntology) heavyOntology1.getOntology())) {
                        if (ob.getClass().toString().startsWith("class")) {
                            String iri = ob.getClassesInSignature().iterator().next().getIRI().getFragment();
                            if (!iri.endsWith("Thing"))
                                temp.add(iri);
                        }
                    }
                    supO1.add(temp);
                    //subO1.add(((OWLClassImpl)entity1o[i]).getSubClasses((OWLOntology) heavyOntology1.getOntology()));
                }

                for (i = 0; i < nbEntities2; ++i) {
                    iriC2.put(((OWLClass) entity2o[i]).getIRI().getFragment(), i);
                    Set<String> temp = new HashSet<>();
                    for (OWLObject ob : ((OWLClassImpl) entity2o[i]).getSuperClasses((OWLOntology) heavyOntology2.getOntology())) {
                        if (ob.getClass().toString().startsWith("class")) {
                            String iri = ob.getClassesInSignature().iterator().next().getIRI().getFragment();
                            if (!iri.endsWith("Thing"))
                                temp.add(iri);
                        }
                    }
                    supO2.add(temp);
                    //subO2.add(((OWLClassImpl)entity2o[i]).getSubClasses((OWLOntology) heavyOntology2.getOntology()));
                }
//                iriC1.put("Thing", -1);
//                iriC2.put("Thing", -1);
                double maxSim;
                for (i = 0; i < nbEntities1; ++i) {
                    for (j = 0; j < nbEntities2; ++j) {
                        maxSim = 0.0;
                        for (String ob1 : supO1.get(i)) {
                            int ind1 = iriC1.get(ob1);
                            if(ind1 == -1){
                                for (String ob2 : supO2.get(j))
                                    if(iriC2.get(ob2) == -1){
                                        maxSim = 1; break;
                                    }
                            } else {
                                for (String ob2 : supO2.get(j))
                                    maxSim = Math.max(maxSim, matrix[ind1][iriC2.get(ob2)]);
                            }
                            if(maxSim == 1) break;
                        }
                        matSup[i][j] = maxSim;
                    }
                }
            }

            System.out.println("\nRunning SA:");
            double threshold = 0.0;
            SimulatedAnnealing SA = new SimulatedAnnealing(matrix, matSup);
            SA.solve(nbIter);
            List<Pair<Integer, Integer>> result = SA.getSolution();
            System.out.println("\nSA finished.");
            for (Pair<Integer, Integer> item : result)
                if (matrix[item.getL()][item.getR()] >= threshold)
                    addAlignCell(entity1o[item.getL()], entity2o[item.getR()], "=", matrix[item.getL()][item.getR()]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}