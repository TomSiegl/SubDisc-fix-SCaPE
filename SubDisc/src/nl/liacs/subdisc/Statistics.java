package nl.liacs.subdisc;

/*
 * Class intended to replace the current usage of a float array for obtaining various statistics about the subgroup (and its complement)
 */
public class Statistics
{
	private int itsCoverage;
	private int itsComplementCoverage;
	private float itsSubgroupSum = Float.NaN;
	private float itsSubgroupSumSquaredDeviations = Float.NaN;
	private float itsMedian = Float.NaN;
	private float itsMedianAbsoluteDeviations = Float.NaN;

	//complement
	private float itsComplementSum = Float.NaN;
	private float itsComplementSumSquaredDeviations = Float.NaN;

	//entire data
	//not necessary for coverage (=data size=itsCoverage+itsComplementCoverage) and sum (=itsSubgroupSum+itsComplementSum)
	private float itsSumSquaredDeviations = Float.NaN;

	//empty statistics object
	Statistics()
	{
	}

	Statistics(int theCoverage, float theSum, float theSumSquaredDeviations)
	{
		itsCoverage = theCoverage;
		itsSubgroupSum = theSum;
		itsSubgroupSumSquaredDeviations = theSumSquaredDeviations;
	}

	Statistics(int theCoverage, float theSum, float theSumSquaredDeviations, float theMedian, float theMedianAbsoluteDeviations)
	{
		itsCoverage = theCoverage;
		itsSubgroupSum = theSum;
		itsSubgroupSumSquaredDeviations = theSumSquaredDeviations;
		itsMedian = theMedian;
		itsMedianAbsoluteDeviations = theMedianAbsoluteDeviations;
	}

	public void addComplement(int theComplementCoverage, float theSum, float theSumSquaredDeviations)
	{
		itsComplementCoverage = theComplementCoverage;
		itsComplementSum = theSum;
		itsComplementSumSquaredDeviations = theSumSquaredDeviations;
	}

	public void addDatasetSSD(float theSumSquaredDeviations)
	{
		itsSumSquaredDeviations = theSumSquaredDeviations;
	}

	public int getCoverage() { return itsCoverage; }
	public float getSubgroupAverage() { return itsSubgroupSum/itsCoverage; }
	public float getSubgroupStandardDeviation() { return (float) Math.sqrt(itsSubgroupSumSquaredDeviations/(double)itsCoverage); }
	public float getSubgroupSum() { return itsSubgroupSum; }
	public float getSubgroupSumSquaredDeviations() { return itsSubgroupSumSquaredDeviations; }
	public float getMedian() { return itsMedian; }
	public float getMedianAbsoluteDeviations() { return itsMedianAbsoluteDeviations; }

	//complement
	public int getComplementCoverage() { return itsComplementCoverage; }
	public float getComplementSum() { return itsComplementSum; }
	public float getComplementSumSquaredDeviations() { return itsComplementSumSquaredDeviations; }
	public float getComplementAverage() { return itsComplementSum/itsComplementCoverage; }
	public float getComplementStandardDeviation() { return (float) Math.sqrt(itsComplementSumSquaredDeviations/(double)itsComplementCoverage); }

	//entire data
	public float getAverage() { return (itsSubgroupSum+itsComplementSum)/(itsCoverage+itsComplementCoverage); } //average of all data
	public float getSumSquaredDeviations() { return itsSumSquaredDeviations; }

	public void print() 
	{
		Log.logCommandLine("total: " + getAverage() + ", " + Math.sqrt(itsSumSquaredDeviations/(double)(itsCoverage+itsComplementCoverage)));
		Log.logCommandLine("subgroup (" + getCoverage() + "): " + getSubgroupAverage() + ", " + Math.sqrt(itsSubgroupSumSquaredDeviations/(double)itsCoverage));
		Log.logCommandLine("complement (" + getComplementCoverage() + "): " + getComplementAverage() + ", " + 
				   Math.sqrt(itsComplementSumSquaredDeviations/(double)itsComplementCoverage));
	}
}