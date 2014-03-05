package gnu.trove.strategy;

/**
 * A {@link gnu.trove.strategy.HashingStrategy} that does identity comparisons
 * (<tt>==</tt>) and uses {@link System#identityHashCode(Object)} for hashCode generation.
 */
public class IdentityHashingStrategy<K> implements HashingStrategy<K> {
	static final long serialVersionUID = -5188534454583764904L;


    /**
     * A single instance that can be shared with multiple collections.
     * This instance is thread safe.
     */
    public static final IdentityHashingStrategy<Object> INSTANCE =
        new IdentityHashingStrategy<Object>();


	public int computeHashCode( K object ) {
		return System.identityHashCode( object );
	}

	public boolean equals( K o1, K o2 ) {
		return o1 == o2;
	}
}
