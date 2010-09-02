package nl.liacs.subdisc;

import java.util.BitSet;

/**
 * A Subgroup contains a number of instances form the original data. Subgroups
 * are formed by, a number of, {@link Condition Condition}s. Its members include
 * : a {@link ConditionList ConditionList}, a BitSet representing the instances
 * included in this Subgroup, the number of instances in this Subgroup (its
 * coverage), a unique identifier, and the value used to form this Subgroup. It
 * may also contain a {@link DAG DAG}, and a {@link SubgroupSet SubgroupSet}.
 * @see Condition
 * @see ConditionList
 * @see DAG
 * @see MiningWindow
 * @see SubgroupDiscovery
 * @see SubgroupSet
 * @see Condition
 */
public class Subgroup implements Comparable<Object>
{
	private ConditionList itsConditions;
	private BitSet itsMembers;
	private int itsID = 0;
	private int itsCoverage;
	private DAG itsDAG;
	private double itsMeasureValue;
	int itsDepth;
	private final SubgroupSet itsParentSet;

	/*
	 *  TODO null check ConditionList
	 * In case no SubgroupSet is provided an empty one is created, this avoids
	 * extra checks in for example getFalsePositiveRate().
	 */
	/**
	 * Creates a Subgroup, this constructor is called by
	 * {@link SubgroupDiscovery#Mine() Mine} and
	 * {@link MiningWindow#jButtonRandomSubgroupsActionPerformed()
	 * jButtonRandomSubgroupsActionPerformed}.
	 * @param theMeasureValue the value used to create this Subgroup.
	 * @param theCoverage the number of instances contained in this Subgroup.
	 * @param theDepth
	 * @param theSubgroupSet the SubgroupSet this Subgroup is contained in.
	 */
	public Subgroup(double theMeasureValue, int theCoverage, int theDepth, SubgroupSet theSubgroupSet)
	{
		itsConditions = new ConditionList();
		itsMeasureValue = theMeasureValue;
		itsCoverage = theCoverage;
		itsMembers = new BitSet(1000);
		itsDepth = theDepth;
		itsDAG = null;	//not set yet
		itsParentSet = (theSubgroupSet == null ? new SubgroupSet(0) : theSubgroupSet);
	}

	/*
	 * TODO null check theConditions
	 * Most of subgroups' members for which no parameter is supplied are still
	 * set, this avoids extra checks in for example getFalsePositiveRate().
	 */
	 /**
	 * Creates a Subgroup, this constructor is called by
	 * {@link MiningWindow#jButtonRandomConditionsActionPerformed()
	 * jButtonRandomConditionsActionPerformed}.
	 * @param theConditions
	 * @param theMembers
	 * @param theDepth
	 */
	public Subgroup(ConditionList theConditions, BitSet theMembers, int theDepth)
	{
		itsConditions = (theConditions == null ? new ConditionList() : theConditions);
		itsMeasureValue = 0.0f;
		itsMembers = (theMembers == null ? new BitSet(0) : theMembers);
		itsCoverage = theMembers.cardinality();
		itsDepth = theDepth;
		itsDAG = null;	//not set yet
		itsParentSet = new SubgroupSet(0);
	}

	public void addCondition(Condition theCondition)
	{
		itsConditions.addCondition(theCondition);
		itsDepth++;
	}

	//TODO: check correctheid van diepe copy
	public Object copy()
	{
		Subgroup aReturn = new Subgroup(itsMeasureValue, itsCoverage, itsDepth, itsParentSet);
		aReturn.itsConditions = itsConditions.copy();
		aReturn.itsMembers = (BitSet) itsMembers.clone();
		return (Object)aReturn;
	}

	public void print()
	{
		Log.logCommandLine("conditions: " + itsConditions.toString());
		Log.logCommandLine("bitset: " + itsMembers.toString());
	}

	public BitSet getMembers() { return itsMembers; }

	public void setMembers(BitSet theMembers)
	{
		itsMembers = theMembers;
		itsCoverage = theMembers.cardinality();
	}

	public int getID() { return itsID; }
	public void setID(int theID) { itsID = theID; }

	public double getMeasureValue() { return itsMeasureValue; }
	public void setMeasureValue(double theMeasureValue) { itsMeasureValue = theMeasureValue; }

	public void setDAG(DAG theDAG) { itsDAG = theDAG; }
	public DAG getDAG() { return itsDAG; }

	public int getCoverage() { return itsCoverage; }

	public ConditionList getConditions() { return itsConditions;  }
	public int getNrConditions() { return itsConditions.size();  }

	public int getDepth() { return itsDepth; }

	public int compareTo(Object o)
	{
		if(!(o instanceof Subgroup) || (o == null))
			return 1;

		Subgroup s = (Subgroup) o;
		
		if(getMeasureValue() > s.getMeasureValue())
			return -1;
		else if(getMeasureValue() < s.getMeasureValue())
			return 1;
		else if(getCoverage() > s.getCoverage())
			return -1;
		else if(getCoverage() < s.getCoverage())
			return 1;
		else if(itsConditions.itsConditions.size() > s.itsConditions.itsConditions.size())
			return -1;
		else if(itsConditions.itsConditions.size() < s.itsConditions.itsConditions.size())
			return 1;

		for(Condition c : itsConditions.itsConditions)
		{
			boolean hasSameAttributeAndOperator = false;
			for(Condition sc : s.itsConditions.itsConditions)
			{
				if(c.getAttribute().getName().equals(sc.getAttribute().getName()) &&
						c.getOperatorString().equals(sc.getOperatorString()))
				{
					hasSameAttributeAndOperator = true;
					break;
				}
			}
			if(!hasSameAttributeAndOperator)
				return 1;	// TODO arbitrary, could have been -1 also
		}

		return 0;
//		return itsMembers.equals(s.itsMembers);
	}
/*
	public int compareTo(Object theObject)
	{
		Subgroup A = this;
		Subgroup B = (Subgroup)theObject;	// TODO null safe?

		if (A.getMeasureValue() > B.getMeasureValue())
			return -1;
		else
			if (A.getMeasureValue() < B.getMeasureValue())
				return 1;
			else
				if (A.getCoverage() > B.getCoverage())
					return -1;
				else
					if (A.getCoverage() < B.getCoverage())
						return 1;
							else
								return -1; // TODO set to -1, 0 does not work as expected
	}
*/
	/**
	 * NOTE For now this equals implementation is only used for the ROCList
	 * HashSet implementation.
	 * Two subgroups are considered equal if:
	 * for each condition(Attribute-Operator pair) in this.conditionList there
	 * is a matching condition(Attribute-Operator pair) in other.conditionList
	 * and both itsMembers are equal.
	 */
/*
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Subgroup))
			return false;

		Subgroup s = (Subgroup) o;

		for(Condition c : itsConditions.itsConditions)
		{
			boolean hasSameAttributeAndOperator = false;
			for(Condition sc : s.itsConditions.itsConditions)
			{
				if(c.getAttribute().getName().equalsIgnoreCase(sc.getAttribute().getName()) &&
						c.getOperatorString().equalsIgnoreCase(sc.getOperatorString()))
				{
					hasSameAttributeAndOperator = true;
					System.out.println(this.getID()+ " " + s.getID());
					this.print();
					s.print();
					break;
				}
			}
			if(!hasSameAttributeAndOperator)
				return false;
		}

		return itsMembers.equals(s.itsMembers);
		//getTruePositiveRate().equals(s.getTruePositiveRate()) &&
			//	getFalsePositiveRate().equals(s.getFalsePositiveRate());
	}
*/
	/**
	 * TODO Even for the SubgroupSet.getROCList code this is NOT enough.
	 * All subgroups are from the same SubgroupSet/ experiment with the same target.
	 * However, two subgroups formed from different Attributes in itsConditions
	 * should be considered unequal. This requires an @Override from itsConditions
	 * hashCode(), as it should not include condition values. 
	 * Eg. two subgroups that have the same members and are formed from:
	 * (x < 10) and (x < 11) should be considered equal
	 * (y < 10) and (x < 10) should be considered different 
	 */
/*
	@Override
	public int hashCode()
	{
		int hashCode = 0;
		for(Condition c : itsConditions.itsConditions)
			hashCode += (c.getAttribute().getName().hashCode() + c.getOperatorString().hashCode());
		return 31*itsMembers.hashCode() + hashCode;
	}
*/
	// TODO not safe for divide by ZERO
	/**
	 * Returns the TruePositiveRate for this Subgroup.
	 * If no itsParentSet was set for this subgroup, or no itsBinaryTarget was
	 * set for this subgroups' itsParentSet this function return 0.0f.
	 * @return the TruePositiveRate, also known as TPR.
	 */
	public Float getTruePositiveRate()
	{
		BitSet tmp = itsParentSet.getBinaryTarget();
		tmp.and(getMembers());
		int aHeadBody = tmp.cardinality();

		return aHeadBody / itsParentSet.getTotalTargetCoverage();
	}

	// TODO not safe for divide by ZERO
	/**
	 * Returns the FalsePositiveRate for this Subgroup.
	 * If no itsParentSet was set for this subgroup, or no itsBinaryTarget was
	 * set for this subgroups' itsParentSet this function return 0.0f.
	 * @return the FalsePositiveRate, also known as FPR.
	 */
	public Float getFalsePositiveRate()
	{
		BitSet tmp = itsParentSet.getBinaryTarget();
		tmp.and(getMembers());
		int aHeadBody = tmp.cardinality();

		return (getCoverage() - aHeadBody) / (itsParentSet.getTotalCoverage() - itsParentSet.getTotalTargetCoverage());
	}
}
