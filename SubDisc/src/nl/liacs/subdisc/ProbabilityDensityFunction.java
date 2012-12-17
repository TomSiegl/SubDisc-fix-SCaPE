package nl.liacs.subdisc;

import java.util.*;

public class ProbabilityDensityFunction
{
	private float[] itsDensity;
	private float itsMin, itsMax, itsBinWidth;
	private int itsNrBins = 1000;

	//create from entire dataset
	public ProbabilityDensityFunction(Column theData)
	{
		//TODO include outlier treatment
		itsMin = theData.getMin();
		itsMax = theData.getMax();
		itsBinWidth = (itsMax-itsMin)/itsNrBins;
		itsDensity = new float[itsNrBins];

		int aSize = theData.size();
		for (int i=0; i<aSize; i++)
		{
			float aValue = theData.getFloat(i)/theData.size(); //the total sum should be 1.0000
			add(aValue);
		}
	}

	//create for subgroup, relative to existing PDF
	public ProbabilityDensityFunction(ProbabilityDensityFunction thePDF, Column theData, BitSet theMembers)
	{
		itsMin = thePDF.itsMin;
		itsMax = thePDF.itsMax;
		itsBinWidth = thePDF.itsBinWidth;
		itsDensity = new float[itsNrBins];

		int aMembers = theMembers.cardinality();
		int aSize = theData.size();
		for (int i = theMembers.nextSetBit(0), j = -1; i >= 0; i = theMembers.nextSetBit(i + 1))
		{
			float aValue = theData.getFloat(i)/aMembers;
			add(aValue);
		}
	}

	public float getDensity(float theValue)
	{
		int anIndex = getIndex(theValue);
		return itsDensity[anIndex];
	}

	public float getDensity(int theIndex)
	{
		return itsDensity[theIndex];
	}

	private int getIndex(float aValue)
	{
		if (aValue == itsMax)
			return itsNrBins-1;
		else
			return (int) ((aValue-itsMin)/itsBinWidth);
	}

	private void add(float aValue)
	{
		int aBin = getIndex(aValue);
		itsDensity[aBin]++;
	}

	public void print()
	{
		Log.logCommandLine("ProbabilityDensityFunction:\n");
		for (int i = 0; i < itsDensity.length; i++)
			Log.logCommandLine("  " + i + "	" + itsDensity[i]);
		Log.logCommandLine("");
	}

	public int size()
	{
		return itsNrBins;
	}
}
