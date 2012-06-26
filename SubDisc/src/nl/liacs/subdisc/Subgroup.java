package nl.liacs.subdisc;

import java.util.BitSet;

/**
 * A Subgroup contains a number of instances from the original data. Subgroups
 * are formed by, a number of, {@link Condition Condition}s. Its members include
 * : a {@link ConditionList ConditionList}, a BitSet representing the instances
 * included in this Subgroup, the number of instances in this Subgroup (its
 * coverage), a unique identifier, and the value used to form this Subgroup. It
 * may also contain a {@link DAG DAG}, and a {@link SubgroupSet SubgroupSet}.
 * @see Condition
 * @see ConditionList
 * @see DAG
 * @see nl.liacs.subdisc.gui.MiningWindow
 * @see SubgroupDiscovery
 * @see SubgroupSet
 * @see Condition
 */
public class Subgroup implements Comparable<Subgroup>
{
	private ConditionList itsConditions;
	private BitSet itsMembers;
	private int itsID = 0;
	private int itsCoverage; // crucial to keep it in sync with itsMembers
	private DAG itsDAG;
	private double itsMeasureValue;
	private double itsSecondaryStatistic = 0;
	private double itsTertiaryStatistic = 0;
	int itsDepth;
	private final SubgroupSet itsParentSet;
	// XXX not strictly need by setting itsPValue to NaN
	private boolean isPValueComputed;
	private double itsPValue;
	private String itsRegressionModel;

	/*
	 * In case no SubgroupSet is provided an empty one is created, this avoids
	 * extra checks in for example getFalsePositiveRate().
	 * This constructor is called by SubgroupDiscovery.Mine(long) and
	 * Table.getRandomSubgroups(int);
	 * TODO theMeasureValue = valid, theCoverage > 0, theDepth > 0
	 */
	/**
	 * Creates a Subgroup.
	 * @param theMeasureValue the value used to create this Subgroup.
	 * @param theCoverage the number of instances contained in this Subgroup.
	 * @param theDepth
	 * @param theSubgroupSet the SubgroupSet this Subgroup is contained in.
	 */
	// TODO null checks/ merge with other constructor
	public Subgroup(double theMeasureValue, int theCoverage, int theDepth, SubgroupSet theSubgroupSet, BitSet theBitSet)
	{
		itsConditions = new ConditionList();
		itsMeasureValue = theMeasureValue;
		itsCoverage = theCoverage;
		itsMembers = theBitSet;
		itsDepth = theDepth;
		itsDAG = null;	//not set yet
		itsParentSet = (theSubgroupSet == null ? new SubgroupSet(0) : theSubgroupSet);
		isPValueComputed = false;
	}

	/*
	 * Most of subgroups' members for which no parameter is supplied are still
	 * set, this avoids extra checks in for example getFalsePositiveRate().
	 * This constructor is called by Validation#RandomConditions(int).
	 * TODO theDepth > 0
	 */
	 /**
	 * Creates a Subgroup, but for the Bayesian setting.
	 * @param theConditions
	 * @param theMembers
	 * @param theDepth
	 */
	// TODO null checks/ merge with other constructor
	public Subgroup(ConditionList theConditions, BitSet theMembers, int theDepth)
	{
		itsConditions = (theConditions == null ? new ConditionList() : theConditions);	// TODO warning
		itsMeasureValue = 0.0f;
		itsMembers = (theMembers == null ? new BitSet(0) : theMembers);	// TODO warning
		itsCoverage = theMembers.cardinality();
		itsDepth = theDepth;
		itsDAG = null;	//not set yet
		itsParentSet = new SubgroupSet(0);
		isPValueComputed = false;
	}

	// significant speedup in mining algorithm
	// use old_subgroup.members and update it for new Condition
	public void addCondition(Condition theCondition)
	{
		if (theCondition == null)
		{
			Log.logCommandLine("Subgroup.addCondition(): argument can not be 'null', no Condition added.");
			return;
		}

		itsConditions.addCondition(theCondition);

		itsMembers.and(theCondition.getColumn().evaluate(theCondition));
		// crucial to keep it in sync with itsMembers
		itsCoverage = itsMembers.cardinality();

		++itsDepth;
	}

	// itsMeasureValue, itsCoverage, itsDepth are primitive types, no need
	// to deep-copy
	// itsParentSet must not be deep-copied
	// see remarks for ConditionList/ Condition, which are not true complete
	// deep-copies, but in current code this is no problem
	// itsMembers is deep-copied
	public Subgroup copy()
	{
		Subgroup aReturn = new Subgroup(itsMeasureValue, itsCoverage, itsDepth, itsParentSet, (BitSet) itsMembers.clone());
		aReturn.itsConditions = itsConditions.copy();
		aReturn.itsSecondaryStatistic = itsSecondaryStatistic;
		aReturn.itsTertiaryStatistic = itsTertiaryStatistic;
		return aReturn;
	}

	public void print()
	{
		Log.logCommandLine("conditions: " + itsConditions.toString());
		Log.logCommandLine("bitset: " + itsMembers.toString());
	}

	public String toString()
	{
		return itsConditions.toString();
	}

	/**
	 * Most callers should not want to modify the returned
	 * {@link BitSet BitSet}.
	 *
	 * @return a BitSet representing this Subgroups members.
	 */
	// TODO return clone is feasible.
	public BitSet getMembers() { return itsMembers; }

	public boolean covers(int theRow) { return itsMembers.get(theRow); }

	public int getID() { return itsID; }
	public void setID(int theID) { itsID = theID; }

	public double getMeasureValue() { return itsMeasureValue; }
	public void setMeasureValue(double theMeasureValue) { itsMeasureValue = theMeasureValue; }
	public double getSecondaryStatistic() { return itsSecondaryStatistic; }
	public void setSecondaryStatistic(double theSecondaryStatistic) { itsSecondaryStatistic = theSecondaryStatistic; }
	public double getTertiaryStatistic() { return itsTertiaryStatistic; }
	public void setTertiaryStatistic(double theTertiaryStatistic) { itsTertiaryStatistic = theTertiaryStatistic; }

	public void setDAG(DAG theDAG) { itsDAG = theDAG; }
	public DAG getDAG() { return itsDAG; }

	public int getCoverage() { return itsCoverage; }

	public ConditionList getConditions() { return itsConditions; }
	public int getNrConditions() { return itsConditions.size(); }

	public int getDepth() { return itsDepth; }

	// NOTE Map interface expects compareTo and equals to be consistent.
	@Override
	public int compareTo(Subgroup theSubgroup)
	{
		// why not throw NullPointerException if theCondition is null?
		if (theSubgroup == null)
			return 1;
		else if (getMeasureValue() > theSubgroup.getMeasureValue())
			return -1;
		else if (getMeasureValue() < theSubgroup.getMeasureValue())
			return 1;
		else if (getCoverage() > theSubgroup.getCoverage())
			return -1;
		else if (getCoverage() < theSubgroup.getCoverage())
			return 1;
		else
		{
			int aTest = itsConditions.compareTo(theSubgroup.itsConditions);
			if (aTest != 0)
				return aTest;
		}

		return 0;
//		return itsMembers.equals(s.itsMembers);
	}

/*	@Override
	public int compareTo(Subgroup theSubgroup)
	{
		int aTest = itsConditions.compareTo(theSubgroup.itsConditions);
		return aTest;
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
	// used to determine TP/FP
	public SubgroupSet getParentSet()
	{
		return itsParentSet;
	}

	// TODO not safe for divide by ZERO
	/**
	 * Returns the TruePositiveRate for this Subgroup.
	 * If no itsParentSet was set for this SubGroup, or no itsBinaryTarget was
	 * set for this SubGroups' itsParentSet this function returns 0.0f.
	 * @return the TruePositiveRate, also known as TPR.
	 */
	public Float getTruePositiveRate()
	{
		BitSet tmp = itsParentSet.getBinaryTargetClone();

		if (tmp == null)
			return 0.0f;
		else
		{
			tmp.and(itsMembers);

			float aTotalTargetCoverage = itsParentSet.getTotalTargetCoverage();

			if (aTotalTargetCoverage <= 0)
				return 0.0f;
			else
				return tmp.cardinality() / aTotalTargetCoverage;
			// tmp.cardinality() = aHeadBody
		}
	}

	// TODO not safe for divide by ZERO
	/**
	 * Returns the FalsePositiveRate for this Subgroup.
	 * If no itsParentSet was set for this subgroup, or no itsBinaryTarget was
	 * set for this subgroups' itsParentSet this function returns 0.0f.
	 * @return the FalsePositiveRate, also known as FPR.
	 */
	public Float getFalsePositiveRate()
	{
		BitSet tmp = itsParentSet.getBinaryTargetClone();

		if (tmp == null)
			return 0.0f;
		else
		{
			tmp.and(itsMembers);

			int aTotalCoverage = itsParentSet.getTotalCoverage();
			float aTotalTargetCoverage = itsParentSet.getTotalTargetCoverage();
			float aBody = (itsParentSet.getTotalCoverage() -
							itsParentSet.getTotalTargetCoverage());

			// something is wrong
			if (aTotalCoverage <= 0 || aTotalTargetCoverage < 0 ||
				aTotalCoverage < aTotalTargetCoverage || aBody <= 0)
				return 0.0f;
			else
				// tmp.cardinality() = aHeadBody
				return (itsCoverage - tmp.cardinality()) / aBody;
		}
	}

	public double getPValue()
	{
		return (isPValueComputed ? itsPValue : Double.NaN);
	}

	public void setPValue(NormalDistribution theDistro)
	{
		isPValueComputed = true;
		itsPValue = 1 - theDistro.calcCDF(itsMeasureValue);
	}

	public void setEmpiricalPValue(double[] theQualities)
	{
		isPValueComputed = true;
		int aLength = theQualities.length;
		double aP = 0.0;
		for (int i=0; i<aLength; i++)
		{
			if (theQualities[i]>=itsMeasureValue)
				aP++;
		}
		itsPValue = aP/aLength;
	}

	public void renouncePValue()
	{
		isPValueComputed = false;
	}

	public String getRegressionModel() { return itsRegressionModel; }
	public void setRegressionModel(String theModel) { itsRegressionModel = theModel; }
}
