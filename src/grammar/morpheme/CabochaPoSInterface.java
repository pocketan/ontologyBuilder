package grammar.morpheme;

public interface CabochaPoSInterface {
	int MINIMUM_TAGS_SIZE = 7;
	int MAXIMUM_TAGS_SIZE = 9;

	/** 品詞 */
	String mainPoS();
	/** 品詞細分類1 */
	String subPoS1();
	/** 品詞細分類2 */
	String subPoS2();
	/** 品詞細分類3 */
	String subPoS3();
	/** 活用形 */
	String inflection();
	/** 活用型 */
	String conjugation();
	/** 原形 */
	String infinitive();
	/** 読み */
	String kana();
	/** 発音 */
	String pronunciation();

}