package modules.relationExtract;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import data.RDF.MyResource;
import data.RDF.RDFTriple;
import data.id.IDTuple;
import data.id.ModelIDMap;
import data.id.SentenceIDMap;
import data.id.StatementIDMap;

public class RelationExtractor {
	/**
	 * 拡張ルール
	 */
	private final RDFRules extensionRules;
	/**
	 * オントロジー変換ルール
	 */
	private final RDFRules ontologyRules;
	
	/****************************************/
	/**********     Constructor    **********/
	/****************************************/
	public RelationExtractor(RDFRules extensionRules, RDFRules ontologyRules) {
		this.extensionRules = extensionRules;
		this.ontologyRules = ontologyRules;
	}
	public RelationExtractor() {
		this(RDFRuleReader.readNewRDFRules(Paths.get("resource/rule/extensionRules.txt")), 
				RDFRuleReader.readNewRDFRules(Paths.get("resource/rule/ontologyRules.txt")));
	}
	

	/****************************************/
	/**********   Member  Method   **********/
	/****************************************/
	public ModelIDMap convertMap_Sentence2JASSModel(SentenceIDMap sentenceMap) {
		ModelIDMap mm = new ModelIDMap();
		sentenceMap.forEach((k, v) -> mm.put(JASSFactory.createJASSModel(k), v));
		return mm;
	}

	public StatementIDMap convertMap_Model2Statements(ModelIDMap modelMap) {
		Map<Model, List<Statement>> replaceMap = modelMap.keySet().stream()
				.collect(Collectors.toMap(m -> m, m -> m.listStatements().toList()));
		return modelMap.replaceModel2Statements(replaceMap);
	}

	/**
	 * JenaのModelを独自クラスRDFTripleのリストに置き換える.
	 * @param model JenaのModel
	 * @return RDFTripleのリスト
	 */
	public List<RDFTriple> convertModel_Jena2TripleList(ModelIDMap modelMap) {
		Model model = modelMap.uniteModels();
		List<RDFTriple> triples = new LinkedList<>();
		StmtIterator stmtIter = model.listStatements();
		while (stmtIter.hasNext()) {
			Statement stmt = stmtIter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			RDFTriple triple = new RDFTriple(
					new MyResource(subject),
					new MyResource(predicate),
					new MyResource(object));
			triples.add(triple);
		}
		return triples;
	}

	/**
	 * JASSモデルからオントロジーに変換する.
	 * @param JASSMap
	 * @return
	 */
	public ModelIDMap convertMap_JASSModel2RDFModel(ModelIDMap JASSMap) {
		ModelIDMap ontologyMap = new ModelIDMap();
		// 拡張
		JASSMap.forEachKey(extensionRules::expand);
		// 変換
		for (Map.Entry<Model, IDTuple> e : JASSMap.entrySet()) {
			Model convertingModel = e.getKey();
			IDTuple idt = e.getValue();
			Map<Model, Integer> modelsWithRuleID = ontologyRules.convert(convertingModel);
			for (Map.Entry<Model, Integer> e2 : modelsWithRuleID.entrySet()) {
				Model convertedModel = e2.getKey();
				int ruleID = e2.getValue();
				IDTuple idt_clone = idt.clone();
				idt_clone.setRDFRuleID(ruleID);
				ontologyMap.put(convertedModel, idt_clone);
			}
		}
		return ontologyMap;
	}

	/****************************************/
	/**********   Getter, Setter   **********/
	/****************************************/
	public RDFRules getExtensionRules() {
		return extensionRules;
	}
	public RDFRules getOntologyRules() {
		return ontologyRules;
	}
}