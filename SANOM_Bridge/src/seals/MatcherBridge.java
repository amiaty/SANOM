/*
 * Copyright (c) 2017.
 *    * Unauthorized copying of this file  (MatcherBridge.java), via any medium is strictly prohibited
 *    * Proprietary and confidential
 *    * Written by :
 * 		Amir Ahooye Atashin - FUM <amir.atashin@mail.um.ac.ir>
 * 		Majeed Mohammadi - TBM <M.Mohammadi@tudelft.nl>
 *
 * 																						Last modified: 2017 - 9 - 6
 *
 */

package seals;

import static am.StringUtilsAM.*;
import am.SANOM;
import com.hp.hpl.jena.sparql.util.StringUtils;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import eu.sealsproject.platform.res.tool.api.ToolType;
import eu.sealsproject.platform.res.tool.impl.AbstractPlugin;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
public class MatcherBridge extends AbstractPlugin implements IOntologyMatchingToolBridge {

	/**
	* Aligns to ontologies specified via their URL and returns the 
	* URL of the resulting alignment, which should be stored locally.
	* 
	*/
	public URL align(URL source, URL target) throws ToolBridgeException {
		SANOM matcher = new SANOM();
		try {
			matcher.init(source.toURI(), target.toURI());
            matcher.initSANOM(source.toURI(), target.toURI());
			Properties properties = new Properties();
			properties.put("objType", "class");
			properties.put("nbIter", "1");
			matcher.align(null, properties);
            try {
                File alignmentFile = File.createTempFile("alignment", ".rdf");
                FileWriter fw = new FileWriter(alignmentFile);
                PrintWriter pw = new PrintWriter(fw);
                AlignmentVisitor rendererVisitor = new RDFRendererVisitor(pw);
                matcher.render(rendererVisitor);
                fw.flush();
                fw.close();
                return alignmentFile.toURI().toURL();
            }
            catch (IOException e) {
                throw new ToolBridgeException("cannot create file for resulting alignment", e);
            }
		} catch (AlignmentException | URISyntaxException e) {
            throw new ToolBridgeException("cannot convert the input param to URI as required");
		}
    }

	/**
	* This functionality is not supported by the tool. In case
	* it is invoced a ToolException is thrown.
	*/
	public URL align(URL source, URL target, URL inputAlignment) throws ToolBridgeException {
		throw new ToolException("functionality of called method is not supported");
	}

	/**
	* In our case the DemoMatcher can be executed on the fly. In case
	* prerequesites are required it can be checked here. 
	*/
	public boolean canExecute() {
		return true;
	}

	/**
	* The DemoMatcher is an ontology matching tool. SEALS supports the
	* evaluation of different tool types like e.g., reasoner and storage systems.
	*/
	public ToolType getType() {
		return ToolType.OntologyMatchingTool;
	}

	public static void main(String[] args)
	{
        try {
            URI uri1 = new URI("file:./res/anatomy/mouse.owl");
			//URI uri1 = new URI("file:./res/pheno/doid.owl");
			//URI uri1 = new URI("file:./res/ua/Cologne.rdf");

            URI uri2 = new URI("file:./res/anatomy/human.owl");
			//URI uri2 = new URI("file:./res/pheno/ordo.owl");
			//URI uri2 = new URI("file:./res/ua/Frankfurt.rdf");

            //boolean bb = am.StringUtilsAM.ContrainNumber("S4 Vertebra");
            //am.StringUtilsAM.StringSetDistance("s4 vertebra", "sacral vertebra 4");
			Properties properties = new Properties();
			properties.put("objType", "class");
			properties.put("nbIter", "2");

            SANOM matcher = new SANOM();
            matcher.init(uri1, uri2);
			matcher.initSANOM(uri1, uri2);


            matcher.align( null, properties);
			File alignmentFile = File.createTempFile("alignment", ".rdf");
			FileWriter fw = new FileWriter(alignmentFile);
			PrintWriter pw = new PrintWriter(fw);
			AlignmentVisitor rendererVisitor = new RDFRendererVisitor(pw);
			matcher.render(rendererVisitor);
			fw.flush();
			fw.close();
            System.out.println(alignmentFile.toURI().toURL());

            // eval
			AlignmentParser alignmentParser = new AlignmentParser(0);
			Alignment ref = alignmentParser.parse("file:./res/anatomy/reference.rdf");
			//Alignment ref = alignmentParser.parse("file:./res/ua/Cologne-Frankfurt.rdf");
			//Alignment ref = alignmentParser.parse("file:./res/pheno/DOID_ORDO.rdf");
			ref.init(uri1, uri2);
			ref.harden(0.01);
			Evaluator evaluator = new PRecEvaluator(ref, matcher);
			evaluator.eval(System.getProperties());

			OutputStream stream1 = System.out;
			PrintWriter printWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream1, "UTF-8" )),false);
			evaluator.write(printWriter1);
			printWriter1.flush();
			printWriter1.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
