package seals;

import am.SANOM;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import eu.sealsproject.platform.res.tool.api.ToolType;
import eu.sealsproject.platform.res.tool.impl.AbstractPlugin;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
            URI uri2 = new URI("file:./res/anatomy/human.owl");

            SANOM matcher = new SANOM();
            //Properties properties = new Properties();
            matcher.init(uri1, uri2);
			matcher.initSANOM(uri1, uri2);
            matcher.align( null, System.getProperties());
			File alignmentFile = File.createTempFile("alignment", ".rdf");
			FileWriter fw = new FileWriter(alignmentFile);
			PrintWriter pw = new PrintWriter(fw);
			AlignmentVisitor rendererVisitor = new RDFRendererVisitor(pw);
			matcher.render(rendererVisitor);
			fw.flush();
			fw.close();
            System.out.println(alignmentFile.toURI().toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
