package resolution_prover;
import java.util.Collection;
import java.util.Iterator;


public class Util {

	public static <E> String collectionToString(Collection<E> collection) {
		if (collection.isEmpty())
			return "";
		StringBuilder builder = new StringBuilder();
		Iterator<E> iterator = collection.iterator();
		builder.append(iterator.next());
		for (int i = 1; i < collection.size(); i++) {
			if (i == collection.size() - 1)
				builder.append(" and ");
			else
				builder.append(", ");
			builder.append(iterator.next());
		}
		return builder.toString();
	}

	public static int calculateCommentIndent(Collection<Disjunction> disjunctions) {
		int maxLength = 0;
		for (Disjunction disjunction : disjunctions) {
			int length = disjunction.formulae.toString().length();
			if (length > maxLength)
				maxLength = length;
		}
		return maxLength + 2;
	}
}
