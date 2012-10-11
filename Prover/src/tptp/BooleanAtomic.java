package tptp;

public class BooleanAtomic extends Formula implements TptpParserOutput.AtomicFormula {

	public static final BooleanAtomic TRUE = new BooleanAtomic(true);
	public static final BooleanAtomic FALSE = new BooleanAtomic(false);
	
	public boolean value;

	private BooleanAtomic(boolean value) {
		_kind = Kind.Boolean;
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		BooleanAtomic other = (BooleanAtomic) obj;
		if (value != other.value)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return value ? "$true" : "$false";
	}
}
