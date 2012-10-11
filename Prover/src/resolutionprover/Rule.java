package resolutionprover;

public enum Rule {

	NEGNEG("double negation rule"),
	ALPHA1("alpha rule (alpha1)"),
	ALPHA2("alpha rule (alpha2)"),
	BETA("beta rule"),
	NOT_FALSE("not false rule"),
	NOT_TRUE("not true rule"),
	AXIOM("axiom"),
	CONJECTURE("conjecture"),
	RESOLUTION("resolution rule");
	
	public final String humanReadable;

	private Rule(String humanReadable) {
		this.humanReadable = humanReadable;
	}
}
