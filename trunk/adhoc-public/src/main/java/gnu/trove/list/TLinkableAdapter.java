package gnu.trove.list;

/**
 * Simple adapter class implementing {@link TLinkable}, so you don't have to. Example:
 * <pre>
	private class MyObject extends TLinkableAdapter<MyObject> {
		private final String value;

		MyObject( String value ) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
 * </pre>
 */
public abstract class TLinkableAdapter<T extends TLinkable> implements TLinkable<T> {
	private volatile T next;
	private volatile T prev;

	@Override
	public T getNext() {
		return next;
	}

	@Override
	public void setNext( T next ) {
		this.next = next;
	}

	@Override
	public T getPrevious() {
		return prev;
	}

	@Override
	public void setPrevious( T prev ) {
		this.prev = prev;
	}
}
