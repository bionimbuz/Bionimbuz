package br.unb.cic.bionimbuz.services.sched.policy.impl;

public class CppSimpleRating extends CppSched{
	protected  String GetSchedPolicy(){
		return "SIMPLE_RATING_SCHED";
	}
	@Override
	public String getPolicyName() {
        return "Name: "+ CppSimpleRating.class.getSimpleName()";
	}
}
