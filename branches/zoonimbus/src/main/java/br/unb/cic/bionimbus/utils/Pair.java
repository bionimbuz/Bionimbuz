package br.unb.cic.bionimbus.utils;

public class Pair<F,S> {
	
	public final F first;
	public final S second;
	
	public Pair(F first, S second){
		this.first = first;
		this.second = second;
	}

	public Pair() {
		this(null, null);
	}
	
	public static <F,S> Pair<F,S> of(F first, S second){
		return new Pair<F,S>(first, second);
	}
	
	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}

	public boolean equals(Object object){
		if (this == object)
			return true;
		
		if (!(object instanceof Pair)){
			return false;
		}
		
		@SuppressWarnings("unchecked")
		Pair<F,S> other = (Pair<F,S>) object;
		
		return first.equals(other.first) && second.equals(other.second);
	}

}
