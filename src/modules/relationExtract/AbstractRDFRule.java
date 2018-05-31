package modules.relationExtract;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;

import data.RDF.Namespace;
import data.id.Identifiable;

public abstract class AbstractRDFRule implements Identifiable{
	protected static final String prefixRDF = Namespace.RDF.toQueryPrefixDefinition();
	protected static final String prefixRDFS = Namespace.RDFS.toQueryPrefixDefinition();
	protected static final String prefixOWL = Namespace.OWL.toQueryPrefixDefinition();
	protected static final String prefixDC = Namespace.DC.toQueryPrefixDefinition();
	protected static final String prefixDCTERM = Namespace.DCTERMS.toQueryPrefixDefinition();
	protected static final String prefixSCHEMA = Namespace.SCHEMA.toQueryPrefixDefinition();
	protected static final String prefixJASS = Namespace.JASS.toQueryPrefixDefinition();
	protected static final String prefixGOO = Namespace.GOO.toQueryPrefixDefinition();
	protected static final String prefixSIO = Namespace.SIO.toQueryPrefixDefinition();
	

	/****************************************/
	/**********     Constructor    **********/
	/****************************************/


	/****************************************/
	/**********    Member Method   **********/
	/****************************************/
	public Model solves(Model model) {
		return QueryExecutionFactory.create(toConstructQuery(), model).execConstruct();
	}
	
	public Model expands(Model model) {
		return model.add(solves(model));
	}

	public Model converts(Model model) {
		return solves(model);
	}

	
	public abstract Query toConstructQuery();



	/****************************************/
	/**********  Interface Method  **********/
	/****************************************/


	/****************************************/
	/**********   Object  Method   **********/
	/****************************************/
	public abstract String toString();
}