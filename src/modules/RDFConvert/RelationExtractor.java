package modules.RDFConvert;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import data.RDF.MyResource;
import data.RDF.RDFTriple;
import data.RDF.rule.RDFRule;
import data.RDF.rule.RDFRuleReader;
import data.RDF.rule.RDFRules;
import data.id.IDTuple;
import data.id.ModelIDMap;
import data.id.SentenceIDMap;
import data.id.StatementIDMap;

public class RelationExtractor {

	/**
	 * 標準のJASSオントロジー
	 */
	public final Model defaultJASSModel;

	/**
	 * 拡張ルール
	 */
	private final RDFRules extensionRules;
	/**
	 * オントロジー変換ルール
	 */
	private final RDFRules ontologyRules;

	/* ================================================== */
	/* ==========          Constructor         ========== */
	/* ================================================== */
	public RelationExtractor(RDFRules extensionRules, RDFRules ontologyRules, Model jassModel) {
		this.extensionRules = extensionRules;
		this.ontologyRules = ontologyRules;
		this.defaultJASSModel = jassModel;
	}
	public RelationExtractor(Path extensionRulePath, Path ontologyRulePath, String jassModelURL) {
		this(RDFRuleReader.readRDFRules(extensionRulePath),
				RDFRuleReader.readRDFRules(ontologyRulePath),
				ModelFactory.createDefaultModel().read(jassModelURL)
				);
	}

	/* ================================================== */
	/* ==========        Member  Method        ========== */
	/* ================================================== */
	/**
	 * {@code Sentence}のIDMapから{@code Model}(JASS) のIDMapに変換する.
	 * @param sentenceMap
	 * @return
	 */
	public ModelIDMap convertMap_Sentence2JASSModel(SentenceIDMap sentenceMap) {
		ModelIDMap modelIDMap = new ModelIDMap();
		sentenceMap.forEach((stc, id) -> {
			Model model = ModelFactory.createDefaultModel().add(defaultJASSModel);
			modelIDMap.put(stc.toRDF(model).getModel(), id);
		});
		return modelIDMap;
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
	public List<RDFTriple> convertModel_Jena2TripleList(Model model) {
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
		JASSMap.forEachKey(this::extendsJASSModel);
		// 変換
		for (Map.Entry<Model, IDTuple> e : JASSMap.entrySet()) {
			Model convertingModel = e.getKey();
			IDTuple idt = e.getValue();
			Map<Model, RDFRule> modelsWithRuleID = convertsJASSModel(convertingModel);
			for (Map.Entry<Model, RDFRule> e2 : modelsWithRuleID.entrySet()) {
				Model convertedModel = e2.getKey();
				int ruleID = e2.getValue().id();
				IDTuple idt_clone = idt.clone();
				idt_clone.setRDFRuleID(String.valueOf(ruleID));
				ontologyMap.put(convertedModel, idt_clone);
			}
		}
		return ontologyMap;
	}

	/* クエリ解決用 */
	private Model extendsJASSModel(Model jass) {
		extensionRules.forEach(r -> jass.add(solveConstructQuery(jass, r)));
		return jass;
	}
	private Map<Model, RDFRule> convertsJASSModel(Model jass) {
		return ontologyRules.stream()
				.collect(Collectors.toMap(
						r -> solveConstructQuery(jass, r),
						r -> r
						));
	}
	private Model solveConstructQuery(Model model, RDFRule rule) {
		return QueryExecutionFactory.create(createQuery(rule), model).execConstruct();
	}
	private Query createQuery(RDFRule rule) {
		return QueryFactory.create(createPrefixes4Query() + rule.writeQuery());
	}
	private String createPrefixes4Query() {
		return defaultJASSModel.getNsPrefixMap().entrySet().parallelStream()
				.map(e -> "PREFIX "+ e.getKey() +": <"+ e.getValue() +">")
				.collect(Collectors.joining(" "));
	}

	/* ================================================== */
	/* ==========            Getter            ========== */
	/* ================================================== */
	public RDFRules getExtensionRules() {
		return extensionRules;
	}
	public RDFRules getOntologyRules() {
		return ontologyRules;
	}
}