package br.unb.cic.bionimbus.p2p;

import java.math.BigInteger;


public final class ID implements Comparable<ID> {

	private final BigInteger id;

	private static final BigInteger TWO = new BigInteger("2");

	public ID(BigInteger value) {
		this.id = value;
	}

	public ID(int value) {
		this.id = new BigInteger("" + value);
	}

	public boolean gt(ID other) {
		return id.compareTo(other.id) > 0;
	}

	public boolean lt(ID other) {
		return id.compareTo(other.id) < 0;
	}

	public boolean eq(ID other) {
		return id.compareTo(other.id) == 0;
	}

	public boolean gte(ID other) {
		return gt(other)||eq(other);
	}

	public boolean lte(ID other) {
		return lt(other)||eq(other);
	}

	public ID add(ID other) {
		return new ID(this.id.add(other.id));
	}

	// base 2
	public static ID pow(int exponent) {
		return new ID(TWO.pow(exponent));
	}

	public ID mod(int exponent) {
		return new ID(id.mod(TWO.pow(exponent)));
	}

	@Override
	public String toString() {
		//TODO: return to 16
		return id.toString(10);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (!(object instanceof ID)){
			return false;
		}

		ID other = (ID) object;

		return this.id.equals(other.id);
	}

	@Override
	public int compareTo(ID other) {
		return id.compareTo(other.id);
	}

}
