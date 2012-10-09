package resolution_prover;

public enum Rule {

	NEGNEG("negneg"),
	ALPHA1("alpha1"),
	ALPHA2("alpha2"),
	BETA("beta"),
	NOT_FALSE("not false rule"),
	NOT_TRUE("not true rule"),
	RESOLUTION("resolution");
	
	public final String humanReadable;

	private Rule(String humanReadable) {
		this.humanReadable = humanReadable;
	}
}
