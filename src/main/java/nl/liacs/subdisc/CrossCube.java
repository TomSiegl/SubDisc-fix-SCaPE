package nl.liacs.subdisc;

import java.util.*;

/**
 * Implements an <i>n</i>-dimensional cube of counts.
 */
// TODO document overflow in case of (theDimensions>31)
public class CrossCube extends DataCube
{
	// protected and not final, why?
	protected int itsCounts[];
	protected int itsTotalCount;
	protected int itsDimensions;

	public CrossCube(int theDimensions)
	{
		int aPower = (int)Math.pow(2, theDimensions);
		setSize(aPower);
		itsDimensions = theDimensions;
		itsCounts = new int[aPower];
	}

	final void incrementCount(BitSet theBitSet) { incrementCount(getIndex(theBitSet)); }

	private final void incrementCount(int theIndex)
	{
		if(theIndex >= itsSize)
			Log.error("out of bounds");
		else
		{
			itsCounts[theIndex]++;
			itsTotalCount++;
		}
	}

	private final int getIndex(BitSet theBitSet)
	{
		int anIndex = 0;

		for(int i=0; i<itsDimensions; i++)
			if(theBitSet.get(i))
				anIndex += (int)Math.pow(2, itsDimensions-i-1);

		return anIndex;
	}

	// NOTE SubgroupSet.postProcessGetCoverRedundancyAndJointEntropy(k)
	// TODO rewrite to reduce rounding errors
	public double getEntropy()
	{
		double anEntropy = 0.0;

		if (itsTotalCount == 0)
			return 0.0;

		for (int i=0; i<itsSize; i++)
		{
			double aFraction = itsCounts[i]/(double)itsTotalCount;
			if (aFraction > 0.0)
				anEntropy += (-aFraction * Math.log(aFraction) / Math.log(2.0));
		}

		return anEntropy;
	}

	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	///// start of obsolete code                                           /////
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	@Deprecated
	public void setCount(BitSet theBitSet, int theCount) { setCount(getIndex(theBitSet), theCount); }

	@Deprecated
	public void setCount(int theIndex, int theCount)
	{
		if(theIndex >= itsSize)
			Log.error("out of bounds");
		else
		{
			int aCount = itsCounts[theIndex]; //save old count in case not 0;
			itsCounts[theIndex] = theCount;
			itsTotalCount += theCount - aCount;
		}
	}

	@Deprecated
	public int getCount(BitSet theBitSet) { return getCount(getIndex(theBitSet)); }

	//computes coverage of itemsets containing only 1 item
	@Deprecated
	public double getExclusiveCoverage()
	{
		double aCoverage = 0;

		if(itsTotalCount == 0)
			return 0;

		for(int i=0; i<itsDimensions; i++)
			aCoverage += itsCounts[(int)Math.pow(2, i)];

		return aCoverage;
	}

	@Deprecated
	public final int getCount(int theIndex) { return itsCounts[theIndex]; }

	//computes overlap of all itemsets
	@Deprecated
	public int getOverlap()
	{
		return itsCounts[itsCounts.length-1]; //last cell represents all 1's
	}

	@Deprecated
	public BitSet getBitSet(int theIndex)
	{
		BitSet aBitSet = new BitSet(itsDimensions);
		int anIndex = theIndex;

		for(int i=itsDimensions - 1; i >= 0; i--)
			if(anIndex >= (int) Math.pow(2, i))
			{
				aBitSet.set(itsDimensions - i - 1);
				anIndex -= (int) Math.pow(2, i);
			}

		return aBitSet;
	}

	@Deprecated
	public int getDimensions() { return itsDimensions; }

	@Deprecated
	public int getTotalCount() { return itsTotalCount; }

	// Riggelsen 2006
	// page 27, 3.15
	//Bayesian Dirichlet Equivalent Uniform
	@Deprecated
	public double getBDeu()
	{
		double aQuality = 0.0;

		if(itsTotalCount == 0)
			return 0;

		int q_i = getSize() / 2;
		double alpha_ijk = 1 / (double) getSize();
		double alpha_ij  = 1 / (double) q_i;
		double LogGam_alpha_ijk = Function.logGamma(alpha_ijk); //uniform prior BDeu metric
		double LogGam_alpha_ij  = Function.logGamma(alpha_ij);

		for (int j=0; j<q_i; j++)
		{
			double aSum  = 0.0;
			double aPost = 0.0;

			//child = 0;
			aPost += Function.logGamma(alpha_ijk + getCount(j*2)) - LogGam_alpha_ijk;
			aSum  += getCount(j*2);
			//child = 1;
			aPost += Function.logGamma(alpha_ijk + getCount(j*2 + 1)) - LogGam_alpha_ijk;
			aSum  += getCount(j*2 + 1);

			aQuality += LogGam_alpha_ij - Function.logGamma(alpha_ij + aSum) + aPost;
		}

		return aQuality;
	}
}
