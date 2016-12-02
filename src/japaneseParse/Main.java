package japaneseParse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

public class Main {

	public static void main(String[] args) {
		List<String> writings = new ArrayList<String>();
		String writing1 = "クジラは広い海を泳いでいる。";	
		String writing2 = "鯨とは水生の巨大な哺乳類である。";
		//String sample = "辞書とは多数の語を集録し、一定の順序に配列して一つの集合体として、個々の語の意味・用法、またはその示す内容について記したもの。";
		//String sample = "夏目漱石はこころを執筆した。";
		String writing3 = "アースカラーは大地のような褐色や、空・海の青色、草木の緑色など";
		//writings.addAll(Arrays.asList(writing1,writing2,writing3));
		
		/*** Collecting Entries ***/
		/* 外部ファイルから日本語テキストを読み込む */
		String readFile = "writings/gooText生物-動物名-さ.txt";
		File file = new File(readFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while(line != null){
				writings.add(line);
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<List<String>> relations = new ArrayList<List<String>>();
		for(String writing: writings) {
			/*** Syntactic Parsing Module ***/
			System.out.println("\n\t Step0");
			Parser parse = new Parser("cabocha");
			Sentence sent = parse.run(writing);
			sent.printC();
			
			/*** Semantic Parsing Module ***/
			/** Step1: Term Extraction **/
			/* 名詞と形容詞だけ取り出す */
			//System.out.println("\n\t Step1");
			String[][] tagNP = {{"形容詞"}, {"連体詞"}, {"助詞", "連体化"}, {"助動詞", "体言接続"}}; //これらを含むChunkを係り受け先につなげる
			List<Integer> wordList_NP = new ArrayList<Integer>(sent.collectTagWords(tagNP)); // 上記のtagを持つWordを集めた
			
			for(int word_NP: wordList_NP) {
				Word wd = Word.get(word_NP);
				System.out.print(word_NP + "@(C"+wd.inChunk+"):" + wd.wordName + "|");
			}
			System.out.println();
			 		
			
			/** Step2: Concatenation **/
			/* 名詞と名詞または形容詞と名詞をつなげて1つの名詞句にする */
			//System.out.println("\n\t Step2");
			sent = sent.concatenate(wordList_NP);
			sent.printS();
			
			/** Step3: Break Phrases **/
			/* 長文を分割し複数の短文に分ける */
			//System.out.println("\n\t Step3");
			// 未実装
			
			/** Step4: Relations Extraction **/
			/* 単語間の関係を見つけ，グラフにする(各単語及び関係性をNodeのインスタンスとする) */
			//System.out.println("\n\t Step4");
			List<List<String>> relation = sent.extractRelation(); 
			relations.addAll(relation);
		}
		
		// 得られた関係を読み取り，uriとtriplesに入れる
		List<String> uri = new ArrayList<String>();	// ここに入れた単語はWordとしての情報を失いその後は文字列として扱う
		uri.add("rdf:type");
		uri.add("rdfs:subClassOf");
		uri.add("rdfs:subPropertyOf");
		uri.add("rdfs:domain");
		uri.add("rdfs:range");
		
		List<List<Integer>> triples = new ArrayList<List<Integer>>();
		for(final List<String> relation: relations) {
        	System.out.println(relation);
        	List<Integer> triple_id = new ArrayList<Integer>(3);
        	for (int i=0; i < 3; i++) {
        		String concept = relation.get(i);
        		if( !uri.contains(concept) ) uri.add(concept);
        		triple_id.add(uri.indexOf(concept));
        	}
        	triples.add(triple_id);
        }
        
		//List<Node> nodes = Node.setTriples2Nodes(uri, triples);
		/*
		for(int i=0; i<uri.size(); i++) {
			System.out.println(i + ":\t" + uri.get(i));
			nodes.get(i).printNode1();
			nodes.get(i).printNode2();
		}
		*/
	// ここまでMainに詰め込みすぎ．何らかのクラスのメソッドにしましょう
		
		/*** OWL DL Axiom Module ***/
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MMdd_HHmm");
		OntologyBuilder ob = new OntologyBuilder();		
		ob.writeOntology(uri, triples);
		ob.output("owls/ontology"+sdf.format(c.getTime())+".owl");	// 渡すのは保存先のパス
	}

}