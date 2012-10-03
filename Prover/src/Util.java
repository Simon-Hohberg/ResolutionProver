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
}
